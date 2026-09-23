package com.crosslens.app.data.ingestion

import java.time.Instant

/**
 * A source adapter emits link-and-excerpt records from an approved news source.
 * It MUST NOT scrape, fetch article bodies, or make unauthorized requests.
 * All permissions and attribution requirements are enforced at the adapter level.
 */
interface SourceAdapter {
    /** Unique identifier matching the source's registered sourceId in the database */
    val sourceId: String

    /** Human-readable name for logging and provenance */
    val sourceName: String

    /**
     * Fetch available articles from this source.
     * Returns only records that have explicit permission to use.
     * Must return empty list rather than throw on transient failures.
     */
    suspend fun fetchArticles(): List<SourceArticleRecord>
}

/**
 * A single article record from a source adapter.
 * Contains only HTTPS link and excerpt data, never full copyrighted bodies.
 */
data class SourceArticleRecord(
    /** HTTPS URL to the original article */
    val url: String,

    /** Publication timestamp from the source */
    val publishedAt: Instant,

    /** BCP 47 language tag of the original article */
    val languageTag: String,

    /** Original headline as supplied by the source */
    val headline: String,

    /** Short excerpt or summary, explicitly permitted for display */
    val excerpt: String,

    /** Optional: content use permission details */
    val contentPermission: ContentPermission = ContentPermission.EXPLICIT_EXCERPT
)

/**
 * Documents the permission model for using this content.
 */
enum class ContentPermission {
    /** Excerpt explicitly provided by source for display (RSS, API, etc.) */
    EXPLICIT_EXCERPT,

    /** Preview text within fair use limits */
    FAIR_USE_PREVIEW,

    /** Licensed content with specific attribution requirements */
    LICENSED_CONTENT
}

/**
 * Validates that a source adapter is properly configured and authorized.
 */
object SourceAdapterValidator {
    fun validate(adapter: SourceAdapter) {
        require(adapter.sourceId.isNotBlank()) {
            "SourceAdapter must have a non-blank sourceId"
        }
        require(adapter.sourceName.isNotBlank()) {
            "SourceAdapter must have a non-blank sourceName"
        }
    }

    fun validateRecord(record: SourceArticleRecord) {
        require(record.url.startsWith("https://")) {
            "Article URL must use HTTPS: ${record.url}"
        }
        require(record.headline.isNotBlank()) {
            "Article headline cannot be blank"
        }
        require(record.languageTag.matches(Regex("[a-z]{2}(-[A-Z]{2})?"))) {
            "Invalid language tag: ${record.languageTag}"
        }
    }
}
