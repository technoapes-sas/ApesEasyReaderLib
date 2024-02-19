package com.apes.capuchin.rfidcorelib.enums

enum class AntennaPowerLevelsEnum(val value: Int) {

    MIN(value = 1),
    MEDIUM(value = 2),
    MAX(value = 3);

    companion object {
        private fun getAntennaPowerLevel(key: Int): AntennaPowerLevelsEnum {
            val antennaPowers = entries.associateBy(AntennaPowerLevelsEnum::value)
            return antennaPowers[key] ?: MIN
        }
    }
}