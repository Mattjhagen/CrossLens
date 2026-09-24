package com.crosslens.app.data.repository

import com.crosslens.app.core.model.*
import com.crosslens.app.data.ingestion.RssSourceAdapter
import com.crosslens.app.data.local.CrossLensDatabase
import com.crosslens.app.data.local.dao.*
import com.crosslens.app.data.local.entity.*
import com.crosslens.app.data.local.toDomain
import com.crosslens.app.data.mock.MockStoryRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.time.Instant
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Live feed repository with caching strategy:
 * - Shows cached results immediately on launch
 * - Background refresh when cache > 15 minutes old
 * - Pull-to-refresh forces refresh
 * - Falls back to mock only when no live cache AND all sources fail
 */
@Singleton
class LiveStoryRepository @Inject constructor(
    private val rssAdapters: List<RssSourceAdapter>,
    private val storyDao: StoryDao,
    private val articleDao: ArticleDao,
    private val sourceDao: SourceDao,
    private val feedMetadataDao: FeedMetadataDao,
    private val database: CrossLensDatabase,
    private val mockRepository: MockStoryRepository,
    private val digestGenerator: SourceDigestGenerator
) : StoryRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private const val CACHE_FRESHNESS_THRESHOLD_MINUTES = 15L
        private const val LIVE_FEED_MARKER = "live_feed_"
    }

    init {
        // On init, check cache age and refresh in background if stale
        scope.launch {
            val metadata = feedMetadataDao.getFeedMetadata()
            if (shouldRefreshCache(metadata)) {
                refreshLiveFeed(isUserTriggered = false)
            }
        }
    }

    override fun observeStories(): Flow<List<Story>> {
        return combine(
            storyDao.observeAllStories(),
            feedMetadataDao.observeFeedMetadata()
        ) { stories, metadata ->
            // Return stories, preferring live over mock
            val liveStories = stories.filter { it.id.startsWith(LIVE_FEED_MARKER) }
            if (liveStories.isNotEmpty()) {
                liveStories.map { it.toDomain() }
            } else {
                // No live cache, return mock fallback
                stories.filter { !it.id.startsWith(LIVE_FEED_MARKER) }.map { it.toDomain() }
            }
        }
    }

    override fun observeStory(storyId: String): Flow<Story?> {
        return storyDao.observeStoryById(storyId).map { it?.toDomain() }
    }

    override fun observeLocalStories(locationId: String): Flow<List<Story>> {
        // For v0.0.14, live feed doesn't support local stories filtering yet
        // Delegate to mock repository for local stories
        return mockRepository.observeLocalStories(locationId)
    }

    override suspend fun getStory(storyId: String): Story? {
        return storyDao.getStoryById(storyId)?.toDomain()
    }

    override suspend fun getArticlesForStory(storyId: String): List<Article> {
        return articleDao.observeArticlesByStory(storyId)
            .map { entities -> entities.map { it.toDomain() } }
            .first()
    }

    override suspend fun getClaimsForStory(storyId: String): List<Claim> {
        // Live feed stories don't have claims yet
        return emptyList()
    }

    override suspend fun getFrameObservationsForStory(storyId: String): List<FrameObservation> {
        // Live feed stories don't have observations yet
        return emptyList()
    }

    override suspend fun getArticle(articleId: String): Article? {
        return articleDao.getArticlesByIds(listOf(articleId)).firstOrNull()?.toDomain()
    }

    override suspend fun getSourceDigest(storyId: String): SourceDigest? {
        val articles = getArticlesForStory(storyId)
        if (articles.isEmpty()) return null

        val sourceIds = articles.map { it.sourceId }.distinct()
        val sources = database.sourceDao().getSourcesByIds(sourceIds)
            .associate { it.id to it.toDomain() }

        return digestGenerator.generateDigest(storyId, articles, sources)
    }

    /**
     * Refresh live feed. If user-triggered (pull-to-refresh), always fetch.
     * Otherwise, only fetch if cache is stale.
     */
    override suspend fun refresh(): Result<Unit> {
        return refreshLiveFeed(isUserTriggered = true)
    }

    /**
     * Get current feed metadata for UI display.
     */
    suspend fun getFeedMetadata(): FeedMetadata {
        val metadata = feedMetadataDao.getFeedMetadata()
        val hasLiveCache = storyDao.getAllStories().any { it.id.startsWith(LIVE_FEED_MARKER) }

        return if (metadata == null || !hasLiveCache) {
            // No cache yet, using mock fallback
            FeedMetadata(
                state = FeedState.DEMO_FALLBACK,
                lastUpdated = null,
                lastRefreshAttempt = null,
                successfulSourceCount = 0,
                failedSourceCount = 0,
                errorMessage = null
            )
        } else {
            val state = when {
                isCacheFresh(metadata) -> FeedState.LIVE
                else -> FeedState.CACHED
            }

            FeedMetadata(
                state = state,
                lastUpdated = metadata.lastSuccessfulFetch,
                lastRefreshAttempt = metadata.lastAttemptedFetch,
                successfulSourceCount = metadata.successfulSourceCount,
                failedSourceCount = metadata.failedSourceCount,
                errorMessage = null
            )
        }
    }

    private suspend fun refreshLiveFeed(isUserTriggered: Boolean): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val now = Instant.now()
                val allItems = mutableListOf<Pair<RssSourceAdapter, com.crosslens.app.data.ingestion.SourceArticleRecord>>()
                var successCount = 0
                var failCount = 0

                // Fetch from all sources in parallel, isolating failures
                val results = rssAdapters.map { adapter ->
                    async {
                        try {
                            val articles = adapter.fetchArticles()
                            if (articles.isNotEmpty()) {
                                successCount++
                                articles.map { adapter to it }
                            } else {
                                failCount++
                                emptyList()
                            }
                        } catch (e: Exception) {
                            failCount++
                            emptyList()
                        }
                    }
                }.awaitAll().flatten()

                allItems.addAll(results)

                if (allItems.isEmpty()) {
                    // All sources failed
                    feedMetadataDao.insertMetadata(
                        FeedMetadataEntity(
                            lastSuccessfulFetch = feedMetadataDao.getFeedMetadata()?.lastSuccessfulFetch,
                            lastAttemptedFetch = now,
                            successfulSourceCount = 0,
                            failedSourceCount = rssAdapters.size,
                            totalArticleCount = 0
                        )
                    )

                    // Keep existing cache if available, otherwise mock is shown via observeStories
                    return@withContext Result.success(Unit)
                }

                // Create/update live feed sources
                val liveSourceIds = mutableSetOf<String>()
                allItems.groupBy { it.first }.forEach { (adapter, items) ->
                    val sourceId = "live_${adapter.sourceId}"
                    liveSourceIds.add(sourceId)

                    // Upsert source if not exists
                    if (sourceDao.getSourcesByIds(listOf(sourceId)).isEmpty()) {
                        sourceDao.insertSources(listOf(
                            SourceEntity(
                                id = sourceId,
                                name = adapter.sourceName,
                                homepage = "https://", // RSS adapters don't expose this yet
                                countryCodes = emptyList(),
                                regionIds = emptyList(),
                                defaultLanguages = listOf(items.first().second.languageTag)
                            )
                        ))
                    }
                }

                // Clear old live feed articles
                val oldLiveStories = storyDao.getAllStories().filter { it.id.startsWith(LIVE_FEED_MARKER) }
                oldLiveStories.forEach { story ->
                    articleDao.deleteArticlesByStory(story.id)
                    storyDao.deleteStories(listOf(story.id))
                }

                // Create stories from articles (one story per article for now, no clustering)
                val newStories = allItems.map { (adapter, article) ->
                    val storyId = "${LIVE_FEED_MARKER}${UUID.randomUUID()}"
                    val articleId = "article_${UUID.randomUUID()}"
                    val sourceId = "live_${adapter.sourceId}"

                    val storyEntity = StoryEntity(
                        id = storyId,
                        title = article.headline,
                        summary = article.excerpt,
                        eventTime = article.publishedAt,
                        updatedTime = now,
                        topicIds = emptyList(),
                        eventCountryCodes = emptyList(),
                        articleIds = listOf(articleId),
                        claimIds = emptyList(),
                        lensGapScore = null,
                        lensGapStatus = "NOT_ASSESSED",
                        lensGapIsDemo = false
                    )

                    val articleEntity = ArticleEntity(
                        id = articleId,
                        storyId = storyId,
                        sourceId = sourceId,
                        originalUrl = article.url,
                        publishedTime = article.publishedAt,
                        originalLanguage = article.languageTag,
                        originalHeadline = article.headline,
                        originalExcerpt = article.excerpt,
                        originalContent = article.excerpt, // RSS only has excerpts
                        attribution = adapter.sourceName,
                        isDemo = false,
                        requiresSubscription = true // Assume paywall for original articles
                    )

                    storyEntity to articleEntity
                }

                // Insert new stories and articles
                storyDao.insertStories(newStories.map { it.first })
                articleDao.insertArticles(newStories.map { it.second })

                // Update metadata
                feedMetadataDao.insertMetadata(
                    FeedMetadataEntity(
                        lastSuccessfulFetch = now,
                        lastAttemptedFetch = now,
                        successfulSourceCount = successCount,
                        failedSourceCount = failCount,
                        totalArticleCount = allItems.size
                    )
                )

                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private fun shouldRefreshCache(metadata: FeedMetadataEntity?): Boolean {
        if (metadata == null) return true
        val lastFetch = metadata.lastSuccessfulFetch ?: return true
        val ageMinutes = java.time.Duration.between(lastFetch, Instant.now()).toMinutes()
        return ageMinutes >= CACHE_FRESHNESS_THRESHOLD_MINUTES
    }

    private fun isCacheFresh(metadata: FeedMetadataEntity): Boolean {
        val lastFetch = metadata.lastSuccessfulFetch ?: return false
        val ageMinutes = java.time.Duration.between(lastFetch, Instant.now()).toMinutes()
        return ageMinutes < CACHE_FRESHNESS_THRESHOLD_MINUTES
    }
}
