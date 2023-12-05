package com.outsystems.plugins.barcode.controller.helper

import android.graphics.Bitmap

fun interface OSBARCImageHelperInterface {
    fun bitmapFromImageBytes(imageBytes: ByteArray): Bitmap
}