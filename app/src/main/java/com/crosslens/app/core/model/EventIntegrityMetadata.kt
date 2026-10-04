package com.crosslens.app.core.model

import java.time.Instant

/**
 * Event Integrity Metadata - Factual cluster-quality signals.
 *
 * Records observable, measurable clustering signals WITHOUT inferring:
 * - Ideology, political alignment, or bias
 * - Truthfulness or which reporting is "correct"
 * - Sentiment or editorial perspective
 *
 * Uses only: publisher diversity, temporal proximity, textual similarity,
 * named entity overlap, and documented match logic.
 */
data class EventIntegrityMetadata(
    /** Unique cluster identifier */
    val clusterId: String,

    /** Number of distinct publishers in this cluster */
    val distinctPublisherCount: Int,

    /** Total number of articles in cluster */
    val articleCount: Int,

    /** Clustering time window (earliest to latest article) in hours */
    val timeWindowHours: Long,

    /** Named entities that appear in 2+ articles (factual overlap signal) */
    val commonNamedEntities: List<String>,

    /** Headline similarity scores (Jaccard index, 0.0 to 1.0) for article pairs */
    val headlineSimilarityScores: List<Double>,

    /** Average headline similarity across all pairs */
    val averageHeadlineSimilarity: Double,

    /** Number of shared entities across article pairs */
    val sharedEntityCounts: List<Int>,

    /** Source IDs included in cluster */
    val sourceIds: List<String>,

    /** Article URLs included in cluster */
    val articleUrls: List<String>,

    /** Clustering confidence (based on factual signals only) */
    val confidence: ClusterConfidence,

    /** Factual match rationale (no ideology/bias inference) */
    val matchRationale: String,

    /** When this cluster was created */
    val clusteredAt: Instant,

    /** When this metadata was last updated */
    val updatedAt: Instant
) {
    /**
     * Whether this cluster meets basic integrity thresholds.
     * Requires 2+ distinct publishers and not LOW confidence.
     */
    val meetsIntegrityThreshold: Boolean
        get() = distinctPublisherCount >= 2 && confidence != ClusterConfidence.LOW

    /**
     * Whether this is a single-publisher cluster (should not present as comparison).
     */
    val isSinglePublisher: Boolean
        get() = distinctPublisherCount == 1
}

/**
 * Integrity check result for a cluster.
 */
data class IntegrityCheckResult(
    /** Cluster ID checked */
    val clusterId: String,

    /** Whether cluster passes integrity checks */
    val passed: Boolean,

    /** Specific integrity findings (factual observations) */
    val findings: List<IntegrityFinding>,

    /** When check was performed */
    val checkedAt: Instant
)

/**
 * Factual finding from integrity check (no ideology/bias inference).
 */
data class IntegrityFinding(
    /** Finding category */
    val category: FindingCategory,

    /** Severity level */
    val severity: FindingSeverity,

    /** Factual description of finding */
    val description: String,

    /** Supporting evidence (e.g., specific values, thresholds) */
    val evidence: String
)

enum class FindingCategory {
    /** Publisher diversity metrics */
    PUBLISHER_DIVERSITY,

    /** Temporal proximity metrics */
    TEMPORAL_PROXIMITY,

    /** Headline similarity metrics */
    HEADLINE_SIMILARITY,

    /** Named entity overlap metrics */
    ENTITY_OVERLAP,

    /** Overall confidence assessment */
    CONFIDENCE_LEVEL
}

enum class FindingSeverity {
    /** Informational - cluster is valid */
    INFO,

    /** Warning - cluster meets minimum but has weak signals */
    WARNING,

    /** Error - cluster fails integrity checks */
    ERROR
}
