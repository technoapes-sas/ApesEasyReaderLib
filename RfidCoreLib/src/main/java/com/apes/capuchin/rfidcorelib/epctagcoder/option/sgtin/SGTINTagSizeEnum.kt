package com.apes.capuchin.rfidcorelib.epctagcoder.option.sgtin

enum class SGTINTagSizeEnum(val value: Int) {
    BITS_96(value = 96) {
        override fun getHeader(): Int = 48
        override fun getSerialBitCount(): Int = 38
        override fun getSerialMaxLength(): Int = 11
        override fun getSerialMaxValue(): Long = 274877906943L
    },
    BITS_198(value = 198) {
        override fun getHeader(): Int = 54
        override fun getSerialBitCount(): Int = 140
        override fun getSerialMaxLength(): Int = 20
        override fun getSerialMaxValue(): Long? = null
    };

    abstract fun getHeader(): Int
    abstract fun getSerialBitCount(): Int
    abstract fun getSerialMaxLength(): Int
    abstract fun getSerialMaxValue(): Long?

    companion object {
        fun findByValue(value: Int?): SGTINTagSizeEnum {
            val values = entries.associateBy(SGTINTagSizeEnum::value)
            return values[value] ?: BITS_96
        }
    }
}