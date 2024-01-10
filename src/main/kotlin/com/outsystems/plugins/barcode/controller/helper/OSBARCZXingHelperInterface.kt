package com.outsystems.plugins.barcode.controller.helper

import android.graphics.Bitmap

/**
 * Interface that provides the signature of the type's methods.
 */
interface OSBARCZXingHelperInterface {
    fun rotateBitmap(bitmap: Bitmap, rotationDegrees: Int): Bitmap
    fun decodeImage(
        pixels: IntArray,
        width: Int,
        height: Int,
        onSuccess: (String) -> Unit,
        onError: () -> Unit
    )
}