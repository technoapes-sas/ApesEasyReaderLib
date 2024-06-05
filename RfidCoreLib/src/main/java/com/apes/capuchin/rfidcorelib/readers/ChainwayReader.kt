package com.apes.capuchin.rfidcorelib.readers

import com.apes.capuchin.rfidcorelib.EasyReader
import com.apes.capuchin.rfidcorelib.enums.AntennaPowerLevelsEnum
import com.apes.capuchin.rfidcorelib.enums.BeeperLevelsEnum
import com.apes.capuchin.rfidcorelib.enums.SessionControlEnum

class ChainwayReader : EasyReader() {
    override fun connectReader() {
        TODO("Not yet implemented")
    }

    override fun disconnectReader() {
        TODO("Not yet implemented")
    }

    override fun isReaderConnected(): Boolean {
        TODO("Not yet implemented")
    }

    override fun initRead() {
        TODO("Not yet implemented")
    }

    override fun stopRead() {
        TODO("Not yet implemented")
    }

    override fun initReader() {
        TODO("Not yet implemented")
    }

    override fun setSessionControl(sessionControlEnum: SessionControlEnum) {
        TODO("Not yet implemented")
    }

    override fun getSessionControl(): SessionControlEnum {
        TODO("Not yet implemented")
    }

    override fun setAntennaSound(beeperLevelsEnum: BeeperLevelsEnum) {
        TODO("Not yet implemented")
    }

    override fun getAntennaSound(): BeeperLevelsEnum {
        TODO("Not yet implemented")
    }

    override fun setAntennaPower(antennaPowerLevelsEnum: AntennaPowerLevelsEnum) {
        TODO("Not yet implemented")
    }

    override fun getAntennaPower(): AntennaPowerLevelsEnum {
        TODO("Not yet implemented")
    }
}