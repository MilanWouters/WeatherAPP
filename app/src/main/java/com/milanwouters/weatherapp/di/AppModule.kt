package com.milanwouters.weatherapp.di

import android.content.Context
import androidx.room.Room
import com.milanwouters.weatherapp.data.local.AppDatabase
import com.milanwouters.weatherapp.data.local.dao.FarmerDao
import com.milanwouters.weatherapp.data.local.dao.SmsLogDao
import com.milanwouters.weatherapp.data.local.dao.WeatherCacheDao
import com.milanwouters.weatherapp.data.repository.FarmerRepositoryImpl
import com.milanwouters.weatherapp.data.repository.SmsRepositoryImpl
import com.milanwouters.weatherapp.domain.repository.FarmerRepository
import com.milanwouters.weatherapp.domain.repository.SmsRepository
import com.milanwouters.weatherapp.domain.repository.WeatherRepository
import com.milanwouters.weatherapp.scraper.TmaWeatherRepository
import com.milanwouters.weatherapp.sms.FakeSmsGateway
import com.milanwouters.weatherapp.sms.SmsGateway
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "weatherapp_db"
        ).build()
    }

    @Provides
    fun provideFarmerDao(db: AppDatabase): FarmerDao = db.farmerDao()

    @Provides
    fun provideSmsLogDao(db: AppDatabase): SmsLogDao = db.smsLogDao()

    @Provides
    fun provideWeatherCacheDao(db: AppDatabase): WeatherCacheDao = db.weatherCacheDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindFarmerRepository(impl: FarmerRepositoryImpl): FarmerRepository

    @Binds
    @Singleton
    abstract fun bindSmsRepository(impl: SmsRepositoryImpl): SmsRepository

    @Binds
    @Singleton
    abstract fun bindWeatherRepository(impl: TmaWeatherRepository): WeatherRepository

    @Binds
    @Singleton
    abstract fun bindSmsGateway(impl: FakeSmsGateway): SmsGateway
}
