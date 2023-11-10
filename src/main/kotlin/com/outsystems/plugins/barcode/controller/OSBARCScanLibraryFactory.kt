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
            return if (scanLibrary == LIBRARY_MLKIT) {
                OSBARCMLKitWrapper(mlkitHelper)
            } else {
                OSBARCZXingWrapper(zxingHelper)
            }
        }
    }
}