package com.outsystems.plugins.barcode.model

import java.io.Serializable

/**
 * Data class that represents the object with the scan parameters.
 */
data class OSBARCScanParameters(
    val scanInstructions: String?,
    val cameraDirection: Int?,
    val scanOrientation: Int?,
    val scanButton: Boolean?,
    val scanText: String?,
    val hint: Int?,
    val androidScanningLibrary: String?
) : Serializable