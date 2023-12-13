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
import com.outsystems.plugins.barcode.view.ui.theme.SizeRatioHeight
import com.outsystems.plugins.barcode.view.ui.theme.SizeRatioWidth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

    var isPortrait = true

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
        CoroutineScope(Dispatchers.Default).launch {
            try {
                scanLibrary.scanBarcode(
                    image,
                    cropBitmap(imageProxyToBitmap(image)),
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
    }

    /**
     * Converts an ImageProxy object to a Bitmap.
     * Once we can compile this library with Kotlin 1.9.10, and consequently
     * can use version 1.5.3 of the Compose Compiler, this method is unnecessary,
     * since we will be able to use version 1.3.0 of the CameraX library
     * and obtain the bitmap directly from the ImageProxy, using ImageProxy.toBitmap.
     * More info:
     * - https://developer.android.com/jetpack/androidx/releases/compose-kotlin
     * - https://developer.android.com/jetpack/androidx/releases/camera#1.3.0
     * @param image - ImageProxy object that represents the image to be analyzed.
     */
    private suspend fun imageProxyToBitmap(image: ImageProxy): Bitmap {

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


    /**
     * Creates a cropped bitmap for the region of interest to scan,
     * where the cropped image is approximately the same size as the frame
     * shown in the UI, with some padding.
     * As such, it will be a bit bigger than the rectangle in the UI
     * It uses different ratios depending on the orientation of the device - portrait or landscape.
     * @param bitmap - Bitmap object to crop.
     */
    private suspend fun cropBitmap(bitmap: Bitmap): Bitmap {
        val rectWidth: Int
        val rectHeight: Int

        if (isPortrait) {
            // for portrait, the image is rotated
            rectWidth = (bitmap.height * SizeRatioWidth).toInt()
            rectHeight = rectWidth
        } else {
            rectWidth = (bitmap.width * SizeRatioWidth).toInt()
            rectHeight = (bitmap.height * SizeRatioHeight).toInt()
        }

        val rectLeft = (bitmap.width - rectWidth) / 2
        val rectTop = (bitmap.height - rectHeight) / 2

        return imageHelper.createSubsetBitmapFromSource(
            bitmap,
            rectLeft,
            rectTop,
            rectWidth,
            rectHeight
        )
    }

}