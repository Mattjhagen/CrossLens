package com.crosslens.app.data.ingestion

import java.time.Duration
import java.time.Instant

/**
 * Cross-language event matcher using entity overlap.
 *
 * IMPORTANT: This produces CANDIDATE event matches requiring editorial review.
 * Entity overlap suggests articles may cover the same event, but:
 * - Does NOT confirm they are the same event
 * - Does NOT determine factuality or accuracy
 * - Does NOT determine independent vs. syndicated reporting
 * - Does NOT determine political orientation or bias
 * - Does NOT calculate Lens Gap
 *
 * Multilingual matching is inherently uncertain. Shared entities may indicate:
 * - Same event, independent reporting (desired match)
 * - Same event, syndicated wire copy (should be marked via syndication detection)
 * - Different events sharing entities (false positive)
 * - Generic entities coincidentally shared (false positive)
 *
 * Editorial review is REQUIRED for all cross-language candidate matches.
 */
class CrossLanguageEventMatcher(
    private val timeWindow: Duration = Duration.ofHours(72),
    private val minimumSharedEntities: Int = 2,
    private val minimumEntityOverlap: Double = 0.3
) {
    /**
     * Find candidate event matches across languages using entity overlap.
     * Returns groups of articles that may cover the same event based on shared entities.
     */
    fun findCandidateMatches(articlesWithEntities: List<ArticleWithEntities>): CrossLanguageMatchResult {
        if (articlesWithEntities.size < 2) {
            return CrossLanguageMatchResult(
                candidateMatches = emptyList(),
                unmatchedArticleIds = articlesWithEntities.map { it.article.id }
            )
        }

        val groups = mutableListOf<CandidateEventMatch>()
        val assigned = mutableSetOf<String>()

        // Find articles with shared entities across different languages
        articlesWithEntities.forEachIndexed { i, candidate ->
            if (candidate.article.id in assigned) return@forEachIndexed
            if (candidate.entities.isEmpty()) return@forEachIndexed

            val matches = mutableListOf(candidate)
            val candidateEntityIds = candidate.entities.map { it.id }.toSet()

            articlesWithEntities.forEachIndexed { j, other ->
                if (j <= i || other.article.id in assigned) return@forEachIndexed
                if (other.entities.isEmpty()) return@forEachIndexed

                // Check language difference
                if (candidate.article.languageTag == other.article.languageTag) {
                    // Same language articles use existing title similarity clustering
                    return@forEachIndexed
                }

                // Check time window
                val timeDiff = Duration.between(candidate.article.publishedAt, other.article.publishedAt).abs()
                if (timeDiff > timeWindow) return@forEachIndexed

                // Calculate entity overlap
                val otherEntityIds = other.entities.map { it.id }.toSet()
                val sharedEntityIds = candidateEntityIds.intersect(otherEntityIds)
                val overlapScore = if (candidateEntityIds.isEmpty() || otherEntityIds.isEmpty()) {
                    0.0
                } else {
                    sharedEntityIds.size.toDouble() / minOf(candidateEntityIds.size, otherEntityIds.size)
                }

                if (sharedEntityIds.size >= minimumSharedEntities && overlapScore >= minimumEntityOverlap) {
                    matches += other
                }
            }

            if (matches.size >= 2) {
                val allArticles = matches.map { it.article }
                val allLanguages = allArticles.map { it.languageTag }.distinct()
                val allEntityIds = matches.flatMap { it.entities.map { e -> e.id } }
                val sharedEntityIds = allEntityIds.groupBy { it }
                    .filter { it.value.size >= 2 }
                    .keys.toList()

                val confidence = calculateConfidence(
                    sharedEntityIds = sharedEntityIds,
                    articles = allArticles,
                    languages = allLanguages
                )

                groups += CandidateEventMatch(
                    id = "cross-lang-${groups.size + 1}",
                    articleIds = allArticles.map { it.id },
                    sourceIds = allArticles.map { it.sourceId }.distinct(),
                    languages = allLanguages,
                    sharedEntities = sharedEntityIds,
                    confidence = confidence,
                    matchingMethod = "entity-overlap-v1",
                    requiresEditorialReview = true,
                    rationale = buildRationale(sharedEntityIds, allLanguages, confidence),
                    uncertaintyReasons = buildUncertaintyReasons(allArticles, sharedEntityIds, allLanguages)
                )

                matches.forEach { assigned += it.article.id }
            }
        }

        val unmatchedIds = articlesWithEntities
            .map { it.article.id }
            .filterNot { it in assigned }

        return CrossLanguageMatchResult(
            candidateMatches = groups,
            unmatchedArticleIds = unmatchedIds
        )
    }

    private fun calculateConfidence(
        sharedEntityIds: List<String>,
        articles: List<NormalizedArticle>,
        languages: List<String>
    ): EntityMatchConfidence {
        // Check for high-signal event identifier
        val hasEventIdentifier = sharedEntityIds.any { it.startsWith("event:") }
        if (hasEventIdentifier && sharedEntityIds.size >= 3) {
            return EntityMatchConfidence.HIGH
        }

        // Check for strong entity overlap (multiple people/orgs/locations)
        val hasPerson = sharedEntityIds.any { it.startsWith("person:") }
        val hasOrg = sharedEntityIds.any { it.startsWith("org:") }
        val hasLocation = sharedEntityIds.any { it.startsWith("location:") }
        val diverseEntityTypes = listOf(hasPerson, hasOrg, hasLocation).count { it }

        if (diverseEntityTypes >= 2 && sharedEntityIds.size >= 4) {
            return EntityMatchConfidence.MEDIUM
        }

        if (sharedEntityIds.size >= 3 && diverseEntityTypes >= 2) {
            return EntityMatchConfidence.MEDIUM
        }

        // Generic entities or weak overlap
        val hasGenericLocation = sharedEntityIds.any {
            it in listOf("location:washington", "location:beijing", "location:brussels")
        }

        if (hasGenericLocation && sharedEntityIds.size < 4) {
            return EntityMatchConfidence.LOW
        }

        // Default: low confidence for minimal overlap
        if (sharedEntityIds.size >= 2) {
            return EntityMatchConfidence.LOW
        }

        return EntityMatchConfidence.UNCERTAIN
    }

    private fun buildRationale(
        sharedEntityIds: List<String>,
        languages: List<String>,
        confidence: EntityMatchConfidence
    ): String {
        val entityCount = sharedEntityIds.size
        val languageList = languages.sorted().joinToString(", ")

        return when (confidence) {
            EntityMatchConfidence.HIGH ->
                "High confidence candidate: $entityCount shared entities including event identifier across $languageList. Likely same event."
            EntityMatchConfidence.MEDIUM ->
                "Medium confidence candidate: $entityCount shared entities across $languageList. Possible same event, review for confirmation."
            EntityMatchConfidence.LOW ->
                "Low confidence candidate: $entityCount shared entities across $languageList. Weak overlap, may be different events."
            EntityMatchConfidence.UNCERTAIN ->
                "Uncertain candidate: Minimal entity overlap across $languageList. Editorial review required to determine relationship."
        }
    }

    private fun buildUncertaintyReasons(
        articles: List<NormalizedArticle>,
        sharedEntityIds: List<String>,
        languages: List<String>
    ): List<String> {
        val reasons = mutableListOf<String>()

        reasons += "Cross-language match requires editorial verification"

        if (sharedEntityIds.size < 3) {
            reasons += "Minimal entity overlap (${sharedEntityIds.size} shared entities)"
        }

        val hasGenericEntity = sharedEntityIds.any {
            it.startsWith("location:") && it in listOf(
                "location:washington", "location:beijing", "location:brussels",
                "location:london", "location:paris", "location:moscow"
            )
        }
        if (hasGenericEntity) {
            reasons += "Generic entities may appear in unrelated events"
        }

        if (languages.size > 2) {
            reasons += "Multiple languages (${languages.size}) increase ambiguity"
        }

        return reasons
    }
}

/**
 * Candidate event match found via cross-language entity overlap.
 *
 * IMPORTANT: This is a CANDIDATE match requiring editorial review.
 * It is NOT a confirmed same-event determination.
 */
data class CandidateEventMatch(
    val id: String,
    val articleIds: List<String>,
    val sourceIds: List<String>,
    val languages: List<String>,
    val sharedEntities: List<String>,
    val confidence: EntityMatchConfidence,
    val matchingMethod: String,
    val requiresEditorialReview: Boolean,
    val rationale: String,
    val uncertaintyReasons: List<String>
)

/**
 * Confidence bands for cross-language entity matches.
 *
 * HIGH: Event identifier + strong entity overlap (likely same event)
 * MEDIUM: Multiple diverse entities (person + org + location)
 * LOW: Minimal overlap or generic entities
 * UNCERTAIN: Insufficient evidence for confident candidate
 */
enum class EntityMatchConfidence {
    HIGH,
    MEDIUM,
    LOW,
    UNCERTAIN
}

/**
 * Result of cross-language matching on a batch of articles.
 */
data class CrossLanguageMatchResult(
    val candidateMatches: List<CandidateEventMatch>,
    val unmatchedArticleIds: List<String>
)
