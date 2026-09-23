package com.crosslens.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.crosslens.app.data.local.entity.EditorialDecisionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EditorialDecisionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(decision: EditorialDecisionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(decisions: List<EditorialDecisionEntity>)

    @Query("SELECT * FROM editorial_decisions WHERE clusterId = :clusterId")
    suspend fun getByClusterId(clusterId: String): EditorialDecisionEntity?

    @Query("SELECT * FROM editorial_decisions WHERE decision = 'APPROVED' ORDER BY decidedAt DESC")
    fun observeApprovedDecisions(): Flow<List<EditorialDecisionEntity>>

    @Query("SELECT * FROM editorial_decisions WHERE decision = :decision ORDER BY decidedAt DESC")
    suspend fun getByDecision(decision: String): List<EditorialDecisionEntity>

    @Query("SELECT COUNT(*) FROM editorial_decisions WHERE decision = 'APPROVED'")
    suspend fun countApproved(): Int

    @Query("SELECT COUNT(*) FROM editorial_decisions WHERE decision = 'REJECTED'")
    suspend fun countRejected(): Int
}
