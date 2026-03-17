package com.milanwouters.weatherapp.domain.usecase

import com.milanwouters.weatherapp.domain.model.WeatherAlert
import com.milanwouters.weatherapp.domain.model.WeatherSourceItem
import com.milanwouters.weatherapp.domain.repository.WeatherRepository
import com.milanwouters.weatherapp.domain.repository.WeatherResult
import javax.inject.Inject

data class WeatherRefreshResult(
    val items: List<WeatherSourceItem>,
    val alerts: List<WeatherAlert>,
    val isLive: Boolean,
    val isCached: Boolean,
    val errorMessage: String? = null,
    val statusLabel: String
)

class RefreshWeatherUseCase @Inject constructor(
    private val weatherRepository: WeatherRepository
) {
    suspend operator fun invoke(): WeatherRefreshResult {
        return when (val result = weatherRepository.fetchLatestForecast()) {
            is WeatherResult.Success -> {
                val alerts = weatherRepository.getAlerts(result.items)
                WeatherRefreshResult(
                    items = result.items,
                    alerts = alerts,
                    isLive = result.isLive,
                    isCached = false,
                    statusLabel = if (result.isLive) "LIVE" else "SUCCESS"
                )
            }
            is WeatherResult.Cached -> {
                val alerts = weatherRepository.getAlerts(result.items)
                WeatherRefreshResult(
                    items = result.items,
                    alerts = alerts,
                    isLive = false,
                    isCached = true,
                    statusLabel = "CACHED"
                )
            }
            is WeatherResult.Error -> {
                val alerts = if (result.cachedItems.isNotEmpty()) {
                    weatherRepository.getAlerts(result.cachedItems)
                } else emptyList()
                WeatherRefreshResult(
                    items = result.cachedItems,
                    alerts = alerts,
                    isLive = false,
                    isCached = result.cachedItems.isNotEmpty(),
                    errorMessage = result.message,
                    statusLabel = "DEMO"
                )
            }
        }
    }
}
