package com.outsystems.plugins.barcode.controller

import android.util.Log
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.outsystems.plugins.barcode.model.OSBARCError
import kotlinx.coroutines.runBlocking

class OSBARCMLKitWrapper: OSBARCScanLibraryInterface {

    companion object {
        private const val LOG_TAG = "OSBARCMLKitWrapper"
    }

    override fun scanBarcode(
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
                }
            }
        } catch (e: Exception) {
            e.message?.let { Log.e(LOG_TAG, it) }
            onError(OSBARCError.MLKIT_LIBRARY_ERROR)
        } finally {
            imageProxy.close()
        }
    }

}