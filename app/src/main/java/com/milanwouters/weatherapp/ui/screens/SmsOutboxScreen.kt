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
import com.milanwouters.weatherapp.domain.model.SmsMessage
import com.milanwouters.weatherapp.ui.components.*
import com.milanwouters.weatherapp.ui.theme.*
import com.milanwouters.weatherapp.ui.viewmodel.SmsViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SmsOutboxScreen(
    navController: NavController,
    viewModel: SmsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val outbound = state.outboundMessages

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(
            title = "SMS OUTBOX",
            subtitle = "${outbound.size} messages sent via FakeSmsGateway"
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusChip(label = "GATEWAY: FakeSmsGateway", color = StatusCached)
            StatusChip(label = "SIMULATED", color = ConsoleAmber)
        }

        if (outbound.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📤", style = MaterialTheme.typography.headlineLarge)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "No outbound SMS yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Text(
                        "Trigger alerts or simulate incoming SMS to generate outbound messages.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
            return@Column
        }

        LazyColumn(
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(outbound) { sms ->
                OutboundSmsCard(sms = sms)
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun OutboundSmsCard(sms: SmsMessage) {
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
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📤", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            sms.phoneNumber,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (sms.farmerName.isNotEmpty()) {
                        Text(
                            sms.farmerName,
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.Gray
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(sms.timestamp)),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray
                    )
                    Text(
                        SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(Date(sms.timestamp)),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray
                    )
                    if (sms.alertType.isNotEmpty()) {
                        StatusChip(
                            label = sms.alertType,
                            color = ConsoleAmber
                        )
                    }
                }
            }
            HorizontalDivider(color = ConsoleDivider, modifier = Modifier.padding(vertical = 6.dp))
            Text(
                sms.content,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
