package com.milanwouters.weatherapp.domain.usecase

import com.milanwouters.weatherapp.domain.model.*
import com.milanwouters.weatherapp.domain.repository.FarmerRepository
import com.milanwouters.weatherapp.sms.SmsGateway
import com.milanwouters.weatherapp.util.TanzaniaRegions
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Alert engine: matches weather alerts to farmers based on:
 * - farmer region and city
 * - subscription type (DAILY/ALERT_ONLY/WEEKLY)
 * - alert severity
 *
 * DAILY: receives all general updates and alerts
 * ALERT_ONLY: receives only HIGH/CRITICAL severity alerts
 * WEEKLY: receives weekly summaries (not individual alerts unless CRITICAL)
 */
class SendAlertsUseCase @Inject constructor(
    private val farmerRepository: FarmerRepository,
    private val smsGateway: SmsGateway
) {
    suspend operator fun invoke(alerts: List<WeatherAlert>): SendAlertsResult {
        val farmers = farmerRepository.getAllFarmers().first()

        val sentMessages = mutableListOf<AlertDelivery>()

        for (alert in alerts) {
            val matchingFarmers = farmers.filter { farmer ->
                farmerMatchesAlert(farmer, alert)
            }

            for (farmer in matchingFarmers) {
                if (shouldSendToFarmer(farmer, alert)) {
                    smsGateway.sendWeatherAlert(farmer, alert)
                    sentMessages.add(AlertDelivery(farmer, alert))
                }
            }
        }

        return SendAlertsResult(
            totalAlerts = alerts.size,
            farmersReached = sentMessages.map { it.farmer.id }.distinct().size,
            deliveries = sentMessages
        )
    }

    suspend fun sendDailySummaries(items: List<WeatherSourceItem>): Int {
        var count = 0
        val summary = buildDailySummary(items)
        val farmers = farmerRepository.getAllFarmers().first()

        for (farmer in farmers) {
            if (farmer.subscriptionType == SubscriptionType.DAILY && farmer.isActive) {
                smsGateway.sendDailySummary(farmer, summary)
                count++
            }
        }
        return count
    }

    private fun farmerMatchesAlert(farmer: Farmer, alert: WeatherAlert): Boolean {
        if (!farmer.isActive) return false
        // Empty targetRegion = affects all of Tanzania
        if (alert.targetRegion.isEmpty()) return true
        // Check region match
        return TanzaniaRegions.textMatchesRegion(alert.targetRegion, farmer.region) ||
               TanzaniaRegions.textMatchesRegion(alert.targetRegion, farmer.city)
    }

    private fun shouldSendToFarmer(farmer: Farmer, alert: WeatherAlert): Boolean {
        return when (farmer.subscriptionType) {
            SubscriptionType.DAILY -> true // daily receives everything
            SubscriptionType.ALERT_ONLY -> alert.severity.priority >= AlertSeverity.MEDIUM.priority
            SubscriptionType.WEEKLY -> alert.severity == AlertSeverity.CRITICAL // only critical for weekly subscribers
        }
    }

    private fun buildDailySummary(items: List<WeatherSourceItem>): String {
        val topItem = items.maxByOrNull { it.severity.priority }
        return topItem?.summary ?: "No significant weather events reported for Tanzania today."
    }
}

data class AlertDelivery(val farmer: Farmer, val alert: WeatherAlert)

data class SendAlertsResult(
    val totalAlerts: Int,
    val farmersReached: Int,
    val deliveries: List<AlertDelivery>
)
