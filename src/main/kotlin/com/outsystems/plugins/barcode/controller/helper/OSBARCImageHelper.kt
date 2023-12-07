package com.outsystems.plugins.barcode.controller.helper

import android.graphics.Bitmap
import android.graphics.BitmapFactory

/**
 * Helper class that implements the OSBARCImageHelperInterface
 * and provides a method to convert a ByteArray to a Bitmap.
 */
class OSBARCImageHelper: OSBARCImageHelperInterface {

    /**
     * Converts a ByteArray into a Bitmap using BitmapFactory
     * @param imageBytes - ByteArray to convert
     * @return the resulting bitmap.
     */
    override fun bitmapFromImageBytes(imageBytes: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(
            imageBytes,
            0, // use 0 in the offset to decode from the beginning of imageBytes
            imageBytes.size // use byte array size as length because we want to decode the whole image
        )
    }

    /**
     * Creates a bitmap that is a subset of the source bitmap,
     * using the specified coordinates and size.
     * @param source - Source bitmap to use.
     * @param rectLeft - X coordinate where the bitmap starts.
     * @param rectTop - Y coordinate where the bitmap starts.
     * @param rectWidth - Width of the bitmap.
     * @param rectHeight - Height of the bitmap.
     * @return the resulting bitmap.
     */
    override fun createSubsetBitmapFromSource(
        source: Bitmap,
        rectLeft: Int,
        rectTop: Int,
        rectWidth: Int,
        rectHeight: Int
    ): Bitmap {
        return Bitmap.createBitmap(
            source,
            rectLeft,
            rectTop,
            rectWidth,
            rectHeight
        )
    }
}