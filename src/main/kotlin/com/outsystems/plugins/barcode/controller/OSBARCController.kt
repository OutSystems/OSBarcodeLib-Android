package com.outsystems.plugins.barcode.controller

import android.app.Activity
import android.content.Intent
import com.outsystems.plugins.barcode.model.OSBARCScanParameters
import com.outsystems.plugins.barcode.view.OSBARCScannerActivity

class OSBARCController {

    companion object {
        private const val BACK_CAMERA = 1
        private const val FRONT_CAMERA = 2
        private const val SCAN_REQUEST_CODE = 222
    }

    fun scanCode(activity: Activity, parameters: OSBARCScanParameters) {
        /*
        val integrator = IntentIntegrator(activity);
        integrator.setOrientationLocked(false);

        if (parameters.cameraDirection == BACK_CAMERA) {
            integrator.setCameraId(0);
        } else if (parameters.cameraDirection == FRONT_CAMERA) {
            integrator.setCameraId(1);
        }

        integrator.captureActivity = OSBARCScannerActivity::class.java
        //integrator.captureActivity = OSBARCScannerActivityXML::class.java
        integrator.addExtra("SCAN_INSTRUCTIONS", parameters.scanInstructions)
        integrator.addExtra("SCAN_ORIENTATION", parameters.scanOrientation)
        //integrator.addExtra("SCAN_LINE", scanLine)
        integrator.addExtra("SCAN_BUTTON", parameters.scanButton)
        integrator.addExtra("SCAN_TEXT", parameters.scanButtonText)
        integrator.initiateScan()

         */

        val scanningIntent = Intent(activity, OSBARCScannerActivity::class.java)
        activity.startActivityForResult(scanningIntent, SCAN_REQUEST_CODE)

    }

}