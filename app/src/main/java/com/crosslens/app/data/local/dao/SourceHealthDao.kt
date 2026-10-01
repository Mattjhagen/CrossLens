package com.crosslens.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.crosslens.app.data.local.entity.SourceHealthEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for persisted source health status.
 */
@Dao
interface SourceHealthDao {

    /**
     * Insert or update source health.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(health: SourceHealthEntity)

    /**
     * Get health for a specific source.
     */
    @Query("SELECT * FROM source_health WHERE sourceId = :sourceId")
    suspend fun getHealth(sourceId: String): SourceHealthEntity?

    /**
     * Get all source health records.
     */
    @Query("SELECT * FROM source_health ORDER BY sourceName ASC")
    suspend fun getAllHealth(): List<SourceHealthEntity>

    /**
     * Observe all source health records.
     */
    @Query("SELECT * FROM source_health ORDER BY sourceName ASC")
    fun observeAllHealth(): Flow<List<SourceHealthEntity>>

    /**
     * Get degraded/disabled sources count.
     */
    @Query("SELECT COUNT(*) FROM source_health WHERE status != 'ACTIVE'")
    suspend fun getProblematicSourcesCount(): Int

    /**
     * Delete all health records (for testing).
     */
    @Query("DELETE FROM source_health")
    suspend fun deleteAll()
}
