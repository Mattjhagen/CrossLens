package com.crosslens.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Persists editorial decisions for audit trail and review history.
 * Every story in the database has a corresponding approved decision.
 */
@Entity(tableName = "editorial_decisions")
data class EditorialDecisionEntity(
    @PrimaryKey val id: String,
    val proposalId: String,
    val clusterId: String,
    val decision: String, // APPROVED, REJECTED, DEFERRED
    val reason: String,
    val reviewerId: String,
    val decidedAt: Instant,

    /** References to all article IDs in the cluster */
    val articleIds: List<String>,

    /** Source IDs represented in the cluster */
    val sourceIds: List<String>,

    /** Provisional title from the clustering algorithm */
    val provisionalTitle: String,

    /** Final title (may differ if editor revised) */
    val finalTitle: String
)
