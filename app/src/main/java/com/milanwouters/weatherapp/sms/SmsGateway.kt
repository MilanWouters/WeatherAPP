package com.milanwouters.weatherapp.sms

import com.milanwouters.weatherapp.domain.model.Farmer
import com.milanwouters.weatherapp.domain.model.WeatherAlert

/**
 * Interface for SMS gateway. In production, this would connect to
 * a real SMS provider (e.g., Africa's Talking, Twilio).
 * For demo, FakeSmsGateway simulates SMS sending.
 */
interface SmsGateway {
    suspend fun sendSms(phoneNumber: String, message: String): Boolean
    suspend fun sendWeatherAlert(farmer: Farmer, alert: WeatherAlert): Boolean
    suspend fun sendDailySummary(farmer: Farmer, summaryText: String): Boolean
    suspend fun sendWeeklySummary(farmer: Farmer, summaryText: String): Boolean
}
