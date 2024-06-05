package com.apes.capuchin.rfidcorelib.readers

import android.content.Context
import android.util.Log
import com.apes.capuchin.rfidcorelib.CONNECTION_CLOSE
import com.apes.capuchin.rfidcorelib.CONNECTION_SUCCEEDED
import com.apes.capuchin.rfidcorelib.EMPTY_STRING
import com.apes.capuchin.rfidcorelib.EasyReader
import com.apes.capuchin.rfidcorelib.enums.AntennaPowerLevelsEnum
import com.apes.capuchin.rfidcorelib.enums.BeeperLevelsEnum
import com.apes.capuchin.rfidcorelib.enums.ReaderModeEnum
import com.apes.capuchin.rfidcorelib.enums.SessionControlEnum
import com.apes.capuchin.rfidcorelib.enums.SettingsEnum
import com.apes.capuchin.rfidcorelib.models.EasyResponse
import com.apes.capuchin.rfidcorelib.models.StartStopReading
import com.zebra.rfid.api3.BEEPER_VOLUME
import com.zebra.rfid.api3.ENUM_TRANSPORT
import com.zebra.rfid.api3.ENUM_TRIGGER_MODE
import com.zebra.rfid.api3.HANDHELD_TRIGGER_EVENT_TYPE
import com.zebra.rfid.api3.INVENTORY_STATE
import com.zebra.rfid.api3.InvalidUsageException
import com.zebra.rfid.api3.OperationFailureException
import com.zebra.rfid.api3.RFIDReader
import com.zebra.rfid.api3.ReaderDevice
import com.zebra.rfid.api3.Readers
import com.zebra.rfid.api3.Readers.RFIDReaderEventHandler
import com.zebra.rfid.api3.RfidEventsListener
import com.zebra.rfid.api3.RfidReadEvents
import com.zebra.rfid.api3.RfidStatusEvents
import com.zebra.rfid.api3.SESSION
import com.zebra.rfid.api3.SL_FLAG
import com.zebra.rfid.api3.START_TRIGGER_TYPE
import com.zebra.rfid.api3.STATUS_EVENT_TYPE
import com.zebra.rfid.api3.STOP_TRIGGER_TYPE
import com.zebra.rfid.api3.TriggerInfo
import com.zebra.scannercontrol.DCSSDKDefs
import com.zebra.scannercontrol.DCSScannerInfo
import com.zebra.scannercontrol.FirmwareUpdateEvent
import com.zebra.scannercontrol.IDcsSdkApiDelegate
import com.zebra.scannercontrol.SDKHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ZebraReader(
    private val context: Context
) : EasyReader(), RFIDReaderEventHandler, IDcsSdkApiDelegate {

    //region variable
    private var readerName: String? = null

    private var readers: Readers? = null
    private var reader: RFIDReader? = null
    private var readerDevice: ReaderDevice? = null

    private var sdkHandler: SDKHandler? = null
    private val scannerList by lazy { mutableListOf<DCSScannerInfo>() }
    private var scannerId: Int = 0

    private var eventHandler = object : RfidEventsListener {
        override fun eventReadNotify(events: RfidReadEvents?) {
            CoroutineScope(Dispatchers.IO).launch {
                val myTags = reader?.Actions?.getReadTags(100).orEmpty()
                myTags.forEach {
                    notifyItemRead(epc = it.tagID, rssi = it.peakRSSI.toInt())
                }
            }
        }

        override fun eventStatusNotify(events: RfidStatusEvents?) {
            requireNotNull(events)
            val statusEvent = events.StatusEventData.statusEventType
            when (statusEvent) {
                STATUS_EVENT_TYPE.HANDHELD_TRIGGER_EVENT -> {
                    val handheldEvent =
                        events.StatusEventData.HandheldTriggerEventData.handheldEvent
                    when (handheldEvent) {
                        HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_PRESSED ->
                            CoroutineScope(Dispatchers.IO).launch {
                                initRead()
                                notifyObservers(StartStopReading(true))
                            }

                        HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_RELEASED ->
                            CoroutineScope(Dispatchers.IO).launch {
                                stopRead()
                                notifyObservers(StartStopReading(false))
                            }
                    }
                }

                STATUS_EVENT_TYPE.DISCONNECTION_EVENT -> disconnectReader()
            }
        }
    }

    private var availableRFIDReaderList: List<ReaderDevice> = emptyList()
    //endregion

    //region override parent methods
    @Synchronized
    override fun connectReader() {
        if (!isReaderConnected()) {
            connectionTask()
        }
    }

    @Synchronized
    override fun disconnectReader() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                reader?.let {
                    it.Events.removeEventsListener(eventHandler)
                    it.disconnect()
                }
                reader = null
                sdkHandler = null
                notifyObservers(
                    EasyResponse(
                        success = true,
                        message = "Reader disconnected",
                        code = CONNECTION_CLOSE
                    )
                )
            } catch (e: InvalidUsageException) {
                e.printStackTrace()
            } catch (e: OperationFailureException) {
                e.printStackTrace()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun isReaderConnected(): Boolean = reader?.isConnected ?: false

    @Synchronized
    override fun initRead() {
        try {
            when (readerModeEnum) {
                ReaderModeEnum.RFID_MODE -> reader?.Actions?.Inventory?.perform()
                ReaderModeEnum.BARCODE_MODE -> scanCode()
            }
        } catch (e: InvalidUsageException) {
            e.printStackTrace()
        } catch (e: OperationFailureException) {
            e.printStackTrace()
        }
    }

    @Synchronized
    override fun stopRead() {
        try {
            reader?.Actions?.Inventory?.stop()
        } catch (e: InvalidUsageException) {
            e.printStackTrace()
        } catch (e: OperationFailureException) {
            e.printStackTrace()
        }
    }

    override fun initReader() {
        readers?.let { connectReader() } ?: run { createInstanceTask() }
    }

    override fun setSessionControl(sessionControlEnum: SessionControlEnum) {
        reader?.let {
            val sessionValue = when (sessionControlEnum) {
                SessionControlEnum.S1 -> SESSION.SESSION_S1
                SessionControlEnum.S2 -> SESSION.SESSION_S2
                SessionControlEnum.S3 -> SESSION.SESSION_S3
                else -> SESSION.SESSION_S0
            }
            val config = it.Config.Antennas.getSingulationControl(1).apply {
                session = sessionValue
                Action.inventoryState = INVENTORY_STATE.INVENTORY_STATE_A
                Action.slFlag = SL_FLAG.SL_ALL
            }
            it.Config.Antennas.setSingulationControl(1, config)
            notifySettingsChange(
                lastSettings = SettingsEnum.CHANGE_SESSION_CONTROL,
                session = sessionControlEnum
            )
        }
    }

    override fun getSessionControl(): SessionControlEnum {
        var sessionValue: SessionControlEnum? = null
        reader?.let {
            val session = it.Config.Antennas.getSingulationControl(1).session
            sessionValue = when (session) {
                SESSION.SESSION_S1 -> SessionControlEnum.S1
                SESSION.SESSION_S2 -> SessionControlEnum.S2
                SESSION.SESSION_S3 -> SessionControlEnum.S3
                else -> SessionControlEnum.S0
            }
        }
        return sessionValue ?: SessionControlEnum.UNKNOWN
    }

    override fun setAntennaSound(beeperLevelsEnum: BeeperLevelsEnum) {
        reader?.let {
            it.Config.beeperVolume = when (beeperLevelsEnum) {
                BeeperLevelsEnum.MAX -> BEEPER_VOLUME.HIGH_BEEP
                BeeperLevelsEnum.MEDIUM -> BEEPER_VOLUME.MEDIUM_BEEP
                BeeperLevelsEnum.MIN -> BEEPER_VOLUME.LOW_BEEP
                else -> BEEPER_VOLUME.QUIET_BEEP
            }
            notifySettingsChange(
                lastSettings = SettingsEnum.CHANGE_ANTENNA_SOUND,
                beeperLevel = beeperLevelsEnum
            )
        }
    }

    override fun getAntennaSound(): BeeperLevelsEnum {
        var beeperLevel: BeeperLevelsEnum? = null
        reader?.let {
            beeperLevel = when (it.Config.beeperVolume) {
                BEEPER_VOLUME.HIGH_BEEP -> BeeperLevelsEnum.MAX
                BEEPER_VOLUME.MEDIUM_BEEP -> BeeperLevelsEnum.MEDIUM
                BEEPER_VOLUME.LOW_BEEP -> BeeperLevelsEnum.MIN
                else -> BeeperLevelsEnum.QUIET
            }
        }
        return beeperLevel ?: BeeperLevelsEnum.UNKNOWN
    }

    override fun setAntennaPower(antennaPowerLevelsEnum: AntennaPowerLevelsEnum) {
        reader?.let {
            val config = it.Config.Antennas.getAntennaRfConfig(1).apply {
                tari = 0
                transmitPowerIndex = when (antennaPowerLevelsEnum) {
                    AntennaPowerLevelsEnum.MAX -> 270
                    AntennaPowerLevelsEnum.MEDIUM -> 100
                    else -> 33
                }
                setrfModeTableIndex(0)
            }
            it.Config.Antennas.setAntennaRfConfig(1, config)
            notifySettingsChange(
                lastSettings = SettingsEnum.CHANGE_ANTENNA_POWER,
                power = antennaPowerLevelsEnum
            )
        }
    }

    override fun getAntennaPower(): AntennaPowerLevelsEnum {
        var antennaValue: AntennaPowerLevelsEnum? = null
        reader?.let {
            val power = it.Config.Antennas.getAntennaRfConfig(1).transmitPowerIndex
            antennaValue = when (power) {
                270 -> AntennaPowerLevelsEnum.MAX
                100 -> AntennaPowerLevelsEnum.MEDIUM
                else -> AntennaPowerLevelsEnum.MIN
            }
        }
        return antennaValue ?: AntennaPowerLevelsEnum.UNKNOWN
    }
    //endregion

    //region appeared events observer
    override fun RFIDReaderAppeared(devide: ReaderDevice?) {
        connectReader()
    }

    override fun RFIDReaderDisappeared(device: ReaderDevice?) {
        if (device?.name.orEmpty() == reader?.hostName.orEmpty()) {
            disconnectReader()
        }
    }
    //endregion

    //region barcode events observe
    override fun dcssdkEventScannerAppeared(p0: DCSScannerInfo?) = Unit

    override fun dcssdkEventScannerDisappeared(p0: Int) = Unit

    override fun dcssdkEventCommunicationSessionEstablished(p0: DCSScannerInfo?) = Unit

    override fun dcssdkEventCommunicationSessionTerminated(p0: Int) = Unit

    override fun dcssdkEventBarcode(barcodeData: ByteArray?, barcodeType: Int, fromScannerID: Int) {
        barcodeData?.let {
            val response = String(it)
            notifyItemRead(epc = response, rssi = 0)
        }
    }

    override fun dcssdkEventImage(p0: ByteArray?, p1: Int) = Unit

    override fun dcssdkEventVideo(p0: ByteArray?, p1: Int) = Unit

    override fun dcssdkEventBinaryData(p0: ByteArray?, p1: Int) = Unit

    override fun dcssdkEventFirmwareUpdate(p0: FirmwareUpdateEvent?) = Unit

    override fun dcssdkEventAuxScannerAppeared(p0: DCSScannerInfo?, p1: DCSScannerInfo?) = Unit
    //endregion

    //region dispose reader
    @Synchronized
    private fun dispose() {
        disconnectReader()
        try {
            if (reader != null) {
                reader = null
            }
            if (readers != null) {
                readers?.Dispose()
                readers = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    //endregion

    //region connect reader
    private fun createInstanceTask() {
        CoroutineScope(Dispatchers.IO).launch {
            var exception: InvalidUsageException? = null
            readers = Readers(context, ENUM_TRANSPORT.SERVICE_SERIAL)
            try {
                availableRFIDReaderList = readers?.GetAvailableRFIDReaderList().orEmpty().toList()
            } catch (e: InvalidUsageException) {
                exception = e
                exception.printStackTrace()
            }
            if (exception != null || availableRFIDReaderList.isEmpty()) {
                dispose()
                delay(600)
                readers = Readers(context, ENUM_TRANSPORT.BLUETOOTH)
            }
            readers?.let { connectReader() }
        }
    }

    private fun connectionTask() {
        CoroutineScope(Dispatchers.IO).launch {
            getAvailableReader()
            val result = when {
                reader != null -> connect()
                else -> "Failed to find or connect reader"
            }
            println(result)
        }
    }

    @Synchronized
    private fun getAvailableReader() {
        readers?.let {
            Readers.attach(this)
            try {
                if (readers?.GetAvailableRFIDReaderList().orEmpty().isNotEmpty()) {
                    availableRFIDReaderList = readers?.GetAvailableRFIDReaderList().orEmpty()
                    if (availableRFIDReaderList.isNotEmpty()) {
                        availableRFIDReaderList.forEach { device ->
                            if (device.name.startsWith(readerName.orEmpty())) {
                                readerDevice = device
                                reader = readerDevice?.rfidReader
                                return@forEach
                            }
                        }
                    }
                }
            } catch (e: InvalidUsageException) {
                e.printStackTrace()
            }
        }
    }

    @Synchronized
    private fun connect(): String {
        return try {
            when {
                !isReaderConnected() -> {
                    reader?.connect()
                    configureReader()
                    setupScannerSDK()
                    when {
                        isReaderConnected() -> {
                            notifyObservers(
                                EasyResponse(
                                    success = true,
                                    message = "Reader connected",
                                    code = CONNECTION_SUCCEEDED
                                )
                            )
                            "Connected: ${reader?.hostName.orEmpty()}"
                        }

                        else -> EMPTY_STRING
                    }
                }

                else -> EMPTY_STRING
            }
        } catch (e: InvalidUsageException) {
            e.printStackTrace()
            EMPTY_STRING
        } catch (e: NegativeArraySizeException) {
            e.printStackTrace()
            EMPTY_STRING
        } catch (e: OperationFailureException) {
            e.printStackTrace()
            "Connection failed ${e.vendorMessage} ${e.results}"
        }
    }
    //endregion

    //region configure reader
    private fun configureReader() {
        if (isReaderConnected()) {
            val triggerInfo = TriggerInfo().apply {
                StartTrigger.triggerType = START_TRIGGER_TYPE.START_TRIGGER_TYPE_IMMEDIATE
                StopTrigger.triggerType = STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_IMMEDIATE
            }
            try {
                reader?.Events?.addEventsListener(eventHandler)
                reader?.Events?.setHandheldEvent(true)
                reader?.Events?.setTagReadEvent(true)
                reader?.Events?.setAttachTagDataWithReadEvent(true)

                reader?.Config?.setTriggerMode(ENUM_TRIGGER_MODE.RFID_MODE, true)
                reader?.Config?.startTrigger = triggerInfo.StartTrigger
                reader?.Config?.stopTrigger = triggerInfo.StopTrigger

                setAntennaPower(AntennaPowerLevelsEnum.MAX)
                setSessionControl(SessionControlEnum.S0)

                reader?.Actions?.PreFilters?.deleteAll()
            } catch (e: InvalidUsageException) {
                e.printStackTrace()
            } catch (e: OperationFailureException) {
                e.printStackTrace()
            }
        }
    }

    private fun setupScannerSDK() {
        sdkHandler?.let {
            val availableScanners: List<DCSScannerInfo> = it.dcssdkGetAvailableScannersList()
            scannerList.clear()
            when {
                availableScanners.isNotEmpty() -> {
                    availableScanners.forEach { scanner ->
                        scannerList.add(scanner)
                    }
                }

                else -> Log.d("", "Available scanners null")
            }
        } ?: run {
            sdkHandler = SDKHandler(context)
            sdkHandler?.dcssdkSetOperationalMode(DCSSDKDefs.DCSSDK_MODE.DCSSDK_OPMODE_USB_CDC)
            sdkHandler?.dcssdkSetOperationalMode(DCSSDKDefs.DCSSDK_MODE.DCSSDK_OPMODE_BT_LE)
            sdkHandler?.dcssdkSetOperationalMode(DCSSDKDefs.DCSSDK_MODE.DCSSDK_OPMODE_BT_NORMAL)

            sdkHandler?.dcssdkSetDelegate(this)

            val mask = 0
            mask or DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SCANNER_APPEARANCE.value or DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SCANNER_DISAPPEARANCE.value
            mask or DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_BARCODE.value or DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SESSION_ESTABLISHMENT.value or DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SESSION_TERMINATION.value

            sdkHandler?.dcssdkSubsribeForEvents(mask)
        }
        reader?.let {
            scannerList.forEach { device ->
                if (device.scannerName.contains(it.hostName)) {
                    try {
                        scannerId = device.scannerID
                        sdkHandler?.dcssdkEstablishCommunicationSession(scannerId)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
    //endregion

    //region open barcode scanner
    private fun scanCode() {
        val inXml = "<inArgs><scannerID>$scannerId</scannerID></inArgs>"
        CoroutineScope(Dispatchers.IO).launch {
            executeCommand(
                inXML = inXml,
                scannerID = scannerId
            )
        }
    }

    private fun executeCommand(
        opcode: DCSSDKDefs.DCSSDK_COMMAND_OPCODE = DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_DEVICE_PULL_TRIGGER,
        inXML: String,
        outXML: StringBuilder? = null,
        scannerID: Int
    ): Boolean {
        sdkHandler?.let { handler ->
            val result =
                handler.dcssdkExecuteCommandOpCodeInXMLForScanner(opcode, inXML, outXML, scannerID)
            Log.i(TAG, "execute command returned $result")
            return result == DCSSDKDefs.DCSSDK_RESULT.DCSSDK_RESULT_SUCCESS
        }
        return false
    }
    //endregion

    companion object {
        val TAG: String = ZebraReader::class.java.simpleName
    }
}