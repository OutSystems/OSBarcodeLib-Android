package com.outsystems.plugins.barcode.controller.helper

import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.outsystems.plugins.barcode.model.OSBARCScannerHint

/**
 * Helper class that implements the OSBARCZXingHelperInterface
 * to scan an image using the ZXing library.
 * It encapsulates all the code related with the ZXing library.
 */
class OSBARCZXingHelper(private val hint: OSBARCScannerHint?): OSBARCZXingHelperInterface {

    companion object {
        private const val LOG_TAG = "OSBARCZXingHelper"
    }

    private val reader: MultiFormatReader by lazy {
        val format = hint.toZXingBarcodeFormat()
        MultiFormatReader().apply {
            if (format != null) {
                setHints(
                    mapOf(DecodeHintType.POSSIBLE_FORMATS to setOf(format))
                )
            }
        }
    }

    /**
     * Rotates a bitmap, provided with the rotation degrees.
     * @param bitmap - Bitmap object to rotate
     * @param rotationDegrees - degrees to rotate the image.
     * @return the resulting bitmap.
     */
    override fun rotateBitmap(bitmap: Bitmap, rotationDegrees: Int): Bitmap {
        // create a matrix for rotation
        val matrix = Matrix()
        matrix.postRotate(rotationDegrees.toFloat())

        // actually rotate the image
        return Bitmap.createBitmap(
            bitmap,
            0, // 0 is the x coordinate of the first pixel in source bitmap
            0, // 0 is the y coordinate of the first pixel in source bitmap
            bitmap.width, // number of pixels in each row
            bitmap.height, // number of rows
            matrix, // matrix to be used for rotation
            true // true states that source bitmap should be filtered using matrix (rotation)
        )
    }

    /**
     * Scans an image looking for barcodes, using the ZXing library.
     * @param pixels - IntArray that represents the image to be analyzed.
     * @param onSuccess - The code to be executed if the operation was successful.
     * @param onError - The code to be executed if the operation was not successful.
     */
    override fun decodeImage(
        pixels: IntArray, width: Int, height: Int,
        onSuccess: (String) -> Unit,
        onError: () -> Unit
    ) {
        try {
            val source = RGBLuminanceSource(width, height, pixels)
            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
            val result = reader.decodeWithState(binaryBitmap)
            onSuccess(result.text)
        } catch (e: NotFoundException) {
            // keep trying, no barcode was found in this camera frame
            e.message?.let { Log.d(LOG_TAG, it) }
        } catch (e: Exception) {
            e.message?.let { Log.e(LOG_TAG, it) }
            onError()
        }
    }

    private fun OSBARCScannerHint?.toZXingBarcodeFormat(): BarcodeFormat? = when (this) {
        OSBARCScannerHint.QR_CODE -> BarcodeFormat.QR_CODE
        OSBARCScannerHint.AZTEC -> BarcodeFormat.AZTEC
        OSBARCScannerHint.CODABAR -> BarcodeFormat.CODABAR
        OSBARCScannerHint.CODE_39 -> BarcodeFormat.CODE_39
        OSBARCScannerHint.CODE_93 -> BarcodeFormat.CODE_93
        OSBARCScannerHint.CODE_128 -> BarcodeFormat.CODE_128
        OSBARCScannerHint.DATA_MATRIX -> BarcodeFormat.DATA_MATRIX
        OSBARCScannerHint.MAXICODE -> BarcodeFormat.MAXICODE
        OSBARCScannerHint.ITF -> BarcodeFormat.ITF
        OSBARCScannerHint.EAN_13 -> BarcodeFormat.EAN_13
        OSBARCScannerHint.EAN_8 -> BarcodeFormat.EAN_8
        OSBARCScannerHint.PDF_417 -> BarcodeFormat.PDF_417
        OSBARCScannerHint.RSS_14 -> BarcodeFormat.RSS_14
        OSBARCScannerHint.RSS_EXPANDED -> BarcodeFormat.RSS_EXPANDED
        OSBARCScannerHint.UPC_A -> BarcodeFormat.UPC_A
        OSBARCScannerHint.UPC_E -> BarcodeFormat.UPC_E
        OSBARCScannerHint.UPC_EAN_EXTENSION -> BarcodeFormat.UPC_EAN_EXTENSION
        OSBARCScannerHint.UNKNOWN -> null
        null -> null
    }
}