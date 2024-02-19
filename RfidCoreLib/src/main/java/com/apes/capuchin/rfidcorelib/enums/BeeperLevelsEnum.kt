package com.apes.capuchin.rfidcorelib.enums

enum class BeeperLevelsEnum(private val level: Int) {

    QUIET(level = 0),
    MIN(level = 33),
    MEDIUM(level = 66),
    MAX(level = 100);

    companion object {
        fun getBeeperLevel(value: Int) : BeeperLevelsEnum {
            val level = entries.associateBy(BeeperLevelsEnum::level)
            return level[value] ?: QUIET
        }
    }
}
