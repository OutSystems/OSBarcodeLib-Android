package com.outsystems.plugins.barcode.controller

import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.outsystems.plugins.barcode.model.OSBARCError
import java.lang.Exception

/**
 * This class is responsible for implementing the ImageAnalysis.Analyzer interface,
 * and overriding its analyze() method to scan for barcodes in the image frames.
 */
class OSBARCBarcodeAnalyzer(
    private val scanLibrary: OSBARCScanLibraryInterface,
    private val onBarcodeScanned: (String) -> Unit,
    private val onScanningError: (OSBARCError) -> Unit
): ImageAnalysis.Analyzer {

    companion object {
        private const val LOG_TAG = "OSBARCBarcodeAnalyzer"
    }

    /**
     * Overrides the analyze() method from ImageAnalysis.Analyzer,
     * calling the respective implementation of OSBARCScanLibraryInterface
     * to scan for barcodes in the image.
     * @param image - ImageProxy object that represents the image to be analyzed.
     */
    override fun analyze(image: ImageProxy) {
        try {
            scanLibrary.scanBarcode(
                image,
                {
                    onBarcodeScanned(it)
                },
                {
                    onScanningError(it)
                }
            )
        } catch (e: Exception) {
            e.message?.let { Log.e(LOG_TAG, it) }
            onScanningError(OSBARCError.SCANNING_GENERAL_ERROR)
        }
    }

}