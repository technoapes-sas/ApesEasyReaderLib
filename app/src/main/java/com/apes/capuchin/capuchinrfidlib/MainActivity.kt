package com.apes.capuchin.capuchinrfidlib

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.apes.capuchin.rfidcorelib.EasyReader
import com.apes.capuchin.rfidcorelib.EasyReading
import com.apes.capuchin.rfidcorelib.enums.CoderEnum
import com.apes.capuchin.rfidcorelib.enums.ReadModeEnum
import com.apes.capuchin.rfidcorelib.enums.ReadTypeEnum
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var easyReader: EasyReader

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val easyReading = EasyReading.Builder()
            .coder(CoderEnum.SGTIN)
            .readMode(ReadModeEnum.NOTIFY_BY_ITEM_READ)
            .readType(ReadTypeEnum.INVENTORY)
            .companyPrefixes(emptyList())
            .build(applicationContext)

        easyReader = easyReading.easyReader
        subscribeToObservable()
    }

    private fun subscribeToObservable() {
        CoroutineScope(Dispatchers.IO).launch {
            easyReader.observer.onReaderConnected.collect { onReaderConnected ->
                val message = when {
                    onReaderConnected -> "Reader connected"
                    else -> "Reader connection failed"
                }
                Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
            }
            easyReader.observer.onReaderConnectionFailed.collect {

            }
            easyReader.observer.onReaderError.collect {

            }
            easyReader.observer.onAntennaPowerChanged.collect {

            }
            easyReader.observer.onAntennaSoundChanged.collect {

            }
            easyReader.observer.onSessionControlChanged.collect {

            }
            easyReader.observer.onStartReading.collect {

            }
            easyReader.observer.onItemsRead.collect {

            }
            easyReader.observer.onLocateTag.collect {

            }
        }
    }
}