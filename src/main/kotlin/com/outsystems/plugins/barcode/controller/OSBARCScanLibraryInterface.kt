package com.outsystems.plugins.barcode.controller

import android.graphics.Bitmap
import androidx.camera.core.ImageProxy
import com.outsystems.plugins.barcode.model.OSBARCError

/**
 * Interface that provides the signature of the scanBarcode method
 */
fun interface OSBARCScanLibraryInterface {
    suspend fun scanBarcode(
        imageProxy: ImageProxy,
        imageBitmap: Bitmap,
        onSuccess: (String) -> Unit,
        onError: (OSBARCError) -> Unit
    )
}