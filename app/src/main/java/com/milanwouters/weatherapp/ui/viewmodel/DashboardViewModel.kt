package com.milanwouters.weatherapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.milanwouters.weatherapp.domain.model.*
import com.milanwouters.weatherapp.domain.repository.FarmerRepository
import com.milanwouters.weatherapp.domain.repository.SmsRepository
import com.milanwouters.weatherapp.domain.usecase.RefreshWeatherUseCase
import com.milanwouters.weatherapp.domain.usecase.SendAlertsUseCase
import com.milanwouters.weatherapp.domain.usecase.WeatherRefreshResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val totalFarmers: Int = 0,
    val farmersByRegion: Map<String, Int> = emptyMap(),
    val activeAlerts: List<WeatherAlert> = emptyList(),
    val recentSms: List<SmsMessage> = emptyList(),
    val scrapeStatus: String = "NOT LOADED",
    val lastForecastPeriod: String = "-",
    val isRefreshing: Boolean = false,
    val farmersReachedByAlerts: Int = 0
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val farmerRepository: FarmerRepository,
    private val smsRepository: SmsRepository,
    private val refreshWeatherUseCase: RefreshWeatherUseCase,
    private val sendAlertsUseCase: SendAlertsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadFarmers()
        loadRecentSms()
        refreshWeather()
    }

    private fun loadFarmers() {
        viewModelScope.launch {
            farmerRepository.getAllFarmers().collect { farmers ->
                val byRegion = farmers.groupBy { it.region }.mapValues { it.value.size }
                _uiState.update { it.copy(
                    totalFarmers = farmers.size,
                    farmersByRegion = byRegion
                )}
            }
        }
    }

    private fun loadRecentSms() {
        viewModelScope.launch {
            smsRepository.getAllMessages().collect { messages ->
                _uiState.update { it.copy(recentSms = messages.take(10)) }
            }
        }
    }

    fun refreshWeather() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, scrapeStatus = "LOADING") }
            try {
                val result = refreshWeatherUseCase()
                val farmersReached = if (result.alerts.isNotEmpty()) {
                    val sendResult = sendAlertsUseCase(result.alerts)
                    sendResult.farmersReached
                } else 0

                _uiState.update { it.copy(
                    activeAlerts = result.alerts,
                    scrapeStatus = result.statusLabel,
                    lastForecastPeriod = result.items.firstOrNull()?.dateRange?.ifBlank { "Latest" } ?: "Latest",
                    isRefreshing = false,
                    farmersReachedByAlerts = farmersReached
                )}
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isRefreshing = false,
                    scrapeStatus = "ERROR: ${e.message?.take(30)}"
                )}
            }
        }
    }
}
