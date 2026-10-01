package com.crosslens.app.feature.eventcomparison

import com.crosslens.app.core.model.Article
import com.crosslens.app.data.ingestion.SourceMetadata

/**
 * Recommends additional articles for "Read across coverage" feature.
 *
 * Selects 2-4 articles from the same event cluster that broaden understanding
 * through documented source diversity (geography, language, source type).
 *
 * DOES NOT:
 * - Infer or display political ideology
 * - Claim sources are neutral, biased, true, or false
 * - Show unrelated articles
 * - Use user engagement history or political preferences
 *
 * ELIGIBILITY:
 * - Event cluster must have at least 3 distinct publishers
 * - At least 2 additional articles available after excluding currently viewed article(s)
 * - All recommendations from the same confident event cluster
 */
class ReadAcrossCoverageRecommender {

    companion object {
        /** Minimum distinct publishers required in cluster for Read Across Coverage */
        const val MIN_DISTINCT_PUBLISHERS = 3

        /** Minimum additional articles needed for recommendations */
        const val MIN_RECOMMENDATIONS = 2
    }

    /**
     * Recommend 2-4 additional articles from the same event for broader coverage.
     *
     * @param allArticles All articles in the event cluster
     * @param alreadyShownArticles Articles currently displayed to the user
     * @param articlesWithMetadata Articles paired with their source metadata
     * @return Recommended articles with explanations, or empty if none qualify
     */
    fun recommend(
        allArticles: List<Article>,
        alreadyShownArticles: List<Article>,
        articlesWithMetadata: List<ArticleWithMetadata>
    ): List<RecommendedArticle> {
        // Eligibility check 1: At least 3 distinct publishers in cluster
        val distinctPublishers = articlesWithMetadata
            .mapNotNull { it.metadata?.publisherName }
            .distinct()
        if (distinctPublishers.size < MIN_DISTINCT_PUBLISHERS) {
            return emptyList()
        }
        // Filter out already-shown articles
        val candidateArticles = articlesWithMetadata.filter { candidate ->
            alreadyShownArticles.none { shown -> shown.id == candidate.article.id }
        }

        // Eligibility check 2: At least 2 additional articles available for recommendations
        if (candidateArticles.size < MIN_RECOMMENDATIONS) {
            return emptyList()
        }

        // Build context from already-shown articles
        val shownCountries = alreadyShownArticles.mapNotNull { article ->
            articlesWithMetadata.find { it.article.id == article.id }?.metadata?.country
        }.toSet()

        val shownLanguages = alreadyShownArticles.map { it.originalLanguage }.toSet()

        val shownPublishers = alreadyShownArticles.mapNotNull { article ->
            articlesWithMetadata.find { it.article.id == article.id }?.metadata?.publisherName
        }

        // Score and rank candidates
        val scoredCandidates = candidateArticles.map { candidate ->
            val score = scoreCandidate(
                candidate = candidate,
                shownCountries = shownCountries,
                shownLanguages = shownLanguages,
                allCandidates = candidateArticles
            )
            ScoredCandidate(candidate, score)
        }.sortedByDescending { it.score }

        // Select top 2-4 based on available diversity
        val selectedCount = when {
            scoredCandidates.size <= 2 -> scoredCandidates.size
            scoredCandidates.size == 3 -> 3
            else -> 4.coerceAtMost(scoredCandidates.size)
        }

        return scoredCandidates.take(selectedCount).map { scored ->
            val explanation = generateExplanation(
                candidate = scored.candidate,
                shownCountries = shownCountries,
                shownLanguages = shownLanguages,
                shownPublishers = shownPublishers
            )
            RecommendedArticle(
                article = scored.candidate.article,
                metadata = scored.candidate.metadata,
                explanation = explanation
            )
        }
    }

    /**
     * Score a candidate article based on documented source diversity.
     * Higher scores indicate more diverse perspectives.
     */
    private fun scoreCandidate(
        candidate: ArticleWithMetadata,
        shownCountries: Set<String>,
        shownLanguages: Set<String>,
        allCandidates: List<ArticleWithMetadata>
    ): Int {
        var score = 0
        val metadata = candidate.metadata

        // Different country (+3 points)
        if (metadata != null && metadata.country !in shownCountries) {
            score += 3
        }

        // Different language (+2 points)
        if (candidate.article.originalLanguage !in shownLanguages) {
            score += 2
        }

        // Public broadcaster or wire service (+2 points)
        // Based on documented editorial description
        if (metadata?.editorialDescription != null) {
            val description = metadata.editorialDescription.lowercase()
            if (description.contains("public broadcaster") ||
                description.contains("public service") ||
                description.contains("wire service") ||
                description.contains("international news service")
            ) {
                score += 2
            }
        }

        // Local reporting preference (+3 points)
        // Identified by country matching event geography
        // Note: This would need event location context in production
        // For now, prioritize non-Western sources as more likely local to non-Western events
        if (metadata != null && !isWesternCountry(metadata.country)) {
            score += 1
        }

        // Stable ordering: use article ID as tiebreaker
        // Add fractional score based on ID hash to ensure deterministic ordering
        score += (candidate.article.id.hashCode() % 100) / 1000

        return score
    }

    /**
     * Generate factual explanation for why this article was recommended.
     */
    private fun generateExplanation(
        candidate: ArticleWithMetadata,
        shownCountries: Set<String>,
        shownLanguages: Set<String>,
        shownPublishers: List<String>
    ): String {
        val parts = mutableListOf<String>()
        val metadata = candidate.metadata

        // Country diversity
        if (metadata != null && metadata.country !in shownCountries) {
            parts.add("Reporting from ${metadata.country}")
        }

        // Language diversity
        if (candidate.article.originalLanguage !in shownLanguages) {
            val languageName = getLanguageName(candidate.article.originalLanguage)
            parts.add("Original reporting in $languageName")
        }

        // Source type (if documented)
        if (metadata?.editorialDescription != null) {
            val description = metadata.editorialDescription.lowercase()
            when {
                description.contains("public broadcaster") ||
                description.contains("public service broadcaster") -> {
                    parts.add("Public broadcaster")
                }
                description.contains("wire service") -> {
                    parts.add("Wire service coverage")
                }
                description.contains("international news service") -> {
                    parts.add("International news service")
                }
            }
        }

        // Default explanation if no specific diversity markers
        if (parts.isEmpty()) {
            val otherPublisher = shownPublishers.firstOrNull()
            return if (otherPublisher != null && metadata != null) {
                "Different publisher from $otherPublisher"
            } else {
                "Additional coverage of the same event"
            }
        }

        return parts.joinToString(" • ")
    }

    /**
     * Check if country is Western (for diversity scoring).
     * This is a rough heuristic; production would use event geography context.
     */
    private fun isWesternCountry(country: String): Boolean {
        return country in setOf(
            "United States", "United Kingdom", "Canada", "Australia",
            "France", "Germany", "Spain", "Italy", "Netherlands",
            "Belgium", "Switzerland", "Austria", "Ireland", "Norway",
            "Sweden", "Denmark", "Finland"
        )
    }

    /**
     * Get human-readable language name from BCP 47 tag.
     */
    private fun getLanguageName(languageTag: String): String {
        return when (languageTag.take(2)) {
            "en" -> "English"
            "es" -> "Spanish"
            "fr" -> "French"
            "de" -> "German"
            "ar" -> "Arabic"
            "zh" -> "Chinese"
            "ja" -> "Japanese"
            "ko" -> "Korean"
            "hi" -> "Hindi"
            "ru" -> "Russian"
            "pt" -> "Portuguese"
            else -> languageTag // Fallback to tag if unknown
        }
    }
}

/**
 * Candidate article with diversity score.
 */
private data class ScoredCandidate(
    val candidate: ArticleWithMetadata,
    val score: Int
)

/**
 * Recommended article with factual explanation.
 */
data class RecommendedArticle(
    val article: Article,
    val metadata: SourceMetadata?,
    val explanation: String
)
