package com.outsystems.plugins.barcode.mocks

import android.graphics.Bitmap
import com.outsystems.plugins.barcode.controller.helper.OSBARCZXingHelperInterface
import com.outsystems.plugins.barcode.model.OSBARCScanResult
import com.outsystems.plugins.barcode.model.OSBARCScannerHint
import org.mockito.Mockito

class OSBARCZXingHelperMock: OSBARCZXingHelperInterface {

    var scanResult = OSBARCScanResult("", OSBARCScannerHint.UNKNOWN)
    var success = true
    var exception = false

    override fun rotateBitmap(bitmap: Bitmap, rotationDegrees: Int): Bitmap {
        return Mockito.mock(Bitmap::class.java)
    }

    override fun decodeImage(
        pixels: IntArray,
        width: Int,
        height: Int,
        onSuccess: (OSBARCScanResult) -> Unit,
        onError: () -> Unit
    ) {
        if (success) {
            onSuccess(scanResult)
        }
        else if (!exception) {
            onError()
        }
        else {
            throw Exception()
        }
    }
}