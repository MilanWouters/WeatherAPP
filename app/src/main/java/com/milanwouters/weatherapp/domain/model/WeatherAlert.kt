package com.milanwouters.weatherapp.domain.model

data class WeatherAlert(
    val id: String = java.util.UUID.randomUUID().toString(),
    val alertType: AlertType,
    val targetRegion: String,        // empty = all regions
    val targetCity: String = "",     // empty = all cities in region
    val message: String,
    val recommendation: String,
    val severity: AlertSeverity,
    val forecastPeriod: String = "",
    val sourceName: String = "TMA",
    val sourceUrl: String = "",
    val sourceText: String = "",     // the raw text that triggered this alert
    val createdAt: Long = System.currentTimeMillis()
)

enum class AlertType(val displayName: String, val icon: String) {
    HEAVY_RAIN("Heavy Rainfall", "🌧"),
    FLOOD_RISK("Flood Risk", "🌊"),
    DROUGHT_RISK("Drought Risk", "🏜"),
    HEAT_STRESS("Heat Stress", "🌡"),
    STRONG_WIND("Strong Winds", "💨"),
    GENERAL_UPDATE("General Update", "📋")
}
