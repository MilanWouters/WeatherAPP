package com.milanwouters.weatherapp.domain.model

data class FarmerLocation(
    val region: String,
    val city: String,
    val optionalZoneLabel: String? = null
) {
    fun matchesRegion(text: String): Boolean {
        val lower = text.lowercase()
        return lower.contains(region.lowercase()) ||
               lower.contains(city.lowercase())
    }
}
