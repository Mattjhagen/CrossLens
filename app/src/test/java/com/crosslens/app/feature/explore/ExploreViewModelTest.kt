package com.crosslens.app.feature.explore

import com.crosslens.app.core.model.Story
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
        val viewModel = ExploreViewModel(storyRepository)

        viewModel.toggleTopic("technology")
        viewModel.toggleRegion("europe")
        viewModel.clearFilters()

        assertEquals(emptySet<String>(), viewModel.selectedTopics.value)
        assertEquals(emptySet<String>(), viewModel.selectedRegions.value)
    }
}
