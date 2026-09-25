package com.crosslens.app.data.ingestion

import java.time.Instant

/**
 * Health status for a configured RSS source.
 */
enum class SourceHealthStatus {
    /** Source is fetching and parsing successfully */
    ACTIVE,

    /** Source is experiencing issues but may recover (transient failures) */
    DEGRADED,

    /** Source has been disabled due to persistent failures */
    DISABLED
}

/**
 * Health metrics for a single source fetch attempt.
 */
data class SourceHealthCheck(
    val sourceId: String,
    val checkedAt: Instant,
    val fetchSucceeded: Boolean,
    val parseSucceeded: Boolean,
    val articlesReturned: Int,
    val articlesWithValidDates: Int,
    val articlesWithImages: Int,
    val articlesWithHttpsLinks: Int,
    val latestArticleAge: Long?, // seconds since publication
    val fetchDurationMs: Long,
    val errorMessage: String? = null
)

/**
 * Aggregated health status for a source across multiple checks.
 */
data class SourceHealthSummary(
    val sourceId: String,
    val sourceName: String,
    val status: SourceHealthStatus,
    val lastSuccessAt: Instant?,
    val lastFailureAt: Instant?,
    val consecutiveFailures: Int,
    val last24hSuccessRate: Double, // 0.0 to 1.0
    val last24hArticleCount: Int,
    val last24hImageRate: Double, // 0.0 to 1.0
    val recentChecks: List<SourceHealthCheck>,
    val disabledReason: String? = null
)

/**
 * Overall feed health metrics across all sources.
 */
data class FeedHealthReport(
    val generatedAt: Instant,
    val totalConfigured: Int,
    val activeCount: Int,
    val degradedCount: Int,
    val disabledCount: Int,
    val overallSuccessRate: Double, // 0.0 to 1.0
    val sourceSummaries: List<SourceHealthSummary>
) {
    /**
     * Check if the feed meets production-ready criteria.
     * Requires at least 90% of configured sources to be active.
     */
    fun isProductionReady(): Boolean {
        return overallSuccessRate >= 0.90 && activeCount >= (totalConfigured * 0.9).toInt()
    }

    fun getStatusMessage(): String {
        return when {
            isProductionReady() -> "Production Ready: $activeCount/$totalConfigured sources active (${String.format("%.1f", overallSuccessRate * 100)}%)"
            overallSuccessRate >= 0.75 -> "Needs Attention: $activeCount/$totalConfigured sources active (${String.format("%.1f", overallSuccessRate * 100)}%)"
            else -> "Critical: Only $activeCount/$totalConfigured sources active (${String.format("%.1f", overallSuccessRate * 100)}%)"
        }
    }
}
