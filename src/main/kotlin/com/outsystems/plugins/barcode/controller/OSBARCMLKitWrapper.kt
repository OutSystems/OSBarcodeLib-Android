package com.outsystems.plugins.barcode.controller

import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.util.Log
import androidx.camera.core.ImageProxy
import com.outsystems.plugins.barcode.controller.helper.OSBARCMLKitHelperInterface
import com.outsystems.plugins.barcode.model.OSBARCError
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

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
        onSuccess: (String) -> Unit,
        onError: (OSBARCError) -> Unit
    ) {
        try {
            helper.decodeImage(imageProxy, imageProxyToBitmap(imageProxy),
                { barcodes ->
                    var result: String? = null
                    if (barcodes.isNotEmpty()) {
                        result = barcodes.first().rawValue
                    }
                    if (!result.isNullOrEmpty()) {
                        onSuccess(result)
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