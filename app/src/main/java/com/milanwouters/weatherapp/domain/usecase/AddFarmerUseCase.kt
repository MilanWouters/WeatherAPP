package com.milanwouters.weatherapp.domain.usecase

import com.milanwouters.weatherapp.domain.model.Farmer
import com.milanwouters.weatherapp.domain.repository.FarmerRepository
import javax.inject.Inject

class AddFarmerUseCase @Inject constructor(
    private val farmerRepository: FarmerRepository
) {
    suspend operator fun invoke(farmer: Farmer): Result<Long> {
        return try {
            require(farmer.name.isNotBlank()) { "Farmer name is required" }
            require(farmer.phoneNumber.isNotBlank()) { "Phone number is required" }
            require(farmer.region.isNotBlank()) { "Region is required" }
            require(farmer.city.isNotBlank()) { "City is required" }
            val id = farmerRepository.addFarmer(farmer)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
