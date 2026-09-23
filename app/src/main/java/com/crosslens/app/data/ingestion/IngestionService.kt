package com.crosslens.app.data.ingestion

import com.crosslens.app.data.local.dao.ArticleDao
import com.crosslens.app.data.local.dao.EditorialDecisionDao
import com.crosslens.app.data.local.dao.StoryDao
import com.crosslens.app.data.local.entity.ArticleEntity
import com.crosslens.app.data.local.entity.EditorialDecisionEntity
import com.crosslens.app.data.local.entity.StoryEntity
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Orchestrates the ingestion pipeline: source adapters → clustering → editorial review → persistence.
 * Only approved clusters become stories in the database.
 */
@Singleton
class IngestionService @Inject constructor(
    private val pipeline: EventClusteringPipeline,
    private val storyDao: StoryDao,
    private val articleDao: ArticleDao,
    private val editorialDecisionDao: EditorialDecisionDao,
    private val registryValidator: SourceRegistryValidator
) {
    /**
     * Process articles from source adapters through clustering.
     * Returns proposals that need editorial review.
     * Validates source eligibility via registry before accepting any records.
     */
    suspend fun ingestFromAdapters(adapters: List<SourceAdapter>): IngestionResult {
        val allRecords = mutableListOf<IngestionArticleInput>()
        val adapterErrors = mutableListOf<AdapterError>()
        val ineligibleSources = mutableListOf<IneligibleSource>()

        for (adapter in adapters) {
            try {
                SourceAdapterValidator.validate(adapter)

                // Check source eligibility via registry
                val intakeMethod = when (adapter.javaClass.simpleName) {
                    "MockSourceAdapter" -> IntakeMethod.DEMO_FIXTURE
                    else -> IntakeMethod.RSS_WITH_EXCERPT // Default for real adapters
                }

                when (val eligibility = registryValidator.checkEligibility(adapter.sourceId, intakeMethod)) {
                    is SourceEligibilityResult.Ineligible -> {
                        ineligibleSources += IneligibleSource(
                            sourceId = adapter.sourceId,
                            reason = eligibility.reason,
                            details = eligibility.details
                        )
                        continue // Skip this adapter
                    }
                    is SourceEligibilityResult.Eligible -> {
                        val entry = eligibility.entry
                        val records = adapter.fetchArticles()

                        // Validate each record against attribution requirements
                        records.forEach { record ->
                            SourceAdapterValidator.validateRecord(record)
                            registryValidator.validateAttribution(entry, record).getOrThrow()
                        }

                        allRecords += records.map { record ->
                            IngestionArticleInput(
                                sourceId = adapter.sourceId,
                                sourceName = entry.attributionRequirements.sourceName,
                                url = record.url,
                                publishedAt = record.publishedAt,
                                languageTag = record.languageTag,
                                headline = record.headline,
                                excerpt = record.excerpt
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                adapterErrors += AdapterError(adapter.sourceId, e.message ?: "Unknown error")
            }
        }

        val batchResult = pipeline.process(allRecords)

        return IngestionResult(
            totalArticles = allRecords.size,
            acceptedArticles = batchResult.acceptedArticles,
            duplicates = batchResult.duplicates,
            proposals = batchResult.clusters.filter { it.status == ClusterStatus.REVIEWABLE },
            singleSourceClusters = batchResult.clusters.filter { it.status == ClusterStatus.SINGLE_SOURCE },
            adapterErrors = adapterErrors,
            ineligibleSources = ineligibleSources
        )
    }

    /**
     * Persist an approved cluster as a story with full audit trail.
     */
    suspend fun persistApprovedCluster(cluster: ApprovedCluster): Result<String> {
        return try {
            EditorialValidator.validateApprovedCluster(cluster)

            // Create story entity
            val storyEntity = StoryEntity(
                id = cluster.clusterId,
                title = cluster.title,
                summary = cluster.summary,
                eventTime = cluster.firstPublishedAt,
                updatedTime = cluster.lastPublishedAt,
                topicIds = cluster.topicIds,
                eventCountryCodes = cluster.eventCountryCodes,
                articleIds = cluster.articles.map { it.id },
                claimIds = emptyList(), // Claims added separately
                lensGapScore = null,
                lensGapStatus = "NOT_ASSESSED",
                lensGapIsDemo = false
            )

            // Create article entities
            val articleEntities = cluster.articles.map { article ->
                ArticleEntity(
                    id = article.id,
                    storyId = cluster.clusterId,
                    sourceId = article.sourceId,
                    originalUrl = article.originalUrl,
                    publishedTime = article.publishedAt,
                    originalLanguage = article.languageTag,
                    originalHeadline = article.headline,
                    originalExcerpt = article.excerpt,
                    attribution = article.sourceName,
                    isDemo = false
                )
            }

            // Create editorial decision entity for audit trail
            val decisionEntity = EditorialDecisionEntity(
                id = "decision-${cluster.clusterId}",
                proposalId = cluster.decision.proposalId,
                clusterId = cluster.clusterId,
                decision = cluster.decision.decision.name,
                reason = cluster.decision.reason,
                reviewerId = cluster.decision.reviewerId,
                decidedAt = cluster.decision.decidedAt,
                articleIds = cluster.articles.map { it.id },
                sourceIds = cluster.articles.map { it.sourceId }.distinct(),
                provisionalTitle = cluster.articles.first().headline,
                finalTitle = cluster.title
            )

            // Persist in order: decision (audit), articles, story
            editorialDecisionDao.insert(decisionEntity)
            articleDao.insertArticles(articleEntities)
            storyDao.insertStories(listOf(storyEntity))

            Result.success(cluster.clusterId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Record a rejected or deferred decision for audit purposes only.
     * Does not create a story in the database.
     */
    suspend fun recordDecision(proposal: EventClusterProposal, decision: EditorialDecision) {
        EditorialValidator.validate(decision)

        val decisionEntity = EditorialDecisionEntity(
            id = "decision-${proposal.id}",
            proposalId = decision.proposalId,
            clusterId = proposal.id,
            decision = decision.decision.name,
            reason = decision.reason,
            reviewerId = decision.reviewerId,
            decidedAt = decision.decidedAt,
            articleIds = proposal.articleIds,
            sourceIds = proposal.sourceIds,
            provisionalTitle = proposal.provisionalTitle,
            finalTitle = "" // Not finalized for rejected/deferred
        )

        editorialDecisionDao.insert(decisionEntity)
    }

    /**
     * Get count of decisions by type for monitoring.
     */
    suspend fun getDecisionStats(): DecisionStats {
        return DecisionStats(
            approved = editorialDecisionDao.countApproved(),
            rejected = editorialDecisionDao.countRejected()
        )
    }
}

data class IngestionResult(
    val totalArticles: Int,
    val acceptedArticles: List<NormalizedArticle>,
    val duplicates: List<DuplicateArticle>,
    val proposals: List<EventClusterProposal>,
    val singleSourceClusters: List<EventClusterProposal>,
    val adapterErrors: List<AdapterError>,
    val ineligibleSources: List<IneligibleSource>
)

data class AdapterError(
    val sourceId: String,
    val error: String
)

data class IneligibleSource(
    val sourceId: String,
    val reason: IneligibilityReason,
    val details: String
)

data class DecisionStats(
    val approved: Int,
    val rejected: Int
)
