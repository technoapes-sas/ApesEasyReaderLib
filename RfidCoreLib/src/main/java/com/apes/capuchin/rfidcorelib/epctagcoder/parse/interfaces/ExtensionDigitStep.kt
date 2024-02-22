package com.apes.capuchin.rfidcorelib.epctagcoder.parse.interfaces

interface ExtensionDigitStep {
    fun withExtensionDigit(extensionDigit: Any?): ItemReferenceStep?
}