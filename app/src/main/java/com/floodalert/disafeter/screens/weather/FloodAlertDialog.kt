package com.floodalert.disafeter.screens.weather

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

/*
 Alert dialog for river critical level
*/

@Composable
fun FloodAlertDialog(onEvacuate: () -> Unit, onCloseDialog: () -> Unit) {
    AlertDialog(
        onDismissRequest = onCloseDialog,
        title = {
            Text(text = "Marikina River Reached Critical Level!", style = MaterialTheme.typography.titleMedium)
        },
        text = {
            Text(
                text = "Residents are advised to evacuate to the nearest evacuation center. Please check the 'Evacuate' tab for directions. Stay safe!",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(onClick = {
                onEvacuate()
                onCloseDialog()
            }) {
                Text(text = "Evacuate")
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