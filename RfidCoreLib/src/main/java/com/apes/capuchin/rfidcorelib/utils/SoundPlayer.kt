package com.apes.capuchin.rfidcorelib.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.apes.capuchin.capuchinrfidlib.lib.R
import java.util.concurrent.ConcurrentHashMap

class SoundPlayer {

    private val soundMap = ConcurrentHashMap<Int, Int>()
    private var soundPool: SoundPool? = null

    var soundLevel: Int = 100

    fun initSound(context: Context) {
        soundPool = SoundPool.Builder()
            .setMaxStreams(10)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .build()
        soundMap[1] = soundPool?.load(context, R.raw.barcodebeep, 1) ?: 0
        soundMap[2] = soundPool?.load(context, R.raw.serror, 1) ?: 0
    }

    fun releaseSoundPool() {
        soundPool?.release()
        soundPool = null
    }

    fun playSound(id: Int, interval: Int = soundLevel) {
        val volumeRatio = interval / 100f
        val sound = soundMap[id] ?: return
        soundPool?.play(sound, volumeRatio, volumeRatio, 1, 0, 1f)
    }
}