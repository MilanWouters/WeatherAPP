package com.milanwouters.weatherapp.domain.usecase

import com.milanwouters.weatherapp.domain.model.Farmer
import com.milanwouters.weatherapp.domain.repository.FarmerRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFarmersUseCase @Inject constructor(
    private val farmerRepository: FarmerRepository
) {
    operator fun invoke(): Flow<List<Farmer>> = farmerRepository.getAllFarmers()
}
