package com.crosslens.app.data.repository

import com.crosslens.app.core.model.EventCluster
import com.crosslens.app.core.model.ClusterConfidence
import com.crosslens.app.data.clustering.EventClusteringService
import com.crosslens.app.data.ingestion.SourceArticleRecord
import com.crosslens.app.data.local.dao.EventClusterDao
import com.crosslens.app.data.local.entity.EventClusterEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing event clusters.
 * Handles clustering, persistence, and retrieval of grouped articles.
 */
@Singleton
class EventClusterRepository @Inject constructor(
    private val clusterDao: EventClusterDao,
    private val clusteringService: EventClusteringService
) {

    /**
     * Cluster articles and persist the results.
     * Returns the list of persisted clusters.
     */
    suspend fun clusterAndPersist(articles: List<SourceArticleRecord>): List<EventCluster> {
        // Clear old clusters (older than 7 days)
        val cutoffTime = Instant.now().minus(7, ChronoUnit.DAYS)
        clusterDao.deleteClustersOlderThan(cutoffTime)

        // Perform clustering
        val clusters = clusteringService.clusterArticles(articles)

        // Convert to entities and persist
        val entities = clusters.map { cluster ->
            EventClusterEntity(
                id = cluster.id,
                eventSummary = cluster.eventSummary,
                eventTime = cluster.eventTime,
                clusteredAt = cluster.clusteredAt,
                confidence = cluster.confidence.name,
                groupingExplanation = cluster.groupingExplanation,
                commonEntities = cluster.commonEntities,
                publisherCount = cluster.publisherCount,
                imageUrl = cluster.articles.firstOrNull()?.imageUrl,
                articleIds = emptyList() // Will be populated when wiring to live feed
            )
        }

        clusterDao.insertClusters(entities)

        return clusters
    }

    /**
     * Observe all clusters, ordered by event time (newest first).
     */
    fun observeClusters(): Flow<List<EventCluster>> {
        return clusterDao.observeAllClusters().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    /**
     * Observe a specific cluster by ID.
     */
    fun observeCluster(clusterId: String): Flow<EventCluster?> {
        return clusterDao.observeClusterById(clusterId).map { it?.toDomain() }
    }

    /**
     * Get a cluster by ID.
     */
    suspend fun getCluster(clusterId: String): EventCluster? {
        return clusterDao.getClusterById(clusterId)?.toDomain()
    }

    /**
     * Get all clusters.
     */
    suspend fun getAllClusters(): List<EventCluster> {
        return clusterDao.getAllClusters().map { it.toDomain() }
    }

    /**
     * Delete all clusters (for testing or reset).
     */
    suspend fun deleteAllClusters() {
        clusterDao.deleteAllClusters()
    }

    /**
     * Convert EventClusterEntity to domain EventCluster.
     */
    private fun EventClusterEntity.toDomain(): EventCluster {
        return EventCluster(
            id = id,
            eventSummary = eventSummary,
            articles = emptyList(), // Will be populated from article data when needed
            eventTime = eventTime,
            clusteredAt = clusteredAt,
            confidence = ClusterConfidence.valueOf(confidence),
            groupingExplanation = groupingExplanation,
            commonEntities = commonEntities,
            publisherCount = publisherCount
        )
    }
}
