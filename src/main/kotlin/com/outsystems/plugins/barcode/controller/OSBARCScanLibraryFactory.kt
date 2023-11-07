package com.outsystems.plugins.barcode.controller

class OSBARCScanLibraryFactory {

    companion object {
        private const val LIBRARY_ZXING = "zxing"
        private const val LIBRARY_MLKIT = "mlkit"

        fun createScanLibraryWrapper(scanLibrary: String): OSBARCScanLibraryInterface {
            return when (scanLibrary) {
                LIBRARY_ZXING -> {
                    createZXingWrapper()
                }
                LIBRARY_MLKIT -> {
                    createMLKitWrapper()
                }
                else -> {
                    createZXingWrapper()
                }
            }
        }

        private fun createZXingWrapper(): OSBARCZXingWrapper {
            return OSBARCZXingWrapper()
        }

        private fun createMLKitWrapper(): OSBARCMLKitWrapper {
            return OSBARCMLKitWrapper()
        }
    }
}