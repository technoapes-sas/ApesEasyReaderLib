package com.apes.capuchin.rfidcorelib.epctagcoder.option

enum class PrefixLengthEnum(val value: Int? = null) {

    DIGIT_6(value = 6),
    DIGIT_7(value = 7),
    DIGIT_8(value = 8),
    DIGIT_9(value = 9),
    DIGIT_10(value = 10),
    DIGIT_11(value = 11),
    DIGIT_12(value = 12),
    UNKNOWN;

    companion object {
        fun findByCode(code: Int?): PrefixLengthEnum {
            val codes = entries.associateBy(PrefixLengthEnum::value)
            return codes[code] ?: UNKNOWN
        }
    }
}