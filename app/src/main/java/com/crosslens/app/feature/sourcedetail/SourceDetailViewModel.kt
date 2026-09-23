package com.crosslens.app.feature.sourcedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crosslens.app.core.model.*
import com.crosslens.app.data.preferences.PersonalRelevanceRepository
import com.crosslens.app.data.repository.SourceRepository
import com.crosslens.app.data.repository.StoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SourceDetailUiState {
    object Loading : SourceDetailUiState()
    data class Success(
        val article: Article,
        val source: Source,
        val digest: SourceDigest?,
        val story: Story,
        val existingPreferences: List<PersonalRelevancePreference>
    ) : SourceDetailUiState()
    data class Error(val message: String) : SourceDetailUiState()
}

@HiltViewModel
class SourceDetailViewModel @Inject constructor(
    private val storyRepository: StoryRepository,
    private val sourceRepository: SourceRepository,
    private val personalRelevanceRepository: PersonalRelevanceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SourceDetailUiState>(SourceDetailUiState.Loading)
    val uiState: StateFlow<SourceDetailUiState> = _uiState.asStateFlow()

    private var currentArticleContext: ArticleRelevanceContext? = null

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

                val story = storyRepository.getStory(article.storyId)
                if (story == null) {
                    _uiState.value = SourceDetailUiState.Error("Story not found")
                    return@launch
                }

                // Load digest for the story
                val digest = storyRepository.getSourceDigest(article.storyId)

                // Use source's regionIds directly
                val sourceRegionIds = source.regionIds

                // Store context for feedback actions
                currentArticleContext = ArticleRelevanceContext(
                    topicIds = story.topicIds,
                    regionIds = sourceRegionIds
                )

                // Collect current preferences
                personalRelevanceRepository.preferencesFlow.collect { preferences ->
                    val relevantPreferences = preferences.filter { pref ->
                        when (pref.dimensionType) {
                            DimensionType.TOPIC -> pref.dimensionValue in story.topicIds
                            DimensionType.REGION -> pref.dimensionValue in sourceRegionIds
                        }
                    }

                    _uiState.value = SourceDetailUiState.Success(
                        article = article,
                        source = source,
                        digest = digest,
                        story = story,
                        existingPreferences = relevantPreferences
                    )
                }
            } catch (e: Exception) {
                _uiState.value = SourceDetailUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun onShowMoreLikeThis() {
        viewModelScope.launch {
            val context = currentArticleContext ?: return@launch

            // Add preference for each topic
            context.topicIds.forEach { topicId ->
                personalRelevanceRepository.addPreference(
                    preferenceType = PreferenceType.MORE,
                    dimensionType = DimensionType.TOPIC,
                    dimensionValue = topicId
                )
            }

            // Add preference for each region
            context.regionIds.forEach { regionId ->
                personalRelevanceRepository.addPreference(
                    preferenceType = PreferenceType.MORE,
                    dimensionType = DimensionType.REGION,
                    dimensionValue = regionId
                )
            }
        }
    }

    fun onShowLessLikeThis() {
        viewModelScope.launch {
            val context = currentArticleContext ?: return@launch

            // Add preference for each topic
            context.topicIds.forEach { topicId ->
                personalRelevanceRepository.addPreference(
                    preferenceType = PreferenceType.LESS,
                    dimensionType = DimensionType.TOPIC,
                    dimensionValue = topicId
                )
            }

            // Add preference for each region
            context.regionIds.forEach { regionId ->
                personalRelevanceRepository.addPreference(
                    preferenceType = PreferenceType.LESS,
                    dimensionType = DimensionType.REGION,
                    dimensionValue = regionId
                )
            }
        }
    }
}
