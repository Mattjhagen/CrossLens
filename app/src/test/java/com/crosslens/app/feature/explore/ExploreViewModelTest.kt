package com.crosslens.app.feature.explore

import com.crosslens.app.core.model.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect
import com.crosslens.app.data.repository.StoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.Instant
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class ExploreViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var storyRepository: StoryRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        storyRepository = mock()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test fun `region and country describe publisher while language and search filter cached reporting`() = runTest {
        val story = Story("one", "Climate report", "Summary", null, Instant.now(), listOf("environment"),
            listOf("JP"), listOf("article"), emptyList(), null)
        val article = Article("article", "one", "publisher", "https://publisher.example/a", Instant.now(), "fr",
            "Titre", "Extrait", "Extrait", "Publisher", ContentUseMetadata(false, false))
        val source = Source("publisher", "Publisher", "https://publisher.example", listOf("FR"), listOf("europe"), listOf("fr"), null, null, null)
        val sources = mock<com.crosslens.app.data.repository.SourceRepository>()
        whenever(sources.observeSources()).thenReturn(flowOf(listOf(source)))
        whenever(storyRepository.observeStories()).thenReturn(flowOf(listOf(story)))
        whenever(storyRepository.getArticlesForStory("one")).thenReturn(listOf(article))
        val viewModel = ExploreViewModel(storyRepository, sources)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.filteredStories.collect { } }
        viewModel.toggleRegion("europe")
        viewModel.toggleCountry("FR")
        viewModel.toggleLanguage("fr")
        advanceUntilIdle()
        assertEquals(listOf("one"), viewModel.filteredStories.value.map { it.id })
        viewModel.toggleCountry("FR")
        viewModel.toggleCountry("JP")
        advanceUntilIdle()
        assertTrue(viewModel.filteredStories.value.isEmpty())
        viewModel.clearFilters()
        viewModel.updateQuery("no matching headline")
        advanceUntilIdle()
        assertTrue(viewModel.filteredStories.value.isEmpty())
    }

    @Test
    fun `clearFilters resets all filters`() = runTest {
        val stories = listOf(
            Story(
                id = "1",
                title = "Story",
                summary = "",
                eventTime = null,
                updatedTime = Instant.now(),
                topicIds = listOf("technology"),
                eventCountryCodes = listOf("US"),
                articleIds = emptyList(),
                claimIds = emptyList(),
                lensGapAssessment = null
            )
        )
        whenever(storyRepository.observeStories()).thenReturn(flowOf(stories))
        val sourceRepository = mock<com.crosslens.app.data.repository.SourceRepository>()
        whenever(sourceRepository.observeSources()).thenReturn(flowOf(emptyList()))
        val viewModel = ExploreViewModel(storyRepository, sourceRepository)

        viewModel.toggleTopic("technology")
        viewModel.toggleRegion("europe")
        viewModel.clearFilters()

        assertEquals(emptySet<String>(), viewModel.selectedTopics.value)
        assertEquals(emptySet<String>(), viewModel.selectedRegions.value)
    }
}
