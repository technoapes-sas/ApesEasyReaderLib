package com.apes.capuchin.rfidcorelib.models

import com.apes.capuchin.rfidcorelib.enums.AntennaPowerLevelsEnum
import com.apes.capuchin.rfidcorelib.enums.BeeperLevelsEnum
import com.apes.capuchin.rfidcorelib.enums.SessionControlEnum
import com.apes.capuchin.rfidcorelib.enums.SettingsEnum

data class EasyReaderSettings(
    var lastSettingChanged: SettingsEnum? = null,
    var antennaPower: AntennaPowerLevelsEnum? = null,
    var antennaSound: BeeperLevelsEnum? = null,
    var sessionControl: SessionControlEnum? = null
)
