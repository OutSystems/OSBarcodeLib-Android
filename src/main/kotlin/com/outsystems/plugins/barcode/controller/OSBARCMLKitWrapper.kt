package com.outsystems.plugins.barcode.controller

import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.outsystems.plugins.barcode.model.OSBARCError
import kotlinx.coroutines.runBlocking

/**
 * Helper class that implements the OSBARCScanLibraryInterface
 * to scan an image using the ML Kit library.
 */
class OSBARCMLKitWrapper: OSBARCScanLibraryInterface {

    companion object {
        private const val LOG_TAG = "OSBARCMLKitWrapper"
    }

    /**
     * Scans an image looking for barcodes, using the ML Kit library.
     * @param imageProxy - ImageProxy object that represents the image to be analyzed.
     * @param onSuccess - The code to be executed if the operation was successful.
     * @param onError - The code to be executed if the operation was not successful.
     */
    @OptIn(ExperimentalGetImage::class) override fun scanBarcode(
        imageProxy: ImageProxy,
        onSuccess: (String) -> Unit,
        onError: (OSBARCError) -> Unit
    ) {
        try {
            val options = BarcodeScannerOptions.Builder()
                .enableAllPotentialBarcodes()
                .build()
            val scanner = BarcodeScanning.getClient(options)
            val mediaImage = imageProxy.image

            if (mediaImage != null) {
                val image = InputImage.fromMediaImage(
                    mediaImage,
                    imageProxy.imageInfo.rotationDegrees
                )
                runBlocking {
                    scanner.process(image)
                        .addOnSuccessListener { barcodes ->
                            var result: String? = null
                            if (barcodes.isNotEmpty()) {
                                result = barcodes.first().rawValue
                            }
                            if (!result.isNullOrEmpty()) {
                                onSuccess(result)
                            }
                        }
                        .addOnFailureListener { e ->
                            e.message?.let { Log.e(LOG_TAG, it) }
                            onError(OSBARCError.MLKIT_LIBRARY_ERROR)
                        }
                        .addOnCompleteListener {
                            imageProxy.close()
                        }
                }
            }
        } catch (e: Exception) {
            e.message?.let { Log.e(LOG_TAG, it) }
            onError(OSBARCError.MLKIT_LIBRARY_ERROR)
        }
    }

}