package com.outsystems.plugins.barcode.view

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.camera.core.ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
import androidx.compose.ui.Modifier
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.outsystems.plugins.barcode.controller.OSBARCBarcodeAnalyzer
import com.outsystems.plugins.barcode.view.ui.theme.BarcodeScannerTheme
import java.lang.Exception

class OSBARCScannerActivity : ComponentActivity() {

    companion object {
        private const val SCAN_SUCCESS_RESULT_CODE = -1
        private const val CAMERA_PERMISSION_DENIED_RESULT_CODE = 1
        private const val SCANNING_EXCEPTION_RESULT_CODE = 2
        private const val SCAN_RESULT = "scanResult"
        private const val LOG_TAG = "OSBARCScannerActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BarcodeScannerTheme {
                ScanScreen()
            }
        }
    }

    @Composable
    fun ScanScreen() {
        val lifecycleOwner = LocalLifecycleOwner.current
        val context = LocalContext.current

        // permissions
        val requestPermissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // do nothing, continue
            } else {
                this.setResult(CAMERA_PERMISSION_DENIED_RESULT_CODE)
                this.finish()
            }
        }
        SideEffect {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        // rest of the UI
        val cameraProviderFuture = remember {
            ProcessCameraProvider.getInstance(context)
        }
        var barcode by remember {
            mutableStateOf("")
        }

        Column (
            modifier = Modifier.fillMaxSize()
        ) {
            AndroidView(
                factory = { context ->
                    val previewView = PreviewView(context)
                    val preview = Preview.Builder().build()
                    val selector = CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK) // temporary
                        .build()
                    preview.setSurfaceProvider(previewView.surfaceProvider)
                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                    imageAnalysis.setAnalyzer(
                        ContextCompat.getMainExecutor(context),
                        OSBARCBarcodeAnalyzer(
                            { result ->
                                barcode = result
                                val resultIntent = Intent()
                                resultIntent.putExtra(SCAN_RESULT, result)
                                setResult(SCAN_SUCCESS_RESULT_CODE, resultIntent)
                                finish()
                            },
                            {
                                setResult(SCANNING_EXCEPTION_RESULT_CODE)
                                finish()
                            }
                        )
                    )
                    try {
                        cameraProviderFuture.get().bindToLifecycle(
                            lifecycleOwner,
                            selector,
                            preview,
                            imageAnalysis
                        )
                    } catch (e: Exception) {
                        e.message?.let { Log.e(LOG_TAG, it) }
                        setResult(SCANNING_EXCEPTION_RESULT_CODE)
                        finish()
                    }
                    previewView
                },
                modifier = Modifier.weight(1f)
            )
        }
    }

}