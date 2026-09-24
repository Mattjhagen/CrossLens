package com.crosslens.app.core.model

/**
 * Represents a demo local location for local news exploration.
 *
 * These are clearly labeled fictional demo locations. In a live product,
 * location availability would depend on actual source coverage and user choice.
 */
data class LocalLocation(
    val id: String,
    val cityName: String,
    val regionName: String,
    val countryCode: String, // ISO 3166-1 alpha-2
    val isDemo: Boolean = true
)

object DemoLocalLocations {
    val SEATTLE = LocalLocation(
        id = "seattle_wa_us",
        cityName = "Seattle",
        regionName = "Washington",
        countryCode = "US"
    )

    val PARIS = LocalLocation(
        id = "paris_idf_fr",
        cityName = "Paris",
        regionName = "Île-de-France",
        countryCode = "FR"
    )

    val LONDON = LocalLocation(
        id = "london_eng_gb",
        cityName = "London",
        regionName = "England",
        countryCode = "GB"
    )

    val TORONTO = LocalLocation(
        id = "toronto_on_ca",
        cityName = "Toronto",
        regionName = "Ontario",
        countryCode = "CA"
    )

    val ALL_DEMO_LOCATIONS = listOf(SEATTLE, PARIS, LONDON, TORONTO)

    fun findById(id: String): LocalLocation? = ALL_DEMO_LOCATIONS.find { it.id == id }
}
