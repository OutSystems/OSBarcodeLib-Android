package com.outsystems.plugins.barcode.model

enum class OSBARCError(val code: Int, val description: String) {
    CAMERA_PERMISSION_DENIED_ERROR(1, "Couldn't access camera. Check your camera permissions and try again."),
    INVALID_PARAMETERS_ERROR(2, "Barcode parameters are invalid.")
}