package com.crosslens.app.data.local.dao

import androidx.room.*
import com.crosslens.app.data.local.entity.EditorialReviewEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EditorialReviewDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(review: EditorialReviewEntity)

    @Query("SELECT * FROM editorial_reviews WHERE candidateId = :candidateId LIMIT 1")
    suspend fun getReviewForCandidate(candidateId: String): EditorialReviewEntity?

    @Query("SELECT * FROM editorial_reviews WHERE candidateId = :candidateId LIMIT 1")
    fun observeReviewForCandidate(candidateId: String): Flow<EditorialReviewEntity?>

    @Query("SELECT * FROM editorial_reviews ORDER BY reviewedAt DESC")
    fun observeAllReviews(): Flow<List<EditorialReviewEntity>>

    @Query("SELECT * FROM editorial_reviews WHERE decision = :decision ORDER BY reviewedAt DESC")
    fun observeReviewsByDecision(decision: String): Flow<List<EditorialReviewEntity>>

    @Query("SELECT COUNT(*) FROM editorial_reviews WHERE decision = 'APPROVED'")
    suspend fun countApproved(): Int

    @Query("SELECT COUNT(*) FROM editorial_reviews WHERE decision = 'REJECTED'")
    suspend fun countRejected(): Int

    @Query("SELECT COUNT(*) FROM editorial_reviews WHERE decision = 'DEFERRED'")
    suspend fun countDeferred(): Int

    @Query("DELETE FROM editorial_reviews")
    suspend fun deleteAllReviews()
}
