package com.floodalert.disafeter.screens.emergency

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@Composable
fun CallPermissionDeniedDialog(onCloseDialog: () -> Unit) {
    AlertDialog(
        onDismissRequest = onCloseDialog,
        title = {
            Text(text = "Permission Denied!", style = MaterialTheme.typography.titleMedium)
        },
        text = {
            Text(
                text = "Permission to call is denied. To use this feature, please go to settings and allow it. Thank you",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {

        },
        dismissButton = {
            OutlinedButton(
                onClick = onCloseDialog
            ) {
                Text(text = "Okay")
            }
        }
    )
}