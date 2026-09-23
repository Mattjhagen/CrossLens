package com.crosslens.app.data.repository

import com.crosslens.app.data.local.dao.EditorialReviewDao
import com.crosslens.app.data.local.entity.EditorialReviewEntity
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EditorialReviewRepository @Inject constructor(
    private val editorialReviewDao: EditorialReviewDao
) {
    fun observeAllReviews(): Flow<List<EditorialReviewEntity>> =
        editorialReviewDao.observeAllReviews()

    fun observeReviewsByDecision(decision: String): Flow<List<EditorialReviewEntity>> =
        editorialReviewDao.observeReviewsByDecision(decision)

    suspend fun submitReview(
        candidateId: String,
        candidateType: String,
        decision: String,
        note: String,
        articleIds: List<String>,
        sourceIds: List<String>,
        languages: List<String>,
        sharedEntities: List<String>?,
        confidence: String?,
        method: String,
        rationale: String,
        uncertaintyReasons: List<String>
    ) {
        val review = EditorialReviewEntity(
            id = "review-$candidateId-${Instant.now().toEpochMilli()}",
            candidateType = candidateType,
            candidateId = candidateId,
            decision = decision,
            note = note,
            reviewedBy = "demo-editor",
            reviewedAt = Instant.now(),
            articleIds = articleIds,
            sourceIds = sourceIds,
            languages = languages,
            sharedEntities = sharedEntities,
            confidence = confidence,
            method = method,
            rationale = rationale,
            uncertaintyReasons = uncertaintyReasons
        )
        editorialReviewDao.insert(review)
    }

    suspend fun getReviewForCandidate(candidateId: String): EditorialReviewEntity? =
        editorialReviewDao.getReviewForCandidate(candidateId)

    suspend fun getStats(): ReviewStats {
        return ReviewStats(
            approved = editorialReviewDao.countApproved(),
            rejected = editorialReviewDao.countRejected(),
            deferred = editorialReviewDao.countDeferred()
        )
    }

    suspend fun resetAllReviews() {
        editorialReviewDao.deleteAllReviews()
    }
}

data class ReviewStats(
    val approved: Int,
    val rejected: Int,
    val deferred: Int
)
