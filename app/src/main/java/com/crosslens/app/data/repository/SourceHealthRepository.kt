package com.crosslens.app.data.repository

import com.crosslens.app.data.ingestion.FeedHealthReport
import com.crosslens.app.data.ingestion.RssSourceAdapter
import com.crosslens.app.data.ingestion.SourceHealthCheck
import com.crosslens.app.data.ingestion.SourceHealthMonitor
import com.crosslens.app.data.ingestion.SourceHealthSummary
import com.crosslens.app.data.local.dao.SourceHealthDao
import com.crosslens.app.data.local.entity.SourceHealthEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for source health data and operations.
 * Provides UI-layer access to health monitoring and manual refresh.
 */
@Singleton
class SourceHealthRepository @Inject constructor(
    private val healthDao: SourceHealthDao,
    private val healthMonitor: SourceHealthMonitor,
    private val rssAdapters: List<RssSourceAdapter>
) {

    /**
     * Observe all source health as UI-friendly models.
     */
    fun observeAllSourceHealth(): Flow<List<SourceHealthUiModel>> {
        return healthDao.observeAllHealth().map { entities ->
            entities.map { it.toUiModel() }
        }
    }

    /**
     * Get current health report for all configured sources.
     */
    suspend fun getHealthReport(): FeedHealthReport {
        val sourcesWithNames = rssAdapters.map { it.sourceId to it.sourceName }
        return healthMonitor.generateReport(sourcesWithNames)
    }

    /**
     * Manually refresh all sources and return results.
     * This triggers fetches for all configured adapters and updates health state.
     *
     * Health checks are recorded by each adapter during fetchArticles().
     * This method adds a fallback to ensure health state updates even if
     * an unexpected exception bypasses the adapter's internal error handling.
     *
     * @return RefreshResult with successes/failures
     */
    suspend fun refreshAllSources(): RefreshResult = withContext(Dispatchers.IO) {
        val results = mutableListOf<SourceRefreshOutcome>()

        for (adapter in rssAdapters) {
            try {
                val startTime = System.currentTimeMillis()
                val articles = adapter.fetchArticles()
                val duration = System.currentTimeMillis() - startTime

                // fetchArticles() already recorded a health check internally
                results.add(
                    SourceRefreshOutcome.Success(
                        sourceId = adapter.sourceId,
                        sourceName = adapter.sourceName,
                        articlesReturned = articles.size,
                        durationMs = duration
                    )
                )
            } catch (e: Exception) {
                // Fallback: Record a health check if adapter didn't (shouldn't happen normally)
                // The adapter's fetchArticles() has comprehensive error handling and should
                // record health checks for all error paths. This is defense-in-depth.
                healthMonitor.recordCheck(
                    SourceHealthCheck(
                        sourceId = adapter.sourceId,
                        checkedAt = Instant.now(),
                        fetchSucceeded = false,
                        parseSucceeded = false,
                        articlesReturned = 0,
                        articlesWithValidDates = 0,
                        articlesWithImages = 0,
                        articlesWithHttpsLinks = 0,
                        latestArticleAge = null,
                        fetchDurationMs = 0,
                        errorMessage = "Unexpected exception: ${e.javaClass.simpleName}: ${e.message}"
                    ),
                    sourceName = adapter.sourceName
                )

                results.add(
                    SourceRefreshOutcome.Failure(
                        sourceId = adapter.sourceId,
                        sourceName = adapter.sourceName,
                        errorMessage = e.message ?: "Unknown error"
                    )
                )
            }
        }

        val successCount = results.count { it is SourceRefreshOutcome.Success }
        val failureCount = results.count { it is SourceRefreshOutcome.Failure }

        RefreshResult(
            totalSources = rssAdapters.size,
            successCount = successCount,
            failureCount = failureCount,
            outcomes = results,
            completedAt = Instant.now()
        )
    }

    /**
     * Get count of sources needing attention (degraded or disabled).
     */
    suspend fun getProblematicSourcesCount(): Int {
        return healthDao.getProblematicSourcesCount()
    }

    /**
     * Load persisted health state on app startup.
     */
    suspend fun loadPersistedState() {
        healthMonitor.loadPersistedState()
    }
}

/**
 * UI-friendly model for source health display.
 */
data class SourceHealthUiModel(
    val sourceId: String,
    val sourceName: String,
    val status: SourceHealthStatus,
    val lastSuccessAt: Instant?,
    val lastFailureAt: Instant?,
    val consecutiveFailures: Int,
    val last24hSuccessRate: Double,
    val last24hArticleCount: Int,
    val lastErrorMessage: String?,
    val lastErrorCategory: String?,
    val disabledReason: String?,
    val updatedAt: Instant
)

enum class SourceHealthStatus {
    ACTIVE,
    DEGRADED,
    DISABLED
}

/**
 * Result of manual refresh operation.
 */
data class RefreshResult(
    val totalSources: Int,
    val successCount: Int,
    val failureCount: Int,
    val outcomes: List<SourceRefreshOutcome>,
    val completedAt: Instant
) {
    val allSucceeded: Boolean
        get() = failureCount == 0

    val successRate: Double
        get() = if (totalSources > 0) successCount.toDouble() / totalSources else 0.0
}

/**
 * Outcome for a single source refresh.
 */
sealed class SourceRefreshOutcome {
    abstract val sourceId: String
    abstract val sourceName: String

    data class Success(
        override val sourceId: String,
        override val sourceName: String,
        val articlesReturned: Int,
        val durationMs: Long
    ) : SourceRefreshOutcome()

    data class Failure(
        override val sourceId: String,
        override val sourceName: String,
        val errorMessage: String
    ) : SourceRefreshOutcome()
}

/**
 * Convert entity to UI model.
 */
private fun SourceHealthEntity.toUiModel(): SourceHealthUiModel {
    return SourceHealthUiModel(
        sourceId = sourceId,
        sourceName = sourceName,
        status = SourceHealthStatus.valueOf(status),
        lastSuccessAt = lastSuccessAt?.let { Instant.ofEpochMilli(it) },
        lastFailureAt = lastFailureAt?.let { Instant.ofEpochMilli(it) },
        consecutiveFailures = consecutiveFailures,
        last24hSuccessRate = last24hSuccessRate,
        last24hArticleCount = last24hArticleCount,
        lastErrorMessage = lastErrorMessage,
        lastErrorCategory = lastErrorCategory,
        disabledReason = disabledReason,
        updatedAt = Instant.ofEpochMilli(updatedAt)
    )
}
