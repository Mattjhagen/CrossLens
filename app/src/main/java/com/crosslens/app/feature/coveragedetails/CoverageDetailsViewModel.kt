package com.crosslens.app.feature.coveragedetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crosslens.app.data.ingestion.SourceMetadataRegistry
import com.crosslens.app.data.repository.SourceHealthRepository
import com.crosslens.app.data.repository.SourceHealthStatus
import com.crosslens.app.data.repository.StoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject

/**
 * ViewModel for coverage details screen.
 * Loads story and article metadata, maps to coverage details model.
 * Includes technical source health limitations when applicable.
 */
@HiltViewModel
class CoverageDetailsViewModel @Inject constructor(
    private val repository: StoryRepository,
    private val healthRepository: SourceHealthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val storyId: String = checkNotNull(savedStateHandle["storyId"])

    private val _uiState = MutableStateFlow<CoverageDetailsUiState>(CoverageDetailsUiState.Loading)
    val uiState: StateFlow<CoverageDetailsUiState> = _uiState.asStateFlow()

    init {
        loadCoverageDetails()
    }

    private fun loadCoverageDetails() {
        viewModelScope.launch {
            try {
                val story = repository.getStory(storyId)
                if (story == null) {
                    _uiState.value = CoverageDetailsUiState.NotFound
                    return@launch
                }

                val articles = repository.getArticlesForStory(storyId)
                if (articles.isEmpty()) {
                    _uiState.value = CoverageDetailsUiState.Error("No articles found")
                    return@launch
                }

                // Map articles to coverage sources with documented metadata
                val sources = articles.map { article ->
                    val sourceId = article.sourceId.removePrefix("live_")
                    val metadata = SourceMetadataRegistry.getMetadata(sourceId)

                    CoverageSource(
                        publisherName = metadata?.publisherName ?: article.attribution,
                        country = metadata?.country,
                        language = metadata?.primaryLanguage,
                        languageCode = metadata?.languageCode,
                        publishedAt = article.publishedTime,
                        editorialDescription = metadata?.editorialDescription,
                        descriptionProvenance = metadata?.descriptionProvenance,
                        headline = article.originalHeadline
                    )
                }.sortedBy { it.publishedAt } // Oldest first

                // Calculate distinct counts
                val distinctPublishers = sources.map { it.publisherName }.distinct().size
                val distinctLanguages = sources.mapNotNull { it.language }.distinct()
                val distinctCountries = sources.mapNotNull { it.country }.distinct()

                // Temporal coverage
                val earliest = sources.minOf { it.publishedAt }
                val latest = sources.maxOf { it.publishedAt }
                val now = Instant.now()
                val temporalCoverage = TemporalCoverage(
                    earliest = earliest,
                    latest = latest,
                    freshnessDescription = formatFreshness(latest, now),
                    spanDescription = formatSpan(earliest, latest)
                )

                // Detect coverage limitations
                val limitations = mutableListOf<CoverageLimitation>()

                // Source diversity limitations
                when {
                    distinctPublishers == 1 -> {
                        limitations.add(
                            CoverageLimitation(
                                type = LimitationType.SOURCE_DIVERSITY,
                                description = "Single publisher - coverage may be incomplete"
                            )
                        )
                    }
                    distinctPublishers == 2 -> {
                        limitations.add(
                            CoverageLimitation(
                                type = LimitationType.SOURCE_DIVERSITY,
                                description = "This event currently includes reporting from 2 publishers"
                            )
                        )
                    }
                    distinctPublishers <= 3 -> {
                        limitations.add(
                            CoverageLimitation(
                                type = LimitationType.SOURCE_DIVERSITY,
                                description = "This event currently includes reporting from $distinctPublishers publishers"
                            )
                        )
                    }
                }

                // Language diversity limitations
                if (distinctLanguages.size == 1) {
                    limitations.add(
                        CoverageLimitation(
                            type = LimitationType.LANGUAGE_DIVERSITY,
                            description = if (distinctPublishers == 1) {
                                "One language represented"
                            } else {
                                "$distinctPublishers publishers; one language represented"
                            }
                        )
                    )
                }

                // Geographic diversity limitations
                if (distinctPublishers > 1 && distinctCountries.size == 1) {
                    limitations.add(
                        CoverageLimitation(
                            type = LimitationType.GEOGRAPHIC_DIVERSITY,
                            description = "All sources from ${distinctCountries.first()}"
                        )
                    )
                }

                // Temporal limitations (if all coverage is very recent)
                val hoursSinceEarliest = ChronoUnit.HOURS.between(earliest, now)
                if (hoursSinceEarliest < 6) {
                    limitations.add(
                        CoverageLimitation(
                            type = LimitationType.TEMPORAL_SPAN,
                            description = "Coverage from last ${hoursSinceEarliest} hours only"
                        )
                    )
                }

                // Check for degraded/disabled sources affecting this story
                val healthLimitations = checkSourceHealthLimitations(sources)
                limitations.addAll(healthLimitations)

                // General completeness warning
                limitations.add(
                    CoverageLimitation(
                        type = LimitationType.GENERAL,
                        description = "No additional qualifying coverage is currently available"
                    )
                )

                // Clustering rationale
                val clusteringRationale = if (story.isEventCluster) {
                    buildClusteringExplanation(distinctPublishers, distinctLanguages, distinctCountries)
                } else {
                    "Single-source article (not part of an event cluster)"
                }

                val coverageDetails = CoverageDetails(
                    eventTitle = story.title,
                    distinctPublisherCount = distinctPublishers,
                    sources = sources,
                    temporalCoverage = temporalCoverage,
                    clusteringRationale = clusteringRationale,
                    limitations = limitations
                )

                _uiState.value = CoverageDetailsUiState.Success(coverageDetails)
            } catch (e: Exception) {
                _uiState.value = CoverageDetailsUiState.Error(
                    e.message ?: "Failed to load coverage details"
                )
            }
        }
    }

    private fun buildClusteringExplanation(
        publisherCount: Int,
        languages: List<String>,
        countries: List<String>
    ): String {
        val parts = mutableListOf<String>()

        parts.add("$publisherCount distinct ${if (publisherCount == 1) "publisher" else "publishers"}")

        if (languages.size > 1) {
            parts.add("${languages.size} languages (${languages.joinToString(", ")})")
        }

        if (countries.size > 1) {
            parts.add("reporting from ${countries.size} countries")
        }

        val diversityDescription = parts.joinToString(", ")

        return "These articles were grouped because they report on the same specific event. " +
                "The cluster includes $diversityDescription with overlapping headlines, " +
                "shared named entities, and close publication times."
    }

    private fun formatFreshness(timestamp: Instant, now: Instant): String {
        val hours = ChronoUnit.HOURS.between(timestamp, now)
        val minutes = ChronoUnit.MINUTES.between(timestamp, now)

        return when {
            minutes < 60 -> "Last update ${minutes}m ago"
            hours < 24 -> "Last update ${hours}h ago"
            hours < 48 -> "Last update 1 day ago"
            else -> {
                val days = hours / 24
                "Last update $days days ago"
            }
        }
    }

    private fun formatSpan(earliest: Instant, latest: Instant): String {
        val hours = ChronoUnit.HOURS.between(earliest, latest)
        val minutes = ChronoUnit.MINUTES.between(earliest, latest)

        return when {
            hours == 0L && minutes == 0L -> "All articles published at same time"
            hours == 0L -> "Coverage spans ${minutes}m"
            hours < 24 -> "Coverage spans ${hours}h"
            else -> {
                val days = hours / 24
                "Coverage spans $days ${if (days == 1L) "day" else "days"}"
            }
        }
    }

    /**
     * Check if any sources in this story are degraded or disabled.
     * Returns technical limitations based on source health evidence.
     */
    private suspend fun checkSourceHealthLimitations(sources: List<CoverageSource>): List<CoverageLimitation> {
        val limitations = mutableListOf<CoverageLimitation>()

        try {
            // Get current health state for all sources
            val allHealth = healthRepository.observeAllSourceHealth().first()
            val healthByPublisher = allHealth.associateBy { it.sourceName }

            // Check each source in this story
            val degradedSources = sources.mapNotNull { source ->
                val health = healthByPublisher[source.publisherName]
                when {
                    health == null -> null
                    health.status == SourceHealthStatus.DEGRADED -> {
                        Triple(source.publisherName, health.consecutiveFailures, health.lastErrorCategory)
                    }
                    health.status == SourceHealthStatus.DISABLED -> {
                        Triple(source.publisherName, health.consecutiveFailures, health.lastErrorCategory)
                    }
                    else -> null
                }
            }

            // Add limitation for each degraded/disabled source
            degradedSources.forEach { (publisher, failures, errorCategory) ->
                val categoryText = when (errorCategory) {
                    "NETWORK_ERROR" -> "network issue"
                    "HTTP_ERROR" -> "server error"
                    "PARSE_ERROR" -> "feed format issue"
                    "STALE_FEED" -> "stale feed"
                    else -> "technical issue"
                }

                limitations.add(
                    CoverageLimitation(
                        type = LimitationType.SOURCE_HEALTH,
                        description = "$publisher temporarily unavailable ($failures consecutive failures, $categoryText)"
                    )
                )
            }
        } catch (e: Exception) {
            // Don't fail the whole screen if health check fails
            // Just skip adding health limitations
        }

        return limitations
    }
}

/**
 * UI state for coverage details screen.
 */
sealed interface CoverageDetailsUiState {
    data object Loading : CoverageDetailsUiState
    data object NotFound : CoverageDetailsUiState
    data class Error(val message: String) : CoverageDetailsUiState
    data class Success(val details: CoverageDetails) : CoverageDetailsUiState
}
