package com.outsystems.plugins.barcode.controller.helper

import android.graphics.Bitmap

interface OSBARCImageHelperInterface {
    fun bitmapFromImageBytes(imageBytes: ByteArray): Bitmap
    fun createSubsetBitmapFromSource(
        source: Bitmap,
        rectLeft: Int,
        rectTop: Int,
        rectWidth: Int,
        rectHeight: Int
    ): Bitmap
}