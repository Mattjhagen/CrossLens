package com.crosslens.app.feature.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crosslens.app.core.model.Story
import com.crosslens.app.data.repository.SourceRepository
import com.crosslens.app.data.repository.StoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val storyRepository: StoryRepository,
    private val sourceRepository: SourceRepository
) : ViewModel() {
    private val _selectedRegions = MutableStateFlow<Set<String>>(emptySet())
    private val _selectedCountries = MutableStateFlow<Set<String>>(emptySet())
    private val _selectedTopics = MutableStateFlow<Set<String>>(emptySet())
    private val _selectedLanguages = MutableStateFlow<Set<String>>(emptySet())
    private val _query = MutableStateFlow("")
    val selectedRegions = _selectedRegions.asStateFlow()
    val selectedCountries = _selectedCountries.asStateFlow()
    val selectedTopics = _selectedTopics.asStateFlow()
    val selectedLanguages = _selectedLanguages.asStateFlow()
    val query = _query.asStateFlow()
    val availableCountries = sourceRepository.observeSources().map { it.flatMap { source -> source.countryCodes }.distinct().sorted() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val availableLanguages = sourceRepository.observeSources().map { it.flatMap { source -> source.defaultLanguages }.distinct().sorted() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    private data class Filters(val regions: Set<String>, val countries: Set<String>, val topics: Set<String>, val languages: Set<String>, val query: String)
    private val filters = combine(_selectedRegions, _selectedCountries, _selectedTopics, _selectedLanguages, _query) { regions, countries, topics, languages, query ->
        Filters(regions, countries, topics, languages, query)
    }
    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    val filteredStories: StateFlow<List<Story>> = combine(
        storyRepository.observeStories(), sourceRepository.observeSources(), filters
    ) { stories, sources, filter ->
        val byId = sources.associateBy { it.id }
        _error.value = null
        stories.filter { story ->
            val articles = if (filter.regions.isNotEmpty() || filter.countries.isNotEmpty() || filter.languages.isNotEmpty())
                storyRepository.getArticlesForStory(story.id) else emptyList()
            // Each dimension matches the sampled reporting, not the event's location or publisher ideology.
            val region = filter.regions.isEmpty() || articles.any { article -> byId[article.sourceId]?.regionIds?.any { it in filter.regions } == true }
            val country = filter.countries.isEmpty() || articles.any { article -> byId[article.sourceId]?.countryCodes?.any { it in filter.countries } == true }
            val language = filter.languages.isEmpty() || articles.any { article -> filter.languages.any { article.originalLanguage.equals(it, ignoreCase = true) } }
            val topic = filter.topics.isEmpty() || story.topicIds.any { it in filter.topics }
            val search = filter.query.isBlank() || story.title.contains(filter.query, true) || story.summary.contains(filter.query, true)
            region && country && language && topic && search
        }
    }.catch { _error.value = "Unable to load cached stories"; emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleRegion(value: String) = _selectedRegions.update { if (value in it) it - value else it + value }
    fun toggleCountry(value: String) = _selectedCountries.update { if (value in it) it - value else it + value }
    fun toggleTopic(value: String) = _selectedTopics.update { if (value in it) it - value else it + value }
    fun toggleLanguage(value: String) = _selectedLanguages.update { if (value in it) it - value else it + value }
    fun updateQuery(value: String) { _query.value = value }
    fun clearFilters() {
        _selectedRegions.value = emptySet()
        _selectedCountries.value = emptySet()
        _selectedTopics.value = emptySet()
        _selectedLanguages.value = emptySet()
        _query.value = ""
    }
}
