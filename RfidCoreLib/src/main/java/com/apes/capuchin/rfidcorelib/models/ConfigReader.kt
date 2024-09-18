package com.apes.capuchin.rfidcorelib.models

import com.apes.capuchin.rfidcorelib.enums.AntennaPowerLevelsEnum
import com.apes.capuchin.rfidcorelib.enums.BeeperLevelsEnum
import com.apes.capuchin.rfidcorelib.enums.ReadModeEnum
import com.apes.capuchin.rfidcorelib.enums.ReadTypeEnum
import com.apes.capuchin.rfidcorelib.enums.ReaderModeEnum
import com.apes.capuchin.rfidcorelib.enums.SessionControlEnum

data class ConfigReader(
    var readerMode: ReaderModeEnum? = null,
    var readMode: ReadModeEnum? = null,
    var readType: ReadTypeEnum? = null,
    var antennaPower: AntennaPowerLevelsEnum? = null,
    var sessionControl: SessionControlEnum? = null,
    var beeper: BeeperLevelsEnum? = null,
)
