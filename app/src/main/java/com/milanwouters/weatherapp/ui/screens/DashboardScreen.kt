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
import com.milanwouters.weatherapp.domain.model.SmsDirection
import com.milanwouters.weatherapp.navigation.Screen
import com.milanwouters.weatherapp.ui.components.*
import com.milanwouters.weatherapp.ui.theme.*
import com.milanwouters.weatherapp.ui.viewmodel.DashboardViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            ScreenHeader(
                title = "TZ WEATHER ALERT CONSOLE",
                subtitle = "Tanzania Agricultural SMS Alert System"
            )
        }

        item {
            // Top status bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val scrapeColor = when {
                    state.scrapeStatus.startsWith("LIVE") -> StatusLive
                    state.scrapeStatus.startsWith("CACHED") -> StatusCached
                    state.scrapeStatus.startsWith("LOADING") -> StatusLoading
                    else -> StatusFailed
                }
                StatusChip(label = "TMA: ${state.scrapeStatus}", color = scrapeColor)
                Spacer(Modifier.weight(1f))
                if (state.isRefreshing) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                }
                Button(
                    onClick = { viewModel.refreshWeather() },
                    enabled = !state.isRefreshing,
                    colors = ButtonDefaults.buttonColors(containerColor = ConsoleGreen),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("REFRESH", style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        // Farmer stats
        item {
            SectionCard(
                title = "FARMER REGISTRY",
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatBlock("Total Farmers", state.totalFarmers.toString())
                    StatBlock("Active Alerts", state.activeAlerts.size.toString())
                    StatBlock("Farmers Alerted", state.farmersReachedByAlerts.toString())
                }
            }
        }

        // Region distribution
        if (state.farmersByRegion.isNotEmpty()) {
            item {
                SectionCard(
                    title = "FARMERS BY REGION",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    state.farmersByRegion.entries
                        .sortedByDescending { it.value }
                        .take(8)
                        .forEach { (region, count) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(region, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    "$count farmer${if (count != 1) "s" else ""}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = ConsoleGreen
                                )
                            }
                        }
                }
            }
        }

        // Active alerts
        if (state.activeAlerts.isNotEmpty()) {
            item {
                SectionCard(
                    title = "ACTIVE WEATHER ALERTS",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    state.activeAlerts.take(5).forEach { alert ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "${alert.alertType.icon} ${alert.alertType.displayName}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                if (alert.targetRegion.isNotEmpty()) {
                                    Text(
                                        alert.targetRegion,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.Gray
                                    )
                                }
                            }
                            SeverityChip(alert.severity)
                        }
                        HorizontalDivider(color = ConsoleDivider, modifier = Modifier.padding(vertical = 2.dp))
                    }
                    if (state.activeAlerts.size > 5) {
                        TextButton(onClick = { navController.navigate(Screen.Alerts.route) }) {
                            Text("View all ${state.activeAlerts.size} alerts →")
                        }
                    }
                }
            }
        }

        // Forecast info
        item {
            SectionCard(
                title = "LAST FORECAST",
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                InfoRow("Source", "Tanzania Meteorological Authority")
                InfoRow("Period", state.lastForecastPeriod.ifBlank { "Latest available" })
                InfoRow("Data Status", state.scrapeStatus)
            }
        }

        // Recent SMS
        if (state.recentSms.isNotEmpty()) {
            item {
                SectionCard(
                    title = "RECENT SMS ACTIVITY",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    state.recentSms.take(5).forEach { sms ->
                        val isOut = sms.direction == SmsDirection.OUTBOUND
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(if (isOut) "📤" else "📨")
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    sms.phoneNumber,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.Gray
                                )
                                Text(
                                    sms.content.take(60),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        HorizontalDivider(color = ConsoleDivider)
                    }
                }
            }
        }

        // Dataflow summary
        item {
            SectionCard(
                title = "DATAFLOW OVERVIEW",
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("INPUT", style = MaterialTheme.typography.labelMedium, color = ConsoleBlue)
                Text("→ TMA forecast data scraped from meteo.go.tz", style = MaterialTheme.typography.bodyMedium)
                Text("→ Incoming SMS commands from farmers", style = MaterialTheme.typography.bodyMedium)
                Text("→ Farmer region/city preferences", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(4.dp))
                Text("PROCESSING", style = MaterialTheme.typography.labelMedium, color = ConsoleAmber)
                Text("→ HTML parsing + keyword detection", style = MaterialTheme.typography.bodyMedium)
                Text("→ Region matching algorithm", style = MaterialTheme.typography.bodyMedium)
                Text("→ Alert type classification", style = MaterialTheme.typography.bodyMedium)
                Text("→ Subscription filter (DAILY/ALERT/WEEKLY)", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(4.dp))
                Text("OUTPUT", style = MaterialTheme.typography.labelMedium, color = ConsoleGreen)
                Text("→ Targeted SMS alerts to registered farmers", style = MaterialTheme.typography.bodyMedium)
                Text("→ ${state.farmersReachedByAlerts} farmers reached in last cycle", style = MaterialTheme.typography.bodyMedium)
            }
        }

        item { Spacer(Modifier.height(80.dp)) }
    }
}
