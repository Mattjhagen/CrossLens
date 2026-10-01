package com.crosslens.app.feature.coveragedetails

import androidx.lifecycle.SavedStateHandle
import com.crosslens.app.core.model.Article
import com.crosslens.app.core.model.ContentUseMetadata
import com.crosslens.app.core.model.Story
import com.crosslens.app.data.ingestion.SourceMetadata
import com.crosslens.app.data.ingestion.SourceMetadataRegistry
import com.crosslens.app.data.repository.StoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Unit tests for CoverageDetailsViewModel.
 *
 * Tests cover:
 * - Multi-publisher event clusters
 * - Two-source clusters
 * - Single-source articles
 * - Missing metadata handling
 * - Temporal coverage calculations
 * - Coverage limitation detection
 * - Clustering rationale generation
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CoverageDetailsViewModelTest {

    private lateinit var repository: StoryRepository
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var viewModel: CoverageDetailsViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mock()
        mockSourceMetadata()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        SourceMetadataRegistry.clearTestMetadata()
    }

    @Test
    fun `multi-publisher cluster shows correct diversity counts`() = runTest {
        // Given: 4-publisher event cluster
        val now = Instant.now()
        val story = createTestStory("story1", isEventCluster = true)
        val articles = listOf(
            createTestArticle("article1", "story1", "live_publisher1", now.minus(3, ChronoUnit.HOURS)),
            createTestArticle("article2", "story1", "live_publisher2", now.minus(2, ChronoUnit.HOURS)),
            createTestArticle("article3", "story1", "live_publisher3", now.minus(1, ChronoUnit.HOURS)),
            createTestArticle("article4", "story1", "live_publisher4", now)
        )

        whenever(repository.getStory("story1")).thenReturn(story)
        whenever(repository.getArticlesForStory("story1")).thenReturn(articles)

        savedStateHandle = SavedStateHandle(mapOf("storyId" to "story1"))
        viewModel = CoverageDetailsViewModel(repository, savedStateHandle)
        advanceUntilIdle()

        // Then: Coverage details show correct counts
        val uiState = viewModel.uiState.value
        assertTrue(uiState is CoverageDetailsUiState.Success)
        val details = (uiState as CoverageDetailsUiState.Success).details

        assertEquals(4, details.distinctPublisherCount)
        assertEquals(4, details.sources.size)
        assertEquals("Test Story story1", details.eventTitle)
    }

    @Test
    fun `two-source cluster shows limitation notice`() = runTest {
        // Given: 2-publisher event cluster
        val now = Instant.now()
        val story = createTestStory("story1", isEventCluster = true)
        val articles = listOf(
            createTestArticle("article1", "story1", "live_publisher1", now),
            createTestArticle("article2", "story1", "live_publisher2", now)
        )

        whenever(repository.getStory("story1")).thenReturn(story)
        whenever(repository.getArticlesForStory("story1")).thenReturn(articles)

        savedStateHandle = SavedStateHandle(mapOf("storyId" to "story1"))
        viewModel = CoverageDetailsViewModel(repository, savedStateHandle)
        advanceUntilIdle()

        // Then: Limitation notice for 2 publishers
        val uiState = viewModel.uiState.value
        assertTrue(uiState is CoverageDetailsUiState.Success)
        val details = (uiState as CoverageDetailsUiState.Success).details

        val sourceDiversityLimitation = details.limitations.find {
            it.type == LimitationType.SOURCE_DIVERSITY
        }
        assertNotNull(sourceDiversityLimitation)
        assertTrue(sourceDiversityLimitation!!.description.contains("2 publishers"))
    }

    @Test
    fun `single-source article shows single publisher limitation`() = runTest {
        // Given: Single-source article
        val now = Instant.now()
        val story = createTestStory("story1", isEventCluster = false)
        val articles = listOf(
            createTestArticle("article1", "story1", "live_publisher1", now)
        )

        whenever(repository.getStory("story1")).thenReturn(story)
        whenever(repository.getArticlesForStory("story1")).thenReturn(articles)

        savedStateHandle = SavedStateHandle(mapOf("storyId" to "story1"))
        viewModel = CoverageDetailsViewModel(repository, savedStateHandle)
        advanceUntilIdle()

        // Then: Single publisher limitation shown
        val uiState = viewModel.uiState.value
        assertTrue(uiState is CoverageDetailsUiState.Success)
        val details = (uiState as CoverageDetailsUiState.Success).details

        assertEquals(1, details.distinctPublisherCount)

        val sourceDiversityLimitation = details.limitations.find {
            it.type == LimitationType.SOURCE_DIVERSITY
        }
        assertNotNull(sourceDiversityLimitation)
        assertTrue(sourceDiversityLimitation!!.description.contains("Single publisher"))
        assertTrue(sourceDiversityLimitation.description.contains("incomplete"))
    }

    @Test
    fun `missing metadata handled gracefully`() = runTest {
        // Given: Article with unknown source (no metadata)
        val now = Instant.now()
        val story = createTestStory("story1", isEventCluster = true)
        val articles = listOf(
            createTestArticle("article1", "story1", "live_unknown_source", now),
            createTestArticle("article2", "story1", "live_publisher1", now)
        )

        whenever(repository.getStory("story1")).thenReturn(story)
        whenever(repository.getArticlesForStory("story1")).thenReturn(articles)

        savedStateHandle = SavedStateHandle(mapOf("storyId" to "story1"))
        viewModel = CoverageDetailsViewModel(repository, savedStateHandle)
        advanceUntilIdle()

        // Then: Coverage details load successfully with null metadata
        val uiState = viewModel.uiState.value
        assertTrue(uiState is CoverageDetailsUiState.Success)
        val details = (uiState as CoverageDetailsUiState.Success).details

        assertEquals(2, details.sources.size)

        // Unknown source should have publisher name from article attribution
        val unknownSource = details.sources.find { it.publisherName == "Test attribution" }
        assertNotNull(unknownSource)
        assertEquals(null, unknownSource!!.country)
        assertEquals(null, unknownSource.language)
        assertEquals(null, unknownSource.editorialDescription)
    }

    @Test
    fun `single language cluster shows language limitation`() = runTest {
        // Given: 3 publishers, all English
        val now = Instant.now()
        val story = createTestStory("story1", isEventCluster = true)
        val articles = listOf(
            createTestArticle("article1", "story1", "live_publisher1", now), // English
            createTestArticle("article2", "story1", "live_publisher2", now), // English
            createTestArticle("article3", "story1", "live_publisher3", now)  // English
        )

        whenever(repository.getStory("story1")).thenReturn(story)
        whenever(repository.getArticlesForStory("story1")).thenReturn(articles)

        savedStateHandle = SavedStateHandle(mapOf("storyId" to "story1"))
        viewModel = CoverageDetailsViewModel(repository, savedStateHandle)
        advanceUntilIdle()

        // Then: Language diversity limitation shown
        val uiState = viewModel.uiState.value
        assertTrue(uiState is CoverageDetailsUiState.Success)
        val details = (uiState as CoverageDetailsUiState.Success).details

        val languageLimitation = details.limitations.find {
            it.type == LimitationType.LANGUAGE_DIVERSITY
        }
        assertNotNull(languageLimitation)
        assertTrue(languageLimitation!!.description.contains("one language"))
    }

    @Test
    fun `single country cluster shows geographic limitation`() = runTest {
        // Given: 3 publishers, all from United States
        val now = Instant.now()
        val story = createTestStory("story1", isEventCluster = true)
        val articles = listOf(
            createTestArticle("article1", "story1", "live_publisher1", now), // US
            createTestArticle("article2", "story1", "live_publisher2", now), // US
            createTestArticle("article3", "story1", "live_publisher3", now)  // US
        )

        whenever(repository.getStory("story1")).thenReturn(story)
        whenever(repository.getArticlesForStory("story1")).thenReturn(articles)

        savedStateHandle = SavedStateHandle(mapOf("storyId" to "story1"))
        viewModel = CoverageDetailsViewModel(repository, savedStateHandle)
        advanceUntilIdle()

        // Then: Geographic diversity limitation shown
        val uiState = viewModel.uiState.value
        assertTrue(uiState is CoverageDetailsUiState.Success)
        val details = (uiState as CoverageDetailsUiState.Success).details

        val geoLimitation = details.limitations.find {
            it.type == LimitationType.GEOGRAPHIC_DIVERSITY
        }
        assertNotNull(geoLimitation)
        assertTrue(geoLimitation!!.description.contains("All sources from"))
    }

    @Test
    fun `temporal coverage calculated correctly`() = runTest {
        // Given: Articles spanning 6 hours
        val now = Instant.now()
        val earliest = now.minus(6, ChronoUnit.HOURS)
        val latest = now.minus(2, ChronoUnit.HOURS)

        val story = createTestStory("story1", isEventCluster = true)
        val articles = listOf(
            createTestArticle("article1", "story1", "live_publisher1", earliest),
            createTestArticle("article2", "story1", "live_publisher2", now.minus(4, ChronoUnit.HOURS)),
            createTestArticle("article3", "story1", "live_publisher3", latest)
        )

        whenever(repository.getStory("story1")).thenReturn(story)
        whenever(repository.getArticlesForStory("story1")).thenReturn(articles)

        savedStateHandle = SavedStateHandle(mapOf("storyId" to "story1"))
        viewModel = CoverageDetailsViewModel(repository, savedStateHandle)
        advanceUntilIdle()

        // Then: Temporal coverage shows correct span
        val uiState = viewModel.uiState.value
        assertTrue(uiState is CoverageDetailsUiState.Success)
        val details = (uiState as CoverageDetailsUiState.Success).details

        assertEquals(earliest, details.temporalCoverage.earliest)
        assertEquals(latest, details.temporalCoverage.latest)
        assertTrue(details.temporalCoverage.spanDescription.contains("4h")) // 6 hours to 2 hours = 4 hours span
    }

    @Test
    fun `recent coverage shows temporal limitation`() = runTest {
        // Given: All articles published in last 3 hours
        val now = Instant.now()
        val story = createTestStory("story1", isEventCluster = true)
        val articles = listOf(
            createTestArticle("article1", "story1", "live_publisher1", now.minus(3, ChronoUnit.HOURS)),
            createTestArticle("article2", "story1", "live_publisher2", now.minus(1, ChronoUnit.HOURS)),
            createTestArticle("article3", "story1", "live_publisher3", now)
        )

        whenever(repository.getStory("story1")).thenReturn(story)
        whenever(repository.getArticlesForStory("story1")).thenReturn(articles)

        savedStateHandle = SavedStateHandle(mapOf("storyId" to "story1"))
        viewModel = CoverageDetailsViewModel(repository, savedStateHandle)
        advanceUntilIdle()

        // Then: Temporal limitation shown for recent coverage
        val uiState = viewModel.uiState.value
        assertTrue(uiState is CoverageDetailsUiState.Success)
        val details = (uiState as CoverageDetailsUiState.Success).details

        val temporalLimitation = details.limitations.find {
            it.type == LimitationType.TEMPORAL_SPAN
        }
        assertNotNull(temporalLimitation)
        assertTrue(temporalLimitation!!.description.contains("last"))
        assertTrue(temporalLimitation.description.contains("hours only"))
    }

    @Test
    fun `clustering rationale includes diversity details`() = runTest {
        // Given: Multi-language, multi-country cluster
        val now = Instant.now()
        val story = createTestStory("story1", isEventCluster = true)
        val articles = listOf(
            createTestArticle("article1", "story1", "live_publisher1", now), // US, English
            createTestArticle("article2", "story1", "live_publisher5", now), // France, French
            createTestArticle("article3", "story1", "live_publisher6", now)  // Germany, German
        )

        whenever(repository.getStory("story1")).thenReturn(story)
        whenever(repository.getArticlesForStory("story1")).thenReturn(articles)

        savedStateHandle = SavedStateHandle(mapOf("storyId" to "story1"))
        viewModel = CoverageDetailsViewModel(repository, savedStateHandle)
        advanceUntilIdle()

        // Then: Clustering rationale mentions diversity
        val uiState = viewModel.uiState.value
        assertTrue(uiState is CoverageDetailsUiState.Success)
        val details = (uiState as CoverageDetailsUiState.Success).details

        assertTrue(details.clusteringRationale.contains("3 distinct publishers"))
        assertTrue(details.clusteringRationale.contains("3 languages"))
        assertTrue(details.clusteringRationale.contains("3 countries"))
        assertTrue(details.clusteringRationale.contains("same specific event"))
    }

    @Test
    fun `single-source article has appropriate rationale`() = runTest {
        // Given: Single-source article (not a cluster)
        val now = Instant.now()
        val story = createTestStory("story1", isEventCluster = false)
        val articles = listOf(
            createTestArticle("article1", "story1", "live_publisher1", now)
        )

        whenever(repository.getStory("story1")).thenReturn(story)
        whenever(repository.getArticlesForStory("story1")).thenReturn(articles)

        savedStateHandle = SavedStateHandle(mapOf("storyId" to "story1"))
        viewModel = CoverageDetailsViewModel(repository, savedStateHandle)
        advanceUntilIdle()

        // Then: Rationale indicates single-source
        val uiState = viewModel.uiState.value
        assertTrue(uiState is CoverageDetailsUiState.Success)
        val details = (uiState as CoverageDetailsUiState.Success).details

        assertTrue(details.clusteringRationale.contains("Single-source article"))
        assertTrue(details.clusteringRationale.contains("not part of an event cluster"))
    }

    @Test
    fun `sources sorted by publication time`() = runTest {
        // Given: Articles published out of order
        val now = Instant.now()
        val earliest = now.minus(6, ChronoUnit.HOURS)
        val middle = now.minus(3, ChronoUnit.HOURS)
        val latest = now

        val story = createTestStory("story1", isEventCluster = true)
        val articles = listOf(
            createTestArticle("article1", "story1", "live_publisher1", latest), // Latest
            createTestArticle("article2", "story1", "live_publisher2", earliest), // Earliest
            createTestArticle("article3", "story1", "live_publisher3", middle) // Middle
        )

        whenever(repository.getStory("story1")).thenReturn(story)
        whenever(repository.getArticlesForStory("story1")).thenReturn(articles)

        savedStateHandle = SavedStateHandle(mapOf("storyId" to "story1"))
        viewModel = CoverageDetailsViewModel(repository, savedStateHandle)
        advanceUntilIdle()

        // Then: Sources are sorted oldest first
        val uiState = viewModel.uiState.value
        assertTrue(uiState is CoverageDetailsUiState.Success)
        val details = (uiState as CoverageDetailsUiState.Success).details

        assertEquals(3, details.sources.size)
        assertEquals("Publisher 2", details.sources[0].publisherName) // Earliest
        assertEquals("Publisher 3", details.sources[1].publisherName) // Middle
        assertEquals("Publisher 1", details.sources[2].publisherName) // Latest
    }

    @Test
    fun `editorial descriptions included when available`() = runTest {
        // Given: Sources with editorial descriptions
        val now = Instant.now()
        val story = createTestStory("story1", isEventCluster = true)
        val articles = listOf(
            createTestArticle("article1", "story1", "live_publisher1", now),
            createTestArticle("article2", "story1", "live_publisher2", now)
        )

        whenever(repository.getStory("story1")).thenReturn(story)
        whenever(repository.getArticlesForStory("story1")).thenReturn(articles)

        savedStateHandle = SavedStateHandle(mapOf("storyId" to "story1"))
        viewModel = CoverageDetailsViewModel(repository, savedStateHandle)
        advanceUntilIdle()

        // Then: Editorial descriptions preserved
        val uiState = viewModel.uiState.value
        assertTrue(uiState is CoverageDetailsUiState.Success)
        val details = (uiState as CoverageDetailsUiState.Success).details

        val source1 = details.sources.find { it.publisherName == "Publisher 1" }
        assertNotNull(source1)
        assertEquals("Test publisher 1", source1!!.editorialDescription)
        assertEquals("Test", source1.descriptionProvenance)
    }

    @Test
    fun `story not found shows appropriate error`() = runTest {
        // Given: Story does not exist
        whenever(repository.getStory("missing")).thenReturn(null)

        savedStateHandle = SavedStateHandle(mapOf("storyId" to "missing"))
        viewModel = CoverageDetailsViewModel(repository, savedStateHandle)
        advanceUntilIdle()

        // Then: NotFound state
        val uiState = viewModel.uiState.value
        assertTrue(uiState is CoverageDetailsUiState.NotFound)
    }

    @Test
    fun `empty articles shows error`() = runTest {
        // Given: Story exists but has no articles
        val story = createTestStory("story1", isEventCluster = true)
        whenever(repository.getStory("story1")).thenReturn(story)
        whenever(repository.getArticlesForStory("story1")).thenReturn(emptyList())

        savedStateHandle = SavedStateHandle(mapOf("storyId" to "story1"))
        viewModel = CoverageDetailsViewModel(repository, savedStateHandle)
        advanceUntilIdle()

        // Then: Error state
        val uiState = viewModel.uiState.value
        assertTrue(uiState is CoverageDetailsUiState.Error)
        assertEquals("No articles found", (uiState as CoverageDetailsUiState.Error).message)
    }

    // Helper functions
    private fun mockSourceMetadata() {
        // Standard test sources
        for (i in 1..4) {
            SourceMetadataRegistry.registerForTesting(
                "publisher$i",
                SourceMetadata(
                    publisherName = "Publisher $i",
                    country = "United States",
                    primaryLanguage = "English",
                    languageCode = "en-US",
                    editorialDescription = "Test publisher $i",
                    descriptionProvenance = "Test",
                    homepage = "https://example.com"
                )
            )
        }

        // French source
        SourceMetadataRegistry.registerForTesting(
            "publisher5",
            SourceMetadata(
                publisherName = "Publisher 5",
                country = "France",
                primaryLanguage = "French",
                languageCode = "fr",
                editorialDescription = "Test French publisher",
                descriptionProvenance = "Test",
                homepage = "https://example.fr"
            )
        )

        // German source
        SourceMetadataRegistry.registerForTesting(
            "publisher6",
            SourceMetadata(
                publisherName = "Publisher 6",
                country = "Germany",
                primaryLanguage = "German",
                languageCode = "de",
                editorialDescription = "Test German publisher",
                descriptionProvenance = "Test",
                homepage = "https://example.de"
            )
        )
    }

    private fun createTestStory(
        id: String,
        isEventCluster: Boolean
    ): Story {
        return Story(
            id = id,
            title = "Test Story $id",
            summary = "Test summary",
            eventTime = Instant.now(),
            updatedTime = Instant.now(),
            articleIds = emptyList(),
            claimIds = emptyList(),
            topicIds = emptyList(),
            eventCountryCodes = emptyList(),
            lensGapAssessment = null,
            isEventCluster = isEventCluster,
            imageUrl = null
        )
    }

    private fun createTestArticle(
        id: String,
        storyId: String,
        sourceId: String,
        publishedTime: Instant
    ): Article {
        return Article(
            id = id,
            storyId = storyId,
            sourceId = sourceId,
            originalUrl = "https://example.com/$id",
            publishedTime = publishedTime,
            originalLanguage = "en",
            originalHeadline = "Test Headline $id",
            originalContent = "Test content $id",
            originalExcerpt = "Test excerpt $id",
            attribution = "Test attribution",
            contentUseMetadata = ContentUseMetadata(isDemo = true, isDemoPlaceholder = false),
            imageUrl = null
        )
    }
}
