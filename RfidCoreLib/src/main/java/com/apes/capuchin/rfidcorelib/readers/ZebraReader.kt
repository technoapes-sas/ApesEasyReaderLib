package com.apes.capuchin.rfidcorelib.readers

import android.content.Context
import com.apes.capuchin.rfidcorelib.EasyReader
import com.zebra.rfid.api3.ENUM_TRANSPORT
import com.zebra.rfid.api3.InvalidUsageException
import com.zebra.rfid.api3.RFIDReader
import com.zebra.rfid.api3.ReaderDevice
import com.zebra.rfid.api3.Readers
import com.zebra.rfid.api3.Readers.RFIDReaderEventHandler
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
        return ""
    }
}