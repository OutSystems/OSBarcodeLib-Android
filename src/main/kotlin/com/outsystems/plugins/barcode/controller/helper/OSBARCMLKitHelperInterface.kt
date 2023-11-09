package com.outsystems.plugins.barcode.controller.helper

import android.media.Image
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.common.Barcode

/**
 * Interface that provides the signature of the type's methods.
 */
fun interface OSBARCMLKitHelperInterface {
    fun decodeImage(
        imageProxy: ImageProxy,
        mediaImage: Image,
        onSuccess: (MutableList<Barcode>) -> Unit,
        onError: () -> Unit
    )

}