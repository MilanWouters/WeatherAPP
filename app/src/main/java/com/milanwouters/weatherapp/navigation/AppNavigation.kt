package com.milanwouters.weatherapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.milanwouters.weatherapp.ui.screens.*

sealed class Screen(val route: String, val title: String) {
    object Dashboard : Screen("dashboard", "Dashboard")
    object Farmers : Screen("farmers", "Farmers")
    object FarmerForm : Screen("farmer_form", "Add Farmer")
    object SmsInbox : Screen("sms_inbox", "SMS Inbox Simulator")
    object SmsOutbox : Screen("sms_outbox", "SMS Outbox")
    object WeatherOverview : Screen("weather_overview", "Weather Overview")
    object Alerts : Screen("alerts", "Alerts")
    object SystemFlow : Screen("system_flow", "System Flow")
}

@Composable
fun AppNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        modifier = modifier
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(navController = navController)
        }
        composable(Screen.Farmers.route) {
            FarmersScreen(navController = navController)
        }
        composable(Screen.FarmerForm.route) {
            FarmerFormScreen(navController = navController)
        }
        composable(Screen.SmsInbox.route) {
            SmsInboxSimulatorScreen(navController = navController)
        }
        composable(Screen.SmsOutbox.route) {
            SmsOutboxScreen(navController = navController)
        }
        composable(Screen.WeatherOverview.route) {
            WeatherOverviewScreen(navController = navController)
        }
        composable(Screen.Alerts.route) {
            AlertsScreen(navController = navController)
        }
        composable(Screen.SystemFlow.route) {
            SystemFlowScreen(navController = navController)
        }
    }
}
