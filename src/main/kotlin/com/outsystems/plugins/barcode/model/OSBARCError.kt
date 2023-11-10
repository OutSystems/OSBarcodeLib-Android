package com.outsystems.plugins.barcode.model

/**
 * Enum class that holds the library's errors.
 */
enum class OSBARCError(val code: Int, val description: String) {
    CAMERA_PERMISSION_DENIED_ERROR(7, "Scanning cancelled due to missing camera permissions."),
    INVALID_PARAMETERS_ERROR(10, "Scanning parameters are invalid."),
    SCAN_CANCELLED_ERROR(6, "Scanning cancelled."),
    SCANNING_GENERAL_ERROR(4, "Error while trying to scan code."),
    ZXING_LIBRARY_ERROR(11, "There was an error scanning the barcode with ZXing."),
    MLKIT_LIBRARY_ERROR(12, "There was an error scanning the barcode with ML Kit.")
}