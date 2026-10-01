package com.crosslens.app.feature.eventcomparison

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crosslens.app.core.model.Article
import com.crosslens.app.core.model.Source
import com.crosslens.app.core.model.Story
import com.crosslens.app.data.ingestion.SourceMetadata
import com.crosslens.app.data.ingestion.SourceMetadataRegistry
import com.crosslens.app.data.repository.StoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for event comparison screen.
 * Loads story, articles, and source metadata for side-by-side comparison.
 */
@HiltViewModel
class EventComparisonViewModel @Inject constructor(
    private val repository: StoryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val storyId: String = checkNotNull(savedStateHandle["storyId"])
    private val recommender = ReadAcrossCoverageRecommender()

    private val _uiState = MutableStateFlow<EventComparisonUiState>(EventComparisonUiState.Loading)
    val uiState: StateFlow<EventComparisonUiState> = _uiState.asStateFlow()

    private val _showReadAcrossSheet = MutableStateFlow(false)
    val showReadAcrossSheet: StateFlow<Boolean> = _showReadAcrossSheet.asStateFlow()

    init {
        loadEvent()
    }

    fun toggleReadAcrossCoverage() {
        _showReadAcrossSheet.value = !_showReadAcrossSheet.value
    }

    fun dismissReadAcrossCoverage() {
        _showReadAcrossSheet.value = false
    }

    private fun loadEvent() {
        viewModelScope.launch {
            try {
                val story = repository.getStory(storyId)
                if (story == null) {
                    _uiState.value = EventComparisonUiState.NotFound
                    return@launch
                }

                val articles = repository.getArticlesForStory(storyId)
                if (articles.isEmpty()) {
                    _uiState.value = EventComparisonUiState.Error("No articles found for this event")
                    return@launch
                }

                // Load source metadata for each article
                val articleWithMetadata = articles.map { article ->
                    val sourceId = article.sourceId.removePrefix("live_")
                    val metadata = SourceMetadataRegistry.getMetadata(sourceId)
                    ArticleWithMetadata(
                        article = article,
                        metadata = metadata
                    )
                }

                // Generate read-across recommendations
                // Pass only the first article as "already shown" since comparison screen
                // displays all articles initially. Recommender will find additional perspectives.
                val recommendations = recommender.recommend(
                    allArticles = articles,
                    alreadyShownArticles = articles.take(1), // Only first article is "primary"
                    articlesWithMetadata = articleWithMetadata
                )

                _uiState.value = EventComparisonUiState.Success(
                    story = story,
                    articles = articleWithMetadata,
                    recommendations = recommendations
                )
            } catch (e: Exception) {
                _uiState.value = EventComparisonUiState.Error(
                    e.message ?: "Failed to load event comparison"
                )
            }
        }
    }
}

/**
 * UI state for event comparison screen.
 */
sealed interface EventComparisonUiState {
    data object Loading : EventComparisonUiState
    data object NotFound : EventComparisonUiState
    data class Error(val message: String) : EventComparisonUiState
    data class Success(
        val story: Story,
        val articles: List<ArticleWithMetadata>,
        val recommendations: List<RecommendedArticle> = emptyList()
    ) : EventComparisonUiState
}

/**
 * Article with its source metadata for display.
 */
data class ArticleWithMetadata(
    val article: Article,
    val metadata: SourceMetadata?
)
