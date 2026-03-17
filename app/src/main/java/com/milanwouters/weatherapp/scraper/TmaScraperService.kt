package com.milanwouters.weatherapp.scraper

import android.util.Log
import com.milanwouters.weatherapp.domain.model.AlertSeverity
import com.milanwouters.weatherapp.domain.model.WeatherSourceItem
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Scrapes the Tanzania Meteorological Authority (TMA) website.
 * Primary source: https://www.meteo.go.tz/
 *
 * Strategy:
 * 1. Fetch main TMA page and extract forecast links/content
 * 2. Try specific forecast pages (warnings, 10-day, agro bulletins)
 * 3. Parse text for Tanzania-relevant weather information
 * 4. Return structured WeatherSourceItem objects
 *
 * Designed to be robust: does not crash on HTML changes, falls back gracefully.
 */
@Singleton
class TmaScraperService @Inject constructor() {

    private val tag = "TmaScraperService"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    private val tmaBaseUrl = "https://www.meteo.go.tz"
    private val tmaUrls = listOf(
        "$tmaBaseUrl/",
        "$tmaBaseUrl/forecast/",
        "$tmaBaseUrl/warnings/",
        "$tmaBaseUrl/agrometeorological/"
    )

    /**
     * Main scraping entry point. Attempts to fetch and parse TMA pages.
     * Returns a list of WeatherSourceItems or throws an exception on total failure.
     */
    suspend fun scrapeAll(): List<WeatherSourceItem> {
        val results = mutableListOf<WeatherSourceItem>()

        for (url in tmaUrls) {
            try {
                Log.d(tag, "Fetching: $url")
                val doc = fetchDocument(url) ?: continue
                val items = parseDocument(doc, url)
                results.addAll(items)
                Log.d(tag, "Got ${items.size} items from $url")
            } catch (e: Exception) {
                Log.w(tag, "Failed to scrape $url: ${e.message}")
                // Continue to next URL rather than failing entirely
            }
        }

        return results.distinctBy { it.id }
    }

    private fun fetchDocument(url: String): Document? {
        return try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (compatible; TanzaniaWeatherBot/1.0)")
                .build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string() ?: return null
                Jsoup.parse(body, url)
            } else {
                Log.w(tag, "HTTP ${response.code} for $url")
                null
            }
        } catch (e: Exception) {
            Log.w(tag, "Network error for $url: ${e.message}")
            null
        }
    }

    private fun parseDocument(doc: Document, sourceUrl: String): List<WeatherSourceItem> {
        val items = mutableListOf<WeatherSourceItem>()

        // Strategy 1: Look for forecast/warning cards or sections
        val forecastSections = doc.select(
            "article, .forecast, .weather-item, .warning, .bulletin, " +
            ".news-item, .post, [class*=forecast], [class*=weather], [class*=warning]"
        )

        forecastSections.forEachIndexed { index, element ->
            val title = element.select("h1, h2, h3, h4, .title, .heading").firstOrNull()?.text()
                ?: element.select("strong, b").firstOrNull()?.text()
                ?: "Forecast Item ${index + 1}"
            val text = element.text().trim()
            if (text.length < 30) return@forEachIndexed // skip tiny fragments

            val item = buildWeatherSourceItem(
                id = "tma-section-${sourceUrl.hashCode()}-$index",
                title = title,
                rawText = text,
                sourceUrl = sourceUrl,
                category = detectCategory(title, text)
            )
            items.add(item)
        }

        // Strategy 2: Parse main content area for any Tanzania weather text
        if (items.isEmpty()) {
            val mainContent = doc.select("main, #content, .content, article, body").firstOrNull()
            val text = mainContent?.text()?.trim() ?: doc.body()?.text()?.trim() ?: ""
            if (text.length > 100) {
                // Split by paragraphs or headings
                val paragraphs = text.split("\n\n", "  ").filter { it.length > 50 }
                paragraphs.forEachIndexed { index, para ->
                    if (isTanzaniaWeatherRelevant(para)) {
                        items.add(buildWeatherSourceItem(
                            id = "tma-para-${sourceUrl.hashCode()}-$index",
                            title = "TMA Forecast",
                            rawText = para,
                            sourceUrl = sourceUrl,
                            category = detectCategory("", para)
                        ))
                    }
                }
            }
        }

        // Strategy 3: Extract headings with content
        if (items.isEmpty()) {
            doc.select("h1, h2, h3").forEachIndexed { index, heading ->
                val headingText = heading.text().trim()
                val nextText = heading.nextElementSibling()?.text()?.trim() ?: ""
                val combined = "$headingText $nextText"
                if (isTanzaniaWeatherRelevant(combined) && combined.length > 50) {
                    items.add(buildWeatherSourceItem(
                        id = "tma-heading-${sourceUrl.hashCode()}-$index",
                        title = headingText,
                        rawText = combined,
                        sourceUrl = sourceUrl,
                        category = detectCategory(headingText, nextText)
                    ))
                }
            }
        }

        // Strategy 4: If still nothing, create a single item from page title + meta description
        if (items.isEmpty()) {
            val pageTitle = doc.title()
            val metaDesc = doc.select("meta[name=description]").attr("content")
            val text = "$pageTitle $metaDesc"
            if (text.isNotBlank() && text.length > 20) {
                items.add(buildWeatherSourceItem(
                    id = "tma-page-${sourceUrl.hashCode()}",
                    title = pageTitle,
                    rawText = text,
                    sourceUrl = sourceUrl,
                    category = "general"
                ))
            }
        }

        return items
    }

    private fun buildWeatherSourceItem(
        id: String,
        title: String,
        rawText: String,
        sourceUrl: String,
        category: String
    ): WeatherSourceItem {
        val regionText = detectRegions(rawText)
        val cityText = detectCities(rawText)
        val dateRange = extractDateRange(rawText)
        val summary = rawText.take(200).trim()
        val severity = detectSeverity(rawText)

        return WeatherSourceItem(
            id = id,
            sourceName = "TMA",
            sourceUrl = sourceUrl,
            title = title,
            category = category,
            regionText = regionText,
            cityText = cityText,
            dateRange = dateRange,
            summary = summary,
            rawText = rawText,
            severity = severity
        )
    }

    private fun isTanzaniaWeatherRelevant(text: String): Boolean {
        val lower = text.lowercase()
        return lower.contains("tanzania") ||
               lower.contains("forecast") ||
               lower.contains("rainfall") ||
               lower.contains("weather") ||
               lower.contains("warning") ||
               lower.contains("drought") ||
               lower.contains("flood") ||
               lower.contains("temperature")
    }

    private fun detectCategory(title: String, text: String): String {
        val combined = "$title $text".lowercase()
        return when {
            "warning" in combined || "severe" in combined -> "warning"
            "agro" in combined || "agricultural" in combined -> "agro bulletin"
            "10-day" in combined || "10 day" in combined -> "10-day forecast"
            "weekly" in combined -> "weekly forecast"
            "flood" in combined -> "flood bulletin"
            "drought" in combined -> "drought advisory"
            else -> "general forecast"
        }
    }

    private fun detectRegions(text: String): String {
        val lower = text.lowercase()
        val tanzaniaRegions = listOf(
            "Arusha", "Dar es Salaam", "Dodoma", "Geita", "Iringa", "Kagera",
            "Katavi", "Kigoma", "Kilimanjaro", "Lindi", "Manyara", "Mara",
            "Mbeya", "Morogoro", "Mtwara", "Mwanza", "Njombe", "Pwani",
            "Rukwa", "Ruvuma", "Shinyanga", "Simiyu", "Singida", "Songwe",
            "Tabora", "Tanga", "Zanzibar"
        )
        return tanzaniaRegions.filter { lower.contains(it.lowercase()) }.joinToString(", ")
    }

    private fun detectCities(text: String): String {
        val lower = text.lowercase()
        val cities = listOf(
            "Moshi", "Arusha", "Dodoma", "Mbeya", "Morogoro", "Mwanza",
            "Tanga", "Ifakara", "Songea", "Sumbawanga", "Tabora", "Kigoma",
            "Bukoba", "Musoma", "Lindi", "Mtwara", "Mpanda", "Babati",
            "Shinyanga", "Singida", "Bariadi"
        )
        return cities.filter { lower.contains(it.lowercase()) }.joinToString(", ")
    }

    private fun extractDateRange(text: String): String {
        // Look for common date patterns
        val patterns = listOf(
            Regex("\\d{1,2}\\s+\\w+\\s+\\d{4}\\s*[-–to]+\\s*\\d{1,2}\\s+\\w+\\s+\\d{4}"),
            Regex("\\w+\\s+\\d{1,2}[-–]\\d{1,2},?\\s+\\d{4}"),
            Regex("\\d{4}-\\d{2}-\\d{2}")
        )
        for (pattern in patterns) {
            val match = pattern.find(text)
            if (match != null) return match.value
        }
        return ""
    }

    private fun detectSeverity(text: String): AlertSeverity {
        val lower = text.lowercase()
        return when {
            "extreme" in lower || "critical" in lower || "emergency" in lower -> AlertSeverity.CRITICAL
            "severe" in lower || "warning" in lower || "flood" in lower -> AlertSeverity.HIGH
            "heavy rainfall" in lower || "strong wind" in lower || "heat stress" in lower -> AlertSeverity.MEDIUM
            "advisory" in lower || "watch" in lower -> AlertSeverity.LOW
            else -> AlertSeverity.INFO
        }
    }
}
