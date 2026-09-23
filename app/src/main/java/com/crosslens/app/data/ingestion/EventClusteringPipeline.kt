package com.crosslens.app.data.ingestion

import java.net.URI
import java.security.MessageDigest
import java.time.Duration
import java.time.Instant
import java.util.Locale

/**
 * Offline prototype for turning source-supplied link-and-excerpt records into
 * reviewable event proposals. It deliberately does not fetch, scrape, translate,
 * infer editorial labels, or calculate Lens Gap.
 *
 * Pipeline stages:
 * 1. Normalize articles (URL canonicalization, tokenization)
 * 2. Deduplicate by canonical URL
 * 3. Detect suspected syndication/wire-copy
 * 4. Cluster by title similarity and time window
 */
class EventClusteringPipeline(
    private val clusterWindow: Duration = Duration.ofHours(72),
    private val minimumTitleSimilarity: Double = 0.25,
    private val syndicationDetector: SyndicationDetector = SyndicationDetector()
) {
    fun process(inputs: List<IngestionArticleInput>): IngestionBatchResult {
        // Stage 1: Normalize
        val normalized = inputs.map(::normalize).sortedBy { it.publishedAt }

        // Stage 2: Deduplicate by canonical URL
        val uniqueByUrl = linkedMapOf<String, NormalizedArticle>()
        val duplicates = mutableListOf<DuplicateArticle>()

        normalized.forEach { article ->
            val existing = uniqueByUrl.putIfAbsent(article.canonicalUrl, article)
            if (existing != null) {
                duplicates += DuplicateArticle(article, existing.id, DuplicateReason.CANONICAL_URL)
            }
        }

        val uniqueArticles = uniqueByUrl.values.toList()

        // Stage 3: Detect suspected syndication
        val syndicationAnalysis = syndicationDetector.analyze(uniqueArticles)

        // Stage 4: Cluster by similarity
        val clusters = mutableListOf<MutableCluster>()
        uniqueArticles.forEach { article ->
            val best = clusters
                .map { cluster -> cluster to cluster.similarityTo(article) }
                .filter { (cluster, similarity) ->
                    Duration.between(cluster.firstPublishedAt, article.publishedAt).abs() <= clusterWindow &&
                        similarity >= minimumTitleSimilarity
                }
                .maxByOrNull { it.second }
                ?.first

            if (best == null) clusters += MutableCluster(article, clusterWindow.toHours()) else best.add(article)
        }

        return IngestionBatchResult(
            acceptedArticles = uniqueArticles,
            duplicates = duplicates,
            syndicationAnalysis = syndicationAnalysis,
            clusters = clusters.map { it.toProposal() }
        )
    }

    private fun normalize(input: IngestionArticleInput): NormalizedArticle {
        require(input.sourceId.isNotBlank()) { "sourceId is required" }
        require(input.url.startsWith("https://")) { "Only HTTPS article links are accepted" }
        require(input.headline.isNotBlank()) { "headline is required" }
        return NormalizedArticle(
            id = stableId("article", input.sourceId, canonicalizeUrl(input.url)),
            sourceId = input.sourceId,
            sourceName = input.sourceName,
            originalUrl = input.url,
            canonicalUrl = canonicalizeUrl(input.url),
            publishedAt = input.publishedAt,
            languageTag = input.languageTag,
            headline = input.headline.trim(),
            excerpt = input.excerpt.trim(),
            headlineTokens = tokens(input.headline)
        )
    }

    private fun canonicalizeUrl(value: String): String {
        val uri = URI(value)
        val filteredQuery = uri.query
            ?.split("&")
            ?.filterNot { it.substringBefore('=').lowercase(Locale.ROOT).startsWith("utm_") }
            ?.filterNot { it.substringBefore('=').lowercase(Locale.ROOT) in setOf("fbclid", "gclid") }
            ?.sorted()
            ?.joinToString("&")
            ?.takeIf { it.isNotBlank() }
        return URI(uri.scheme.lowercase(Locale.ROOT), uri.authority.lowercase(Locale.ROOT), uri.path, filteredQuery, null).toString()
    }

    private fun tokens(value: String): Set<String> = value
        .lowercase(Locale.ROOT)
        .split(Regex("[^\\p{L}\\p{N}]+"))
        .filter { it.length > 2 && it !in STOP_WORDS }
        .toSet()

    private fun stableId(prefix: String, vararg values: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(values.joinToString("|").toByteArray())
            .joinToString("") { "%02x".format(it) }
        return "$prefix-${digest.take(16)}"
    }

    private class MutableCluster(first: NormalizedArticle, private val windowHours: Long) {
        private val articles = mutableListOf(first)
        val firstPublishedAt: Instant get() = articles.minOf { it.publishedAt }

        fun similarityTo(candidate: NormalizedArticle): Double = articles
            .map { jaccard(it.headlineTokens, candidate.headlineTokens) }
            .maxOrNull() ?: 0.0

        fun add(article: NormalizedArticle) { articles += article }

        fun toProposal(): EventClusterProposal {
            val ordered = articles.sortedBy { it.publishedAt }
            val sourceIds = ordered.map { it.sourceId }.distinct()
            return EventClusterProposal(
                id = stableClusterId(ordered),
                provisionalTitle = ordered.first().headline,
                firstPublishedAt = ordered.first().publishedAt,
                lastPublishedAt = ordered.last().publishedAt,
                articleIds = ordered.map { it.id },
                sourceIds = sourceIds,
                sourceCount = sourceIds.size,
                status = if (sourceIds.size >= 2) ClusterStatus.REVIEWABLE else ClusterStatus.SINGLE_SOURCE,
                rationale = "Title similarity and a $windowHours-hour publication window; requires editorial review."
            )
        }

        private fun stableClusterId(articles: List<NormalizedArticle>): String =
            "event-" + articles.map { it.canonicalUrl }.sorted().joinToString("|").hashCode().toUInt().toString(16)
    }

    private companion object {
        val STOP_WORDS = setOf("the", "and", "for", "with", "from", "that", "this", "into", "new")
        fun jaccard(left: Set<String>, right: Set<String>): Double {
            if (left.isEmpty() || right.isEmpty()) return 0.0
            return left.intersect(right).size.toDouble() / left.union(right).size
        }
    }
}

data class IngestionArticleInput(
    val sourceId: String,
    val sourceName: String,
    val url: String,
    val publishedAt: Instant,
    val languageTag: String,
    val headline: String,
    val excerpt: String
)

data class NormalizedArticle(
    val id: String,
    val sourceId: String,
    val sourceName: String,
    val originalUrl: String,
    val canonicalUrl: String,
    val publishedAt: Instant,
    val languageTag: String,
    val headline: String,
    val excerpt: String,
    val headlineTokens: Set<String>
)

data class IngestionBatchResult(
    val acceptedArticles: List<NormalizedArticle>,
    val duplicates: List<DuplicateArticle>,
    val syndicationAnalysis: SyndicationAnalysisResult,
    val clusters: List<EventClusterProposal>
)

data class DuplicateArticle(val article: NormalizedArticle, val duplicateOfId: String, val reason: DuplicateReason)
enum class DuplicateReason { CANONICAL_URL }
enum class ClusterStatus { REVIEWABLE, SINGLE_SOURCE }

data class EventClusterProposal(
    val id: String,
    val provisionalTitle: String,
    val firstPublishedAt: Instant,
    val lastPublishedAt: Instant,
    val articleIds: List<String>,
    val sourceIds: List<String>,
    val sourceCount: Int,
    val status: ClusterStatus,
    val rationale: String
)
