package com.milanwouters.weatherapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.milanwouters.weatherapp.navigation.AppNavHost
import com.milanwouters.weatherapp.navigation.Screen
import com.milanwouters.weatherapp.ui.theme.WeatherAPPTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WeatherAPPTheme {
                MainScaffold()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold() {
    val navController = rememberNavController()
    val navItems = listOf(
        Screen.Dashboard,
        Screen.Farmers,
        Screen.WeatherOverview,
        Screen.Alerts,
        Screen.SmsInbox,
        Screen.SmsOutbox,
        Screen.SystemFlow
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                val currentRoute = navController.currentBackStackEntry?.destination?.route
                navItems.forEach { screen ->
                    NavigationBarItem(
                        selected = currentRoute == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Text(
                                text = when (screen) {
                                    Screen.Dashboard -> "📊"
                                    Screen.Farmers -> "👨‍🌾"
                                    Screen.WeatherOverview -> "🌤"
                                    Screen.Alerts -> "⚠️"
                                    Screen.SmsInbox -> "📨"
                                    Screen.SmsOutbox -> "📤"
                                    Screen.SystemFlow -> "🔄"
                                    else -> "•"
                                },
                                style = MaterialTheme.typography.labelLarge
                            )
                        },
                        label = { Text(screen.title, maxLines = 1) }
                    )
                }
            }
        }
    ) { innerPadding ->
        AppNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
