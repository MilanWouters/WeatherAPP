package com.milanwouters.weatherapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.milanwouters.weatherapp.domain.model.Farmer
import com.milanwouters.weatherapp.domain.model.Language
import com.milanwouters.weatherapp.domain.model.SubscriptionType

@Entity(tableName = "farmers")
data class FarmerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phoneNumber: String,
    val language: String,          // Language.code
    val subscriptionType: String,  // SubscriptionType.name
    val region: String,
    val city: String,
    val isActive: Boolean = true,
    val registeredAt: Long = System.currentTimeMillis()
)

fun FarmerEntity.toDomain() = Farmer(
    id = id,
    name = name,
    phoneNumber = phoneNumber,
    language = Language.entries.firstOrNull { it.code == language } ?: Language.ENGLISH,
    subscriptionType = SubscriptionType.valueOf(subscriptionType),
    region = region,
    city = city,
    isActive = isActive,
    registeredAt = registeredAt
)

fun Farmer.toEntity() = FarmerEntity(
    id = id,
    name = name,
    phoneNumber = phoneNumber,
    language = language.code,
    subscriptionType = subscriptionType.name,
    region = region,
    city = city,
    isActive = isActive,
    registeredAt = registeredAt
)
