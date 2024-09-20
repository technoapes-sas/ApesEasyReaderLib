package com.apes.capuchin.rfidcorelib.readers

import android.util.Log
import com.apes.capuchin.rfidcorelib.EasyReaderObserver
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
import com.apes.capuchin.rfidcorelib.models.HighReading
import com.apes.capuchin.rfidcorelib.models.LocateTag
import com.apes.capuchin.rfidcorelib.models.StartStopReading
import com.apes.capuchin.rfidcorelib.utils.EMPTY_STRING
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class EasyReader : EasyReaderObserver() {

    private val easyReaderInventory by lazy { MutableStateFlow(EasyReaderInventory()) }
    private val inventoryFlow by lazy { MutableStateFlow(Pair(first = "", second = 0)) }

    private val easyReaderSettings: EasyReaderSettings by lazy { EasyReaderSettings() }

    val companyPrefixes: MutableList<String> by lazy { mutableListOf() }

    var searchTag: String = EMPTY_STRING
    private var epcFound: Boolean = false

    var readerMode: ReaderModeEnum = ReaderModeEnum.RFID_MODE
    var readMode: ReadModeEnum = ReadModeEnum.NOTIFY_BY_ITEM_READ
    var readType: ReadTypeEnum = ReadTypeEnum.INVENTORY
    var coder: CoderEnum = CoderEnum.SGTIN

    abstract fun initRead()
    abstract fun stopRead()
    abstract fun initReader()
    abstract fun connectReader()
    abstract fun disconnectReader()
    abstract fun isReaderConnected(): Boolean
    abstract fun setSessionControl(sessionControlEnum: SessionControlEnum)
    abstract fun getSessionControl(): SessionControlEnum
    abstract fun setAntennaSound(beeperLevelsEnum: BeeperLevelsEnum)
    abstract fun getAntennaSound(): BeeperLevelsEnum
    abstract fun setAntennaPower(antennaPowerLevelsEnum: AntennaPowerLevelsEnum)
    abstract fun getAntennaPower(): AntennaPowerLevelsEnum

    init {
        addReaderSubscription()
    }

    protected fun notifyObservers(arg: Any?) {
        handleReportedArg(arg)
    }

    protected fun notifySettingsChange(
        lastSettings: SettingsEnum? = null,
        power: AntennaPowerLevelsEnum? = null,
        beeperLevel: BeeperLevelsEnum? = null,
        session: SessionControlEnum? = null
    ) {
        easyReaderSettings.apply {
            lastSettingChanged = lastSettings
            antennaPower = power
            antennaSound = beeperLevel
            sessionControl = session
        }
        notifyObservers(easyReaderSettings)
    }

    protected fun notifyItemRead(epc: String, rssi: Int) {
        inventoryFlow.update { Pair(first = epc, second = rssi) }
    }

    private fun handleReportedArg(arg: Any?) {
        CoroutineScope(Dispatchers.IO).launch {
            when {
                arg is StartStopReading && readMode.isNotifyWhenStopped() ->
                    handleReadWhenStopped()

                else ->
                    update(arg)
            }
        }.runCatching {
            throw IllegalArgumentException("An error has occurred while reading.")
        }
    }

    private fun handleReadWhenStopped() {
        when (readType) {
            ReadTypeEnum.SEARCH_TAG -> Unit

            ReadTypeEnum.HIGH_READING -> HighReading(
                easyReaderInventory.value.itemsRead.sortedByDescending { it.rssi }.first()
            )

            ReadTypeEnum.INVENTORY -> notifyObservers(easyReaderInventory.value)
        }
    }

    private fun addReaderSubscription() {
        CoroutineScope(Dispatchers.Default).launch {
            inventoryFlow.filter { (epc, rssi) -> epc.isNotEmpty() && rssi != 0 }
                .map { (epc, rssi) ->
                    when (readType) {
                        ReadTypeEnum.SEARCH_TAG -> LocateTag(search = epc, rssi = rssi.toLong())

                        else -> evaluateTag(epc, rssi)
                    }
                }.filter {
                    when (readType) {
                        ReadTypeEnum.SEARCH_TAG -> true

                        ReadTypeEnum.HIGH_READING, ReadTypeEnum.INVENTORY ->
                            continueReading(easyReaderInventory.value)
                    }
                }.catch {
                    Log.e("EasyReader", "An error has occurred while reading.", it)
                }.collect { reading ->

                    notifyObservers(reading)
                }
        }
    }

    private fun evaluateTag(epc: String, rssi: Int) {
        val baseReading = getBaseReading(epc)
        baseReading.rssi = rssi
        when {
            validateCompanyPrefix(baseReading) -> setBaseReading(baseReading)

            else -> NONE(epc)
        }
    }

    private fun getBaseReading(epc: String): BaseReading {
        return try {
            when (coder) {
                CoderEnum.SGTIN ->
                    (ParseSGTIN.builder().withEPCTag(epc).build() as ParseSGTIN).getSGTIN()

                CoderEnum.GRAI ->
                    (ParseGRAI.builder().withEPCTag(epc).build() as ParseGRAI).getGRAI()

                else -> NONE(epc)
            }
        } catch (e: IllegalArgumentException) {
            NONE(epc)
        }
    }

    private fun validateCompanyPrefix(baseReading: BaseReading): Boolean {
        val company = baseReading.companyPrefix.orEmpty()
        return when {
            companyPrefixes.isNotEmpty() -> companyPrefixes.contains(company)
            else -> true
        }
    }

    private fun setBaseReading(baseReading: BaseReading): EasyReaderInventory {
        return easyReaderInventory.value.also {
            it.itemsRead.add(baseReading)
        }
    }

    private fun continueReading(inventory: EasyReaderInventory): Boolean {
        return when (readMode) {
            ReadModeEnum.NOTIFY_BY_ITEM_READ -> inventory.itemsRead.isNotEmpty()
            else -> false
        }
    }

    fun clearBuffer() {
        easyReaderInventory.value.itemsRead.clear()
    }

    fun clearSearch() {
        readType = ReadTypeEnum.INVENTORY
        epcFound = false
        searchTag = EMPTY_STRING
    }
}