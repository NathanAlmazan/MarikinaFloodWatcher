package com.floodalert.disafeter.screens.evacuate

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.google.android.gms.maps.model.LatLng
import com.floodalert.disafeter.MainViewModel
import com.floodalert.disafeter.R
import com.floodalert.disafeter.model.repos.Step
import kotlin.math.pow
import kotlin.math.sqrt

@Composable
fun EvacuateMapScreen(
    mainViewModel: MainViewModel,
    viewModel: EvacuateViewModel,
    onNavigateToMain: () -> Unit
) {
    val directions = viewModel.directions
    val selectedCenter = viewModel.selectedCenter
    val userLocation = mainViewModel.userLocation
    val showDialog = remember { mutableStateOf(false) }

    val shape = remember { mutableStateOf<String?>(null) }
    val distance = remember { mutableIntStateOf(0) }
    val travelTime = remember { mutableStateOf<String?>(null) }
    val instructions = remember { mutableStateOf<List<Step?>?>(null) }
    val expand = remember { mutableStateOf(false) }
    val cardHeight by animateDpAsState(
        targetValue = if (expand.value) 500.dp else 250.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ), label = "evacuate map"
    )

    // convert the instructions and directions from google API to readable text
    LaunchedEffect(directions) {
        shape.value = directions?.routes?.get(0)?.overviewPolyline?.points
        travelTime.value = directions?.routes?.get(0)?.legs?.get(0)?.duration?.text
        instructions.value = directions?.routes?.get(0)?.legs?.get(0)?.steps
    }

    // calculate the distance between the user and the evacuation center using the pythagorean theorem
    LaunchedEffect(userLocation) {
        if (userLocation.latitude > 0 && userLocation.longitude > 0) {
            val hypotenuse = sqrt(
                (userLocation.latitude - selectedCenter.latitude).pow(2) +
                        (userLocation.longitude - selectedCenter.longitude).pow(2)
            )

            distance.intValue = (hypotenuse * 111139).toInt()
        }
    }

    val onRequestDirections = {
        if (userLocation.latitude > 0 && userLocation.longitude > 0) {
            viewModel.fetchDirection(
                LatLng(userLocation.latitude, userLocation.longitude),
                LatLng(selectedCenter.latitude, selectedCenter.longitude)
            )
            expand.value = true
        } else {
            showDialog.value = true
        }
    }

    if (showDialog.value) {
        LocationAlertDialog(
            onRequestLocation = { mainViewModel.requestUserLocation() },
            onCloseDialog = {  showDialog.value = false }
        )
    }

    ConstraintLayout(modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.secondaryContainer)) {
        val (map, card, button) = createRefs()

        /*
            Displays the Google Map. The Google Map is commented out
            to save cost. There is a free google map and google direction
            request per month. So to prevent exceeding the free tier
            we comment out the google map unless its necessary.
        */
        Card(modifier = Modifier
            .fillMaxWidth()
            .constrainAs(map) {
                top.linkTo(parent.top)
                bottom.linkTo(card.top)
                height = Dimension.fillToConstraints
            },
            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(0.dp, 0.dp, 0.dp, 120.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            GoogleMaps(selected = selectedCenter, shape.value, userLocation)
        }

        Column(modifier = Modifier
            .fillMaxWidth()
            .height(cardHeight)
            .padding(24.dp, 24.dp, 24.dp, 0.dp)
            .constrainAs(card) {
                bottom.linkTo(parent.bottom)
            }
        ) {

            Text(
                text = selectedCenter.name,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.secondary
            )

            Text(
                text = if (travelTime.value != null) "${travelTime.value} away" else "${distance.intValue}m away",
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                text = selectedCenter.address,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(0.dp, 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp, 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (shape.value == null) {
                    Button(
                        onClick = onRequestDirections,
                        elevation = ButtonDefaults.buttonElevation(12.dp),
                        colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Icon(
                            painterResource(R.drawable.ic_baseline_directions_24),
                            contentDescription = stringResource(R.string.button_icon),
                            modifier = Modifier.size(ButtonDefaults.IconSize),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text(
                            text = "Show Directions",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
                    Button(
                        onClick = { expand.value = !expand.value },
                        elevation = ButtonDefaults.buttonElevation(12.dp),
                        colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Icon(
                            painterResource(R.drawable.ic_baseline_checklist_24),
                            contentDescription = stringResource(R.string.button_icon),
                            modifier = Modifier.size(ButtonDefaults.IconSize),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text(
                            text = if (expand.value) "See Map" else "See Instructions",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = expand.value,
                enter = slideInVertically(),
                exit = fadeOut()
            ) {
                instructions.value?.let {
                    LazyColumn(modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(it) { index, item ->
                            InstructionCard(item, index)
                        }
                    }
                }
            }
        }

        // Button to go back in the previous screeen
        Button(
            onClick = onNavigateToMain,
            shape = CircleShape,
            contentPadding = PaddingValues(0.dp),
            modifier= Modifier
                .padding(16.dp, 40.dp)
                .constrainAs(button) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                }
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
        }
    }
}

// The card that displays the instruction from the user location
// to the evauation center

@Composable
fun InstructionCard(instruction: Step?, index: Int) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(0.dp, 8.dp)
        .height(100.dp)) {
        Row(modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)) {

            Column(modifier = Modifier.fillMaxHeight(), verticalArrangement = Arrangement.Center) {
                Box(modifier = Modifier
                    .height(50.dp)
                    .width(50.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (index + 1).toString(),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Column(modifier = Modifier
                .weight(1f, true)
                .fillMaxHeight()
                .padding(12.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = instruction?.htmlInstructions?.replace("<[^>]*>".toRegex(), "") ?: "",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}