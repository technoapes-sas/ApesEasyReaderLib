package com.apes.capuchin.capuchinrfidlib

import android.content.Context
import com.apes.capuchin.rfidcorelib.EasyReading
import com.apes.capuchin.rfidcorelib.enums.AntennaPowerLevelsEnum
import com.apes.capuchin.rfidcorelib.enums.BeeperLevelsEnum
import com.apes.capuchin.rfidcorelib.enums.CoderEnum
import com.apes.capuchin.rfidcorelib.enums.ReadModeEnum
import com.apes.capuchin.rfidcorelib.enums.ReadTypeEnum
import com.apes.capuchin.rfidcorelib.enums.ReaderModeEnum
import com.apes.capuchin.rfidcorelib.enums.SessionControlEnum
import com.apes.capuchin.rfidcorelib.models.ConfigReader
import com.apes.capuchin.rfidcorelib.models.ConnectionState
import com.apes.capuchin.rfidcorelib.models.ReadState
import com.apes.capuchin.rfidcorelib.models.ConfigState
import com.apes.capuchin.rfidcorelib.readers.EasyReader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReaderManager(private val context: Context) {

    private var reader: EasyReader? = null

    private val _connectionState: MutableStateFlow<ConnectionState> = MutableStateFlow(ConnectionState())
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _readState: MutableStateFlow<ReadState> = MutableStateFlow(ReadState())
    val readState: StateFlow<ReadState> = _readState.asStateFlow()

    private val _configState: MutableStateFlow<ConfigState> = MutableStateFlow(ConfigState())
    val configState: StateFlow<ConfigState> = _configState.asStateFlow()

    private val moduleConfigs: MutableMap<String, ConfigReader> = mutableMapOf()

    var readerMode: ReaderModeEnum
        get() = reader?.readerMode ?: ReaderModeEnum.RFID_MODE
        set(value) {
            reader?.readerMode = value
        }

    var readMode: ReadModeEnum
        get() = reader?.readMode ?: ReadModeEnum.NOTIFY_BY_ITEM_READ
        set(value) {
            reader?.readMode = value
        }

    var readType: ReadTypeEnum
        get() = reader?.readType ?: ReadTypeEnum.INVENTORY
        set(value) {
            reader?.readType = value
        }

    init {
        buildReader()
    }

    private fun buildReader() {
        val easyReading = EasyReading.Builder()
            .coder(CoderEnum.SGTIN)
            .readMode(ReadModeEnum.NOTIFY_BY_ITEM_READ)
            .readerMode(ReaderModeEnum.RFID_MODE)
            .readType(ReadTypeEnum.INVENTORY)
            .companyPrefixes(listOf())
            .build(context)

        reader = easyReading.easyReader
        reader?.let { observeReaderEvents(it) }
    }

    private fun <T> observeEvent(flow: SharedFlow<T>, action: (T) -> Unit) {
        CoroutineScope(Dispatchers.Main).launch {
            flow.collect { action(it) }
        }
    }

    private fun observeReaderEvents(easyReader: EasyReader) {

        with(easyReader) {

            observeEvent(onReaderConnected) { isConnected ->
                _connectionState.update { it.copy(isReaderConnected = isConnected) }
            }

            observeEvent(onReaderConnectionFailed) { response ->
                _connectionState.update {
                    it.copy(
                        isReaderConnected = false,
                        error = response
                    )
                }
            }

            observeEvent(onReaderError) { response ->
                _connectionState.update {
                    it.copy(
                        isReaderConnected = isReaderConnected(),
                        error = response
                    )
                }
            }

            observeEvent(onItemsRead) { inventory ->
                _readState.update { it.copy(itemsRead = inventory.itemsRead.toSet()) }
            }

            observeEvent(onLocateTag) { locate ->
                _readState.update { it.copy(locateTag = locate) }
            }

            observeEvent(onStartReading) { startStop ->
                _readState.update { ReadState(isReading = startStop.startStop ?: false) }
            }

            observeEvent(onAntennaPowerChanged) { power ->
                _configState.update { ConfigState(antennaPower = power) }
            }

            observeEvent(onAntennaSoundChanged) { sound ->
                _configState.update { ConfigState(antennaSound = sound) }
            }

            observeEvent(onSessionControlChanged) { session ->
                _configState.update { ConfigState(sessionControl = session) }
            }
        }
    }

    private fun applyConfig(value: Any) {
        when (value) {
            is ReaderModeEnum -> readerMode = value
            is ReadModeEnum -> readMode = value
            is ReadTypeEnum -> readType = value
            is AntennaPowerLevelsEnum -> setAntennaPower(value)
            is SessionControlEnum -> setSessionControl(value)
            is BeeperLevelsEnum -> setAntennaSound(value)
        }
    }

    fun hasConfigByModule(key: String): Boolean {
        return moduleConfigs.containsKey(key)
    }

    fun getConfigByModule(key: String): ConfigReader? {
        return moduleConfigs[key]
    }

    fun applyConfigByModule(key: String) {
        moduleConfigs.filter { it.key == key }.values.forEach { applyConfig(it) }
    }

    fun configReader(key: String, config: ConfigReader) {
        when {
            moduleConfigs.containsKey(key) -> {
                config.readerMode?.let { moduleConfigs[key]?.readerMode = it }
                config.readMode?.let { moduleConfigs[key]?.readMode = it }
                config.readType?.let { moduleConfigs[key]?.readType = it }
                config.antennaPower?.let { moduleConfigs[key]?.antennaPower = it }
                config.sessionControl?.let { moduleConfigs[key]?.sessionControl = it }
                config.beeper?.let { moduleConfigs[key]?.beeper = it }
            }

            else -> moduleConfigs[key] = config
        }
        applyConfigByModule(key)
    }

    fun isReaderConnected(): Boolean {
        return reader?.isReaderConnected() ?: false
    }

    fun connectReader() {
        when {
            !isReaderConnected() -> {
                reader?.initReader()
            }
        }
    }

    fun disconnectReader() {
        when {
            isReaderConnected() -> {
                reader?.disconnectReader()
            }
        }
    }

    fun initRead() {
        reader?.initRead()
    }

    fun stopRead() {
        reader?.stopRead()
    }

    fun setSearchTag(tag: String) {
        reader?.searchTag = tag
        reader?.readType = ReadTypeEnum.SEARCH_TAG
    }

    fun clearSearchTag() {
        reader?.clearSearch()
    }

    fun clearBuffer() {
        reader?.clearBuffer()
    }

    fun setSessionControl(session: SessionControlEnum) {
        reader?.setSessionControl(session)
    }

    fun getSessionControl(): SessionControlEnum {
        return reader?.getSessionControl() ?: SessionControlEnum.UNKNOWN
    }

    fun setAntennaPower(power: AntennaPowerLevelsEnum) {
        reader?.setAntennaPower(power)
    }

    fun getAntennaPower(): AntennaPowerLevelsEnum {
        return reader?.getAntennaPower() ?: AntennaPowerLevelsEnum.UNKNOWN
    }

    fun setAntennaSound(beeper: BeeperLevelsEnum) {
        reader?.setAntennaSound(beeper)
    }

    fun getAntennaSound(): BeeperLevelsEnum {
        return reader?.getAntennaSound() ?: BeeperLevelsEnum.UNKNOWN
    }
}