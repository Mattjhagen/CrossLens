package com.crosslens.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * DAO for event integrity metadata.
 */
@Dao
interface EventIntegrityDao {

    /**
     * Insert or update event integrity metadata.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entity: EventIntegrityEntity)

    /**
     * Insert or update multiple integrity records.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAll(entities: List<EventIntegrityEntity>)

    /**
     * Get integrity metadata for a specific cluster.
     */
    @Query("SELECT * FROM event_integrity WHERE clusterId = :clusterId")
    suspend fun getByClusterId(clusterId: String): EventIntegrityEntity?

    /**
     * Get integrity metadata for a specific cluster as Flow.
     */
    @Query("SELECT * FROM event_integrity WHERE clusterId = :clusterId")
    fun getByClusterIdFlow(clusterId: String): Flow<EventIntegrityEntity?>

    /**
     * Get all integrity records ordered by clustered time (most recent first).
     */
    @Query("SELECT * FROM event_integrity ORDER BY clusteredAt DESC")
    fun getAllFlow(): Flow<List<EventIntegrityEntity>>

    /**
     * Get recent integrity records (last N clusters).
     */
    @Query("SELECT * FROM event_integrity ORDER BY clusteredAt DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<EventIntegrityEntity>

    /**
     * Get clusters that meet integrity thresholds.
     * (2+ publishers AND not LOW confidence)
     */
    @Query("""
        SELECT * FROM event_integrity
        WHERE distinctPublisherCount >= 2
        AND confidence != 'LOW'
        ORDER BY clusteredAt DESC
    """)
    fun getValidClustersFlow(): Flow<List<EventIntegrityEntity>>

    /**
     * Get clusters that fail integrity thresholds.
     */
    @Query("""
        SELECT * FROM event_integrity
        WHERE distinctPublisherCount < 2
        OR confidence = 'LOW'
        ORDER BY clusteredAt DESC
    """)
    suspend fun getInvalidClusters(): List<EventIntegrityEntity>

    /**
     * Get count of total clusters tracked.
     */
    @Query("SELECT COUNT(*) FROM event_integrity")
    suspend fun getCount(): Int

    /**
     * Get count of valid clusters.
     */
    @Query("""
        SELECT COUNT(*) FROM event_integrity
        WHERE distinctPublisherCount >= 2
        AND confidence != 'LOW'
    """)
    suspend fun getValidCount(): Int

    /**
     * Delete integrity metadata for a specific cluster.
     */
    @Query("DELETE FROM event_integrity WHERE clusterId = :clusterId")
    suspend fun deleteByClusterId(clusterId: String)

    /**
     * Delete all integrity metadata.
     */
    @Query("DELETE FROM event_integrity")
    suspend fun deleteAll()

    /**
     * Delete integrity records older than the given timestamp.
     */
    @Query("DELETE FROM event_integrity WHERE clusteredAt < :timestampMillis")
    suspend fun deleteOlderThan(timestampMillis: Long)
}
