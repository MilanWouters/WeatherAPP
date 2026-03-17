package com.milanwouters.weatherapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.milanwouters.weatherapp.ui.components.ScreenHeader
import com.milanwouters.weatherapp.ui.components.SectionCard
import com.milanwouters.weatherapp.ui.theme.*

@Composable
fun SystemFlowScreen(navController: NavController) {
    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(
            title = "SYSTEM FLOW",
            subtitle = "End-to-end data flow visualization"
        )

        LazyColumn(
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            item {
                Text(
                    "This screen shows how data flows through the TZ Weather Alert System from source to farmer.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            // INPUT layer
            item {
                FlowLayerBlock(
                    title = "INPUT",
                    color = ConsoleBlue,
                    items = listOf(
                        FlowItem("🌐", "TMA Website", "Tanzania Meteorological Authority\nmeteo.go.tz - Official source"),
                        FlowItem("🌍", "ICPAC (Fallback)", "Regional forecast fallback\nicpac.net/weekly-forecast"),
                        FlowItem("📨", "Farmer SMS", "Incoming SMS commands\nSTART / DAILY / REGION / STOP"),
                        FlowItem("📋", "Registration", "Farmer name, phone, region, city\nStored in local Room database")
                    )
                )
            }

            item {
                FlowArrow()
            }

            // PROCESSING layer
            item {
                FlowLayerBlock(
                    title = "PROCESSING",
                    color = ConsoleAmber,
                    items = listOf(
                        FlowItem("🔍", "Web Scraping", "OkHttp fetches TMA HTML pages\nJsoup parses HTML structure"),
                        FlowItem("📝", "Text Parsing", "Keyword detection:\nheavy rainfall → HEAVY_RAIN\nflood → FLOOD_RISK\nheat stress → HEAT_STRESS\ndrought → DROUGHT_RISK"),
                        FlowItem("📍", "Region Matching", "Alert regions matched against\nfarmer location (region + city)\nUsing TanzaniaRegions lookup"),
                        FlowItem("⚙", "Alert Engine", "Subscription filter:\nDAILY = all updates\nALERT_ONLY = HIGH+ severity\nWEEKLY = CRITICAL only"),
                        FlowItem("💾", "Caching", "Successful TMA data cached in Room\nServes as fallback when offline")
                    )
                )
            }

            item {
                FlowArrow()
            }

            // OUTPUT layer
            item {
                FlowLayerBlock(
                    title = "OUTPUT",
                    color = ConsoleGreen,
                    items = listOf(
                        FlowItem("📤", "SMS Alerts", "Personalized SMS per farmer\nMatched to region & subscription"),
                        FlowItem("📊", "Dashboard", "Real-time console view\nFarmer counts, alert status"),
                        FlowItem("📋", "Logs", "Full SMS inbox/outbox history\nAlert audit trail")
                    )
                )
            }

            item { Spacer(Modifier.height(12.dp)) }

            // ITF pillars
            item {
                SectionCard(title = "ITF TECHNOLOGY PILLARS IMPLEMENTED") {
                    ITFPillar("Frontend / UI", "Jetpack Compose + Material 3\nMVVM pattern with StateFlow")
                    ITFPillar("Business Logic", "AlertParser, SendAlertsUseCase\nSmsCommandParser, Region matching")
                    ITFPillar("Data Layer", "Room (SQLite) + Repositories\nOkHttp + Jsoup scraping + Cache")
                    ITFPillar("Architecture", "MVVM + Clean Architecture\nUse cases, domain/data separation")
                    ITFPillar("Async / State", "Kotlin Coroutines + StateFlow\nViewModelScope for lifecycle")
                    ITFPillar("Dependency Injection", "Hilt (Dagger)\nSingleton-scoped services")
                    ITFPillar("Navigation", "Navigation Compose\n8-screen bottom nav")
                }
            }

            // Real-world constraints
            item {
                SectionCard(title = "REAL-WORLD DESIGN CONSTRAINTS") {
                    ConstraintRow("📱", "Old smartphones", "minSdk 24, system fonts, no heavy animations")
                    ConstraintRow("🌐", "Poor internet", "Aggressive timeout (15s), fallback cache, offline mode")
                    ConstraintRow("⚡", "Limited power", "Lightweight UI, minimal background work")
                    ConstraintRow("💾", "Low storage", "Room database, no media/image caching")
                    ConstraintRow("📡", "SMS delivery", "Messages <160 chars, ASCII-safe characters")
                    ConstraintRow("🌍", "Local language", "EN + SW support, Swahili can be extended")
                    ConstraintRow("💰", "Low cost", "Africa's Talking SMS API recommended for production")
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

data class FlowItem(val icon: String, val label: String, val description: String)

@Composable
fun FlowLayerBlock(title: String, color: Color, items: List<FlowItem>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, color, RoundedCornerShape(4.dp))
            .padding(12.dp)
    ) {
        Text(
            text = "[ $title ]",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        items.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(item.icon, style = MaterialTheme.typography.bodyLarge)
                Column {
                    Text(item.label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Text(item.description, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun FlowArrow() {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "▼",
            style = MaterialTheme.typography.headlineLarge,
            color = ConsoleDivider,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ITFPillar(title: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "✓",
            color = ConsoleGreen,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium
        )
        Column {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Text(description, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
        }
    }
}

@Composable
fun ConstraintRow(icon: String, label: String, solution: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(icon)
        Column {
            Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Text(solution, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
        }
    }
}
