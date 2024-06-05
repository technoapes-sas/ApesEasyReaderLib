package com.apes.capuchin.capuchinrfidlib

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.apes.capuchin.rfidcorelib.EasyReader
import com.apes.capuchin.rfidcorelib.EasyReading
import com.apes.capuchin.rfidcorelib.enums.AntennaPowerLevelsEnum
import com.apes.capuchin.rfidcorelib.enums.CoderEnum
import com.apes.capuchin.rfidcorelib.enums.ReadModeEnum
import com.apes.capuchin.rfidcorelib.enums.ReadTypeEnum
import com.apes.capuchin.rfidcorelib.enums.ReaderModeEnum
import com.apes.capuchin.rfidcorelib.enums.SessionControlEnum
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private var easyReader: EasyReader? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val easyReading = EasyReading.Builder()
            .coder(CoderEnum.SGTIN)
            .readMode(ReadModeEnum.NOTIFY_BY_ITEM_READ)
            .readType(ReadTypeEnum.INVENTORY)
            .companyPrefixes(listOf("7705751", "7701749"))
            .build(applicationContext)

        easyReader = easyReading.easyReader
        subscribeToObservable()
    }

    private fun configReader() {
        easyReader?.let {
            println("Configuring reader")
            it.readerModeEnum = ReaderModeEnum.RFID_MODE
            it.readModeEnum = ReadModeEnum.NOTIFY_BY_ITEM_READ
            it.readTypeEnum = ReadTypeEnum.INVENTORY
            it.setAntennaPower(AntennaPowerLevelsEnum.MAX)
            it.setSessionControl(SessionControlEnum.S0)
        }
    }

    private fun subscribeToObservable() {
        MainScope().launch {
            easyReader?.let {
                it.observer.onReaderConnected.collect { onReaderConnected ->
                    val message = when {
                        onReaderConnected -> {
                            configReader()
                            "Reader connected"
                        }
                        else -> "Reader connection failed"
                    }
                    Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
                }
            }
        }
        MainScope().launch {
            easyReader?.let {
                it.observer.onReaderConnectionFailed.collect { abc ->
                    Toast.makeText(applicationContext, abc.message.orEmpty(), Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
        MainScope().launch {
            easyReader?.let {
                it.observer.onReaderError.collect { error ->
                    Toast.makeText(applicationContext, error.message.orEmpty(), Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
        MainScope().launch {
            easyReader?.let {
                it.observer.onAntennaPowerChanged.collect { power ->
                    Toast.makeText(applicationContext, power.name, Toast.LENGTH_SHORT).show()
                }
            }
        }
        MainScope().launch {
            easyReader?.let {
                it.observer.onAntennaSoundChanged.collect { sound ->
                    Toast.makeText(applicationContext, sound.name, Toast.LENGTH_SHORT).show()
                }
            }
        }
        MainScope().launch {
            easyReader?.let {
                it.observer.onSessionControlChanged.collect { session ->
                    Toast.makeText(applicationContext, session.name, Toast.LENGTH_SHORT).show()
                }
            }
        }
        MainScope().launch {
            easyReader?.let {
                it.observer.onStartReading.collect { onStart ->
                    val message =
                        if (onStart.startStop == true) "Lectura iniciada" else "Lectura detenida"
                    Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
                }
            }
        }
        MainScope().launch {
            easyReader?.let {
                it.observer.onItemsRead.collect { inventory ->
                    Toast.makeText(applicationContext, "${inventory.itemsRead.size}", Toast.LENGTH_SHORT).show()
                }
            }
        }
        MainScope().launch {
            easyReader?.let {
                it.observer.onLocateTag.collect { locate ->
                    Toast.makeText(applicationContext, locate.search.orEmpty(), Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }
}