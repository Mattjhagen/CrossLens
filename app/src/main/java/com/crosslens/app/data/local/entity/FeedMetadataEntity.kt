package com.crosslens.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Stores metadata about the most recent live feed refresh.
 * Single row with id = "live_feed" tracks cache freshness.
 */
@Entity(tableName = "feed_metadata")
data class FeedMetadataEntity(
    @PrimaryKey val id: String = "live_feed",
    val lastSuccessfulFetch: Instant?,
    val lastAttemptedFetch: Instant?,
    val successfulSourceCount: Int,
    val failedSourceCount: Int,
    val totalArticleCount: Int
)
