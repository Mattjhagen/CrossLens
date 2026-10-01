package com.crosslens.app.data.clustering

import com.crosslens.app.core.model.ClusteredArticle
import com.crosslens.app.core.model.ClusterConfidence
import com.crosslens.app.core.model.ClusterMatch
import com.crosslens.app.core.model.EventCluster
import com.crosslens.app.data.ingestion.SourceArticleRecord
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

/**
 * Conservative event clustering service.
 *
 * Groups articles only when confident they describe the SAME SPECIFIC EVENT.
 * Uses headline similarity, named entities, publication time, and source diversity.
 *
 * DOES NOT group articles just because they:
 * - Mention the same person
 * - Mention the same country
 * - Are about the same broad topic
 */
@Singleton
class EventClusteringService @Inject constructor() {

    /**
     * Cluster a list of articles into events.
     * Returns only valid clusters (2+ articles from 2+ distinct publishers).
     */
    fun clusterArticles(articles: List<SourceArticleRecord>): List<EventCluster> {
        if (articles.size < 2) return emptyList()

        val clusteredArticles = articles.map { toClusteredArticle(it) }
        val clusters = mutableListOf<MutableList<ClusteredArticle>>()

        // Simple greedy clustering: compare each article to existing clusters
        for (article in clusteredArticles) {
            val matchedCluster = clusters.firstOrNull { cluster ->
                // Check if this article matches any article in the cluster
                cluster.any { other ->
                    val match = compareArticles(article, other)
                    match.shouldCluster
                }
            }

            if (matchedCluster != null) {
                // Add to existing cluster
                matchedCluster.add(article)
            } else {
                // Start new cluster
                clusters.add(mutableListOf(article))
            }
        }

        // Convert to EventCluster objects
        return clusters
            .filter { it.size >= 2 } // At least 2 articles
            .mapNotNull { articles -> buildEventCluster(articles) }
            .filter { it.isValid } // At least 2 distinct publishers
    }

    /**
     * Compare two articles to determine if they're about the same event.
     */
    fun compareArticles(a: ClusteredArticle, b: ClusteredArticle): ClusterMatch {
        // Rule 1: Different publishers required
        if (a.sourceId == b.sourceId) {
            return ClusterMatch(
                shouldCluster = false,
                confidence = null,
                explanation = "Same publisher - no clustering within single source",
                headlineSimilarity = 0.0,
                sharedEntities = 0,
                timeDifferenceHours = 0
            )
        }

        // Rule 2: Time proximity (events happen at a specific time)
        val timeDiffHours = abs(ChronoUnit.HOURS.between(a.publishedAt, b.publishedAt))
        if (timeDiffHours > 72) { // 3 days max
            return ClusterMatch(
                shouldCluster = false,
                confidence = null,
                explanation = "Too far apart in time ($timeDiffHours hours) - likely different events",
                headlineSimilarity = 0.0,
                sharedEntities = 0,
                timeDifferenceHours = timeDiffHours
            )
        }

        // Rule 3: Headline similarity
        val headlineSimilarity = calculateHeadlineSimilarity(
            a.normalizedHeadline,
            b.normalizedHeadline
        )

        // Rule 4: Shared entities (people, places, organizations)
        val sharedEntities = (a.entities.toSet() intersect b.entities.toSet()).size

        // Decision logic: require shared entities + good similarity, OR very high similarity alone
        val shouldCluster: Boolean
        val confidence: ClusterConfidence?
        val explanation: String

        when {
            // HIGH confidence: strong headline + multiple shared entities + same day
            sharedEntities >= 2 && headlineSimilarity >= 0.20 && timeDiffHours <= 24 -> {
                shouldCluster = true
                confidence = ClusterConfidence.HIGH
                explanation = "Strong match: $sharedEntities shared entities + ${"%.0f".format(headlineSimilarity * 100)}% headline similarity + within $timeDiffHours hours"
            }

            // MEDIUM confidence: good entities + some similarity + close in time
            sharedEntities >= 2 && headlineSimilarity >= 0.15 && timeDiffHours <= 48 -> {
                shouldCluster = true
                confidence = ClusterConfidence.MEDIUM
                explanation = "Good match: $sharedEntities shared entities + ${"%.0f".format(headlineSimilarity * 100)}% headline similarity + within $timeDiffHours hours"
            }

            // MEDIUM confidence: one shared entity + good headline similarity
            sharedEntities >= 1 && headlineSimilarity >= 0.30 && timeDiffHours <= 24 -> {
                shouldCluster = true
                confidence = ClusterConfidence.MEDIUM
                explanation = "Good match: $sharedEntities shared entities + ${"%.0f".format(headlineSimilarity * 100)}% headline similarity + within $timeDiffHours hours"
            }

            // HIGH confidence: exceptional headline match even without entity extraction
            headlineSimilarity >= 0.5 && timeDiffHours <= 24 -> {
                shouldCluster = true
                confidence = ClusterConfidence.HIGH
                explanation = "Very strong headline match (${"%.0f".format(headlineSimilarity * 100)}%) + within $timeDiffHours hours"
            }

            // MEDIUM confidence: strong headline match with slightly more time
            headlineSimilarity >= 0.5 && timeDiffHours <= 48 -> {
                shouldCluster = true
                confidence = ClusterConfidence.MEDIUM
                explanation = "Strong headline match (${"%.0f".format(headlineSimilarity * 100)}%) + within $timeDiffHours hours"
            }

            else -> {
                shouldCluster = false
                confidence = null
                explanation = "Insufficient similarity (headline: ${"%.0f".format(headlineSimilarity * 100)}%, entities: $sharedEntities, time: ${timeDiffHours}h)"
            }
        }

        return ClusterMatch(
            shouldCluster = shouldCluster,
            confidence = confidence,
            explanation = explanation,
            headlineSimilarity = headlineSimilarity,
            sharedEntities = sharedEntities,
            timeDifferenceHours = timeDiffHours
        )
    }

    /**
     * Calculate headline similarity using token overlap.
     * Returns a score from 0.0 to 1.0.
     */
    private fun calculateHeadlineSimilarity(normalized1: String, normalized2: String): Double {
        val tokens1 = normalized1.split(" ").toSet()
        val tokens2 = normalized2.split(" ").toSet()

        if (tokens1.isEmpty() || tokens2.isEmpty()) return 0.0

        val intersection = tokens1 intersect tokens2
        val union = tokens1 union tokens2

        // Jaccard similarity
        return intersection.size.toDouble() / union.size.toDouble()
    }

    /**
     * Normalize a headline for matching:
     * - Lowercase
     * - Remove punctuation
     * - Remove common stop words
     * - Remove source-specific prefixes
     */
    fun normalizeHeadline(headline: String): String {
        var normalized = headline.lowercase()

        // Remove common article prefixes
        val prefixes = listOf(
            "breaking: ", "breaking news: ", "live: ", "update: ",
            "opinion: ", "analysis: ", "exclusive: ", "video: "
        )
        for (prefix in prefixes) {
            if (normalized.startsWith(prefix)) {
                normalized = normalized.removePrefix(prefix)
            }
        }

        // Remove punctuation except spaces
        normalized = normalized.replace(Regex("[^a-z0-9\\s]"), " ")

        // Remove extra spaces
        normalized = normalized.replace(Regex("\\s+"), " ").trim()

        // Remove common stop words
        val stopWords = setOf(
            "a", "an", "the", "is", "are", "was", "were", "be", "been", "being",
            "to", "of", "in", "on", "at", "by", "for", "with", "from", "as",
            "this", "that", "these", "those", "it", "its", "their", "they",
            "has", "have", "had", "do", "does", "did", "will", "would", "should",
            "can", "could", "may", "might", "must", "shall"
        )

        val tokens = normalized.split(" ").filter { it !in stopWords && it.length > 2 }
        return tokens.joinToString(" ")
    }

    /**
     * Extract simple named entities from text.
     * In production, use a proper NER model.
     */
    private fun extractEntities(text: String): List<String> {
        // Simple heuristic: capitalized sequences (names, places, organizations)
        val pattern = Regex("\\b[A-Z][a-z]+(?:\\s+[A-Z][a-z]+)*\\b")
        return pattern.findAll(text)
            .map { it.value }
            .filter { it.length > 3 } // Filter out short matches
            .distinct()
            .toList()
    }

    /**
     * Convert SourceArticleRecord to ClusteredArticle with normalization and entity extraction.
     */
    private fun toClusteredArticle(record: SourceArticleRecord): ClusteredArticle {
        val entities = extractEntities(record.headline + " " + record.excerpt)

        return ClusteredArticle(
            sourceId = record.sourceId,
            headline = record.headline,
            excerpt = record.excerpt,
            publishedAt = record.publishedAt,
            url = record.url,
            imageUrl = record.imageUrl,
            languageTag = record.languageTag,
            normalizedHeadline = normalizeHeadline(record.headline),
            entities = entities
        )
    }

    /**
     * Build an EventCluster from a group of articles.
     */
    private fun buildEventCluster(articles: List<ClusteredArticle>): EventCluster? {
        if (articles.isEmpty()) return null

        // Check for distinct publishers
        val publisherCount = articles.map { it.sourceId }.distinct().size
        if (publisherCount < 2) return null

        // Derive event summary from most common headline tokens
        val eventSummary = deriveEventSummary(articles)

        // Find common entities
        val entityCounts = articles
            .flatMap { it.entities }
            .groupingBy { it }
            .eachCount()
        val commonEntities = entityCounts
            .filter { it.value >= 2 } // Appears in at least 2 articles
            .map { it.key }
            .take(5)

        // Calculate overall confidence
        val confidence = when {
            articles.size >= 3 -> ClusterConfidence.HIGH
            articles.size == 2 -> ClusterConfidence.MEDIUM
            else -> ClusterConfidence.LOW
        }

        // Build explanation
        val explanation = buildString {
            append("Grouped ${articles.size} articles from $publisherCount publishers: ")
            append("${commonEntities.size} common entities")
            val timeSpan = ChronoUnit.HOURS.between(
                articles.minOf { it.publishedAt },
                articles.maxOf { it.publishedAt }
            )
            append(", published within $timeSpan hours")
        }

        return EventCluster(
            id = "event-${UUID.randomUUID()}",
            eventSummary = eventSummary,
            articles = articles,
            eventTime = articles.minOf { it.publishedAt },
            clusteredAt = Instant.now(),
            confidence = confidence,
            groupingExplanation = explanation,
            commonEntities = commonEntities,
            publisherCount = publisherCount
        )
    }

    /**
     * Derive a summary of the event from article headlines.
     */
    private fun deriveEventSummary(articles: List<ClusteredArticle>): String {
        // Find most common significant tokens across headlines
        val tokenCounts = articles
            .flatMap { it.normalizedHeadline.split(" ") }
            .filter { it.length > 3 }
            .groupingBy { it }
            .eachCount()

        val topTokens = tokenCounts
            .toList()
            .sortedByDescending { it.second }
            .take(5)
            .map { it.first }

        // Use the shortest headline as template, filling in common tokens
        val shortestHeadline = articles.minByOrNull { it.headline.length }?.headline ?: ""

        return if (topTokens.isNotEmpty()) {
            // Capitalize first letter
            shortestHeadline.replaceFirstChar { it.uppercase() }
        } else {
            shortestHeadline
        }
    }
}
