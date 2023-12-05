package com.outsystems.plugins.barcode.controller.helper

import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
     * Converts a ByteArray into a Bitmap using BitmapFactory
     * @param imageBytes - ByteArray to convert
     * @return the resulting bitmap.
     */
    override fun bitmapFromImageBytes(imageBytes: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(
            imageBytes,
            0, // use 0 in the offset to decode from the beginning of imageBytes
            imageBytes.size // use byte array size as length because we want to decode the whole image
        )
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
        val options = BarcodeScannerOptions.Builder()
            .enableAllPotentialBarcodes()
            .build()
        val scanner = BarcodeScanning.getClient(options)
        val image = InputImage.fromBitmap(
            imageBitmap,
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