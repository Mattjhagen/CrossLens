package com.crosslens.app.data.repository

/** Publisher location is attribution context, never an ideology or event-location inference. */
object PublisherGeography {
    private val countries = mapOf(
        "United Kingdom" to ("GB" to "europe"), "Qatar" to ("QA" to "middle_east"),
        "Germany" to ("DE" to "europe"), "France" to ("FR" to "europe"),
        "Switzerland" to ("CH" to "europe"), "Spain" to ("ES" to "europe"),
        "Ireland" to ("IE" to "europe"), "United States" to ("US" to "north_america"),
        "Canada" to ("CA" to "north_america"), "Australia" to ("AU" to "oceania"),
        "Japan" to ("JP" to "asia"), "India" to ("IN" to "asia"),
        "Singapore" to ("SG" to "asia"), "South Korea" to ("KR" to "asia"),
        "Saudi Arabia" to ("SA" to "middle_east"), "Hong Kong" to ("HK" to "asia"),
        "South Africa" to ("ZA" to "africa"), "Russia" to ("RU" to "europe")
    )
    fun countryCodes(country: String?): List<String> = countries[country]?.first?.let(::listOf).orEmpty()
    fun regionIds(country: String?): List<String> = countries[country]?.second?.let(::listOf).orEmpty()
}
