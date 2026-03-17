package com.milanwouters.weatherapp.util

/**
 * Fixed list of Tanzania regions and their main cities/towns.
 * Farmers must select from these lists during registration.
 * This ensures weather alerts can be properly matched to farmer locations.
 */
object TanzaniaRegions {

    val regionCityMap: Map<String, List<String>> = mapOf(
        "Arusha" to listOf("Arusha", "Karatu", "Monduli", "Longido", "Ngorongoro"),
        "Dar es Salaam" to listOf("Dar es Salaam", "Kinondoni", "Ilala", "Temeke", "Ubungo", "Kigamboni"),
        "Dodoma" to listOf("Dodoma", "Kondoa", "Mpwapwa", "Bahi", "Chamwino"),
        "Geita" to listOf("Geita", "Chato", "Nyang'hwale", "Mbogwe", "Bukombe"),
        "Iringa" to listOf("Iringa", "Kilolo", "Mufindi", "Ihethe"),
        "Kagera" to listOf("Bukoba", "Biharamulo", "Muleba", "Karagwe", "Kyerwa"),
        "Katavi" to listOf("Mpanda", "Mlele", "Nsimbo"),
        "Kigoma" to listOf("Kigoma", "Kasulu", "Kibondo", "Buhigwe", "Kakonko"),
        "Kilimanjaro" to listOf("Moshi", "Same", "Hai", "Rombo", "Siha", "Mwanga"),
        "Lindi" to listOf("Lindi", "Kilwa", "Nachingwea", "Ruangwa", "Liwale"),
        "Manyara" to listOf("Babati", "Hanang", "Kiteto", "Mbulu", "Simanjiro"),
        "Mara" to listOf("Musoma", "Tarime", "Bunda", "Rorya", "Serengeti", "Butiama"),
        "Mbeya" to listOf("Mbeya", "Tukuyu", "Chunya", "Mbarali", "Rungwe"),
        "Morogoro" to listOf("Morogoro", "Ifakara", "Kilosa", "Mvomero", "Kilombero", "Ulanga", "Gairo"),
        "Mtwara" to listOf("Mtwara", "Masasi", "Newala", "Tandahimba", "Nanyamba"),
        "Mwanza" to listOf("Mwanza", "Sengerema", "Magu", "Ilemela", "Kwimba", "Ukerewe"),
        "Njombe" to listOf("Njombe", "Makete", "Ludewa", "Wanging'ombe"),
        "Pwani" to listOf("Kibaha", "Bagamoyo", "Mkuranga", "Kisarawe", "Rufiji", "Mafia"),
        "Rukwa" to listOf("Sumbawanga", "Nkasi", "Kalambo"),
        "Ruvuma" to listOf("Songea", "Mbinga", "Nyasa", "Madaba", "Namtumbo"),
        "Shinyanga" to listOf("Shinyanga", "Kahama", "Kishapu", "Ushetu"),
        "Simiyu" to listOf("Bariadi", "Busega", "Itilima", "Maswa", "Meatu"),
        "Singida" to listOf("Singida", "Manyoni", "Iramba", "Ikungi"),
        "Songwe" to listOf("Vwawa", "Ileje", "Mbozi", "Momba"),
        "Tabora" to listOf("Tabora", "Urambo", "Igunga", "Sikonge", "Nzega"),
        "Tanga" to listOf("Tanga", "Lushoto", "Handeni", "Korogwe", "Kilindi", "Muheza", "Pangani"),
        "Zanzibar North" to listOf("Mkokotoni", "Mahonda", "Kivunge"),
        "Zanzibar South" to listOf("Koani", "Mkoani", "Chake Chake"),
        "Zanzibar Urban/West" to listOf("Zanzibar City", "Stone Town", "Fuoni", "Bububu")
    )

    val regions: List<String> get() = regionCityMap.keys.sorted()

    fun getCitiesForRegion(region: String): List<String> =
        regionCityMap[region] ?: emptyList()

    /** Check if a piece of text mentions a given region or any of its cities */
    fun textMatchesRegion(text: String, region: String): Boolean {
        val lower = text.lowercase()
        if (lower.contains(region.lowercase())) return true
        val cities = regionCityMap[region] ?: return false
        return cities.any { lower.contains(it.lowercase()) }
    }

    /** Find all regions mentioned in a text block */
    fun findMentionedRegions(text: String): List<String> {
        return regions.filter { textMatchesRegion(text, it) }
    }
}
