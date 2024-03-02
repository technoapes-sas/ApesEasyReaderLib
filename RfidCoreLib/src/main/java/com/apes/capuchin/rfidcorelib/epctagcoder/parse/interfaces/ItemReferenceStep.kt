package com.apes.capuchin.rfidcorelib.epctagcoder.parse.interfaces

interface ItemReferenceStep {
    fun withItemReference(itemReference: String?): SerialStep
}