package com.apes.capuchin.rfidcorelib.models

import com.apes.capuchin.rfidcorelib.enums.SettingsEnum

data class PendingSetting(
    val typeSetting: SettingsEnum? = null,
    val valueToApply: Int? = null
)
