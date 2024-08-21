package com.floodalert.disafeter.screens.bulletin

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.floodalert.disafeter.MainViewModel
import com.floodalert.disafeter.R
import com.floodalert.disafeter.model.Bulletin

sealed class Severity(val severity: String, val color: List<Color>) {
    data object High: Severity("high", listOf(Color(0xFFFAD4D4), Color(0xFFFFF2F2)))
    data object Medium: Severity("medium", listOf(Color(0xFFFFD8A9), Color(0xFFFDEEDC)))
    data object Low: Severity("medium", listOf(Color(0xFFB8DFD8), Color(0xFFE8F6EF)))
}

@Composable
fun BulletinScreen(
    viewModel: BulletinViewModel,
    mainViewModel: MainViewModel,
    onNavigateToForm: () -> Unit
) {
    var isAdmin by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(mainViewModel.currentUser) {
        if (mainViewModel.currentUser != null) {
            isAdmin = mainViewModel.currentUser!!.admin
        }
    }

    LaunchedEffect(Unit) {
        if (mainViewModel.currentUser != null) {
            mainViewModel.hideActionButton = !mainViewModel.currentUser!!.admin
        }
        mainViewModel.hideNavbar = false
        viewModel.setSelectedBulletin(null)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Bulletin",
            style = MaterialTheme.typography.displaySmall,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, bottom = 16.dp),
        )

        if (viewModel.announcements.isNotEmpty()) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(viewModel.announcements) {
                    BulletinCard(bulletin = it, isAdmin = isAdmin) {
                        viewModel.setSelectedBulletin(it)
                        onNavigateToForm()
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_announcement),
                    contentDescription = null,
                    modifier = Modifier.width(300.dp)
                )
                Text(
                    text = "No Announcement Yet",
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }
    }
}

@Composable
fun BulletinCard(
    bulletin: Bulletin,
    isAdmin: Boolean,
    onSelect: () -> Unit
) {
    var colorSeverity by remember { mutableStateOf<Severity>(Severity.Low) }

    LaunchedEffect(bulletin) {
        colorSeverity = when (bulletin.severity) {
            "high" -> Severity.High
            "medium" -> Severity.Medium
            else -> Severity.Low
        }
    }

    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp, 8.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.linearGradient(
                    colors = colorSeverity.color,
                    start = Offset(0f, Float.POSITIVE_INFINITY),
                    end = Offset(Float.POSITIVE_INFINITY, 0f)
                )
            )
        ) {
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = bulletin.title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "Subject",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
            ) {
                Text(
                    text = bulletin.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            if (isAdmin) {
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onSelect) {
                        Text(text = "Edit")
                    }
                }
            }
        }
    }
}