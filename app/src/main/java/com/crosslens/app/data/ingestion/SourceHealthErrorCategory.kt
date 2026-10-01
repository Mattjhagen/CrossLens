package com.crosslens.app.data.ingestion

/**
 * Technical error categories for source health diagnostics.
 * Based only on observable fetch/parse failures, never on editorial characteristics.
 */
enum class SourceHealthErrorCategory {
    /** Network timeout or connection failure */
    NETWORK_ERROR,

    /** HTTP error response (4xx, 5xx) */
    HTTP_ERROR,

    /** Feed XML/RSS parse failure */
    PARSE_ERROR,

    /** Feed returned zero articles */
    EMPTY_FEED,

    /** All returned articles are duplicates (stale feed) */
    DUPLICATE_ONLY,

    /** Articles have invalid or missing publication dates */
    INVALID_DATES,

    /** Feed hasn't updated in expected timeframe (stale) */
    STALE_FEED,

    /** Missing required attribution or HTTPS links */
    ATTRIBUTION_ERROR,

    /** Unknown or uncategorized error */
    UNKNOWN
}

/**
 * Categorize an error message into a diagnostic category.
 * Uses only technical evidence from the error, never editorial judgments.
 */
fun categorizeError(errorMessage: String?): SourceHealthErrorCategory {
    if (errorMessage == null) return SourceHealthErrorCategory.UNKNOWN

    val lower = errorMessage.lowercase()

    return when {
        lower.contains("timeout") || lower.contains("sockettime") -> SourceHealthErrorCategory.NETWORK_ERROR
        lower.contains("connection") || lower.contains("unreachablehost") -> SourceHealthErrorCategory.NETWORK_ERROR
        lower.contains("http 4") || lower.contains("http 5") -> SourceHealthErrorCategory.HTTP_ERROR
        lower.contains("parse") || lower.contains("xml") || lower.contains("malformed") -> SourceHealthErrorCategory.PARSE_ERROR
        lower.contains("empty") && lower.contains("feed") -> SourceHealthErrorCategory.EMPTY_FEED
        lower.contains("duplicate") -> SourceHealthErrorCategory.DUPLICATE_ONLY
        lower.contains("stale") -> SourceHealthErrorCategory.STALE_FEED
        lower.contains("invalid date") || lower.contains("missing date") -> SourceHealthErrorCategory.INVALID_DATES
        lower.contains("attribution") || lower.contains("https") -> SourceHealthErrorCategory.ATTRIBUTION_ERROR
        else -> SourceHealthErrorCategory.UNKNOWN
    }
}
