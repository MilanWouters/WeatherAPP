package com.milanwouters.weatherapp.data.local.dao

import androidx.room.*
import com.milanwouters.weatherapp.data.local.entity.FarmerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FarmerDao {
    @Query("SELECT * FROM farmers ORDER BY registeredAt DESC")
    fun getAllFarmers(): Flow<List<FarmerEntity>>

    @Query("SELECT * FROM farmers WHERE id = :id")
    suspend fun getFarmerById(id: Long): FarmerEntity?

    @Query("SELECT * FROM farmers WHERE phoneNumber = :phone LIMIT 1")
    suspend fun getFarmerByPhone(phone: String): FarmerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFarmer(farmer: FarmerEntity): Long

    @Update
    suspend fun updateFarmer(farmer: FarmerEntity)

    @Delete
    suspend fun deleteFarmer(farmer: FarmerEntity)

    @Query("SELECT COUNT(*) FROM farmers")
    suspend fun getFarmerCount(): Int

    @Query("SELECT * FROM farmers WHERE region = :region")
    suspend fun getFarmersByRegion(region: String): List<FarmerEntity>
}
