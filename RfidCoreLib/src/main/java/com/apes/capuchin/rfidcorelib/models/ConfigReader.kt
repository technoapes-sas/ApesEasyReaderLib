package com.apes.capuchin.rfidcorelib.models

import com.apes.capuchin.rfidcorelib.enums.AntennaPowerLevelsEnum
import com.apes.capuchin.rfidcorelib.enums.BeeperLevelsEnum
import com.apes.capuchin.rfidcorelib.enums.ReadModeEnum
import com.apes.capuchin.rfidcorelib.enums.ReadTypeEnum
import com.apes.capuchin.rfidcorelib.enums.ReaderModeEnum
import com.apes.capuchin.rfidcorelib.enums.SessionControlEnum

data class ConfigReader(
    val readerMode: ReaderModeEnum = ReaderModeEnum.RFID_MODE,
    val readMode: ReadModeEnum = ReadModeEnum.NOTIFY_BY_ITEM_READ,
    val readType: ReadTypeEnum = ReadTypeEnum.INVENTORY,
    val antennaPower: AntennaPowerLevelsEnum = AntennaPowerLevelsEnum.MAX,
    val sessionControl: SessionControlEnum = SessionControlEnum.S0,
    val beeper: BeeperLevelsEnum = BeeperLevelsEnum.MAX
)
