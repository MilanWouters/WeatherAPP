package com.milanwouters.weatherapp.domain.repository

import com.milanwouters.weatherapp.domain.model.WeatherAlert
import com.milanwouters.weatherapp.domain.model.WeatherSourceItem

interface WeatherRepository {
    suspend fun fetchLatestForecast(): WeatherResult
    suspend fun getAlerts(items: List<WeatherSourceItem>): List<WeatherAlert>
    suspend fun getCachedItems(): List<WeatherSourceItem>
}
