package com.crosslens.app.feature.home

import com.crosslens.app.core.model.*
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
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var storyRepository: StoryRepository
    private lateinit var userPreferencesRepository: com.crosslens.app.data.preferences.UserPreferencesRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        storyRepository = mock()
        userPreferencesRepository = mock()

        // Default preferences with no local location
        whenever(userPreferencesRepository.preferencesFlow).thenReturn(
            flowOf(
                UserPreferences(
                    readingLanguage = "en",
                    homeCountry = null,
                    homeRegion = null,
                    enabledSourceIds = emptySet(),
                    translationPreference = TranslationPreference.AUTO,
                    theme = Theme.SYSTEM,
                    reducedMotion = false,
                    demoLocalLocation = null
                )
            )
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `empty story list shows Empty state`() = runTest {
        whenever(storyRepository.observeStories()).thenReturn(flowOf(emptyList()))
        val viewModel = HomeViewModel(storyRepository, userPreferencesRepository)

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is HomeUiState.Empty)
    }

    @Test
    fun `story list shows Success state`() = runTest {
        val stories = listOf(
            Story(
                id = "test-1",
                title = "Test Story",
                summary = "Summary",
                eventTime = Instant.now(),
                updatedTime = Instant.now(),
                topicIds = listOf("tech"),
                eventCountryCodes = listOf("US"),
                articleIds = listOf("art-1"),
                claimIds = emptyList(),
                lensGapAssessment = null
            )
        )
        whenever(storyRepository.observeStories()).thenReturn(flowOf(stories))
        val viewModel = HomeViewModel(storyRepository, userPreferencesRepository)

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is HomeUiState.Success)
        assertEquals(1, (state as HomeUiState.Success).stories.size)
    }
}
