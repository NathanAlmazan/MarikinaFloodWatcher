package com.floodalert.disafeter.screens.emergency

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import com.floodalert.disafeter.MainViewModel
import com.floodalert.disafeter.R
import com.floodalert.disafeter.model.EmergencyDirectory
import com.floodalert.disafeter.screens.evacuate.LocationAlertDialog

sealed class DirectoryIcon(val value: String, @DrawableRes val icon: Int) {
    data object Mobile : DirectoryIcon("mobile", R.drawable.ic_phone)
    data object Telephone : DirectoryIcon("telephone", R.drawable.ic_phone_classic)
    data object Email : DirectoryIcon("email", R.drawable.ic_email)
}

@Composable
fun EmergencyScreen(
    viewModel: EmergencyViewModel,
    mainViewModel: MainViewModel,
    onNavigateToDirectory: () -> Unit
) {
    var isAdmin by rememberSaveable { mutableStateOf(false) }
    var selectedDirectory by rememberSaveable { mutableStateOf<EmergencyDirectory?>(null) }
    var isDenied by rememberSaveable { mutableStateOf(false) }
    var showDialog by rememberSaveable { mutableStateOf(false) }
    val userLocation = mainViewModel.userLocation

    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) isDenied = true
    }

    val onSendLocation = {
        if (userLocation.latitude > 0 && userLocation.longitude > 0) {
            val directory = viewModel.barangayDirectories.filter { it.type == "mobile" }
            val contact = directory[0].contact

            val smsUri = Uri.parse("smsto:$contact")
            val intent = Intent(Intent.ACTION_SENDTO, smsUri)
            intent.putExtra(
                "sms_body",
                "Help!! We need your immediate assistance. Our location is at ${userLocation.latitude} latitude and " +
                        "${userLocation.longitude} longitude. Thank you."
            )

            startActivity(context, intent, null)
        } else {
            showDialog = true
        }
    }

    LaunchedEffect(Unit) {
        if (mainViewModel.currentUser != null) {
            mainViewModel.hideActionButton = !mainViewModel.currentUser!!.admin
        }
        mainViewModel.hideNavbar = false
        viewModel.selectedDirectory = null
    }

    LaunchedEffect(mainViewModel.currentUser) {
        if (mainViewModel.currentUser != null) {
            isAdmin = mainViewModel.currentUser!!.admin
        }
    }

    if (selectedDirectory != null) {
        CallPhoneAlertDialog(
            name = selectedDirectory!!.name,
            onCallDirectory = {
                when (PackageManager.PERMISSION_GRANTED) {
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.CALL_PHONE
                    ) -> {
                        val callIntent = Intent(Intent.ACTION_CALL)
                        callIntent.data = Uri.parse("tel:" + selectedDirectory!!.contact)
                        startActivity(context, callIntent, null)
                    }

                    else -> {
                        launcher.launch(Manifest.permission.CALL_PHONE)
                    }
                }
            },
            onCloseDialog = { selectedDirectory = null }
        )
    }

    if (isDenied) {
        CallPermissionDeniedDialog(
            onCloseDialog = { isDenied = false }
        )
    }

    if (showDialog) {
        LocationAlertDialog(
            onRequestLocation = { mainViewModel.requestUserLocation() },
            onCloseDialog = { showDialog = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Text(
            text = "Emergency Directories",
            style = MaterialTheme.typography.displaySmall,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, bottom = 16.dp),
        )

        DirectoryCard(
            label = "Barangay Directories",
            colors = listOf(
                Color(0xFFBFACE0),
                Color(0xFF645CAA)
            ),
            icon = R.drawable.ic_home_city,
            directories = viewModel.barangayDirectories,
            onNavigateToDirectory = {
                viewModel.selectedDirectory = it
                onNavigateToDirectory()
            },
            onPhoneCall = { selectedDirectory = it },
            isAdmin = isAdmin
        )
        DirectoryCard(
            label = "Medical Directories",
            colors = listOf(
                Color(0xFFFFE898),
                Color(0xFFF65A83)
            ),
            icon = R.drawable.ic_medical_bag,
            directories = viewModel.medicalDirectories,
            onNavigateToDirectory = {
                viewModel.selectedDirectory = it
                onNavigateToDirectory()
            },
            onPhoneCall = { selectedDirectory = it },
            isAdmin = isAdmin
        )
        DirectoryCard(
            label = "Municipal Directories",
            colors = listOf(
                Color(0xFF64C9CF),
                Color(0xFF035397)
            ),
            icon = R.drawable.ic_caloocan_city,
            directories = viewModel.municipalDirectories,
            onNavigateToDirectory = {
                viewModel.selectedDirectory = it
                onNavigateToDirectory()
            },
            onPhoneCall = { selectedDirectory = it },
            isAdmin = isAdmin
        )
        DirectoryCard(
            label = "NDRRMC",
            colors = listOf(
                Color(0xFF4CACBC),
                Color(0xFFA0D995)
            ),
            icon = R.drawable.ic_ndrrmc_logo,
            directories = viewModel.nationalDirectories,
            onNavigateToDirectory = {
                viewModel.selectedDirectory = it
                onNavigateToDirectory()
            },
            onPhoneCall = { selectedDirectory = it },
            isAdmin = isAdmin
        )
        DirectoryCard(
            label = "Send Location",
            colors = listOf(
                Color(0xFF355764),
                Color(0xFF81CACF)
            ),
            icon = R.drawable.ic_baseline_send_24,
            directories = viewModel.nationalDirectories,
            onNavigateToDirectory = {
                viewModel.selectedDirectory = it
                onNavigateToDirectory()
            },
            onPhoneCall = { selectedDirectory = it },
            onSendLocation = onSendLocation,
            isAdmin = isAdmin,
            maxHeight = 250
        )
    }
}

@Composable
fun DirectoryCard(
    label: String,
    colors: List<Color>,
    @DrawableRes icon: Int,
    directories: List<EmergencyDirectory>,
    onNavigateToDirectory: (EmergencyDirectory) -> Unit,
    onPhoneCall: (EmergencyDirectory) -> Unit,
    onSendLocation: (() -> Unit)? = null,
    maxHeight: Int = 370,
    isAdmin: Boolean = false
) {
    var expanded by remember { mutableStateOf(false) }

    val extraPadding by animateDpAsState(
        if (expanded) maxHeight.dp else 120.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ), label = "directory card"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(extraPadding.coerceAtLeast(120.dp))
            .padding(16.dp, 12.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(12.dp)
    ) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .height(120.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = colors,
                        start = Offset(0f, Float.POSITIVE_INFINITY),
                        end = Offset(Float.POSITIVE_INFINITY, 0f)
                    )
                )
        ) {
            val (title, image, button) = createRefs()

            Image(
                painter = painterResource(id = icon),
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                alpha = 0.3f,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .height(200.dp)
                    .width(200.dp)
                    .constrainAs(image) {
                        top.linkTo(parent.top)
                        end.linkTo(parent.end)
                    }
            )

            Row(
                modifier = Modifier
                    .padding(12.dp)
                    .constrainAs(title) {
                        start.linkTo(parent.start)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    },
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .height(40.dp)
                        .width(40.dp)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.width(180.dp)
                )
            }

            IconButton(
                onClick = { expanded = !expanded },
                modifier = Modifier
                    .padding(12.dp)
                    .constrainAs(button) {
                        top.linkTo(parent.top)
                        end.linkTo(parent.end)
                        bottom.linkTo(parent.bottom)
                    }
            ) {
                if (expanded) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_keyboard_arrow_down_24),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.surface
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_keyboard_arrow_up_24),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.surface
                    )
                }
            }
        }

        if (onSendLocation != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                LocationInfo(
                    onSendLocation = onSendLocation
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                items(directories) { directory ->
                    DirectoryInfo(
                        directory = directory,
                        isAdmin = isAdmin,
                        onIconClick = { onNavigateToDirectory(directory) },
                        onPhoneCall = { onPhoneCall(directory) }
                    )
                }
            }
        }
    }
}

@Composable
fun LocationInfo(onSendLocation: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_baseline_location_on_24),
            contentDescription = null
        )
        Column(modifier = Modifier.weight(1f, true)) {
            Text(
                text = "Send my location",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Send your location via SMS to rescuers as an SOS signal",
                style = MaterialTheme.typography.labelMedium
            )
        }
        IconButton(
            onClick = onSendLocation,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_baseline_send_24),
                contentDescription = null,
                modifier = Modifier
                    .width(15.dp)
                    .height(15.dp)
            )
        }
    }
}

@Composable
fun DirectoryInfo(
    directory: EmergencyDirectory,
    isAdmin: Boolean,
    onIconClick: () -> Unit,
    onPhoneCall: () -> Unit
) {
    val directoryIcon = remember {
        mutableStateOf<DirectoryIcon>(DirectoryIcon.Mobile)
    }

    LaunchedEffect(directory.type) {
        when (directory.type) {
            "telephone" -> directoryIcon.value = DirectoryIcon.Telephone
            "email" -> directoryIcon.value = DirectoryIcon.Email
            else -> directoryIcon.value = DirectoryIcon.Mobile
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            painter = painterResource(id = directoryIcon.value.icon),
            contentDescription = null
        )
        Column(modifier = Modifier.weight(1f, true)) {
            Text(
                text = directory.contact,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = directory.name,
                style = MaterialTheme.typography.labelMedium
            )
        }
        IconButton(
            onClick = if (isAdmin) onIconClick else onPhoneCall,
            enabled = isAdmin || directory.type != "email"
        ) {
            Icon(
                painter = if (isAdmin) painterResource(R.drawable.ic_baseline_edit_24) else painterResource(
                    R.drawable.ic_baseline_arrow_forward_ios_24
                ),
                contentDescription = null,
                modifier = Modifier
                    .width(15.dp)
                    .height(15.dp)
            )
        }
    }
}