package com.floodalert.disafeter.screens.evacuate

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

/*
 Alert dialog to ask user to open their GPS
*/

@Composable
fun LocationAlertDialog(onRequestLocation: () -> Unit, onCloseDialog: () -> Unit) {
    AlertDialog(
        onDismissRequest = onCloseDialog,
        title = {
            Text(text = "Your phone's GPS is disabled", style = MaterialTheme.typography.titleMedium)
        },
        text = {
            Text(
                text = "Please switch your GPS on so we can provide you accurate directions. Thank you",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(onClick = {
                onRequestLocation()
                onCloseDialog()
            }) {
                Text(text = "Okay")
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