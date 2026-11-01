package com.crosslens.app.data.repository

import androidx.room.withTransaction
import com.crosslens.app.data.preferences.ReadingStateRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import com.crosslens.app.core.model.*
import com.crosslens.app.data.clustering.EventClusteringService
import com.crosslens.app.data.ingestion.RssSourceAdapter
import com.crosslens.app.data.ingestion.SourceArticleRecord
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
    private val eventClusterDao: EventClusterDao,
    private val database: CrossLensDatabase,
    private val mockRepository: MockStoryRepository,
    private val digestGenerator: SourceDigestGenerator,
    private val clusteringService: EventClusteringService,
    private val debugFixtureProvider: com.crosslens.app.data.fixture.DebugEventFixtureProvider,
    private val readingStateRepository: ReadingStateRepository
) : StoryRepository {

    private val clusteringAuditor = com.crosslens.app.data.clustering.ClusteringAuditor(clusteringService)

    private val refreshMutex = Mutex()

    companion object {
        private const val CACHE_FRESHNESS_THRESHOLD_MINUTES = 15L
        private const val LIVE_FEED_MARKER = "live_feed_"
        private const val MAX_ARTICLES_PER_PUBLISHER = 5
    }

    /** Called after fallback seeding by the application, never from a constructor. */
    suspend fun refreshIfStale(): Result<Unit> = refreshLiveFeed(isUserTriggered = false)

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
        return if (storyId.startsWith(LIVE_FEED_MARKER)) emptyList()
        else mockRepository.getClaimsForStory(storyId)
    }

    override suspend fun getFrameObservationsForStory(storyId: String): List<FrameObservation> {
        return if (storyId.startsWith(LIVE_FEED_MARKER)) emptyList()
        else mockRepository.getFrameObservationsForStory(storyId)
    }

    override suspend fun getArticle(articleId: String): Article? {
        return articleDao.getArticlesByIds(listOf(articleId)).firstOrNull()?.toDomain()
    }

    override suspend fun getSourceDigest(storyId: String): SourceDigest? {
        val articles = getArticlesForStory(storyId)
        if (articles.isEmpty() || articles.any { !it.contentUseMetadata.isDemo }) return null

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
                isCacheFresh(metadata) && metadata.successfulSourceCount > 0 -> FeedState.LIVE
                else -> FeedState.CACHED
            }

            FeedMetadata(
                state = state,
                lastUpdated = metadata.lastSuccessfulFetch,
                lastRefreshAttempt = metadata.lastAttemptedFetch,
                successfulSourceCount = metadata.successfulSourceCount,
                failedSourceCount = metadata.failedSourceCount,
                errorMessage = if (metadata.successfulSourceCount == 0) "Refresh unavailable; showing cached reporting" else null
            )
        }
    }

    private suspend fun refreshLiveFeed(isUserTriggered: Boolean): Result<Unit> {
        return refreshMutex.withLock { withContext(Dispatchers.IO) {
            try {
                if (!isUserTriggered && !shouldRefreshCache(feedMetadataDao.getFeedMetadata())) {
                    return@withContext Result.success(Unit)
                }
                val now = Instant.now()
                val allItems = mutableListOf<Pair<RssSourceAdapter, com.crosslens.app.data.ingestion.SourceArticleRecord>>()
                // Results are collected before counting; no shared mutable counters across IO threads.
                val fetched = rssAdapters.map { adapter ->
                    async {
                        try {
                            adapter to adapter.fetchArticles()
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            adapter to emptyList<SourceArticleRecord>()
                        }
                    }
                }.awaitAll()
                val successCount = fetched.count { it.second.isNotEmpty() }
                val failCount = fetched.size - successCount
                val results = fetched.flatMap { (adapter, articles) -> articles.map { adapter to it } }
                    .distinctBy { (adapter, article) -> adapter.sourceId to NewsIdentity.canonicalUrl(article.url) }

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
                    return@withContext Result.failure(java.io.IOException("All news feeds unavailable; cache retained"))
                }

                val sourcesToInsert = allItems.groupBy { it.first }.map { (adapter, items) ->
                    val metadata = com.crosslens.app.data.ingestion.SourceMetadataRegistry.getMetadata(adapter.sourceId)
                    SourceEntity(
                        id = "live_${adapter.sourceId}",
                        name = adapter.sourceName,
                        homepage = metadata?.homepage ?: adapter.homepage,
                        countryCodes = PublisherGeography.countryCodes(metadata?.country),
                        regionIds = PublisherGeography.regionIds(metadata?.country),
                        defaultLanguages = items.map { it.second.languageTag }.distinct()
                    )
                }

                // Apply per-publisher balancing: max 5 articles per publisher, chronological within publisher
                val balancedItems = allItems
                    .groupBy { it.first.sourceId }
                    .flatMap { (_, items) ->
                        items.sortedByDescending { it.second.publishedAt }
                            .take(MAX_ARTICLES_PER_PUBLISHER)
                    }
                    .sortedByDescending { it.second.publishedAt }

                // Convert to SourceArticleRecord for clustering
                val articleRecords = balancedItems.map { (adapter, article) -> article }

                // Add debug fixture articles (debug builds only, release returns empty list)
                val fixtureArticles = debugFixtureProvider.getReadAcrossFixture()
                val allArticleRecords = articleRecords + fixtureArticles

                // Audit clustering run (logs detailed information)
                if (com.crosslens.app.BuildConfig.DEBUG) {
                    // Auditor uses reflection and verbose headlines; never run it in optimized releases.
                    clusteringAuditor.auditClusteringRun(allArticleRecords)
                }

                // Use EventClusteringService for intelligent grouping (includes fixture in debug builds)
                val clusters = clusteringService.clusterArticles(allArticleRecords)

                // Create a map from article URL to adapter for later lookup
                // For fixture articles, create synthetic adapters to preserve source attribution
                val fixtureAdapters = fixtureArticles.associate { fixtureArticle ->
                    fixtureArticle.url to RssSourceAdapter(
                        sourceId = fixtureArticle.sourceId,
                        sourceName = getSourceName(fixtureArticle.sourceId),
                        feedUrl = "https://test.crosslens.fixture/feed",
                        httpClient = okhttp3.OkHttpClient()
                    )
                }

                val articleToAdapter = (balancedItems.associate { (adapter, article) ->
                    article.url to adapter
                } + fixtureAdapters)

                // Create story and article entities from clusters
                val storiesToInsert = mutableListOf<StoryEntity>()
                val articlesToInsert = mutableListOf<ArticleEntity>()
                val eventClustersToInsert = mutableListOf<EventClusterEntity>()

                // Process clustered events (2+ publishers)
                clusters.forEach { cluster ->
                    val storyId = NewsIdentity.storyId(cluster.articles.map { it.url })
                    val articleIds = mutableListOf<String>()

                    // Create articles for each source in cluster
                    cluster.articles.forEach { clusteredArticle ->
                        val adapter = articleToAdapter[clusteredArticle.url]
                        if (adapter != null) {
                            val articleId = NewsIdentity.articleId(clusteredArticle.url, storyId)
                            articleIds.add(articleId)
                            val sourceId = "live_${adapter.sourceId}"

                            articlesToInsert.add(
                                ArticleEntity(
                                    id = articleId,
                                    storyId = storyId,
                                    sourceId = sourceId,
                                    originalUrl = clusteredArticle.url,
                                    publishedTime = clusteredArticle.publishedAt,
                                    originalLanguage = clusteredArticle.languageTag,
                                    originalHeadline = clusteredArticle.headline,
                                    originalExcerpt = clusteredArticle.excerpt,
                                    originalContent = clusteredArticle.excerpt,
                                    attribution = adapter.sourceName,
                                    isDemo = fixtureArticles.any { it.url == clusteredArticle.url },
                                    requiresSubscription = false,
                                    imageUrl = clusteredArticle.imageUrl
                                )
                            )
                        }
                    }

                    // Create story for this event cluster
                    storiesToInsert.add(
                        StoryEntity(
                            id = storyId,
                            title = cluster.eventSummary,
                            summary = "${cluster.articles.size} sources • ${cluster.commonEntities.joinToString(", ")}",
                            eventTime = cluster.eventTime,
                            updatedTime = now,
                            topicIds = emptyList(),
                            eventCountryCodes = emptyList(),
                            articleIds = articleIds,
                            claimIds = emptyList(),
                            lensGapScore = null,
                            lensGapStatus = "EVENT_CLUSTER",
                            lensGapIsDemo = false,
                            imageUrl = cluster.articles.firstOrNull()?.imageUrl
                        )
                    )

                    // Store event cluster metadata
                    eventClustersToInsert.add(
                        EventClusterEntity(
                            id = storyId,
                            eventSummary = cluster.eventSummary,
                            eventTime = cluster.eventTime,
                            clusteredAt = cluster.clusteredAt,
                            confidence = cluster.confidence.name,
                            groupingExplanation = cluster.groupingExplanation,
                            commonEntities = cluster.commonEntities,
                            publisherCount = cluster.publisherCount,
                            imageUrl = cluster.articles.firstOrNull()?.imageUrl,
                            articleIds = articleIds
                        )
                    )
                }

                // Process unclustered articles (single publisher or didn't match)
                val clusteredUrls = clusters.flatMap { cluster ->
                    cluster.articles.map { it.url }
                }.toSet()

                balancedItems.filter { (_, article) -> article.url !in clusteredUrls }
                    .forEach { (adapter, article) ->
                        val storyId = NewsIdentity.storyId(listOf(article.url))
                        val articleId = NewsIdentity.articleId(article.url, storyId)
                        val sourceId = "live_${adapter.sourceId}"

                        articlesToInsert.add(
                            ArticleEntity(
                                id = articleId,
                                storyId = storyId,
                                sourceId = sourceId,
                                originalUrl = article.url,
                                publishedTime = article.publishedAt,
                                originalLanguage = article.languageTag,
                                originalHeadline = article.headline,
                                originalExcerpt = article.excerpt,
                                originalContent = article.excerpt,
                                attribution = adapter.sourceName,
                                isDemo = false,
                                requiresSubscription = false,
                                imageUrl = article.imageUrl
                            )
                        )

                        storiesToInsert.add(
                            StoryEntity(
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
                                lensGapIsDemo = false,
                                imageUrl = article.imageUrl
                            )
                        )
                    }

                val readingState = readingStateRepository.readingStateFlow.first()
                val protectedIds = readingState.savedStoryIds + listOfNotNull(readingState.lastOpenedStoryId)
                database.withTransaction {
                // Replacement is atomic. Retain saved/last-opened stories, including legacy random IDs.
                val replacementIds = storiesToInsert.map { it.id }.toSet()
                val obsolete = storyDao.getAllStories().filter {
                    it.id.startsWith(LIVE_FEED_MARKER) && it.id !in protectedIds && it.id !in replacementIds
                }
                obsolete.forEach { story ->
                    articleDao.deleteArticlesByStory(story.id)
                    storyDao.deleteStories(listOf(story.id))
                }
                sourceDao.insertSources(sourcesToInsert)
                eventClusterDao.deleteAllClusters()
                storyDao.insertStories(storiesToInsert)
                articleDao.insertArticles(articlesToInsert)
                eventClusterDao.insertClusters(eventClustersToInsert)

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

                }
                Result.success(Unit)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(e)
            }
        } }
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

    /**
     * Get display name for source ID (used for debug fixture attribution).
     */
    private fun getSourceName(sourceId: String): String {
        return when (sourceId) {
            "bbc-news-rss" -> "BBC News"
            "nytimes-rss" -> "The New York Times"
            "dw-rss" -> "Deutsche Welle"
            "guardian-rss" -> "The Guardian"
            "aljazeera-rss" -> "Al Jazeera"
            "lemonde-rss" -> "Le Monde"
            else -> sourceId.replace("-rss", "").replace("-", " ").replaceFirstChar { it.uppercase() }
        }
    }
}
