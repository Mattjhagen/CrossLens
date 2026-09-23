package com.crosslens.app

import com.crosslens.app.core.model.Theme
import com.crosslens.app.core.model.TranslationPreference
import com.crosslens.app.core.model.UserPreferences
import com.crosslens.app.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var userPreferencesRepository: UserPreferencesRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        userPreferencesRepository = mock()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `userPreferences flow emits theme SYSTEM`() = runTest {
        val prefs = createTestPreferences(Theme.SYSTEM)
        whenever(userPreferencesRepository.preferencesFlow).thenReturn(flowOf(prefs))

        val viewModel = MainViewModel(userPreferencesRepository)
        advanceUntilIdle()

        assertEquals(Theme.SYSTEM, viewModel.userPreferences.value?.theme)
    }

    @Test
    fun `userPreferences flow emits theme LIGHT`() = runTest {
        val prefs = createTestPreferences(Theme.LIGHT)
        whenever(userPreferencesRepository.preferencesFlow).thenReturn(flowOf(prefs))

        val viewModel = MainViewModel(userPreferencesRepository)
        advanceUntilIdle()

        assertEquals(Theme.LIGHT, viewModel.userPreferences.value?.theme)
    }

    @Test
    fun `userPreferences flow emits theme DARK`() = runTest {
        val prefs = createTestPreferences(Theme.DARK)
        whenever(userPreferencesRepository.preferencesFlow).thenReturn(flowOf(prefs))

        val viewModel = MainViewModel(userPreferencesRepository)
        advanceUntilIdle()

        assertEquals(Theme.DARK, viewModel.userPreferences.value?.theme)
    }

    @Test
    fun `userPreferences flow updates when theme changes`() = runTest {
        val initialPrefs = createTestPreferences(Theme.SYSTEM)
        val updatedPrefs = createTestPreferences(Theme.DARK)
        whenever(userPreferencesRepository.preferencesFlow)
            .thenReturn(flowOf(initialPrefs, updatedPrefs))

        val viewModel = MainViewModel(userPreferencesRepository)
        advanceUntilIdle()

        assertEquals(Theme.DARK, viewModel.userPreferences.value?.theme)
    }

    private fun createTestPreferences(theme: Theme) = UserPreferences(
        readingLanguage = "en",
        homeCountry = "US",
        homeRegion = null,
        enabledSourceIds = emptySet(),
        translationPreference = TranslationPreference.AUTO,
        theme = theme,
        reducedMotion = false
    )
}
