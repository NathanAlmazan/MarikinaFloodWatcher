package com.floodalert.disafeter.screens.emergency

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@Composable
fun CallPhoneAlertDialog(name: String, onCallDirectory: () -> Unit, onCloseDialog: () -> Unit) {
    AlertDialog(
        onDismissRequest = onCloseDialog,
        title = {
            Text(text = "Are you sure you want to call $name?", style = MaterialTheme.typography.titleMedium)
        },
        text = {
            Text(
                text = "This is an emergency service. Calling for unnecessary reasons can be punishable. Thank you",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(onClick = {
                onCallDirectory()
                onCloseDialog()
            }) {
                Text(text = "Call")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onCloseDialog
            ) {
                Text(text = "Cancel")
            }
        }
    )
}