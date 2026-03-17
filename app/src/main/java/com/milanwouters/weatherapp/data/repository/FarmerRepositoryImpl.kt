package com.milanwouters.weatherapp.data.repository

import com.milanwouters.weatherapp.data.local.dao.FarmerDao
import com.milanwouters.weatherapp.data.local.entity.toDomain
import com.milanwouters.weatherapp.data.local.entity.toEntity
import com.milanwouters.weatherapp.domain.model.Farmer
import com.milanwouters.weatherapp.domain.repository.FarmerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FarmerRepositoryImpl @Inject constructor(
    private val farmerDao: FarmerDao
) : FarmerRepository {

    override fun getAllFarmers(): Flow<List<Farmer>> =
        farmerDao.getAllFarmers().map { entities -> entities.map { it.toDomain() } }

    override suspend fun getFarmerById(id: Long): Farmer? =
        farmerDao.getFarmerById(id)?.toDomain()

    override suspend fun getFarmerByPhone(phone: String): Farmer? =
        farmerDao.getFarmerByPhone(phone)?.toDomain()

    override suspend fun addFarmer(farmer: Farmer): Long =
        farmerDao.insertFarmer(farmer.toEntity())

    override suspend fun updateFarmer(farmer: Farmer) =
        farmerDao.updateFarmer(farmer.toEntity())

    override suspend fun deleteFarmer(farmer: Farmer) =
        farmerDao.deleteFarmer(farmer.toEntity())

    override suspend fun getFarmerCount(): Int =
        farmerDao.getFarmerCount()

    override suspend fun getFarmersByRegion(region: String): List<Farmer> =
        farmerDao.getFarmersByRegion(region).map { it.toDomain() }
}
