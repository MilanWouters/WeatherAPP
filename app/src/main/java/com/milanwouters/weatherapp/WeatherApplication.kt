package com.milanwouters.weatherapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for TZ Weather Alert Console.
 * Hilt entry point for dependency injection.
 */
@HiltAndroidApp
class WeatherApplication : Application()
