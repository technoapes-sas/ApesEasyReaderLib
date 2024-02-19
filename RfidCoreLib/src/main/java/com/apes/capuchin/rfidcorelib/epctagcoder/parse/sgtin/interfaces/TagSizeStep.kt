package com.apes.capuchin.rfidcorelib.epctagcoder.parse.sgtin.interfaces

import com.apes.capuchin.rfidcorelib.epctagcoder.option.sgtin.SGTINTagSizeEnum
import com.apes.capuchin.rfidcorelib.epctagcoder.parse.sgtin.ParseSGTIN

interface TagSizeStep {
    fun withTagSize(tagSize: SGTINTagSizeEnum?): ParseSGTIN.FilterValueStep?
}