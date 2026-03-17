package com.milanwouters.weatherapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.milanwouters.weatherapp.domain.model.WeatherAlert
import com.milanwouters.weatherapp.domain.model.WeatherSourceItem
import com.milanwouters.weatherapp.domain.usecase.RefreshWeatherUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WeatherUiState(
    val items: List<WeatherSourceItem> = emptyList(),
    val alerts: List<WeatherAlert> = emptyList(),
    val isLoading: Boolean = false,
    val statusLabel: String = "NOT LOADED",
    val isLive: Boolean = false,
    val isCached: Boolean = false,
    val errorMessage: String? = null,
    val lastUpdated: Long? = null,
    val regionFilter: String = "",
    val cityFilter: String = ""
)

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val refreshWeatherUseCase: RefreshWeatherUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, statusLabel = "LOADING") }
            try {
                val result = refreshWeatherUseCase()
                _uiState.update { it.copy(
                    items = result.items,
                    alerts = result.alerts,
                    isLoading = false,
                    statusLabel = result.statusLabel,
                    isLive = result.isLive,
                    isCached = result.isCached,
                    errorMessage = result.errorMessage,
                    lastUpdated = System.currentTimeMillis()
                )}
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    statusLabel = "FAILED",
                    errorMessage = e.message
                )}
            }
        }
    }

    fun setRegionFilter(region: String) {
        _uiState.update { it.copy(regionFilter = region) }
    }

    fun setCityFilter(city: String) {
        _uiState.update { it.copy(cityFilter = city) }
    }

    fun getFilteredItems(): List<WeatherSourceItem> {
        val state = _uiState.value
        return state.items.filter { item ->
            (state.regionFilter.isEmpty() || item.regionText.contains(state.regionFilter, ignoreCase = true)) &&
            (state.cityFilter.isEmpty() || item.cityText.contains(state.cityFilter, ignoreCase = true))
        }
    }
}
