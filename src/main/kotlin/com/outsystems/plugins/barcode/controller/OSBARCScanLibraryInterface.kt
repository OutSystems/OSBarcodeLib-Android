package com.outsystems.plugins.barcode.controller

import androidx.camera.core.ImageProxy
import com.outsystems.plugins.barcode.model.OSBARCError

interface OSBARCScanLibraryInterface {
    fun scanBarcode(
        imageProxy: ImageProxy,
        onSuccess: (String) -> Unit,
        onError: (OSBARCError) -> Unit
    )
}