package com.apes.capuchin.rfidcorelib.epctagcoder.parse.sgtin.interfaces

import com.apes.capuchin.rfidcorelib.epctagcoder.option.sgtin.SGTINFilterValueEnum
import com.apes.capuchin.rfidcorelib.epctagcoder.parse.sgtin.ParseSGTIN

interface FilterValueStep {
    fun withFilterValue(filterValue: SGTINFilterValueEnum?): ParseSGTIN.BuildStep?
}