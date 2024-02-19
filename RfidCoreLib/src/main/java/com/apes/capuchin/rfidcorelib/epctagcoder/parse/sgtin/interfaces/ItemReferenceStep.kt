package com.apes.capuchin.rfidcorelib.epctagcoder.parse.sgtin.interfaces

interface ItemReferenceStep {
    fun withItemReference(itemReference: String?): SerialStep?
}