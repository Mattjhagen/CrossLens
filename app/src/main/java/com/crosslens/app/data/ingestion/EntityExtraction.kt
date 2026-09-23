package com.crosslens.app.data.ingestion

/**
 * Offline entity extraction for cross-language event clustering.
 *
 * IMPORTANT: This is a mock-only prototype using explicit entity metadata.
 * It does NOT use NLP models, external APIs, or live entity extraction.
 * All entities come from structured mock data in test fixtures.
 *
 * Extracted entities support cross-language event matching by providing
 * shared identifiers (e.g., "person:biden", "org:un", "location:geneva")
 * that can link articles in different languages covering the same event.
 */

/**
 * Entity extracted from article metadata.
 * Each entity has a stable ID that can match across languages.
 */
sealed class Entity {
    abstract val id: String
    abstract val displayName: String
    abstract val sourceLanguage: String
}

data class PersonEntity(
    override val id: String,        // e.g., "person:biden"
    override val displayName: String,  // e.g., "Joe Biden"
    override val sourceLanguage: String,
    val role: String? = null        // e.g., "President", "Prime Minister"
) : Entity()

data class OrganizationEntity(
    override val id: String,        // e.g., "org:un"
    override val displayName: String,  // e.g., "United Nations"
    override val sourceLanguage: String,
    val orgType: String? = null     // e.g., "government", "ngo", "company"
) : Entity()

data class LocationEntity(
    override val id: String,        // e.g., "location:geneva"
    override val displayName: String,  // e.g., "Geneva"
    override val sourceLanguage: String,
    val countryCode: String? = null // ISO 3166-1 alpha-2
) : Entity()

data class DateEntity(
    override val id: String,        // e.g., "date:2026-09-22"
    override val displayName: String,  // e.g., "September 22, 2026"
    override val sourceLanguage: String
) : Entity()

data class EventIdentifierEntity(
    override val id: String,        // e.g., "event:geneva-climate-summit-2026"
    override val displayName: String,  // e.g., "Geneva Climate Summit 2026"
    override val sourceLanguage: String,
    val eventType: String? = null   // e.g., "summit", "election", "crisis"
) : Entity()

/**
 * Article with extracted entities.
 * Entities come from explicit mock metadata, not automated NLP.
 */
data class ArticleWithEntities(
    val article: NormalizedArticle,
    val entities: List<Entity>,
    val extractionMethod: String,   // e.g., "mock-metadata-v1"
    val isDemo: Boolean = true      // Always true for prototype
)

/**
 * Mock entity extractor that reads structured entity metadata.
 * Does NOT perform NLP or call external services.
 *
 * In production, this would integrate with:
 * - Licensed entity extraction API
 * - Self-hosted entity recognition model
 * - Human-reviewed entity annotations
 *
 * For the prototype, entities are provided explicitly in test fixtures.
 */
class EntityExtractor {
    /**
     * Extract entities from article metadata.
     * For the prototype, returns empty list unless mock metadata is provided.
     */
    fun extract(article: NormalizedArticle, mockEntities: List<Entity> = emptyList()): ArticleWithEntities {
        return ArticleWithEntities(
            article = article,
            entities = mockEntities,
            extractionMethod = "mock-metadata-v1",
            isDemo = true
        )
    }

    /**
     * Batch extract entities from multiple articles.
     * Uses a map of article ID to mock entities for testing.
     */
    fun extractBatch(
        articles: List<NormalizedArticle>,
        mockEntityMap: Map<String, List<Entity>> = emptyMap()
    ): List<ArticleWithEntities> {
        return articles.map { article ->
            val entities = mockEntityMap[article.id] ?: emptyList()
            extract(article, entities)
        }
    }
}

/**
 * Result of entity extraction on a batch of articles.
 */
data class EntityExtractionResult(
    val articlesWithEntities: List<ArticleWithEntities>,
    val totalEntitiesExtracted: Int,
    val entitiesByType: Map<String, Int>
) {
    companion object {
        fun from(articlesWithEntities: List<ArticleWithEntities>): EntityExtractionResult {
            val allEntities = articlesWithEntities.flatMap { it.entities }
            val byType = allEntities.groupBy { entity ->
                when (entity) {
                    is PersonEntity -> "Person"
                    is OrganizationEntity -> "Organization"
                    is LocationEntity -> "Location"
                    is DateEntity -> "Date"
                    is EventIdentifierEntity -> "EventIdentifier"
                }
            }.mapValues { it.value.size }

            return EntityExtractionResult(
                articlesWithEntities = articlesWithEntities,
                totalEntitiesExtracted = allEntities.size,
                entitiesByType = byType
            )
        }
    }
}
