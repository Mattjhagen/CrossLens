package com.crosslens.app.data.ingestion

import java.security.MessageDigest
import java.util.Locale

/**
 * Detects suspected wire-copy and syndicated content across articles from different sources.
 *
 * IMPORTANT: This detector makes NO claims about confirmed syndication unless explicit wire
 * attribution is present. Cross-domain matches are labeled as SUSPECTED until editorial review.
 * All articles and their source attributions are preserved; detection results are transparent
 * evidence for reviewers, not automatic proof of non-independence.
 *
 * Detection runs after canonical URL deduplication and before event clustering. It uses:
 * - Normalized title exact match (high confidence)
 * - Excerpt fingerprint overlap (medium-to-high confidence)
 * - Canonical URL (handled by prior deduplication step)
 *
 * Multilingual matching is conservative: different languages trigger UNCERTAIN confidence unless
 * very strong evidence exists (exact title translation + high excerpt overlap).
 */
class SyndicationDetector(
    private val minimumExcerptLength: Int = 100,
    private val excerptOverlapThreshold: Double = 0.80
) {
    /**
     * Analyze articles for suspected syndication relationships.
     * Returns groups of articles that share strong similarity evidence.
     * Articles not in any group are considered independent reporting.
     */
    fun analyze(articles: List<NormalizedArticle>): SyndicationAnalysisResult {
        if (articles.size < 2) {
            return SyndicationAnalysisResult(
                analyzedCount = articles.size,
                syndicationGroups = emptyList(),
                independentArticleIds = articles.map { it.id }
            )
        }

        val groups = mutableListOf<SyndicationGroup>()
        val assigned = mutableSetOf<String>()

        // Group 1: Exact normalized title match across different sources
        val byNormalizedTitle = articles
            .filterNot { it.id in assigned }
            .groupBy { normalizeTitle(it.headline) }
            .filter { (_, articles) -> articles.size > 1 && articles.map { it.sourceId }.distinct().size > 1 }

        byNormalizedTitle.forEach { (normalizedTitle, matches) ->
            val distinctSources = matches.map { it.sourceId }.distinct()
            if (distinctSources.size > 1 && normalizedTitle.isNotBlank()) {
                val languages = matches.map { it.languageTag }.distinct()
                val confidence = when {
                    languages.size > 1 -> ConfidenceBand.UNCERTAIN // Cross-language needs review
                    else -> ConfidenceBand.HIGH // Same language, exact title
                }

                groups += SyndicationGroup(
                    id = "syndication-${groups.size + 1}",
                    articleIds = matches.map { it.id },
                    sourceIds = distinctSources,
                    detectionEvidence = DetectionEvidence(
                        titleMatch = TitleMatchEvidence(
                            normalizedTitle = normalizedTitle,
                            matchType = MatchType.EXACT_NORMALIZED
                        ),
                        excerptMatch = null, // Will check below
                        wireAttribution = null // Would come from article metadata
                    ),
                    confidence = confidence,
                    methodVersion = "v1.0-title-fingerprint",
                    requiresEditorialReview = true,
                    rationale = buildRationale(confidence, distinctSources.size, languages.size)
                )
                matches.forEach { assigned += it.id }
            }
        }

        // Group 2: High excerpt overlap across different sources (for articles not already grouped)
        val remaining = articles.filterNot { it.id in assigned }
        val excerptGroups = findExcerptOverlapGroups(remaining)

        excerptGroups.forEach { group ->
            val distinctSources = group.map { it.sourceId }.distinct()
            if (distinctSources.size > 1) {
                val languages = group.map { it.languageTag }.distinct()
                val excerptSample = group.first().excerpt.take(150)

                val confidence = when {
                    languages.size > 1 -> ConfidenceBand.UNCERTAIN
                    group.first().excerpt.length < minimumExcerptLength -> ConfidenceBand.LOW
                    else -> ConfidenceBand.MEDIUM
                }

                groups += SyndicationGroup(
                    id = "syndication-${groups.size + 1}",
                    articleIds = group.map { it.id },
                    sourceIds = distinctSources,
                    detectionEvidence = DetectionEvidence(
                        titleMatch = null,
                        excerptMatch = ExcerptMatchEvidence(
                            fingerprint = fingerprint(excerptSample),
                            overlapScore = calculateMaxOverlap(group),
                            excerptLength = group.first().excerpt.length
                        ),
                        wireAttribution = null
                    ),
                    confidence = confidence,
                    methodVersion = "v1.0-excerpt-fingerprint",
                    requiresEditorialReview = true,
                    rationale = buildRationale(confidence, distinctSources.size, languages.size)
                )
                group.forEach { assigned += it.id }
            }
        }

        val independentIds = articles.map { it.id }.filterNot { it in assigned }

        return SyndicationAnalysisResult(
            analyzedCount = articles.size,
            syndicationGroups = groups,
            independentArticleIds = independentIds
        )
    }

    private fun normalizeTitle(title: String): String {
        // Remove punctuation, lowercase, trim whitespace
        return title.lowercase(Locale.ROOT)
            .replace(Regex("[^\\p{L}\\p{N}\\s]+"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun fingerprint(text: String): String {
        val normalized = text.lowercase(Locale.ROOT).replace(Regex("\\s+"), " ").trim()
        return MessageDigest.getInstance("SHA-256")
            .digest(normalized.toByteArray())
            .joinToString("") { "%02x".format(it) }
            .take(16)
    }

    private fun findExcerptOverlapGroups(articles: List<NormalizedArticle>): List<List<NormalizedArticle>> {
        if (articles.size < 2) return emptyList()

        val groups = mutableListOf<MutableList<NormalizedArticle>>()
        val assigned = mutableSetOf<String>()

        articles.forEach { candidate ->
            if (candidate.id in assigned || candidate.excerpt.length < minimumExcerptLength) return@forEach

            val matches = articles.filter { other ->
                other.id != candidate.id &&
                other.id !in assigned &&
                other.excerpt.length >= minimumExcerptLength &&
                excerptOverlap(candidate.excerpt, other.excerpt) >= excerptOverlapThreshold
            }

            if (matches.isNotEmpty()) {
                val group = mutableListOf(candidate)
                group.addAll(matches)
                groups += group
                group.forEach { assigned += it.id }
            }
        }

        return groups
    }

    private fun excerptOverlap(left: String, right: String): Double {
        val leftNorm = left.lowercase(Locale.ROOT).replace(Regex("\\s+"), " ").trim()
        val rightNorm = right.lowercase(Locale.ROOT).replace(Regex("\\s+"), " ").trim()

        if (leftNorm.isEmpty() || rightNorm.isEmpty()) return 0.0

        // Find longest common substring ratio
        val shorter = if (leftNorm.length < rightNorm.length) leftNorm else rightNorm
        val longer = if (leftNorm.length < rightNorm.length) rightNorm else leftNorm

        val lcs = longestCommonSubstring(shorter, longer)
        return lcs.length.toDouble() / shorter.length
    }

    private fun longestCommonSubstring(s1: String, s2: String): String {
        val lengths = Array(s1.length + 1) { IntArray(s2.length + 1) }
        var maxLength = 0
        var maxI = 0

        for (i in s1.indices) {
            for (j in s2.indices) {
                if (s1[i] == s2[j]) {
                    lengths[i + 1][j + 1] = lengths[i][j] + 1
                    if (lengths[i + 1][j + 1] > maxLength) {
                        maxLength = lengths[i + 1][j + 1]
                        maxI = i + 1
                    }
                }
            }
        }

        return if (maxLength > 0) s1.substring(maxI - maxLength, maxI) else ""
    }

    private fun calculateMaxOverlap(group: List<NormalizedArticle>): Double {
        if (group.size < 2) return 0.0
        return group.flatMap { a ->
            group.filter { b -> b.id != a.id }.map { b -> excerptOverlap(a.excerpt, b.excerpt) }
        }.maxOrNull() ?: 0.0
    }

    private fun buildRationale(confidence: ConfidenceBand, sourceCount: Int, languageCount: Int): String {
        return when (confidence) {
            ConfidenceBand.HIGH -> "High confidence: Exact title match across $sourceCount distinct sources, same language."
            ConfidenceBand.MEDIUM -> "Medium confidence: Strong excerpt overlap across $sourceCount distinct sources, same language."
            ConfidenceBand.LOW -> "Low confidence: Limited text similarity across $sourceCount sources (short excerpt or weak match)."
            ConfidenceBand.UNCERTAIN -> "Uncertain: Matched across $languageCount languages; may be independent translations or syndication. Editorial review required."
        }
    }
}

/**
 * Result of syndication analysis on a batch of articles.
 * Every article is either in a syndication group or listed as independent.
 * Source attributions are preserved for all articles.
 */
data class SyndicationAnalysisResult(
    val analyzedCount: Int,
    val syndicationGroups: List<SyndicationGroup>,
    val independentArticleIds: List<String>
)

/**
 * A group of articles suspected to be syndicated or wire-copy content.
 *
 * IMPORTANT: This is SUSPECTED syndication based on text similarity evidence.
 * It is NOT confirmed syndication unless wireAttribution is present.
 * All groups require editorial review before any claims about independence.
 */
data class SyndicationGroup(
    val id: String,
    val articleIds: List<String>,
    val sourceIds: List<String>,
    val detectionEvidence: DetectionEvidence,
    val confidence: ConfidenceBand,
    val methodVersion: String,
    val requiresEditorialReview: Boolean,
    val rationale: String
)

/**
 * Evidence used to detect suspected syndication.
 * Transparent so reviewers can understand and override the detection.
 */
data class DetectionEvidence(
    val titleMatch: TitleMatchEvidence?,
    val excerptMatch: ExcerptMatchEvidence?,
    val wireAttribution: WireAttributionEvidence?
)

data class TitleMatchEvidence(
    val normalizedTitle: String,
    val matchType: MatchType
)

data class ExcerptMatchEvidence(
    val fingerprint: String,
    val overlapScore: Double,
    val excerptLength: Int
)

data class WireAttributionEvidence(
    val wireSource: String, // e.g., "AP", "Reuters", "AFP"
    val foundInMetadata: Boolean
)

enum class MatchType {
    EXACT_NORMALIZED,
    NEAR_MATCH,
    TRANSLATION_SUSPECTED
}

/**
 * Conservative confidence bands for syndication detection.
 *
 * HIGH: Same language, exact title match or very high excerpt overlap
 * MEDIUM: Same language, strong excerpt similarity
 * LOW: Weak text similarity or short excerpt
 * UNCERTAIN: Cross-language match (could be translation or syndication)
 */
enum class ConfidenceBand {
    HIGH,
    MEDIUM,
    LOW,
    UNCERTAIN
}
