package com.apes.capuchin.rfidcorelib.epctagcoder.parse.interfaces

fun interface SerialStep {
    fun withSerial(serial: String?): TagSizeStep
}