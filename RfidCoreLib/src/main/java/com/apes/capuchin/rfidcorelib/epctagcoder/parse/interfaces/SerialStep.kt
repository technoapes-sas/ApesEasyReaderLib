package com.apes.capuchin.rfidcorelib.epctagcoder.parse.interfaces

interface SerialStep {
    fun withSerial(serial: String?): TagSizeStep
}