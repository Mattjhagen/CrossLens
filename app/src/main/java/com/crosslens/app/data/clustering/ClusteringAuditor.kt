package com.crosslens.app.data.clustering

import android.util.Log
import com.crosslens.app.core.model.ClusteredArticle
import com.crosslens.app.core.model.ClusterMatch
import com.crosslens.app.data.ingestion.SourceArticleRecord
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Real-data clustering auditor that logs detailed information about
 * clustering decisions for diagnosis and improvement.
 */
class ClusteringAuditor(
    private val clusteringService: EventClusteringService
) {
    private val tag = "ClusteringAudit"

    /**
     * Audit a clustering run with detailed logging.
     */
    fun auditClusteringRun(articles: List<SourceArticleRecord>): ClusteringAuditReport {
        Log.d(tag, "======== CLUSTERING AUDIT START ========")
        Log.d(tag, "Total articles: ${articles.size}")
        Log.d(tag, "Publishers: ${articles.map { it.sourceId }.distinct().size}")
        Log.d(tag, "Time range: ${articles.minOfOrNull { it.publishedAt }} to ${articles.maxOfOrNull { it.publishedAt }}")

        // Log article summary
        articles.groupBy { it.sourceId }.forEach { (sourceId, sourceArticles) ->
            Log.d(tag, "Source: $sourceId - ${sourceArticles.size} articles")
            sourceArticles.forEach { article ->
                Log.d(tag, "  - [${article.publishedAt}] ${article.headline.take(60)}...")
            }
        }

        // Perform clustering with detailed comparison logging
        val clusteredArticles = articles.map { toClusteredArticle(it) }
        val pairwiseComparisons = mutableListOf<PairwiseComparison>()

        // Compare all pairs
        for (i in clusteredArticles.indices) {
            for (j in (i + 1) until clusteredArticles.size) {
                val a = clusteredArticles[i]
                val b = clusteredArticles[j]

                val match = clusteringService.compareArticles(a, b)

                val comparison = PairwiseComparison(
                    article1 = ArticleSummary(
                        sourceId = a.sourceId,
                        headline = a.headline,
                        normalizedHeadline = a.normalizedHeadline,
                        entities = a.entities,
                        publishedAt = a.publishedAt,
                        url = a.url
                    ),
                    article2 = ArticleSummary(
                        sourceId = b.sourceId,
                        headline = b.headline,
                        normalizedHeadline = b.normalizedHeadline,
                        entities = b.entities,
                        publishedAt = b.publishedAt,
                        url = b.url
                    ),
                    match = match
                )

                pairwiseComparisons.add(comparison)

                logComparison(comparison)
            }
        }

        // Perform actual clustering
        val clusters = clusteringService.clusterArticles(articles)

        Log.d(tag, "\n======== CLUSTERING RESULTS ========")
        Log.d(tag, "Clusters formed: ${clusters.size}")
        clusters.forEachIndexed { idx, cluster ->
            Log.d(tag, "\nCluster ${idx + 1}:")
            Log.d(tag, "  Event: ${cluster.eventSummary}")
            Log.d(tag, "  Articles: ${cluster.articles.size}")
            Log.d(tag, "  Publishers: ${cluster.publisherCount} (${cluster.articles.map { it.sourceId }.distinct().joinToString(", ")})")
            Log.d(tag, "  Confidence: ${cluster.confidence}")
            Log.d(tag, "  Common entities: ${cluster.commonEntities.joinToString(", ")}")
            Log.d(tag, "  Explanation: ${cluster.groupingExplanation}")

            cluster.articles.forEach { article ->
                Log.d(tag, "    - [${article.sourceId}] ${article.headline}")
            }
        }

        // Log unclustered articles
        val clusteredUrls = clusters.flatMap { it.articles.map { article -> article.url } }.toSet()
        val unclusteredArticles = articles.filter { it.url !in clusteredUrls }

        Log.d(tag, "\n======== UNCLUSTERED ARTICLES ========")
        Log.d(tag, "Count: ${unclusteredArticles.size}")
        unclusteredArticles.forEach { article ->
            Log.d(tag, "  [${article.sourceId}] ${article.headline.take(60)}...")
        }

        Log.d(tag, "\n======== AUDIT COMPLETE ========\n")

        return ClusteringAuditReport(
            totalArticles = articles.size,
            distinctPublishers = articles.map { it.sourceId }.distinct().size,
            pairwiseComparisons = pairwiseComparisons,
            clustersFormed = clusters.size,
            clusteredArticleCount = clusters.sumOf { it.articles.size },
            unclusteredArticleCount = unclusteredArticles.size,
            acceptedMatches = pairwiseComparisons.count { it.match.shouldCluster },
            rejectedMatches = pairwiseComparisons.count { !it.match.shouldCluster }
        )
    }

    private fun logComparison(comparison: PairwiseComparison) {
        val match = comparison.match
        val a = comparison.article1
        val b = comparison.article2

        Log.d(tag, "\n--- COMPARING ---")
        Log.d(tag, "A: [${a.sourceId}] ${a.headline.take(50)}...")
        Log.d(tag, "B: [${b.sourceId}] ${b.headline.take(50)}...")
        Log.d(tag, "Normalized A: ${a.normalizedHeadline}")
        Log.d(tag, "Normalized B: ${b.normalizedHeadline}")
        Log.d(tag, "Entities A: ${a.entities.joinToString(", ")}")
        Log.d(tag, "Entities B: ${b.entities.joinToString(", ")}")
        Log.d(tag, "Similarity: ${String.format("%.1f%%", match.headlineSimilarity * 100)}")
        Log.d(tag, "Shared entities: ${match.sharedEntities}")
        Log.d(tag, "Time difference: ${match.timeDifferenceHours}h")
        Log.d(tag, "Decision: ${if (match.shouldCluster) "✅ CLUSTER" else "❌ SEPARATE"}")
        Log.d(tag, "Confidence: ${match.confidence ?: "N/A"}")
        Log.d(tag, "Explanation: ${match.explanation}")
    }

    private fun toClusteredArticle(record: SourceArticleRecord): ClusteredArticle {
        // Use reflection to access private extractEntities method
        val method = EventClusteringService::class.java.getDeclaredMethod(
            "extractEntities",
            String::class.java
        )
        method.isAccessible = true

        @Suppress("UNCHECKED_CAST")
        val entities = method.invoke(clusteringService, record.headline + " " + record.excerpt) as List<String>

        val normalizeMethod = EventClusteringService::class.java.getDeclaredMethod(
            "normalizeHeadline",
            String::class.java
        )
        normalizeMethod.isAccessible = true
        val normalizedHeadline = normalizeMethod.invoke(clusteringService, record.headline) as String

        return ClusteredArticle(
            sourceId = record.sourceId,
            headline = record.headline,
            excerpt = record.excerpt,
            publishedAt = record.publishedAt,
            url = record.url,
            imageUrl = record.imageUrl,
            languageTag = record.languageTag,
            normalizedHeadline = normalizedHeadline,
            entities = entities
        )
    }
}

data class ClusteringAuditReport(
    val totalArticles: Int,
    val distinctPublishers: Int,
    val pairwiseComparisons: List<PairwiseComparison>,
    val clustersFormed: Int,
    val clusteredArticleCount: Int,
    val unclusteredArticleCount: Int,
    val acceptedMatches: Int,
    val rejectedMatches: Int
)

data class PairwiseComparison(
    val article1: ArticleSummary,
    val article2: ArticleSummary,
    val match: ClusterMatch
)

data class ArticleSummary(
    val sourceId: String,
    val headline: String,
    val normalizedHeadline: String,
    val entities: List<String>,
    val publishedAt: Instant,
    val url: String
)
