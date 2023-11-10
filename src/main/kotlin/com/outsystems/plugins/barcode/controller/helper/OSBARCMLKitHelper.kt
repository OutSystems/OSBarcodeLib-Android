package com.outsystems.plugins.barcode.controller.helper

import android.media.Image
import android.util.Log
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.runBlocking

/**
 * Helper class that implements the OSBARCMLKitHelperInterface
 * to scan an image using the ML Kit library.
 * It encapsulates all the code related with the ML Kit library.
 */
class OSBARCMLKitHelper: OSBARCMLKitHelperInterface {
    companion object {
        private const val LOG_TAG = "OSBARCMLKitHelper"
    }

    /**
     * Scans an image looking for barcodes, using the ML Kit library.
     * @param imageProxy - ImageProxy object that represents the image to be analyzed.
     * @param mediaImage - Image object that represents the image to be analyzed.
     * @param onSuccess - The code to be executed if the operation was successful.
     * @param onError - The code to be executed if the operation was not successful.
     */
    override fun decodeImage(
        imageProxy: ImageProxy,
        mediaImage: Image,
        onSuccess: (MutableList<Barcode>) -> Unit,
        onError: () -> Unit
    ) {
        val options = BarcodeScannerOptions.Builder()
            .enableAllPotentialBarcodes()
            .build()
        val scanner = BarcodeScanning.getClient(options)
        val image = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )
        runBlocking {
            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    onSuccess(barcodes)
                }
                .addOnFailureListener { e ->
                    e.message?.let { Log.e(LOG_TAG, it) }
                    onError()
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        }
    }

}