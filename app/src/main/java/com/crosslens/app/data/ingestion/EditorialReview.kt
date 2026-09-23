package com.crosslens.app.data.ingestion

import java.time.Instant

/**
 * An editorial decision on whether to publish a proposed event cluster.
 * Every cluster must be explicitly approved before appearing to readers.
 */
data class EditorialDecision(
    /** The cluster proposal being reviewed */
    val proposalId: String,

    /** Review outcome */
    val decision: DecisionType,

    /** Reason for the decision (required for reject/defer) */
    val reason: String,

    /** Reviewer identifier (for audit trail) */
    val reviewerId: String,

    /** When this decision was made */
    val decidedAt: Instant
)

enum class DecisionType {
    /** Cluster is approved and should be published */
    APPROVED,

    /** Cluster is rejected and should not be published */
    REJECTED,

    /** Cluster needs more review or additional sources */
    DEFERRED
}

/**
 * A reviewed cluster ready for persistence.
 * Only approved clusters become stories in the database.
 */
data class ApprovedCluster(
    /** Stable cluster ID */
    val clusterId: String,

    /** Final title for the story (may be edited from provisional title) */
    val title: String,

    /** Event summary (may be derived or editor-supplied) */
    val summary: String,

    /** First article publication time in the cluster */
    val firstPublishedAt: Instant,

    /** Most recent article publication time */
    val lastPublishedAt: Instant,

    /** All normalized articles in this cluster */
    val articles: List<NormalizedArticle>,

    /** Editorial decision record */
    val decision: EditorialDecision,

    /** Topic IDs assigned by the reviewer */
    val topicIds: List<String> = emptyList(),

    /** Event country codes (locations, not source countries) */
    val eventCountryCodes: List<String> = emptyList()
)

/**
 * Tracks the review status of a cluster proposal.
 */
data class ReviewRecord(
    val proposalId: String,
    val status: ReviewStatus,
    val decision: EditorialDecision? = null,
    val createdAt: Instant,
    val updatedAt: Instant
)

enum class ReviewStatus {
    /** Awaiting initial review */
    PENDING,

    /** Under active review */
    IN_REVIEW,

    /** Decision made (check decision field) */
    DECIDED
}

/**
 * Validates editorial decisions meet quality requirements.
 */
object EditorialValidator {
    fun validate(decision: EditorialDecision) {
        require(decision.proposalId.isNotBlank()) {
            "Decision must reference a proposal ID"
        }
        require(decision.reviewerId.isNotBlank()) {
            "Decision must have a reviewer ID"
        }
        when (decision.decision) {
            DecisionType.REJECTED, DecisionType.DEFERRED -> {
                require(decision.reason.isNotBlank()) {
                    "${decision.decision} decisions require a reason"
                }
            }
            DecisionType.APPROVED -> {
                // Approved decisions can have optional reason
            }
        }
    }

    fun validateApprovedCluster(cluster: ApprovedCluster) {
        require(cluster.articles.size >= 2) {
            "Approved cluster must have at least 2 articles from different sources"
        }
        require(cluster.articles.map { it.sourceId }.distinct().size >= 2) {
            "Approved cluster must have articles from at least 2 different sources"
        }
        require(cluster.title.isNotBlank()) {
            "Approved cluster must have a non-blank title"
        }
        require(cluster.summary.isNotBlank()) {
            "Approved cluster must have a non-blank summary"
        }
        require(cluster.decision.decision == DecisionType.APPROVED) {
            "Approved cluster must have APPROVED decision"
        }
    }
}
