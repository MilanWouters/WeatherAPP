package com.milanwouters.weatherapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.milanwouters.weatherapp.domain.model.SmsDirection
import com.milanwouters.weatherapp.domain.model.SmsMessage
import com.milanwouters.weatherapp.domain.model.SmsStatus

@Entity(tableName = "sms_logs")
data class SmsLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val direction: String,     // SmsDirection.name
    val phoneNumber: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = SmsStatus.SENT.name,
    val farmerName: String = "",
    val alertType: String = ""
)

fun SmsLogEntity.toDomain() = SmsMessage(
    id = id,
    direction = SmsDirection.valueOf(direction),
    phoneNumber = phoneNumber,
    content = content,
    timestamp = timestamp,
    status = SmsStatus.valueOf(status),
    farmerName = farmerName,
    alertType = alertType
)

fun SmsMessage.toEntity() = SmsLogEntity(
    id = id,
    direction = direction.name,
    phoneNumber = phoneNumber,
    content = content,
    timestamp = timestamp,
    status = status.name,
    farmerName = farmerName,
    alertType = alertType
)
