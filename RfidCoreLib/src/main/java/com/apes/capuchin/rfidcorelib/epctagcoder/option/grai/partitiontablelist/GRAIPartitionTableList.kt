package com.apes.capuchin.rfidcorelib.epctagcoder.option.grai.partitiontablelist

import com.apes.capuchin.rfidcorelib.epctagcoder.option.TableItem
import com.apes.capuchin.rfidcorelib.epctagcoder.option.grai.GRAITagSizeEnum

class GRAIPartitionTableList(tagSizeEnum: GRAITagSizeEnum) {

    private var tableItemList: List<TableItem> = listOf(
        TableItem(partitionValue = 0, m = 40, l = 12, n = 4, digits = 0),
        TableItem(partitionValue = 1, m = 37, l = 11, n = 7, digits = 1),
        TableItem(partitionValue = 2, m = 34, l = 10, n = 10, digits = 2),
        TableItem(partitionValue = 3, m = 30, l = 9, n = 14, digits = 3),
        TableItem(partitionValue = 4, m = 27, l = 8, n = 17, digits = 4),
        TableItem(partitionValue = 5, m = 24, l = 7, n = 20, digits = 5),
        TableItem(partitionValue = 6, m = 20, l = 6, n = 24, digits = 6)
    )

    fun getPartitionByL(index: Int): TableItem {
        return tableItemList.firstOrNull { it.l == index }
            ?: tableItemList.first { it.l == 6 }
    }

    fun getPartitionByValue(index: Int): TableItem {
        return tableItemList.firstOrNull { it.partitionValue == index }
            ?: tableItemList.first { it.partitionValue == 6 }
    }
}