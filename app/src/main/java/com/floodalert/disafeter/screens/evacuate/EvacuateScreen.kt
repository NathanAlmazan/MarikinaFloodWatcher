package com.floodalert.disafeter.screens.evacuate

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.google.android.gms.maps.model.LatLng
import com.floodalert.disafeter.MainViewModel
import com.floodalert.disafeter.model.EvacuationCenter

@Composable
fun EvacuateScreen(
    mainViewModel: MainViewModel,
    viewModel: EvacuateViewModel,
    onNavigateToMap: () -> Unit,
    onNavigateToEdit: () -> Unit
) {
    val selectedCenter = viewModel.selectedCenter
    val userLocation = mainViewModel.userLocation
    val showDialog = remember { mutableStateOf(false) }
    var isAdmin by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (mainViewModel.currentUser != null) {
            mainViewModel.hideActionButton = !mainViewModel.currentUser!!.admin
        }
        mainViewModel.hideNavbar = false
        viewModel.setSelectedCenter(EvacuationCenter())
    }

    LaunchedEffect(selectedCenter) {
        viewModel.resetDirection()
    }

    LaunchedEffect(mainViewModel.currentUser) {
        if (mainViewModel.currentUser != null) {
            isAdmin = mainViewModel.currentUser!!.admin
        }
    }

    val onSearchClosestCenter = {
        if (userLocation.latitude > 0 && userLocation.longitude > 0) {
            viewModel.calculateClosestCenter(userLocation)
            onNavigateToMap()
        } else {
            showDialog.value = true
        }
    }

    if (showDialog.value) {
        LocationAlertDialog(
            onRequestLocation = { mainViewModel.requestUserLocation() },
            onCloseDialog = { showDialog.value = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(380.dp),
            shape = RoundedCornerShape(0.dp, 0.dp, 0.dp, 120.dp),
            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.secondaryContainer)
        ) {
            ConstraintLayout(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(0.dp, 40.dp, 0.dp, 0.dp)
            ) {
                val (button, headline, image) = createRefs()

                Image(
                    painter = painterResource(com.floodalert.disafeter.R.drawable.house_flood),
                    contentDescription = null,
                    modifier = Modifier.constrainAs(image) {
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                )

                Text(
                    text = stringResource(com.floodalert.disafeter.R.string.evacuate_hero_banner),
                    style = MaterialTheme.typography.headlineLarge,
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .padding(24.dp, 16.dp)
                        .constrainAs(headline) {
                            top.linkTo(parent.top)
                            end.linkTo(parent.end)
                        }
                )

                Button(
                    onClick = onSearchClosestCenter,
                    elevation = ButtonDefaults.buttonElevation(12.dp),
                    modifier = Modifier
                        .padding(24.dp, 0.dp)
                        .constrainAs(button) {
                            top.linkTo(headline.bottom)
                            end.linkTo(parent.end)
                        }
                ) {
                    Icon(
                        Icons.Filled.Search,
                        contentDescription = stringResource(com.floodalert.disafeter.R.string.button_icon),
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text(
                        text = "Search",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }

        Text(
            text = stringResource(com.floodalert.disafeter.R.string.evacuation_centers),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp, 24.dp, 24.dp, 0.dp)
        )

        LazyRow {
            items(viewModel.centers) { place ->
                PlacesCard(
                    place,
                    {
                        viewModel.setSelectedCenter(place)
                        onNavigateToMap()
                    },
                    {
                        if (userLocation.latitude > 0 && userLocation.longitude > 0) {
                            viewModel.setSelectedCenter(place)
                            viewModel.fetchDirection(
                                LatLng(userLocation.latitude, userLocation.longitude),
                                LatLng(place.latitude, place.longitude)
                            )
                            onNavigateToMap()
                        } else {
                            showDialog.value = true
                        }
                    },
                    {
                        viewModel.setSelectedCenter(place)
                        onNavigateToEdit()
                    },
                    isAdmin
                )
            }
        }
    }

}