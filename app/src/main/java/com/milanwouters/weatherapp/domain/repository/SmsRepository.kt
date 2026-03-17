package com.milanwouters.weatherapp.domain.repository

import com.milanwouters.weatherapp.domain.model.SmsMessage
import kotlinx.coroutines.flow.Flow

interface SmsRepository {
    fun getAllMessages(): Flow<List<SmsMessage>>
    fun getOutboundMessages(): Flow<List<SmsMessage>>
    fun getInboundMessages(): Flow<List<SmsMessage>>
    suspend fun saveMessage(message: SmsMessage): Long
    suspend fun getRecentMessages(limit: Int): List<SmsMessage>
}
