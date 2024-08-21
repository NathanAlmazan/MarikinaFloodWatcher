package com.floodalert.disafeter.screens.evacuate

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.floodalert.disafeter.R
import com.floodalert.disafeter.model.EvacuationCenter


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvacuateFormScreen(
    viewModel: EvacuateViewModel,
    onNavigateToMain: () -> Unit
) {
    val context = LocalContext.current
    val selectedCenter = viewModel.selectedCenter
    var selected by rememberSaveable { mutableStateOf(false) }
    var centerName by rememberSaveable { mutableStateOf("") }
    var centerAddress by rememberSaveable { mutableStateOf("") }
    var centerLatitude by rememberSaveable { mutableStateOf("") }
    var centerLongitude by rememberSaveable { mutableStateOf("") }
    var centerImage by rememberSaveable { mutableStateOf<Uri?>(null) }
    var fileName by rememberSaveable { mutableStateOf<String?>(null) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>("") }

    LaunchedEffect(selectedCenter) {
        if (selectedCenter.latitude > 0) {
            selected = true
            centerName = selectedCenter.name
            centerAddress = selectedCenter.address
            centerLatitude = selectedCenter.latitude.toString()
            centerLongitude = selectedCenter.longitude.toString()
        }
    }


    val createEvacuationCenter = {
        if (fileName == null || centerImage == null) errorMessage = "Image is required."
        else if (centerName.isEmpty())  errorMessage = "Center name is required."
        else if (centerAddress.isEmpty())  errorMessage = "Center address is required."
        else if (centerLatitude.isEmpty())  errorMessage = "Center latitude is required."
        else if (centerLongitude.isEmpty())  errorMessage = "Center longitude is required."
        else if (!centerLatitude.matches(Regex("^\\d*\\.\\d+|\\d+\\.\\d*$"))) errorMessage = "Invalid latitude."
        else if (!centerLongitude.matches(Regex("^\\d*\\.\\d+|\\d+\\.\\d*$"))) errorMessage = "Invalid longitude."
        else {
            viewModel.createEvacuationCenter(
                EvacuationCenter(
                    name = centerName,
                    address = centerAddress,
                    image = fileName!!,
                    latitude = centerLatitude.toDouble(),
                    longitude = centerLongitude.toDouble()
                ),
                centerImage!!
            )
            onNavigateToMain()
        }
    }

    val updateEvacuationCenter = {
        if (centerName.isEmpty())  errorMessage = "Center name is required."
        else if (centerAddress.isEmpty())  errorMessage = "Center address is required."
        else if (centerLatitude.isEmpty())  errorMessage = "Center latitude is required."
        else if (centerLongitude.isEmpty())  errorMessage = "Center longitude is required."
        else if (!centerLatitude.matches(Regex("^\\d*\\.\\d+|\\d+\\.\\d*$"))) errorMessage = "Invalid latitude."
        else if (!centerLongitude.matches(Regex("^\\d*\\.\\d+|\\d+\\.\\d*$"))) errorMessage = "Invalid longitude."
        else {
            viewModel.updateEvacuationCenter(
                EvacuationCenter(
                    generatedId = selectedCenter.generatedId,
                    name = centerName,
                    address = centerAddress,
                    image = fileName ?: selectedCenter.image,
                    latitude = centerLatitude.toDouble(),
                    longitude = centerLongitude.toDouble()
                ),
                centerImage
            )
            onNavigateToMain()
        }
    }

    val deleteEvacuationCenter = {
        viewModel.deleteEvacuationCenter(
            EvacuationCenter(
            generatedId = selectedCenter.generatedId
        )
        )
        onNavigateToMain()
    }

    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { result ->
        fileName = result?.let { DocumentFile.fromSingleUri(context, it)?.name }
        centerImage = result
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (selected) "Edit Center Info" else "Register Center",
            style = MaterialTheme.typography.displaySmall,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp, 30.dp, 24.dp, 12.dp),
        )

        Box(modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(24.dp, 12.dp)
            .clip(RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.BottomEnd
        ) {
            if (centerImage != null) {
                AsyncImage(
                    model = BitmapFactory.decodeStream(context.contentResolver.openInputStream(
                        centerImage!!
                    )),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            else if (selected) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(selectedCenter.image)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            else {
                Image(
                    painterResource(id = R.drawable.ic_baseline_image_24),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.tertiaryContainer),
                    modifier = Modifier.fillMaxSize()
                )
            }

            Button(
                onClick = {
                    launcher.launch("image/*")
                },
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.tertiary)
            ) {
                Text(text = "Upload Image")
            }
        }

        OutlinedTextField(
            value = centerName,
            onValueChange = { centerName = it },
            label = {
                Text(
                    text = "Center Name",
                    style = MaterialTheme.typography.labelLarge
                )
            },
            placeholder = {
                Text(
                    text = "e.g. 'Basketball Court'",
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
            value = centerAddress,
            onValueChange = { centerAddress = it },
            label = {
                Text(
                    text = "Center Address",
                    style = MaterialTheme.typography.labelLarge
                )
            },
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_baseline_location_on_24),
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
            value = centerLatitude,
            onValueChange = { centerLatitude = it },
            label = {
                Text(
                    text = "Center Latitude",
                    style = MaterialTheme.typography.labelLarge
                )
            },
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_baseline_my_location_24),
                    contentDescription = null
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            isError = false,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp, 8.dp)
        )

        OutlinedTextField(
            value = centerLongitude,
            onValueChange = { centerLongitude = it },
            label = {
                Text(
                    text = "Center Longitude",
                    style = MaterialTheme.typography.labelLarge
                )
            },
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_baseline_my_location_24),
                    contentDescription = null
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            isError = false,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp, 8.dp)
        )

        errorMessage?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        if (selected) {
            Button(
                onClick = updateEvacuationCenter,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Text(text = "Update Center")
            }
            Button(
                onClick = deleteEvacuationCenter,
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                ) {
                Text(text = "Remove Center")
            }
        } else {
            Button(
                onClick = createEvacuationCenter,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Text(text = "Register Center")
            }
        }

        OutlinedButton(
            onClick = onNavigateToMain,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Text(text = "Cancel")
        }
    }
}

