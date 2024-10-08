package com.apes.capuchin.rfidcorelib

import com.apes.capuchin.rfidcorelib.enums.AntennaPowerLevelsEnum
import com.apes.capuchin.rfidcorelib.enums.BeeperLevelsEnum
import com.apes.capuchin.rfidcorelib.enums.SessionControlEnum
import com.apes.capuchin.rfidcorelib.enums.SettingsEnum
import com.apes.capuchin.rfidcorelib.models.EasyReaderInventory
import com.apes.capuchin.rfidcorelib.models.EasyReaderSettings
import com.apes.capuchin.rfidcorelib.models.EasyResponse
import com.apes.capuchin.rfidcorelib.models.HighReading
import com.apes.capuchin.rfidcorelib.models.LocateTag
import com.apes.capuchin.rfidcorelib.models.StartStopReading
import com.apes.capuchin.rfidcorelib.utils.CONNECTION_CLOSE_CODE
import com.apes.capuchin.rfidcorelib.utils.CONNECTION_FAILED_CODE
import com.apes.capuchin.rfidcorelib.utils.CONNECTION_SUCCEEDED_CODE
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

abstract class EasyReaderObserver {

    private val _onReaderConnected = MutableSharedFlow<Boolean>()
    val onReaderConnected = _onReaderConnected.asSharedFlow()

    private val _onReaderConnectionFailed = MutableSharedFlow<EasyResponse>()
    val onReaderConnectionFailed = _onReaderConnectionFailed.asSharedFlow()

    private val _onReaderError = MutableSharedFlow<EasyResponse>()
    val onReaderError = _onReaderError.asSharedFlow()

    private val _onItemsRead = MutableSharedFlow<EasyReaderInventory>()
    val onItemsRead = _onItemsRead.asSharedFlow()

    private val _onAntennaPowerChanged = MutableSharedFlow<AntennaPowerLevelsEnum>()
    val onAntennaPowerChanged = _onAntennaPowerChanged.asSharedFlow()

    private val _onAntennaSoundChanged = MutableSharedFlow<BeeperLevelsEnum>()
    val onAntennaSoundChanged = _onAntennaSoundChanged.asSharedFlow()

    private val _onSessionControlChanged = MutableSharedFlow<SessionControlEnum>()
    val onSessionControlChanged = _onSessionControlChanged.asSharedFlow()

    private val _onLocateTag = MutableSharedFlow<LocateTag>()
    val onLocateTag = _onLocateTag.asSharedFlow()

    private val _onStartReading = MutableSharedFlow<StartStopReading>()
    val onStartReading = _onStartReading.asSharedFlow()

    suspend fun update(arg: Any?) {
        when (arg) {
            is EasyResponse -> handleEasyResponse(arg)
            is HighReading -> handleHighReading(arg)
            is EasyReaderInventory -> handleEasyReaderInventory(arg)
            is EasyReaderSettings -> handleEasyReaderSettings(arg)
            is LocateTag -> handleLocateTag(arg)
            is StartStopReading -> handleStartStopReading(arg)
            else -> Unit
        }
    }

    private suspend fun handleEasyResponse(arg: EasyResponse) {
        when (arg.success) {
            true -> when (arg.code) {
                CONNECTION_SUCCEEDED_CODE -> _onReaderConnected.emit(true)
                CONNECTION_CLOSE_CODE -> _onReaderConnected.emit(false)
            }

            else -> when (arg.code) {
                CONNECTION_FAILED_CODE -> _onReaderConnectionFailed.emit(arg)
                else -> _onReaderError.emit(arg)
            }
        }
    }

    private suspend fun handleHighReading(arg: HighReading) {
        _onItemsRead.emit(EasyReaderInventory(itemsRead = mutableSetOf(arg.highReading)))
    }

    private suspend fun handleEasyReaderInventory(arg: EasyReaderInventory) {
        _onItemsRead.emit(arg)
    }

    private suspend fun handleEasyReaderSettings(arg: EasyReaderSettings) {
        when (arg.lastSettingChanged) {
            SettingsEnum.CHANGE_ANTENNA_POWER ->
                arg.antennaPower?.let { _onAntennaPowerChanged.emit(it) }
            SettingsEnum.CHANGE_ANTENNA_SOUND ->
                arg.antennaSound?.let { _onAntennaSoundChanged.emit(it) }
            SettingsEnum.CHANGE_SESSION_CONTROL ->
                arg.sessionControl?.let { _onSessionControlChanged.emit(it) }
            else -> Unit
        }
    }

    private suspend fun handleLocateTag(arg: LocateTag) {
        _onLocateTag.emit(arg)
    }

    private suspend fun handleStartStopReading(arg: StartStopReading) {
        _onStartReading.emit(arg)
    }
}