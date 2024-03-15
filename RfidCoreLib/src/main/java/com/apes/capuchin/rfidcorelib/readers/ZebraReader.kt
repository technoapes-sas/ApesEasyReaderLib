package com.apes.capuchin.rfidcorelib.readers

import android.content.Context
import com.apes.capuchin.rfidcorelib.EMPTY_STRING
import com.apes.capuchin.rfidcorelib.EasyReader
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ZebraReader(
    private val context: Context
) : EasyReader(), RFIDReaderEventHandler {

    private var readerName: String? = null

    private var readers: Readers? = null
    private var reader: RFIDReader? = null
    private var readerDevice: ReaderDevice? = null

    private var MAX_POWER: Int = 270

    private var eventHandler = object : RfidEventsListener {
        override fun eventReadNotify(events: RfidReadEvents?) {
            val myTags = reader?.Actions?.getReadTags(100).orEmpty()
            TODO("Definir la lógica para devolver tags leídos")
        }

        override fun eventStatusNotify(events: RfidStatusEvents?) {
            requireNotNull(events)
            val statusEvent = events.StatusEventData.statusEventType
            when (statusEvent) {
                STATUS_EVENT_TYPE.HANDHELD_TRIGGER_EVENT -> {
                    val handheldEvent = events.StatusEventData.HandheldTriggerEventData.handheldEvent
                    when (handheldEvent) {
                        HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_PRESSED -> {
                            CoroutineScope(Dispatchers.IO).launch {
                                performInventory()
                            }
                        }

                        HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_RELEASED -> {
                            CoroutineScope(Dispatchers.IO).launch {
                                stopInventory()
                            }
                        }
                    }
                }

                STATUS_EVENT_TYPE.DISCONNECTION_EVENT -> {
                    disconnectReader()
                }
            }
        }
    }

    private var availableRFIDReaderList: List<ReaderDevice> = emptyList()

    //region
    @Synchronized
    override fun connectReader() {
        if (!isReaderConnected()) {
            connectionTask()
        }
    }

    override fun disconnectReader() {
        TODO("Not yet implemented")
    }

    override fun isReaderConnected(): Boolean = reader?.isConnected ?: false

    override fun intiRead() {
        TODO("Not yet implemented")
    }

    override fun stopRead() {
        TODO("Not yet implemented")
    }

    override fun initReader() {
        readers?.let { connectReader() } ?: run { createInstanceTask() }
    }

    override fun setSessionControl() {
        TODO("Not yet implemented")
    }

    override fun getSessionControl() {
        TODO("Not yet implemented")
    }

    override fun setAntennaSound() {
        TODO("Not yet implemented")
    }

    override fun getAntennaSound() {
        TODO("Not yet implemented")
    }

    override fun setAntennaPower() {
        TODO("Not yet implemented")
    }

    override fun getAntennaPower() {
        TODO("Not yet implemented")
    }
    //endregion

    override fun RFIDReaderAppeared(p0: ReaderDevice?) {
        TODO("Not yet implemented")
    }

    override fun RFIDReaderDisappeared(p0: ReaderDevice?) {
        TODO("Not yet implemented")
    }

    private fun createInstanceTask() {
        CoroutineScope(Dispatchers.IO).launch {
            var exception: InvalidUsageException? = null
            readers = Readers(context, ENUM_TRANSPORT.SERVICE_USB)
            try {
                availableRFIDReaderList = readers?.GetAvailableRFIDReaderList().orEmpty().toList()
            } catch (e: InvalidUsageException) {
                exception = e
                e.printStackTrace()
            }
            if (exception != null && availableRFIDReaderList.isEmpty()) {
                readers?.Dispose()
                delay(600)
                readers = Readers(context, ENUM_TRANSPORT.BLUETOOTH)
            }
            readers?.let { connectReader() }
        }
    }

    private fun connectionTask() {
        CoroutineScope(Dispatchers.IO).launch {
            getAvailableReader()
            when {
                reader != null -> connect()
                else -> "Failed to find or connect reader"
            }
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
                        isReaderConnected() -> "Connected: ${reader?.hostName.orEmpty()}"
                        else -> EMPTY_STRING
                    }
                }
                else -> EMPTY_STRING
            }
        } catch (e: InvalidUsageException) {
            e.printStackTrace()
            EMPTY_STRING
        } catch (e: OperationFailureException) {
            e.printStackTrace()
            "Connection failed ${e.vendorMessage} ${e.results}"
        }
    }

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

                MAX_POWER = (reader?.ReaderCapabilities?.transmitPowerLevelValues?.size ?: 0) - 1
                val config = reader?.Config?.Antennas?.getAntennaRfConfig(1)
                config?.transmitPowerIndex = MAX_POWER
                config?.setrfModeTableIndex(0)
                config?.tari = 0
                reader?.Config?.Antennas?.setAntennaRfConfig(1, config)

                val sessionControl = reader?.Config?.Antennas?.getSingulationControl(1)
                sessionControl?.session = SESSION.SESSION_S0
                sessionControl?.Action?.inventoryState = INVENTORY_STATE.INVENTORY_STATE_A
                sessionControl?.Action?.slFlag = SL_FLAG.SL_ALL
                reader?.Config?.Antennas?.setSingulationControl(1, sessionControl)

                reader?.Actions?.PreFilters?.deleteAll()
            } catch (e: InvalidUsageException) {
                e.printStackTrace()
            } catch (e: OperationFailureException) {
                e.printStackTrace()
            }
        }
    }

    private fun setupScannerSDK() {

    }

    @Synchronized
    private fun performInventory() {
        try {
            reader?.Actions?.Inventory?.perform()
        } catch (e: InvalidUsageException) {
            e.printStackTrace()
        } catch (e: OperationFailureException) {
            e.printStackTrace()
        }
    }

    @Synchronized
    private fun stopInventory() {
        try {
            reader?.Actions?.Inventory?.stop()
        } catch (e: InvalidUsageException) {
            e.printStackTrace()
        } catch (e: OperationFailureException) {
            e.printStackTrace()
        }
    }
}