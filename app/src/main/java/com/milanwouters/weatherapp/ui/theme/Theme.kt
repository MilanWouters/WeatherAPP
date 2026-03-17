package com.milanwouters.weatherapp.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = ConsoleGreen,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = ConsoleDarkGreen,
    secondary = ConsoleSecondary,
    background = ConsoleBackground,
    surface = ConsoleSurface,
    onSurface = ConsoleOnSurface,
    error = ConsoleRed
)

@Composable
fun WeatherAPPTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Keep light theme only - sober, functional, low-tech aesthetic
    // Dark mode support can be added later if needed
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = AppTypography,
        content = content
    )
}
