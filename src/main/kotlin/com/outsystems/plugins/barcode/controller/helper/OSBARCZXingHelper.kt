package com.outsystems.plugins.barcode.controller.helper

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Log
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer

/**
 * Helper class that implements the OSBARCZXingHelperInterface
 * to scan an image using the ZXing library.
 * It encapsulates all the code related with the ZXing library.
 */
class OSBARCZXingHelper: OSBARCZXingHelperInterface {

    companion object {
        private const val LOG_TAG = "OSBARCZXingHelper"
    }

    /**
     * Converts a ByteArray into a Bitmap using BitmapFactory
     * @param imageBytes - ByteArray to convert
     * @return the resulting bitmap.
     */
    override fun bitmapFromImageBytes(imageBytes: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
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
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
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
            val result = MultiFormatReader().apply {
                setHints(
                    mapOf(
                        DecodeHintType.TRY_HARDER to arrayListOf(true)
                    )
                )
            }.decode(binaryBitmap)
            onSuccess(result.text)
        } catch (e: NotFoundException) {
            // keep trying
            e.message?.let { Log.d(LOG_TAG, it) }
        } catch (e: Exception) {
            e.message?.let { Log.e(LOG_TAG, it) }
            onError()
        }

    }

}