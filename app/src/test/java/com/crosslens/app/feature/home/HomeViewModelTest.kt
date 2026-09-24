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
import org.mockito.kotlin.*
import java.time.Instant
import org.junit.Assert.*
import kotlinx.coroutines.flow.first

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

    @Test
    fun `local filter active with no location shows EmptyLocal state`() = runTest {
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

        // Preferences with showLocalOnly=true but no location
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
                    demoLocalLocation = null,
                    showLocalOnly = true
                )
            )
        )

        val viewModel = HomeViewModel(storyRepository, userPreferencesRepository)
        advanceUntilIdle()

        // Should show EmptyLocal state because filter is on but no location selected
        assertTrue(viewModel.uiState.value is HomeUiState.EmptyLocal)
    }

    @Test
    fun `filter state initializes from preferences`() = runTest {
        whenever(storyRepository.observeStories()).thenReturn(flowOf(emptyList()))

        // Preferences with showLocalOnly=true
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
                    demoLocalLocation = null,
                    showLocalOnly = true
                )
            )
        )

        val viewModel = HomeViewModel(storyRepository, userPreferencesRepository)
        advanceUntilIdle()

        // Filter state should be restored from preferences
        assertTrue(viewModel.showLocalOnly.value)
    }

    @Test
    fun `toggleLocalFilter persists state`() = runTest {
        whenever(storyRepository.observeStories()).thenReturn(flowOf(emptyList()))
        val viewModel = HomeViewModel(storyRepository, userPreferencesRepository)
        advanceUntilIdle()

        viewModel.toggleLocalFilter()
        advanceUntilIdle()

        // Should call updateShowLocalOnly with true
        verify(userPreferencesRepository).updateShowLocalOnly(true)
    }

    @Test
    fun `currentLocation flow maps location ID to LocalLocation object`() = runTest {
        whenever(storyRepository.observeStories()).thenReturn(flowOf(emptyList()))

        // Preferences with Seattle location
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
                    demoLocalLocation = "seattle_wa_us",
                    showLocalOnly = false
                )
            )
        )

        val viewModel = HomeViewModel(storyRepository, userPreferencesRepository)
        advanceUntilIdle()

        val location = viewModel.currentLocation.value
        assertNotNull(location)
        assertEquals("Seattle", location?.cityName)
        assertEquals("Washington", location?.regionName)
    }

    @Test
    fun `currentLocation is null when no location selected`() = runTest {
        whenever(storyRepository.observeStories()).thenReturn(flowOf(emptyList()))
        val viewModel = HomeViewModel(storyRepository, userPreferencesRepository)
        advanceUntilIdle()

        assertNull(viewModel.currentLocation.value)
    }
}
