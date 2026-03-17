package com.milanwouters.weatherapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.milanwouters.weatherapp.domain.model.*
import com.milanwouters.weatherapp.ui.components.*
import com.milanwouters.weatherapp.ui.theme.*
import com.milanwouters.weatherapp.ui.viewmodel.FarmersViewModel
import com.milanwouters.weatherapp.util.TanzaniaRegions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmerFormScreen(
    navController: NavController,
    viewModel: FarmersViewModel = hiltViewModel()
) {
    var name by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var selectedLanguage by remember { mutableStateOf(Language.ENGLISH) }
    var selectedSubscription by remember { mutableStateOf(SubscriptionType.DAILY) }
    var selectedRegion by remember { mutableStateOf("") }
    var selectedCity by remember { mutableStateOf("") }

    var showRegionDropdown by remember { mutableStateOf(false) }
    var showCityDropdown by remember { mutableStateOf(false) }
    var showLangDropdown by remember { mutableStateOf(false) }
    var showSubDropdown by remember { mutableStateOf(false) }

    var snackbarMessage by remember { mutableStateOf("") }
    var showSnackbar by remember { mutableStateOf(false) }

    val isFormValid = name.isNotBlank() &&
            phoneNumber.isNotBlank() &&
            selectedRegion.isNotEmpty() &&
            selectedCity.isNotEmpty()

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ADD FARMER", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ConsoleGreen,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        snackbarHost = {
            if (showSnackbar) {
                Snackbar(
                    modifier = Modifier.padding(8.dp),
                    action = {
                        TextButton(onClick = { showSnackbar = false }) { Text("OK") }
                    }
                ) {
                    Text(snackbarMessage)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Register a new farmer for TMA weather SMS alerts.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            // Name field
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Phone number field
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text("Phone Number * (e.g. +255712345678)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Language dropdown
            ExposedDropdownMenuBox(
                expanded = showLangDropdown,
                onExpandedChange = { showLangDropdown = it }
            ) {
                OutlinedTextField(
                    value = selectedLanguage.displayName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Language *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showLangDropdown) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = showLangDropdown,
                    onDismissRequest = { showLangDropdown = false }
                ) {
                    Language.entries.forEach { lang ->
                        DropdownMenuItem(
                            text = { Text(lang.displayName) },
                            onClick = { selectedLanguage = lang; showLangDropdown = false }
                        )
                    }
                }
            }

            // Subscription type dropdown
            ExposedDropdownMenuBox(
                expanded = showSubDropdown,
                onExpandedChange = { showSubDropdown = it }
            ) {
                OutlinedTextField(
                    value = selectedSubscription.displayName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Subscription Type *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showSubDropdown) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = showSubDropdown,
                    onDismissRequest = { showSubDropdown = false }
                ) {
                    SubscriptionType.entries.forEach { type ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(type.displayName, fontWeight = FontWeight.Medium)
                                    Text(type.description, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                                }
                            },
                            onClick = { selectedSubscription = type; showSubDropdown = false }
                        )
                    }
                }
            }

            // Region dropdown (required)
            Text(
                "Location (Required for weather matching)",
                style = MaterialTheme.typography.labelMedium,
                color = ConsoleGreen,
                fontWeight = FontWeight.Bold
            )

            ExposedDropdownMenuBox(
                expanded = showRegionDropdown,
                onExpandedChange = { showRegionDropdown = it }
            ) {
                OutlinedTextField(
                    value = selectedRegion.ifEmpty { "Select Region *" },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Region *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showRegionDropdown) },
                    isError = selectedRegion.isEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = showRegionDropdown,
                    onDismissRequest = { showRegionDropdown = false }
                ) {
                    TanzaniaRegions.regions.forEach { region ->
                        DropdownMenuItem(
                            text = { Text(region) },
                            onClick = {
                                selectedRegion = region
                                selectedCity = "" // Reset city when region changes
                                showRegionDropdown = false
                            }
                        )
                    }
                }
            }

            // City dropdown (depends on region selection)
            ExposedDropdownMenuBox(
                expanded = showCityDropdown && selectedRegion.isNotEmpty(),
                onExpandedChange = { if (selectedRegion.isNotEmpty()) showCityDropdown = it }
            ) {
                OutlinedTextField(
                    value = selectedCity.ifEmpty { if (selectedRegion.isEmpty()) "Select region first" else "Select City *" },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Nearest City *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCityDropdown) },
                    isError = selectedCity.isEmpty() && selectedRegion.isNotEmpty(),
                    enabled = selectedRegion.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = showCityDropdown && selectedRegion.isNotEmpty(),
                    onDismissRequest = { showCityDropdown = false }
                ) {
                    TanzaniaRegions.getCitiesForRegion(selectedRegion).forEach { city ->
                        DropdownMenuItem(
                            text = { Text(city) },
                            onClick = { selectedCity = city; showCityDropdown = false }
                        )
                    }
                }
            }

            if (selectedRegion.isNotEmpty() && selectedCity.isEmpty()) {
                Text(
                    "⚠ Please select a city to complete registration",
                    style = MaterialTheme.typography.labelMedium,
                    color = ConsoleAmber
                )
            }

            Spacer(Modifier.height(8.dp))

            // Submit button - only enabled when form is complete
            Button(
                onClick = {
                    val farmer = Farmer(
                        name = name.trim(),
                        phoneNumber = phoneNumber.trim(),
                        language = selectedLanguage,
                        subscriptionType = selectedSubscription,
                        region = selectedRegion,
                        city = selectedCity
                    )
                    viewModel.addFarmer(farmer) { success, message ->
                        snackbarMessage = message
                        showSnackbar = true
                        if (success) {
                            navController.navigateUp()
                        }
                    }
                },
                enabled = isFormValid,
                colors = ButtonDefaults.buttonColors(containerColor = ConsoleGreen),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    "REGISTER FARMER",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            if (!isFormValid) {
                Text(
                    "All fields including region and city are required",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray
                )
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}
