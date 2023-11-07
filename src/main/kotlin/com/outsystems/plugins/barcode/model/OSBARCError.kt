package com.outsystems.plugins.barcode.model

enum class OSBARCError(val code: Int, val description: String) {
    CAMERA_PERMISSION_DENIED_ERROR(1, "Couldn't access camera. Check your camera permissions and try again."),
    INVALID_PARAMETERS_ERROR(2, "Barcode parameters are invalid."),
    SCAN_CANCELLED_ERROR(3, "Barcode scanning was cancelled."),
    SCANNING_GENERAL_ERROR(4, "There was an error scanning the barcode."),
    ZXING_LIBRARY_ERROR(5, "There was an error scanning the barcode with ZXing."),
    MLKIT_LIBRARY_ERROR(6, "There was an error scanning the barcode with ML Kit.")
}