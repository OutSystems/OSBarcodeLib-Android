package com.outsystems.plugins.barcode.controller.helper

import android.graphics.Bitmap

/**
 * Interface that provides the signature of the type's methods.
 */
interface OSBARCImageHelperInterface {
    suspend fun bitmapFromImageBytes(imageBytes: ByteArray): Bitmap
    suspend fun createSubsetBitmapFromSource(
        source: Bitmap,
        rectLeft: Int,
        rectTop: Int,
        rectWidth: Int,
        rectHeight: Int
    ): Bitmap
}