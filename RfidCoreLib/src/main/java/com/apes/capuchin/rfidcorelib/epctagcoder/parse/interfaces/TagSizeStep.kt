package com.apes.capuchin.rfidcorelib.epctagcoder.parse.interfaces


fun interface TagSizeStep {
    fun withTagSize(tagSize: Any?): FilterValueStep
}