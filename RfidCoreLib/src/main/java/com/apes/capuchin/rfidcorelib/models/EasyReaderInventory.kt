package com.apes.capuchin.rfidcorelib.models

import com.apes.capuchin.rfidcorelib.epctagcoder.result.BaseReading

data class EasyReaderInventory(
    val itemsRead: MutableSet<BaseReading> = mutableSetOf(),
    val error: String? = null
)
