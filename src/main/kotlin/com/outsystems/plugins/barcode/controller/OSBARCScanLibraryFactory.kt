package com.outsystems.plugins.barcode.controller

import com.outsystems.plugins.barcode.controller.helper.OSBARCMLKitHelperInterface
import com.outsystems.plugins.barcode.controller.helper.OSBARCZXingHelperInterface

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
        fun createScanLibraryWrapper(
            scanLibrary: String,
            zxingHelper: OSBARCZXingHelperInterface,
            mlkitHelper: OSBARCMLKitHelperInterface
        ): OSBARCScanLibraryInterface {
            return when (scanLibrary) {
                LIBRARY_ZXING -> {
                    createZXingWrapper(zxingHelper)
                }

                LIBRARY_MLKIT -> {
                    createMLKitWrapper(mlkitHelper)
                }

                else -> {
                    createZXingWrapper(zxingHelper)
                }
            }
        }

        /**
         * Creates and returns a OSBARCZXingWrapper instance.
         * @return the newly created OSBARCZXingWrapper instance.
         */
        private fun createZXingWrapper(helper: OSBARCZXingHelperInterface): OSBARCZXingWrapper {
            return OSBARCZXingWrapper(helper)
        }

        /**
         * Creates and returns a OSBARCMLKitWrapper instance.
         * @return the newly created OSBARCMLKitWrapper instance.
         */
        private fun createMLKitWrapper(helper: OSBARCMLKitHelperInterface): OSBARCMLKitWrapper {
            return OSBARCMLKitWrapper(helper)
        }
    }
}