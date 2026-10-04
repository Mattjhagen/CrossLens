package com.crosslens.app.data.ingestion

import com.crosslens.app.data.local.dao.SourceHealthDao
import com.crosslens.app.data.local.entity.SourceHealthEntity
import com.crosslens.app.util.SourceHealthDiagnostics
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
 *
 * Persists health state to database for durability across app restarts.
 */
@Singleton
class SourceHealthMonitor @Inject constructor(
    private val healthDao: SourceHealthDao
) {

    private val mutex = Mutex()
    private val healthChecks = mutableMapOf<String, MutableList<SourceHealthCheck>>()
    private val sourceStatuses = mutableMapOf<String, SourceHealthStatus>()
    private val disabledReasons = mutableMapOf<String, String>()
    private val lastErrorCategories = mutableMapOf<String, SourceHealthErrorCategory>()

    // Configuration
    private val maxChecksPerSource = 50 // Keep last 50 checks
    private val degradedThreshold = 3 // 3 consecutive failures = degraded
    private val disabledThreshold = 10 // 10 consecutive failures = disabled
    private val healthCheckWindow = Duration.ofHours(24)

    /**
     * Record a health check for a source and persist to database.
     */
    suspend fun recordCheck(check: SourceHealthCheck, sourceName: String) = mutex.withLock {
        // DIAGNOSTIC: Log record check start
        SourceHealthDiagnostics.logMonitorRecordCheckStart(
            sourceId = check.sourceId,
            fetchSucceeded = check.fetchSucceeded,
            parseSucceeded = check.parseSucceeded
        )

        val checks = healthChecks.getOrPut(check.sourceId) { mutableListOf() }
        checks.add(check)

        // Keep only recent checks
        if (checks.size > maxChecksPerSource) {
            checks.removeAt(0)
        }

        // Categorize error if present
        if (check.errorMessage != null) {
            lastErrorCategories[check.sourceId] = categorizeError(check.errorMessage)
        }

        // Update status based on consecutive failures
        updateSourceStatus(check.sourceId, sourceName, checks)
    }

    /**
     * Mark a source as manually disabled with reason and persist.
     */
    suspend fun disableSource(sourceId: String, sourceName: String, reason: String) = mutex.withLock {
        sourceStatuses[sourceId] = SourceHealthStatus.DISABLED
        disabledReasons[sourceId] = reason

        // Persist to database
        val existing = healthDao.getHealth(sourceId)
        if (existing != null) {
            healthDao.upsert(
                existing.copy(
                    status = SourceHealthStatus.DISABLED.name,
                    disabledReason = reason,
                    updatedAt = Instant.now().toEpochMilli()
                )
            )
        }
    }

    /**
     * Initialize database with entries for all configured sources.
     * Creates ACTIVE entries for sources that don't have a record yet.
     *
     * Call this on app startup AFTER loadPersistedState() to ensure
     * all configured sources have database entries.
     *
     * @param configuredSources List of (sourceId, sourceName) pairs
     */
    suspend fun initializeSourceHealth(
        configuredSources: List<Pair<String, String>>
    ) = mutex.withLock {
        // DIAGNOSTIC: Log initialization start
        SourceHealthDiagnostics.logDatabaseInitialization(sourceCount = configuredSources.size)

        val now = Instant.now().toEpochMilli()
        var createdCount = 0

        for ((sourceId, sourceName) in configuredSources) {
            // Check if this source already has a database entry
            val existing = healthDao.getHealth(sourceId)
            if (existing == null) {
                // Create initial ACTIVE entry
                val initialEntity = SourceHealthEntity(
                    sourceId = sourceId,
                    sourceName = sourceName,
                    status = SourceHealthStatus.ACTIVE.name,
                    lastSuccessAt = null,
                    lastFailureAt = null,
                    consecutiveFailures = 0,
                    last24hSuccessRate = 1.0, // Assume healthy until proven otherwise
                    last24hArticleCount = 0,
                    lastErrorMessage = null,
                    lastErrorCategory = null,
                    disabledReason = null,
                    updatedAt = now
                )
                healthDao.upsert(initialEntity)
                sourceStatuses[sourceId] = SourceHealthStatus.ACTIVE
                createdCount++
            }
        }

        // DIAGNOSTIC: Log initialization complete
        SourceHealthDiagnostics.logDatabaseInitializationComplete(sourceCount = createdCount)
    }

    /**
     * Load persisted health state from database on initialization.
     * Call this after app startup to restore health state.
     */
    suspend fun loadPersistedState() = mutex.withLock {
        val allHealth = healthDao.getAllHealth()

        // DIAGNOSTIC: Log persisted state load
        SourceHealthDiagnostics.logLoadPersistedState(rowsLoaded = allHealth.size)

        for (entity in allHealth) {
            sourceStatuses[entity.sourceId] = SourceHealthStatus.valueOf(entity.status)
            entity.disabledReason?.let { disabledReasons[entity.sourceId] = it }
            entity.lastErrorCategory?.let {
                lastErrorCategories[entity.sourceId] = SourceHealthErrorCategory.valueOf(it)
            }
        }
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

    private suspend fun updateSourceStatus(sourceId: String, sourceName: String, checks: List<SourceHealthCheck>) {
        val consecutiveFailures = countConsecutiveFailures(checks)

        val newStatus = when {
            consecutiveFailures >= disabledThreshold -> SourceHealthStatus.DISABLED
            consecutiveFailures >= degradedThreshold -> SourceHealthStatus.DEGRADED
            else -> SourceHealthStatus.ACTIVE
        }

        // DIAGNOSTIC: Log status update
        SourceHealthDiagnostics.logMonitorStatusUpdate(
            sourceId = sourceId,
            sourceName = sourceName,
            consecutiveFailures = consecutiveFailures,
            newStatus = newStatus.name
        )

        sourceStatuses[sourceId] = newStatus

        // Auto-disable reason for persistent failures
        if (newStatus == SourceHealthStatus.DISABLED && disabledReasons[sourceId] == null) {
            val lastCheck = checks.lastOrNull()
            disabledReasons[sourceId] = "Disabled automatically after $consecutiveFailures consecutive failures. Last error: ${lastCheck?.errorMessage ?: "Unknown"}"
        }

        // Calculate metrics for persistence
        val recent24h = checks.filter {
            Duration.between(it.checkedAt, Instant.now()) <= healthCheckWindow
        }

        val lastSuccess = checks.lastOrNull { it.fetchSucceeded && it.parseSucceeded }
        val lastFailure = checks.lastOrNull { !it.fetchSucceeded || !it.parseSucceeded }

        val successRate = if (recent24h.isNotEmpty()) {
            recent24h.count { it.fetchSucceeded && it.parseSucceeded }.toDouble() / recent24h.size
        } else {
            1.0
        }

        val totalArticles = recent24h.sumOf { it.articlesReturned }

        val updatedAtMs = Instant.now().toEpochMilli()

        // Persist to database
        val entity = SourceHealthEntity(
            sourceId = sourceId,
            sourceName = sourceName,
            status = newStatus.name,
            lastSuccessAt = lastSuccess?.checkedAt?.toEpochMilli(),
            lastFailureAt = lastFailure?.checkedAt?.toEpochMilli(),
            consecutiveFailures = consecutiveFailures,
            last24hSuccessRate = successRate,
            last24hArticleCount = totalArticles,
            lastErrorMessage = lastFailure?.errorMessage,
            lastErrorCategory = lastErrorCategories[sourceId]?.name,
            disabledReason = disabledReasons[sourceId],
            updatedAt = updatedAtMs
        )

        // DIAGNOSTIC: Log database write
        SourceHealthDiagnostics.logMonitorDatabaseWrite(
            sourceId = sourceId,
            status = newStatus.name,
            updatedAtMs = updatedAtMs
        )

        healthDao.upsert(entity)

        // DIAGNOSTIC: Log database write complete
        SourceHealthDiagnostics.logMonitorDatabaseWriteComplete(sourceId = sourceId)
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
