package com.crosslens.app.data.recommendation

import com.crosslens.app.core.model.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Transparent, offline-only demo recommendation engine.
 *
 * Ranks stories using ONLY explicit metadata-based signals:
 * - Topic preferences (MORE/LESS)
 * - Region preferences (MORE/LESS)
 * - Selected demo local location
 *
 * Does NOT use:
 * - Passive reading behavior (time, clicks, scrolling)
 * - Device data or analytics
 * - Network signals
 * - Inferred political beliefs or sensitive traits
 * - Editorial reliability or truthfulness judgments
 *
 * This is a local demo preview that does not affect evidence display,
 * framing analysis, Lens Gap scores, or editorial review decisions.
 */
@Singleton
class PersonalizedRecommendationEngine @Inject constructor() {

    /**
     * Check if user is eligible for personalized recommendations.
     *
     * Eligible when:
     * - Has at least one "MORE" preference OR
     * - Has selected a demo local location
     */
    fun isEligible(
        preferences: List<PersonalRelevancePreference>,
        demoLocalLocation: String?
    ): Boolean {
        val hasMorePreference = preferences.any { it.preferenceType == PreferenceType.MORE }
        val hasLocation = demoLocalLocation != null
        return hasMorePreference || hasLocation
    }

    /**
     * Generate personalized recommendations for eligible users.
     *
     * Returns empty list if user is not eligible.
     * Excludes stories matching LESS preferences.
     * Ranks remaining stories by metadata match score.
     * Generates transparent "Why you're seeing this" explanations.
     */
    fun generateRecommendations(
        allStories: List<Story>,
        preferences: List<PersonalRelevancePreference>,
        demoLocalLocation: String?
    ): PersonalizationResult {
        // Check eligibility
        if (!isEligible(preferences, demoLocalLocation)) {
            return PersonalizationResult(isEligible = false, recommendations = emptyList())
        }

        // Get LESS preferences to exclude
        val lessTopics = preferences.filter {
            it.preferenceType == PreferenceType.LESS && it.dimensionType == DimensionType.TOPIC
        }.map { it.dimensionValue }.toSet()

        val lessRegions = preferences.filter {
            it.preferenceType == PreferenceType.LESS && it.dimensionType == DimensionType.REGION
        }.map { it.dimensionValue }.toSet()

        // Filter out stories matching LESS preferences
        val candidateStories = allStories.filter { story ->
            val matchesLessTopic = story.topicIds.any { it in lessTopics }
            val matchesLessRegion = story.eventCountryCodes.any { it in lessRegions }
            !matchesLessTopic && !matchesLessRegion
        }

        // Score and explain each story
        val recommendations = candidateStories.mapNotNull { story ->
            val (score, explanation) = scoreAndExplain(story, preferences, demoLocalLocation)
            if (score > 0) {
                PersonalizedRecommendation(
                    story = story,
                    explanation = explanation,
                    score = score
                )
            } else null
        }

        // Sort by score descending (highest relevance first)
        val sortedRecommendations = recommendations.sortedByDescending { it.score }

        // Limit to top 3-5 recommendations
        val topRecommendations = sortedRecommendations.take(5)

        return PersonalizationResult(
            isEligible = true,
            recommendations = topRecommendations
        )
    }

    /**
     * Score a story and generate explanation based on metadata matches.
     *
     * Scoring:
     * - Topic match: +10 points
     * - Region/country match: +5 points
     * - Local location match: +15 points (strongest signal)
     *
     * Explanation: Picks the highest-weighted reason.
     */
    private fun scoreAndExplain(
        story: Story,
        preferences: List<PersonalRelevancePreference>,
        demoLocalLocation: String?
    ): Pair<Int, String> {
        var score = 0
        val reasons = mutableListOf<Pair<Int, String>>() // (weight, reason)

        // Check topic matches
        val moreTopics = preferences.filter {
            it.preferenceType == PreferenceType.MORE && it.dimensionType == DimensionType.TOPIC
        }
        for (pref in moreTopics) {
            if (story.topicIds.contains(pref.dimensionValue)) {
                score += 10
                reasons.add(10 to "Matches your interest in ${formatTopicId(pref.dimensionValue)}")
            }
        }

        // Check region/country matches
        val moreRegions = preferences.filter {
            it.preferenceType == PreferenceType.MORE && it.dimensionType == DimensionType.REGION
        }
        for (pref in moreRegions) {
            if (story.eventCountryCodes.contains(pref.dimensionValue)) {
                score += 5
                reasons.add(5 to "Matches your interest in ${pref.dimensionValue} coverage")
            }
        }

        // Check local location match (highest weight)
        if (demoLocalLocation != null) {
            // For demo, check if story is from the selected location
            // Location stories would have a specific marker or be in a local collection
            // For now, we'll use a simple heuristic based on story metadata
            val locationInfo = when (demoLocalLocation) {
                "seattle_wa_us" -> Pair("Seattle", "US")
                "paris_idf_fr" -> Pair("Paris", "FR")
                "london_eng_gb" -> Pair("London", "GB")
                "toronto_on_ca" -> Pair("Toronto", "CA")
                else -> null
            }

            if (locationInfo != null) {
                val (cityName, countryCode) = locationInfo
                // Check if story is about this location
                val matchesLocation = story.eventCountryCodes.contains(countryCode) &&
                        (story.title.contains(cityName, ignoreCase = true) ||
                                story.summary.contains(cityName, ignoreCase = true))

                if (matchesLocation) {
                    score += 15
                    reasons.add(15 to "Matches your $cityName local-news selection")
                }
            }
        }

        // Pick highest-weighted reason for explanation
        val bestReason = reasons.maxByOrNull { it.first }?.second
                ?: "Recommended for you" // Fallback (shouldn't happen if score > 0)

        return Pair(score, bestReason)
    }

    /**
     * Format topic ID for display in explanations.
     * Converts internal IDs like "tech" or "climate" to readable form.
     */
    private fun formatTopicId(topicId: String): String {
        return when (topicId) {
            "tech" -> "Technology"
            "climate" -> "Climate & Environment"
            "politics" -> "Politics"
            "business" -> "Business"
            "health" -> "Health"
            "science" -> "Science"
            else -> topicId.replaceFirstChar { it.uppercase() }
        }
    }
}
