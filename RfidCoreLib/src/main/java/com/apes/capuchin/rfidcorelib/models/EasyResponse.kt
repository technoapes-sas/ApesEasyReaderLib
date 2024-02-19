package com.apes.capuchin.rfidcorelib.models

data class EasyResponse(
    val success: Boolean? = null,
    val message: String? = null,
    val code: Byte? = null
)