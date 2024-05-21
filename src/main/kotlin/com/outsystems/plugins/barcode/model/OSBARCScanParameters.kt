package com.outsystems.plugins.barcode.model

import java.io.Serializable
import com.google.gson.annotations.SerializedName

/**
 * Data class that represents the object with the scan parameters.
 */
data class OSBARCScanParameters(
    @SerializedName("scanInstructions") val scanInstructions: String?,
    @SerializedName("cameraDirection") val cameraDirection: Int?,
    @SerializedName("scanOrientation") val scanOrientation: Int?,
    @SerializedName("scanButton") val scanButton: Boolean,
    @SerializedName("scanText") val scanText: String,
    @SerializedName("hint") val hint: Int?,
    @SerializedName("androidScanningLibrary") val androidScanningLibrary: String?
) : Serializable