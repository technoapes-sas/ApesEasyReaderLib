package com.apes.capuchin.rfidcorelib.models

import com.apes.capuchin.rfidcorelib.enums.AntennaPowerLevelsEnum
import com.apes.capuchin.rfidcorelib.enums.BeeperLevelsEnum
import com.apes.capuchin.rfidcorelib.enums.SessionControlEnum
import com.apes.capuchin.rfidcorelib.epctagcoder.result.BaseReading

data class ReaderState(
    val isReaderConnected: Boolean = false,
    val isReading: Boolean = false,
    val itemsRead: Set<BaseReading>? = null,
    val locateTag: LocateTag? = null,
    val antennaPower: AntennaPowerLevelsEnum? = null,
    val antennaSound: BeeperLevelsEnum? = null,
    val sessionControl: SessionControlEnum? = null,
    val error: EasyResponse? = null
)
