package com.apes.capuchin.rfidcorelib.epctagcoder.parse.interfaces

fun interface ExtensionDigitStep {
    fun withExtensionDigit(extensionDigit: Any?): ItemReferenceStep
}