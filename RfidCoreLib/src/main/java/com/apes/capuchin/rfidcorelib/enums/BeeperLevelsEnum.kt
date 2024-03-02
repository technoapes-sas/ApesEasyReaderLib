package com.apes.capuchin.rfidcorelib.enums

enum class BeeperLevelsEnum(val level: Int? = null) {

    QUIET(level = 0),
    MIN(level = 33),
    MEDIUM(level = 66),
    MAX(level = 100),
    UNKNOWN;

    companion object {
        fun getBeeperLevel(value: Int?) : BeeperLevelsEnum {
            val level = entries.associateBy(BeeperLevelsEnum::level)
            return level[value] ?: UNKNOWN
        }
    }
}
