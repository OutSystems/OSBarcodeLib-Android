package com.outsystems.plugins.barcode.controller

import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.common.Barcode
import com.outsystems.plugins.barcode.controller.helper.OSBARCMLKitHelperInterface
import com.outsystems.plugins.barcode.model.OSBARCError
import com.outsystems.plugins.barcode.model.OSBARCScanResult
import com.outsystems.plugins.barcode.model.OSBARCScannerHint

/**
 * Wrapper class that implements the OSBARCScanLibraryInterface
 * to scan an image using the ML Kit library.
 */
class OSBARCMLKitWrapper(private val helper: OSBARCMLKitHelperInterface): OSBARCScanLibraryInterface {

    companion object {
        private const val LOG_TAG = "OSBARCMLKitWrapper"
    }

    /**
     * Scans an image looking for barcodes, using the ML Kit library.
     * @param imageProxy - ImageProxy object that represents the image to be analyzed.
     * @param onSuccess - The code to be executed if the operation was successful.
     * @param onError - The code to be executed if the operation was not successful.
     */
    override fun scanBarcode(
        imageProxy: ImageProxy,
        imageBitmap: Bitmap,
        onSuccess: (OSBARCScanResult) -> Unit,
        onError: (OSBARCError) -> Unit
    ) {
        try {
            helper.decodeImage(imageProxy, imageBitmap,
                { barcodes ->
                    barcodes.firstOrNull()?.let { barcode ->
                        val result = OSBARCScanResult(
                            text = barcode.rawValue ?: "",
                            format = barcode.format.toOSBARCScannerHint()
                        )
                        if (result.text.isNotEmpty()) {
                            onSuccess(result)
                        }
                    }
                },
                {
                    onError(OSBARCError.MLKIT_LIBRARY_ERROR)
                }
            )
        } catch (e: Exception) {
            e.message?.let { Log.e(LOG_TAG, it) }
            onError(OSBARCError.MLKIT_LIBRARY_ERROR)
        }
    }

    private fun Int?.toOSBARCScannerHint(): OSBARCScannerHint = when (this) {
        Barcode.FORMAT_QR_CODE -> OSBARCScannerHint.QR_CODE
        Barcode.FORMAT_AZTEC -> OSBARCScannerHint.AZTEC
        Barcode.FORMAT_CODABAR -> OSBARCScannerHint.CODABAR
        Barcode.FORMAT_CODE_39 -> OSBARCScannerHint.CODE_39
        Barcode.FORMAT_CODE_93 -> OSBARCScannerHint.CODE_93
        Barcode.FORMAT_CODE_128 -> OSBARCScannerHint.CODE_128
        Barcode.FORMAT_DATA_MATRIX -> OSBARCScannerHint.DATA_MATRIX
        Barcode.FORMAT_ITF -> OSBARCScannerHint.ITF
        Barcode.FORMAT_EAN_13 -> OSBARCScannerHint.EAN_13
        Barcode.FORMAT_EAN_8 -> OSBARCScannerHint.EAN_8
        Barcode.FORMAT_PDF417 -> OSBARCScannerHint.PDF_417
        Barcode.FORMAT_UPC_A -> OSBARCScannerHint.UPC_A
        Barcode.FORMAT_UPC_E -> OSBARCScannerHint.UPC_E

        // Formats not supported by ML Kit → map to UNKNOWN
        else -> OSBARCScannerHint.UNKNOWN
    }
}