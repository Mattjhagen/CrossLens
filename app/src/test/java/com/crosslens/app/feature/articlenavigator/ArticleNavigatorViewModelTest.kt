package com.crosslens.app.feature.articlenavigator

import androidx.lifecycle.SavedStateHandle
import com.crosslens.app.core.model.Article
import com.crosslens.app.core.model.ContentUseMetadata
import com.crosslens.app.core.model.Story
import com.crosslens.app.data.ingestion.SourceMetadata
import com.crosslens.app.data.ingestion.SourceMetadataRegistry
import com.crosslens.app.data.repository.StoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant

/**
 * Unit tests for ArticleNavigatorViewModel.
 *
 * Tests cover:
 * - Eligibility threshold (3 distinct publishers)
 * - Navigation state transitions
 * - Boundary detection
 * - Article position tracking
 * - Feed position tracking
 * - Error handling
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ArticleNavigatorViewModelTest {

    private lateinit var repository: StoryRepository
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var viewModel: ArticleNavigatorViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mock()

        // Mock registry to return test metadata
        mockSourceMetadata()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        SourceMetadataRegistry.clearTestMetadata()
    }

    @Test
    fun `horizontal navigation requires 3 distinct publishers`() = runTest {
        // Given: 2-source cluster (below threshold)
        val story = createTestStory(id = "story1", isEventCluster = true, articleCount = 2)
        val articles = listOf(
            createTestArticle("article1", "story1", "live_publisher1"),
            createTestArticle("article2", "story1", "live_publisher2")
        )

        whenever(repository.observeStories()).thenReturn(flowOf(listOf(story)))
        whenever(repository.getArticlesForStory("story1")).thenReturn(articles)

        savedStateHandle = SavedStateHandle(mapOf("storyId" to "story1"))
        viewModel = ArticleNavigatorViewModel(repository, savedStateHandle)
        advanceUntilIdle()

        // Then: Horizontal navigation is NOT available (requires 3)
        val navState = viewModel.navigationState.value
        assertNotNull(navState)
        assertFalse("Should require 3 distinct publishers", navState!!.hasHorizontalNavigation)
    }

    @Test
    fun `horizontal navigation enabled for 3 distinct publishers`() = runTest {
        // Given: 3-source cluster (meets threshold)
        val story = createTestStory(id = "story1", isEventCluster = true, articleCount = 3)
        val articles = listOf(
            createTestArticle("article1", "story1", "live_publisher1"),
            createTestArticle("article2", "story1", "live_publisher2"),
            createTestArticle("article3", "story1", "live_publisher3")
        )

        whenever(repository.observeStories()).thenReturn(flowOf(listOf(story)))
        whenever(repository.getArticlesForStory("story1")).thenReturn(articles)

        savedStateHandle = SavedStateHandle(mapOf("storyId" to "story1"))
        viewModel = ArticleNavigatorViewModel(repository, savedStateHandle)
        advanceUntilIdle()

        // Then: Horizontal navigation IS available
        val navState = viewModel.navigationState.value
        assertNotNull(navState)
        assertTrue("Should allow navigation with 3 distinct publishers", navState!!.hasHorizontalNavigation)
    }

    @Test
    fun `single-source story has no horizontal navigation`() = runTest {
        // Given: Single-source story
        val story = createTestStory(id = "story1", isEventCluster = false, articleCount = 1)
        val articles = listOf(createTestArticle("article1", "story1", "live_publisher1"))

        whenever(repository.observeStories()).thenReturn(flowOf(listOf(story)))
        whenever(repository.getArticlesForStory("story1")).thenReturn(articles)

        savedStateHandle = SavedStateHandle(mapOf("storyId" to "story1"))
        viewModel = ArticleNavigatorViewModel(repository, savedStateHandle)
        advanceUntilIdle()

        // Then: No horizontal navigation
        val navState = viewModel.navigationState.value
        assertNotNull(navState)
        assertFalse(navState!!.hasHorizontalNavigation)
    }

    @Test
    fun `vertical navigation resets to first article in story`() = runTest {
        // Given: Two stories in feed
        val story1 = createTestStory(id = "story1", isEventCluster = true, articleCount = 3)
        val story2 = createTestStory(id = "story2", isEventCluster = true, articleCount = 3)

        val articles1 = createMultipleTestArticles("story1", 3)
        val articles2 = createMultipleTestArticles("story2", 3)

        whenever(repository.observeStories()).thenReturn(flowOf(listOf(story1, story2)))
        whenever(repository.getArticlesForStory("story1")).thenReturn(articles1)
        whenever(repository.getArticlesForStory("story2")).thenReturn(articles2)

        savedStateHandle = SavedStateHandle(mapOf("storyId" to "story1"))
        viewModel = ArticleNavigatorViewModel(repository, savedStateHandle)
        advanceUntilIdle()

        // When: Navigate to second article then to next story
        viewModel.navigateToNextInCluster()
        advanceUntilIdle()
        viewModel.navigateToNextInFeed()
        advanceUntilIdle()

        // Then: Should reset to first article (position 0) in new story
        val navState = viewModel.navigationState.value
        assertEquals("Feed position should advance", 1, navState!!.currentFeedPosition)
        assertEquals("Cluster position should reset to 0", 0, navState.currentClusterPosition)
    }

    @Test
    fun `boundary detection at first in cluster`() = runTest {
        val story = createTestStory(id = "story1", isEventCluster = true, articleCount = 3)
        val articles = createMultipleTestArticles("story1", 3)

        whenever(repository.observeStories()).thenReturn(flowOf(listOf(story)))
        whenever(repository.getArticlesForStory("story1")).thenReturn(articles)

        savedStateHandle = SavedStateHandle(mapOf("storyId" to "story1"))
        viewModel = ArticleNavigatorViewModel(repository, savedStateHandle)
        advanceUntilIdle()

        val navState = viewModel.navigationState.value
        assertFalse("Should not have previous at first article", navState!!.hasPreviousInCluster)
        assertTrue("Should have next", navState.hasNextInCluster)
    }

    @Test
    fun `boundary detection at last in cluster`() = runTest {
        val story = createTestStory(id = "story1", isEventCluster = true, articleCount = 3)
        val articles = createMultipleTestArticles("story1", 3)

        whenever(repository.observeStories()).thenReturn(flowOf(listOf(story)))
        whenever(repository.getArticlesForStory("story1")).thenReturn(articles)

        savedStateHandle = SavedStateHandle(mapOf("storyId" to "story1"))
        viewModel = ArticleNavigatorViewModel(repository, savedStateHandle)
        advanceUntilIdle()

        // Navigate to last
        viewModel.navigateToNextInCluster()
        advanceUntilIdle()
        viewModel.navigateToNextInCluster()
        advanceUntilIdle()

        val navState = viewModel.navigationState.value
        assertEquals(2, navState!!.currentClusterPosition)
        assertTrue("Should have previous", navState.hasPreviousInCluster)
        assertFalse("Should not have next at last article", navState.hasNextInCluster)
    }

    @Test
    fun `boundary detection at first story in feed`() = runTest {
        val story1 = createTestStory(id = "story1", isEventCluster = true, articleCount = 3)
        val story2 = createTestStory(id = "story2", isEventCluster = true, articleCount = 3)

        whenever(repository.observeStories()).thenReturn(flowOf(listOf(story1, story2)))
        whenever(repository.getArticlesForStory("story1")).thenReturn(createMultipleTestArticles("story1", 3))

        savedStateHandle = SavedStateHandle(mapOf("storyId" to "story1"))
        viewModel = ArticleNavigatorViewModel(repository, savedStateHandle)
        advanceUntilIdle()

        val navState = viewModel.navigationState.value
        assertEquals(0, navState!!.currentFeedPosition)
        assertFalse("Should not have previous at first story", navState.hasPreviousInFeed)
        assertTrue("Should have next story", navState.hasNextInFeed)
    }

    @Test
    fun `invalid story ID shows error state`() = runTest {
        val story = createTestStory(id = "story1", isEventCluster = true, articleCount = 3)

        whenever(repository.observeStories()).thenReturn(flowOf(listOf(story)))

        savedStateHandle = SavedStateHandle(mapOf("storyId" to "invalid_id"))
        viewModel = ArticleNavigatorViewModel(repository, savedStateHandle)
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertTrue(uiState is ArticleNavigatorUiState.Error)
        assertEquals("Story not found in feed", (uiState as ArticleNavigatorUiState.Error).message)
    }

    @Test
    fun `empty articles shows error state`() = runTest {
        val story = createTestStory(id = "story1", isEventCluster = true, articleCount = 0)

        whenever(repository.observeStories()).thenReturn(flowOf(listOf(story)))
        whenever(repository.getArticlesForStory("story1")).thenReturn(emptyList())

        savedStateHandle = SavedStateHandle(mapOf("storyId" to "story1"))
        viewModel = ArticleNavigatorViewModel(repository, savedStateHandle)
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertTrue(uiState is ArticleNavigatorUiState.Error)
        assertEquals("No articles found", (uiState as ArticleNavigatorUiState.Error).message)
    }

    @Test
    fun `return to first and refresh resets positions`() = runTest {
        val stories = listOf(
            createTestStory(id = "story1", isEventCluster = true, articleCount = 3),
            createTestStory(id = "story2", isEventCluster = true, articleCount = 3),
            createTestStory(id = "story3", isEventCluster = true, articleCount = 3)
        )

        whenever(repository.observeStories()).thenReturn(flowOf(stories))
        whenever(repository.getArticlesForStory(any())).thenReturn(createMultipleTestArticles("story", 3))
        whenever(repository.refresh()).then {}

        savedStateHandle = SavedStateHandle(mapOf("storyId" to "story2"))
        viewModel = ArticleNavigatorViewModel(repository, savedStateHandle)
        advanceUntilIdle()

        // When: Return to first and refresh
        viewModel.returnToFirstAndRefresh()
        advanceUntilIdle()

        // Then: Should be at first story, first article
        val navState = viewModel.navigationState.value
        assertEquals("Should be at first story", 0, navState!!.currentFeedPosition)
        assertEquals("Should be at first article", 0, navState.currentClusterPosition)
        verify(repository).refresh()
    }

    // Helper functions
    private fun mockSourceMetadata() {
        // Populate registry with test metadata
        for (i in 1..10) {
            SourceMetadataRegistry.registerForTesting(
                "publisher$i",
                SourceMetadata(
                    publisherName = "Publisher $i",
                    country = "US",
                    primaryLanguage = "English",
                    languageCode = "en",
                    editorialDescription = "Test publisher $i",
                    descriptionProvenance = "Test",
                    homepage = "https://example.com"
                )
            )
        }
    }

    private fun createTestStory(
        id: String,
        isEventCluster: Boolean,
        articleCount: Int
    ): Story {
        return Story(
            id = id,
            title = "Test Story $id",
            summary = "Test summary",
            updatedTime = Instant.now(),
            articleIds = (1..articleCount).map { "article$it" },
            claimIds = emptyList(),
            topicIds = emptyList(),
            eventCountryCodes = emptyList(),
            lensGapAssessment = null,
            isEventCluster = isEventCluster,
            imageUrl = null,
            eventTime = null
        )
    }

    private fun createTestArticle(
        id: String,
        storyId: String,
        sourceId: String
    ): Article {
        return Article(
            id = id,
            storyId = storyId,
            sourceId = sourceId,
            originalUrl = "https://example.com/$id",
            publishedTime = Instant.now(),
            originalLanguage = "en",
            originalHeadline = "Test Headline $id",
            originalContent = "Test content $id",
            originalExcerpt = "Test excerpt $id",
            attribution = "Test attribution",
            contentUseMetadata = ContentUseMetadata(isDemo = true, isDemoPlaceholder = false),
            imageUrl = null
        )
    }

    private fun createMultipleTestArticles(storyId: String, count: Int): List<Article> {
        return (1..count).map { index ->
            createTestArticle("article$index", storyId, "live_publisher$index")
        }
    }
}
