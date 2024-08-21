package com.floodalert.disafeter.screens.evacuate

import android.Manifest
import android.location.Location
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import com.google.maps.android.compose.*
import com.floodalert.disafeter.R
import com.floodalert.disafeter.model.EvacuationCenter

/*
 This displays the google maps
*/

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun GoogleMaps(selected: EvacuationCenter, directions: String?, userLocation: Location) {
    val permissionState = rememberPermissionState(
        permission = Manifest.permission.ACCESS_FINE_LOCATION,
    )

    when (permissionState.status) {
        PermissionStatus.Granted -> {
            val location = LatLng(selected.latitude, selected.longitude)
            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(location, 16f)
            }

            GoogleMap(
                cameraPositionState = cameraPositionState,
                modifier = Modifier.fillMaxSize()
            ) {
                Marker(
                    state = MarkerState(position = location),
                    title = selected.name,
                    snippet = selected.name
                )

                directions?.let {
                    Marker(
                        state = MarkerState(position = LatLng(userLocation.latitude, userLocation.longitude)),
                        title = selected.name,
                        snippet = "Your location"
                    )
                }

                directions?.let {
                    Polyline(
                        points = PolyUtil.decode(it),
                        jointType = JointType.ROUND,
                        color = Color(0xFF93000A),
                        zIndex = 1f,
                        width = 12f
                    )
                }
            }
        }
        is PermissionStatus.Denied -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.location_permission_warning),
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }
    }
}

