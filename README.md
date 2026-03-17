# TZ Weather Alert Console

**An Android prototype for a low-tech weather alert system for Tanzanian farmers.**

---

## Problem Statement

Farmers in Tanzania often receive weather warnings too late — or not at all. Dangerous weather events such as heavy rainfall, flooding, drought, and heat stress can devastate harvests and livelihoods. Most rural farmers:

- Use old or basic mobile phones
- Have limited storage and processing power
- Have poor or no internet connectivity
- Have limited digital literacy
- Cannot afford or install smartphone apps

## Doelgroep / Target Personas

| Persona | Description |
|---------|-------------|
| Juma Hassan | Small-scale farmer in Dodoma. Uses a basic Android phone. Speaks Swahili. Needs flood warnings before rainy season. |
| Amina Mwangi | Rice farmer in Ifakara, Morogoro. Relies on seasonal rain. Wants daily weather updates. |
| Peter Kimaro | Coffee farmer near Moshi, Kilimanjaro. Has some English. Wants only critical alerts. |

---

## Why No Consumer App?

The end-users (farmers) do **not** install any app. Most don't have smartphones capable of running modern apps. Installing and maintaining an app requires:
- Stable internet for download
- Sufficient storage
- Regular updates
- Technical skills

**This Android project is a management console / demonstration environment** — not a farmer-facing app. It is operated by agricultural extension officers or NGO staff to:
- Register farmers
- Manage subscriptions
- Monitor system status
- Demonstrate the full data flow

---

## Why SMS?

SMS is the ideal delivery channel for rural Tanzania because:

| Factor | SMS | App |
|--------|-----|-----|
| Works on basic phones | ✓ | ✗ |
| No internet needed | ✓ | ✗ |
| No app install | ✓ | ✗ |
| Works with feature phones | ✓ | ✗ |
| Low cost per message | ✓ | — |
| Wide network coverage | ✓ | Limited |

In production, the system would use **Africa's Talking** (africastalking.com), an SMS API specifically designed for East African markets with excellent Tanzania coverage.

---

## Why TMA as Primary Source?

**Tanzania Meteorological Authority (TMA)** — [meteo.go.tz](https://www.meteo.go.tz/) — is used as the **primary data source** because:

1. **Official authority**: TMA is the national meteorological agency of Tanzania, with direct access to observation stations across all 29 regions.
2. **Tanzania-specific**: All forecasts are tailored to Tanzanian regions, agrozones, and crop calendars.
3. **Agrometeorological bulletins**: TMA publishes specialized agricultural weather advice, including crop-specific recommendations.
4. **Reliable source**: Used by government, NGOs, and international organizations operating in Tanzania.
5. **Free public access**: Forecast data is publicly available without API keys.

**ICPAC** (icpac.net/weekly-forecast/) is used only as an **optional regional fallback** when TMA data is insufficient. ICPAC covers the broader East Africa region and provides context but lacks Tanzania-specific granularity.

---

## How Scraping Works

```
TmaScraperService
    ↓
OkHttp HTTP request → meteo.go.tz pages
    ↓
Jsoup HTML parser
    ↓
4-strategy parsing:
  1. Forecast/warning cards (CSS selectors)
  2. Main content area (paragraph splitting)
  3. Headings + adjacent content
  4. Page title + meta description
    ↓
WeatherSourceItem objects
    ↓
Room cache (last successful data)
```

**Robustness**: The scraper uses multiple fallback strategies and does not crash if TMA changes its HTML structure. Each strategy is tried independently.

---

## Region & City Matching

When a farmer registers, they must select:
1. **Region** (from a fixed list of all 29 Tanzania regions)
2. **Nearest city** (from a fixed list per region)

This hierarchical selection ensures:
- No typos or ambiguous location names
- Deterministic matching against forecast text
- Accurate alert targeting

When an alert is generated, `TanzaniaRegions.textMatchesRegion()` checks whether the forecast text mentions the farmer's region or city. This uses case-insensitive substring matching across all region names and known cities.

---

## Data Flow

```
INPUT
  ├── TMA forecast (live scrape from meteo.go.tz)
  ├── Farmer registration (name, phone, region, city)
  └── Incoming SMS commands (START/DAILY/ALERT/STOP/...)

PROCESSING
  ├── HTML fetching (OkHttp)
  ├── HTML parsing (Jsoup, 4 strategies)
  ├── Keyword detection → AlertType classification
  ├── Region text extraction
  ├── Farmer-alert region matching
  ├── Subscription filter (DAILY/ALERT_ONLY/WEEKLY)
  └── Room cache update

OUTPUT
  ├── Targeted SMS to matched farmers (FakeSmsGateway → Africa's Talking in production)
  ├── Dashboard stats update
  └── Outbox log entry
```

---

## ITF Technology Pillars

| Pillar | Implementation |
|--------|----------------|
| **Frontend / UI** | Jetpack Compose + Material 3, sober field-console aesthetic |
| **Business Logic** | AlertParser, SendAlertsUseCase, SmsCommandParser, region matching |
| **Data Layer** | Room (SQLite), repositories, OkHttp + Jsoup scraping, local cache |
| **Architecture** | MVVM + Clean Architecture (domain / data separation) |
| **Async / State** | Kotlin Coroutines + StateFlow, ViewModelScope |
| **Dependency Injection** | Hilt (Dagger 2), singleton-scoped services |
| **Navigation** | Navigation Compose, 8-screen bottom navigation |

---

## Project Structure

```
app/src/main/java/com/milanwouters/weatherapp/
├── data/
│   ├── local/          # Room database, DAOs, entities
│   ├── repository/     # Repository implementations
│   └── model/          # Mappers (toDomain / toEntity)
├── domain/
│   ├── model/          # Farmer, WeatherAlert, SmsMessage, etc.
│   ├── repository/     # Repository interfaces
│   ├── usecase/        # Use cases (AddFarmer, RefreshWeather, SendAlerts, ...)
│   └── rules/          # Alert detection rules
├── scraper/            # TmaScraperService, TmaWeatherRepository, IcpacFallback
├── sms/                # SmsGateway, FakeSmsGateway, SmsCommandParser
├── util/               # TanzaniaRegions, SeedData
├── di/                 # Hilt modules (DatabaseModule, RepositoryModule)
├── navigation/         # AppNavHost, Screen sealed class
└── ui/
    ├── theme/          # Color, Typography, Theme
    ├── components/     # Reusable composables
    ├── screens/        # 8 screens
    └── viewmodel/      # ViewModels (Dashboard, Farmers, Weather, Sms, Alerts)
```

---

## Demo Walkthrough (10 minutes)

1. **Launch app** → Dashboard loads automatically, attempts TMA live scrape
2. **Add a farmer** → Farmers tab → Add Farmer → Fill name, phone, select region (e.g. Morogoro) → select city (e.g. Ifakara) → Register
3. **Simulate incoming SMS** → SMS Inbox tab → Type phone + "ALERT" → Send → System logs inbound, replies with confirmation
4. **Refresh weather** → Weather tab → Tap "REFRESH FORECAST" → Watch status change from LOADING → LIVE or CACHED/DEMO
5. **View alerts** → Alerts tab → See detected alert types (HEAVY_RAIN, FLOOD_RISK, etc.) → Tap alert to expand details + affected farmers
6. **Send alerts** → Tap "SEND ALL ALERTS" → System matches farmers to alerts by region → SMS Outbox fills up
7. **View outbox** → SMS Outbox tab → See all outgoing SMS messages with farmer names and alert types
8. **System flow** → System Flow tab → Visual overview of INPUT → PROCESSING → OUTPUT with ITF pillars

---

## SMS Commands Reference

| Command | Action |
|---------|--------|
| `START` | Register / reactivate subscription |
| `STOP` | Unsubscribe from alerts |
| `DAILY` | Switch to daily weather updates |
| `ALERT` | Switch to alerts-only mode |
| `WEEKLY` | Switch to weekly summary |
| `REGION <name>` | Update region (e.g. `REGION DODOMA`) |
| `CITY <name>` | Update nearest city (e.g. `CITY MPWAPWA`) |
| `LANG EN` / `LANG SW` | Set language (English / Swahili) |
| `STATUS` | Get current subscription info |
| `HELP` | Get list of available commands |

---

## Extending to Swahili

The `Language` enum supports both English (`en`) and Swahili (`sw`). SMS messages check `farmer.language` and can render Swahili alternatives. To fully localize:

1. Add Swahili strings to `strings.xml` with locale `sw`
2. Extend `FakeSmsGateway.buildAlertMessage()` with full Swahili translations
3. Extend `SmsCommandParser.generateHelpResponse()` Swahili branch

---

## Production Deployment Notes

- Replace `FakeSmsGateway` with Africa's Talking SMS API implementation
- Add a background `WorkManager` job for periodic TMA scraping (every 6–12 hours)
- Add push notification to extension officers when CRITICAL alerts are detected
- Deploy SMS short code (e.g. 15XXX) through Tanzania Communications Regulatory Authority (TCRA)
- Consider USSD as an alternative registration channel for feature phones

---

*Built as a prototype / ITF demonstration. Data source: Tanzania Meteorological Authority (meteo.go.tz).*
