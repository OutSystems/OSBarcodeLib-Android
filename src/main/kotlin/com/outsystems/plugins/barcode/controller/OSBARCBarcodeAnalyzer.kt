package com.outsystems.plugins.barcode.controller

import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.outsystems.plugins.barcode.controller.helper.OSBARCImageHelperInterface
import com.outsystems.plugins.barcode.model.OSBARCError
import java.io.ByteArrayOutputStream
import java.lang.Exception
import java.nio.ByteBuffer

/**
 * This class is responsible for implementing the ImageAnalysis.Analyzer interface,
 * and overriding its analyze() method to scan for barcodes in the image frames.
 */
class OSBARCBarcodeAnalyzer(
    private val scanLibrary: OSBARCScanLibraryInterface,
    private val imageHelper: OSBARCImageHelperInterface,
    private val onBarcodeScanned: (String) -> Unit,
    private val onScanningError: (OSBARCError) -> Unit
): ImageAnalysis.Analyzer {

    companion object {
        private const val LOG_TAG = "OSBARCBarcodeAnalyzer"
    }

    /**
     * Overrides the analyze() method from ImageAnalysis.Analyzer,
     * calling the respective implementation of OSBARCScanLibraryInterface
     * to scan for barcodes in the image.
     * @param image - ImageProxy object that represents the image to be analyzed.
     */
    override fun analyze(image: ImageProxy) {
        try {
            scanLibrary.scanBarcode(
                image,
                imageProxyToBitmap(image),
                {
                    onBarcodeScanned(it)
                },
                {
                    onScanningError(it)
                }
            )
        } catch (e: Exception) {
            e.message?.let { Log.e(LOG_TAG, it) }
            onScanningError(OSBARCError.SCANNING_GENERAL_ERROR)
        }
    }

    // Function to convert ImageProxy to Bitmap
    private fun imageProxyToBitmap(image: ImageProxy): Bitmap {

        // get image data
        val planes = image.planes
        val yBuffer: ByteBuffer = planes[0].buffer
        val uBuffer: ByteBuffer = planes[1].buffer
        val vBuffer: ByteBuffer = planes[2].buffer

        // get image width and height
        val imageWidth = image.width
        val imageHeight = image.height

        // calculate image data size
        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        // use byte arrays for image data
        val data = ByteArray(ySize + uSize + vSize)
        yBuffer.get(data, 0, ySize)
        uBuffer.get(data, ySize, uSize)
        vBuffer.get(data, ySize + uSize, vSize)

        // create a YUV image
        // ImageFormat.NV21 used because it's efficient and widely supported
        val yuvImage = YuvImage(data, ImageFormat.NV21, imageWidth, imageHeight, null)

        // convert YUV to Bitmap
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, imageWidth, imageHeight), 100, out)
        val imageBytes = out.toByteArray()
        return imageHelper.bitmapFromImageBytes(imageBytes)
    }

}