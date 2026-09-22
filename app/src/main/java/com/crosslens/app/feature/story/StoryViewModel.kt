package com.crosslens.app.feature.story

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crosslens.app.core.model.*
import com.crosslens.app.data.preferences.ReadingStateRepository
import com.crosslens.app.data.repository.StoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StoryViewModel @Inject constructor(
    private val storyRepository: StoryRepository,
    private val readingStateRepository: ReadingStateRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val storyId: String = checkNotNull(savedStateHandle["storyId"])

    private val _uiState = MutableStateFlow<StoryUiState>(StoryUiState.Loading)
    val uiState: StateFlow<StoryUiState> = _uiState.asStateFlow()

    val readingState = readingStateRepository.readingStateFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReadingState(emptySet(), null))

    init {
        loadStory()
        updateLastOpened()
    }

    private fun loadStory() {
        viewModelScope.launch {
            try {
                val story = storyRepository.getStory(storyId)
                if (story == null) {
                    _uiState.value = StoryUiState.NotFound
                    return@launch
                }

                val articles = storyRepository.getArticlesForStory(storyId)
                val claims = storyRepository.getClaimsForStory(storyId)
                val observations = storyRepository.getFrameObservationsForStory(storyId)

                _uiState.value = StoryUiState.Success(
                    story = story,
                    articles = articles,
                    claims = claims,
                    frameObservations = observations
                )
            } catch (e: Exception) {
                _uiState.value = StoryUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun updateLastOpened() {
        viewModelScope.launch {
            readingStateRepository.updateLastOpened(storyId)
        }
    }

    fun toggleSave() {
        viewModelScope.launch {
            val currentState = readingState.value
            if (currentState.savedStoryIds.contains(storyId)) {
                readingStateRepository.unsaveStory(storyId)
            } else {
                readingStateRepository.saveStory(storyId)
            }
        }
    }
}

sealed interface StoryUiState {
    data object Loading : StoryUiState
    data object NotFound : StoryUiState
    data class Success(
        val story: Story,
        val articles: List<Article>,
        val claims: List<Claim>,
        val frameObservations: List<FrameObservation>
    ) : StoryUiState
    data class Error(val message: String) : StoryUiState
}
