package com.apes.capuchin.rfidcorelib.readers.zebra

import android.content.Context
import com.zebra.rfid.api3.ENUM_TRANSPORT
import com.zebra.rfid.api3.InvalidUsageException
import com.zebra.rfid.api3.RFIDReader
import com.zebra.rfid.api3.ReaderDevice
import com.zebra.rfid.api3.Readers
import com.zebra.rfid.api3.Readers.RFIDReaderEventHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReadersManager(private val context: Context) {

    private var readers: Readers? = null
    private var eventHandler: RFIDReaderEventHandler? = null
    private var availableReaders: List<ReaderDevice> = emptyList()

    fun initReader(eventHandler: RFIDReaderEventHandler, onReadersCreated: () -> Unit) {
        this.eventHandler = eventHandler
        createInstanceTask {
            onReadersCreated()
        }
    }

    fun connectReader(device: ReaderDevice? , onReaderConnected: (RFIDReader) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            getAvailableReader()
            val reader = if (device != null) device.rfidReader else availableReaders.firstOrNull()?.rfidReader
            if (reader != null) {
                reader.connect()
                onReaderConnected(reader)
            }
        }
    }

    private fun createInstanceTask(onReadersCreated: () -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            readers = Readers(context, ENUM_TRANSPORT.SERVICE_SERIAL)
            try {
                availableReaders = readers?.GetAvailableRFIDReaderList().orEmpty().toList()
                if (availableReaders.isEmpty()) {
                    throw InvalidUsageException(
                        "No available readers",
                        "ServiceSerial not available"
                    )
                }
            } catch (e: InvalidUsageException) {
                readers = Readers(context, ENUM_TRANSPORT.BLUETOOTH)
            }
            onReadersCreated()
        }
    }

    private fun getAvailableReader() {
        readers?.let {
            Readers.attach(eventHandler)
            try {
                availableReaders = it.GetAvailableRFIDReaderList().toList()
            } catch (e: InvalidUsageException) {
                e.printStackTrace()
            }
        }
    }
}