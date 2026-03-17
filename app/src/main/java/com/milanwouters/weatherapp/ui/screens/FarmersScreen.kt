package com.milanwouters.weatherapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.milanwouters.weatherapp.domain.model.Farmer
import com.milanwouters.weatherapp.navigation.Screen
import com.milanwouters.weatherapp.ui.components.*
import com.milanwouters.weatherapp.ui.theme.*
import com.milanwouters.weatherapp.ui.viewmodel.FarmersViewModel

@Composable
fun FarmersScreen(
    navController: NavController,
    viewModel: FarmersViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(
            title = "FARMER REGISTRY",
            subtitle = "${state.farmers.size} registered farmers"
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (state.seedDataLoaded) {
                StatusChip(label = "SEED DATA LOADED", color = StatusLive)
            }
            Spacer(Modifier.weight(1f))
            Button(
                onClick = { navController.navigate(Screen.FarmerForm.route) },
                colors = ButtonDefaults.buttonColors(containerColor = ConsoleGreen)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("ADD FARMER")
            }
        }

        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ConsoleGreen)
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(state.farmers, key = { it.id }) { farmer ->
                FarmerCard(farmer = farmer, onDelete = { viewModel.deleteFarmer(farmer) })
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun FarmerCard(farmer: Farmer, onDelete: () -> Unit) {
    var showDelete by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ConsoleSurface),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("👨‍🌾", style = MaterialTheme.typography.headlineSmall)
                    Text(
                        farmer.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (!farmer.isActive) {
                        StatusChip(label = "INACTIVE", color = Color.Gray)
                    }
                }
                Spacer(Modifier.height(4.dp))
                InfoRow("Phone", farmer.phoneNumber)
                InfoRow("Region", "${farmer.region} → ${farmer.city}")
                InfoRow("Subscription", farmer.subscriptionType.displayName)
                InfoRow("Language", farmer.language.displayName)
            }
            IconButton(onClick = { showDelete = true }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray)
            }
        }
    }

    if (showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            title = { Text("Remove Farmer") },
            text = { Text("Remove ${farmer.name} from the registry?") },
            confirmButton = {
                TextButton(onClick = { onDelete(); showDelete = false }) {
                    Text("Remove", color = ConsoleRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDelete = false }) { Text("Cancel") }
            }
        )
    }
}
