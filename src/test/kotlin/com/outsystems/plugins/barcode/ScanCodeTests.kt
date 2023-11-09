package com.outsystems.plugins.barcode

import android.app.Activity
import android.content.Intent
import android.media.Image
import android.os.Bundle
import androidx.camera.core.ImageInfo
import androidx.camera.core.ImageProxy
import com.outsystems.plugins.barcode.controller.OSBARCBarcodeAnalyzer
import com.outsystems.plugins.barcode.controller.OSBARCController
import com.outsystems.plugins.barcode.controller.OSBARCScanLibraryFactory
import com.outsystems.plugins.barcode.mocks.OSBARCMLKitHelperMock
import com.outsystems.plugins.barcode.mocks.OSBARCZXingHelperMock
import com.outsystems.plugins.barcode.mocks.OSBARCScanLibraryMock
import com.outsystems.plugins.barcode.model.OSBARCError
import com.outsystems.plugins.barcode.model.OSBARCScanParameters
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
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
    private lateinit var mockException: Exception

    companion object {
        private const val SCAN_REQUEST_CODE = 112
        private const val INVALID_REQUEST_CODE = 113
        private const val INVALID_RESULT_CODE = 9
        private const val SCAN_RESULT = "scanResult"
        private const val RESULT_CODE = "myCode"
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
        mockException = Mockito.mock(Exception::class.java)
        Mockito.doReturn(planes).`when`(mockImageProxy).planes
        Mockito.doReturn(mockByteBuffer).`when`(mockPlaneProxy).buffer
        Mockito.doReturn(30).`when`(mockImageProxy).width
        Mockito.doReturn(30).`when`(mockImageProxy).height
        Mockito.doReturn(30).`when`(mockByteBuffer).remaining()
        Mockito.doReturn(mockImageInfo).`when`(mockImageProxy).imageInfo
        Mockito.doReturn("errorMessage").`when`(mockException).message
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
            1,
            "zxing"
        )
        barcodeController.scanCode(mockActivity, parameters)
    }

    @Test
    fun givenResultOKAndBarcodeValidWhenHandleScanResultThenSuccess() {
        Mockito.doReturn(mockBundle).`when`(mockIntent).extras
        Mockito.doReturn(RESULT_CODE).`when`(mockBundle).getString(SCAN_RESULT)

        val barcodeController = OSBARCController()
        barcodeController.handleActivityResult(SCAN_REQUEST_CODE, Activity.RESULT_OK, mockIntent,
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
        Mockito.doReturn(mockBundle).`when`(mockIntent).extras
        Mockito.doReturn(null).`when`(mockBundle).getString(SCAN_RESULT)

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
        Mockito.doReturn(mockBundle).`when`(mockIntent).extras
        Mockito.doReturn(null).`when`(mockBundle).getString(SCAN_RESULT)

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
        Mockito.doReturn(mockBundle).`when`(mockIntent).extras
        Mockito.doReturn(null).`when`(mockBundle).getString(SCAN_RESULT)

        val barcodeController = OSBARCController()
        barcodeController.handleActivityResult(SCAN_REQUEST_CODE, INVALID_RESULT_CODE, mockIntent,
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
        Mockito.doReturn(mockBundle).`when`(mockIntent).extras
        Mockito.doReturn("").`when`(mockBundle).getString(SCAN_RESULT)

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
        val mockImageProxy = Mockito.mock(ImageProxy::class.java)
        val scanLibMock = OSBARCScanLibraryMock().apply {
            success = true
            resultCode = RESULT_CODE
        }
        OSBARCBarcodeAnalyzer(scanLibMock,
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
        OSBARCBarcodeAnalyzer(scanLibMock,
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
        val mockImageProxy = Mockito.mock(ImageProxy::class.java)
        val scanLibMock = OSBARCScanLibraryMock().apply {
            success = false
            exception = false
            error = OSBARCError.ZXING_LIBRARY_ERROR
        }
        OSBARCBarcodeAnalyzer(scanLibMock,
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
        val mockImageProxy = Mockito.mock(ImageProxy::class.java)
        val scanLibMock = OSBARCScanLibraryMock().apply {
            success = false
            exception = false
            error = OSBARCError.MLKIT_LIBRARY_ERROR
        }
        OSBARCBarcodeAnalyzer(scanLibMock,
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
        OSBARCBarcodeAnalyzer(scanLibMock,
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
    fun givenImage90DegreesWhenZXingScanThenSuccess() {
        val wrapper = OSBARCScanLibraryFactory.createScanLibraryWrapper(
            "zxing",
            OSBARCZXingHelperMock().apply { scanResult = SCAN_RESULT },
            OSBARCMLKitHelperMock()
            )

        Mockito.doReturn(90).`when`(mockImageInfo).rotationDegrees // do the same for 270 and 0 to cover all cases

        wrapper.scanBarcode(mockImageProxy,
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

        wrapper.scanBarcode(mockImageProxy,
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

        wrapper.scanBarcode(mockImageProxy,
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

        wrapper.scanBarcode(mockImageProxy,
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

        wrapper.scanBarcode(mockImageProxy,
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
            }
        )

        val mockMediaImage = Mockito.mock(Image::class.java)
        Mockito.doReturn(mockMediaImage).`when`(mockImageProxy).image

        wrapper.scanBarcode(mockImageProxy,
            {
                assertEquals(SCAN_RESULT, it)
            },
            {
                fail()
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

        wrapper.scanBarcode(mockImageProxy,
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
            }
        )

        val mockMediaImage = Mockito.mock(Image::class.java)
        Mockito.doReturn(mockMediaImage).`when`(mockImageProxy).image

        wrapper.scanBarcode(mockImageProxy,
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
            }
        )

        val mockMediaImage = Mockito.mock(Image::class.java)
        Mockito.doReturn(mockMediaImage).`when`(mockImageProxy).image

        wrapper.scanBarcode(mockImageProxy,
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
            OSBARCMLKitHelperMock()
        )

        val mockMediaImage = Mockito.mock(Image::class.java)
        Mockito.doReturn(mockMediaImage).`when`(mockImageProxy).image

        wrapper.scanBarcode(mockImageProxy,
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
            OSBARCMLKitHelperMock()
        )

        Mockito.doReturn(null).`when`(mockImageProxy).image

        wrapper.scanBarcode(mockImageProxy,
            {
                // do nothing
            },
            {
                // do nothing
            }
        )
    }

}