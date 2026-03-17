package com.milanwouters.weatherapp.scraper

import android.util.Log
import com.milanwouters.weatherapp.domain.model.AlertSeverity
import com.milanwouters.weatherapp.domain.model.WeatherSourceItem
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Optional secondary source: ICPAC (Intergovernmental Authority on Development
 * Climate Prediction and Applications Centre).
 * Used as a regional fallback when TMA data is insufficient.
 * https://www.icpac.net/weekly-forecast/
 */
@Singleton
class IcpacFallbackRepository @Inject constructor() {

    private val tag = "IcpacFallback"
    private val icpacUrl = "https://www.icpac.net/weekly-forecast/"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    suspend fun fetchIcpacForecast(): List<WeatherSourceItem> {
        return try {
            Log.d(tag, "Fetching ICPAC fallback: $icpacUrl")
            val request = Request.Builder()
                .url(icpacUrl)
                .header("User-Agent", "Mozilla/5.0 (compatible; TanzaniaWeatherBot/1.0)")
                .build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.w(tag, "ICPAC returned HTTP ${response.code}")
                return emptyList()
            }
            val body = response.body?.string() ?: return emptyList()
            val doc = Jsoup.parse(body, icpacUrl)
            parseIcpacDocument(doc)
        } catch (e: Exception) {
            Log.w(tag, "ICPAC fetch failed: ${e.message}")
            emptyList()
        }
    }

    private fun parseIcpacDocument(doc: org.jsoup.nodes.Document): List<WeatherSourceItem> {
        val items = mutableListOf<WeatherSourceItem>()

        // Try to find Tanzania-specific content in ICPAC page
        val allText = doc.body()?.text() ?: return emptyList()
        val sections = allText.split("\n").filter { it.length > 50 }

        sections.forEachIndexed { index, section ->
            val lower = section.lowercase()
            if (lower.contains("tanzania") || lower.contains("east africa")) {
                items.add(WeatherSourceItem(
                    id = "icpac-$index",
                    sourceName = "ICPAC",
                    sourceUrl = icpacUrl,
                    title = "ICPAC Regional Forecast",
                    category = "regional forecast",
                    regionText = extractTanzaniaRegions(section),
                    summary = section.take(200),
                    rawText = section,
                    severity = detectSeverity(section)
                ))
            }
        }

        return items
    }

    private fun extractTanzaniaRegions(text: String): String {
        val lower = text.lowercase()
        val regions = listOf("northern", "southern", "eastern", "western", "central", "coastal")
        return regions.filter { lower.contains(it) }.joinToString(", ")
    }

    private fun detectSeverity(text: String): AlertSeverity {
        val lower = text.lowercase()
        return when {
            "heavy" in lower || "warning" in lower -> AlertSeverity.HIGH
            "moderate" in lower -> AlertSeverity.MEDIUM
            else -> AlertSeverity.INFO
        }
    }
}
