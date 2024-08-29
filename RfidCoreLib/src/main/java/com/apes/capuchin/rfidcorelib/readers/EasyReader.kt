package com.apes.capuchin.rfidcorelib.readers

import com.apes.capuchin.rfidcorelib.utils.CONNECTION_CLOSE_CODE
import com.apes.capuchin.rfidcorelib.utils.CONNECTION_FAILED_CODE
import com.apes.capuchin.rfidcorelib.utils.CONNECTION_SUCCEEDED_CODE
import com.apes.capuchin.rfidcorelib.utils.EMPTY_STRING
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
import com.apes.capuchin.rfidcorelib.models.EasyResponse
import com.apes.capuchin.rfidcorelib.models.HighReading
import com.apes.capuchin.rfidcorelib.models.LocateTag
import com.apes.capuchin.rfidcorelib.models.StartStopReading
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

    private var easyReaderSettings: EasyReaderSettings? = null

    val companyPrefixes: MutableList<String> by lazy { mutableListOf() }
    val searchTags: MutableList<String> by lazy { mutableListOf() }

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

    fun notifyObservers(arg: Any?) {
        handleReportedArg(arg)
    }

    private fun handleReportedArg(arg: Any?) {
        CoroutineScope(Dispatchers.IO).launch {
            update(arg)
            when {
                arg is EasyResponse ->
                    handleEasyResponse(arg)

                arg is StartStopReading && readMode.isNotifyWhenStopped() ->
                    handleStartStopReading(arg)
            }
        }.runCatching {
            throw IllegalArgumentException("An error has occurred while reading.")
        }
    }

    private suspend fun handleEasyResponse(arg: EasyResponse) {
        when (arg.code) {
            CONNECTION_SUCCEEDED_CODE -> addReaderSubscription()
            CONNECTION_CLOSE_CODE, CONNECTION_FAILED_CODE -> removeReaderSubscription()
        }
    }

    private fun handleStartStopReading(arg: StartStopReading) {
        when {
            readType == ReadTypeEnum.INVENTORY && arg.startStop != true ->
                notifyObservers(easyReaderInventory.value)

            readType == ReadTypeEnum.HIGH_READING && arg.startStop != true ->
                observeInventory()
        }
    }

    private suspend fun addReaderSubscription() {
        inventoryFlow.filter { (epc, rssi) -> epc.isNotEmpty() && rssi != 0 }
            .map { (epc, rssi) ->
                when (readType) {
                    ReadTypeEnum.SEARCH_TAG -> LocateTag(search = epc, rssi = rssi.toLong())
                    else -> {
                        var baseReading = getBaseReading(epc)
                        baseReading.rssi = rssi
                        when {
                            validateCompanyPrefix(baseReading) -> setBaseReading(baseReading)
                            else -> baseReading = NONE(epc)
                        }
                        baseReading
                    }
                }
            }.filter {
                when (readType) {
                    ReadTypeEnum.SEARCH_TAG -> true
                    else -> continueReading(easyReaderInventory.value)
                }
            }.catch {
                it.printStackTrace()
            }.collect { reading ->
                notifyObservers(reading)
            }
    }

    private fun removeReaderSubscription() {
        inventoryFlow.update { it.copy(first = "", second = 0) }
    }

    private fun observeInventory() {
        CoroutineScope(Dispatchers.IO).launch {
            easyReaderInventory.filter { it.itemsRead.isNotEmpty() }.map { inventory ->
                val readings = inventory.itemsRead.sortedByDescending { it.rssi }
                clearBuffer()
                readings
            }.catch {
                throw IllegalArgumentException("An error has occurred while reading.")
            }.collect { notifyObservers(HighReading(it.first())) }
        }
    }

    private fun getBaseReading(epc: String): BaseReading {
        return when (coder) {
            CoderEnum.SGTIN ->
                (ParseSGTIN.builder().withEPCTag(epc).build() as ParseSGTIN).getSGTIN()

            CoderEnum.GRAI ->
                (ParseGRAI.builder().withEPCTag(epc).build() as ParseGRAI).getGRAI()

            else -> NONE(epc)
        }
    }

    private fun validateCompanyPrefix(baseReading: BaseReading): Boolean {
        val company = baseReading.companyPrefix.orEmpty()
        return when {
            companyPrefixes.isNotEmpty() -> companyPrefixes.contains(company)
            else -> true
        }
    }

    private fun setBaseReading(baseReading: BaseReading) {
        easyReaderInventory.value.itemsRead.add(baseReading)
    }

    private fun continueReading(inventory: EasyReaderInventory): Boolean {
        return when (readMode) {
            ReadModeEnum.NOTIFY_BY_ITEM_READ -> inventory.itemsRead.isNotEmpty()
            else -> false
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

    fun notifySettingsChange(
        lastSettings: SettingsEnum? = null,
        power: AntennaPowerLevelsEnum? = null,
        beeperLevel: BeeperLevelsEnum? = null,
        session: SessionControlEnum? = null
    ) {
        easyReaderSettings?.let { settings ->
            settings.lastSettingChanged = lastSettings
            settings.antennaPower = power
            settings.antennaSound = beeperLevel
            settings.sessionControl = session
            notifyObservers(settings)
        }
        notifyObservers(easyReaderSettings)
    }

    fun notifyItemRead(epc: String, rssi: Int) {
        inventoryFlow.update { it.copy(first = epc, second = rssi) }
    }
}