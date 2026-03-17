package com.milanwouters.weatherapp.domain.model

data class WeatherSourceItem(
    val id: String,
    val sourceName: String,          // e.g. "TMA" or "ICPAC"
    val sourceUrl: String,
    val title: String,
    val category: String,            // e.g. "10-day forecast", "warning", "agro bulletin"
    val regionText: String = "",     // detected region mentions in text
    val cityText: String = "",       // detected city mentions in text
    val dateRange: String = "",      // forecast period
    val summary: String,             // short human-readable summary
    val rawText: String,             // full scraped text
    val severity: AlertSeverity = AlertSeverity.INFO,
    val scrapedAt: Long = System.currentTimeMillis()
)

enum class AlertSeverity(val label: String, val priority: Int) {
    INFO("Info", 0),
    LOW("Low", 1),
    MEDIUM("Medium", 2),
    HIGH("High", 3),
    CRITICAL("Critical", 4)
}
