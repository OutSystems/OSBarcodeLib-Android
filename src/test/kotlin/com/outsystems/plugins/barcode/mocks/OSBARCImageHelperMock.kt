package com.outsystems.plugins.barcode.mocks

import android.graphics.Bitmap
import com.outsystems.plugins.barcode.controller.helper.OSBARCImageHelperInterface
import org.mockito.Mockito

class OSBARCImageHelperMock: OSBARCImageHelperInterface {
    override fun bitmapFromImageBytes(imageBytes: ByteArray): Bitmap {
        return Mockito.mock(Bitmap::class.java)
    }
}