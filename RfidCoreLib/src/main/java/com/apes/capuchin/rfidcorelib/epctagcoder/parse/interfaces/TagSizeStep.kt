package com.apes.capuchin.rfidcorelib.epctagcoder.parse.interfaces


interface TagSizeStep {
    fun withTagSize(tagSize: Any?): FilterValueStep
}