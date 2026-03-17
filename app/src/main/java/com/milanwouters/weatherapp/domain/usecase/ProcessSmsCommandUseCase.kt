package com.milanwouters.weatherapp.domain.usecase

import com.milanwouters.weatherapp.domain.model.*
import com.milanwouters.weatherapp.domain.repository.FarmerRepository
import com.milanwouters.weatherapp.domain.repository.SmsRepository
import com.milanwouters.weatherapp.sms.SmsCommandParser
import com.milanwouters.weatherapp.sms.SmsGateway
import javax.inject.Inject

/**
 * Processes an incoming SMS command from a farmer.
 * Parses the command, updates farmer preferences in Room,
 * and sends an appropriate reply via the SMS gateway.
 */
class ProcessSmsCommandUseCase @Inject constructor(
    private val farmerRepository: FarmerRepository,
    private val smsRepository: SmsRepository,
    private val smsGateway: SmsGateway
) {
    suspend operator fun invoke(phoneNumber: String, rawMessage: String): String {
        // Log inbound message
        smsRepository.saveMessage(SmsMessage(
            direction = SmsDirection.INBOUND,
            phoneNumber = phoneNumber,
            content = rawMessage
        ))

        val command = SmsCommandParser.parse(rawMessage)
        val farmer = farmerRepository.getFarmerByPhone(phoneNumber)

        val reply = when (command) {
            is SmsCommandParser.SmsCommand.Start -> {
                val lang = farmer?.language ?: Language.ENGLISH
                SmsCommandParser.generateStartResponse(lang)
            }
            is SmsCommandParser.SmsCommand.Stop -> {
                if (farmer != null) {
                    farmerRepository.updateFarmer(farmer.copy(isActive = false))
                    "You have been unsubscribed from TMA Weather Alerts. Send START to re-subscribe."
                } else {
                    "You are not registered. Send START to subscribe."
                }
            }
            is SmsCommandParser.SmsCommand.Help -> {
                val lang = farmer?.language ?: Language.ENGLISH
                SmsCommandParser.generateHelpResponse(lang)
            }
            is SmsCommandParser.SmsCommand.Status -> {
                if (farmer != null) SmsCommandParser.generateStatusResponse(farmer)
                else "Not registered. Send START to subscribe."
            }
            is SmsCommandParser.SmsCommand.SetSubscription -> {
                if (farmer != null) {
                    farmerRepository.updateFarmer(farmer.copy(subscriptionType = command.type))
                    "Subscription updated to: ${command.type.displayName}"
                } else {
                    "Not registered. Send START first."
                }
            }
            is SmsCommandParser.SmsCommand.SetRegion -> {
                if (farmer != null) {
                    farmerRepository.updateFarmer(farmer.copy(region = command.region))
                    "Region updated to: ${command.region}. Now send CITY [name]."
                } else "Not registered. Send START first."
            }
            is SmsCommandParser.SmsCommand.SetCity -> {
                if (farmer != null) {
                    farmerRepository.updateFarmer(farmer.copy(city = command.city))
                    "City updated to: ${command.city}. Your profile is complete."
                } else "Not registered. Send START first."
            }
            is SmsCommandParser.SmsCommand.SetLanguage -> {
                if (farmer != null) {
                    farmerRepository.updateFarmer(farmer.copy(language = command.language))
                    "Language set to: ${command.language.displayName}"
                } else "Not registered. Send START first."
            }
            is SmsCommandParser.SmsCommand.Unknown -> {
                "Unknown command: ${command.raw}. Send HELP for a list of commands."
            }
        }

        // Send reply
        smsGateway.sendSms(phoneNumber, "TMA: $reply")
        return reply
    }
}
