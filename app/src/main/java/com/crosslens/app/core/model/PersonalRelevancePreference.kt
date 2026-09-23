package com.crosslens.app.core.model

import java.time.Instant

/**
 * Represents a user's feedback on article relevance.
 *
 * This preference is based on observable article metadata (topic, region) and does not
 * infer political beliefs, sensitive traits, or editorial truth from reading behavior.
 * It's used only to shape future recommendations when that feature is introduced.
 */
data class PersonalRelevancePreference(
    val id: String,
    val preferenceType: PreferenceType,
    val dimensionType: DimensionType,
    val dimensionValue: String, // topic ID or region ID
    val createdTime: Instant
)

enum class PreferenceType {
    MORE,  // Show more like this
    LESS   // Show less like this
}

enum class DimensionType {
    TOPIC,   // Story topic
    REGION   // Geographic region
}

/**
 * Context for creating a preference from an article.
 */
data class ArticleRelevanceContext(
    val topicIds: List<String>,
    val regionIds: List<String>
)
