package com.outsystems.plugins.barcode.controller.helper

import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
     * Rotates a bitmap, provided with the rotation degrees.
     * @param bitmap - Bitmap object to rotate
     * @param rotationDegrees - degrees to rotate the image.
     * @return the resulting bitmap.
     */
    override suspend fun rotateBitmap(bitmap: Bitmap, rotationDegrees: Int): Bitmap =
        withContext(Dispatchers.Default) {
            return@withContext try {
                // create a matrix for rotation
                val matrix = Matrix()
                matrix.postRotate(rotationDegrees.toFloat())
                // actually rotate the image
                Bitmap.createBitmap(
                    bitmap,
                    0, // 0 is the x coordinate of the first pixel in source bitmap
                    0, // 0 is the y coordinate of the first pixel in source bitmap
                    bitmap.width, // number of pixels in each row
                    bitmap.height, // number of rows
                    matrix, // matrix to be used for rotation
                    true // true states that source bitmap should be filtered using matrix (rotation)
                )
            } finally {
                // do nothing
                // we need to use finally to avoid compilation error
            }
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
            // keep trying, no barcode was found in this camera frame
            e.message?.let { Log.d(LOG_TAG, it) }
        } catch (e: Exception) {
            e.message?.let { Log.e(LOG_TAG, it) }
            onError()
        }

    }

}