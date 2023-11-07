package com.outsystems.plugins.barcode.controller

import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import androidx.camera.core.ImageProxy
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.outsystems.plugins.barcode.model.OSBARCError

class OSBARCZXingWrapper: OSBARCScanLibraryInterface {

    companion object {
        private const val LOG_TAG = "OSBARCZXingWrapper"
    }

    override fun scanBarcode(
        imageProxy: ImageProxy,
        onSuccess: (String) -> Unit,
        onError: (OSBARCError) -> Unit
    ) {
        try {
            var imageBitmap = imageProxy.toBitmap()

            // rotate the image if it's in portrait mode (rotation = 90 or 270 degrees)
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
            if (rotationDegrees == 90 || rotationDegrees == 270) {
                // create a matrix for rotation
                val matrix = Matrix()
                matrix.postRotate(rotationDegrees.toFloat())

                // actually rotate the image
                imageBitmap = Bitmap.createBitmap(
                    imageBitmap,
                    0,
                    0,
                    imageBitmap.width,
                    imageBitmap.height,
                    matrix,
                    true
                )
            }

            // scan image using zxing
            val width = imageBitmap.width
            val height = imageBitmap.height
            val pixels = IntArray(width * height)
            imageBitmap.getPixels(pixels, 0, width, 0, 0, width, height)

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
            onError(OSBARCError.ZXING_LIBRARY_ERROR)
        } finally {
            imageProxy.close()
        }
    }

}