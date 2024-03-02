package com.apes.capuchin.rfidcorelib.epctagcoder.option.sgtin

enum class SGTINExtensionDigitEnum(val value: Int? = null) {

    EXTENSION_0(value = 0),
    EXTENSION_1(value = 1),
    EXTENSION_2(value = 2),
    EXTENSION_3(value = 3),
    EXTENSION_4(value = 4),
    EXTENSION_5(value = 5),
    EXTENSION_6(value = 6),
    EXTENSION_7(value = 7),
    EXTENSION_8(value = 8),
    EXTENSION_9(value = 9),
    UNKNOWN;

    companion object {
        fun findByValue(value: Int?): SGTINExtensionDigitEnum {
            val values = entries.associateBy(SGTINExtensionDigitEnum::value)
            return values[value] ?: UNKNOWN
        }
    }
}