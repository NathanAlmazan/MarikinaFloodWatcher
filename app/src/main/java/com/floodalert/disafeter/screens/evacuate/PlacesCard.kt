package com.floodalert.disafeter.screens.evacuate

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.floodalert.disafeter.R
import com.floodalert.disafeter.model.EvacuationCenter

/*
 This card aesthetically display the
 available evacuation centers
*/

@Composable
fun PlacesCard(
    place: EvacuationCenter,
    onSelect: () -> Unit,
    onLocate: () -> Unit,
    onEdit: () -> Unit,
    isAdmin: Boolean
) {
    Card(
        modifier = Modifier
            .width(250.dp)
            .height(320.dp)
            .padding(8.dp, 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.onPrimary),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(place.image)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(16.dp))
            )
            Text(
                text = place.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(12.dp, 8.dp, 12.dp, 4.dp),
                overflow = TextOverflow.Clip
            )
            Text(
                text = place.address,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(12.dp, 0.dp)
            )
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onSelect,
                        colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Icon(
                            painterResource(R.drawable.ic_baseline_my_location_24),
                            contentDescription = stringResource(R.string.button_icon),
                            modifier = Modifier.size(ButtonDefaults.IconSize),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text(
                            text = "Locate",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    Button(
                        onClick = onLocate,
                        shape = CircleShape,
                        contentPadding = PaddingValues(0.dp),
                        modifier= Modifier.size(40.dp)
                    ) {
                        Icon(
                            painterResource(R.drawable.ic_baseline_directions_24),
                            contentDescription = stringResource(R.string.button_icon),
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                    }

                    if (isAdmin) {
                        OutlinedButton(
                            onClick = onEdit,
                            shape = CircleShape,
                            contentPadding = PaddingValues(0.dp),
                            modifier= Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = stringResource(R.string.button_icon),
                                modifier = Modifier.size(ButtonDefaults.IconSize)
                            )
                        }
                    }
                }
            }
        }
    }
}