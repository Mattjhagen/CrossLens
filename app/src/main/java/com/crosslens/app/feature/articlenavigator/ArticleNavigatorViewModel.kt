package com.crosslens.app.feature.articlenavigator

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crosslens.app.core.model.Article
import com.crosslens.app.core.model.Story
import com.crosslens.app.data.ingestion.SourceMetadata
import com.crosslens.app.data.ingestion.SourceMetadataRegistry
import com.crosslens.app.data.repository.StoryRepository
import com.crosslens.app.feature.eventcomparison.ArticleWithMetadata
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for full-screen article navigator with Flipboard-inspired swipe navigation.
 *
 * Navigation model:
 * - Horizontal swipe: navigate across publishers in same confident event cluster (2+ sources)
 * - Vertical swipe: navigate between stories in main feed sequence
 * - Top tap: return to first article in feed and refresh
 *
 * State tracking:
 * - Current feed position (which story in the feed)
 * - Current article position within cluster (which publisher)
 * - Available navigation directions (has prev/next)
 */
@HiltViewModel
class ArticleNavigatorViewModel @Inject constructor(
    private val repository: StoryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val initialStoryId: String = checkNotNull(savedStateHandle["storyId"])
    private val initialArticleId: String? = savedStateHandle["articleId"]

    private val _uiState = MutableStateFlow<ArticleNavigatorUiState>(ArticleNavigatorUiState.Loading)
    val uiState: StateFlow<ArticleNavigatorUiState> = _uiState.asStateFlow()

    private val _navigationState = MutableStateFlow<NavigationState?>(null)
    val navigationState: StateFlow<NavigationState?> = _navigationState.asStateFlow()

    private var feedStories: List<Story> = emptyList()
    private var currentFeedIndex: Int = 0
    private var currentClusterArticles: List<ArticleWithMetadata> = emptyList()
    private var currentArticleIndex: Int = 0

    init {
        loadNavigator()
    }

    private fun loadNavigator() {
        viewModelScope.launch {
            try {
                // Load all feed stories from the flow
                repository.observeStories().collect { stories ->
                    feedStories = stories

                    // Find initial story position in feed
                    currentFeedIndex = feedStories.indexOfFirst { it.id == initialStoryId }
                    if (currentFeedIndex == -1) {
                        _uiState.value = ArticleNavigatorUiState.Error("Story not found in feed")
                        return@collect
                    }

                    // Load initial story and articles
                    loadCurrentStoryAndArticles()

                    // Stop collecting after first emission for initialization
                    return@collect
                }
            } catch (e: Exception) {
                _uiState.value = ArticleNavigatorUiState.Error(
                    e.message ?: "Failed to load article navigator"
                )
            }
        }
    }

    private suspend fun loadCurrentStoryAndArticles() {
        val story = feedStories.getOrNull(currentFeedIndex)
        if (story == null) {
            _uiState.value = ArticleNavigatorUiState.Error("Invalid feed position")
            return
        }

        val articles = repository.getArticlesForStory(story.id)
        if (articles.isEmpty()) {
            _uiState.value = ArticleNavigatorUiState.Error("No articles found")
            return
        }

        // Load source metadata for each article
        currentClusterArticles = articles.map { article ->
            val sourceId = article.sourceId.removePrefix("live_")
            val metadata = SourceMetadataRegistry.getMetadata(sourceId)
            ArticleWithMetadata(article = article, metadata = metadata)
        }

        // Determine initial article position in cluster
        if (initialArticleId != null) {
            val index = currentClusterArticles.indexOfFirst { it.article.id == initialArticleId }
            if (index != -1) {
                currentArticleIndex = index
            }
        }

        updateUiState()
    }

    private fun updateUiState() {
        val story = feedStories.getOrNull(currentFeedIndex)
        val articleWithMetadata = currentClusterArticles.getOrNull(currentArticleIndex)

        if (story == null || articleWithMetadata == null) {
            _uiState.value = ArticleNavigatorUiState.Error("Failed to load current article")
            return
        }

        // Check if horizontal navigation is available (only for confident clusters with 2+ sources)
        val hasHorizontalNavigation = story.isEventCluster && currentClusterArticles.size >= 2

        // Determine distinct publisher count for eligibility
        val distinctPublishers = currentClusterArticles
            .mapNotNull { it.metadata?.publisherName }
            .distinct()
            .size

        _navigationState.value = NavigationState(
            currentFeedPosition = currentFeedIndex,
            totalFeedItems = feedStories.size,
            currentClusterPosition = currentArticleIndex,
            totalClusterItems = currentClusterArticles.size,
            hasHorizontalNavigation = hasHorizontalNavigation && distinctPublishers >= 2,
            hasPreviousInCluster = hasHorizontalNavigation && currentArticleIndex > 0,
            hasNextInCluster = hasHorizontalNavigation && currentArticleIndex < currentClusterArticles.size - 1,
            hasPreviousInFeed = currentFeedIndex > 0,
            hasNextInFeed = currentFeedIndex < feedStories.size - 1
        )

        _uiState.value = ArticleNavigatorUiState.Success(
            story = story,
            article = articleWithMetadata.article,
            metadata = articleWithMetadata.metadata,
            allClusterArticles = currentClusterArticles
        )
    }

    /**
     * Navigate to previous article in cluster (swipe right).
     */
    fun navigateToPreviousInCluster() {
        if (currentArticleIndex > 0) {
            currentArticleIndex--
            updateUiState()
        }
    }

    /**
     * Navigate to next article in cluster (swipe left).
     */
    fun navigateToNextInCluster() {
        if (currentArticleIndex < currentClusterArticles.size - 1) {
            currentArticleIndex++
            updateUiState()
        }
    }

    /**
     * Navigate to previous story in feed (swipe down).
     */
    fun navigateToPreviousInFeed() {
        viewModelScope.launch {
            if (currentFeedIndex > 0) {
                currentFeedIndex--
                currentArticleIndex = 0 // Reset to first article in new story
                loadCurrentStoryAndArticles()
            }
        }
    }

    /**
     * Navigate to next story in feed (swipe up).
     */
    fun navigateToNextInFeed() {
        viewModelScope.launch {
            if (currentFeedIndex < feedStories.size - 1) {
                currentFeedIndex++
                currentArticleIndex = 0 // Reset to first article in new story
                loadCurrentStoryAndArticles()
            }
        }
    }

    /**
     * Return to first article in feed and refresh.
     */
    fun returnToFirstAndRefresh() {
        viewModelScope.launch {
            _uiState.value = ArticleNavigatorUiState.Loading
            try {
                repository.refresh()
                currentFeedIndex = 0
                currentArticleIndex = 0
                loadNavigator()
            } catch (e: Exception) {
                _uiState.value = ArticleNavigatorUiState.Error(
                    e.message ?: "Failed to refresh feed"
                )
            }
        }
    }
}

/**
 * UI state for article navigator screen.
 */
sealed class ArticleNavigatorUiState {
    data object Loading : ArticleNavigatorUiState()
    data class Success(
        val story: Story,
        val article: Article,
        val metadata: SourceMetadata?,
        val allClusterArticles: List<ArticleWithMetadata>
    ) : ArticleNavigatorUiState()
    data class Error(val message: String) : ArticleNavigatorUiState()
}

/**
 * Navigation state tracking current position and available directions.
 */
data class NavigationState(
    val currentFeedPosition: Int,
    val totalFeedItems: Int,
    val currentClusterPosition: Int,
    val totalClusterItems: Int,
    val hasHorizontalNavigation: Boolean,
    val hasPreviousInCluster: Boolean,
    val hasNextInCluster: Boolean,
    val hasPreviousInFeed: Boolean,
    val hasNextInFeed: Boolean
)
