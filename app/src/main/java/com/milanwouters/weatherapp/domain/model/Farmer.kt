package com.milanwouters.weatherapp.domain.model

data class Farmer(
    val id: Long = 0,
    val name: String,
    val phoneNumber: String,
    val language: Language,
    val subscriptionType: SubscriptionType,
    val region: String,
    val city: String,
    val isActive: Boolean = true,
    val registeredAt: Long = System.currentTimeMillis()
)

enum class Language(val code: String, val displayName: String) {
    ENGLISH("en", "English"),
    SWAHILI("sw", "Kiswahili")
}

enum class SubscriptionType(val displayName: String, val description: String) {
    DAILY("Daily Update", "Receive a weather summary every day"),
    ALERT_ONLY("Alerts Only", "Receive SMS only when dangerous weather is detected"),
    WEEKLY("Weekly Summary", "Receive a weather overview every week")
}
