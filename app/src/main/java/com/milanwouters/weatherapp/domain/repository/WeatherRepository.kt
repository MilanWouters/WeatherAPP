package com.milanwouters.weatherapp.domain.repository

import com.milanwouters.weatherapp.domain.model.WeatherAlert
import com.milanwouters.weatherapp.domain.model.WeatherSourceItem

sealed class WeatherResult {
    data class Success(val items: List<WeatherSourceItem>, val isLive: Boolean) : WeatherResult()
    data class Cached(val items: List<WeatherSourceItem>) : WeatherResult()
    data class Error(val message: String, val cachedItems: List<WeatherSourceItem> = emptyList()) : WeatherResult()
}

interface WeatherRepository {
    suspend fun fetchLatestForecast(): WeatherResult
    suspend fun getAlerts(items: List<WeatherSourceItem>): List<WeatherAlert>
    suspend fun getCachedItems(): List<WeatherSourceItem>
}
