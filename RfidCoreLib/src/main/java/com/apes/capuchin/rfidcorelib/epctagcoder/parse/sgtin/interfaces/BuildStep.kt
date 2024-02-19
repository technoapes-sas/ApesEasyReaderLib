package com.apes.capuchin.rfidcorelib.epctagcoder.parse.sgtin.interfaces

import com.apes.capuchin.rfidcorelib.epctagcoder.parse.sgtin.ParseSGTIN

interface BuildStep {
    fun build(): ParseSGTIN?
}