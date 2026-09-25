package com.crosslens.app.data.local.dao

import androidx.room.*
import com.crosslens.app.data.local.entity.EventClusterEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for event cluster operations.
 */
@Dao
interface EventClusterDao {

    @Query("SELECT * FROM event_clusters ORDER BY eventTime DESC")
    fun observeAllClusters(): Flow<List<EventClusterEntity>>

    @Query("SELECT * FROM event_clusters WHERE id = :clusterId")
    fun observeClusterById(clusterId: String): Flow<EventClusterEntity?>

    @Query("SELECT * FROM event_clusters WHERE id = :clusterId")
    suspend fun getClusterById(clusterId: String): EventClusterEntity?

    @Query("SELECT * FROM event_clusters ORDER BY eventTime DESC")
    suspend fun getAllClusters(): List<EventClusterEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClusters(clusters: List<EventClusterEntity>)

    @Query("DELETE FROM event_clusters WHERE id = :clusterId")
    suspend fun deleteCluster(clusterId: String)

    @Query("DELETE FROM event_clusters")
    suspend fun deleteAllClusters()

    @Query("DELETE FROM event_clusters WHERE clusteredAt < :cutoffTime")
    suspend fun deleteClustersOlderThan(cutoffTime: java.time.Instant)
}
