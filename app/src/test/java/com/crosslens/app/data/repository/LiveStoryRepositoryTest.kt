package com.crosslens.app.data.repository

import android.app.Application
import androidx.room.Room
import com.crosslens.app.core.model.FeedState
import com.crosslens.app.core.model.ReadingState
import com.crosslens.app.data.clustering.EventClusteringService
import com.crosslens.app.data.fixture.DebugEventFixtureProvider
import com.crosslens.app.data.ingestion.RssSourceAdapter
import com.crosslens.app.data.ingestion.SourceArticleRecord
import com.crosslens.app.data.local.CrossLensDatabase
import com.crosslens.app.data.local.dao.EventClusterDao
import com.crosslens.app.data.local.entity.FeedMetadataEntity
import com.crosslens.app.data.local.entity.StoryEntity
import com.crosslens.app.data.mock.MockStoryRepository
import com.crosslens.app.data.preferences.ReadingStateRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.time.Instant

/** Use real Room transactions: mock DAOs cannot prove cache rollback or seed integrity. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class LiveStoryRepositoryTest {
    private lateinit var database: CrossLensDatabase
    private lateinit var fallback: MockStoryRepository
    private val reading: ReadingStateRepository = mock()
    private val digest = SourceDigestGenerator()
    private val fixture = object : DebugEventFixtureProvider {
        override fun getReadAcrossFixture(): List<SourceArticleRecord> =
            error("Normal feed refresh must not request demo fixtures")
    }

    @Before fun setup() {
        database = Room.inMemoryDatabaseBuilder(RuntimeEnvironment.getApplication(), CrossLensDatabase::class.java)
            .allowMainThreadQueries().build()
        fallback = MockStoryRepository(database.storyDao(), database.articleDao(), database.claimDao(),
            database.frameObservationDao(), database, digest)
        whenever(reading.readingStateFlow).thenReturn(flowOf(ReadingState(emptySet(), null)))
    }
    @After fun close() = database.close()

    private fun repository(adapters: List<RssSourceAdapter> = emptyList(),
        clusters: EventClusterDao = database.eventClusterDao()) = LiveStoryRepository(
        adapters, database.storyDao(), database.articleDao(), database.sourceDao(), database.feedMetadataDao(),
        clusters, database, fallback, digest, EventClusteringService(), fixture, reading)

    private fun story(id: String) = StoryEntity(id, "Cached headline", "Excerpt", null, Instant.now(),
        emptyList(), emptyList(), emptyList(), emptyList(), null, "NOT_ASSESSED", false)

    private suspend fun adapter(url: String = "https://publisher.example/news/1"): RssSourceAdapter {
        val adapter = mock<RssSourceAdapter>()
        whenever(adapter.sourceId).thenReturn("test-source")
        whenever(adapter.sourceName).thenReturn("Test publisher")
        whenever(adapter.homepage).thenReturn("https://publisher.example")
        whenever(adapter.fetchArticles()).thenReturn(listOf(SourceArticleRecord(url, Instant.parse("2026-10-31T12:00:00Z"),
            "en", "A dated report", "Publisher excerpt", imageUrl = "https://publisher.example/image.jpg", sourceId = "test-source")))
        return adapter
    }

    @Test fun `fresh offline launch stays empty without demo fallback`() = runTest {
        val repository = repository()
        assertTrue(repository.refresh().isFailure)
        assertTrue(repository.observeStories().first().isEmpty())
        assertEquals(FeedState.ERROR, repository.getFeedMetadata().state)
    }

    @Test fun `legacy demo cache is excluded while live stories remain`() = runTest {
        database.storyDao().insertStories(listOf(story("fixture_story"), story("live_feed_existing")))
        assertEquals(listOf("live_feed_existing"), repository().observeStories().first().map { it.id })
        assertEquals(FeedState.CACHED, repository().getFeedMetadata().state)
        assertTrue(repository().observeLocalStories("legacy_location").first().isEmpty())
    }

    @Test fun `startup seeding preserves live cache and is idempotent`() = runTest {
        database.storyDao().insertStories(listOf(story("live_feed_existing")))
        fallback.seedData()
        val count = database.storyDao().getAllStories().size
        fallback.seedData()
        assertEquals(count, database.storyDao().getAllStories().size)
        assertNotNull(database.storyDao().getStoryById("live_feed_existing"))
    }

    @Test fun `seeding repairs missing fixture articles without deleting existing stories`() = runTest {
        fallback.seedData()
        val fixtureStory = database.storyDao().getAllStories().first()
        database.articleDao().deleteArticlesByStory(fixtureStory.id)
        fallback.seedData()
        assertTrue(fallback.getArticlesForStory(fixtureStory.id).isNotEmpty())
        assertNotNull(database.storyDao().getStoryById(fixtureStory.id))
    }

    @Test fun `offline refresh reports failure and preserves last good cache`() = runTest {
        database.storyDao().insertStories(listOf(story("live_feed_existing")))
        val lastSuccess = Instant.now().minusSeconds(1800).truncatedTo(java.time.temporal.ChronoUnit.MILLIS)
        database.feedMetadataDao().insertMetadata(FeedMetadataEntity(lastSuccessfulFetch = lastSuccess,
            lastAttemptedFetch = lastSuccess, successfulSourceCount = 1, failedSourceCount = 0, totalArticleCount = 1))
        val repository = repository()
        assertTrue(repository.refresh().isFailure)
        assertNotNull(database.storyDao().getStoryById("live_feed_existing"))
        assertEquals(lastSuccess, repository.getFeedMetadata().lastUpdated)
        assertEquals(FeedState.CACHED, repository.getFeedMetadata().state)
    }

    @Test fun `stable refresh retains IDs and images without duplicates`() = runTest {
        val repository = repository(listOf(adapter()))
        assertTrue(repository.refresh().isSuccess)
        val first = repository.observeStories().first().single()
        assertTrue(repository.refresh().isSuccess)
        assertEquals(first.id, repository.observeStories().first().single().id)
        val article = repository.getArticlesForStory(first.id).single()
        assertEquals("https://publisher.example/image.jpg", article.imageUrl)
        assertNull(repository.getSourceDigest(first.id))
        assertNull(first.lensGapAssessment?.score)
    }

    @Test fun `transaction failure rolls back removed cache and inserted stories`() = runTest {
        database.storyDao().insertStories(listOf(story("live_feed_previous")))
        val clusters = mock<EventClusterDao>()
        whenever(clusters.insertClusters(any())).thenThrow(IllegalStateException("Persistence failure"))
        assertTrue(repository(listOf(adapter()), clusters).refresh().isFailure)
        assertEquals(listOf("live_feed_previous"), database.storyDao().getAllStories().map { it.id })
    }

    @Test fun `refresh retains saved legacy IDs and last opened story`() = runTest {
        database.storyDao().insertStories(listOf(story("live_feed_saved"), story("live_feed_reading"), story("live_feed_old")))
        whenever(reading.readingStateFlow).thenReturn(flowOf(ReadingState(setOf("live_feed_saved"), "live_feed_reading")))
        assertTrue(repository(listOf(adapter())).refresh().isSuccess)
        assertNotNull(database.storyDao().getStoryById("live_feed_saved"))
        assertNotNull(database.storyDao().getStoryById("live_feed_reading"))
        assertNull(database.storyDao().getStoryById("live_feed_old"))
    }

    @Test fun `partial provider failure still commits available reporting`() = runTest {
        val unavailable = mock<RssSourceAdapter>()
        whenever(unavailable.fetchArticles()).thenReturn(emptyList())
        val repository = repository(listOf(adapter(), unavailable))
        assertTrue(repository.refresh().isSuccess)
        val metadata = repository.getFeedMetadata()
        assertEquals(1, metadata.successfulSourceCount)
        assertEquals(1, metadata.failedSourceCount)
    }

    @Test fun `cancellation is never converted to a successful refresh`() = runTest {
        val unavailable = mock<RssSourceAdapter>()
        whenever(unavailable.fetchArticles()).thenThrow(CancellationException("Canceled"))
        try {
            repository(listOf(unavailable)).refresh()
            fail("Cancellation must propagate")
        } catch (_: CancellationException) { }
        assertNull(database.feedMetadataDao().getFeedMetadata())
    }

    @Test fun `legacy demo fallback never appears in normal feed`() = runTest {
        fallback.seedData()
        assertEquals(FeedState.ERROR, repository().getFeedMetadata().state)
        assertTrue(repository().observeStories().first().isEmpty())
    }
}
