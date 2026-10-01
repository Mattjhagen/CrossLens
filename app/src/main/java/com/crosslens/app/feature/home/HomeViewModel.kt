package com.crosslens.app.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crosslens.app.core.model.DemoLocalLocations
import com.crosslens.app.core.model.FeedMetadata
import com.crosslens.app.core.model.LocalLocation
import com.crosslens.app.core.model.PersonalizedRecommendation
import com.crosslens.app.core.model.Story
import com.crosslens.app.data.recommendation.PersonalizedRecommendationEngine
import com.crosslens.app.data.repository.LiveStoryRepository
import com.crosslens.app.data.repository.StoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val storyRepository: StoryRepository,
    private val userPreferencesRepository: com.crosslens.app.data.preferences.UserPreferencesRepository,
    private val personalRelevanceRepository: com.crosslens.app.data.preferences.PersonalRelevanceRepository,
    private val recommendationEngine: PersonalizedRecommendationEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _showLocalOnly = MutableStateFlow(false)
    val showLocalOnly: StateFlow<Boolean> = _showLocalOnly.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    val currentLocation: StateFlow<LocalLocation?> = userPreferencesRepository.preferencesFlow
        .map { prefs -> prefs.demoLocalLocation?.let { DemoLocalLocations.findById(it) } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val lastRefreshedTime: StateFlow<Instant?> = userPreferencesRepository.preferencesFlow
        .map { prefs -> prefs.lastRefreshedTime }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val showForYou: StateFlow<Boolean> = userPreferencesRepository.preferencesFlow
        .map { prefs -> prefs.showForYou }
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    private val _forYouRecommendations = MutableStateFlow<List<PersonalizedRecommendation>>(emptyList())
    val forYouRecommendations: StateFlow<List<PersonalizedRecommendation>> = _forYouRecommendations.asStateFlow()

    private val _forYouEligible = MutableStateFlow(false)
    val forYouEligible: StateFlow<Boolean> = _forYouEligible.asStateFlow()

    private val _feedMetadata = MutableStateFlow<FeedMetadata?>(null)
    val feedMetadata: StateFlow<FeedMetadata?> = _feedMetadata.asStateFlow()

    init {
        // Initialize filter state from preferences
        viewModelScope.launch {
            userPreferencesRepository.preferencesFlow.collect { prefs ->
                _showLocalOnly.value = prefs.showLocalOnly
            }
        }
        loadStories()
        loadFeedMetadata()
    }

    private fun loadFeedMetadata() {
        viewModelScope.launch {
            if (storyRepository is LiveStoryRepository) {
                // Poll metadata every 30 seconds for freshness indicator
                while (true) {
                    _feedMetadata.value = storyRepository.getFeedMetadata()
                    kotlinx.coroutines.delay(30_000)
                }
            }
        }
    }

    private fun loadStories() {
        viewModelScope.launch {
            combine(
                storyRepository.observeStories(),
                userPreferencesRepository.preferencesFlow,
                personalRelevanceRepository.preferencesFlow,
                _showLocalOnly
            ) { allStories, userPrefs, personalPrefs, localOnly ->
                // Determine which stories to show in main feed
                val feedStories = when {
                    localOnly && userPrefs.demoLocalLocation != null -> {
                        // Show only local stories for selected location
                        storyRepository.observeLocalStories(userPrefs.demoLocalLocation).first()
                    }
                    localOnly && userPrefs.demoLocalLocation == null -> {
                        // Filter is on but no location selected - show empty
                        emptyList()
                    }
                    else -> {
                        // Filter is off - show all stories
                        allStories
                    }
                }

                // Generate For You recommendations
                val recommendationResult = recommendationEngine.generateRecommendations(
                    allStories = allStories,
                    preferences = personalPrefs,
                    demoLocalLocation = userPrefs.demoLocalLocation
                )

                _forYouEligible.value = recommendationResult.isEligible
                _forYouRecommendations.value = recommendationResult.recommendations

                feedStories
            }
                .catch { error ->
                    _uiState.value = HomeUiState.Error(error.message ?: "Unknown error")
                }
                .collect { stories ->
                    _uiState.value = when {
                        stories.isEmpty() && _showLocalOnly.value -> HomeUiState.EmptyLocal
                        stories.isEmpty() -> HomeUiState.Empty
                        else -> HomeUiState.Success(stories)
                    }
                }
        }
    }

    fun toggleLocalFilter() {
        viewModelScope.launch {
            val newValue = !_showLocalOnly.value
            userPreferencesRepository.updateShowLocalOnly(newValue)
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                // Reload data (live or demo fallback)
                storyRepository.refresh()
                // Update the timestamp to now
                userPreferencesRepository.updateLastRefreshedTime(Instant.now())
                // Immediately update feed metadata after refresh
                if (storyRepository is LiveStoryRepository) {
                    _feedMetadata.value = storyRepository.getFeedMetadata()
                }
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data object Empty : HomeUiState
    data object EmptyLocal : HomeUiState
    data class Success(val stories: List<Story>) : HomeUiState
    data class Error(val message: String) : HomeUiState
}
