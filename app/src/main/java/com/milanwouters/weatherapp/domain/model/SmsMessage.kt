package com.milanwouters.weatherapp.domain.model

data class SmsMessage(
    val id: Long = 0,
    val direction: SmsDirection,
    val phoneNumber: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: SmsStatus = SmsStatus.SENT,
    val farmerName: String = "",
    val alertType: String = ""
)

enum class SmsDirection {
    INBOUND,   // simulated incoming SMS from farmer
    OUTBOUND   // simulated outgoing SMS to farmer
}

enum class SmsStatus {
    PENDING, SENT, DELIVERED, FAILED
}
