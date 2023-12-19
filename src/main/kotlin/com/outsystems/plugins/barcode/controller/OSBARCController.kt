package com.outsystems.plugins.barcode.controller

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.outsystems.plugins.barcode.model.OSBARCError
import com.outsystems.plugins.barcode.model.OSBARCScanParameters
import com.outsystems.plugins.barcode.view.OSBARCScannerActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * This class is responsible for implementing the Controller
 * of the library, following the MVC pattern
 */
class OSBARCController {

    companion object {
        private const val SCAN_REQUEST_CODE = 112
        private const val SCAN_PARAMETERS = "SCAN_PARAMETERS"
        private const val SCAN_RESULT = "scanResult"
        private const val LOG_TAG = "OSBARCController"
    }

    /**
     * Scans barcodes, opening the device's camera and using scanning libraries.
     * To do that, it launches the OSBARCScannerActivity activity.
     * @param activity - used to open the scanner activity, its onActivityResult() will be called after scanning the barcode.
     * @param parameters - object that contains all the barcode parameters to be used when scanning.
     */
    fun scanCode(activity: Activity, parameters: OSBARCScanParameters) {
        CoroutineScope(Dispatchers.Default).launch {
            activity.startActivityForResult(
                Intent(
                    activity, OSBARCScannerActivity::class.java
                ).putExtra(SCAN_PARAMETERS, parameters), SCAN_REQUEST_CODE
            )
        }
    }

    /**
     * Handles the result of calling the scanCode feature.
     * @param requestCode - the code identifying the request.
     * @param resultCode - the code identifying the result of the request.
     * @param intent - the resulting intent from the operation.
     * @param onSuccess - The code to be executed if the operation was successful.
     * @param onError - The code to be executed if the operation was not successful.
     */
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
                    OSBARCError.SCAN_CANCELLED_ERROR.code ->
                        onError(OSBARCError.SCAN_CANCELLED_ERROR)
                    OSBARCError.CAMERA_PERMISSION_DENIED_ERROR.code ->
                        onError(OSBARCError.CAMERA_PERMISSION_DENIED_ERROR)
                    OSBARCError.SCANNING_GENERAL_ERROR.code ->
                        onError(OSBARCError.SCANNING_GENERAL_ERROR)
                    OSBARCError.ZXING_LIBRARY_ERROR.code ->
                        onError(OSBARCError.ZXING_LIBRARY_ERROR)
                    OSBARCError.MLKIT_LIBRARY_ERROR.code ->
                        onError(OSBARCError.MLKIT_LIBRARY_ERROR)
                    else -> {
                        Log.d(LOG_TAG, "Invalid result code")
                        onError(OSBARCError.SCANNING_GENERAL_ERROR)
                    }
                }
            }
            else -> {
                Log.d(LOG_TAG, "Invalid request code")
                onError(OSBARCError.SCANNING_GENERAL_ERROR)
            }
        }
    }

}