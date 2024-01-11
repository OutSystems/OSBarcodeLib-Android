# OSBarcodeLib

An Android library to scan barcodes, using ML Kit or ZXing for code detection. The UI is implemented with Jetpack Compose, and image analysis is setup with CameraX (https://developer.android.com/reference/androidx/camera/core/ImageAnalysis).

The library supports many popular encoding types of 1D and 2D barcodes, such as:
- 1D Barcodes
	- Codabar
	- Code 39
	- Code 93
	- Code 128
	- Databar (GS1)	(*only with ML Kit*)
	- EAN-8
	- EAN-13
	- ITF
	- ISBN-10
	- ISBN-13
	- ISBN-13 Dual Barcode
	- RSSExpanded (*only with ZXing*)
	- UPC-A
	- UPC-E
- 2D Barcodes
	- Aztec Code
	- Data Matrix
 	- MaxiCode (*only with ZXing*)
	- PDF 417
	- QR Code

The `OSBARCController` class provides the main feature of the library, which is the **Barcode Scanner**, to be detailed in the following sections. 
The `OSBARCScanLibraryFactory` provides a way to create a instance of `OSBARCScanLibraryInterface`, which has an implementation for ML Kit and another for ZXing.

## Index

- [Motivation](#motivation)
- [Usage](#usage)
- [Methods](#methods)
    - [scanBarcode](#scanbarcode)
    - [handleActivityResult](#handleactivityresult)

## Motivation

This library is to be used by the [Barcode Plugin](https://github.com/OutSystems/cordova-outsystems-barcode).

## Usage

In your app-level gradle file, import the OSBarcodeLib library like so:

```gradle
dependencies {
    implementation("com.github.outsystems:osbarcode-android:1.0.0@aar")
}
```

## Methods

### scanBarcode

```kotlin
fun scanCode(
    activity: Activity,
    parameters: OSBARCScanParameters
)
```

A method that triggers the barcode reader/scanner, opening a new activity with the scanning UI.

#### Parameters

- **activity**: The activity to be used to launch the activity with the scanning UI. When scanning ends, either because a code was detected or by being cancelled, the result will come in the ``onActivityResult()`` of this activity.
- **parameters**: OSBARCScanParameters object that contains all the barcode parameters to be used when scanning.
	- **scanInstructions**: A string that contains the scan instructions to be displayed in the screen.
	- **cameraDirection**: An integer indicating which camera to use - back or front.
	- **scanOrientation**: An integer indicating which scan orientation to use - portrait, landscape, or adaptive.
	- **scanButton**: A boolean that will display a scan button on the barcode reader. If true, scanning will only be triggered when pressing the button instead of automatically when framing the barcode. A second click on the button disables scannning.
  - **scanText**: A string that contains the text to be displayed on the scan button. It will only be shown if **scanButton** is set to true.
  - **hint**: An integer that holds a hint to what type of barcode to look for. **This parameter isn't being used yet**.
	- **androidScanningLibrary**: A string which determines what barcode library to use - ML Kit or ZXing.
    
#### Usage

```kotlin
var barcodeController = OSBARCController()
barcodeController.scanCode(activity, parameters)
```

### handleActivityResult

```kotlin
fun handleActivityResult(
        requestCode: Int,
        resultCode: Int,
        intent: Intent?,
        onSuccess: (String) -> Unit,
        onError: (OSBARCError) -> Unit
    )
```

A method that can be used to handle the activitu result from scanning a barcode.

#### Parameters

- **requestCode**: The code identifying the request.
- **resultCode**: The code identifying the result of the request.
- **intent**: The resulting intent from the operation.
- **onSuccess**: The code to be executed if the operation was successful.
- **onError**: The code to be executed if the operation was not successful.
    
#### Usage

```kotlin
barcodeController.handleActivityResult(requestCode, resultCode, intent,
    { result ->
        // handle success, probably returning result
    },
    { error ->
        // handle error, probably returning it
    }
)
```

#### Errors

|Code|Message|
|:-|:-|
|OS-PLUG-BARC-0004|Error while trying to scan code.|
|OS-PLUG-BARC-0006|Couldn't scan because the process was cancelled.|
|OS-PLUG-BARC-0007|Couldn't scan because camera access wasn’t provided. Check your camera permissions and try again.|
|OS-PLUG-BARC-0008|Scanning parameters are invalid.|
|OS-PLUG-BARC-0009|There was an error scanning the barcode with ZXing.|
|OS-PLUG-BARC-0010|There was an error scanning the barcode with ML Kit.|
