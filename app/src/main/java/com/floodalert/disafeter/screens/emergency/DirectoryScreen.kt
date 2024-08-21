package com.floodalert.disafeter.screens.emergency

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.floodalert.disafeter.R
import com.floodalert.disafeter.model.EmergencyDirectory

@Composable
fun DirectoryScreen(
    viewModel: EmergencyViewModel,
    onNavigateToEmergency: () -> Unit
) {
    var directoryName by rememberSaveable { mutableStateOf("") }
    var directoryContact by rememberSaveable { mutableStateOf("") }
    var directoryType by rememberSaveable { mutableStateOf("Mobile") }
    var directoryCategory by rememberSaveable { mutableStateOf("Barangay") }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    val selected = viewModel.selectedDirectory
    val submitError = viewModel.errorMessage

    val createDirectory = {
        if (directoryName.isEmpty()) errorMessage = "Directory name is required."
        else if (directoryContact.length < 7 && directoryType == "Telephone") errorMessage = "Invalid contact number."
        else if (directoryContact.length != 11 && directoryType == "Mobile") errorMessage = "Invalid contact number."
        else {
            viewModel.createDirectory(
                EmergencyDirectory(
                name = directoryName, contact = directoryContact, type = directoryType.lowercase(), category = directoryCategory.lowercase()
            )
            )
            onNavigateToEmergency()
        }
    }

    val updateDirectory = {
        if (selected == null) errorMessage = "Selected directory is required."
        else if (directoryName.isEmpty()) errorMessage = "Directory name is required."
        else if (directoryContact.length < 7 && directoryType == "Telephone") errorMessage = "Invalid contact number."
        else if (directoryContact.length != 11 && directoryType == "Mobile") errorMessage = "Invalid contact number."
        else {
            viewModel.updateDirectory(
                EmergencyDirectory(
                selected.generatedId, directoryName, directoryContact, directoryType.lowercase(), directoryCategory.lowercase()
            )
            )
            onNavigateToEmergency()
        }
    }

    val deleteDirectory = {
        if (selected == null) errorMessage = "Selected directory is required."
        else {
            viewModel.deleteDirectory(
                EmergencyDirectory(
                selected.generatedId, directoryName, directoryContact, directoryType.lowercase(), directoryCategory.lowercase()
            )
            )
            onNavigateToEmergency()
        }
    }

    LaunchedEffect(selected) {
        if (selected != null) {
            directoryName = selected.name
            directoryContact = selected.contact
            directoryType = selected.type.replaceFirstChar { it.uppercase() }
            directoryCategory = selected.category.replaceFirstChar { it.uppercase() }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (selected != null) "Edit Directory" else "Create Directory",
            style = MaterialTheme.typography.displaySmall,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp, 12.dp),
        )

        OutlinedTextField(
            value = directoryName,
            onValueChange = { directoryName = it },
            label = {
                Text(
                    text = "Directory Name",
                    style = MaterialTheme.typography.labelLarge
                )
            },
            placeholder = {
                Text(
                    text = "e.g. 'Health Center'",
                    style = MaterialTheme.typography.labelLarge
                )
            },
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_home_city),
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
            value = directoryContact,
            onValueChange = { directoryContact = it },
            label = {
                Text(
                    text = if (directoryType == "Email") "Email" else "Contact Number",
                    style = MaterialTheme.typography.labelLarge
                )
            },
            placeholder = {
                Text(
                    text = if (directoryType == "Email") "e.g. john.doe@gmail.com" else "e.g. 09*********",
                    style = MaterialTheme.typography.labelLarge
                )
            },
            leadingIcon = {
                Icon(
                    painter = if (directoryType == "Email") painterResource(R.drawable.ic_email) else painterResource(R.drawable.ic_phone),
                    contentDescription = null
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = if (directoryType == "Email") KeyboardType.Email else KeyboardType.Number
            ),
            isError = false,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp, 8.dp)
        )

        DropDownTextField(
            label = "Contact Type",
            selectedText = directoryType,
            options = listOf("Mobile", "Telephone", "Email"),
            onSelect = { directoryType = it }
        )

        if (selected == null) {
            DropDownTextField(
                label = "Category",
                selectedText = directoryCategory,
                options = listOf("Barangay", "Medical", "Municipal", "NDRRMC"),
                onSelect = { directoryCategory = it }
            )
        }

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
                onClick = createDirectory,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp, 8.dp)
            ) {
                Text(text = "Create Directory")
            }
        } else {
            Button(
                onClick = updateDirectory,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp, 8.dp)
            ) {
                Text(text = "Update Directory")
            }
            Button(
                onClick = deleteDirectory,
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp, 8.dp),

            ) {
                Text(text = "Delete Directory")
            }
        }

        OutlinedButton(
            onClick = onNavigateToEmergency,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Text(text = "Cancel")
        }
    }
}

@Composable
fun DropDownTextField(
    label: String,
    selectedText: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    var textFieldSize by remember { mutableStateOf(Size.Zero) }

    val icon = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp, 8.dp)
    ) {
        OutlinedTextField(
            value = selectedText,
            onValueChange = { onSelect(it) },
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    // This value is used to assign to
                    // the DropDown the same width
                    textFieldSize = coordinates.size.toSize()
                },
            label = {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge
                )
            },
            trailingIcon = {
                Icon(
                    imageVector = icon,
                    contentDescription = "contentDescription",
                    Modifier.clickable { expanded = !expanded })
            },
            readOnly = true
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(with(LocalDensity.current){textFieldSize.width.toDp()})
        ) {
            options.forEach { label ->
                DropdownMenuItem(
                    text = { 
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    onClick = {
                        onSelect(label)
                        expanded = false
                    }
                )
            }
        }
    }
}