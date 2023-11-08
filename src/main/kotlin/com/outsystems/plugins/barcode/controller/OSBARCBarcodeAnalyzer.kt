package com.outsystems.plugins.barcode.controller

import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.lang.Exception

class OSBARCBarcodeAnalyzer(
    private val onBarcodeScanned: (String) -> Unit,
    private val onScanningError: () -> Unit
): ImageAnalysis.Analyzer {

    companion object {
        private const val LOG_TAG = "OSBARCBarcodeAnalyzer"
    }

    override fun analyze(image: ImageProxy) {
        try {
            var imageBitmap = image.toBitmap()

            // rotate the image if it's in portrait mode (rotation = 90 or 270 degrees)
            val rotationDegrees = image.imageInfo.rotationDegrees
            if (rotationDegrees == 90 || rotationDegrees == 270) {
                // create a matrix for rotation
                val matrix = Matrix()
                matrix.postRotate(rotationDegrees.toFloat())

                // actually rotate the image
                imageBitmap = Bitmap.createBitmap(imageBitmap, 0, 0, imageBitmap.width, imageBitmap.height, matrix, true)
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
            onBarcodeScanned(result.text)
        } catch (e: Exception) {
            e.message?.let { Log.e(LOG_TAG, it) }
            onScanningError
        } finally {
            image.close()
        }
    }

}