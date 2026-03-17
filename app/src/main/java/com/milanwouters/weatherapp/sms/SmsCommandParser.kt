package com.milanwouters.weatherapp.sms

import com.milanwouters.weatherapp.domain.model.Language
import com.milanwouters.weatherapp.domain.model.SubscriptionType

/**
 * Parses incoming SMS commands from farmers.
 * Supports simple keyword-based commands for low-tech users.
 *
 * Supported commands:
 * START                - Register for weather alerts
 * STOP                 - Unsubscribe
 * DAILY                - Switch to daily updates
 * ALERT / ALERT_ONLY   - Switch to alerts only
 * WEEKLY               - Switch to weekly summary
 * REGION <name>        - Set region preference
 * CITY <name>          - Set city preference
 * LANG SW / LANG EN    - Set language
 * STATUS               - Request current subscription status
 * HELP                 - Get list of commands
 */
object SmsCommandParser {

    sealed class SmsCommand {
        object Start : SmsCommand()
        object Stop : SmsCommand()
        object Help : SmsCommand()
        object Status : SmsCommand()
        data class SetSubscription(val type: SubscriptionType) : SmsCommand()
        data class SetRegion(val region: String) : SmsCommand()
        data class SetCity(val city: String) : SmsCommand()
        data class SetLanguage(val language: Language) : SmsCommand()
        data class Unknown(val raw: String) : SmsCommand()
    }

    fun parse(rawSms: String): SmsCommand {
        val trimmed = rawSms.trim().uppercase()
        val parts = trimmed.split("\\s+".toRegex(), limit = 2)
        val command = parts.firstOrNull() ?: ""
        val argument = parts.getOrNull(1)?.trim() ?: ""

        return when (command) {
            "START" -> SmsCommand.Start
            "STOP", "UNSUBSCRIBE", "QUIT" -> SmsCommand.Stop
            "HELP", "MSAADA" -> SmsCommand.Help
            "STATUS", "INFO" -> SmsCommand.Status
            "DAILY" -> SmsCommand.SetSubscription(SubscriptionType.DAILY)
            "ALERT", "ALERT_ONLY", "ALERTS" -> SmsCommand.SetSubscription(SubscriptionType.ALERT_ONLY)
            "WEEKLY" -> SmsCommand.SetSubscription(SubscriptionType.WEEKLY)
            "REGION" -> if (argument.isNotEmpty()) SmsCommand.SetRegion(argument.lowercase().replaceFirstChar { it.uppercase() }) else SmsCommand.Unknown(rawSms)
            "CITY" -> if (argument.isNotEmpty()) SmsCommand.SetCity(argument.lowercase().replaceFirstChar { it.uppercase() }) else SmsCommand.Unknown(rawSms)
            "LANG" -> when (argument) {
                "SW", "SWAHILI" -> SmsCommand.SetLanguage(Language.SWAHILI)
                "EN", "ENGLISH" -> SmsCommand.SetLanguage(Language.ENGLISH)
                else -> SmsCommand.Unknown(rawSms)
            }
            else -> SmsCommand.Unknown(rawSms)
        }
    }

    fun generateHelpResponse(language: Language): String {
        return if (language == Language.SWAHILI) {
            "Amri: DAILY=kila siku, ALERT=tahadhari tu, WEEKLY=kila wiki, REGION [jina], CITY [mji], STOP=acha, STATUS=hali"
        } else {
            "Commands: DAILY=daily updates, ALERT=alerts only, WEEKLY=weekly, REGION [name], CITY [name], STOP=unsubscribe, STATUS=check status"
        }
    }

    fun generateStatusResponse(farmer: com.milanwouters.weatherapp.domain.model.Farmer): String {
        return "Status: ${farmer.subscriptionType.displayName} | Region: ${farmer.region} | City: ${farmer.city} | Lang: ${farmer.language.displayName}"
    }

    fun generateStartResponse(language: Language): String {
        return if (language == Language.SWAHILI) {
            "Karibu TMA Weather Alert! Tuma REGION [jina la mkoa] na CITY [mji] kukamilisha usajili. Mfano: REGION DODOMA"
        } else {
            "Welcome to TMA Weather Alert! Send REGION [name] and CITY [name] to complete registration. E.g.: REGION DODOMA"
        }
    }
}
