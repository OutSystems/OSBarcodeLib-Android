package com.outsystems.plugins.barcode.controller.helper

import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.outsystems.plugins.barcode.model.OSBARCScannerHint

/**
 * Helper class that implements the OSBARCMLKitHelperInterface
 * to scan an image using the ML Kit library.
 * It encapsulates all the code related with the ML Kit library.
 */
class OSBARCMLKitHelper(private val hint: OSBARCScannerHint?): OSBARCMLKitHelperInterface {
    companion object {
        private const val LOG_TAG = "OSBARCMLKitHelper"
    }

    private val scanner by lazy {
        val options = BarcodeScannerOptions.Builder().apply {
            val format = hint.toMLKitBarcodeFormat()
            if (format != null) {
                setBarcodeFormats(format)
            } else {
                enableAllPotentialBarcodes()
            }
        }.build()
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

    private fun OSBARCScannerHint?.toMLKitBarcodeFormat(): Int? = when (this) {
        OSBARCScannerHint.QR_CODE -> Barcode.FORMAT_QR_CODE
        OSBARCScannerHint.AZTEC -> Barcode.FORMAT_AZTEC
        OSBARCScannerHint.CODABAR -> Barcode.FORMAT_CODABAR
        OSBARCScannerHint.CODE_39 -> Barcode.FORMAT_CODE_39
        OSBARCScannerHint.CODE_93 -> Barcode.FORMAT_CODE_93
        OSBARCScannerHint.CODE_128 -> Barcode.FORMAT_CODE_128
        OSBARCScannerHint.DATA_MATRIX -> Barcode.FORMAT_DATA_MATRIX
        OSBARCScannerHint.MAXICODE -> null // not supported by ML Kit
        OSBARCScannerHint.ITF -> Barcode.FORMAT_ITF
        OSBARCScannerHint.EAN_13 -> Barcode.FORMAT_EAN_13
        OSBARCScannerHint.EAN_8 -> Barcode.FORMAT_EAN_8
        OSBARCScannerHint.PDF_417 -> Barcode.FORMAT_PDF417
        OSBARCScannerHint.RSS_14 -> null // not supported by ML Kit
        OSBARCScannerHint.RSS_EXPANDED -> null // not supported by ML Kit
        OSBARCScannerHint.UPC_A -> Barcode.FORMAT_UPC_A
        OSBARCScannerHint.UPC_E -> Barcode.FORMAT_UPC_E
        OSBARCScannerHint.UPC_EAN_EXTENSION -> null // not supported by ML Kit
        OSBARCScannerHint.UNKNOWN -> null
        null -> null
    }
}