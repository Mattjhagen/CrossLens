package com.crosslens.app.core.model

import java.time.Instant

data class UserPreferences(
    val readingLanguage: String, // BCP 47
    val homeCountry: String?, // ISO 3166-1 alpha-2
    val homeRegion: String?,
    val enabledSourceIds: Set<String>,
    val translationPreference: TranslationPreference,
    val theme: Theme,
    val reducedMotion: Boolean,
    val demoLocalLocation: String? = null, // LocalLocation.id for demo local news
    val showLocalOnly: Boolean = false, // Local filter toggle state
    val lastRefreshedTime: Instant? = null, // Last time demo data was refreshed
    val showForYou: Boolean = true // Show personalized For You section when eligible
)

enum class TranslationPreference {
    AUTO,
    ORIGINAL_ONLY,
    TRANSLATED_PREFERRED
}

enum class Theme {
    SYSTEM,
    LIGHT,
    DARK
}
