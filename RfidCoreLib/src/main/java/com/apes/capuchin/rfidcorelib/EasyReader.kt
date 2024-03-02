package com.apes.capuchin.rfidcorelib

import com.apes.capuchin.rfidcorelib.enums.AntennaPowerLevelsEnum
import com.apes.capuchin.rfidcorelib.enums.BeeperLevelsEnum
import com.apes.capuchin.rfidcorelib.enums.CoderEnum
import com.apes.capuchin.rfidcorelib.enums.ReadModeEnum
import com.apes.capuchin.rfidcorelib.enums.ReadTypeEnum
import com.apes.capuchin.rfidcorelib.enums.ReaderModeEnum
import com.apes.capuchin.rfidcorelib.enums.SessionControlEnum
import com.apes.capuchin.rfidcorelib.enums.SettingsEnum
import com.apes.capuchin.rfidcorelib.epctagcoder.parse.grai.ParseGRAI
import com.apes.capuchin.rfidcorelib.epctagcoder.parse.sgtin.ParseSGTIN
import com.apes.capuchin.rfidcorelib.epctagcoder.result.BaseReading
import com.apes.capuchin.rfidcorelib.epctagcoder.result.NONE
import com.apes.capuchin.rfidcorelib.models.EasyReaderInventory
import com.apes.capuchin.rfidcorelib.models.EasyReaderSettings
import com.apes.capuchin.rfidcorelib.models.EasyResponse
import com.apes.capuchin.rfidcorelib.models.HighReading
import com.apes.capuchin.rfidcorelib.models.LocateTag
import com.apes.capuchin.rfidcorelib.models.StartStopReading
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import sun.jvm.hotspot.utilities.Observable

abstract class EasyReader : Observable() {

    private var easyReaderSettings: EasyReaderSettings? = null

    private val companyPrefixes: MutableList<String> by lazy { mutableListOf() }
    private val searchTags: MutableList<String> by lazy { mutableListOf() }

    private var searchTag: String = EMPTY_STRING
    private var epcFound: Boolean = false
    private var startStop: Boolean = true

    private var readerMode: ReaderModeEnum = ReaderModeEnum.RFID_MODE
    private var readMode: ReadModeEnum = ReadModeEnum.NOTIFY_BY_ITEM_READ
    private var readType: ReadTypeEnum = ReadTypeEnum.INVENTORY
    private val coder: CoderEnum = CoderEnum.SGTIN

    private val easyReaderInventory by lazy { MutableStateFlow(EasyReaderInventory()) }
    private val inventoryFlow by lazy { MutableStateFlow(ItemRead()) }

    abstract fun connectReader()

    abstract fun disconnectReader()

    abstract fun isReaderConnected()

    abstract fun intiRead()

    abstract fun stopRead()

    abstract fun setSessionControl()

    abstract fun getSessionControl()

    override fun notifyObservers() {
        super.notifyObservers()
        super.setChanged()
    }

    override fun notifyObservers(arg: Any?) {
        super.notifyObservers(arg)
        handleReportedArg(arg)
    }

    private fun handleReportedArg(arg: Any?) {
        when (arg) {
            is EasyResponse -> {
                when (arg.code) {
                    CONNECTION_SUCCEEDED -> addReaderSubscription()
                    CONNECTION_CLOSE -> removeReaderSubscription()
                }
            }

            is StartStopReading -> {
                when {
                    readType == ReadTypeEnum.INVENTORY && arg.startStop != true ->
                        notifyObservers(easyReaderInventory.value)

                    readType == ReadTypeEnum.HIGH_READING && arg.startStop != true -> observeInventory()
                }
            }
        }
    }

    private fun addReaderSubscription() {
        CoroutineScope(Dispatchers.IO).launch {
            inventoryFlow.collect {
                val inventory: Any? = when (readType) {
                    ReadTypeEnum.SEARCH_TAG -> LocateTag(search = it.epc, rssi = it.rssi?.toLong())
                    else -> {
                        val baseReading = getBaseReading(it.epc.orEmpty()).apply {
                            rssi = it.rssi
                        }
                        if (validateCompanyPrefix(baseReading)) setBaseReading(baseReading)
                        if (continueReading(easyReaderInventory.value)) easyReaderInventory.value else null
                    }
                }
                notifyObservers(inventory)
            }
        }
    }

    private fun removeReaderSubscription() {
        inventoryFlow.update { it.copy(epc = null, rssi = null) }
    }

    private fun observeInventory() {
        CoroutineScope(Dispatchers.IO).launch {
            easyReaderInventory.collect { inventory ->
                when {
                    !inventory.error.isNullOrEmpty() ->
                        throw IllegalArgumentException("An error has occurred while reading.")

                    inventory.itemsRead.isNotEmpty() -> {
                        val readings = inventory.itemsRead.sortedBy { it.rssi }
                        clearBuffer()
                        notifyObservers(HighReading(readings.first()))
                    }
                }
            }
        }
    }

    private fun continueReading(inventory: EasyReaderInventory): Boolean {
        return when (readMode) {
            ReadModeEnum.NOTIFY_BY_ITEM_READ -> inventory.itemsRead.isNotEmpty()
            else -> false
        }
    }

    private fun setBaseReading(baseReading: BaseReading) {
        easyReaderInventory.value.itemsRead.add(baseReading)
    }

    private fun getBaseReading(epc: String): BaseReading {
        return when (coder) {
            CoderEnum.SGTIN -> (ParseSGTIN.Builder().withRFIDTag(epc)
                .build() as ParseSGTIN).getSGTIN()

            CoderEnum.GRAI -> (ParseGRAI.Builder().withRFIDTag(epc).build() as ParseGRAI).getGRAI()

            else -> NONE(epc)
        }
    }

    private fun filterWithCompanyPrefix(companyPrefixList: List<String>) {
        if (companyPrefixList.isNotEmpty()) companyPrefixes.addAll(companyPrefixList)
    }

    private fun validateCompanyPrefix(baseReading: BaseReading): Boolean {
        val company = baseReading.companyPrefix.orEmpty()
        return when {
            companyPrefixes.isNotEmpty() -> companyPrefixes.contains(company)
            else -> true
        }
    }

    private fun clearBuffer() {
        easyReaderInventory.update { it.copy(itemsRead = mutableSetOf()) }
    }

    private fun clearSearch() {
        readType = ReadTypeEnum.INVENTORY
        epcFound = false
        searchTag = EMPTY_STRING
        searchTags.clear()
    }

    private fun notifySettingsChange(
        lastSettings: SettingsEnum,
        power: AntennaPowerLevelsEnum,
        beeperLevel: Int,
        session: SessionControlEnum
    ) {
        easyReaderSettings?.let { settings ->
            settings.lastSettingChanged = lastSettings
            settings.antennaPower = power
            settings.antennaSound = beeperLevel
            settings.sessionControl = session
            notifyObservers(settings)
        }
    }

    private fun notifyItemRead(epc: String, rssi: Int) {
        inventoryFlow.update { it.copy(epc = epc, rssi = rssi) }
    }

    data class ItemRead(val epc: String? = null, val rssi: Int? = null)
}