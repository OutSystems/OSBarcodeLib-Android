package com.outsystems.plugins.barcode.mocks

import android.graphics.Bitmap
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.common.Barcode
import com.outsystems.plugins.barcode.controller.helper.OSBARCMLKitHelperInterface
import org.mockito.Mockito

class OSBARCMLKitHelperMock: OSBARCMLKitHelperInterface {

    var success = true
    var scanResult: String? = null
    var exception = false
    var barcodesEmpty = true

    override fun decodeImage(
        imageProxy: ImageProxy,
        imageBitmap: Bitmap,
        onSuccess: (MutableList<Barcode>) -> Unit,
        onError: () -> Unit
    ) {
        val mockBarcode = Mockito.mock(Barcode::class.java)

        if (barcodesEmpty) {
            onSuccess(mutableListOf())
            return
        }

        if (success) {
            Mockito.doReturn(scanResult).`when`(mockBarcode).rawValue
            onSuccess(mutableListOf(mockBarcode))
        }
        else if (!exception) {
            onError()
        }
        else {
            throw Exception()
        }
    }
}