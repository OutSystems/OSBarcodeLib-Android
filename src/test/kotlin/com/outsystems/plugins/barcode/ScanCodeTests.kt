package com.outsystems.plugins.barcode

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.camera.core.ImageProxy
import com.outsystems.plugins.barcode.controller.OSBARCBarcodeAnalyzer
import com.outsystems.plugins.barcode.controller.OSBARCController
import com.outsystems.plugins.barcode.mocks.OSBARCScanLibraryMock
import com.outsystems.plugins.barcode.model.OSBARCError
import com.outsystems.plugins.barcode.model.OSBARCScanParameters
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class ScanCodeTests {

    private lateinit var mockActivity: Activity
    private lateinit var mockIntent: Intent
    private lateinit var mockBundle: Bundle

    companion object {
        private const val SCAN_REQUEST_CODE = 112
        private const val SCAN_RESULT = "scanResult"
        private const val RESULT_CODE = "myCode"
    }

    @Before
    fun before() {
        mockActivity = Mockito.mock(Activity::class.java)
        mockIntent = Mockito.mock(Intent::class.java)
        mockBundle = Mockito.mock(Bundle::class.java)
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
    fun givenIntentNullWhenHandleScanResultThenSuccess() {
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
    fun givenBundleNullWhenHandleScanResultThenSuccess() {
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
    fun givenStringNullWhenHandleScanResultThenSuccess() {
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
    fun givenResultOKAndBarcodeNullWhenHandleScanResultThenScanningError() {
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

}