package com.milanwouters.weatherapp.scraper

import android.util.Log
import com.milanwouters.weatherapp.data.local.dao.WeatherCacheDao
import com.milanwouters.weatherapp.data.local.entity.toDomain
import com.milanwouters.weatherapp.data.local.entity.toEntity
import com.milanwouters.weatherapp.domain.model.*
import com.milanwouters.weatherapp.domain.repository.WeatherRepository
import com.milanwouters.weatherapp.domain.repository.WeatherResult
import com.milanwouters.weatherapp.util.SeedData
import com.milanwouters.weatherapp.util.TanzaniaRegions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Primary weather data source: Tanzania Meteorological Authority (TMA).
 * Implements WeatherRepository using live TMA scraping as primary flow,
 * with local Room cache as fallback.
 *
 * Flow:
 * 1. Try live scraping from TMA
 * 2. If successful, cache results in Room and return Success(isLive=true)
 * 3. If failed, return cached data from Room
 * 4. If cache also empty, return demo fallback data
 */
@Singleton
class TmaWeatherRepository @Inject constructor(
    private val scraperService: TmaScraperService,
    private val icpacFallback: IcpacFallbackRepository,
    private val weatherCacheDao: WeatherCacheDao
) : WeatherRepository {

    private val tag = "TmaWeatherRepository"

    override suspend fun fetchLatestForecast(): WeatherResult = withContext(Dispatchers.IO) {
        // Step 1: Try live TMA scraping
        try {
            Log.d(tag, "Starting live TMA scrape...")
            val tmaItems = scraperService.scrapeAll()

            if (tmaItems.isNotEmpty()) {
                // Cache successful results
                weatherCacheDao.clearCache()
                weatherCacheDao.insertAll(tmaItems.map { it.toEntity() })
                Log.d(tag, "Live scrape successful: ${tmaItems.size} items cached")
                return@withContext WeatherResult.Success(tmaItems, isLive = true)
            }

            // TMA returned nothing, try ICPAC fallback
            Log.d(tag, "TMA returned no items, trying ICPAC fallback...")
            val icpacItems = icpacFallback.fetchIcpacForecast()

            if (icpacItems.isNotEmpty()) {
                val combined = (tmaItems + icpacItems).distinctBy { it.id }
                weatherCacheDao.clearCache()
                weatherCacheDao.insertAll(combined.map { it.toEntity() })
                return@withContext WeatherResult.Success(combined, isLive = true)
            }

            // Both live sources failed, fall back to cache
            return@withContext loadFromCache()

        } catch (e: Exception) {
            Log.w(tag, "Live scrape failed: ${e.message}")
            return@withContext loadFromCache()
        }
    }

    private suspend fun loadFromCache(): WeatherResult {
        val cached = weatherCacheDao.getCachedItemsOnce()
        return if (cached.isNotEmpty()) {
            Log.d(tag, "Returning ${cached.size} cached items")
            WeatherResult.Cached(cached.map { it.toDomain() })
        } else {
            // Final fallback: use demo data
            Log.d(tag, "No cache available, using demo fallback data")
            WeatherResult.Error(
                message = "TMA website unreachable. Using demo data for demonstration.",
                cachedItems = SeedData.demoForecastCache
            )
        }
    }

    override suspend fun getAlerts(items: List<WeatherSourceItem>): List<WeatherAlert> {
        return AlertParser.parseAlerts(items)
    }

    override suspend fun getCachedItems(): List<WeatherSourceItem> {
        val cached = weatherCacheDao.getCachedItemsOnce()
        return if (cached.isNotEmpty()) {
            cached.map { it.toDomain() }
        } else {
            SeedData.demoForecastCache
        }
    }
}

/**
 * Parses WeatherSourceItems into WeatherAlerts using keyword rules.
 * Designed to be defensive: no crashes on unexpected input.
 */
object AlertParser {

    fun parseAlerts(items: List<WeatherSourceItem>): List<WeatherAlert> {
        val alerts = mutableListOf<WeatherAlert>()

        for (item in items) {
            val text = "${item.title} ${item.rawText}".lowercase()
            val regions = item.regionText.split(",").map { it.trim() }.filter { it.isNotEmpty() }

            // Determine target regions
            val targetRegions = if (regions.isEmpty()) listOf("") else regions

            for (targetRegion in targetRegions) {
                val targetCity = if (item.cityText.isNotBlank()) item.cityText.split(",").firstOrNull()?.trim() ?: "" else ""

                when {
                    "flood" in text -> alerts.add(createAlert(
                        AlertType.FLOOD_RISK, targetRegion, targetCity, item,
                        message = "Flood risk detected. ${item.summary}",
                        recommendation = "Move livestock to higher ground. Avoid low-lying areas and river banks. Check water channels."
                    ))
                    "heavy rainfall" in text || "heavy rain" in text -> alerts.add(createAlert(
                        AlertType.HEAVY_RAIN, targetRegion, targetCity, item,
                        message = "Heavy rainfall expected. ${item.summary}",
                        recommendation = "Protect seedlings from waterlogging. Delay planting on slopes. Ensure drainage channels are clear."
                    ))
                    "heat stress" in text -> alerts.add(createAlert(
                        AlertType.HEAT_STRESS, targetRegion, targetCity, item,
                        message = "Heat stress conditions. ${item.summary}",
                        recommendation = "Increase irrigation frequency. Provide shade for livestock. Harvest early if crops are mature."
                    ))
                    "less than usual rainfall" in text || "below normal rainfall" in text || "drought" in text -> alerts.add(createAlert(
                        AlertType.DROUGHT_RISK, targetRegion, targetCity, item,
                        message = "Drought risk. ${item.summary}",
                        recommendation = "Conserve water. Consider drought-resistant crops. Reduce livestock numbers if pasture is limited."
                    ))
                    "strong wind" in text || "gale" in text -> alerts.add(createAlert(
                        AlertType.STRONG_WIND, targetRegion, targetCity, item,
                        message = "Strong winds expected. ${item.summary}",
                        recommendation = "Secure loose structures. Protect young crops. Avoid working in open fields during peak wind."
                    ))
                    else -> alerts.add(createAlert(
                        AlertType.GENERAL_UPDATE, targetRegion, targetCity, item,
                        message = item.summary,
                        recommendation = "Monitor weather conditions. Check TMA website for updates."
                    ))
                }
            }
        }

        // Remove duplicate alert types for same region
        return alerts
            .groupBy { "${it.alertType}-${it.targetRegion}" }
            .map { (_, group) -> group.maxByOrNull { it.severity.priority }!! }
            .sortedByDescending { it.severity.priority }
    }

    private fun createAlert(
        type: AlertType,
        targetRegion: String,
        targetCity: String,
        source: WeatherSourceItem,
        message: String,
        recommendation: String
    ): WeatherAlert {
        val severity = when (type) {
            AlertType.FLOOD_RISK -> maxOf(source.severity, AlertSeverity.HIGH) { a, b -> a.priority - b.priority }
            AlertType.HEAVY_RAIN -> maxOf(source.severity, AlertSeverity.MEDIUM) { a, b -> a.priority - b.priority }
            AlertType.HEAT_STRESS -> maxOf(source.severity, AlertSeverity.MEDIUM) { a, b -> a.priority - b.priority }
            AlertType.DROUGHT_RISK -> maxOf(source.severity, AlertSeverity.MEDIUM) { a, b -> a.priority - b.priority }
            AlertType.STRONG_WIND -> maxOf(source.severity, AlertSeverity.HIGH) { a, b -> a.priority - b.priority }
            AlertType.GENERAL_UPDATE -> source.severity
        }

        return WeatherAlert(
            alertType = type,
            targetRegion = targetRegion,
            targetCity = targetCity,
            message = message,
            recommendation = recommendation,
            severity = severity,
            forecastPeriod = source.dateRange,
            sourceName = source.sourceName,
            sourceUrl = source.sourceUrl,
            sourceText = source.rawText.take(300)
        )
    }
}
