package com.crosslens.app.data.ingestion

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Diagnostic utility for source health reporting and analysis.
 *
 * This utility generates detailed health reports and identifies sources
 * needing attention before production deployment.
 */
@Singleton
class SourceHealthDiagnostic @Inject constructor(
    private val healthMonitor: SourceHealthMonitor,
    private val rssAdapters: List<RssSourceAdapter>
) {

    companion object {
        private const val TAG = "SourceHealthDiagnostic"
        private const val PRODUCTION_READY_THRESHOLD = 0.90 // 90% success rate
    }

    /**
     * Generate and log comprehensive health report.
     * Returns the report for programmatic access.
     */
    suspend fun generateAndLogReport(): FeedHealthReport {
        val sourcesWithNames = rssAdapters.map { it.sourceId to it.sourceName }
        val report = healthMonitor.generateReport(sourcesWithNames)

        logReport(report)
        return report
    }

    /**
     * Log detailed health report with formatting.
     */
    private fun logReport(report: FeedHealthReport) {
        Log.i(TAG, "═══════════════════════════════════════════════════════════")
        Log.i(TAG, "SOURCE HEALTH REPORT")
        Log.i(TAG, "Generated: ${report.generatedAt}")
        Log.i(TAG, "═══════════════════════════════════════════════════════════")
        Log.i(TAG, "")
        Log.i(TAG, "SUMMARY:")
        Log.i(TAG, "  Total Configured: ${report.totalConfigured}")
        Log.i(TAG, "  Active: ${report.activeCount} (${formatPercent(report.activeCount, report.totalConfigured)})")
        Log.i(TAG, "  Degraded: ${report.degradedCount}")
        Log.i(TAG, "  Disabled: ${report.disabledCount}")
        Log.i(TAG, "  Overall Success Rate: ${formatPercent(report.overallSuccessRate)}")
        Log.i(TAG, "  Status: ${report.getStatusMessage()}")
        Log.i(TAG, "  Production Ready: ${if (report.isProductionReady()) "✅ YES" else "❌ NO"}")
        Log.i(TAG, "")

        // Group sources by status
        val active = report.sourceSummaries.filter { it.status == SourceHealthStatus.ACTIVE }
        val degraded = report.sourceSummaries.filter { it.status == SourceHealthStatus.DEGRADED }
        val disabled = report.sourceSummaries.filter { it.status == SourceHealthStatus.DISABLED }

        if (active.isNotEmpty()) {
            Log.i(TAG, "───────────────────────────────────────────────────────────")
            Log.i(TAG, "ACTIVE SOURCES (${active.size}):")
            Log.i(TAG, "───────────────────────────────────────────────────────────")
            active.forEach { summary ->
                logSourceSummary(summary)
            }
            Log.i(TAG, "")
        }

        if (degraded.isNotEmpty()) {
            Log.w(TAG, "───────────────────────────────────────────────────────────")
            Log.w(TAG, "DEGRADED SOURCES (${degraded.size}):")
            Log.w(TAG, "───────────────────────────────────────────────────────────")
            degraded.forEach { summary ->
                logSourceSummary(summary, isWarning = true)
            }
            Log.w(TAG, "")
        }

        if (disabled.isNotEmpty()) {
            Log.e(TAG, "───────────────────────────────────────────────────────────")
            Log.e(TAG, "DISABLED SOURCES (${disabled.size}):")
            Log.e(TAG, "───────────────────────────────────────────────────────────")
            disabled.forEach { summary ->
                logSourceSummary(summary, isError = true)
            }
            Log.e(TAG, "")
        }

        // Production readiness assessment
        Log.i(TAG, "═══════════════════════════════════════════════════════════")
        Log.i(TAG, "PRODUCTION READINESS ASSESSMENT:")
        Log.i(TAG, "═══════════════════════════════════════════════════════════")

        val needToFix = (report.totalConfigured * PRODUCTION_READY_THRESHOLD).toInt() - report.activeCount

        if (report.isProductionReady()) {
            Log.i(TAG, "✅ READY: Feed meets production criteria (90%+ success rate)")
        } else {
            Log.e(TAG, "❌ NOT READY: Need to fix $needToFix more source(s) to reach 90% threshold")
            Log.e(TAG, "   Current: ${report.activeCount}/${report.totalConfigured} active")
            Log.e(TAG, "   Target: ${(report.totalConfigured * PRODUCTION_READY_THRESHOLD).toInt()}/${report.totalConfigured} active")
        }

        Log.i(TAG, "═══════════════════════════════════════════════════════════")
    }

    /**
     * Log individual source summary.
     */
    private fun logSourceSummary(summary: SourceHealthSummary, isWarning: Boolean = false, isError: Boolean = false) {
        val logFn: (String, String) -> Int = when {
            isError -> { tag, msg -> Log.e(tag, msg) }
            isWarning -> { tag, msg -> Log.w(tag, msg) }
            else -> { tag, msg -> Log.i(tag, msg) }
        }

        logFn(TAG, "")
        logFn(TAG, "${summary.sourceName} (${summary.sourceId})")
        logFn(TAG, "  Status: ${summary.status}")

        if (summary.lastSuccessAt != null) {
            logFn(TAG, "  Last Success: ${summary.lastSuccessAt}")
        } else {
            logFn(TAG, "  Last Success: NEVER")
        }

        if (summary.lastFailureAt != null) {
            logFn(TAG, "  Last Failure: ${summary.lastFailureAt}")
        }

        logFn(TAG, "  Consecutive Failures: ${summary.consecutiveFailures}")
        logFn(TAG, "  24h Success Rate: ${formatPercent(summary.last24hSuccessRate)}")
        logFn(TAG, "  24h Articles: ${summary.last24hArticleCount}")
        logFn(TAG, "  24h Image Rate: ${formatPercent(summary.last24hImageRate)}")

        if (summary.disabledReason != null) {
            logFn(TAG, "  Disabled Reason: ${summary.disabledReason}")
        }

        // Show recent error if present
        val lastError = summary.recentChecks.lastOrNull { it.errorMessage != null }
        if (lastError != null) {
            logFn(TAG, "  Last Error: ${lastError.errorMessage}")
        }
    }

    /**
     * Format percentage for display.
     */
    private fun formatPercent(value: Double): String {
        return String.format("%.1f%%", value * 100)
    }

    /**
     * Format percentage for display (count/total).
     */
    private fun formatPercent(count: Int, total: Int): String {
        if (total == 0) return "0.0%"
        return formatPercent(count.toDouble() / total)
    }

    /**
     * Identify sources that need diagnosis.
     * Returns list of (sourceId, sourceName, issue description).
     */
    suspend fun identifyProblematicSources(): List<Triple<String, String, String>> {
        val sourcesWithNames = rssAdapters.map { it.sourceId to it.sourceName }
        val report = healthMonitor.generateReport(sourcesWithNames)

        return report.sourceSummaries
            .filter { it.status != SourceHealthStatus.ACTIVE }
            .map { summary ->
                val issue = when {
                    summary.consecutiveFailures >= 10 -> "DISABLED: ${summary.consecutiveFailures} consecutive failures"
                    summary.consecutiveFailures >= 3 -> "DEGRADED: ${summary.consecutiveFailures} consecutive failures"
                    summary.last24hSuccessRate < 0.5 -> "Low success rate: ${formatPercent(summary.last24hSuccessRate)}"
                    else -> "Unknown issue"
                }
                Triple(summary.sourceId, summary.sourceName, issue)
            }
    }
}
