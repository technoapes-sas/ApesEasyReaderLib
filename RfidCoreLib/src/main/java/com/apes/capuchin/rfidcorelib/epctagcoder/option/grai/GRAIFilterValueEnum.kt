package com.apes.capuchin.rfidcorelib.epctagcoder.option.grai

enum class GRAIFilterValueEnum(val value: Int? = null) {

    ALL_OTHERS_0(value = 0),
    RAIL_VEHICLER(value = 1),
    RESERVED_1(value = 2),
    RESERVED_2(value = 3),
    RESERVED_3(value = 4),
    RESERVED_4(value = 5),
    RESERVED_5(value = 6),
    RESERVED_6(value = 7),
    UNKNOWN;

    companion object {
        fun findByValue(value: Int?): GRAIFilterValueEnum {
            val values = entries.associateBy(GRAIFilterValueEnum::value)
            return values[value] ?: UNKNOWN
        }
    }
}