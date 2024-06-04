package com.outsystems.plugins.barcode.controller

import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.outsystems.plugins.barcode.controller.helper.OSBARCImageHelperInterface
import com.outsystems.plugins.barcode.model.OSBARCError
import com.outsystems.plugins.barcode.view.ui.theme.SizeRatioHeight
import com.outsystems.plugins.barcode.view.ui.theme.SizeRatioWidth
import java.lang.Exception

/**
 * This class is responsible for implementing the ImageAnalysis.Analyzer interface,
 * and overriding its analyze() method to scan for barcodes in the image frames.
 */
class OSBARCBarcodeAnalyzer(
    private val scanLibrary: OSBARCScanLibraryInterface,
    private val imageHelper: OSBARCImageHelperInterface,
    private val onBarcodeScanned: (String) -> Unit,
    private val onScanningError: (OSBARCError) -> Unit,
    private val delayMillis: Long? = 500L
): ImageAnalysis.Analyzer {

    var isPortrait = true
    private var lastAnalyzedTimestamp = 0L

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
        val currentTimestamp = System.currentTimeMillis()

        // Use the delayMillis value, defaulting to 500 milliseconds if it is null
        val effectiveDelayMillis = delayMillis ?: 500L

        // Only analyze the image if the desired delay has passed
        if (currentTimestamp - lastAnalyzedTimestamp >= effectiveDelayMillis) {
            try {
                scanLibrary.scanBarcode(
                    image,
                    cropBitmap(image.toBitmap()),
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
            // Update the timestamp of the last analyzed frame
            lastAnalyzedTimestamp = currentTimestamp
        }
        image.close()
    }

    /**
     * Creates a cropped bitmap for the region of interest to scan,
     * where the cropped image is approximately the same size as the frame
     * shown in the UI, with some padding.
     * As such, it will be a bit bigger than the rectangle in the UI
     * It uses different ratios depending on the orientation of the device - portrait or landscape.
     * @param bitmap - Bitmap object to crop.
     */
    private fun cropBitmap(bitmap: Bitmap): Bitmap {
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