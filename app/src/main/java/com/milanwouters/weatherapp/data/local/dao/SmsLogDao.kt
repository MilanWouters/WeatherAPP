package com.milanwouters.weatherapp.data.local.dao

import androidx.room.*
import com.milanwouters.weatherapp.data.local.entity.SmsLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SmsLogDao {
    @Query("SELECT * FROM sms_logs ORDER BY timestamp DESC")
    fun getAllMessages(): Flow<List<SmsLogEntity>>

    @Query("SELECT * FROM sms_logs WHERE direction = 'OUTBOUND' ORDER BY timestamp DESC")
    fun getOutboundMessages(): Flow<List<SmsLogEntity>>

    @Query("SELECT * FROM sms_logs WHERE direction = 'INBOUND' ORDER BY timestamp DESC")
    fun getInboundMessages(): Flow<List<SmsLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: SmsLogEntity): Long

    @Query("SELECT * FROM sms_logs ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentMessages(limit: Int): List<SmsLogEntity>
}
