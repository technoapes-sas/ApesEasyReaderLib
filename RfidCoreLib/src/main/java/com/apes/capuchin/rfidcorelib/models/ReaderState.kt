package com.apes.capuchin.rfidcorelib.models

import com.apes.capuchin.rfidcorelib.enums.AntennaPowerLevelsEnum
import com.apes.capuchin.rfidcorelib.enums.BeeperLevelsEnum
import com.apes.capuchin.rfidcorelib.enums.SessionControlEnum
import com.apes.capuchin.rfidcorelib.epctagcoder.result.BaseReading

data class ConnectionState(
    val isReaderConnected: Boolean = false,
    val error: EasyResponse? = null
)

data class ReadState(
    val isReading: Boolean = false,
    val itemsRead: Set<BaseReading> = setOf(),
    val locateTag: LocateTag? = null
)

data class ConfigState(
    val antennaPower: AntennaPowerLevelsEnum? = AntennaPowerLevelsEnum.MAX,
    val antennaSound: BeeperLevelsEnum? = BeeperLevelsEnum.MAX,
    val sessionControl: SessionControlEnum? = SessionControlEnum.S0
)