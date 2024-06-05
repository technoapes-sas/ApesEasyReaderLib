package com.apes.capuchin.rfidcorelib.readers.zebra

import com.apes.capuchin.rfidcorelib.enums.AntennaPowerLevelsEnum
import com.apes.capuchin.rfidcorelib.enums.BeeperLevelsEnum
import com.apes.capuchin.rfidcorelib.enums.SessionControlEnum
import com.zebra.rfid.api3.BEEPER_VOLUME
import com.zebra.rfid.api3.ENUM_TRIGGER_MODE
import com.zebra.rfid.api3.INVENTORY_STATE
import com.zebra.rfid.api3.RFIDReader
import com.zebra.rfid.api3.RfidEventsListener
import com.zebra.rfid.api3.SESSION
import com.zebra.rfid.api3.SL_FLAG
import com.zebra.rfid.api3.START_TRIGGER_TYPE
import com.zebra.rfid.api3.STOP_TRIGGER_TYPE
import com.zebra.rfid.api3.TriggerInfo

class ReaderConfiguration {

    fun configureReader(reader: RFIDReader, eventHandler: RfidEventsListener) {
        reader.apply {
            Events.addEventsListener(eventHandler)
            Events.setHandheldEvent(true)
            Events.setTagReadEvent(true)
            Events.setAttachTagDataWithReadEvent(true)

            Config.setTriggerMode(ENUM_TRIGGER_MODE.RFID_MODE, true)
            val triggerInfo = TriggerInfo().apply {
                StartTrigger.triggerType = START_TRIGGER_TYPE.START_TRIGGER_TYPE_IMMEDIATE
                StopTrigger.triggerType = STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_IMMEDIATE
            }
            Config.startTrigger = triggerInfo.StartTrigger
            Config.stopTrigger = triggerInfo.StopTrigger

            Actions.PreFilters.deleteAll()
        }
    }

    fun setSessionControl(reader: RFIDReader?, sessionControlEnum: SessionControlEnum) {
        reader?.let {
            val sessionValue = when (sessionControlEnum) {
                SessionControlEnum.S1 -> SESSION.SESSION_S1
                SessionControlEnum.S2 -> SESSION.SESSION_S2
                SessionControlEnum.S3 -> SESSION.SESSION_S3
                else -> SESSION.SESSION_S0
            }
            val config = it.Config.Antennas.getSingulationControl(1).apply {
                session = sessionValue
                Action.inventoryState = INVENTORY_STATE.INVENTORY_STATE_A
                Action.slFlag = SL_FLAG.SL_ALL
            }
            it.Config.Antennas.setSingulationControl(1, config)
        }
    }

    fun getSessionControl(reader: RFIDReader?): SessionControlEnum? {
        var sessionValue: SessionControlEnum? = null
        reader?.let {
            val session = it.Config.Antennas.getSingulationControl(1).session
            sessionValue = when (session) {
                SESSION.SESSION_S1 -> SessionControlEnum.S1
                SESSION.SESSION_S2 -> SessionControlEnum.S2
                SESSION.SESSION_S3 -> SessionControlEnum.S3
                else -> SessionControlEnum.S0
            }
        }
        return sessionValue
    }

    fun setAntennaSound(reader: RFIDReader?, beeperLevelsEnum: BeeperLevelsEnum) {
        reader?.Config?.beeperVolume = when (beeperLevelsEnum) {
            BeeperLevelsEnum.MAX -> BEEPER_VOLUME.HIGH_BEEP
            BeeperLevelsEnum.MEDIUM -> BEEPER_VOLUME.MEDIUM_BEEP
            BeeperLevelsEnum.MIN -> BEEPER_VOLUME.LOW_BEEP
            else -> BEEPER_VOLUME.QUIET_BEEP
        }
    }

    fun getAntennaSound(reader: RFIDReader?): BeeperLevelsEnum? {
        var beeperLevel: BeeperLevelsEnum? = null
        reader?.let {
            beeperLevel = when (it.Config.beeperVolume) {
                BEEPER_VOLUME.HIGH_BEEP -> BeeperLevelsEnum.MAX
                BEEPER_VOLUME.MEDIUM_BEEP -> BeeperLevelsEnum.MEDIUM
                BEEPER_VOLUME.LOW_BEEP -> BeeperLevelsEnum.MIN
                else -> BeeperLevelsEnum.QUIET
            }
        }
        return beeperLevel
    }

    fun setAntennaPower(reader: RFIDReader?, antennaPowerLevelsEnum: AntennaPowerLevelsEnum) {
        reader?.let {
            val config = it.Config.Antennas.getAntennaRfConfig(1).apply {
                tari = 0
                transmitPowerIndex = when (antennaPowerLevelsEnum) {
                    AntennaPowerLevelsEnum.MAX -> 270
                    AntennaPowerLevelsEnum.MEDIUM -> 100
                    else -> 33
                }
                setrfModeTableIndex(0)
            }
            it.Config.Antennas.setAntennaRfConfig(1, config)
        }
    }

    fun getAntennaPower(reader: RFIDReader?): AntennaPowerLevelsEnum? {
        var antennaValue: AntennaPowerLevelsEnum? = null
        reader?.let {
            val power = it.Config.Antennas.getAntennaRfConfig(1).transmitPowerIndex
            antennaValue = when (power) {
                270 -> AntennaPowerLevelsEnum.MAX
                100 -> AntennaPowerLevelsEnum.MEDIUM
                else -> AntennaPowerLevelsEnum.MIN
            }
        }
        return antennaValue
    }
}