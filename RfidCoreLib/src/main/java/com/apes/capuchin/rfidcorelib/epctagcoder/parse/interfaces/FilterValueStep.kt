package com.apes.capuchin.rfidcorelib.epctagcoder.parse.interfaces

interface FilterValueStep {
    fun withFilterValue(filterValue: Any?): BuildStep?
}