package com.apes.capuchin.rfidcorelib.epctagcoder.parse.sgtin.interfaces

interface SerialStep {
    fun withSerial(serial: String?): TagSizeStep?
}