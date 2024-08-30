package com.apes.capuchin.rfidcorelib.epctagcoder.parse.interfaces

fun interface FilterValueStep {
    fun withFilterValue(filterValue: Any?): BuildStep
}