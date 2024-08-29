package com.apes.capuchin.rfidcorelib.readers.zebra

import android.content.Context
import com.apes.capuchin.capuchinrfidlib.lib.R
import com.apes.capuchin.rfidcorelib.utils.CONNECTION_CLOSE_CODE
import com.apes.capuchin.rfidcorelib.utils.CONNECTION_SUCCEEDED_CODE
import com.apes.capuchin.rfidcorelib.readers.EasyReader
import com.apes.capuchin.rfidcorelib.utils.SUCCESS
import com.apes.capuchin.rfidcorelib.enums.AntennaPowerLevelsEnum
import com.apes.capuchin.rfidcorelib.enums.BeeperLevelsEnum
import com.apes.capuchin.rfidcorelib.enums.ReaderModeEnum
import com.apes.capuchin.rfidcorelib.enums.SessionControlEnum
import com.apes.capuchin.rfidcorelib.enums.SettingsEnum
import com.apes.capuchin.rfidcorelib.models.EasyResponse
import com.apes.capuchin.rfidcorelib.models.StartStopReading
import com.zebra.rfid.api3.HANDHELD_TRIGGER_EVENT_TYPE
import com.zebra.rfid.api3.RFIDReader
import com.zebra.rfid.api3.ReaderDevice
import com.zebra.rfid.api3.Readers
import com.zebra.rfid.api3.RfidEventsListener
import com.zebra.rfid.api3.RfidReadEvents
import com.zebra.rfid.api3.RfidStatusEvents
import com.zebra.rfid.api3.STATUS_EVENT_TYPE
import com.zebra.scannercontrol.DCSScannerInfo
import com.zebra.scannercontrol.FirmwareUpdateEvent
import com.zebra.scannercontrol.IDcsSdkApiDelegate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ZebraUhfReader(
    private val context: Context
) : EasyReader(), Readers.RFIDReaderEventHandler, IDcsSdkApiDelegate {

    var readers: ReadersManager = ReadersManager(context = context)
    var readerConfiguration: ReaderConfiguration = ReaderConfiguration()
    var scannerManager: ScannerManager = ScannerManager(context = context, this)

    private var device: ReaderDevice? = null

    private val readingObserver = object : RfidEventsListener {
        override fun eventReadNotify(events: RfidReadEvents?) {
            CoroutineScope(Dispatchers.IO).launch {
                events?.let {
                    val myTag = it.readEventData.tagData
                    notifyItemRead(epc = myTag.tagID, rssi = myTag.peakRSSI.toInt())
                }
            }
        }

        override fun eventStatusNotify(events: RfidStatusEvents?) {
            events?.let { handleStatusEvent(it) }
        }
    }

    var reader: RFIDReader? = null

    @Synchronized
    override fun connectReader() {
        readers.connectReader(null) { reader ->
            this.reader = reader
            configureReader()
            notifyObservers(EasyResponse(
                success = SUCCESS,
                message = context.getString(R.string.reader_connected),
                code = CONNECTION_SUCCEEDED_CODE
            ))
        }
    }

    override fun disconnectReader() {
        reader?.disconnect()
        reader = null
        notifyObservers(EasyResponse(
            success = SUCCESS,
            message = context.getString(R.string.disconnect_reader),
            code = CONNECTION_CLOSE_CODE
        ))
    }

    override fun isReaderConnected(): Boolean = reader?.isConnected ?: false

    override fun initRead() {
        when (readerMode) {
            ReaderModeEnum.BARCODE_MODE -> scannerManager.scanCode()
            ReaderModeEnum.RFID_MODE -> reader?.Actions?.Inventory?.perform()
        }
    }

    override fun stopRead() {
        reader?.Actions?.Inventory?.stop()
    }

    override fun initReader() {
        readers.initReader(this) {
            connectReader()
        }
    }

    override fun setSessionControl(sessionControlEnum: SessionControlEnum) {
        readerConfiguration.setSessionControl(reader, sessionControlEnum)
        notifySettingsChange(SettingsEnum.CHANGE_SESSION_CONTROL, session = sessionControlEnum)
    }

    override fun getSessionControl(): SessionControlEnum {
        return readerConfiguration.getSessionControl(reader) ?: SessionControlEnum.S0
    }

    override fun setAntennaSound(beeperLevelsEnum: BeeperLevelsEnum) {
        readerConfiguration.setAntennaSound(reader, beeperLevelsEnum)
        notifySettingsChange(SettingsEnum.CHANGE_ANTENNA_SOUND, beeperLevel = beeperLevelsEnum)
    }

    override fun getAntennaSound(): BeeperLevelsEnum {
        return readerConfiguration.getAntennaSound(reader) ?: BeeperLevelsEnum.MAX
    }

    override fun setAntennaPower(antennaPowerLevelsEnum: AntennaPowerLevelsEnum) {
        readerConfiguration.setAntennaPower(reader, antennaPowerLevelsEnum)
        notifySettingsChange(SettingsEnum.CHANGE_ANTENNA_POWER, power = antennaPowerLevelsEnum)
    }

    override fun getAntennaPower(): AntennaPowerLevelsEnum {
        return readerConfiguration.getAntennaPower(reader) ?: AntennaPowerLevelsEnum.MAX
    }

    override fun RFIDReaderAppeared(device: ReaderDevice?) {
        this.device = device
    }

    override fun RFIDReaderDisappeared(device: ReaderDevice?) {
        if (device?.name == reader?.hostName) {
            disconnectReader()
        }
    }

    override fun dcssdkEventBarcode(barcodeData: ByteArray?, barcodeType: Int, fromScannerID: Int) {
        barcodeData?.let {
            val response = String(it)
            notifyItemRead(epc = response, rssi = 0)
        }
    }

    override fun dcssdkEventScannerAppeared(p0: DCSScannerInfo?) = Unit

    override fun dcssdkEventScannerDisappeared(p0: Int) = Unit

    override fun dcssdkEventCommunicationSessionEstablished(p0: DCSScannerInfo?) = Unit

    override fun dcssdkEventCommunicationSessionTerminated(p0: Int) = Unit

    override fun dcssdkEventImage(p0: ByteArray?, p1: Int) = Unit

    override fun dcssdkEventVideo(p0: ByteArray?, p1: Int) = Unit

    override fun dcssdkEventBinaryData(p0: ByteArray?, p1: Int) = Unit

    override fun dcssdkEventFirmwareUpdate(p0: FirmwareUpdateEvent?) = Unit

    override fun dcssdkEventAuxScannerAppeared(p0: DCSScannerInfo?, p1: DCSScannerInfo?) = Unit

    private fun configureReader() {
        reader?.let {
            readerConfiguration.configureReader(it, readingObserver)
            scannerManager.setupScannerSDK(it)
        }
    }

    private fun handleStatusEvent(statusEventData: RfidStatusEvents) {
        when (statusEventData.StatusEventData.statusEventType) {
            STATUS_EVENT_TYPE.HANDHELD_TRIGGER_EVENT -> {
                val handheldEvent = statusEventData.StatusEventData.HandheldTriggerEventData.handheldEvent
                when (handheldEvent) {
                    HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_PRESSED -> {
                        CoroutineScope(Dispatchers.IO).launch {
                            initRead()
                            notifyObservers(StartStopReading(true))
                        }
                    }
                    HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_RELEASED -> {
                        CoroutineScope(Dispatchers.IO).launch {
                            stopRead()
                            notifyObservers(StartStopReading(false))
                        }
                    }
                }
            }
            STATUS_EVENT_TYPE.DISCONNECTION_EVENT -> disconnectReader()
        }
    }
}