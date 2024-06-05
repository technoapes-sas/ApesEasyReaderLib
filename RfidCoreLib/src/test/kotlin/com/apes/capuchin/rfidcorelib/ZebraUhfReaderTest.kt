package com.apes.capuchin.rfidcorelib

import android.content.Context
import com.apes.capuchin.rfidcorelib.enums.*
import com.apes.capuchin.rfidcorelib.models.EasyResponse
import com.apes.capuchin.rfidcorelib.readers.zebra.*
import com.zebra.rfid.api3.*
import io.mockk.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ZebraUhfReaderTest {

    private lateinit var context: Context
    private lateinit var reader: RFIDReader
    private lateinit var readersManager: ReadersManager
    private lateinit var readerConfiguration: ReaderConfiguration
    private lateinit var scannerManager: ScannerManager
    private lateinit var zebraUhfReader: ZebraUhfReader

    @Before
    fun setup() {
        context = mockk()
        reader = mockk()
        readersManager = mockk()
        scannerManager = mockk()
        readerConfiguration = mockk()

        zebraUhfReader = spyk(ZebraUhfReader(context))

        every { zebraUhfReader.reader } returns reader
        every { zebraUhfReader.readers } returns readersManager
        every { zebraUhfReader.readerConfiguration } returns readerConfiguration
        every { zebraUhfReader.scannerManager } returns scannerManager
    }

    @Test
    fun `connectReader should connect to the reader and configure it`() {
        // Arrange
        val connectReaderCallback = slot<(RFIDReader?) -> Unit>()
        every { readersManager.connectReader(null, capture(connectReaderCallback)) } answers { connectReaderCallback.captured.invoke(reader) }
        every { readerConfiguration.configureReader(reader, any()) } just Runs

        // Act
        zebraUhfReader.connectReader()

        // Assert
        verify(exactly = 0) { readersManager.connectReader(null, any()) }
        verify(exactly = 0) { readerConfiguration.configureReader(reader, any()) }

        assertEquals(reader, zebraUhfReader.reader)
    }

    @Test
    fun `disconnectReader should disconnect the reader and notify observers`() {
        // Arrange
        val observerSlot = slot<EasyResponse>()
        every { reader.disconnect() } just Runs
        every { zebraUhfReader.notifyObservers(capture(observerSlot)) } just Runs

        // Act
        zebraUhfReader.disconnectReader()

        // Assert
        verify(exactly = 0) { reader.disconnect() }
        assertEquals(CONNECTION_CLOSE, observerSlot.captured.code)
    }

    @Test
    fun `isReaderConnected should return true if reader is connected`() {
        // Arrange
        every { reader.isConnected } returns true
        zebraUhfReader.reader = reader

        // Act
        val isConnected = zebraUhfReader.isReaderConnected()

        // Assert
        assertTrue(isConnected)
    }

    @Test
    fun `initRead should perform inventory or scan code based on reader mode`() {
        // Arrange
        val inventoryActions: Inventory = mockk(relaxed = true)

        // Act
        zebraUhfReader.readerModeEnum = ReaderModeEnum.RFID_MODE
        zebraUhfReader.initRead()
        verify(exactly = 0) { inventoryActions.perform() }

        zebraUhfReader.readerModeEnum = ReaderModeEnum.BARCODE_MODE
        zebraUhfReader.initRead()
        verify(exactly = 0) { scannerManager.scanCode() }
    }

    @Test
    fun `stopRead should stop inventory`() {
        // Arrange
        val inventoryActions: Inventory = mockk(relaxed = true)

        // Act
        zebraUhfReader.stopRead()

        // Assert
        verify(exactly = 0) { inventoryActions.stop() }
    }
}