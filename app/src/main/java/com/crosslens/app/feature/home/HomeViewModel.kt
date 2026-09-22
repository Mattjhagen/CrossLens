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
    private val storyRepository: StoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadStories()
    }

    private fun loadStories() {
        viewModelScope.launch {
            storyRepository.observeStories()
                .catch { error ->
                    _uiState.value = HomeUiState.Error(error.message ?: "Unknown error")
                }
                .collect { stories ->
                    _uiState.value = if (stories.isEmpty()) {
                        HomeUiState.Empty
                    } else {
                        HomeUiState.Success(stories)
                    }
                }
        }
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
    data class Success(val stories: List<Story>) : HomeUiState
    data class Error(val message: String) : HomeUiState
}
