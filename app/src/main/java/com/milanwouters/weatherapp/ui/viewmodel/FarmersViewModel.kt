package com.milanwouters.weatherapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.milanwouters.weatherapp.domain.model.*
import com.milanwouters.weatherapp.domain.repository.FarmerRepository
import com.milanwouters.weatherapp.domain.usecase.AddFarmerUseCase
import com.milanwouters.weatherapp.util.SeedData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FarmersUiState(
    val farmers: List<Farmer> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val seedDataLoaded: Boolean = false
)

@HiltViewModel
class FarmersViewModel @Inject constructor(
    private val farmerRepository: FarmerRepository,
    private val addFarmerUseCase: AddFarmerUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(FarmersUiState())
    val uiState: StateFlow<FarmersUiState> = _uiState.asStateFlow()

    init {
        loadFarmers()
        loadSeedDataIfEmpty()
    }

    private fun loadFarmers() {
        viewModelScope.launch {
            farmerRepository.getAllFarmers().collect { farmers ->
                _uiState.update { it.copy(farmers = farmers, isLoading = false) }
            }
        }
    }

    private fun loadSeedDataIfEmpty() {
        viewModelScope.launch {
            val count = farmerRepository.getFarmerCount()
            if (count == 0) {
                SeedData.sampleFarmers.forEach { addFarmerUseCase(it) }
                _uiState.update { it.copy(seedDataLoaded = true) }
            }
        }
    }

    fun addFarmer(farmer: Farmer, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = addFarmerUseCase(farmer)
            if (result.isSuccess) {
                onResult(true, "Farmer registered successfully")
            } else {
                onResult(false, result.exceptionOrNull()?.message ?: "Registration failed")
            }
        }
    }

    fun deleteFarmer(farmer: Farmer) {
        viewModelScope.launch {
            farmerRepository.deleteFarmer(farmer)
        }
    }
}
