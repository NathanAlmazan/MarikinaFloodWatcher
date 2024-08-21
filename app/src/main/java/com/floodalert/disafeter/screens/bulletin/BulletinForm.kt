package com.floodalert.disafeter.screens.bulletin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.floodalert.disafeter.R
import com.floodalert.disafeter.model.Bulletin
import com.floodalert.disafeter.screens.emergency.DropDownTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BulletinFormScreen(
    viewModel: BulletinViewModel,
    onNavigateToBulletin: () -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var severity by rememberSaveable { mutableStateOf("High") }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    val selected = viewModel.selected
    val submitError = viewModel.errorMessage

    LaunchedEffect(selected) {
        if (selected != null) {
            title = selected.title
            description = selected.description
            severity = selected.severity.replaceFirstChar { it.uppercase() }
        }
    }

    val addAnnouncement = {
        if (title.isEmpty()) errorMessage = "Title is required."
        else if (description.isEmpty()) errorMessage = "Description is required."
        else {
            viewModel.createAnnouncement(Bulletin(
                "", title, description, severity.lowercase()
            ))
            onNavigateToBulletin()
        }
    }

    val updateAnnouncement = {
        if (title.isEmpty()) errorMessage = "Title is required."
        else if (description.isEmpty()) errorMessage = "Description is required."
        else if (selected != null) {
            viewModel.updateAnnouncement(Bulletin(
                selected.uid, title, description, severity.lowercase()
            ))
            onNavigateToBulletin()
        }
    }

    val deleteAnnouncement = {
        if (selected != null) {
            viewModel.deleteAnnouncement(selected.uid)
            onNavigateToBulletin()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (selected != null) "Edit Announcement" else "Add Announcement",
            style = MaterialTheme.typography.displaySmall,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp, 12.dp),
        )

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = {
                Text(
                    text = "Subject",
                    style = MaterialTheme.typography.labelLarge
                )
            },
            placeholder = {
                Text(
                    text = "e.g. 'Evacuate Immediately!'",
                    style = MaterialTheme.typography.labelLarge
                )
            },
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_baseline_announcement_24),
                    contentDescription = null
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text
            ),
            isError = false,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp, 8.dp)
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = {
                Text(
                    text = "Description",
                    style = MaterialTheme.typography.labelLarge
                )
            },
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_baseline_announcement_24),
                    contentDescription = null
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text
            ),
            isError = false,
            singleLine = false,
            maxLines = 3,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp, 8.dp),
        )

        DropDownTextField(
            label = "Severity",
            selectedText = severity,
            options = listOf("High", "Medium", "Low"),
            onSelect = { severity = it }
        )

        submitError?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        errorMessage?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        if (selected == null) {
            Button(
                onClick = addAnnouncement,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp, 8.dp)
            ) {
                Text(text = "Add Announcement")
            }
        } else {
            Button(
                onClick = updateAnnouncement,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp, 8.dp)
            ) {
                Text(text = "Update Announcement")
            }
            Button(
                onClick = deleteAnnouncement,
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp, 8.dp),

                ) {
                Text(text = "Delete Announcement")
            }
        }

        OutlinedButton(
            onClick = onNavigateToBulletin,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Text(text = "Cancel")
        }
    }
}