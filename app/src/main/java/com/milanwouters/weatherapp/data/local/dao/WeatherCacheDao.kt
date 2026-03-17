package com.milanwouters.weatherapp.data.local.dao

import androidx.room.*
import com.milanwouters.weatherapp.data.local.entity.WeatherCacheEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherCacheDao {
    @Query("SELECT * FROM weather_cache ORDER BY scrapedAt DESC")
    fun getAllCachedItems(): Flow<List<WeatherCacheEntity>>

    @Query("SELECT * FROM weather_cache ORDER BY scrapedAt DESC")
    suspend fun getCachedItemsOnce(): List<WeatherCacheEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<WeatherCacheEntity>)

    @Query("DELETE FROM weather_cache")
    suspend fun clearCache()

    @Query("SELECT COUNT(*) FROM weather_cache")
    suspend fun getCacheCount(): Int
}
