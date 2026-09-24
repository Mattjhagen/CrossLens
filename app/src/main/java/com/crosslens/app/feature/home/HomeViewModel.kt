package com.crosslens.app.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crosslens.app.core.model.Story
import com.crosslens.app.data.repository.StoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val storyRepository: StoryRepository,
    private val userPreferencesRepository: com.crosslens.app.data.preferences.UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _showLocalOnly = MutableStateFlow(false)
    val showLocalOnly: StateFlow<Boolean> = _showLocalOnly.asStateFlow()

    init {
        loadStories()
    }

    private fun loadStories() {
        viewModelScope.launch {
            combine(
                storyRepository.observeStories(),
                userPreferencesRepository.preferencesFlow,
                _showLocalOnly
            ) { allStories, preferences, localOnly ->
                if (localOnly && preferences.demoLocalLocation != null) {
                    // Show only local stories for selected location
                    storyRepository.observeLocalStories(preferences.demoLocalLocation).first()
                } else {
                    allStories
                }
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
        _showLocalOnly.value = !_showLocalOnly.value
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            storyRepository.refresh()
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
