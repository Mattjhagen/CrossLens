package com.crosslens.app.data.ingestion

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Monitors RSS source health across fetch attempts.
 *
 * Tracks success/failure rates, article quality metrics, and determines
 * whether each source should be active, degraded, or disabled.
 */
@Singleton
class SourceHealthMonitor @Inject constructor() {

    private val mutex = Mutex()
    private val healthChecks = mutableMapOf<String, MutableList<SourceHealthCheck>>()
    private val sourceStatuses = mutableMapOf<String, SourceHealthStatus>()
    private val disabledReasons = mutableMapOf<String, String>()

    // Configuration
    private val maxChecksPerSource = 50 // Keep last 50 checks
    private val degradedThreshold = 3 // 3 consecutive failures = degraded
    private val disabledThreshold = 10 // 10 consecutive failures = disabled
    private val healthCheckWindow = Duration.ofHours(24)

    /**
     * Record a health check for a source.
     */
    suspend fun recordCheck(check: SourceHealthCheck) = mutex.withLock {
        val checks = healthChecks.getOrPut(check.sourceId) { mutableListOf() }
        checks.add(check)

        // Keep only recent checks
        if (checks.size > maxChecksPerSource) {
            checks.removeAt(0)
        }

        // Update status based on consecutive failures
        updateSourceStatus(check.sourceId, checks)
    }

    /**
     * Mark a source as manually disabled with reason.
     */
    suspend fun disableSource(sourceId: String, reason: String) = mutex.withLock {
        sourceStatuses[sourceId] = SourceHealthStatus.DISABLED
        disabledReasons[sourceId] = reason
    }

    /**
     * Get current health summary for a source.
     */
    suspend fun getSourceSummary(sourceId: String, sourceName: String): SourceHealthSummary = mutex.withLock {
        val checks = healthChecks[sourceId] ?: emptyList()
        val status = sourceStatuses[sourceId] ?: SourceHealthStatus.ACTIVE

        val recent24h = checks.filter {
            Duration.between(it.checkedAt, Instant.now()) <= healthCheckWindow
        }

        val lastSuccess = checks.lastOrNull { it.fetchSucceeded && it.parseSucceeded }
        val lastFailure = checks.lastOrNull { !it.fetchSucceeded || !it.parseSucceeded }
        val consecutiveFailures = countConsecutiveFailures(checks)

        val successRate = if (recent24h.isNotEmpty()) {
            recent24h.count { it.fetchSucceeded && it.parseSucceeded }.toDouble() / recent24h.size
        } else {
            1.0 // No data = assume healthy until proven otherwise
        }

        val totalArticles = recent24h.sumOf { it.articlesReturned }
        val totalWithImages = recent24h.sumOf { it.articlesWithImages }
        val imageRate = if (totalArticles > 0) {
            totalWithImages.toDouble() / totalArticles
        } else {
            0.0
        }

        SourceHealthSummary(
            sourceId = sourceId,
            sourceName = sourceName,
            status = status,
            lastSuccessAt = lastSuccess?.checkedAt,
            lastFailureAt = lastFailure?.checkedAt,
            consecutiveFailures = consecutiveFailures,
            last24hSuccessRate = successRate,
            last24hArticleCount = totalArticles,
            last24hImageRate = imageRate,
            recentChecks = checks.takeLast(10),
            disabledReason = disabledReasons[sourceId]
        )
    }

    /**
     * Generate overall feed health report.
     */
    suspend fun generateReport(
        configuredSources: List<Pair<String, String>> // (sourceId, sourceName)
    ): FeedHealthReport = mutex.withLock {
        val summaries = configuredSources.map { (id, name) ->
            getSourceSummary(id, name)
        }

        val activeCount = summaries.count { it.status == SourceHealthStatus.ACTIVE }
        val degradedCount = summaries.count { it.status == SourceHealthStatus.DEGRADED }
        val disabledCount = summaries.count { it.status == SourceHealthStatus.DISABLED }

        val overallSuccessRate = if (summaries.isNotEmpty()) {
            summaries.map { it.last24hSuccessRate }.average()
        } else {
            0.0
        }

        FeedHealthReport(
            generatedAt = Instant.now(),
            totalConfigured = configuredSources.size,
            activeCount = activeCount,
            degradedCount = degradedCount,
            disabledCount = disabledCount,
            overallSuccessRate = overallSuccessRate,
            sourceSummaries = summaries.sortedByDescending { it.status.ordinal }
        )
    }

    /**
     * Check if a source should be used for fetching.
     */
    suspend fun isSourceEnabled(sourceId: String): Boolean = mutex.withLock {
        sourceStatuses[sourceId] != SourceHealthStatus.DISABLED
    }

    private fun updateSourceStatus(sourceId: String, checks: List<SourceHealthCheck>) {
        val consecutiveFailures = countConsecutiveFailures(checks)

        val newStatus = when {
            consecutiveFailures >= disabledThreshold -> SourceHealthStatus.DISABLED
            consecutiveFailures >= degradedThreshold -> SourceHealthStatus.DEGRADED
            else -> SourceHealthStatus.ACTIVE
        }

        sourceStatuses[sourceId] = newStatus

        // Auto-disable reason for persistent failures
        if (newStatus == SourceHealthStatus.DISABLED && disabledReasons[sourceId] == null) {
            val lastCheck = checks.lastOrNull()
            disabledReasons[sourceId] = "Disabled automatically after $consecutiveFailures consecutive failures. Last error: ${lastCheck?.errorMessage ?: "Unknown"}"
        }
    }

    private fun countConsecutiveFailures(checks: List<SourceHealthCheck>): Int {
        var count = 0
        for (check in checks.asReversed()) {
            if (!check.fetchSucceeded || !check.parseSucceeded) {
                count++
            } else {
                break
            }
        }
        return count
    }
}
