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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class EasyReader {

    private val easyReaderInventory by lazy { MutableStateFlow(EasyReaderInventory()) }
    private val inventoryFlow by lazy { MutableSharedFlow<Pair<String, Int>>() }

    private var easyReaderSettings: EasyReaderSettings? = null

    private val companyPrefixes: MutableList<String> by lazy { mutableListOf() }
    private val searchTags: MutableList<String> by lazy { mutableListOf() }

    private var searchTag: String = EMPTY_STRING
    private var epcFound: Boolean = false
    private var startStop: Boolean = true

    private var readerMode: ReaderModeEnum = ReaderModeEnum.RFID_MODE
    private var readMode: ReadModeEnum = ReadModeEnum.NOTIFY_BY_ITEM_READ
    private var readType: ReadTypeEnum = ReadTypeEnum.INVENTORY
    private var coder: CoderEnum = CoderEnum.SGTIN

    val observer by lazy { EasyReaderObserver() }

    var readerModeEnum: ReaderModeEnum
        get() = readerMode
        set(value) { readerMode = value }
    var readModeEnum: ReadModeEnum
        get() = readMode
        set(value) { readMode = value }
    var readTypeEnum: ReadTypeEnum
        get() = readType
        set(value) { readType = value }
    var coderEnum: CoderEnum
        get() = coder
        set(value) { coder = value }
    var prefixes: List<String>
        get() = companyPrefixes
        set(value) { companyPrefixes.addAll(value) }

    abstract fun intiRead()
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
        CoroutineScope(Dispatchers.IO).launch { observer.update(arg) }
        handleReportedArg(arg)
    }

    private fun handleReportedArg(arg: Any?) {
        when {
            arg is EasyResponse -> {
                when (arg.code) {
                    CONNECTION_SUCCEEDED -> addReaderSubscription()
                    CONNECTION_CLOSE, CONNECTION_FAILED -> removeReaderSubscription()
                }
            }

            arg is StartStopReading && readMode.isNotifyWhenStopped() -> {
                when {
                    readType == ReadTypeEnum.INVENTORY && arg.startStop != true ->
                        notifyObservers(easyReaderInventory.value)

                    readType == ReadTypeEnum.HIGH_READING && arg.startStop != true->
                        observeInventory()
                }
            }
        }
    }

    private fun addReaderSubscription() {
        CoroutineScope(Dispatchers.IO).launch {
            inventoryFlow.map { (epc, rssi) ->
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
                throw IllegalArgumentException("An error has occurred while reading.")
            }.collect { notifyObservers(it) }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun removeReaderSubscription() {
        inventoryFlow.resetReplayCache()
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
            CoderEnum.SGTIN -> (ParseSGTIN.Builder().withEPCTag(epc).build() as ParseSGTIN).getSGTIN()
            CoderEnum.GRAI -> (ParseGRAI.Builder().withEPCTag(epc).build() as ParseGRAI).getGRAI()
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

    protected fun notifySettingsChange(
        lastSettings: SettingsEnum? = null,
        power: AntennaPowerLevelsEnum? = null,
        beeperLevel: Int? = null,
        session: SessionControlEnum? = null
    ) {
        easyReaderSettings?.let { settings ->
            settings.lastSettingChanged = lastSettings
            settings.antennaPower = power
            settings.antennaSound = beeperLevel
            settings.sessionControl = session
            notifyObservers(settings)
        }
    }

    fun notifyItemRead(epc: String, rssi: Int) {
        inventoryFlow.tryEmit(epc to rssi)
    }
}