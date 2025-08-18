package com.outsystems.plugins.barcode.controller.helper

import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

/**
 * Helper class that implements the OSBARCMLKitHelperInterface
 * to scan an image using the ML Kit library.
 * It encapsulates all the code related with the ML Kit library.
 */
class OSBARCMLKitHelper: OSBARCMLKitHelperInterface {
    companion object {
        private const val LOG_TAG = "OSBARCMLKitHelper"
    }

    private val scanner by lazy {
        val options = BarcodeScannerOptions.Builder()
            .enableAllPotentialBarcodes()
            .build()
        BarcodeScanning.getClient(options)
    }

    /**
     * Scans an image looking for barcodes, using the ML Kit library.
     * @param imageProxy - ImageProxy object that represents the image to be analyzed.
     * @param imageBitmap - Bitmap object that represents the image to be analyzed.
     * @param onSuccess - The code to be executed if the operation was successful.
     * @param onError - The code to be executed if the operation was not successful.
     */
    override fun decodeImage(
        imageProxy: ImageProxy,
        imageBitmap: Bitmap,
        onSuccess: (MutableList<Barcode>) -> Unit,
        onError: () -> Unit
    ) {
        val image = InputImage.fromBitmap(
            imageBitmap,
            imageProxy.imageInfo.rotationDegrees
        )
        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                onSuccess(barcodes)
            }
            .addOnFailureListener { e ->
                e.message?.let { Log.e(LOG_TAG, it) }
                onError()
            }
    }

}