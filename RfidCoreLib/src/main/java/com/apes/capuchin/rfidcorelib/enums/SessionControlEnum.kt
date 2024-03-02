package com.apes.capuchin.rfidcorelib.enums

enum class SessionControlEnum(val value: Int? = null) {

    S0(value = 0),
    S1(value = 1),
    S2(value = 2),
    S3(value = 3),
    UNKNOWN;

    companion object {
        private fun getSession(value: Int?): SessionControlEnum {
            val session = entries.associateBy(SessionControlEnum::value)
            return session[value] ?: UNKNOWN
        }
    }
}