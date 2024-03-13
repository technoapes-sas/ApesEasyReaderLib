package com.apes.capuchin.rfidcorelib.readers

import com.apes.capuchin.rfidcorelib.EasyReader

class ErrorReader : EasyReader() {
    override fun connectReader() = Unit
    override fun disconnectReader() = Unit
    override fun isReaderConnected() = Unit
    override fun intiRead() = Unit
    override fun stopRead() = Unit
    override fun setSessionControl() = Unit
    override fun getSessionControl() = Unit
    override fun setAntennaSound() = Unit
    override fun getAntennaSound() = Unit
    override fun setAntennaPower() = Unit
    override fun getAntennaPower() = Unit
}