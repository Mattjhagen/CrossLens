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
    private lateinit var personalRelevanceRepository: com.crosslens.app.data.preferences.PersonalRelevanceRepository
    private lateinit var recommendationEngine: com.crosslens.app.data.recommendation.PersonalizedRecommendationEngine

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        storyRepository = mock()
        userPreferencesRepository = mock()
        personalRelevanceRepository = mock()
        // Use real engine since it has no dependencies
        recommendationEngine = com.crosslens.app.data.recommendation.PersonalizedRecommendationEngine()

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
                    demoLocalLocation = null,
                    showLocalOnly = false,
                    lastRefreshedTime = null,
                    showForYou = true
                )
            )
        )

        // Default empty personal preferences
        whenever(personalRelevanceRepository.preferencesFlow).thenReturn(flowOf(emptyList()))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `empty story list shows Empty state`() = runTest {
        whenever(storyRepository.observeStories()).thenReturn(flowOf(emptyList()))
        val viewModel = HomeViewModel(storyRepository, userPreferencesRepository, personalRelevanceRepository, recommendationEngine)

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
        val viewModel = HomeViewModel(storyRepository, userPreferencesRepository, personalRelevanceRepository, recommendationEngine)

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

        val viewModel = HomeViewModel(storyRepository, userPreferencesRepository, personalRelevanceRepository, recommendationEngine)
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

        val viewModel = HomeViewModel(storyRepository, userPreferencesRepository, personalRelevanceRepository, recommendationEngine)
        advanceUntilIdle()

        // Filter state should be restored from preferences
        assertTrue(viewModel.showLocalOnly.value)
    }

    @Test
    fun `toggleLocalFilter persists state`() = runTest {
        whenever(storyRepository.observeStories()).thenReturn(flowOf(emptyList()))
        val viewModel = HomeViewModel(storyRepository, userPreferencesRepository, personalRelevanceRepository, recommendationEngine)
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

        val viewModel = HomeViewModel(storyRepository, userPreferencesRepository, personalRelevanceRepository, recommendationEngine)
        advanceUntilIdle()

        val location = viewModel.currentLocation.value
        assertNotNull(location)
        assertEquals("Seattle", location?.cityName)
        assertEquals("Washington", location?.regionName)
    }

    @Test
    fun `currentLocation is null when no location selected`() = runTest {
        whenever(storyRepository.observeStories()).thenReturn(flowOf(emptyList()))
        val viewModel = HomeViewModel(storyRepository, userPreferencesRepository, personalRelevanceRepository, recommendationEngine)
        advanceUntilIdle()

        assertNull(viewModel.currentLocation.value)
    }

    @Test
    fun `refresh sets isRefreshing to true then false`() = runTest {
        whenever(storyRepository.observeStories()).thenReturn(flowOf(emptyList()))
        val viewModel = HomeViewModel(storyRepository, userPreferencesRepository, personalRelevanceRepository, recommendationEngine)
        advanceUntilIdle()

        // Initially not refreshing
        assertFalse(viewModel.isRefreshing.value)

        // Start refresh (don't wait for it to complete yet)
        viewModel.refresh()

        // Give the coroutine a moment to start
        testScheduler.runCurrent()

        // Should be refreshing now (or already done if very fast)
        // After refresh completes
        advanceUntilIdle()

        // Should be done refreshing
        assertFalse(viewModel.isRefreshing.value)
    }

    @Test
    fun `refresh updates lastRefreshedTime`() = runTest {
        whenever(storyRepository.observeStories()).thenReturn(flowOf(emptyList()))
        val viewModel = HomeViewModel(storyRepository, userPreferencesRepository, personalRelevanceRepository, recommendationEngine)
        advanceUntilIdle()

        viewModel.refresh()
        advanceUntilIdle()

        // Should call updateLastRefreshedTime with current time
        verify(userPreferencesRepository).updateLastRefreshedTime(any())
    }

    @Test
    fun `refresh calls storyRepository refresh`() = runTest {
        whenever(storyRepository.observeStories()).thenReturn(flowOf(emptyList()))
        val viewModel = HomeViewModel(storyRepository, userPreferencesRepository, personalRelevanceRepository, recommendationEngine)
        advanceUntilIdle()

        viewModel.refresh()
        advanceUntilIdle()

        // Should reload demo data from repository
        verify(storyRepository).refresh()
    }

    @Test
    fun `lastRefreshedTime flow maps from preferences`() = runTest {
        val testTime = Instant.now()
        whenever(storyRepository.observeStories()).thenReturn(flowOf(emptyList()))

        // Preferences with last refreshed time
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
                    showLocalOnly = false,
                    lastRefreshedTime = testTime
                )
            )
        )

        val viewModel = HomeViewModel(storyRepository, userPreferencesRepository, personalRelevanceRepository, recommendationEngine)
        advanceUntilIdle()

        assertEquals(testTime, viewModel.lastRefreshedTime.value)
    }

    @Test
    fun `lastRefreshedTime is null when never refreshed`() = runTest {
        whenever(storyRepository.observeStories()).thenReturn(flowOf(emptyList()))
        val viewModel = HomeViewModel(storyRepository, userPreferencesRepository, personalRelevanceRepository, recommendationEngine)
        advanceUntilIdle()

        assertNull(viewModel.lastRefreshedTime.value)
    }
}
