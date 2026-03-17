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
import com.milanwouters.weatherapp.domain.model.WeatherAlert
import com.milanwouters.weatherapp.ui.components.*
import com.milanwouters.weatherapp.ui.theme.*
import com.milanwouters.weatherapp.ui.viewmodel.AlertsViewModel

@Composable
fun AlertsScreen(
    navController: NavController,
    viewModel: AlertsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(
            title = "WEATHER ALERTS",
            subtitle = "${state.alerts.size} active alerts detected"
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (state.lastSentCount > 0) {
                StatusChip(
                    label = "SENT TO ${state.lastSentCount} FARMERS",
                    color = StatusLive
                )
            }
            Spacer(Modifier.weight(1f))
            Button(
                onClick = { viewModel.sendAllAlerts() },
                enabled = state.alerts.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = ConsoleGreen),
                modifier = Modifier.height(36.dp)
            ) {
                Text("SEND ALL ALERTS")
            }
        }

        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ConsoleGreen)
            }
            return@Column
        }

        if (state.alerts.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No active alerts. Refresh weather data to detect alerts.",
                    color = Color.Gray
                )
            }
            return@Column
        }

        LazyColumn(
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.alerts) { alert ->
                val farmersForAlert = viewModel.getFarmersForAlert(alert)
                AlertCard(alert = alert, farmerCount = farmersForAlert.size, farmerNames = farmersForAlert.map { it.name })
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun AlertCard(
    alert: WeatherAlert,
    farmerCount: Int,
    farmerNames: List<String>
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ConsoleSurface),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
        onClick = { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(alert.alertType.icon, style = MaterialTheme.typography.headlineMedium)
                    Column {
                        Text(
                            alert.alertType.displayName.uppercase(),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Source: ${alert.sourceName}",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.Gray
                        )
                    }
                }
                SeverityChip(alert.severity)
            }

            Spacer(Modifier.height(8.dp))

            if (alert.targetRegion.isNotEmpty()) {
                InfoRow("Target Region", alert.targetRegion)
            } else {
                InfoRow("Target", "All Tanzania")
            }
            if (alert.targetCity.isNotEmpty()) {
                InfoRow("Target City", alert.targetCity)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusChip(
                    label = "$farmerCount FARMERS AFFECTED",
                    color = if (farmerCount > 0) ConsoleAmber else Color.Gray
                )
                Text(
                    if (expanded) "▲ collapse" else "▼ details",
                    style = MaterialTheme.typography.labelMedium,
                    color = ConsoleBlue
                )
            }

            if (expanded) {
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = ConsoleDivider)
                Spacer(Modifier.height(8.dp))

                Text("MESSAGE", style = MaterialTheme.typography.labelMedium, color = ConsoleGreen)
                Text(alert.message, style = MaterialTheme.typography.bodyMedium)

                Spacer(Modifier.height(4.dp))
                Text("RECOMMENDATION", style = MaterialTheme.typography.labelMedium, color = ConsoleGreen)
                Text(alert.recommendation, style = MaterialTheme.typography.bodyMedium)

                if (alert.forecastPeriod.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    InfoRow("Period", alert.forecastPeriod)
                }

                if (farmerNames.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text("AFFECTED FARMERS", style = MaterialTheme.typography.labelMedium, color = ConsoleGreen)
                    farmerNames.forEach { name ->
                        Text("• $name", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                if (alert.sourceText.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text("SOURCE TEXT", style = MaterialTheme.typography.labelMedium, color = ConsoleGreen)
                    Text(
                        alert.sourceText.take(200),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.DarkGray
                    )
                }
            }
        }
    }
}
