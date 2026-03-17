package com.milanwouters.weatherapp.domain.repository

import com.milanwouters.weatherapp.domain.model.Farmer
import kotlinx.coroutines.flow.Flow

interface FarmerRepository {
    fun getAllFarmers(): Flow<List<Farmer>>
    suspend fun getFarmerById(id: Long): Farmer?
    suspend fun getFarmerByPhone(phone: String): Farmer?
    suspend fun addFarmer(farmer: Farmer): Long
    suspend fun updateFarmer(farmer: Farmer)
    suspend fun deleteFarmer(farmer: Farmer)
    suspend fun getFarmerCount(): Int
    suspend fun getFarmersByRegion(region: String): List<Farmer>
}
