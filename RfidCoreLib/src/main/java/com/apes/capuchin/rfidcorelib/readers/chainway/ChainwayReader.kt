package com.apes.capuchin.rfidcorelib.readers.chainway

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.apes.capuchin.capuchinrfidlib.lib.R
import com.apes.capuchin.rfidcorelib.utils.BEEP_DELAY_TIME_MAX
import com.apes.capuchin.rfidcorelib.utils.BEEP_DELAY_TIME_MIN
import com.apes.capuchin.rfidcorelib.utils.COMMAND_FAIL
import com.apes.capuchin.rfidcorelib.utils.CONNECTION_FAILED_CODE
import com.apes.capuchin.rfidcorelib.utils.CONNECTION_SUCCEEDED_CODE
import com.apes.capuchin.rfidcorelib.utils.ERROR
import com.apes.capuchin.rfidcorelib.utils.SUCCESS
import com.apes.capuchin.rfidcorelib.enums.AntennaPowerLevelsEnum
import com.apes.capuchin.rfidcorelib.enums.BeeperLevelsEnum
import com.apes.capuchin.rfidcorelib.enums.ReadTypeEnum
import com.apes.capuchin.rfidcorelib.enums.ReaderModeEnum
import com.apes.capuchin.rfidcorelib.enums.SessionControlEnum
import com.apes.capuchin.rfidcorelib.enums.SettingsEnum
import com.apes.capuchin.rfidcorelib.models.EasyResponse
import com.apes.capuchin.rfidcorelib.models.StartStopReading
import com.apes.capuchin.rfidcorelib.readers.EasyReader
import com.apes.capuchin.rfidcorelib.utils.CONNECTION_CLOSE_CODE
import com.apes.capuchin.rfidcorelib.utils.InitTask
import com.apes.capuchin.rfidcorelib.utils.SoundPlayer
import com.rscja.deviceapi.RFIDWithUHFBLE
import com.rscja.deviceapi.RFIDWithUHFUART
import com.rscja.deviceapi.entity.UHFTAGInfo
import com.rscja.deviceapi.interfaces.ConnectionStatus
import com.rscja.deviceapi.interfaces.IUHF
import com.rscja.deviceapi.interfaces.KeyEventCallback
import com.rscja.deviceapi.interfaces.ScanBTCallback
import java.util.concurrent.Executors
import java.util.logging.Logger

class ChainwayReader(
    private val context: Context,
    private val deviceAddress: String? = null
) : EasyReader() {

    private val soundPlayer: SoundPlayer by lazy { SoundPlayer() }
    private val scannerManager: ScannerManager by lazy { ScannerManager() }
    private val readerConfiguration: ReaderConfiguration by lazy { ReaderConfiguration() }

    private var reader: RFIDWithUHFUART? = null
    private var btReader: RFIDWithUHFBLE? = null
    private var loopFlag = false
    private var isReaderConnected = false

    override fun connectReader() {
        try {
            if (deviceAddress == null) {
                reader = RFIDWithUHFUART.getInstance()
                initUhfReader()
            } else {
                btReader = RFIDWithUHFBLE.getInstance()
                initBtReader()
            }
        } catch (ex: Exception) {
            notifyObservers(
                EasyResponse(
                    ERROR,
                    context.getString(R.string.fail_connect),
                    CONNECTION_FAILED_CODE
                )
            )
            Logger.getLogger(ChainwayReader::class.java.name).warning(ex.message)
            return
        }
    }

    override fun disconnectReader() {
        val result = reader?.free() ?: btReader?.free()
        isReaderConnected = !(result ?: false)
        soundPlayer.releaseSoundPool()
        scannerManager.closeScanner()
        notifyObservers(
            EasyResponse(
                success = SUCCESS,
                message = context.getString(R.string.disconnect_reader),
                code = CONNECTION_CLOSE_CODE
            )
        )
    }

    override fun isReaderConnected(): Boolean = isReaderConnected

    override fun initRead() {
        when (readerMode) {
            ReaderModeEnum.BARCODE_MODE -> scannerManager.startScan()
            else -> when (readType) {
                ReadTypeEnum.SEARCH_TAG -> startLocated()
                else -> readInventory()
            }
        }
    }

    override fun stopRead() {
        if (loopFlag) {
            when (readerMode) {
                ReaderModeEnum.BARCODE_MODE -> scannerManager.stopScan()
                else -> {
                    when (readType) {
                        ReadTypeEnum.SEARCH_TAG -> reader?.stopLocation()
                            ?: btReader?.stopLocation()

                        else -> reader?.stopInventory() ?: btReader?.stopInventory()
                    }
                    loopFlag = false
                    notifyObservers(StartStopReading(false))
                }
            }
        }
    }

    override fun initReader() {
        connectReader()
    }

    override fun setSessionControl(sessionControlEnum: SessionControlEnum) {
        when {
            readerConfiguration.setSessionControl(reader, btReader, sessionControlEnum) ->
                notifySettingsChange(
                    SettingsEnum.CHANGE_SESSION_CONTROL,
                    session = sessionControlEnum
                )

            else -> notifyCommandFail()
        }
    }

    override fun getSessionControl(): SessionControlEnum {
        return readerConfiguration.getSessionControl(reader, btReader)
    }

    override fun setAntennaSound(beeperLevelsEnum: BeeperLevelsEnum) {
        readerConfiguration.setAntennaSound(beeperLevelsEnum, soundPlayer)
        notifySettingsChange(SettingsEnum.CHANGE_ANTENNA_SOUND, beeperLevel = beeperLevelsEnum)
    }

    override fun getAntennaSound(): BeeperLevelsEnum {
        return readerConfiguration.getAntennaSound(soundPlayer)
    }

    override fun setAntennaPower(antennaPowerLevelsEnum: AntennaPowerLevelsEnum) {
        when {
            readerConfiguration.setAntennaPower(reader, btReader, antennaPowerLevelsEnum) ->
                notifySettingsChange(
                    SettingsEnum.CHANGE_ANTENNA_POWER,
                    power = antennaPowerLevelsEnum
                )

            else -> notifyCommandFail()
        }
    }

    override fun getAntennaPower(): AntennaPowerLevelsEnum {
        return readerConfiguration.getAntennaPower(reader, btReader)
    }

    private fun initSoundPlayer() {
        soundPlayer.apply {
            initSound(context)
            playSound(1)
        }
    }

    private fun initUhfReader() {
        val executor = Executors.newSingleThreadExecutor()
        InitTask(executor).execute {
            isReaderConnected = reader?.init() ?: false
            when {
                isReaderConnected -> {
                    initSoundPlayer()
                    initBarcodeReader()
                    notifyObservers(
                        EasyResponse(
                            success = SUCCESS,
                            message = context.getString(R.string.reader_connected),
                            code = CONNECTION_SUCCEEDED_CODE
                        )
                    )
                }

                else -> {
                    notifyObservers(
                        EasyResponse(
                            success = ERROR,
                            message = context.getString(R.string.reader_connection_failed),
                            code = CONNECTION_FAILED_CODE
                        )
                    )
                }
            }
        }
    }

    private fun initBtReader() {

        isReaderConnected = btReader?.init(context) ?: false
        if (isReaderConnected) {
            btReader?.setKeyEventCallback(object : KeyEventCallback {
                override fun onKeyDown(i: Int) {
                    initRead()
                }

                override fun onKeyUp(i: Int) {
                    stopRead()
                }
            })

            btReader?.startScanBTDevices { device, _, _ ->
                btReader?.connect(device.address) { connectionStatus, _ ->
                    when (connectionStatus) {
                        ConnectionStatus.CONNECTED -> {
                            initSoundPlayer()
                            initBarcodeReader()
                            notifyObservers(
                                EasyResponse(
                                    success = SUCCESS,
                                    message = context.getString(R.string.reader_connected),
                                    code = CONNECTION_SUCCEEDED_CODE
                                )
                            )
                        }

                        ConnectionStatus.DISCONNECTED -> {
                            notifyObservers(
                                EasyResponse(
                                    success = ERROR,
                                    message = context.getString(R.string.reader_connection_failed),
                                    code = CONNECTION_FAILED_CODE
                                )
                            )
                        }

                        else -> Unit
                    }
                }
            }
        }
    }

    private fun initBarcodeReader() {
        scannerManager.openScanner(context)
        scannerManager.barcodeListener { scan ->
            soundPlayer.playSound(1)
            notifyItemRead(epc = scan, rssi = 0)
        }
    }

    private fun startLocated() {
        if (!loopFlag && searchTag.isNotEmpty()) {
            loopFlag = reader?.startLocation(context, searchTag, IUHF.Bank_EPC, 32, ::handleLocated)
                ?: btReader?.startLocation(context, searchTag, IUHF.Bank_EPC, 32, ::handleLocated)
                        ?: false
            notifyObservers(StartStopReading(loopFlag))
        }
    }

    private fun handleLocated(i: Int, b: Boolean) {
        val interval =
            BEEP_DELAY_TIME_MIN + (((BEEP_DELAY_TIME_MAX - BEEP_DELAY_TIME_MIN) * (100 - i)) / 100)
        soundPlayer.playSound(1, interval)
        notifyItemRead(epc = searchTag, rssi = i)
    }

    private fun readInventory() {
        if (!loopFlag) {
            if (reader != null) {
                reader?.setInventoryCallback(::handleInventory)
            }
            if (btReader != null) {
                btReader?.setInventoryCallback(::handleInventory)
            }
            loopFlag = reader?.startInventoryTag() ?: btReader?.startInventoryTag() ?: false
            if (!loopFlag) {
                stopRead()
            }
            notifyObservers(StartStopReading(loopFlag))
        }
    }

    private fun handleInventory(uhfTagInfo: UHFTAGInfo) {
        uhfTagInfo.epc?.let {
            soundPlayer.playSound(1)
            notifyItemRead(epc = it, rssi = getRssi(uhfTagInfo.rssi).toInt())
        }
    }

    private fun getRssi(rssi: String): Double = rssi.replace(",", ".").toDouble()

    private fun notifyCommandFail() {
        notifyObservers(EasyResponse(ERROR, context.getString(R.string.command_fail), COMMAND_FAIL))
    }
}