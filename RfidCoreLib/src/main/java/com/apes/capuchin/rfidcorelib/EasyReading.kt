package com.apes.capuchin.rfidcorelib

import android.content.Context
import com.apes.capuchin.rfidcorelib.enums.CoderEnum
import com.apes.capuchin.rfidcorelib.enums.ReadModeEnum
import com.apes.capuchin.rfidcorelib.enums.ReadTypeEnum
import com.apes.capuchin.rfidcorelib.enums.ReaderModeEnum
import com.apes.capuchin.rfidcorelib.models.EasyResponse
import com.apes.capuchin.rfidcorelib.readers.EasyReader
import com.apes.capuchin.rfidcorelib.readers.ErrorReader
import com.apes.capuchin.rfidcorelib.readers.zebra.ZebraUhfReader
import com.apes.capuchin.rfidcorelib.utils.CONNECTION_FAILED_CODE
import com.apes.capuchin.rfidcorelib.utils.ERROR

class EasyReading private constructor(val easyReader: EasyReader?) {

    class Builder {

        private lateinit var coder: CoderEnum
        private lateinit var readerMode: ReaderModeEnum
        private lateinit var readMode: ReadModeEnum
        private lateinit var readType: ReadTypeEnum
        private lateinit var companyPrefixes: List<String>

        private var easyReader: EasyReader? = null

        private var deviceId: String? = null
        private var deviceModel: String? = null

        fun coder(coder: CoderEnum) = apply { this.coder = coder }
        fun readMode(readMode: ReadModeEnum) = apply { this.readMode = readMode }
        fun readerMode(readerMode: ReaderModeEnum) = apply { this.readerMode = readerMode }
        fun companyPrefixes(companyPrefixes: List<String>) = apply { this.companyPrefixes = companyPrefixes }
        fun readType(readType: ReadTypeEnum) = apply { this.readType = readType }

        fun build(context: Context): EasyReading {
            detectAndConnect(context)
            return EasyReading(easyReader)
        }

        private fun detectAndConnect(context: Context) {
            try {
                easyReader = ZebraUhfReader(context)
                connectEasyReader()
            } catch (e: Exception) {
                onErrorConnect()
                e.printStackTrace()
            }
        }

        private fun connectEasyReader() {
            try {
                easyReader?.let { reader ->
                    with(reader) {
                        readerMode = this@Builder.readerMode
                        coder = this@Builder.coder
                        readMode = this@Builder.readMode
                        readType = this@Builder.readType
                        companyPrefixes.addAll(this@Builder.companyPrefixes)
                        initReader()
                    }
                } ?: onErrorConnect()
            } catch (e: Exception) {
                onErrorConnect()
                e.printStackTrace()
            }
        }

        private fun onErrorConnect() {
            easyReader = ErrorReader().apply {
                initReader()
            }
            easyReader = null
        }
    }
}