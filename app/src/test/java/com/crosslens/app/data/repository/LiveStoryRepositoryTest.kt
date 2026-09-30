package com.crosslens.app.data.repository

import com.crosslens.app.core.model.FeedState
import com.crosslens.app.data.clustering.EventClusteringService
import com.crosslens.app.data.ingestion.RssSourceAdapter
import com.crosslens.app.data.ingestion.SourceArticleRecord
import com.crosslens.app.data.ingestion.ContentPermission
import com.crosslens.app.data.local.CrossLensDatabase
import com.crosslens.app.data.local.dao.*
import com.crosslens.app.data.local.entity.*
import com.crosslens.app.data.mock.MockStoryRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.mockito.kotlin.*
import java.time.Instant

class LiveStoryRepositoryTest {

    private lateinit var rssAdapters: List<RssSourceAdapter>
    private lateinit var storyDao: StoryDao
    private lateinit var articleDao: ArticleDao
    private lateinit var sourceDao: SourceDao
    private lateinit var feedMetadataDao: FeedMetadataDao
    private lateinit var eventClusterDao: EventClusterDao
    private lateinit var database: CrossLensDatabase
    private lateinit var mockRepository: MockStoryRepository
    private lateinit var digestGenerator: SourceDigestGenerator
    private lateinit var clusteringService: EventClusteringService
    private lateinit var debugFixtureProvider: com.crosslens.app.data.fixture.DebugEventFixtureProvider
    private lateinit var repository: LiveStoryRepository

    @Before
    fun setup() {
        rssAdapters = emptyList()
        storyDao = mock()
        articleDao = mock()
        sourceDao = mock()
        feedMetadataDao = mock()
        eventClusterDao = mock()
        database = mock()
        mockRepository = mock()
        digestGenerator = mock()
        clusteringService = EventClusteringService()
        debugFixtureProvider = object : com.crosslens.app.data.fixture.DebugEventFixtureProvider {
            override fun getReadAcrossFixture() = emptyList<com.crosslens.app.data.ingestion.SourceArticleRecord>()
        }

        // Setup database mock
        whenever(database.storyDao()).thenReturn(storyDao)
        whenever(database.sourceDao()).thenReturn(sourceDao)
        whenever(database.articleDao()).thenReturn(articleDao)

        // Setup default empty responses for non-suspend functions
        whenever(storyDao.observeAllStories()).thenReturn(flowOf(emptyList()))
        whenever(feedMetadataDao.observeFeedMetadata()).thenReturn(flowOf(null))
    }

    @Test
    fun `getFeedMetadata returns demo fallback when no cache exists`() = runTest {
        whenever(storyDao.getAllStories()).thenReturn(emptyList())
        whenever(feedMetadataDao.getFeedMetadata()).thenReturn(null)

        repository = LiveStoryRepository(
            rssAdapters,
            storyDao,
            articleDao,
            sourceDao,
            feedMetadataDao,
            eventClusterDao,
            database,
            mockRepository,
            digestGenerator,
            clusteringService,
            debugFixtureProvider
        )

        val metadata = repository.getFeedMetadata()

        assertEquals(FeedState.DEMO_FALLBACK, metadata.state)
        assertNull(metadata.lastUpdated)
        assertEquals(0, metadata.successfulSourceCount)
    }

    @Test
    fun `getFeedMetadata returns cached when metadata exists but stale`() = runTest {
        val oldTime = Instant.now().minusSeconds(20 * 60) // 20 minutes ago
        val metadata = FeedMetadataEntity(
            lastSuccessfulFetch = oldTime,
            lastAttemptedFetch = oldTime,
            successfulSourceCount = 2,
            failedSourceCount = 0,
            totalArticleCount = 10
        )

        whenever(feedMetadataDao.getFeedMetadata()).thenReturn(metadata)
        whenever(storyDao.getAllStories()).thenReturn(listOf(
            StoryEntity(
                id = "live_feed_story1",
                title = "Test",
                summary = "",
                eventTime = null,
                updatedTime = Instant.now(),
                topicIds = emptyList(),
                eventCountryCodes = emptyList(),
                articleIds = emptyList(),
                claimIds = emptyList(),
                lensGapScore = null,
                lensGapStatus = "",
                lensGapIsDemo = false
            )
        ))

        repository = LiveStoryRepository(
            rssAdapters,
            storyDao,
            articleDao,
            sourceDao,
            feedMetadataDao,
            eventClusterDao,
            database,
            mockRepository,
            digestGenerator,
            clusteringService,
            debugFixtureProvider
        )

        val feedMeta = repository.getFeedMetadata()

        assertEquals(FeedState.CACHED, feedMeta.state)
        assertEquals(oldTime, feedMeta.lastUpdated)
        assertEquals(2, feedMeta.successfulSourceCount)
    }

    @Test
    fun `getFeedMetadata returns live when metadata is fresh`() = runTest {
        val recentTime = Instant.now().minusSeconds(5 * 60) // 5 minutes ago
        val metadata = FeedMetadataEntity(
            lastSuccessfulFetch = recentTime,
            lastAttemptedFetch = recentTime,
            successfulSourceCount = 3,
            failedSourceCount = 1,
            totalArticleCount = 15
        )

        whenever(feedMetadataDao.getFeedMetadata()).thenReturn(metadata)
        whenever(storyDao.getAllStories()).thenReturn(listOf(
            StoryEntity(
                id = "live_feed_story1",
                title = "Test",
                summary = "",
                eventTime = null,
                updatedTime = Instant.now(),
                topicIds = emptyList(),
                eventCountryCodes = emptyList(),
                articleIds = emptyList(),
                claimIds = emptyList(),
                lensGapScore = null,
                lensGapStatus = "",
                lensGapIsDemo = false
            )
        ))

        repository = LiveStoryRepository(
            rssAdapters,
            storyDao,
            articleDao,
            sourceDao,
            feedMetadataDao,
            eventClusterDao,
            database,
            mockRepository,
            digestGenerator,
            clusteringService,
            debugFixtureProvider
        )

        val feedMeta = repository.getFeedMetadata()

        assertEquals(FeedState.LIVE, feedMeta.state)
        assertEquals(recentTime, feedMeta.lastUpdated)
        assertEquals(3, feedMeta.successfulSourceCount)
        assertEquals(1, feedMeta.failedSourceCount)
    }

    @Test
    fun `refresh handles all sources failing gracefully`() = runTest {
        val failingAdapter1 = mock<RssSourceAdapter>()
        val failingAdapter2 = mock<RssSourceAdapter>()

        whenever(failingAdapter1.sourceId).thenReturn("source1")
        whenever(failingAdapter2.sourceId).thenReturn("source2")
        whenever(failingAdapter1.fetchArticles()).thenReturn(emptyList())
        whenever(failingAdapter2.fetchArticles()).thenReturn(emptyList())

        // Mock fresh metadata to prevent init block from triggering background refresh
        val freshMetadata = FeedMetadataEntity(
            lastSuccessfulFetch = Instant.now(),
            lastAttemptedFetch = Instant.now(),
            successfulSourceCount = 0,
            failedSourceCount = 0,
            totalArticleCount = 0
        )
        whenever(feedMetadataDao.getFeedMetadata()).thenReturn(freshMetadata)
        whenever(storyDao.getAllStories()).thenReturn(emptyList())

        repository = LiveStoryRepository(
            listOf(failingAdapter1, failingAdapter2),
            storyDao,
            articleDao,
            sourceDao,
            feedMetadataDao,
            eventClusterDao,
            database,
            mockRepository,
            digestGenerator,
            clusteringService,
            debugFixtureProvider
        )

        val result = repository.refresh()

        assertTrue(result.isSuccess)
        verify(feedMetadataDao, atLeastOnce()).insertMetadata(argThat {
            successfulSourceCount == 0 && failedSourceCount == 2
        })
    }

    @Test
    fun `refresh handles partial source failures`() = runTest {
        val successAdapter = mock<RssSourceAdapter>()
        val failingAdapter = mock<RssSourceAdapter>()

        whenever(successAdapter.sourceId).thenReturn("success-source")
        whenever(successAdapter.sourceName).thenReturn("Success Source")
        whenever(failingAdapter.sourceId).thenReturn("fail-source")
        whenever(failingAdapter.fetchArticles()).thenReturn(emptyList())

        val article = SourceArticleRecord(
            url = "https://example.com/article",
            publishedAt = Instant.now(),
            languageTag = "en",
            headline = "Test Article",
            excerpt = "Test excerpt",
            contentPermission = ContentPermission.EXPLICIT_EXCERPT,
            sourceId = "success-source"
        )
        whenever(successAdapter.fetchArticles()).thenReturn(listOf(article))
        whenever(sourceDao.getSourcesByIds(any())).thenReturn(emptyList())

        // Mock fresh metadata to prevent init block from triggering background refresh
        val freshMetadata = FeedMetadataEntity(
            lastSuccessfulFetch = Instant.now(),
            lastAttemptedFetch = Instant.now(),
            successfulSourceCount = 0,
            failedSourceCount = 0,
            totalArticleCount = 0
        )
        whenever(feedMetadataDao.getFeedMetadata()).thenReturn(freshMetadata)
        whenever(storyDao.getAllStories()).thenReturn(emptyList())

        repository = LiveStoryRepository(
            listOf(successAdapter, failingAdapter),
            storyDao,
            articleDao,
            sourceDao,
            feedMetadataDao,
            eventClusterDao,
            database,
            mockRepository,
            digestGenerator,
            clusteringService,
            debugFixtureProvider
        )

        val result = repository.refresh()

        assertTrue(result.isSuccess)
        verify(storyDao, atLeastOnce()).insertStories(argThat { isNotEmpty() })
        verify(articleDao, atLeastOnce()).insertArticles(argThat { isNotEmpty() })
    }

    @Test
    fun `observeStories prefers live over mock when live cache exists`() = runTest {
        val liveStory = StoryEntity(
            id = "live_feed_story1",
            title = "Live Story",
            summary = "",
            eventTime = null,
            updatedTime = Instant.now(),
            topicIds = emptyList(),
            eventCountryCodes = emptyList(),
            articleIds = emptyList(),
            claimIds = emptyList(),
            lensGapScore = null,
            lensGapStatus = "",
            lensGapIsDemo = false
        )

        val mockStory = StoryEntity(
            id = "mock_story1",
            title = "Mock Story",
            summary = "",
            eventTime = null,
            updatedTime = Instant.now(),
            topicIds = emptyList(),
            eventCountryCodes = emptyList(),
            articleIds = emptyList(),
            claimIds = emptyList(),
            lensGapScore = null,
            lensGapStatus = "",
            lensGapIsDemo = false
        )

        whenever(storyDao.observeAllStories()).thenReturn(flowOf(listOf(liveStory, mockStory)))

        repository = LiveStoryRepository(
            rssAdapters,
            storyDao,
            articleDao,
            sourceDao,
            feedMetadataDao,
            eventClusterDao,
            database,
            mockRepository,
            digestGenerator,
            clusteringService,
            debugFixtureProvider
        )

        val stories = repository.observeStories().first()

        assertEquals(1, stories.size)
        assertEquals("Live Story", stories[0].title)
    }

    @Test
    fun `observeStories falls back to mock when no live cache`() = runTest {
        val mockStory = StoryEntity(
            id = "mock_story1",
            title = "Mock Story",
            summary = "",
            eventTime = null,
            updatedTime = Instant.now(),
            topicIds = emptyList(),
            eventCountryCodes = emptyList(),
            articleIds = emptyList(),
            claimIds = emptyList(),
            lensGapScore = null,
            lensGapStatus = "",
            lensGapIsDemo = false
        )

        whenever(storyDao.observeAllStories()).thenReturn(flowOf(listOf(mockStory)))

        repository = LiveStoryRepository(
            rssAdapters,
            storyDao,
            articleDao,
            sourceDao,
            feedMetadataDao,
            eventClusterDao,
            database,
            mockRepository,
            digestGenerator,
            clusteringService,
            debugFixtureProvider
        )

        val stories = repository.observeStories().first()

        assertEquals(1, stories.size)
        assertEquals("Mock Story", stories[0].title)
    }

    @Test
    fun `refresh preserves article images through ingestion`() = runTest {
        val successAdapter = mock<RssSourceAdapter>()

        whenever(successAdapter.sourceId).thenReturn("test-source")
        whenever(successAdapter.sourceName).thenReturn("Test Source")

        val article = SourceArticleRecord(
            url = "https://example.com/article",
            publishedAt = Instant.now(),
            languageTag = "en",
            headline = "Test Article",
            excerpt = "Test excerpt",
            contentPermission = ContentPermission.EXPLICIT_EXCERPT,
            imageUrl = "https://example.com/image.jpg",
            sourceId = "test-source"
        )
        whenever(successAdapter.fetchArticles()).thenReturn(listOf(article))
        whenever(sourceDao.getSourcesByIds(any())).thenReturn(emptyList())

        // Mock fresh metadata to prevent init block from triggering background refresh
        val freshMetadata = FeedMetadataEntity(
            lastSuccessfulFetch = Instant.now(),
            lastAttemptedFetch = Instant.now(),
            successfulSourceCount = 0,
            failedSourceCount = 0,
            totalArticleCount = 0
        )
        whenever(feedMetadataDao.getFeedMetadata()).thenReturn(freshMetadata)
        whenever(storyDao.getAllStories()).thenReturn(emptyList())

        repository = LiveStoryRepository(
            listOf(successAdapter),
            storyDao,
            articleDao,
            sourceDao,
            feedMetadataDao,
            eventClusterDao,
            database,
            mockRepository,
            digestGenerator,
            clusteringService,
            debugFixtureProvider
        )

        val result = repository.refresh()

        assertTrue(result.isSuccess)

        // Verify article was inserted with imageUrl
        verify(articleDao, atLeastOnce()).insertArticles(argThat { articles ->
            articles.isNotEmpty() && articles.first().imageUrl == "https://example.com/image.jpg"
        })

        // Verify story was created with imageUrl from primary article
        verify(storyDao, atLeastOnce()).insertStories(argThat { stories ->
            stories.isNotEmpty() && stories.first().imageUrl == "https://example.com/image.jpg"
        })
    }
}
