package com.milanwouters.weatherapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
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
import com.milanwouters.weatherapp.domain.model.SmsMessage
import com.milanwouters.weatherapp.ui.components.ScreenHeader
import com.milanwouters.weatherapp.ui.components.SectionCard
import com.milanwouters.weatherapp.ui.theme.*
import com.milanwouters.weatherapp.ui.viewmodel.SmsViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SmsInboxSimulatorScreen(
    navController: NavController,
    viewModel: SmsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    var inputPhone by remember { mutableStateOf("+255712345678") }
    var inputMessage by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Quick command buttons
    val quickCommands = listOf("START", "DAILY", "ALERT", "WEEKLY", "STOP", "STATUS", "HELP",
        "REGION DODOMA", "REGION MWANZA", "CITY IFAKARA", "LANG SW", "LANG EN")

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(
            title = "SMS INBOX SIMULATOR",
            subtitle = "Simulate incoming farmer SMS commands"
        )

        // All messages list
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item {
                SectionCard(title = "SMS LOG (${state.allMessages.size} messages)") {
                    Text(
                        "Tap a quick command or type a custom SMS below.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }

            if (state.allMessages.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No messages yet. Send a command below.", color = Color.Gray)
                    }
                }
            }

            items(state.allMessages) { sms ->
                SmsLogRow(sms = sms)
            }

            item { Spacer(Modifier.height(8.dp)) }
        }

        // Quick commands
        SectionCard(
            title = "QUICK COMMANDS",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                quickCommands.take(7).forEach { cmd ->
                    FilterChip(
                        selected = false,
                        onClick = { inputMessage = cmd },
                        label = { Text(cmd, style = MaterialTheme.typography.labelMedium) }
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                quickCommands.drop(7).forEach { cmd ->
                    FilterChip(
                        selected = false,
                        onClick = { inputMessage = cmd },
                        label = { Text(cmd, style = MaterialTheme.typography.labelMedium) }
                    )
                }
            }
        }

        // Input area
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(ConsoleSurface)
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = inputPhone,
                onValueChange = { inputPhone = it },
                label = { Text("Sender Phone Number") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputMessage,
                    onValueChange = { inputMessage = it },
                    label = { Text("SMS Message") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    placeholder = { Text("e.g. START, DAILY, REGION DODOMA") }
                )
                Button(
                    onClick = {
                        if (inputPhone.isNotBlank() && inputMessage.isNotBlank()) {
                            viewModel.simulateIncomingSms(inputPhone.trim(), inputMessage.trim())
                            inputMessage = ""
                        }
                    },
                    enabled = inputPhone.isNotBlank() && inputMessage.isNotBlank() && !state.isProcessing,
                    colors = ButtonDefaults.buttonColors(containerColor = ConsoleGreen),
                    modifier = Modifier.height(56.dp)
                ) {
                    if (state.isProcessing) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("SEND")
                    }
                }
            }
            if (state.lastReply.isNotEmpty()) {
                Text(
                    "Last reply: ${state.lastReply}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ConsoleGreen
                )
            }
        }
    }
}

@Composable
fun SmsLogRow(sms: SmsMessage) {
    val isInbound = sms.direction == SmsDirection.INBOUND
    val bgColor = if (isInbound) Color(0xFFE8F5E9) else Color(0xFFE3F2FD)
    val alignEnd = !isInbound

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (alignEnd) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .background(bgColor, androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(if (isInbound) "📨" else "📤")
                Text(
                    sms.phoneNumber,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(sms.content, style = MaterialTheme.typography.bodyMedium)
            Text(
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(sms.timestamp)),
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray
            )
        }
    }
}
