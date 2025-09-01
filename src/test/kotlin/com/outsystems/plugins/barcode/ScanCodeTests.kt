package com.outsystems.plugins.barcode

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.media.Image
import android.os.Bundle
import androidx.camera.core.ImageInfo
import androidx.camera.core.ImageProxy
import androidx.core.content.IntentCompat
import com.outsystems.plugins.barcode.controller.OSBARCBarcodeAnalyzer
import com.outsystems.plugins.barcode.controller.OSBARCController
import com.outsystems.plugins.barcode.controller.OSBARCScanLibraryFactory
import com.outsystems.plugins.barcode.controller.helper.OSBARCImageHelperInterface
import com.outsystems.plugins.barcode.mocks.OSBARCImageHelperMock
import com.outsystems.plugins.barcode.mocks.OSBARCMLKitHelperMock
import com.outsystems.plugins.barcode.mocks.OSBARCZXingHelperMock
import com.outsystems.plugins.barcode.mocks.OSBARCScanLibraryMock
import com.outsystems.plugins.barcode.model.OSBARCError
import com.outsystems.plugins.barcode.model.OSBARCScanParameters
import com.outsystems.plugins.barcode.model.OSBARCScanResult
import com.outsystems.plugins.barcode.model.OSBARCScannerHint
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.Mockito
import java.nio.ByteBuffer

class ScanCodeTests {

    private lateinit var mockActivity: Activity
    private lateinit var mockIntent: Intent
    private lateinit var mockBundle: Bundle
    private lateinit var mockImageProxy: ImageProxy
    private lateinit var mockPlaneProxy: ImageProxy.PlaneProxy
    private lateinit var mockByteBuffer: ByteBuffer
    private lateinit var mockImageInfo: ImageInfo
    private lateinit var planes: Array<ImageProxy.PlaneProxy>
    private lateinit var mockIntentCompat: MockedStatic<IntentCompat>

    private lateinit var imageHelperMock: OSBARCImageHelperInterface
    private lateinit var mockBitmap: Bitmap

    companion object {
        private const val SCAN_REQUEST_CODE = 112
        private const val INVALID_REQUEST_CODE = 113
        private const val GENERAL_ERROR_CODE = 4
        private const val SCAN_RESULT_KEY = "scanResult"
        private val SCAN_RESULT = OSBARCScanResult("scanResult", OSBARCScannerHint.ITF)
        private val RESULT_CODE = OSBARCScanResult("myCode", OSBARCScannerHint.QR_CODE)
    }

    @Before
    fun before() {
        mockActivity = Mockito.mock(Activity::class.java)
        mockIntent = Mockito.mock(Intent::class.java)
        mockBundle = Mockito.mock(Bundle::class.java)
        mockImageProxy = Mockito.mock(ImageProxy::class.java)
        mockPlaneProxy = Mockito.mock(ImageProxy.PlaneProxy::class.java)
        mockByteBuffer = Mockito.mock(ByteBuffer::class.java)
        mockImageInfo = Mockito.mock(ImageInfo::class.java)
        planes = arrayOf(mockPlaneProxy, mockPlaneProxy, mockPlaneProxy)
        Mockito.doReturn(planes).`when`(mockImageProxy).planes
        Mockito.doReturn(mockByteBuffer).`when`(mockPlaneProxy).buffer
        Mockito.doReturn(30).`when`(mockImageProxy).width
        Mockito.doReturn(30).`when`(mockImageProxy).height
        Mockito.doReturn(30).`when`(mockByteBuffer).remaining()
        Mockito.doReturn(mockImageInfo).`when`(mockImageProxy).imageInfo

        imageHelperMock = OSBARCImageHelperMock()
        mockBitmap = Mockito.mock(Bitmap::class.java)
        mockIntentCompat = Mockito.mockStatic(IntentCompat::class.java)
    }

    @After
    fun after() {
        mockIntentCompat.close()
    }

    @Test
    fun givenParametersCorrectWhenScanBarcodeThenSuccess() {
        val barcodeController = OSBARCController()
        val parameters = OSBARCScanParameters(
            "Scan the barcode",
            1,
            1,
            false,
            "",
            OSBARCScannerHint.QR_CODE,
            "zxing"
        )
        barcodeController.scanCode(mockActivity, parameters)
    }

    @Test
    fun givenResultOKAndBarcodeValidWhenHandleScanResultThenSuccess() {
        mockIntentCompat
            .`when`<OSBARCScanResult?> {
                IntentCompat.getSerializableExtra(mockIntent, SCAN_RESULT_KEY, OSBARCScanResult::class.java)
            }
            .thenReturn(RESULT_CODE)

        val barcodeController = OSBARCController()
        barcodeController.handleActivityResult(
            SCAN_REQUEST_CODE, Activity.RESULT_OK, mockIntent,
            {
                assertEquals(RESULT_CODE, it)
            },
            {
                fail()
            }
        )
    }

    @Test
    fun givenIntentNullWhenHandleScanResultThenGeneralError() {
        val barcodeController = OSBARCController()
        barcodeController.handleActivityResult(SCAN_REQUEST_CODE, Activity.RESULT_OK, null,
            {
                fail()
            },
            {
                assertEquals(OSBARCError.SCANNING_GENERAL_ERROR.code, it.code)
                assertEquals(OSBARCError.SCANNING_GENERAL_ERROR.description, it.description)
            }
        )
    }

    @Test
    fun givenBundleNullWhenHandleScanResultThenGeneralError() {
        Mockito.doReturn(null).`when`(mockIntent).extras

        val barcodeController = OSBARCController()
        barcodeController.handleActivityResult(SCAN_REQUEST_CODE, Activity.RESULT_OK, mockIntent,
            {
                fail()
            },
            {
                assertEquals(OSBARCError.SCANNING_GENERAL_ERROR.code, it.code)
                assertEquals(OSBARCError.SCANNING_GENERAL_ERROR.description, it.description)
            }
        )
    }

    @Test
    fun givenStringNullWhenHandleScanResultThenGeneralError() {
        mockIntentCompat
            .`when`<OSBARCScanResult?> {
                IntentCompat.getSerializableExtra(mockIntent, SCAN_RESULT_KEY, OSBARCScanResult::class.java)
            }
            .thenReturn(null)

        val barcodeController = OSBARCController()
        barcodeController.handleActivityResult(SCAN_REQUEST_CODE, Activity.RESULT_OK, mockIntent,
            {
                fail()
            },
            {
                assertEquals(OSBARCError.SCANNING_GENERAL_ERROR.code, it.code)
                assertEquals(OSBARCError.SCANNING_GENERAL_ERROR.description, it.description)
            }
        )
    }

    @Test
    fun givenInvalidRequestCodeWhenHandleScanResultThenGeneralError() {
        mockIntentCompat
            .`when`<OSBARCScanResult?> {
                IntentCompat.getSerializableExtra(mockIntent, SCAN_RESULT_KEY, OSBARCScanResult::class.java)
            }
            .thenReturn(null)

        val barcodeController = OSBARCController()
        barcodeController.handleActivityResult(INVALID_REQUEST_CODE, Activity.RESULT_OK, mockIntent,
            {
                fail()
            },
            {
                assertEquals(OSBARCError.SCANNING_GENERAL_ERROR.code, it.code)
                assertEquals(OSBARCError.SCANNING_GENERAL_ERROR.description, it.description)
            }
        )
    }

    @Test
    fun givenInvalidResultCodeWhenHandleScanResultThenGeneralError() {
        mockIntentCompat
            .`when`<OSBARCScanResult?> {
                IntentCompat.getSerializableExtra(mockIntent, SCAN_RESULT_KEY, OSBARCScanResult::class.java)
            }
            .thenReturn(null)

        val barcodeController = OSBARCController()
        barcodeController.handleActivityResult(SCAN_REQUEST_CODE, GENERAL_ERROR_CODE, mockIntent,
            {
                fail()
            },
            {
                assertEquals(OSBARCError.SCANNING_GENERAL_ERROR.code, it.code)
                assertEquals(OSBARCError.SCANNING_GENERAL_ERROR.description, it.description)
            }
        )
    }

    @Test
    fun givenResultOKAndBarcodeEmptyWhenHandleScanResultThenScanningError() {
        mockIntentCompat
            .`when`<OSBARCScanResult?> {
                IntentCompat.getSerializableExtra(mockIntent, SCAN_RESULT_KEY, OSBARCScanResult::class.java)
            }
            .thenReturn(OSBARCScanResult("", OSBARCScannerHint.UNKNOWN))

        val barcodeController = OSBARCController()
        barcodeController.handleActivityResult(SCAN_REQUEST_CODE, Activity.RESULT_OK, mockIntent,
            {
                fail()
            },
            {
                assertEquals(OSBARCError.SCANNING_GENERAL_ERROR.code, it.code)
                assertEquals(OSBARCError.SCANNING_GENERAL_ERROR.description, it.description)
            }
        )
    }

    @Test
    fun givenResultCancelledAndBarcodeEmptyWhenHandleScanResultThenCancelledError() {
        val barcodeController = OSBARCController()
        barcodeController.handleActivityResult(SCAN_REQUEST_CODE, Activity.RESULT_CANCELED, mockIntent,
            {
                fail()
            },
            {
                assertEquals(OSBARCError.SCAN_CANCELLED_ERROR.code, it.code)
                assertEquals(OSBARCError.SCAN_CANCELLED_ERROR.description, it.description)
            }
        )
    }

    @Test
    fun givenScanCancelledAndBarcodeEmptyWhenHandleScanResultThenCancelledError() {
        val barcodeController = OSBARCController()
        barcodeController.handleActivityResult(SCAN_REQUEST_CODE, OSBARCError.SCAN_CANCELLED_ERROR.code, mockIntent,
            {
                fail()
            },
            {
                assertEquals(OSBARCError.SCAN_CANCELLED_ERROR.code, it.code)
                assertEquals(OSBARCError.SCAN_CANCELLED_ERROR.description, it.description)
            }
        )
    }

    @Test
    fun givenCameraPermissionDeniedWhenHandleScanResultThenPermissionDeniedError() {
        val barcodeController = OSBARCController()
        barcodeController.handleActivityResult(SCAN_REQUEST_CODE, OSBARCError.CAMERA_PERMISSION_DENIED_ERROR.code, mockIntent,
            {
                fail()
            },
            {
                assertEquals(OSBARCError.CAMERA_PERMISSION_DENIED_ERROR.code, it.code)
                assertEquals(OSBARCError.CAMERA_PERMISSION_DENIED_ERROR.description, it.description)
            }
        )
    }

    @Test
    fun givenScanningErrorWhenHandleScanResultThenScanningError() {
        val barcodeController = OSBARCController()
        barcodeController.handleActivityResult(SCAN_REQUEST_CODE, OSBARCError.SCANNING_GENERAL_ERROR.code, mockIntent,
            {
                fail()
            },
            {
                assertEquals(OSBARCError.SCANNING_GENERAL_ERROR.code, it.code)
                assertEquals(OSBARCError.SCANNING_GENERAL_ERROR.description, it.description)
            }
        )
    }

    @Test
    fun givenZXingErrorWhenHandleScanResultThenZXingError() {
        val barcodeController = OSBARCController()
        barcodeController.handleActivityResult(SCAN_REQUEST_CODE, OSBARCError.ZXING_LIBRARY_ERROR.code, mockIntent,
            {
                fail()
            },
            {
                assertEquals(OSBARCError.ZXING_LIBRARY_ERROR.code, it.code)
                assertEquals(OSBARCError.ZXING_LIBRARY_ERROR.description, it.description)
            }
        )
    }

    @Test
    fun givenMLKitErrorWhenHandleScanResultThenMLKitError() {
        val barcodeController = OSBARCController()
        barcodeController.handleActivityResult(SCAN_REQUEST_CODE, OSBARCError.MLKIT_LIBRARY_ERROR.code, mockIntent,
            {
                fail()
            },
            {
                assertEquals(OSBARCError.MLKIT_LIBRARY_ERROR.code, it.code)
                assertEquals(OSBARCError.MLKIT_LIBRARY_ERROR.description, it.description)
            }
        )
    }

    @Test
    fun givenScanLibrarySuccessWhenScanBarcodeThenSuccess() {
        val scanLibMock = OSBARCScanLibraryMock().apply {
            success = true
            resultCode = RESULT_CODE
        }
        Mockito.doReturn(mockBitmap).`when`(mockImageProxy).toBitmap()
        OSBARCBarcodeAnalyzer(
            scanLibMock,
            imageHelperMock,
            {
                assertEquals(RESULT_CODE, it)
            },
            {
                fail()
            }
        ).analyze(mockImageProxy)
    }

    @Test
    fun givenScanLibraryGeneralErrorWhenScanBarcodeThenGeneralError() {
        val mockImageProxy = Mockito.mock(ImageProxy::class.java)
        val scanLibMock = OSBARCScanLibraryMock().apply {
            success = false
            exception = false
            error = OSBARCError.SCANNING_GENERAL_ERROR
        }
        OSBARCBarcodeAnalyzer(
            scanLibMock,
            imageHelperMock,
            {
                fail()
            },
            {
                assertEquals(OSBARCError.SCANNING_GENERAL_ERROR.code, it.code)
                assertEquals(OSBARCError.SCANNING_GENERAL_ERROR.description, it.description)
            }
        ).analyze(mockImageProxy)
    }

    @Test
    fun givenScanLibraryZXingErrorWhenScanBarcodeThenZxingError() {
        val scanLibMock = OSBARCScanLibraryMock().apply {
            success = false
            exception = false
            error = OSBARCError.ZXING_LIBRARY_ERROR
        }
        Mockito.doReturn(mockBitmap).`when`(mockImageProxy).toBitmap()
        OSBARCBarcodeAnalyzer(
            scanLibMock,
            imageHelperMock,
            {
                fail()
            },
            {
                assertEquals(OSBARCError.ZXING_LIBRARY_ERROR.code, it.code)
                assertEquals(OSBARCError.ZXING_LIBRARY_ERROR.description, it.description)
            }
        ).analyze(mockImageProxy)
    }

    @Test
    fun givenScanLibraryMLKitErrorWhenScanBarcodeThenMLKitError() {
        val scanLibMock = OSBARCScanLibraryMock().apply {
            success = false
            exception = false
            error = OSBARCError.MLKIT_LIBRARY_ERROR
        }
        Mockito.doReturn(mockBitmap).`when`(mockImageProxy).toBitmap()
        OSBARCBarcodeAnalyzer(
            scanLibMock,
            imageHelperMock,
            {
                fail()
            },
            {
                assertEquals(OSBARCError.MLKIT_LIBRARY_ERROR.code, it.code)
                assertEquals(OSBARCError.MLKIT_LIBRARY_ERROR.description, it.description)
            }
        ).analyze(mockImageProxy)
    }

    @Test
    fun givenScanLibraryExceptionWhenScanBarcodeThenGeneralError() {
        val mockImageProxy = Mockito.mock(ImageProxy::class.java)
        val scanLibMock = OSBARCScanLibraryMock().apply {
            success = false
            exception = true
        }
        OSBARCBarcodeAnalyzer(
            scanLibMock,
            imageHelperMock,
            {
                fail()
            },
            {
                assertEquals(OSBARCError.SCANNING_GENERAL_ERROR.code, it.code)
                assertEquals(OSBARCError.SCANNING_GENERAL_ERROR.description, it.description)
            }
        ).analyze(mockImageProxy)
    }

    @Test
    fun givenScanPhoneInPortraitWhenScanBarcodeThenSuccess() {
        val scanLibMock = OSBARCScanLibraryMock().apply {
            success = true
            resultCode = RESULT_CODE
        }
        Mockito.doReturn(mockBitmap).`when`(mockImageProxy).toBitmap()
        OSBARCBarcodeAnalyzer(
            scanLibMock,
            imageHelperMock,
            {
                assertEquals(RESULT_CODE, it)
            },
            {
                fail()
            }
        ).apply {
            isPortrait = true
        }.analyze(mockImageProxy)
    }

    @Test
    fun givenScanPhoneInLandscapeWhenScanBarcodeThenSuccess() {
        val scanLibMock = OSBARCScanLibraryMock().apply {
            success = true
            resultCode = RESULT_CODE
        }
        Mockito.doReturn(mockBitmap).`when`(mockImageProxy).toBitmap()
        OSBARCBarcodeAnalyzer(
            scanLibMock,
            imageHelperMock,
            {
                assertEquals(RESULT_CODE, it)
            },
            {
                fail()
            }
        ).apply {
            isPortrait = false
        }.analyze(mockImageProxy)
    }

    @Test
    fun givenImage90DegreesWhenZXingScanThenSuccess() {
        val wrapper = OSBARCScanLibraryFactory.createScanLibraryWrapper(
            "zxing",
            OSBARCZXingHelperMock().apply { scanResult = SCAN_RESULT },
            OSBARCMLKitHelperMock()
            )

        Mockito.doReturn(90).`when`(mockImageInfo).rotationDegrees // do the same for 270 and 0 to cover all cases

        wrapper.scanBarcode(
            mockImageProxy,
            mockBitmap,
            {
                assertEquals(SCAN_RESULT, it)
            },
            {
                fail()
            }
        )
    }

    @Test
    fun givenImage270DegreesWhenZXingScanThenSuccess() {
        val wrapper = OSBARCScanLibraryFactory.createScanLibraryWrapper(
            "zxing",
            OSBARCZXingHelperMock().apply { scanResult = SCAN_RESULT },
            OSBARCMLKitHelperMock()
        )

        Mockito.doReturn(270).`when`(mockImageInfo).rotationDegrees // do the same for 270 and 0 to cover all cases

        wrapper.scanBarcode(
            mockImageProxy,
            mockBitmap,
            {
                assertEquals(SCAN_RESULT, it)
            },
            {
                fail()
            }
        )
    }

    @Test
    fun givenImage0DegreesWhenZXingScanThenSuccess() {
        val wrapper = OSBARCScanLibraryFactory.createScanLibraryWrapper(
            "zxing",
            OSBARCZXingHelperMock().apply { scanResult = SCAN_RESULT },
            OSBARCMLKitHelperMock()
        )

        Mockito.doReturn(0).`when`(mockImageInfo).rotationDegrees // do the same for 270 and 0 to cover all cases

        wrapper.scanBarcode(
            mockImageProxy,
            mockBitmap,
            {
                assertEquals(SCAN_RESULT, it)
            },
            {
                fail()
            }
        )
    }

    @Test
    fun givenErrorWhenZXingScanThenZXingError() {
        val wrapper = OSBARCScanLibraryFactory.createScanLibraryWrapper(
            "zxing",
            OSBARCZXingHelperMock().apply {
                success = false
                exception = false
            },
            OSBARCMLKitHelperMock()
        )

        Mockito.doReturn(0).`when`(mockImageInfo).rotationDegrees // do the same for 270 and 0 to cover all cases

        wrapper.scanBarcode(
            mockImageProxy,
            mockBitmap,
            {
                fail()
            },
            {
                assertEquals(OSBARCError.ZXING_LIBRARY_ERROR.code, it.code)
                assertEquals(OSBARCError.ZXING_LIBRARY_ERROR.description, it.description)
            }
        )
    }

    @Test
    fun givenExceptionWhenZXingScanThenZXingError() {
        val wrapper = OSBARCScanLibraryFactory.createScanLibraryWrapper(
            "zxing",
            OSBARCZXingHelperMock().apply {
                success = false
                exception = true
            },
            OSBARCMLKitHelperMock()
        )

        Mockito.doReturn(0).`when`(mockImageInfo).rotationDegrees // do the same for 270 and 0 to cover all cases

        wrapper.scanBarcode(
            mockImageProxy,
            mockBitmap,
            {
                fail()
            },
            {
                assertEquals(OSBARCError.ZXING_LIBRARY_ERROR.code, it.code)
                assertEquals(OSBARCError.ZXING_LIBRARY_ERROR.description, it.description)
            }
        )
    }

    @Test
    fun givenSuccessWhenMLKitScanThenSuccess() {
        val wrapper = OSBARCScanLibraryFactory.createScanLibraryWrapper(
            "mlkit",
            OSBARCZXingHelperMock(),
            OSBARCMLKitHelperMock().apply {
                scanResult = SCAN_RESULT
                success = true
                barcodesEmpty = false
            }
        )

        val mockMediaImage = Mockito.mock(Image::class.java)
        Mockito.doReturn(mockMediaImage).`when`(mockImageProxy).image

        wrapper.scanBarcode(
            mockImageProxy,
            mockBitmap,
            {
                assertEquals(SCAN_RESULT, it)
            },
            {
                fail()
            }
        )
    }

    @Test
    fun givenResultEmptyWhenMLKitScanThenDoNothing() {
        val wrapper = OSBARCScanLibraryFactory.createScanLibraryWrapper(
            "mlkit",
            OSBARCZXingHelperMock(),
            OSBARCMLKitHelperMock().apply {
                scanResult = SCAN_RESULT.copy(text = "")
                success = true
                barcodesEmpty = false
            }
        )

        val mockMediaImage = Mockito.mock(Image::class.java)
        Mockito.doReturn(mockMediaImage).`when`(mockImageProxy).image

        wrapper.scanBarcode(
            mockImageProxy,
            mockBitmap,
            {
                // do nothing
            },
            {
                // do nothing
            }
        )
    }

    @Test
    fun givenResultNullWhenMLKitScanThenDoNothing() {
        val wrapper = OSBARCScanLibraryFactory.createScanLibraryWrapper(
            "mlkit",
            OSBARCZXingHelperMock(),
            OSBARCMLKitHelperMock().apply {
                scanResult = null
                success = true
                barcodesEmpty = false
            }
        )

        val mockMediaImage = Mockito.mock(Image::class.java)
        Mockito.doReturn(mockMediaImage).`when`(mockImageProxy).image

        wrapper.scanBarcode(
            mockImageProxy,
            mockBitmap,
            {
                // do nothing
            },
            {
                // do nothing
            }
        )
    }

    @Test
    fun givenBarcodesEmptyWhenMLKitScanThenDoNothing() {
        val wrapper = OSBARCScanLibraryFactory.createScanLibraryWrapper(
            "mlkit",
            OSBARCZXingHelperMock(),
            OSBARCMLKitHelperMock().apply {
                barcodesEmpty = true
            }
        )

        val mockMediaImage = Mockito.mock(Image::class.java)
        Mockito.doReturn(mockMediaImage).`when`(mockImageProxy).image

        wrapper.scanBarcode(
            mockImageProxy,
            mockBitmap,
            {
                // do nothing
            },
            {
                // do nothing
            }
        )
    }

    @Test
    fun givenErrorWhenMLKitScanThenMLKitError() {
        val wrapper = OSBARCScanLibraryFactory.createScanLibraryWrapper(
            "mlkit",
            OSBARCZXingHelperMock(),
            OSBARCMLKitHelperMock().apply {
                success = false
                barcodesEmpty = false
            }
        )

        val mockMediaImage = Mockito.mock(Image::class.java)
        Mockito.doReturn(mockMediaImage).`when`(mockImageProxy).image

        wrapper.scanBarcode(
            mockImageProxy,
            mockBitmap,
            {
                fail()
            },
            {
                assertEquals(OSBARCError.MLKIT_LIBRARY_ERROR.code, it.code)
                assertEquals(OSBARCError.MLKIT_LIBRARY_ERROR.description, it.description)
            }
        )
    }

    @Test
    fun givenExceptionWhenMLKitScanThenMLKitError() {
        val wrapper = OSBARCScanLibraryFactory.createScanLibraryWrapper(
            "mlkit",
            OSBARCZXingHelperMock(),
            OSBARCMLKitHelperMock().apply {
                success = false
                exception = true
                barcodesEmpty = false
            }
        )

        val mockMediaImage = Mockito.mock(Image::class.java)
        Mockito.doReturn(mockMediaImage).`when`(mockImageProxy).image

        wrapper.scanBarcode(
            mockImageProxy,
            mockBitmap,
            {
                fail()
            },
            {
                assertEquals(OSBARCError.MLKIT_LIBRARY_ERROR.code, it.code)
                assertEquals(OSBARCError.MLKIT_LIBRARY_ERROR.description, it.description)
            }
        )
    }

    @Test
    fun givenWrongLibraryWhenScanThenZXingIsUsed() {
        val wrapper = OSBARCScanLibraryFactory.createScanLibraryWrapper(
            "wrongLibrary",
            OSBARCZXingHelperMock().apply {
                success = false
                exception = false
            },
            OSBARCMLKitHelperMock().apply {
                barcodesEmpty = false
            }
        )

        val mockMediaImage = Mockito.mock(Image::class.java)
        Mockito.doReturn(mockMediaImage).`when`(mockImageProxy).image

        wrapper.scanBarcode(
            mockImageProxy,
            mockBitmap,
            {
                fail()
            },
            {
                assertEquals(OSBARCError.ZXING_LIBRARY_ERROR.code, it.code)
                assertEquals(OSBARCError.ZXING_LIBRARY_ERROR.description, it.description)
            }
        )
    }

    @Test
    fun givenMediaImageNullWhenMLKitScanThenDoNothing() {
        val wrapper = OSBARCScanLibraryFactory.createScanLibraryWrapper(
            "mlkit",
            OSBARCZXingHelperMock(),
            OSBARCMLKitHelperMock().apply {
                barcodesEmpty = false
            }
        )

        Mockito.doReturn(null).`when`(mockImageProxy).image

        wrapper.scanBarcode(
            mockImageProxy,
            mockBitmap,
            {
                // do nothing
            },
            {
                // do nothing
            }
        )
    }

}