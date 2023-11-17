package com.outsystems.plugins.barcode.view

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp

@Composable
fun CameraPermissionRequiredDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    permissionGiven: Boolean,
    shouldShowDialog: Boolean,
    dialogTitle: String,
    dialogText: String,
    confirmButtonText: String,
    dismissButtonText: String
) {
    if (!permissionGiven && shouldShowDialog) {
        AlertDialog(
            title = {
                Text(
                    text = dialogTitle,
                    fontSize = 20.sp
                )
            },
            text = {
                Text(text = dialogText)
            },
            onDismissRequest = {
                onDismissRequest()
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onConfirmation()
                    }
                ) {
                    Text(confirmButtonText)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onDismissRequest()
                    }
                ) {
                    Text(dismissButtonText)
                }
            }
        )
    }
}