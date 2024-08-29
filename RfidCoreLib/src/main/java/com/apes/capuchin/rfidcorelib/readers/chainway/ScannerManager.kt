package com.apes.capuchin.rfidcorelib.readers.chainway

import android.content.Context
import com.rscja.barcode.BarcodeDecoder
import com.rscja.barcode.BarcodeFactory

class ScannerManager {

    private val barcodeDecoder: BarcodeDecoder by lazy {
        BarcodeFactory.getInstance().barcodeDecoder
    }

    fun openScanner(context: Context) {
        if (!barcodeDecoder.isOpen) {
            barcodeDecoder.open(context)
        }
    }

    fun closeScanner() {
        if (barcodeDecoder.isOpen) {
            barcodeDecoder.close()
        }
    }

    fun barcodeListener(scan: (String) -> Unit) {
        barcodeDecoder.setDecodeCallback { barcodeEntity ->
            if (barcodeEntity.resultCode == BarcodeDecoder.DECODE_SUCCESS) {
                scan(barcodeEntity.barcodeData)
            }
        }
    }

    fun startScan() {
        barcodeDecoder.startScan()
    }

    fun stopScan() {
        barcodeDecoder.stopScan()
    }
}