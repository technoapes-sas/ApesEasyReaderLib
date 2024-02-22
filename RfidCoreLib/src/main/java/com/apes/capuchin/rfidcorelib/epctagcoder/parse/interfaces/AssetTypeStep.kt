package com.apes.capuchin.rfidcorelib.epctagcoder.parse.interfaces

interface AssetTypeStep {
    fun withItemReference(itemReference: String?): SerialStep?
}