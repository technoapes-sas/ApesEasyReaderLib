package com.apes.capuchin.rfidcorelib.epctagcoder.parse.sgtin.interfaces

import com.apes.capuchin.rfidcorelib.epctagcoder.option.sgtin.SGTINExtensionDigitEnum

interface ExtensionDigitStep {
    fun withExtensionDigit(extensionDigit: SGTINExtensionDigitEnum?): ItemReferenceStep?
}