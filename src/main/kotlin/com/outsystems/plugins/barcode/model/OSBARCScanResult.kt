package com.outsystems.plugins.barcode.model

import java.io.Serializable

data class OSBARCScanResult(
    val text: String,
    val format: OSBARCScannerHint = OSBARCScannerHint.UNKNOWN
): Serializable
