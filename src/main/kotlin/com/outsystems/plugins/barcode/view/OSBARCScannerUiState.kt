package com.outsystems.plugins.barcode.view

data class OSBARCScannerUiState(
    val hasFlashUnit: Boolean,
    val minZoomRatio: Float,
    val maxZoomRatio: Float
) {
    companion object {
        val DEFAULT = OSBARCScannerUiState(
            hasFlashUnit = false,
            minZoomRatio = 1f,
            maxZoomRatio = 1f
        )
    }
}
