package com.apes.capuchin.rfidcorelib.readers.zebra

import android.content.Context
import android.util.Log
import com.zebra.rfid.api3.RFIDReader
import com.zebra.scannercontrol.DCSSDKDefs
import com.zebra.scannercontrol.DCSScannerInfo
import com.zebra.scannercontrol.IDcsSdkApiDelegate
import com.zebra.scannercontrol.SDKHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ScannerManager(
    private val context: Context,
    private val delegate: IDcsSdkApiDelegate?
) {
    private var sdkHandler: SDKHandler? = null
    private val scannerList by lazy { mutableListOf<DCSScannerInfo>() }
    private var scannerId: Int = 0

    fun setupScannerSDK(reader: RFIDReader) {
        sdkHandler?.let {
            setupScanners(it)
        } ?: run {
            sdkHandler = SDKHandler(context)
            setupSDKHandler()
        }
        reader.let {
            scannerList.forEach { device ->
                if (device.scannerName.contains(it.hostName)) {
                    try {
                        scannerId = device.scannerID
                        sdkHandler?.dcssdkEstablishCommunicationSession(scannerId)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    fun scanCode() {
        val inXml = "<inArgs><scannerID>$scannerId</scannerID></inArgs>"
        CoroutineScope(Dispatchers.IO).launch {
            executeCommand(
                inXML = inXml,
                scannerID = scannerId
            )
        }
    }

    private fun executeCommand(
        opcode: DCSSDKDefs.DCSSDK_COMMAND_OPCODE = DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_DEVICE_PULL_TRIGGER,
        inXML: String,
        outXML: StringBuilder? = null,
        scannerID: Int
    ): Boolean {
        sdkHandler?.let { handler ->
            val result =
                handler.dcssdkExecuteCommandOpCodeInXMLForScanner(opcode, inXML, outXML, scannerID)
            Log.i(TAG, "execute command returned $result")
            return result == DCSSDKDefs.DCSSDK_RESULT.DCSSDK_RESULT_SUCCESS
        }
        return false
    }

    private fun setupSDKHandler() {
        sdkHandler?.let {
            it.dcssdkSetOperationalMode(DCSSDKDefs.DCSSDK_MODE.DCSSDK_OPMODE_USB_CDC)
            it.dcssdkSetOperationalMode(DCSSDKDefs.DCSSDK_MODE.DCSSDK_OPMODE_BT_LE)
            it.dcssdkSetOperationalMode(DCSSDKDefs.DCSSDK_MODE.DCSSDK_OPMODE_BT_NORMAL)

            it.dcssdkSetDelegate(delegate)

            val mask = 0
            mask or DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SCANNER_APPEARANCE.value or DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SCANNER_DISAPPEARANCE.value
            mask or DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_BARCODE.value or DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SESSION_ESTABLISHMENT.value or DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SESSION_TERMINATION.value

            it.dcssdkSubsribeForEvents(mask)
        }
    }

    private fun setupScanners(handler: SDKHandler) {
        val availableScanners: List<DCSScannerInfo> = handler.dcssdkGetAvailableScannersList()
        scannerList.clear()
        when {
            availableScanners.isNotEmpty() -> {
                availableScanners.forEach { scanner ->
                    scannerList.add(scanner)
                }
            }
            else -> Log.d("", "Available scanners null")
        }
    }

    companion object {
        private val TAG: String = ScannerManager::class.java.simpleName
    }
}