package com.crosslens.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Persisted source health status.
 * Tracks technical health metrics for each configured RSS source.
 */
@Entity(tableName = "source_health")
data class SourceHealthEntity(
    @PrimaryKey
    val sourceId: String,

    /** Display name for UI */
    val sourceName: String,

    /** Current health status (ACTIVE, DEGRADED, DISABLED) */
    val status: String,

    /** Last successful fetch timestamp */
    val lastSuccessAt: Long?, // Instant.toEpochMilli()

    /** Last failure timestamp */
    val lastFailureAt: Long?, // Instant.toEpochMilli()

    /** Number of consecutive failures */
    val consecutiveFailures: Int,

    /** 24-hour success rate (0.0 to 1.0) */
    val last24hSuccessRate: Double,

    /** 24-hour article count */
    val last24hArticleCount: Int,

    /** Most recent error message */
    val lastErrorMessage: String?,

    /** Most recent error category */
    val lastErrorCategory: String?,

    /** Reason for manual disable */
    val disabledReason: String?,

    /** When this record was last updated */
    val updatedAt: Long // Instant.toEpochMilli()
)
