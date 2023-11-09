package com.outsystems.plugins.barcode.controller

import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.outsystems.plugins.barcode.controller.helper.OSBARCMLKitHelperInterface
import com.outsystems.plugins.barcode.model.OSBARCError

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
    @OptIn(ExperimentalGetImage::class) override fun scanBarcode(
        imageProxy: ImageProxy,
        onSuccess: (String) -> Unit,
        onError: (OSBARCError) -> Unit
    ) {
        try {
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                helper.decodeImage(imageProxy, mediaImage,
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
            }
        } catch (e: Exception) {
            e.message?.let { Log.e(LOG_TAG, it) }
            onError(OSBARCError.MLKIT_LIBRARY_ERROR)
        }
    }

}