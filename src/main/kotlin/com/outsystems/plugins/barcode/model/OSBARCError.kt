package com.outsystems.plugins.barcode.model

/**
 * Enum class that holds the library's errors.
 */
enum class OSBARCError(val code: Int, val description: String) {
    SCANNING_GENERAL_ERROR(4, "Error while trying to scan code."),
    SCAN_CANCELLED_ERROR(6, "Couldn't scan because the process was cancelled."),
    CAMERA_PERMISSION_DENIED_ERROR(7, "Couldn't scan because camera access wasn't provided. Check your camera permissions and try again."),
    INVALID_PARAMETERS_ERROR(8, "Scanning parameters are invalid."),
    ZXING_LIBRARY_ERROR(9, "There was an error scanning the barcode with ZXing."),
    MLKIT_LIBRARY_ERROR(10, "There was an error scanning the barcode with ML Kit.")
}