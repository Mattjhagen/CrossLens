package com.crosslens.app.core.model

/**
 * A story recommended for a specific user with an explanation.
 *
 * This represents the transparent demo preview of personalized ordering.
 * The explanation must be derived from explicit, observable metadata only.
 */
data class PersonalizedRecommendation(
    val story: Story,
    val explanation: String, // "Why you're seeing this" - must be metadata-based
    val score: Int // Internal relevance score for deterministic ordering
)

/**
 * Result of checking personalization eligibility and generating recommendations.
 */
data class PersonalizationResult(
    val isEligible: Boolean, // Has explicit preferences or location
    val recommendations: List<PersonalizedRecommendation>
)
