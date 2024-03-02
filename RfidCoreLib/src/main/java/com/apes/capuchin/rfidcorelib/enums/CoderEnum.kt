package com.apes.capuchin.rfidcorelib.enums

enum class CoderEnum(val key: String? = null) {

    CPI(key = "cpi"),
    GDTI(key = "gdti"),
    GSRN(key = "gsrn"),
    SGTIN(key = "sgtin"),
    SSCC(key = "sscc"),
    GIAI(key = "giai"),
    GRAI(key = "grai"),
    NONE;

    companion object {
        fun getCoder(key: String?): CoderEnum {
            val coder = entries.associateBy(CoderEnum::key)
            return coder[key] ?: NONE
        }
    }
}