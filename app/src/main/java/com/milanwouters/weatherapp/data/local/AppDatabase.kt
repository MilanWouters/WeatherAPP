package com.milanwouters.weatherapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.milanwouters.weatherapp.data.local.dao.FarmerDao
import com.milanwouters.weatherapp.data.local.dao.SmsLogDao
import com.milanwouters.weatherapp.data.local.dao.WeatherCacheDao
import com.milanwouters.weatherapp.data.local.entity.FarmerEntity
import com.milanwouters.weatherapp.data.local.entity.SmsLogEntity
import com.milanwouters.weatherapp.data.local.entity.WeatherCacheEntity

@Database(
    entities = [FarmerEntity::class, SmsLogEntity::class, WeatherCacheEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun farmerDao(): FarmerDao
    abstract fun smsLogDao(): SmsLogDao
    abstract fun weatherCacheDao(): WeatherCacheDao
}
