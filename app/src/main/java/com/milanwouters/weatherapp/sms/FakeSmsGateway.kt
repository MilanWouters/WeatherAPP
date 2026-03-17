package com.milanwouters.weatherapp.sms

import android.util.Log
import com.milanwouters.weatherapp.domain.model.*
import com.milanwouters.weatherapp.domain.repository.SmsRepository
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Simulated SMS gateway for demo/prototype purposes.
 * In production, replace with a real SMS provider API
 * such as Africa's Talking (africastalking.com) which
 * is specifically designed for East Africa markets.
 *
 * Logs all sent messages to Room database for review.
 */
@Singleton
class FakeSmsGateway @Inject constructor(
    private val smsRepository: SmsRepository
) : SmsGateway {

    private val tag = "FakeSmsGateway"

    override suspend fun sendSms(phoneNumber: String, message: String): Boolean {
        // Simulate network delay for SMS sending
        delay(300)
        Log.d(tag, "SMS -> $phoneNumber: $message")
        val smsMessage = SmsMessage(
            direction = SmsDirection.OUTBOUND,
            phoneNumber = phoneNumber,
            content = message,
            status = SmsStatus.SENT
        )
        smsRepository.saveMessage(smsMessage)
        return true
    }

    override suspend fun sendWeatherAlert(farmer: Farmer, alert: WeatherAlert): Boolean {
        val message = buildAlertMessage(farmer, alert)
        Log.d(tag, "ALERT SMS -> ${farmer.phoneNumber} (${farmer.name})")
        val smsMessage = SmsMessage(
            direction = SmsDirection.OUTBOUND,
            phoneNumber = farmer.phoneNumber,
            content = message,
            farmerName = farmer.name,
            alertType = alert.alertType.name,
            status = SmsStatus.SENT
        )
        smsRepository.saveMessage(smsMessage)
        return true
    }

    override suspend fun sendDailySummary(farmer: Farmer, summaryText: String): Boolean {
        val message = buildDailySummary(farmer, summaryText)
        val smsMessage = SmsMessage(
            direction = SmsDirection.OUTBOUND,
            phoneNumber = farmer.phoneNumber,
            content = message,
            farmerName = farmer.name,
            alertType = "DAILY",
            status = SmsStatus.SENT
        )
        smsRepository.saveMessage(smsMessage)
        return true
    }

    override suspend fun sendWeeklySummary(farmer: Farmer, summaryText: String): Boolean {
        val message = buildWeeklySummary(farmer, summaryText)
        val smsMessage = SmsMessage(
            direction = SmsDirection.OUTBOUND,
            phoneNumber = farmer.phoneNumber,
            content = message,
            farmerName = farmer.name,
            alertType = "WEEKLY",
            status = SmsStatus.SENT
        )
        smsRepository.saveMessage(smsMessage)
        return true
    }

    private fun buildAlertMessage(farmer: Farmer, alert: WeatherAlert): String {
        val greeting = if (farmer.language == Language.SWAHILI) "Habari" else "Hello"
        val alertIcon = alert.alertType.icon
        return buildString {
            append("TMA WEATHER ALERT\n")
            append("$greeting ${farmer.name},\n")
            append("$alertIcon ${alert.alertType.displayName.uppercase()}\n")
            append("Region: ${farmer.region}\n")
            append(alert.message.take(100))
            append("\nAdvice: ${alert.recommendation.take(80)}")
            append("\n-TMA via WeatherAlert System")
        }
    }

    private fun buildDailySummary(farmer: Farmer, summaryText: String): String {
        val greeting = if (farmer.language == Language.SWAHILI) "Habari ya asubuhi" else "Good morning"
        return buildString {
            append("TMA DAILY WEATHER\n")
            append("$greeting ${farmer.name},\n")
            append("Region: ${farmer.region} - ${farmer.city}\n")
            append(summaryText.take(120))
            append("\n-TMA via WeatherAlert System")
        }
    }

    private fun buildWeeklySummary(farmer: Farmer, summaryText: String): String {
        return buildString {
            append("TMA WEEKLY FORECAST\n")
            append("Hello ${farmer.name},\n")
            append("Region: ${farmer.region}\n")
            append(summaryText.take(120))
            append("\n-TMA via WeatherAlert System")
        }
    }
}
