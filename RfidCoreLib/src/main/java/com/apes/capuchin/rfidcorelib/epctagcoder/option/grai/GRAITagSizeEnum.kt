package com.apes.capuchin.rfidcorelib.epctagcoder.option.grai

enum class GRAITagSizeEnum(val value: Int? = null) {

    BITS_96(value = 96) {
        override fun getHeader(): Int = 51
        override fun getSerialBitCount(): Int = 38
    },
    UNKNOWN {
        override fun getHeader(): Int = 0
        override fun getSerialBitCount(): Int = 0
    };

    abstract fun getHeader(): Int
    abstract fun getSerialBitCount(): Int

    companion object {
        fun findByValue(value: Int?): GRAITagSizeEnum {
            val values = entries.associateBy(GRAITagSizeEnum::value)
            return values[value] ?: UNKNOWN
        }
    }
}