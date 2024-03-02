package com.apes.capuchin.rfidcorelib

import com.apes.capuchin.rfidcorelib.enums.AntennaPowerLevelsEnum
import com.apes.capuchin.rfidcorelib.enums.SessionControlEnum
import com.apes.capuchin.rfidcorelib.models.EasyReaderInventory
import com.apes.capuchin.rfidcorelib.models.EasyResponse
import com.apes.capuchin.rfidcorelib.models.LocateTag
import sun.jvm.hotspot.utilities.Observable
import sun.jvm.hotspot.utilities.Observer

class EasyReaderObserver : Observer {

    override fun update(observable: Observable?, args: Any?) {

    }

    fun onSessionControlChanged(session: SessionControlEnum?) = Unit

    fun onReaderConnected() = Unit

    fun onReaderDisconnected() = Unit

    fun onError(easyResponse: EasyResponse?) = Unit

    fun onItemsRead(easyReaderInventory: EasyReaderInventory?) = Unit

    fun onAntennaPowerChanged(antennaPower: AntennaPowerLevelsEnum?) = Unit

    fun onAntennaSoundChanged(level: Int) = Unit

    fun onConnectionFailed(easyResponse: EasyResponse?) = Unit

    fun onLocateTag(locateTag: LocateTag?) = Unit

    fun onStartReading(isRunning: Boolean) = Unit

}