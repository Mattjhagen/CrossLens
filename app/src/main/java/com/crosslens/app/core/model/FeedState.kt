package com.crosslens.app.core.model

import java.time.Instant

/**
 * Feed data source state for UI display.
 */
enum class FeedState {
    /** Currently fetching live data */
    LOADING,

    /** Displaying live data just fetched */
    LIVE,

    /** Displaying cached live data (offline or stale) */
    CACHED,

    /** All live sources failed, showing mock demo data */
    DEMO_FALLBACK,

    /** Error state with no usable data */
    ERROR
}

/**
 * Metadata about the feed's current state and freshness.
 */
data class FeedMetadata(
    val state: FeedState,
    val lastUpdated: Instant?,
    val lastRefreshAttempt: Instant?,
    val successfulSourceCount: Int,
    val failedSourceCount: Int,
    val errorMessage: String?
)
