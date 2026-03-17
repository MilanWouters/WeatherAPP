package com.milanwouters.weatherapp.data.repository

import com.milanwouters.weatherapp.data.local.dao.SmsLogDao
import com.milanwouters.weatherapp.data.local.entity.toDomain
import com.milanwouters.weatherapp.data.local.entity.toEntity
import com.milanwouters.weatherapp.domain.model.SmsMessage
import com.milanwouters.weatherapp.domain.repository.SmsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SmsRepositoryImpl @Inject constructor(
    private val smsLogDao: SmsLogDao
) : SmsRepository {

    override fun getAllMessages(): Flow<List<SmsMessage>> =
        smsLogDao.getAllMessages().map { it.map { e -> e.toDomain() } }

    override fun getOutboundMessages(): Flow<List<SmsMessage>> =
        smsLogDao.getOutboundMessages().map { it.map { e -> e.toDomain() } }

    override fun getInboundMessages(): Flow<List<SmsMessage>> =
        smsLogDao.getInboundMessages().map { it.map { e -> e.toDomain() } }

    override suspend fun saveMessage(message: SmsMessage): Long =
        smsLogDao.insertMessage(message.toEntity())

    override suspend fun getRecentMessages(limit: Int): List<SmsMessage> =
        smsLogDao.getRecentMessages(limit).map { it.toDomain() }
}
