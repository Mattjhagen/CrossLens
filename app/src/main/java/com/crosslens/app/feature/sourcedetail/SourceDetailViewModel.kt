package com.crosslens.app.feature.sourcedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crosslens.app.core.model.Article
import com.crosslens.app.core.model.Source
import com.crosslens.app.core.model.SourceDigest
import com.crosslens.app.data.repository.SourceRepository
import com.crosslens.app.data.repository.StoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SourceDetailUiState {
    object Loading : SourceDetailUiState()
    data class Success(
        val article: Article,
        val source: Source,
        val digest: SourceDigest?
    ) : SourceDetailUiState()
    data class Error(val message: String) : SourceDetailUiState()
}

@HiltViewModel
class SourceDetailViewModel @Inject constructor(
    private val storyRepository: StoryRepository,
    private val sourceRepository: SourceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SourceDetailUiState>(SourceDetailUiState.Loading)
    val uiState: StateFlow<SourceDetailUiState> = _uiState.asStateFlow()

    fun loadArticle(articleId: String) {
        viewModelScope.launch {
            try {
                val article = storyRepository.getArticle(articleId)
                if (article == null) {
                    _uiState.value = SourceDetailUiState.Error("Article not found")
                    return@launch
                }

                val source = sourceRepository.getSource(article.sourceId)
                if (source == null) {
                    _uiState.value = SourceDetailUiState.Error("Source not found")
                    return@launch
                }

                // Load digest for the story
                val digest = storyRepository.getSourceDigest(article.storyId)

                _uiState.value = SourceDetailUiState.Success(
                    article = article,
                    source = source,
                    digest = digest
                )
            } catch (e: Exception) {
                _uiState.value = SourceDetailUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}
