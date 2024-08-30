package com.apes.capuchin.rfidcorelib.epctagcoder.parse.interfaces

fun interface ItemReferenceStep {
    fun withItemReference(itemReference: String?): SerialStep
}