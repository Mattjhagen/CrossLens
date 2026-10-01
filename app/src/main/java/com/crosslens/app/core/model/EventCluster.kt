package com.crosslens.app.core.model

import java.time.Instant

/**
 * An event cluster represents multiple source articles about the same specific event.
 *
 * Key principles:
 * - Only group when confident articles concern the SAME SPECIFIC EVENT
 * - Same person/country/topic does NOT mean same event
 * - Require at least 2 distinct publishers
 * - Preserve all original article data
 * - Include confidence and explanation for grouping decision
 */
data class EventCluster(
    /** Unique cluster identifier */
    val id: String,

    /** Human-readable event summary (derived from common elements) */
    val eventSummary: String,

    /** Source articles grouped into this event */
    val articles: List<ClusteredArticle>,

    /** When the event occurred (earliest publication time) */
    val eventTime: Instant,

    /** When this cluster was created */
    val clusteredAt: Instant,

    /** Confidence level that these articles are about the same event */
    val confidence: ClusterConfidence,

    /** Explanation of why these articles were grouped */
    val groupingExplanation: String,

    /** Common named entities across articles (people, places, organizations) */
    val commonEntities: List<String> = emptyList(),

    /** Number of distinct publishers in this cluster */
    val publisherCount: Int = articles.map { it.sourceId }.distinct().size
) {
    /**
     * Whether this cluster is valid for display.
     * Must have at least 2 articles from distinct publishers.
     */
    val isValid: Boolean
        get() = articles.size >= 2 && publisherCount >= 2
}

/**
 * Article within an event cluster, preserving all original data.
 */
data class ClusteredArticle(
    /** Source identifier */
    val sourceId: String,

    /** Original headline */
    val headline: String,

    /** Original excerpt/description */
    val excerpt: String,

    /** Publication time */
    val publishedAt: Instant,

    /** Original article URL */
    val url: String,

    /** Article image URL (if available) */
    val imageUrl: String? = null,

    /** Primary language */
    val languageTag: String,

    /** Normalized headline for matching */
    val normalizedHeadline: String,

    /** Extracted named entities (people, places, organizations) */
    val entities: List<String> = emptyList()
)

/**
 * Confidence level for event clustering.
 */
enum class ClusterConfidence {
    /** High confidence: strong headline overlap + shared entities + time proximity */
    HIGH,

    /** Medium confidence: good headline similarity + some shared elements */
    MEDIUM,

    /** Low confidence: matched but with some ambiguity */
    LOW
}

/**
 * Result of comparing two articles for event clustering.
 */
data class ClusterMatch(
    /** Whether articles should be clustered */
    val shouldCluster: Boolean,

    /** Confidence level if match is positive */
    val confidence: ClusterConfidence?,

    /** Explanation of match/non-match decision */
    val explanation: String,

    /** Headline similarity score (0.0 to 1.0) */
    val headlineSimilarity: Double,

    /** Number of shared named entities */
    val sharedEntities: Int,

    /** Time difference in hours */
    val timeDifferenceHours: Long
)
