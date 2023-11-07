package com.outsystems.plugins.barcode.controller

/**
 * A factory class to create instances of OSBARCScanLibraryInterface.
 */
class OSBARCScanLibraryFactory {

    companion object {
        private const val LIBRARY_ZXING = "zxing"
        private const val LIBRARY_MLKIT = "mlkit"

        /**
         * Creates and returns OSPMTGatewayInterface instance.
         * @param scanLibrary - String to identify which library to use
         * @return the newly created OSBARCScanLibraryInterface instance.
         */
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

        /**
         * Creates and returns a OSBARCZXingWrapper instance.
         * @return the newly created OSBARCZXingWrapper instance.
         */
        private fun createZXingWrapper(): OSBARCZXingWrapper {
            return OSBARCZXingWrapper()
        }

        /**
         * Creates and returns a OSBARCMLKitWrapper instance.
         * @return the newly created OSBARCMLKitWrapper instance.
         */
        private fun createMLKitWrapper(): OSBARCMLKitWrapper {
            return OSBARCMLKitWrapper()
        }
    }
}