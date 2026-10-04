package com.crosslens.app.util

import android.util.Log

/**
 * Diagnostic logging for Source Health state path debugging.
 *
 * Only enabled in debug builds. No-op in release builds.
 */
object SourceHealthDiagnostics {
    private const val TAG = "SourceHealth:Debug"

    // Enable only in debug builds
    var enabled = com.crosslens.app.BuildConfig.DEBUG

    fun logViewModelRefreshStart() {
        if (!enabled) return
        Log.d(TAG, "═══ ViewModel: refreshAllSources() START ═══")
        Log.d(TAG, "  Thread: ${Thread.currentThread().name}")
        Log.d(TAG, "  Time: ${System.currentTimeMillis()}")
    }

    fun logViewModelRefreshEnd(successCount: Int, failureCount: Int) {
        if (!enabled) return
        Log.d(TAG, "═══ ViewModel: refreshAllSources() END ═══")
        Log.d(TAG, "  Success: $successCount, Failures: $failureCount")
        Log.d(TAG, "  Time: ${System.currentTimeMillis()}")
    }

    fun logViewModelFlowEmission(sourceCount: Int, lastRefreshTimestamp: Long?) {
        if (!enabled) return
        Log.d(TAG, "─── ViewModel: Flow emission received ───")
        Log.d(TAG, "  Source count: $sourceCount")
        Log.d(TAG, "  Last refresh timestamp: $lastRefreshTimestamp")
        Log.d(TAG, "  Time: ${System.currentTimeMillis()}")
    }

    fun logViewModelStateUpdate(
        activeCount: Int,
        degradedCount: Int,
        disabledCount: Int,
        lastRefreshTimestamp: Long?,
        isRefreshing: Boolean
    ) {
        if (!enabled) return
        Log.d(TAG, "─── ViewModel: UI State update ───")
        Log.d(TAG, "  Active: $activeCount, Degraded: $degradedCount, Disabled: $disabledCount")
        Log.d(TAG, "  Last refresh: $lastRefreshTimestamp")
        Log.d(TAG, "  Is refreshing: $isRefreshing")
        Log.d(TAG, "  Time: ${System.currentTimeMillis()}")
    }

    fun logRepositoryRefreshStart(adapterCount: Int) {
        if (!enabled) return
        Log.d(TAG, "═══ Repository: refreshAllSources() START ═══")
        Log.d(TAG, "  Adapter count: $adapterCount")
        Log.d(TAG, "  Thread: ${Thread.currentThread().name}")
        Log.d(TAG, "  Time: ${System.currentTimeMillis()}")
    }

    fun logRepositoryRefreshEnd(successCount: Int, failureCount: Int) {
        if (!enabled) return
        Log.d(TAG, "═══ Repository: refreshAllSources() END ═══")
        Log.d(TAG, "  Success: $successCount, Failures: $failureCount")
        Log.d(TAG, "  Time: ${System.currentTimeMillis()}")
    }

    fun logRepositoryAdapterStart(sourceId: String, sourceName: String) {
        if (!enabled) return
        Log.d(TAG, "  ┌─ Adapter: $sourceName ($sourceId)")
    }

    fun logRepositoryAdapterSuccess(sourceId: String, articleCount: Int, durationMs: Long) {
        if (!enabled) return
        Log.d(TAG, "  └─ SUCCESS: $articleCount articles in ${durationMs}ms")
    }

    fun logRepositoryAdapterFailure(sourceId: String, error: String) {
        if (!enabled) return
        Log.d(TAG, "  └─ FAILURE: $error")
    }

    fun logMonitorRecordCheckStart(sourceId: String, fetchSucceeded: Boolean, parseSucceeded: Boolean) {
        if (!enabled) return
        Log.d(TAG, "═══ Monitor: recordCheck() START ═══")
        Log.d(TAG, "  Source ID: $sourceId")
        Log.d(TAG, "  Fetch succeeded: $fetchSucceeded")
        Log.d(TAG, "  Parse succeeded: $parseSucceeded")
        Log.d(TAG, "  Thread: ${Thread.currentThread().name}")
        Log.d(TAG, "  Time: ${System.currentTimeMillis()}")
    }

    fun logMonitorStatusUpdate(
        sourceId: String,
        sourceName: String,
        consecutiveFailures: Int,
        newStatus: String
    ) {
        if (!enabled) return
        Log.d(TAG, "─── Monitor: updateSourceStatus() ───")
        Log.d(TAG, "  Source: $sourceName ($sourceId)")
        Log.d(TAG, "  Consecutive failures: $consecutiveFailures")
        Log.d(TAG, "  New status: $newStatus")
    }

    fun logMonitorDatabaseWrite(sourceId: String, status: String, updatedAtMs: Long) {
        if (!enabled) return
        Log.d(TAG, "─── Monitor: Writing to database ───")
        Log.d(TAG, "  Source ID: $sourceId")
        Log.d(TAG, "  Status: $status")
        Log.d(TAG, "  Updated at: $updatedAtMs")
        Log.d(TAG, "  Time: ${System.currentTimeMillis()}")
    }

    fun logMonitorDatabaseWriteComplete(sourceId: String) {
        if (!enabled) return
        Log.d(TAG, "✓ Database write completed for $sourceId")
        Log.d(TAG, "  Time: ${System.currentTimeMillis()}")
    }

    fun logDaoQuery(operation: String, sourceId: String? = null) {
        if (!enabled) return
        Log.d(TAG, "─── DAO: $operation ${sourceId?.let { "for $it" } ?: ""} ───")
        Log.d(TAG, "  Thread: ${Thread.currentThread().name}")
        Log.d(TAG, "  Time: ${System.currentTimeMillis()}")
    }

    fun logDaoQueryResult(rowCount: Int) {
        if (!enabled) return
        Log.d(TAG, "  Result: $rowCount rows")
    }

    fun logLoadPersistedState(rowsLoaded: Int) {
        if (!enabled) return
        Log.d(TAG, "═══ Monitor: loadPersistedState() ═══")
        Log.d(TAG, "  Rows loaded from database: $rowsLoaded")
        Log.d(TAG, "  Time: ${System.currentTimeMillis()}")
    }

    fun logDatabaseInitialization(sourceCount: Int) {
        if (!enabled) return
        Log.d(TAG, "═══ Monitor: initializeSourceHealth() ═══")
        Log.d(TAG, "  Initializing $sourceCount sources")
        Log.d(TAG, "  Time: ${System.currentTimeMillis()}")
    }

    fun logDatabaseInitializationComplete(sourceCount: Int) {
        if (!enabled) return
        Log.d(TAG, "✓ Database initialization complete: $sourceCount sources created")
        Log.d(TAG, "  Time: ${System.currentTimeMillis()}")
    }
}
