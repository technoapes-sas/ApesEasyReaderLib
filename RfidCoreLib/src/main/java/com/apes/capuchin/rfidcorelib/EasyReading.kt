package com.apes.capuchin.rfidcorelib

import android.content.Context
import com.apes.capuchin.rfidcorelib.enums.CoderEnum
import com.apes.capuchin.rfidcorelib.enums.ReadModeEnum
import com.apes.capuchin.rfidcorelib.enums.ReadTypeEnum
import com.apes.capuchin.rfidcorelib.models.EasyResponse
import com.apes.capuchin.rfidcorelib.readers.ChainwayReader
import com.apes.capuchin.rfidcorelib.readers.ErrorReader
import com.apes.capuchin.rfidcorelib.readers.ZebraReader

class EasyReading private constructor(
    val easyReader: EasyReader,
    val coder: CoderEnum,
    val readMode: ReadModeEnum,
    val readType: ReadTypeEnum,
    val companyPrefixes: List<String>?
) {
    class Builder {
        private var easyReader: EasyReader? = null
        private var coder: CoderEnum = CoderEnum.NONE
        private var readMode: ReadModeEnum = ReadModeEnum.NOTIFY_WHEN_READER_STOPPED
        private var readType: ReadTypeEnum = ReadTypeEnum.INVENTORY
        private var companyPrefixes: List<String>? = null

        private var deviceId: String? = null
        private var deviceModel: String? = null

        fun coder(coder: CoderEnum) = apply { this.coder = coder }
        fun readMode(readMode: ReadModeEnum) = apply { this.readMode = readMode }
        fun companyPrefixes(companyPrefixes: List<String>) = apply { this.companyPrefixes = companyPrefixes }
        fun readType(readType: ReadTypeEnum) = apply { this.readType = readType }

        fun build(context: Context): EasyReading {
            detectAndConnect(context)
            return EasyReading(easyReader!!, coder, readMode, readType, companyPrefixes)
        }

        private fun detectAndConnect(context: Context) {
            try {
                when (deviceModel) {
                    TC20, MC33 -> ZebraReader(context)
                    CHAINWAY_C72 -> ChainwayReader()
                    else -> Unit
                }
                connectEasyReader()
            } catch (e: Exception) {
                onErrorConnect()
                e.printStackTrace()
            }
        }

        private fun connectEasyReader() {
            try {
                easyReader?.let { reader ->
                    reader.apply {
                        coderEnum = coder
                        readModeEnum = readMode
                        readTypeEnum = readType
                        prefixes = companyPrefixes.orEmpty()
                        initReader()
                    }
                } ?: run { onErrorConnect() }
            } catch (e: Exception) {
                onErrorConnect()
                e.printStackTrace()
            }
        }

        private fun onErrorConnect() {
            easyReader = ErrorReader()
            easyReader?.notifyObservers(EasyResponse(
                success = false,
                message = "El lector no pudo ser conectado.",
                code = CONNECTION_FAILED
            ))
            easyReader = null
        }
    }
}