package com.outsystems.plugins.barcode.controller

import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.ImageProxy
import com.outsystems.plugins.barcode.controller.helper.OSBARCZXingHelperInterface
import com.outsystems.plugins.barcode.model.OSBARCError

/**
 * Wrapper class that implements the OSBARCScanLibraryInterface
 * to scan an image using the ZXing library.
 */
class OSBARCZXingWrapper(private val helper: OSBARCZXingHelperInterface) : OSBARCScanLibraryInterface {

    companion object {
        private const val LOG_TAG = "OSBARCZXingWrapper"
    }

    /**
     * Scans an image looking for barcodes, using the ZXing library.
     * @param imageProxy - ImageProxy object that represents the image to be analyzed.
     * @param onSuccess - The code to be executed if the operation was successful.
     * @param onError - The code to be executed if the operation was not successful.
     */
    override fun scanBarcode(
        imageProxy: ImageProxy,
        imageBitmap: Bitmap,
        onSuccess: (String) -> Unit,
        onError: (OSBARCError) -> Unit
    ) {
        try {
            var resultBitmap = imageBitmap

            // rotate the image if it's in portrait mode (rotation = 90 or 270 degrees)
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
            if (rotationDegrees == 90 || rotationDegrees == 270) {
                resultBitmap = helper.rotateBitmap(resultBitmap, rotationDegrees)
            }

            // scan image using zxing
            val width = resultBitmap.width
            val height = resultBitmap.height
            val pixels = IntArray(width * height)
            resultBitmap.getPixels(
                pixels,
                0, // first index to write into pixels
                width,
                0, // x coordinate of the first pixel to read
                0, // y coordinate of the first pixel to read
                width,
                height
            )

            helper.decodeImage(pixels, width, height,
                {
                    onSuccess(it)
                },
                {
                    onError(OSBARCError.ZXING_LIBRARY_ERROR)
                }
            )
        } catch (e: Exception) {
            e.message?.let { Log.e(LOG_TAG, it) }
            onError(OSBARCError.ZXING_LIBRARY_ERROR)
        } finally {
            imageProxy.close()
        }
    }

}