package com.milanwouters.weatherapp.util

import com.milanwouters.weatherapp.domain.model.*

/**
 * Seed data for demo purposes.
 * Provides 5 sample farmers spread across different Tanzania regions,
 * plus sample SMS messages and a demo forecast cache.
 */
object SeedData {

    val sampleFarmers = listOf(
        Farmer(
            name = "Juma Hassan",
            phoneNumber = "+255712345678",
            language = Language.SWAHILI,
            subscriptionType = SubscriptionType.ALERT_ONLY,
            region = "Dodoma",
            city = "Dodoma"
        ),
        Farmer(
            name = "Amina Mwangi",
            phoneNumber = "+255723456789",
            language = Language.SWAHILI,
            subscriptionType = SubscriptionType.DAILY,
            region = "Morogoro",
            city = "Ifakara"
        ),
        Farmer(
            name = "Peter Kimaro",
            phoneNumber = "+255734567890",
            language = Language.ENGLISH,
            subscriptionType = SubscriptionType.WEEKLY,
            region = "Kilimanjaro",
            city = "Moshi"
        ),
        Farmer(
            name = "Fatuma Salehe",
            phoneNumber = "+255745678901",
            language = Language.SWAHILI,
            subscriptionType = SubscriptionType.ALERT_ONLY,
            region = "Mbeya",
            city = "Mbeya"
        ),
        Farmer(
            name = "John Mwamba",
            phoneNumber = "+255756789012",
            language = Language.ENGLISH,
            subscriptionType = SubscriptionType.DAILY,
            region = "Mwanza",
            city = "Mwanza"
        )
    )

    val sampleInboundSms = listOf(
        SmsMessage(
            direction = SmsDirection.INBOUND,
            phoneNumber = "+255798000001",
            content = "START",
            farmerName = "Unknown"
        ),
        SmsMessage(
            direction = SmsDirection.INBOUND,
            phoneNumber = "+255712345678",
            content = "DAILY",
            farmerName = "Juma Hassan"
        ),
        SmsMessage(
            direction = SmsDirection.INBOUND,
            phoneNumber = "+255723456789",
            content = "ALERT",
            farmerName = "Amina Mwangi"
        ),
        SmsMessage(
            direction = SmsDirection.INBOUND,
            phoneNumber = "+255798000002",
            content = "REGION DODOMA",
            farmerName = "Unknown"
        ),
        SmsMessage(
            direction = SmsDirection.INBOUND,
            phoneNumber = "+255798000002",
            content = "CITY MPWAPWA",
            farmerName = "Unknown"
        ),
        SmsMessage(
            direction = SmsDirection.INBOUND,
            phoneNumber = "+255798000003",
            content = "LANG SW",
            farmerName = "Unknown"
        )
    )

    /** Demo fallback cache used when TMA website is unreachable */
    val demoForecastCache = listOf(
        WeatherSourceItem(
            id = "demo-tma-001",
            sourceName = "TMA",
            sourceUrl = "https://www.meteo.go.tz/",
            title = "10-Day Weather Forecast for Tanzania",
            category = "10-day forecast",
            regionText = "Central Tanzania, Dodoma, Singida",
            cityText = "Dodoma",
            dateRange = "Demo period",
            summary = "Below normal rainfall expected over central Tanzania. Heat stress possible in Dodoma and Singida regions. Farmers advised to prepare irrigation.",
            rawText = "Central Tanzania including Dodoma and Singida regions is expected to receive less than usual rainfall during this period. Heat stress conditions may develop. Farmers should conserve water and implement drought mitigation strategies.",
            severity = AlertSeverity.MEDIUM
        ),
        WeatherSourceItem(
            id = "demo-tma-002",
            sourceName = "TMA",
            sourceUrl = "https://www.meteo.go.tz/",
            title = "Severe Weather Warning - Coastal Regions",
            category = "warning",
            regionText = "Tanga, Pwani, Dar es Salaam",
            cityText = "Tanga, Dar es Salaam",
            dateRange = "Demo period",
            summary = "Heavy rainfall and flood risk over coastal Tanzania. Strong winds expected along the coast.",
            rawText = "The Tanzania Meteorological Authority warns of heavy rainfall and flood risk over coastal regions including Tanga, Pwani, and Dar es Salaam. Strong winds of up to 60km/h are expected. Residents in low-lying areas should move to higher ground.",
            severity = AlertSeverity.HIGH
        ),
        WeatherSourceItem(
            id = "demo-tma-003",
            sourceName = "TMA",
            sourceUrl = "https://www.meteo.go.tz/",
            title = "Agrometeorological Bulletin - Northern Zone",
            category = "agro bulletin",
            regionText = "Kilimanjaro, Arusha, Manyara",
            cityText = "Moshi, Arusha",
            dateRange = "Demo period",
            summary = "Above normal rainfall expected in northern Tanzania. Beneficial for crops but monitor for waterlogging.",
            rawText = "The Northern Zone (Kilimanjaro, Arusha, Manyara) is expected to receive above normal rainfall this period. Conditions are favorable for crop growth but farmers should monitor fields for waterlogging and take preventive measures. Plant disease risk is elevated due to high humidity.",
            severity = AlertSeverity.LOW
        ),
        WeatherSourceItem(
            id = "demo-tma-004",
            sourceName = "TMA",
            sourceUrl = "https://www.meteo.go.tz/",
            title = "General Tanzania Forecast",
            category = "general forecast",
            regionText = "Tanzania",
            cityText = "",
            dateRange = "Demo period",
            summary = "Mixed conditions across Tanzania. Southern highlands may experience early rains.",
            rawText = "Across Tanzania, mixed weather conditions are expected. The southern highlands including Mbeya and Njombe may experience early onset of rains. Lake Victoria regions including Mwanza and Kagera should expect moderate rainfall. Eastern coastal areas remain warm and humid.",
            severity = AlertSeverity.INFO
        )
    )
}
