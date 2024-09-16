package com.apes.capuchin.rfidcorelib.readers.chainway

import com.apes.capuchin.rfidcorelib.enums.AntennaPowerLevelsEnum
import com.apes.capuchin.rfidcorelib.enums.BeeperLevelsEnum
import com.apes.capuchin.rfidcorelib.enums.SessionControlEnum
import com.apes.capuchin.rfidcorelib.utils.SoundPlayer
import com.rscja.deviceapi.RFIDWithUHFBLE
import com.rscja.deviceapi.RFIDWithUHFUART

class ReaderConfiguration {

    fun setSessionControl(
        reader: RFIDWithUHFUART?,
        btReader: RFIDWithUHFBLE?,
        sessionControlEnum: SessionControlEnum
    ): Boolean {
        var isGes2 = false
        reader?.let {
            val gen2 = it.gen2?.apply {
                queryTarget = 0
                querySession = sessionControlEnum.value ?: 0
            }
            isGes2 = gen2?.let { entity -> it.setGen2(entity) } ?: false
        }

        btReader?.let {
            val gen2 = it.gen2?.apply {
                queryTarget = 0
                querySession = sessionControlEnum.value ?: 0
            }
            isGes2 = gen2?.let { entity -> it.setGen2(entity) } ?: false
        }
        return isGes2
    }

    fun getSessionControl(reader: RFIDWithUHFUART?, btReader: RFIDWithUHFBLE?): SessionControlEnum {
        return reader?.gen2?.querySession?.toSessionControlEnum()
            ?: btReader?.gen2?.querySession?.toSessionControlEnum()
            ?: SessionControlEnum.S0
    }

    fun setAntennaSound(beeperLevelsEnum: BeeperLevelsEnum, soundPlayer: SoundPlayer) {
        when (beeperLevelsEnum) {
            BeeperLevelsEnum.MAX -> soundPlayer.soundLevel = 100
            BeeperLevelsEnum.MEDIUM -> soundPlayer.soundLevel = 66
            BeeperLevelsEnum.MIN -> soundPlayer.soundLevel = 33
            else -> soundPlayer.soundLevel = 0
        }
    }

    fun getAntennaSound(soundPlayer: SoundPlayer): BeeperLevelsEnum {
        val level = soundPlayer.soundLevel
        return when (level) {
            in 1..33 -> BeeperLevelsEnum.MIN
            in 34..66 -> BeeperLevelsEnum.MEDIUM
            in 67..100 -> BeeperLevelsEnum.MAX
            else -> BeeperLevelsEnum.QUIET
        }
    }

    fun setAntennaPower(reader: RFIDWithUHFUART?, btReader: RFIDWithUHFBLE?, antennaPowerLevelsEnum: AntennaPowerLevelsEnum) {
        val powerLevel = when (antennaPowerLevelsEnum) {
            AntennaPowerLevelsEnum.MEDIUM -> 20
            AntennaPowerLevelsEnum.MIN -> 10
            else -> 30
        }

        reader?.let { it.power = powerLevel }
        btReader?.let { it.power = powerLevel }
    }

    fun getAntennaPower(reader: RFIDWithUHFUART?, btReader: RFIDWithUHFBLE?): AntennaPowerLevelsEnum {
        return when (reader?.power ?: btReader?.power) {
            10 -> AntennaPowerLevelsEnum.MIN
            20 -> AntennaPowerLevelsEnum.MEDIUM
            else -> AntennaPowerLevelsEnum.MAX
        }
    }

    private fun Int.toSessionControlEnum(): SessionControlEnum {
        return when (this) {
            1 -> SessionControlEnum.S1
            2 -> SessionControlEnum.S2
            3 -> SessionControlEnum.S3
            else -> SessionControlEnum.S0
        }
    }
}