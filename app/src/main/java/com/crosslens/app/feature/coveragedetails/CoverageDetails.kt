package com.crosslens.app.feature.coveragedetails

import java.time.Instant

/**
 * Coverage details model for displaying factual event cluster metadata.
 *
 * Shows only documented, attributable information about:
 * - Source diversity (publishers, languages, countries)
 * - Temporal coverage (publication times, freshness)
 * - Clustering rationale (why articles were grouped)
 * - Coverage limitations (what's missing, incomplete, or uncertain)
 *
 * IMPORTANT: Does not infer or display ideology, bias, neutrality, political
 * alignment, or a nation's viewpoint. Only shows factual metadata with provenance.
 */
data class CoverageDetails(
    /** Event title/summary */
    val eventTitle: String,

    /** Number of distinct publishers covering this event */
    val distinctPublisherCount: Int,

    /** Sources with documented metadata */
    val sources: List<CoverageSource>,

    /** Temporal coverage information */
    val temporalCoverage: TemporalCoverage,

    /** Why these articles were grouped together */
    val clusteringRationale: String,

    /** Coverage limitations and gaps */
    val limitations: List<CoverageLimitation>
)

/**
 * Documented source information for coverage display.
 * Only includes factual, attributable metadata - no inferred classifications.
 */
data class CoverageSource(
    /** Publisher name (from documented source metadata) */
    val publisherName: String,

    /** Publishing country/region (from documented source metadata) */
    val country: String?,

    /** Primary language (from documented article/source metadata) */
    val language: String?,

    /** ISO language code (e.g., "en", "fr", "ar") */
    val languageCode: String?,

    /** Publication timestamp */
    val publishedAt: Instant,

    /** Editorial description with documented provenance (if available) */
    val editorialDescription: String?,

    /** Source of editorial description (e.g., "BBC Royal Charter") */
    val descriptionProvenance: String?,

    /** Original article headline */
    val headline: String
)

/**
 * Temporal coverage information showing when articles were published.
 */
data class TemporalCoverage(
    /** Earliest article publication time */
    val earliest: Instant,

    /** Most recent article publication time */
    val latest: Instant,

    /** Freshness description (e.g., "Last update 2 hours ago") */
    val freshnessDescription: String,

    /** Time span description (e.g., "Coverage spans 6 hours") */
    val spanDescription: String
)

/**
 * Coverage limitation or gap to display.
 * Examples: "Only English-language coverage", "All sources from same country",
 * "No additional qualifying coverage available"
 */
data class CoverageLimitation(
    /** Limitation type for semantic grouping */
    val type: LimitationType,

    /** User-facing description of the limitation */
    val description: String
)

/**
 * Types of coverage limitations.
 */
enum class LimitationType {
    /** Limited source diversity (few publishers) */
    SOURCE_DIVERSITY,

    /** Limited language diversity */
    LANGUAGE_DIVERSITY,

    /** Limited geographic diversity */
    GEOGRAPHIC_DIVERSITY,

    /** Temporal limitation (recent coverage only) */
    TEMPORAL_SPAN,

    /** No additional coverage available */
    NO_ADDITIONAL_COVERAGE,

    /** General coverage completeness warning */
    GENERAL
}
