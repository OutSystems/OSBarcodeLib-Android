package com.outsystems.plugins.barcode.model

data class OSBARCScanParameters(
    val scanInstructions: String?,
    val cameraDirection: Int?,
    val scanOrientation: Int?,
    val scanButton: Boolean?,
    val scanButtonText: String?,
    val scanningLibrary: String?
)