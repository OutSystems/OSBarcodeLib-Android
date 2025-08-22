package com.outsystems.plugins.barcode.mocks

import android.graphics.Bitmap
import androidx.camera.core.ImageProxy
import com.outsystems.plugins.barcode.controller.OSBARCScanLibraryInterface
import com.outsystems.plugins.barcode.model.OSBARCError
import com.outsystems.plugins.barcode.model.OSBARCScanResult
import com.outsystems.plugins.barcode.model.OSBARCScannerHint

class OSBARCScanLibraryMock: OSBARCScanLibraryInterface {

    var resultCode = OSBARCScanResult("", OSBARCScannerHint.UNKNOWN)
    var success = true
    var exception = false
    var error: OSBARCError = OSBARCError.SCANNING_GENERAL_ERROR
    override fun scanBarcode(
        imageProxy: ImageProxy,
        imageBitmap: Bitmap,
        onSuccess: (OSBARCScanResult) -> Unit,
        onError: (OSBARCError) -> Unit
    ) {
        if (success) {
            onSuccess(OSBARCScanResult("myCode", OSBARCScannerHint.QR_CODE))
        }
        else if (!exception) {
            onError(error)
        }
        else {
            throw Exception()
        }

    }

}