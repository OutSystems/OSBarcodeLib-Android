package com.outsystems.plugins.barcode.view

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.outsystems.plugins.barcode.R
import com.outsystems.plugins.barcode.controller.OSBARCBarcodeAnalyzer
import com.outsystems.plugins.barcode.controller.OSBARCScanLibraryFactory
import com.outsystems.plugins.barcode.controller.helper.OSBARCMLKitHelper
import com.outsystems.plugins.barcode.controller.helper.OSBARCZXingHelper
import com.outsystems.plugins.barcode.model.OSBARCError
import com.outsystems.plugins.barcode.model.OSBARCScanParameters
import com.outsystems.plugins.barcode.view.ui.theme.BarcodeScannerTheme
import com.outsystems.plugins.barcode.view.ui.theme.ButtonsBackgroundGray
import com.outsystems.plugins.barcode.view.ui.theme.ButtonsBorderGray
import com.outsystems.plugins.barcode.view.ui.theme.CustomGray
import com.outsystems.plugins.barcode.view.ui.theme.ScannerBackgroundBlack

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
        private const val ORIENTATION_PORTRAIT = 1
        private const val ORIENTATION_LANDSCAPE = 2
    }

    private data class Point(val x: Int, val y: Int)

    /**
     * Overrides the onCreate method from Activity, setting the UI of the screen
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        val parameters = intent.extras?.getSerializable(SCAN_PARAMETERS) as OSBARCScanParameters

        // possibly lock orientation, the screen is adaptive by default
        if (parameters.scanOrientation == ORIENTATION_PORTRAIT) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else if (parameters.scanOrientation == ORIENTATION_LANDSCAPE) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }

        scanning = !parameters.scanButton
        selector = CameraSelector.Builder()
            .requireLensFacing(if (parameters.cameraDirection == CAM_DIRECTION_FRONT) CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK)
            .build()

        setContent {
            BarcodeScannerTheme {
                ScanScreen(parameters)
            }
        }

        makeViewFullscreen()
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

        Box(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding(),
        ) {
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

            // actual UI on top of the camera stream

            val configuration = LocalConfiguration.current
            val screenHeight = configuration.screenHeightDp.dp
            val screenWidth = configuration.screenWidthDp.dp

            val borderPadding = 32.dp

            val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

            if (isPortrait) {
                ScanScreenUIPortrait(parameters = parameters, screenWidth = screenWidth, borderPadding = borderPadding)
            }
            else {
                ScanScreenUILandscape(parameters = parameters, screenWidth = screenWidth, screenHeight = screenHeight, borderPadding = borderPadding)
            }

        }
    }

    @Composable
    fun ScanScreenUIPortrait(parameters: OSBARCScanParameters, screenWidth: Dp, borderPadding: Dp) {

        // padding from the rectangle to each corner
        val rectToCornerPadding = 16.dp
        // size of the scan and torch buttons
        val actionButtonsHeight = 48.dp
        // drawing edges in each corner using lines
        val cornerLength = 50.dp
        // width of each border
        val strokeWidth = 3f

        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ScannerBackgroundBlack)
                    .weight(1f, fill = true),
            ) {
                CloseButton(
                    modifier = Modifier
                        .padding(top = borderPadding, end = borderPadding)
                        .align(Alignment.TopEnd)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                // text with scan instructions
                if (!parameters.scanInstructions.isNullOrEmpty()) {
                    ScanInstructions(modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .background(ScannerBackgroundBlack)
                        //.padding(top = 32.dp)
                        .fillMaxWidth()
                        .wrapContentHeight()
                        ,scanInstructions = parameters.scanInstructions)
                }

                // the canvas includes the rectangle and its edges
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(screenWidth),
                    onDraw = {

                        val radius = 25f
                        val canvasWidth = size.width
                        val canvasHeight = size.height

                        // rectangle size is determined by removing the padding from the border of the screen
                        // and the padding to the corners of the rectangle
                        val rectWidth = canvasWidth - (borderPadding.toPx() * 2) - (rectToCornerPadding.toPx() * 2)
                        val rectHeight = canvasWidth - (borderPadding.toPx() * 2) - (rectToCornerPadding.toPx() * 2)
                        val rectLeft = (canvasWidth - rectWidth) / 2
                        val rectTop = (canvasHeight - rectHeight) / 2

                        val circlePath = Path().apply {
                            addRoundRect(
                                RoundRect(
                                    rect = Rect(Offset(rectLeft, rectTop), Size(rectWidth, rectHeight)),
                                    cornerRadius = CornerRadius(radius, radius)
                                )
                            )
                        }
                        clipPath(circlePath, clipOp = ClipOp.Difference) {
                            drawRect(color=ScannerBackgroundBlack)
                        }

                        val top = rectTop - rectToCornerPadding.toPx()
                        val left = rectLeft - rectToCornerPadding.toPx()
                        val right = left + rectWidth + (rectToCornerPadding * 2).toPx()
                        val bottom = top + rectHeight + (rectToCornerPadding * 2).toPx()
                        val length = cornerLength.toPx()

                        val aimPath = Path()
                        // top left
                        aimPath.moveTo(left + length, top)
                        aimPath.lineTo(left + radius, top)
                        aimPath.quadraticBezierTo(left, top, left, top + radius)
                        aimPath.lineTo(left, top + length)

                        // bottom left
                        aimPath.moveTo(left, bottom - length)
                        aimPath.lineTo(left, bottom - radius)
                        aimPath.quadraticBezierTo(left, bottom, left + radius, bottom)
                        aimPath.lineTo(left + length, bottom)

                        // bottom right
                        aimPath.moveTo(right - length, bottom)
                        aimPath.lineTo(right - radius, bottom)
                        aimPath.quadraticBezierTo(right, bottom, right, bottom - radius)
                        aimPath.lineTo(right, bottom - length)

                        // top right
                        aimPath.moveTo(right, top + length)
                        aimPath.lineTo(right, top + radius)
                        aimPath.quadraticBezierTo(right, top, right - radius, top)
                        aimPath.lineTo(right - length, top)

                        drawPath(aimPath, color = Color.White, style = Stroke(width = strokeWidth))

                    }
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ScannerBackgroundBlack)
                    .weight(1f, fill = true),
            ) {

                // scan button to turn on scanning when used
                if (parameters.scanButton) {
                    ScanButton(
                        modifier = Modifier
                            .padding(bottom = borderPadding)
                            .align(Alignment.BottomCenter)
                            .height(actionButtonsHeight),
                        scanButtonText = parameters.scanText)
                }
                // flashlight button
                if (camera.cameraInfo.hasFlashUnit()) {
                    TorchButton(
                        modifier = Modifier
                            .padding(bottom = borderPadding, end = borderPadding)
                            .align(Alignment.BottomEnd)
                            .size(actionButtonsHeight)
                    )
                }
            }
        }
    }

    @Composable
    fun ScanScreenUILandscape(parameters: OSBARCScanParameters, screenWidth: Dp, screenHeight:Dp, borderPadding: Dp) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            //horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxHeight()
                    // column width is determined by dividing the screen width by 2
                    // and removing the padding from the border of the screen
                    .width((screenWidth / 2) - (borderPadding * 2)/*screenWidth*/)
                    .padding(top = 32.dp, bottom = 32.dp)
                ,
                verticalArrangement = Arrangement.Center
            ) {

                // text with scan instructions
                if (!parameters.scanInstructions.isNullOrEmpty()) {
                    ScanInstructions(modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .background(ScannerBackgroundBlack)
                        .padding(bottom = 24.dp)
                        .fillMaxWidth()
                        ,scanInstructions = parameters.scanInstructions)
                }

                // the canvas includes the rectangle and its edges
                Canvas(
                    modifier = Modifier
                        // canvas width is determined by dividing the screen width by 2
                        // and removing the padding from the border of the screen
                        .width((screenWidth / 2) - (borderPadding * 2)/*screenWidth*/)
                        // canvas height is determined by removing the padding or the border of the screen
                        .height(screenHeight - (borderPadding * 2)/*screenHeight*/)
                        .align(Alignment.CenterHorizontally),
                    onDraw = {
                        // padding from the rectangle to each corner
                        val rectToCornerPadding = 16.dp

                        val canvasWidth = size.width
                        val canvasHeight = size.height

                        // rectangle width is determined with the canvasWidth and
                        // removing the padding from the rect to its corners of the screen
                        val rectWidth = (canvasWidth) - (rectToCornerPadding.toPx() * 2)
                        //val rectWidth = (canvasWidth / 2) - (borderPadding.toPx() * 2) - (rectToCornerPadding.toPx() * 2)

                        // rectangle height is determined by removing the padding from the borders of the screen
                        val rectHeight = (canvasHeight) - (rectToCornerPadding.toPx() * 2)
                        //val rectHeight = (canvasHeight) - (borderPadding.toPx() * 2) - (rectToCornerPadding.toPx() * 2)

                        val rectLeft = (canvasWidth - rectWidth) / 2
                        val rectTop = (canvasHeight - rectHeight) / 2

                        val circlePath = Path().apply {
                            addRect(
                                Rect(Offset(rectLeft, rectTop), Size(rectWidth, rectHeight))
                            )
                        }
                        clipPath(circlePath, clipOp = ClipOp.Difference) {
                            drawRect(SolidColor(ScannerBackgroundBlack))
                        }

                        // drawing edges in each corner using lines
                        val cornerLength = rectWidth / 12

                        val strokeWidth = 3f // width of each border

                        // top left corner
                        drawLine(
                            color = Color.White,
                            start = Offset(rectLeft - rectToCornerPadding.toPx(), rectTop - rectToCornerPadding.toPx()),
                            end = Offset(rectLeft + rectToCornerPadding.toPx() + cornerLength, rectTop - rectToCornerPadding.toPx()),
                            strokeWidth = strokeWidth
                        )
                        drawLine(
                            color = Color.White,
                            start = Offset(rectLeft - rectToCornerPadding.toPx(), rectTop - rectToCornerPadding.toPx()),
                            end = Offset(rectLeft - rectToCornerPadding.toPx(), rectTop + rectToCornerPadding.toPx() + cornerLength),
                            strokeWidth = strokeWidth
                        )

                        // top right corner
                        drawLine(
                            color = Color.White,
                            start = Offset(rectLeft + rectWidth - rectToCornerPadding.toPx() - cornerLength, rectTop - rectToCornerPadding.toPx()),
                            end = Offset(rectLeft + rectWidth + rectToCornerPadding.toPx(), rectTop - rectToCornerPadding.toPx()),
                            strokeWidth = strokeWidth
                        )
                        drawLine(
                            color = Color.White,
                            start = Offset(rectLeft + rectWidth + rectToCornerPadding.toPx(), rectTop - rectToCornerPadding.toPx()),
                            end = Offset(rectLeft + rectWidth + rectToCornerPadding.toPx(), rectTop + rectToCornerPadding.toPx() + cornerLength),
                            strokeWidth = strokeWidth
                        )

                        // bottom left corner
                        drawLine(
                            color = Color.White,
                            start = Offset(rectLeft - rectToCornerPadding.toPx(), rectTop + rectHeight + rectToCornerPadding.toPx()),
                            end = Offset(rectLeft + rectToCornerPadding.toPx() + cornerLength, rectTop + rectHeight + rectToCornerPadding.toPx()),
                            strokeWidth = strokeWidth
                        )
                        drawLine(
                            color = Color.White,
                            start = Offset(rectLeft - rectToCornerPadding.toPx(), rectTop + rectHeight + rectToCornerPadding.toPx()),
                            end = Offset(rectLeft - rectToCornerPadding.toPx(), rectTop + rectHeight - rectToCornerPadding.toPx() - cornerLength),
                            strokeWidth = strokeWidth
                        )

                        // bottom right corner
                        drawLine(
                            color = Color.White,
                            start = Offset(rectLeft + rectWidth + rectToCornerPadding.toPx(), rectTop + rectHeight + rectToCornerPadding.toPx()),
                            end = Offset(rectLeft + rectWidth - rectToCornerPadding.toPx() - cornerLength, rectTop + rectHeight + rectToCornerPadding.toPx()),
                            strokeWidth = strokeWidth
                        )
                        drawLine(
                            color = Color.White,
                            start = Offset(rectLeft + rectWidth + rectToCornerPadding.toPx(), rectTop + rectHeight + rectToCornerPadding.toPx()),
                            end = Offset(rectLeft + rectWidth + rectToCornerPadding.toPx(), rectTop + rectHeight - rectToCornerPadding.toPx() - cornerLength),
                            strokeWidth = strokeWidth
                        )
                    }
                )

            }

            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .background(ScannerBackgroundBlack)
            ) {
                // close button
                CloseButton(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 32.dp, end = 32.dp)
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.CenterEnd),
                    verticalArrangement = Arrangement.Center
                ) {
                    // flashlight button
                    if (camera.cameraInfo.hasFlashUnit()) {
                        TorchButton(
                            modifier = Modifier
                                .align(Alignment.End)
                                .padding(end = 32.dp, bottom = 24.dp)
                        )
                    }
                    // scan button to turn on scanning when used
                    if (parameters.scanButton) {
                        ScanButton(
                            modifier = Modifier
                                .align(Alignment.End)
                                .padding(end = 32.dp),
                            scanButtonText = parameters.scanText
                        )
                    }

                }

            }

        }
    }

    @Composable
    fun CloseButton(modifier: Modifier) {
        Icon(
            painter = painterResource(id = R.drawable.close),
            contentDescription = null,
            tint = CustomGray,
            modifier = modifier
                .clickable {
                    setResult(OSBARCError.SCAN_CANCELLED_ERROR.code)
                    finish()
                }
        )
    }

    @Composable
    fun TorchButton(modifier: Modifier) {
        var isFlashlightOn by remember { mutableStateOf(false) }
        val onIcon = painterResource(id = R.drawable.flash_on)
        val offIcon = painterResource(id = R.drawable.flash_off)
        val icon = if (isFlashlightOn) onIcon else offIcon

        Image(
            painter = icon,
            contentDescription = null,
            modifier = modifier
                .clickable {
                    try {
                        camera.cameraControl.enableTorch(!isFlashlightOn)
                        isFlashlightOn = !isFlashlightOn
                    } catch (e: Exception) {
                        e.message?.let { Log.e(LOG_TAG, it) }
                    }
                }
        )
    }

    @Composable
    fun ScanInstructions(modifier: Modifier, scanInstructions: String) {
        Text(
            text = scanInstructions,
            modifier = modifier,
            color = Color.White,
            textAlign = TextAlign.Center
        )
    }

    @Composable
    fun ScanButton(modifier: Modifier, scanButtonText: String) {
        Button(
            onClick = {
                scanning = true
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = ButtonsBackgroundGray
            ),
            shape = RoundedCornerShape(4.dp),
            border = BorderStroke(width = 1.dp, color = ButtonsBorderGray),
            modifier = modifier
        ) {
            Text(
                text = scanButtonText,
                color = Color.White,
                textAlign = TextAlign.Center
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
        // we only want to process the scan error if scanning is active
        if (scanning) {
            setResult(error.code)
            finish()
        }
    }

    private fun makeViewFullscreen() {
        // hide the action bar
        actionBar?.hide()
        // set full screen
        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        } else {
            window.insetsController?.apply {
                hide(WindowInsets.Type.statusBars())
                systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
    }

}