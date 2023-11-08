package com.outsystems.plugins.barcode.controller

import android.app.Activity
import android.content.Intent
import com.outsystems.plugins.barcode.model.OSBARCError
import com.outsystems.plugins.barcode.model.OSBARCScanParameters
import com.outsystems.plugins.barcode.view.OSBARCScannerActivity

class OSBARCController {

    companion object {
        private const val SCAN_REQUEST_CODE = 112
        private const val SCAN_INSTRUCTIONS = "SCAN_INSTRUCTIONS"
        private const val CAMERA_DIRECTION = "CAMERA_DIRECTION"
        private const val SCAN_ORIENTATION = "SCAN_ORIENTATION"
        private const val SCAN_BUTTON = "SCAN_BUTTON"
        private const val SCAN_BUTTON_TEXT = "SCAN_BUTTON_TEXT"
        private const val SCAN_HINT = "SCAN_HINT"
        private const val SCAN_LIBRARY = "SCAN_LIBRARY"
        private const val SCAN_RESULT = "scanResult"
        private const val CAMERA_PERMISSION_DENIED_RESULT_CODE = 1
        private const val SCANNING_EXCEPTION_RESULT_CODE = 2
    }

    fun scanCode(activity: Activity, parameters: OSBARCScanParameters) {
        val scanningIntent = Intent(activity, OSBARCScannerActivity::class.java).apply {
            putExtra(SCAN_INSTRUCTIONS, parameters.scanInstructions)
            putExtra(CAMERA_DIRECTION, parameters.cameraDirection)
            putExtra(SCAN_ORIENTATION, parameters.scanOrientation)
            putExtra(SCAN_BUTTON, parameters.scanButton)
            putExtra(SCAN_BUTTON_TEXT, parameters.scanText)
            putExtra(SCAN_HINT, parameters.hint)
            putExtra(SCAN_LIBRARY, parameters.androidScanningLibrary)
        }
        activity.startActivityForResult(scanningIntent, SCAN_REQUEST_CODE)
    }

    fun handleActivityResult(
        requestCode: Int,
        resultCode: Int,
        intent: Intent?,
        onSuccess: (String) -> Unit,
        onError: (OSBARCError) -> Unit
    ) {
        when (requestCode) {
            SCAN_REQUEST_CODE -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        val result = intent?.extras?.getString(SCAN_RESULT)
                        if (result.isNullOrEmpty()) {
                            onError(OSBARCError.SCANNING_GENERAL_ERROR)
                            return
                        }
                        onSuccess(result)
                    }
                    Activity.RESULT_CANCELED ->
                        onError(OSBARCError.SCAN_CANCELLED_ERROR)
                    CAMERA_PERMISSION_DENIED_RESULT_CODE ->
                        onError(OSBARCError.CAMERA_PERMISSION_DENIED_ERROR)
                    SCANNING_EXCEPTION_RESULT_CODE ->
                        onError(OSBARCError.SCANNING_GENERAL_ERROR)
                }
            }
        }
    }

}