package com.outsystems.plugins.barcode.controller

import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.util.Log
import androidx.camera.core.ImageProxy
import com.outsystems.plugins.barcode.controller.helper.OSBARCZXingHelperInterface
import com.outsystems.plugins.barcode.model.OSBARCError
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

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
        onSuccess: (String) -> Unit,
        onError: (OSBARCError) -> Unit
    ) {
        try {
            var imageBitmap = imageProxyToBitmap(imageProxy)

            // rotate the image if it's in portrait mode (rotation = 90 or 270 degrees)
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
            if (rotationDegrees == 90 || rotationDegrees == 270) {
                imageBitmap = helper.rotateBitmap(imageBitmap, rotationDegrees)
            }

            // scan image using zxing
            val width = imageBitmap.width
            val height = imageBitmap.height
            val pixels = IntArray(width * height)
            imageBitmap.getPixels(
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
        return helper.bitmapFromImageBytes(imageBytes)
    }

}