package com.milanwouters.weatherapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.milanwouters.weatherapp.domain.model.AlertSeverity
import com.milanwouters.weatherapp.domain.model.WeatherSourceItem

@Entity(tableName = "weather_cache")
data class WeatherCacheEntity(
    @PrimaryKey val id: String,
    val sourceName: String,
    val sourceUrl: String,
    val title: String,
    val category: String,
    val regionText: String = "",
    val cityText: String = "",
    val dateRange: String = "",
    val summary: String,
    val rawText: String,
    val severity: String = AlertSeverity.INFO.name,
    val scrapedAt: Long = System.currentTimeMillis()
)

fun WeatherCacheEntity.toDomain() = WeatherSourceItem(
    id = id,
    sourceName = sourceName,
    sourceUrl = sourceUrl,
    title = title,
    category = category,
    regionText = regionText,
    cityText = cityText,
    dateRange = dateRange,
    summary = summary,
    rawText = rawText,
    severity = AlertSeverity.valueOf(severity),
    scrapedAt = scrapedAt
)

fun WeatherSourceItem.toEntity() = WeatherCacheEntity(
    id = id,
    sourceName = sourceName,
    sourceUrl = sourceUrl,
    title = title,
    category = category,
    regionText = regionText,
    cityText = cityText,
    dateRange = dateRange,
    summary = summary,
    rawText = rawText,
    severity = severity.name,
    scrapedAt = scrapedAt
)
