package com.outsystems.plugins.barcode.view

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.outsystems.plugins.barcode.controller.OSBARCBarcodeAnalyzer
import com.outsystems.plugins.barcode.controller.OSBARCScanLibraryFactory
import com.outsystems.plugins.barcode.controller.helper.OSBARCMLKitHelper
import com.outsystems.plugins.barcode.controller.helper.OSBARCZXingHelper
import com.outsystems.plugins.barcode.model.OSBARCError
import com.outsystems.plugins.barcode.model.OSBARCScanParameters
import com.outsystems.plugins.barcode.view.ui.theme.BarcodeScannerTheme
import com.outsystems.plugins.barcode.view.ui.theme.CustomGray
import com.outsystems.plugins.barcode.R

/**
 * This class is responsible for implementing the UI of the scanning screen using Jetpack Compose.
 * It is also responsible for opening a camera stream using CameraX, and calling the class that
 * implements the ImageAnalysis.Analyzer interface.
 */
class OSBARCScannerActivity : ComponentActivity() {
    private lateinit var camera: Camera
    private lateinit var selector: CameraSelector
    private var permissionRequestCount = 0
    private var showDialog by mutableStateOf(false)
    private var scanning = true

    companion object {
        private const val SCAN_SUCCESS_RESULT_CODE = -1
        private const val SCAN_RESULT = "scanResult"
        private const val LOG_TAG = "OSBARCScannerActivity"
        private const val SCAN_PARAMETERS = "SCAN_PARAMETERS"
        private const val CAM_DIRECTION_FRONT = 2
    }

    /**
     * Overrides the onCreate method from Activity, setting the UI of the screen
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        actionBar?.hide()

        val parameters = intent.extras?.getSerializable(SCAN_PARAMETERS) as OSBARCScanParameters
        scanning = !parameters.scanButton
        selector = CameraSelector.Builder()
            .requireLensFacing(if (parameters.cameraDirection == CAM_DIRECTION_FRONT) CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK)
            .build()

        setContent {
            BarcodeScannerTheme {
                ScanScreen(parameters)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        showDialog = !hasCameraPermission(this.applicationContext)
    }

    /**
     * Composable function, responsible for declaring the UI of the screen,
     * as well as creating an instance of OSBARCBarcodeAnalyzer for image analysis.
     */
    @Composable
    fun ScanScreen(parameters: OSBARCScanParameters) {
        val lifecycleOwner = LocalLifecycleOwner.current
        val context = LocalContext.current
        var permissionGiven by remember { mutableStateOf(true) }

        // permissions
        val requestPermissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                permissionGiven = true
                showDialog = false
            } else {
                permissionGiven = false
                showDialog = true
            }
        }
        SideEffect {
            if (permissionRequestCount == 0) {
                permissionRequestCount++
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }

        CameraPermissionRequiredDialog(
            onDismissRequest = {
                this.setResult(OSBARCError.CAMERA_PERMISSION_DENIED_ERROR.code)
                this.finish()
            },
            onConfirmation = {
                val intent = Intent().apply {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            },
            permissionGiven = permissionGiven,
            shouldShowDialog = showDialog,
            dialogTitle = "Camera Access Not Enabled",
            dialogText = "To continue, please go to the Settings app and enable it.",
            confirmButtonText = "Settings",
            dismissButtonText = "Ok"
        )

        // rest of the UI
        val cameraProviderFuture = remember {
            ProcessCameraProvider.getInstance(context)
        }

        try {
            camera = cameraProviderFuture.get().bindToLifecycle(
                lifecycleOwner,
                selector
            )
        } catch (e: Exception) {
            e.message?.let { Log.e(LOG_TAG, it) }
            setResult(OSBARCError.SCANNING_GENERAL_ERROR.code)
            finish()
        }

        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                factory = { context ->
                    val previewView = PreviewView(context)
                    val preview = Preview.Builder().build()
                    preview.setSurfaceProvider(previewView.surfaceProvider)
                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                    imageAnalysis.setAnalyzer(
                        ContextCompat.getMainExecutor(context),
                        OSBARCBarcodeAnalyzer(
                            OSBARCScanLibraryFactory.createScanLibraryWrapper(
                                parameters.androidScanningLibrary ?: "",
                                OSBARCZXingHelper(),
                                OSBARCMLKitHelper()
                            ),
                            { result ->
                                processReadSuccess(result)
                            },
                            {
                                processReadError(it)
                            }
                        )
                    )
                    try {
                        camera = cameraProviderFuture.get().bindToLifecycle(
                            lifecycleOwner,
                            selector,
                            preview,
                            imageAnalysis
                        )
                    } catch (e: Exception) {
                        e.message?.let { Log.e(LOG_TAG, it) }
                        setResult(OSBARCError.SCANNING_GENERAL_ERROR.code)
                        finish()
                    }
                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            // close button
            Button(
                onClick = {
                    setResult(OSBARCError.SCAN_CANCELLED_ERROR.code)
                    finish()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = null,
                    tint = CustomGray
                )
            }

            // flashlight button
            if (camera.cameraInfo.hasFlashUnit()) {
                TorchButton()
            }

            // text with scan instructions
            if (!parameters.scanInstructions.isNullOrEmpty()) {
                Text(
                    text = parameters.scanInstructions,
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }

            // scan button to turn on scanning when used
            if (parameters.scanButton) {
                Button(
                    onClick = {
                        scanning = true
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.DarkGray
                    ),
                    shape = RectangleShape,
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    Text(
                        text = parameters.scanText,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }

        }
    }

    @Composable
    fun TorchButton() {
        var isFlashlightOn by remember { mutableStateOf(false) }
        val onIcon = painterResource(id = R.drawable.flash_on)
        val offIcon = painterResource(id = R.drawable.flash_off)

        Button(
            onClick = {
                try {
                    camera.cameraControl.enableTorch(!isFlashlightOn)
                    isFlashlightOn = !isFlashlightOn
                } catch (e: Exception) {
                    e.message?.let { Log.e(LOG_TAG, it) }
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isFlashlightOn) Color.White else Color.Black
            ),
            shape = CircleShape
        ) {
            val icon = if (isFlashlightOn) onIcon else offIcon
            Image(
                painter = icon,
                contentDescription = null
            )
        }
    }

    private fun hasCameraPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun processReadSuccess(result: String) {
        // we only want to process the scan result if scanning is active
        if (scanning) {
            val resultIntent = Intent()
            resultIntent.putExtra(SCAN_RESULT, result)
            setResult(SCAN_SUCCESS_RESULT_CODE, resultIntent)
            finish()
        }
    }

    private fun processReadError(error: OSBARCError) {
        // we only want to process the scan result if scanning is active
        if (scanning) {
            setResult(error.code)
            finish()
        }
    }

}