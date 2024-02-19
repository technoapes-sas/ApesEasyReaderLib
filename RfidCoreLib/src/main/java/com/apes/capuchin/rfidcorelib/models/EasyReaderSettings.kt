package com.apes.capuchin.rfidcorelib.models

import com.apes.capuchin.rfidcorelib.enums.AntennaPowerLevelsEnum
import com.apes.capuchin.rfidcorelib.enums.SessionControlEnum
import com.apes.capuchin.rfidcorelib.enums.SettingsEnum

data class EasyReaderSettings(
    val lastSettingChanged: SettingsEnum? = null,
    val antennaPower: AntennaPowerLevelsEnum? = null,
    val antennaSound: Int? = null,
    val sessionControl: SessionControlEnum? = null
)
