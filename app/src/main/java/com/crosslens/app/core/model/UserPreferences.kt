package com.crosslens.app.core.model

data class UserPreferences(
    val readingLanguage: String, // BCP 47
    val homeCountry: String?, // ISO 3166-1 alpha-2
    val homeRegion: String?,
    val enabledSourceIds: Set<String>,
    val translationPreference: TranslationPreference,
    val theme: Theme,
    val reducedMotion: Boolean
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
