package com.crosslens.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Persisted editorial review decision for ingestion candidates.
 * Covers cross-language matches, syndication groups, and cluster proposals.
 */
@Entity(tableName = "editorial_reviews")
data class EditorialReviewEntity(
    @PrimaryKey val id: String,
    val candidateType: String, // "CROSS_LANGUAGE", "SYNDICATION", "CLUSTER"
    val candidateId: String,
    val decision: String, // "APPROVED", "REJECTED", "DEFERRED"
    val note: String,
    val reviewedBy: String, // "demo-editor" for prototype
    val reviewedAt: Instant,

    // Evidence preserved for auditability
    val articleIds: List<String>,
    val sourceIds: List<String>,
    val languages: List<String>,
    val sharedEntities: List<String>?,
    val confidence: String?, // For entity/syndication matches
    val method: String, // Detection/matching method version
    val rationale: String,
    val uncertaintyReasons: List<String>
)
