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
import com.apes.capuchin.rfidcorelib.epctagcoder.result.BaseReading
import com.apes.capuchin.rfidcorelib.models.EasyResponse
import com.apes.capuchin.rfidcorelib.models.LocateTag
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

    private val _readerState: MutableStateFlow<ReaderState> =  MutableStateFlow(ReaderState())
    val readerState: StateFlow<ReaderState> = _readerState.asStateFlow()

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
                _readerState.update { ReaderState(isReaderConnected = isConnected) }
            }

            observeEvent(onReaderConnectionFailed) { response ->
                _readerState.update { ReaderState(error = response) }
            }

            observeEvent(onReaderError) { response ->
                _readerState.update { ReaderState(error = response) }
            }

            observeEvent(onItemsRead) { inventory ->
                _readerState.update { ReaderState(itemsRead = inventory.itemsRead.toSet()) }
            }

            observeEvent(onAntennaPowerChanged) { power ->
                _readerState.update { ReaderState(antennaPower = power) }
            }

            observeEvent(onAntennaSoundChanged) { sound ->
                _readerState.update { ReaderState(antennaSound = sound) }
            }

            observeEvent(onSessionControlChanged) { session ->
                _readerState.update { ReaderState(sessionControl = session) }
            }

            observeEvent(onLocateTag) { locate ->
                _readerState.update { ReaderState(locateTag = locate) }
            }

            observeEvent(onStartReading) { startStop ->
                _readerState.update { ReaderState(isReading = startStop.startStop ?: false) }
            }
        }
    }

    fun connectReader() {
        reader?.initReader()
    }

    fun disconnectReader() {
        reader?.disconnectReader()
    }

    fun isReaderConnected(): Boolean {
        return reader?.isReaderConnected() ?: false
    }

    fun configReader() {
        reader?.let {
            println("Configuring reader")
            it.readerMode = ReaderModeEnum.RFID_MODE
            it.readMode = ReadModeEnum.NOTIFY_BY_ITEM_READ
            it.readType = ReadTypeEnum.INVENTORY
            it.setAntennaPower(AntennaPowerLevelsEnum.MAX)
            it.setSessionControl(SessionControlEnum.S0)
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

    data class ReaderState(
        val isReaderConnected: Boolean = false,
        val isReading: Boolean = false,
        val itemsRead: Set<BaseReading>? = null,
        val locateTag: LocateTag? = null,
        val antennaPower: AntennaPowerLevelsEnum? = null,
        val antennaSound: BeeperLevelsEnum? = null,
        val sessionControl: SessionControlEnum? = null,
        val error: EasyResponse? = null
    )
}