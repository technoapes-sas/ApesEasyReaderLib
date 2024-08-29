package com.apes.capuchin.rfidcorelib.readers

import com.apes.capuchin.rfidcorelib.enums.AntennaPowerLevelsEnum
import com.apes.capuchin.rfidcorelib.enums.BeeperLevelsEnum
import com.apes.capuchin.rfidcorelib.enums.SessionControlEnum
import com.apes.capuchin.rfidcorelib.models.EasyResponse
import com.apes.capuchin.rfidcorelib.utils.CONNECTION_FAILED_CODE
import com.apes.capuchin.rfidcorelib.utils.ERROR

class ErrorReader : EasyReader() {
    override fun connectReader() = Unit

    override fun disconnectReader() = Unit

    override fun isReaderConnected(): Boolean = false

    override fun initRead() = Unit

    override fun stopRead() = Unit

    override fun initReader() {
        notifyObservers(
            EasyResponse(
                success = ERROR,
                message = "El lector no pudo ser conectado.",
                code = CONNECTION_FAILED_CODE
            )
        )
    }

    override fun setSessionControl(sessionControlEnum: SessionControlEnum) = Unit

    override fun getSessionControl(): SessionControlEnum = SessionControlEnum.UNKNOWN

    override fun setAntennaSound(beeperLevelsEnum: BeeperLevelsEnum) = Unit

    override fun getAntennaSound(): BeeperLevelsEnum = BeeperLevelsEnum.UNKNOWN

    override fun setAntennaPower(antennaPowerLevelsEnum: AntennaPowerLevelsEnum) = Unit

    override fun getAntennaPower(): AntennaPowerLevelsEnum = AntennaPowerLevelsEnum.UNKNOWN
}