package com.apes.capuchin.rfidcorelib.models

import com.apes.capuchin.rfidcorelib.utils.EMPTY_STRING

data class LocateTag(
    val search: String = EMPTY_STRING,
    val rssi: Long = 0L
)
