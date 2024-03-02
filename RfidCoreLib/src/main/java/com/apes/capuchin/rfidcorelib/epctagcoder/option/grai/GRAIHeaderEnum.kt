package com.apes.capuchin.rfidcorelib.epctagcoder.option.grai

enum class GRAIHeaderEnum(val value: String? = null) {

    HEADER_00110011("00110011") {
        override fun getTagSize(): Int = 96
    },
    UNKNOWN {
        override fun getTagSize(): Int = 0
    };

    abstract fun getTagSize(): Int

    companion object {
        fun findByValue(value: String?): GRAIHeaderEnum {
            val values = entries.associateBy(GRAIHeaderEnum::value)
            return values[value] ?: UNKNOWN
        }
    }
}