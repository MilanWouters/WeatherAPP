package com.milanwouters.weatherapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.milanwouters.weatherapp.domain.model.WeatherSourceItem
import com.milanwouters.weatherapp.ui.components.*
import com.milanwouters.weatherapp.ui.theme.*
import com.milanwouters.weatherapp.ui.viewmodel.WeatherViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WeatherOverviewScreen(
    navController: NavController,
    viewModel: WeatherViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val filteredItems = viewModel.getFilteredItems()

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(
            title = "WEATHER OVERVIEW",
            subtitle = "Source: Tanzania Meteorological Authority"
        )

        LazyColumn(
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Status row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val statusColor = when (state.statusLabel) {
                        "LIVE" -> StatusLive
                        "CACHED" -> StatusCached
                        "LOADING" -> StatusLoading
                        else -> StatusFailed
                    }
                    StatusChip(label = state.statusLabel, color = statusColor)
                    if (state.isCached) StatusChip(label = "USING CACHE", color = StatusCached)
                    Spacer(Modifier.weight(1f))
                    Button(
                        onClick = { viewModel.refresh() },
                        enabled = !state.isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = ConsoleGreen),
                        modifier = Modifier.height(36.dp)
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text("REFRESH FORECAST")
                        }
                    }
                }
            }

            // Source info
            item {
                SectionCard(title = "DATA SOURCES") {
                    InfoRow("Primary Source", "Tanzania Meteorological Authority (TMA)")
                    InfoRow("TMA URL", "meteo.go.tz")
                    InfoRow("Fallback", "ICPAC Regional Forecast (icpac.net)")
                    InfoRow("Cache", if (state.isCached) "Active - Using cached data" else "Not in use")
                    state.lastUpdated?.let { ts ->
                        InfoRow("Last Updated", SimpleDateFormat("HH:mm dd/MM/yy", Locale.getDefault()).format(Date(ts)))
                    }
                    if (state.errorMessage != null) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "⚠ ${state.errorMessage}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ConsoleAmber
                        )
                    }
                }
            }

            // Filters
            item {
                SectionCard(title = "FILTER BY LOCATION") {
                    OutlinedTextField(
                        value = state.regionFilter,
                        onValueChange = { viewModel.setRegionFilter(it) },
                        label = { Text("Filter by region") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(Modifier.height(4.dp))
                    OutlinedTextField(
                        value = state.cityFilter,
                        onValueChange = { viewModel.setCityFilter(it) },
                        label = { Text("Filter by city") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }

            // Forecast items
            item {
                Text(
                    "FORECAST ITEMS (${filteredItems.size})",
                    style = MaterialTheme.typography.labelMedium,
                    color = ConsoleGreen,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            if (filteredItems.isEmpty() && !state.isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            if (state.regionFilter.isNotEmpty() || state.cityFilter.isNotEmpty())
                                "No items match the current filter"
                            else "No forecast data. Tap REFRESH FORECAST to load.",
                            color = Color.Gray
                        )
                    }
                }
            }

            items(filteredItems) { item ->
                WeatherSourceCard(item = item)
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun WeatherSourceCard(item: WeatherSourceItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ConsoleSurface),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        item.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${item.sourceName} • ${item.category}",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray
                    )
                }
                SeverityChip(item.severity)
            }
            Spacer(Modifier.height(6.dp))
            if (item.regionText.isNotEmpty()) {
                InfoRow("Regions", item.regionText)
            }
            if (item.dateRange.isNotEmpty()) {
                InfoRow("Period", item.dateRange)
            }
            HorizontalDivider(color = ConsoleDivider, modifier = Modifier.padding(vertical = 4.dp))
            Text(
                item.summary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
