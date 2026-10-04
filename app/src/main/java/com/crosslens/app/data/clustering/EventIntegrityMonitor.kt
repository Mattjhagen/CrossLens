package com.crosslens.app.data.clustering

import com.crosslens.app.core.model.ClusterConfidence
import com.crosslens.app.core.model.EventCluster
import com.crosslens.app.core.model.EventIntegrityMetadata
import com.crosslens.app.core.model.FindingCategory
import com.crosslens.app.core.model.FindingSeverity
import com.crosslens.app.core.model.IntegrityCheckResult
import com.crosslens.app.core.model.IntegrityFinding
import com.crosslens.app.data.local.EventIntegrityDao
import com.crosslens.app.data.local.toEntity
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Event Integrity Monitor - Computes and persists factual cluster-quality signals.
 *
 * DOES NOT infer:
 * - Ideology, political alignment, or bias
 * - Truthfulness or which reporting is "correct"
 * - Sentiment or editorial perspective
 *
 * Uses ONLY:
 * - Publisher diversity (distinct source count)
 * - Temporal proximity (time window)
 * - Textual similarity (headline overlap)
 * - Named entity overlap
 * - Documented match logic from EventClusteringService
 */
@Singleton
class EventIntegrityMonitor @Inject constructor(
    private val clusteringService: EventClusteringService,
    private val integrityDao: EventIntegrityDao
) {

    /**
     * Compute integrity metadata for a cluster and persist it.
     */
    suspend fun recordIntegrityMetadata(cluster: EventCluster) {
        val metadata = computeIntegrityMetadata(cluster)
        integrityDao.insertOrUpdate(metadata.toEntity())
    }

    /**
     * Compute integrity metadata for multiple clusters and persist them.
     */
    suspend fun recordIntegrityMetadataForAll(clusters: List<EventCluster>) {
        val metadataList = clusters.map { computeIntegrityMetadata(it) }
        integrityDao.insertOrUpdateAll(metadataList.map { it.toEntity() })
    }

    /**
     * Compute factual integrity metadata for a cluster.
     */
    fun computeIntegrityMetadata(cluster: EventCluster): EventIntegrityMetadata {
        // Extract factual signals
        val articles = cluster.articles
        val distinctPublishers = articles.map { it.sourceId }.distinct()
        val timeWindow = if (articles.size >= 2) {
            ChronoUnit.HOURS.between(
                articles.minOf { it.publishedAt },
                articles.maxOf { it.publishedAt }
            )
        } else {
            0L
        }

        // Compute pairwise headline similarities
        val headlineSimilarities = mutableListOf<Double>()
        val sharedEntityCounts = mutableListOf<Int>()

        for (i in articles.indices) {
            for (j in (i + 1) until articles.size) {
                val match = clusteringService.compareArticles(articles[i], articles[j])
                headlineSimilarities.add(match.headlineSimilarity)
                sharedEntityCounts.add(match.sharedEntities)
            }
        }

        val avgSimilarity = if (headlineSimilarities.isNotEmpty()) {
            headlineSimilarities.average()
        } else {
            0.0
        }

        // Build factual match rationale (no ideology/bias)
        val rationale = buildMatchRationale(
            distinctPublishers.size,
            articles.size,
            timeWindow,
            cluster.commonEntities.size,
            avgSimilarity,
            cluster.confidence
        )

        return EventIntegrityMetadata(
            clusterId = cluster.id,
            distinctPublisherCount = distinctPublishers.size,
            articleCount = articles.size,
            timeWindowHours = timeWindow,
            commonNamedEntities = cluster.commonEntities,
            headlineSimilarityScores = headlineSimilarities,
            averageHeadlineSimilarity = avgSimilarity,
            sharedEntityCounts = sharedEntityCounts,
            sourceIds = articles.map { it.sourceId },
            articleUrls = articles.map { it.url },
            confidence = cluster.confidence,
            matchRationale = rationale,
            clusteredAt = cluster.clusteredAt,
            updatedAt = Instant.now()
        )
    }

    /**
     * Perform integrity check on a cluster.
     * Returns factual findings without ideology/bias inference.
     */
    fun checkIntegrity(metadata: EventIntegrityMetadata): IntegrityCheckResult {
        val findings = mutableListOf<IntegrityFinding>()

        // Check 1: Publisher diversity
        if (metadata.distinctPublisherCount < 2) {
            findings.add(
                IntegrityFinding(
                    category = FindingCategory.PUBLISHER_DIVERSITY,
                    severity = FindingSeverity.ERROR,
                    description = "Single publisher - cannot present as cross-source comparison",
                    evidence = "Found ${metadata.distinctPublisherCount} publisher (requires 2+)"
                )
            )
        } else if (metadata.distinctPublisherCount == 2) {
            findings.add(
                IntegrityFinding(
                    category = FindingCategory.PUBLISHER_DIVERSITY,
                    severity = FindingSeverity.WARNING,
                    description = "Minimum publisher diversity",
                    evidence = "2 publishers (stronger with 3+)"
                )
            )
        } else {
            findings.add(
                IntegrityFinding(
                    category = FindingCategory.PUBLISHER_DIVERSITY,
                    severity = FindingSeverity.INFO,
                    description = "Good publisher diversity",
                    evidence = "${metadata.distinctPublisherCount} distinct publishers"
                )
            )
        }

        // Check 2: Temporal proximity
        if (metadata.timeWindowHours > 72) {
            findings.add(
                IntegrityFinding(
                    category = FindingCategory.TEMPORAL_PROXIMITY,
                    severity = FindingSeverity.WARNING,
                    description = "Wide time window may indicate different events",
                    evidence = "${metadata.timeWindowHours} hours (threshold: 72h)"
                )
            )
        } else if (metadata.timeWindowHours <= 24) {
            findings.add(
                IntegrityFinding(
                    category = FindingCategory.TEMPORAL_PROXIMITY,
                    severity = FindingSeverity.INFO,
                    description = "Strong temporal proximity",
                    evidence = "${metadata.timeWindowHours} hours (within 24h)"
                )
            )
        } else {
            findings.add(
                IntegrityFinding(
                    category = FindingCategory.TEMPORAL_PROXIMITY,
                    severity = FindingSeverity.INFO,
                    description = "Reasonable temporal proximity",
                    evidence = "${metadata.timeWindowHours} hours"
                )
            )
        }

        // Check 3: Headline similarity
        if (metadata.averageHeadlineSimilarity < 0.15) {
            findings.add(
                IntegrityFinding(
                    category = FindingCategory.HEADLINE_SIMILARITY,
                    severity = FindingSeverity.WARNING,
                    description = "Low headline similarity",
                    evidence = "${"%.1f".format(metadata.averageHeadlineSimilarity * 100)}% average (threshold: 15%)"
                )
            )
        } else {
            findings.add(
                IntegrityFinding(
                    category = FindingCategory.HEADLINE_SIMILARITY,
                    severity = FindingSeverity.INFO,
                    description = "Adequate headline similarity",
                    evidence = "${"%.1f".format(metadata.averageHeadlineSimilarity * 100)}% average"
                )
            )
        }

        // Check 4: Entity overlap
        if (metadata.commonNamedEntities.isEmpty()) {
            findings.add(
                IntegrityFinding(
                    category = FindingCategory.ENTITY_OVERLAP,
                    severity = FindingSeverity.WARNING,
                    description = "No common named entities detected",
                    evidence = "0 shared entities (may indicate entity extraction issue or truly different events)"
                )
            )
        } else {
            findings.add(
                IntegrityFinding(
                    category = FindingCategory.ENTITY_OVERLAP,
                    severity = FindingSeverity.INFO,
                    description = "Common named entities found",
                    evidence = "${metadata.commonNamedEntities.size} entities: ${metadata.commonNamedEntities.take(3).joinToString(", ")}"
                )
            )
        }

        // Check 5: Overall confidence
        when (metadata.confidence) {
            ClusterConfidence.LOW -> {
                findings.add(
                    IntegrityFinding(
                        category = FindingCategory.CONFIDENCE_LEVEL,
                        severity = FindingSeverity.ERROR,
                        description = "Low clustering confidence",
                        evidence = "Should not present as confident cross-source comparison"
                    )
                )
            }
            ClusterConfidence.MEDIUM -> {
                findings.add(
                    IntegrityFinding(
                        category = FindingCategory.CONFIDENCE_LEVEL,
                        severity = FindingSeverity.INFO,
                        description = "Medium clustering confidence",
                        evidence = "Adequate signals for same-event match"
                    )
                )
            }
            ClusterConfidence.HIGH -> {
                findings.add(
                    IntegrityFinding(
                        category = FindingCategory.CONFIDENCE_LEVEL,
                        severity = FindingSeverity.INFO,
                        description = "High clustering confidence",
                        evidence = "Strong signals for same-event match"
                    )
                )
            }
        }

        // Overall pass/fail
        val hasErrors = findings.any { it.severity == FindingSeverity.ERROR }
        val passed = !hasErrors && metadata.meetsIntegrityThreshold

        return IntegrityCheckResult(
            clusterId = metadata.clusterId,
            passed = passed,
            findings = findings,
            checkedAt = Instant.now()
        )
    }

    /**
     * Build factual match rationale (no ideology/bias).
     */
    private fun buildMatchRationale(
        publisherCount: Int,
        articleCount: Int,
        timeWindow: Long,
        commonEntities: Int,
        avgSimilarity: Double,
        confidence: ClusterConfidence
    ): String = buildString {
        append("Matched $articleCount articles from $publisherCount publishers: ")

        val signals = mutableListOf<String>()

        if (commonEntities > 0) {
            signals.add("$commonEntities shared entities")
        }

        signals.add("${"%.0f".format(avgSimilarity * 100)}% avg headline similarity")
        signals.add("${timeWindow}h time window")

        append(signals.joinToString(", "))
        append(". Confidence: $confidence based on factual signal strength.")
    }
}
