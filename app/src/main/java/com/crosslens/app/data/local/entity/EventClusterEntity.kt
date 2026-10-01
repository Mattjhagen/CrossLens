package com.crosslens.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.crosslens.app.data.local.Converters
import java.time.Instant

/**
 * Persistent storage for event clusters.
 * Stores grouping metadata and references to clustered articles.
 */
@Entity(tableName = "event_clusters")
@TypeConverters(Converters::class)
data class EventClusterEntity(
    @PrimaryKey
    val id: String,

    /** Human-readable event summary */
    val eventSummary: String,

    /** When the event occurred (earliest publication time) */
    val eventTime: Instant,

    /** When this cluster was created/updated */
    val clusteredAt: Instant,

    /** Confidence level: HIGH, MEDIUM, LOW */
    val confidence: String,

    /** Explanation of grouping decision */
    val groupingExplanation: String,

    /** Common named entities (JSON array) */
    val commonEntities: List<String>,

    /** Number of distinct publishers */
    val publisherCount: Int,

    /** Primary image URL (if available) */
    val imageUrl: String? = null,

    /** Article IDs in this cluster */
    val articleIds: List<String>
)
