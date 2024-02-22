package com.apes.capuchin.rfidcorelib.epctagcoder.option.sgtin

enum class SGTINHeaderEnum(val value: String) {
    HEADER_00110000(value = "00110000") {
        override fun getTagSize(): Int = 96
    },
    HEADER_00110110(value = "00110110") {
        override fun getTagSize(): Int = 198
    };

    abstract fun getTagSize(): Int

    companion object {
        fun findByValue(value: String): SGTINHeaderEnum {
            val values = entries.associateBy(SGTINHeaderEnum::value)
            return values[value] ?: HEADER_00110000
        }
    }
}