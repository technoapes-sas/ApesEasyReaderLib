package com.apes.capuchin.capuchinrfidlib

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.apes.capuchin.rfidcorelib.models.ConfigReader
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val readerManager = ReaderManager(applicationContext)
        readerManager.configReader(ConfigReader())
        lifecycleScope.launch {
            readerManager.readerState.collect { readerState ->
                // Handle reader state changes
                when {
                    readerState.isReaderConnected -> {
                        // Reader is connected
                        Toast.makeText(
                            applicationContext,
                            "Reader is connected",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }
}