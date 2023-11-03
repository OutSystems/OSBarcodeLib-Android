package com.outsystems.plugins.barcode.controller

import android.app.Activity
import android.content.Intent
import com.outsystems.plugins.barcode.model.OSBARCScanParameters
import com.outsystems.plugins.barcode.view.OSBARCScannerActivity

class OSBARCController {

    companion object {
        private const val BACK_CAMERA = 1
        private const val FRONT_CAMERA = 2
        private const val SCAN_REQUEST_CODE = 112
        private const val SCAN_INSTRUCTIONS = "SCAN_INSTRUCTIONS"
        private const val CAMERA_DIRECTION = "CAMERA_DIRECTION"
        private const val SCAN_ORIENTATION = "SCAN_ORIENTATION"
        private const val SCAN_BUTTON = "SCAN_BUTTON"
        private const val SCAN_BUTTON_TEXT = "SCAN_BUTTON_TEXT"
        private const val SCAN_LIBRARY = "SCAN_LIBRARY"
    }

    fun scanCode(activity: Activity, parameters: OSBARCScanParameters) {
        val scanningIntent = Intent(activity, OSBARCScannerActivity::class.java).apply {
            putExtra(SCAN_INSTRUCTIONS, parameters.scanInstructions)
            putExtra(CAMERA_DIRECTION, parameters.cameraDirection)
            putExtra(SCAN_ORIENTATION, parameters.scanOrientation)
            putExtra(SCAN_BUTTON, parameters.scanButton)
            putExtra(SCAN_BUTTON_TEXT, parameters.scanButtonText)
            putExtra(SCAN_LIBRARY, parameters.scanningLibrary)
        }
        activity.startActivityForResult(scanningIntent, SCAN_REQUEST_CODE)
    }

}