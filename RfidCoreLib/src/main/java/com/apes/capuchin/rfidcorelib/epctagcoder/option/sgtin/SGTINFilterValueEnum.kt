package com.apes.capuchin.rfidcorelib.epctagcoder.option.sgtin

enum class SGTINFilterValueEnum(val value: Int) {

    ALL_OTHERS_0(value = 0),
    POS_ITEM_1(value = 1),
    CASE_2(value = 2),
    RESERVED_3(value = 3),
    INNER_PACK_4(value = 4),
    RESERVED_5(value = 5),
    UNIT_LOAD_6(value = 6),
    COMPONENT_7(value = 7);

    companion object {
        fun findByValue(value: Int): SGTINFilterValueEnum {
            val values = entries.associateBy(SGTINFilterValueEnum::value)
            return values[value] ?: ALL_OTHERS_0
        }
    }
}