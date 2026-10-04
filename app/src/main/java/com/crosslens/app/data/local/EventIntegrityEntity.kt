package com.crosslens.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.crosslens.app.core.model.ClusterConfidence
import com.crosslens.app.core.model.EventIntegrityMetadata
import java.time.Instant

/**
 * Room entity for persisting event integrity metadata.
 */
@Entity(tableName = "event_integrity")
@TypeConverters(EventIntegrityConverters::class)
data class EventIntegrityEntity(
    @PrimaryKey
    val clusterId: String,

    val distinctPublisherCount: Int,
    val articleCount: Int,
    val timeWindowHours: Long,

    val commonNamedEntities: String, // JSON array
    val headlineSimilarityScores: String, // JSON array of doubles
    val averageHeadlineSimilarity: Double,
    val sharedEntityCounts: String, // JSON array of ints

    val sourceIds: String, // JSON array
    val articleUrls: String, // JSON array

    val confidence: ClusterConfidence,
    val matchRationale: String,

    val clusteredAt: Long, // epoch millis
    val updatedAt: Long // epoch millis
)

/**
 * Type converters for EventIntegrityEntity.
 */
class EventIntegrityConverters {
    @TypeConverter
    fun fromClusterConfidence(value: ClusterConfidence): String = value.name

    @TypeConverter
    fun toClusterConfidence(value: String): ClusterConfidence = ClusterConfidence.valueOf(value)
}

/**
 * Convert entity to domain model.
 */
fun EventIntegrityEntity.toDomainModel(): EventIntegrityMetadata {
    return EventIntegrityMetadata(
        clusterId = clusterId,
        distinctPublisherCount = distinctPublisherCount,
        articleCount = articleCount,
        timeWindowHours = timeWindowHours,
        commonNamedEntities = parseJsonStringList(commonNamedEntities),
        headlineSimilarityScores = parseJsonDoubleList(headlineSimilarityScores),
        averageHeadlineSimilarity = averageHeadlineSimilarity,
        sharedEntityCounts = parseJsonIntList(sharedEntityCounts),
        sourceIds = parseJsonStringList(sourceIds),
        articleUrls = parseJsonStringList(articleUrls),
        confidence = confidence,
        matchRationale = matchRationale,
        clusteredAt = Instant.ofEpochMilli(clusteredAt),
        updatedAt = Instant.ofEpochMilli(updatedAt)
    )
}

/**
 * Convert domain model to entity.
 */
fun EventIntegrityMetadata.toEntity(): EventIntegrityEntity {
    return EventIntegrityEntity(
        clusterId = clusterId,
        distinctPublisherCount = distinctPublisherCount,
        articleCount = articleCount,
        timeWindowHours = timeWindowHours,
        commonNamedEntities = toJsonStringList(commonNamedEntities),
        headlineSimilarityScores = toJsonDoubleList(headlineSimilarityScores),
        averageHeadlineSimilarity = averageHeadlineSimilarity,
        sharedEntityCounts = toJsonIntList(sharedEntityCounts),
        sourceIds = toJsonStringList(sourceIds),
        articleUrls = toJsonStringList(articleUrls),
        confidence = confidence,
        matchRationale = matchRationale,
        clusteredAt = clusteredAt.toEpochMilli(),
        updatedAt = updatedAt.toEpochMilli()
    )
}

// Simple JSON helpers (no external library dependencies)
private fun parseJsonStringList(json: String): List<String> {
    if (json.isBlank() || json == "[]") return emptyList()
    return json.removeSurrounding("[", "]")
        .split(",")
        .map { it.trim().removeSurrounding("\"") }
        .filter { it.isNotBlank() }
}

private fun parseJsonDoubleList(json: String): List<Double> {
    if (json.isBlank() || json == "[]") return emptyList()
    return json.removeSurrounding("[", "]")
        .split(",")
        .map { it.trim().toDoubleOrNull() ?: 0.0 }
}

private fun parseJsonIntList(json: String): List<Int> {
    if (json.isBlank() || json == "[]") return emptyList()
    return json.removeSurrounding("[", "]")
        .split(",")
        .map { it.trim().toIntOrNull() ?: 0 }
}

private fun toJsonStringList(list: List<String>): String {
    if (list.isEmpty()) return "[]"
    return list.joinToString(",", "[", "]") { "\"$it\"" }
}

private fun toJsonDoubleList(list: List<Double>): String {
    if (list.isEmpty()) return "[]"
    return list.joinToString(",", "[", "]")
}

private fun toJsonIntList(list: List<Int>): String {
    if (list.isEmpty()) return "[]"
    return list.joinToString(",", "[", "]")
}
