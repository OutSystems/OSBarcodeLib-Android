package com.outsystems.plugins.barcode.controller

import androidx.camera.core.ImageProxy
import com.outsystems.plugins.barcode.model.OSBARCError

/**
 * Interface that provides the signature of the scanBarcode method
 */
fun interface OSBARCScanLibraryInterface {
    fun scanBarcode(
        imageProxy: ImageProxy,
        onSuccess: (String) -> Unit,
        onError: (OSBARCError) -> Unit
    )
}