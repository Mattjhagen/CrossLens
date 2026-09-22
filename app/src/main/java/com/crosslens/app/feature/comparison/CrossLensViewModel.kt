package com.crosslens.app.feature.comparison

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crosslens.app.core.model.*
import com.crosslens.app.data.preferences.EntitlementRepository
import com.crosslens.app.data.preferences.PlusFeature
import com.crosslens.app.data.preferences.UserPreferencesRepository
import com.crosslens.app.data.repository.SourceRepository
import com.crosslens.app.data.repository.StoryRepository
import com.crosslens.app.data.repository.TranslationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CrossLensViewModel @Inject constructor(
    private val storyRepository: StoryRepository,
    private val sourceRepository: SourceRepository,
    private val translationRepository: TranslationRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val entitlementRepository: EntitlementRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val storyId: String = checkNotNull(savedStateHandle["storyId"])

    private val _uiState = MutableStateFlow<CrossLensUiState>(CrossLensUiState.Loading)
    val uiState: StateFlow<CrossLensUiState> = _uiState.asStateFlow()

    private val _selectedArticleIndex = MutableStateFlow(0)
    val selectedArticleIndex: StateFlow<Int> = _selectedArticleIndex.asStateFlow()

    val hasAllSourcesAccess = entitlementRepository.hasAccess(PlusFeature.ALL_SOURCES)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val userPreferences = userPreferencesRepository.preferencesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000),
            UserPreferences("en", null, null, emptySet(), TranslationPreference.AUTO, Theme.SYSTEM, false))

    init {
        loadComparison()
    }

    private fun loadComparison() {
        viewModelScope.launch {
            try {
                val story = storyRepository.getStory(storyId)
                if (story == null) {
                    _uiState.value = CrossLensUiState.NotFound
                    return@launch
                }

                val articles = storyRepository.getArticlesForStory(storyId)
                val sources = sourceRepository.getSourcesByIds(articles.map { it.sourceId })
                val observations = storyRepository.getFrameObservationsForStory(storyId)

                if (articles.size < 2) {
                    _uiState.value = CrossLensUiState.InsufficientSources
                    return@launch
                }

                val articlesWithSources = articles.map { article ->
                    val source = sources.find { it.id == article.sourceId }
                    ArticleWithSource(article, source)
                }

                _uiState.value = CrossLensUiState.Success(
                    story = story,
                    articlesWithSources = articlesWithSources,
                    frameObservations = observations
                )
            } catch (e: Exception) {
                _uiState.value = CrossLensUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun selectArticle(index: Int) {
        _selectedArticleIndex.value = index
    }

    fun previousArticle() {
        val current = _selectedArticleIndex.value
        if (current > 0) {
            _selectedArticleIndex.value = current - 1
        }
    }

    fun nextArticle() {
        val state = _uiState.value
        if (state is CrossLensUiState.Success) {
            val current = _selectedArticleIndex.value
            val maxIndex = state.articlesWithSources.size - 1
            if (current < maxIndex) {
                _selectedArticleIndex.value = current + 1
            }
        }
    }

    suspend fun getTranslation(articleId: String, targetLanguage: String): Translation? {
        return translationRepository.getTranslation(articleId, targetLanguage)
    }
}

data class ArticleWithSource(
    val article: Article,
    val source: Source?
)

sealed interface CrossLensUiState {
    data object Loading : CrossLensUiState
    data object NotFound : CrossLensUiState
    data object InsufficientSources : CrossLensUiState
    data class Success(
        val story: Story,
        val articlesWithSources: List<ArticleWithSource>,
        val frameObservations: List<FrameObservation>
    ) : CrossLensUiState
    data class Error(val message: String) : CrossLensUiState
}
