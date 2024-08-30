package com.apes.capuchin.rfidcorelib.epctagcoder.parse.interfaces

fun interface AssetTypeStep {
    fun withItemReference(itemReference: String?): SerialStep
}