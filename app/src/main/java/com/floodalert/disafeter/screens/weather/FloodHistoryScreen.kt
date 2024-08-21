package com.floodalert.disafeter.screens.weather

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.floodalert.disafeter.model.FloodData
import com.floodalert.disafeter.model.FloodSummary
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FloodHistoryScreen(viewModel: WeatherViewModel, onNavigateToWeather: () -> Unit) {
    val floodHistory = viewModel.floodHistory
    val floodSummary = viewModel.floodSummary

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, top = 40.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateToWeather) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = null
                )
            }
            Text(
                text = "Flood History",
                style = MaterialTheme.typography.displaySmall
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .padding(end = 8.dp),
            shape = RoundedCornerShape(topEnd = 120.dp, bottomEnd = 120.dp),
            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.primaryContainer)
        ) {
            Box(modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF93FFD8),
                            Color(0xFF7900FF)
                        ),
                        start = Offset(0f, Float.POSITIVE_INFINITY),
                        end = Offset(Float.POSITIVE_INFINITY, 0f)
                    )
                )
            ) {
                Row(modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                ) {
                    LazyColumn(modifier = Modifier
                        .fillMaxHeight()
                        .width(24.dp)
                        .padding(bottom = 8.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        items(4) { index ->
                            Text(
                                text = "${3 - index} FT",
                                fontSize = 12.sp
                            )
                        }
                    }

                    LazyRow(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        items(floodSummary) { data ->
                            FloodSummaryBar(data)
                        }
                    }
                }
            }
        }

        Row(modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.CenterStart
            ) {
                Divider(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(18.dp)
                        .padding(start = 16.dp)
                )
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(floodHistory) { data ->
                        HistoryCard(data)
                    }
                }
            }
        }
    }
}

@Composable
fun FloodSummaryBar(floodSummary: FloodSummary) {
    val averageLevel = floodSummary.averageLevel.toInt()
    val height = if (averageLevel > 0) averageLevel * 56 else 2

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(20.dp),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .height(height.dp)
                .width(12.dp)
                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                .background(MaterialTheme.colorScheme.surface),
        )
        Text(
            text = floodSummary.month,
            fontSize = 8.sp
        )
    }
}

@Composable
fun HistoryCard(floodData: FloodData) {
    val floodLevel = floodData.floodLevel.toInt()
    val recordDate = floodData.timestamp.toDate()
    val dateFormatter = SimpleDateFormat("MMM. dd", Locale.US)
    val timeFormatter = SimpleDateFormat("HH:mm", Locale.US)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(start = 7.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp, 8.dp),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                if (floodLevel > 2) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(12.dp, 8.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = dateFormatter.format(recordDate),
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = timeFormatter.format(recordDate),
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                Divider(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(2.dp))
                        .width(2.dp)
                )

                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(12.dp, 4.dp)
                        .weight(1f, true),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "$floodLevel FT",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = floodLevelToLabel(floodLevel),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onPrimaryContainer)
            )
        }
    }
}

fun floodLevelToLabel(floodLevel: Int): String {
    return when(floodLevel) {
        0 -> "No Flood"
        1 -> "Yellow Warning"
        2 -> "Orange Warning"
        else -> "Critical Level"
    }
}