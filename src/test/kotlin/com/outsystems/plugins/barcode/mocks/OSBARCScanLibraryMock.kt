package com.outsystems.plugins.barcode.mocks

import android.graphics.Bitmap
import androidx.camera.core.ImageProxy
import com.outsystems.plugins.barcode.controller.OSBARCScanLibraryInterface
import com.outsystems.plugins.barcode.model.OSBARCError

class OSBARCScanLibraryMock: OSBARCScanLibraryInterface {

    var resultCode = ""
    var success = true
    var exception = false
    var error: OSBARCError = OSBARCError.SCANNING_GENERAL_ERROR
    override fun scanBarcode(
        imageProxy: ImageProxy,
        imageBitmap: Bitmap,
        onSuccess: (String) -> Unit,
        onError: (OSBARCError) -> Unit
    ) {
        if (success) {
            onSuccess("myCode")
        }
        else if (!exception) {
            onError(error)
        }
        else {
            throw Exception()
        }

    }

}