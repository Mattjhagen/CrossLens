package com.crosslens.app.feature.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crosslens.app.core.model.Story
import com.crosslens.app.data.repository.StoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val storyRepository: StoryRepository
) : ViewModel() {

    private val _selectedRegions = MutableStateFlow<Set<String>>(emptySet())
    private val _selectedCountries = MutableStateFlow<Set<String>>(emptySet())
    private val _selectedTopics = MutableStateFlow<Set<String>>(emptySet())

    val selectedRegions: StateFlow<Set<String>> = _selectedRegions.asStateFlow()
    val selectedCountries: StateFlow<Set<String>> = _selectedCountries.asStateFlow()
    val selectedTopics: StateFlow<Set<String>> = _selectedTopics.asStateFlow()

    val filteredStories: StateFlow<List<Story>> = combine(
        storyRepository.observeStories(),
        _selectedRegions,
        _selectedCountries,
        _selectedTopics
    ) { stories, regions, countries, topics ->
        stories.filter { story ->
            val matchesRegion = regions.isEmpty() || regions.any { story.eventCountryCodes.contains(it) }
            val matchesCountry = countries.isEmpty() || countries.any { story.eventCountryCodes.contains(it) }
            val matchesTopic = topics.isEmpty() || topics.any { story.topicIds.contains(it) }
            matchesRegion && matchesCountry && matchesTopic
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleRegion(region: String) {
        _selectedRegions.update { current ->
            if (current.contains(region)) current - region
            else current + region
        }
    }

    fun toggleCountry(country: String) {
        _selectedCountries.update { current ->
            if (current.contains(country)) current - country
            else current + country
        }
    }

    fun toggleTopic(topic: String) {
        _selectedTopics.update { current ->
            if (current.contains(topic)) current - topic
            else current + topic
        }
    }

    fun clearFilters() {
        _selectedRegions.value = emptySet()
        _selectedCountries.value = emptySet()
        _selectedTopics.value = emptySet()
    }
}
