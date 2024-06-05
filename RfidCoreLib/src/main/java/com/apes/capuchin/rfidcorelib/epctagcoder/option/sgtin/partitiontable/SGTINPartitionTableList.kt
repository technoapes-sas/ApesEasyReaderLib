package com.apes.capuchin.rfidcorelib.epctagcoder.option.sgtin.partitiontable

import com.apes.capuchin.rfidcorelib.epctagcoder.option.TableItem

class SGTINPartitionTableList {

    private val tableItemList: List<TableItem> = listOf(
        TableItem(partitionValue = 0, m = 40, l = 12, n = 4, digits = 1),
        TableItem(partitionValue = 1, m = 37, l = 11, n = 7, digits = 2),
        TableItem(partitionValue = 2, m = 34, l = 10, n = 10, digits = 3),
        TableItem(partitionValue = 3, m = 30, l = 9, n = 14, digits = 4),
        TableItem(partitionValue = 4, m = 27, l = 8, n = 17, digits = 5),
        TableItem(partitionValue = 5, m = 24, l = 7, n = 20, digits = 6),
        TableItem(partitionValue = 6, m = 20, l = 6, n = 24, digits = 7)
    )

    fun getPartitionByL(index: Int): TableItem? {
        return tableItemList.firstOrNull { it.l == index }
    }

    fun getPartitionByValue(index: Int): TableItem? {
        return tableItemList.firstOrNull { it.partitionValue == index }
    }
}