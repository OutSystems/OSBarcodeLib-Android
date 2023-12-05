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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
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
import androidx.compose.ui.graphics.Path
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
import com.outsystems.plugins.barcode.view.ui.theme.ActionButtonsDistance
import com.outsystems.plugins.barcode.view.ui.theme.BarcodeScannerTheme
import com.outsystems.plugins.barcode.view.ui.theme.ButtonsBackgroundGray
import com.outsystems.plugins.barcode.view.ui.theme.ButtonsBackgroundWhite
import com.outsystems.plugins.barcode.view.ui.theme.ButtonsBorderGray
import com.outsystems.plugins.barcode.view.ui.theme.ButtonsTextGray
import com.outsystems.plugins.barcode.view.ui.theme.ButtonsTextWhite
import com.outsystems.plugins.barcode.view.ui.theme.CustomGray
import com.outsystems.plugins.barcode.view.ui.theme.NoPadding
import com.outsystems.plugins.barcode.view.ui.theme.ScanAimWhite
import com.outsystems.plugins.barcode.view.ui.theme.ScanButtonCornerRadius
import com.outsystems.plugins.barcode.view.ui.theme.ScanButtonStrokeWidth
import com.outsystems.plugins.barcode.view.ui.theme.ScanInstructionsWhite
import com.outsystems.plugins.barcode.view.ui.theme.ScannerAimCornerLength
import com.outsystems.plugins.barcode.view.ui.theme.ScannerAimRectCornerPadding
import com.outsystems.plugins.barcode.view.ui.theme.ScannerAimStrokeWidth
import com.outsystems.plugins.barcode.view.ui.theme.ScannerBackgroundBlack
import com.outsystems.plugins.barcode.view.ui.theme.ScannerBorderPadding

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
    private var isScanning = false

    private data class Point(val x: Float, val y: Float)

    companion object {
        private const val SCAN_SUCCESS_RESULT_CODE = -1
        private const val SCAN_RESULT = "scanResult"
        private const val LOG_TAG = "OSBARCScannerActivity"
        private const val SCAN_PARAMETERS = "SCAN_PARAMETERS"
        private const val CAM_DIRECTION_FRONT = 2
        private const val ORIENTATION_PORTRAIT = 1
        private const val ORIENTATION_LANDSCAPE = 2
    }

    /**
     * Overrides the onCreate method from Activity, setting the UI of the screen
     */
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val parameters = intent.extras?.getSerializable(SCAN_PARAMETERS) as OSBARCScanParameters

        // possibly lock orientation, the screen is adaptive by default
        if (parameters.scanOrientation == ORIENTATION_PORTRAIT) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else if (parameters.scanOrientation == ORIENTATION_LANDSCAPE) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }

        isScanning = !parameters.scanButton
        selector = CameraSelector.Builder()
            .requireLensFacing(if (parameters.cameraDirection == CAM_DIRECTION_FRONT) CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK)
            .build()

        setContent {

            // to know if device is phone or tablet
            // more info: https://developer.android.com/guide/topics/large-screens/support-different-screen-sizes#window_size_classes
            val windowSizeClass = calculateWindowSizeClass(this)

            BarcodeScannerTheme {
                ScanScreen(parameters, windowSizeClass)
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
    fun ScanScreen(parameters: OSBARCScanParameters, windowSizeClass: WindowSizeClass) {
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
                .safeDrawingPadding()
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
            val textToRectPadding = 24.dp

            val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

            if (isPortrait) {
                // determine if device is phone or tablet
                val isPhone = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact
                if (isPhone) {
                    ScanScreenUIPortrait(parameters, screenWidth, borderPadding, true)
                }
                else {
                    ScanScreenUILandscape(parameters, (screenWidth / 2), borderPadding, textToRectPadding, isPhone = false, isPortrait = true)
                }
            }
            else {
                // determine if device is phone or tablet
                val isPhone = windowSizeClass.heightSizeClass == WindowHeightSizeClass.Compact
                if (isPhone) {
                    ScanScreenUILandscape(parameters, screenHeight, borderPadding, textToRectPadding, isPhone = true, isPortrait = false)
                } else {
                    ScanScreenUILandscape(parameters, screenHeight / 2, borderPadding, textToRectPadding, isPhone = false, isPortrait = false)
                }
            }
        }
    }

    /**
     * Composable function, responsible rendering the main centered view with the transparent
     * rectangle
     * @param height the screen height
     * @param horizontalPadding the horizontal padding for the whole view
     * @param verticalPadding the vertical padding for the whole view
     */
    @Composable
    fun ScanScreenAim(
        height: Dp, horizontalPadding: Dp, verticalPadding: Dp,
        isPhone: Boolean,
        isPortrait: Boolean
    ) {


        // the canvas includes the rectangle and its edges
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
            ,
            onDraw = {

                val radius = 25f
                val canvasWidth = size.width
                val canvasHeight = size.height

                // rectangle size is determined by removing the padding from the border of the screen
                // and the padding to the corners of the rectangle
                var rectWidth: Float
                var rectHeight: Float

                if (isPhone) { // for phones
                    rectWidth = canvasWidth - (horizontalPadding.toPx() * 2) - (ScannerAimRectCornerPadding.toPx() * 2)
                    rectHeight = canvasHeight - (verticalPadding.toPx() * 2) - (ScannerAimRectCornerPadding.toPx() * 2)
                } else { // for tablets
                    if (isPortrait) {
                        rectWidth = (canvasWidth) - (horizontalPadding.toPx() * 2) - (ScannerAimRectCornerPadding.toPx() * 2)
                        rectHeight = rectWidth
                    } else {
                        rectWidth = canvasWidth - (horizontalPadding.toPx() * 2) - (ScannerAimRectCornerPadding.toPx() * 2)
                        rectHeight = canvasHeight - (ScannerAimRectCornerPadding.toPx() * 2)
                    }
                }

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
                    drawRect(color = ScannerBackgroundBlack)
                }

                val aimTop = rectTop - ScannerAimRectCornerPadding.toPx()
                val aimLeft = rectLeft - ScannerAimRectCornerPadding.toPx()
                val aimRight = aimLeft + rectWidth + (ScannerAimRectCornerPadding * 2).toPx()
                val aimBottom = aimTop + rectHeight + (ScannerAimRectCornerPadding * 2).toPx()
                val aimLength = ScannerAimCornerLength.toPx()

                val aimPath = Path()
                // top left
                AddCornerToAimPath(
                    aimPath,
                    Point(aimLeft + aimLength, aimTop),
                    Point(aimLeft + radius, aimTop),
                    Point(aimLeft, aimTop),
                    Point(aimLeft, aimTop + radius),
                    Point(aimLeft, aimTop + aimLength)
                )
                // bottom left
                AddCornerToAimPath(
                    aimPath,
                    Point(aimLeft, aimBottom - aimLength),
                    Point(aimLeft, aimBottom - radius),
                    Point(aimLeft, aimBottom),
                    Point(aimLeft + radius, aimBottom),
                    Point(aimLeft + aimLength, aimBottom)
                )
                // bottom right
                AddCornerToAimPath(
                    aimPath,
                    Point(aimRight - aimLength, aimBottom),
                    Point(aimRight - radius, aimBottom),
                    Point(aimRight, aimBottom),
                    Point(aimRight, aimBottom - radius),
                    Point(aimRight, aimBottom - aimLength)
                )
                // top right
                AddCornerToAimPath(
                    aimPath,
                    Point(aimRight, aimTop + aimLength),
                    Point(aimRight, aimTop + radius),
                    Point(aimRight, aimTop),
                    Point(aimRight - radius, aimTop),
                    Point(aimRight - aimLength, aimTop)
                )
                drawPath(aimPath, color = ScanAimWhite, style = Stroke(width = ScannerAimStrokeWidth))
            }
        )
    }

    private fun AddCornerToAimPath(path: Path,
                                   startPoint: Point,
                                   startCornerPoint: Point,
                                   controlPoint: Point,
                                   endCornerPoint: Point,
                                   endPoint: Point) {
        path.moveTo(startPoint.x, startPoint.y)
        path.lineTo(startCornerPoint.x, startCornerPoint.y)
        path.quadraticBezierTo(controlPoint.x, controlPoint.y, endCornerPoint.x, endCornerPoint.y)
        path.lineTo(endPoint.x, endPoint.y)
    }

    /**
     * Composable function, responsible rendering the main UI in portrait mode
     * @param parameters the scan parameters
     * @param screenHeight the screen height
     * @param borderPadding the value for the border padding
     */
    @Composable
    fun ScanScreenUIPortrait(parameters: OSBARCScanParameters,
                             screenHeight:Dp,
                             borderPadding: Dp,
                             isPhone: Boolean) {
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
                        .padding(top = ScannerBorderPadding, end = ScannerBorderPadding)
                        .align(Alignment.TopEnd)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(2f, fill = true),
                verticalArrangement = Arrangement.Center
            ) {

                ScanInstructions(
                    modifier = Modifier
                        .fillMaxWidth(),
                    parameters
                )

                ScanScreenAim(screenHeight, borderPadding, borderPadding, isPhone, true)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ScannerBackgroundBlack)
                    .weight(1f, fill = true),
            ) {
                ScanActionButtons(
                    parameters,
                    NoPadding,
                    scanModifier = Modifier
                        .padding(bottom = ScannerBorderPadding)
                        .align(Alignment.BottomCenter),
                    torchModifier = Modifier
                        .padding(bottom = ScannerBorderPadding, end = ScannerBorderPadding)
                        .align(Alignment.BottomEnd)
                )
            }
        }
    }

    /**
     * Composable function, responsible rendering the main UI in landscape mode.
     * This will also be used to for the UI of tablets in portrait, since the
     * orientation of elements in the screen is the same for both orientations.
     * @param parameters the scan parameters
     * @param screenHeight the screen height
     * @param borderPadding the value for the border padding
     */
    @Composable
    fun ScanScreenUILandscape(parameters: OSBARCScanParameters,
                              screenHeight:Dp,
                              borderPadding: Dp,
                              textToRectPadding: Dp,
                              isPhone: Boolean,
                              isPortrait: Boolean) {
        Row(
            modifier = Modifier
                .fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f, fill = true)
                    .background(ScannerBackgroundBlack)
            )

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(2f, fill = true),
                verticalArrangement = Arrangement.Center
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = true)
                        .background(ScannerBackgroundBlack)
                )

                ScanInstructions(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = borderPadding, bottom = if (isPhone) NoPadding else textToRectPadding),
                    parameters
                )

                ScanScreenAim(screenHeight, NoPadding, borderPadding, isPhone, isPortrait)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = true)
                        .background(ScannerBackgroundBlack)
                )

            }

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f, fill = true)
                    .background(ScannerBackgroundBlack)
            ) {

                CloseButton(
                    modifier = Modifier
                        .padding(top = ScannerBorderPadding, end = ScannerBorderPadding)
                        .align(Alignment.TopEnd)
                )

                Column(
                    modifier = Modifier
                        .padding(end = ScannerBorderPadding)
                        .align(Alignment.CenterEnd),
                    verticalArrangement = Arrangement.Center
                ) {
                    ScanActionButtons(
                        parameters,
                        ScannerBorderPadding,
                        scanModifier = Modifier
                            .align(Alignment.End),
                        torchModifier = Modifier
                            .align(Alignment.End),
                    )
                }

            }

        }
    }

    /**
     * Composable function, responsible rendering the close button
     * @param modifier the custom modifier for the button
     */
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

    /**
     * Composable function, responsible rendering the torch button
     * @param modifier the custom modifier for the button
     */
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

    /**
     * Composable function, responsible rendering the scan button
     * @param modifier the custom modifier for the whole view
     * @param scanButtonText the scan button text
     */
    @Composable
    fun ScanButton(modifier: Modifier, scanButtonText: String) {
        var scanning by remember { mutableStateOf(false) }
        val backgroundColor = if (scanning) ButtonsBackgroundWhite else ButtonsBackgroundGray
        val textColor = if (scanning) ButtonsTextGray else ButtonsTextWhite

        Button(
            onClick = {
                isScanning = !isScanning
                scanning = !scanning
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = backgroundColor
            ),
            shape = RoundedCornerShape(ScanButtonCornerRadius),
            border = BorderStroke(width = ScanButtonStrokeWidth, color = ButtonsBorderGray),
            modifier = modifier
        ) {
            Text(
                text = scanButtonText,
                color = textColor,
                textAlign = TextAlign.Center
            )
        }
    }

    /**
     * Composable function, responsible rendering the scan instructions.
     * This component will only be rendered if scan parameters instructs so.
     * @param modifier the custom modifier for the whole view
     * @param parameters the scan parameters
     */
    @Composable
    fun ScanInstructions(modifier: Modifier, parameters: OSBARCScanParameters) {
        if (!parameters.scanInstructions.isNullOrEmpty()) {
            Box(
                modifier = Modifier
                    .background(ScannerBackgroundBlack)
            ) {
                Text(
                    text = parameters.scanInstructions,
                    modifier = modifier,
                    color = ScanInstructionsWhite,
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    /**
     * Composable function, responsible for building the action buttons
     * on the UI.
     * This component will only be rendered if scan parameters instructs so.
     * @param parameters the scan parameters
     * @param verticalPadding the vertical spacing between buttons
     * @param scanModifier the custom modifier for the scan button
     * @param torchModifier the custom modifier for the torch button
     */
    @Composable
    fun ScanActionButtons(parameters: OSBARCScanParameters,
                          scanModifier: Modifier,
                          torchModifier: Modifier) {
        
        val showTorch = camera.cameraInfo.hasFlashUnit()
        val showScan = parameters.scanButton

        val buttonsVerticalDistance = ActionButtonsDistance
        val buttonSpacing = if(showTorch && showScan)
        { buttonsVerticalDistance.times(0.5f) } else { buttonsVerticalDistance.times(0f) }

        // flashlight button
        if (showTorch) {
            TorchButton(
                torchModifier
                    .padding(bottom = buttonSpacing)
                    .size(buttonsVerticalDistance),
            )
        }

        // scan button to turn on scanning when used
        if (showScan) {
            ScanButton(
                scanModifier
                    .padding(top = buttonSpacing)
                    .height(buttonsVerticalDistance),
                parameters.scanText)
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
        if (isScanning) {
            val resultIntent = Intent()
            resultIntent.putExtra(SCAN_RESULT, result)
            setResult(SCAN_SUCCESS_RESULT_CODE, resultIntent)
            finish()
        }
    }

    private fun processReadError(error: OSBARCError) {
        // we only want to process the scan error if scanning is active
        if (isScanning) {
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