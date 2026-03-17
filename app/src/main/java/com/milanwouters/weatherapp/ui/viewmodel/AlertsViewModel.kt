package com.milanwouters.weatherapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.milanwouters.weatherapp.domain.model.Farmer
import com.milanwouters.weatherapp.domain.model.WeatherAlert
import com.milanwouters.weatherapp.domain.repository.FarmerRepository
import com.milanwouters.weatherapp.domain.usecase.RefreshWeatherUseCase
import com.milanwouters.weatherapp.domain.usecase.SendAlertsUseCase
import com.milanwouters.weatherapp.util.TanzaniaRegions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AlertsUiState(
    val alerts: List<WeatherAlert> = emptyList(),
    val farmers: List<Farmer> = emptyList(),
    val isLoading: Boolean = false,
    val lastSentCount: Int = 0
)

@HiltViewModel
class AlertsViewModel @Inject constructor(
    private val refreshWeatherUseCase: RefreshWeatherUseCase,
    private val farmerRepository: FarmerRepository,
    private val sendAlertsUseCase: SendAlertsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlertsUiState())
    val uiState: StateFlow<AlertsUiState> = _uiState.asStateFlow()

    init {
        loadAlerts()
        loadFarmers()
    }

    private fun loadAlerts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = refreshWeatherUseCase()
            _uiState.update { it.copy(alerts = result.alerts, isLoading = false) }
        }
    }

    private fun loadFarmers() {
        viewModelScope.launch {
            farmerRepository.getAllFarmers().collect { farmers ->
                _uiState.update { it.copy(farmers = farmers) }
            }
        }
    }

    fun getFarmersForAlert(alert: WeatherAlert): List<Farmer> {
        return _uiState.value.farmers.filter { farmer ->
            if (alert.targetRegion.isEmpty()) true
            else TanzaniaRegions.textMatchesRegion(alert.targetRegion, farmer.region)
        }
    }

    fun sendAllAlerts() {
        viewModelScope.launch {
            val result = sendAlertsUseCase(_uiState.value.alerts)
            _uiState.update { it.copy(lastSentCount = result.farmersReached) }
        }
    }
}
