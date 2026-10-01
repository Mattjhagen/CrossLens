package com.crosslens.app.feature.sourcehealth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crosslens.app.data.repository.RefreshResult
import com.crosslens.app.data.repository.SourceHealthRepository
import com.crosslens.app.data.repository.SourceHealthStatus
import com.crosslens.app.data.repository.SourceHealthUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

/**
 * ViewModel for Source Health & Coverage Status screen.
 * Displays technical health of RSS sources and provides manual refresh.
 */
@HiltViewModel
class SourceHealthViewModel @Inject constructor(
    private val repository: SourceHealthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SourceHealthUiState>(SourceHealthUiState.Loading)
    val uiState: StateFlow<SourceHealthUiState> = _uiState.asStateFlow()

    init {
        loadSourceHealth()
    }

    private fun loadSourceHealth() {
        viewModelScope.launch {
            try {
                // Load persisted state first
                repository.loadPersistedState()

                // Observe health data
                repository.observeAllSourceHealth().collect { sources ->
                    val activeCount = sources.count { it.status == SourceHealthStatus.ACTIVE }
                    val degradedCount = sources.count { it.status == SourceHealthStatus.DEGRADED }
                    val disabledCount = sources.count { it.status == SourceHealthStatus.DISABLED }

                    val lastRefresh = sources.maxOfOrNull { it.updatedAt }

                    _uiState.value = SourceHealthUiState.Success(
                        totalSources = sources.size,
                        activeCount = activeCount,
                        degradedCount = degradedCount,
                        disabledCount = disabledCount,
                        lastRefreshAt = lastRefresh,
                        sources = sources.sortedWith(
                            compareBy<SourceHealthUiModel> { it.status.ordinal }
                                .thenBy { it.sourceName }
                        ),
                        isRefreshing = false,
                        refreshResult = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = SourceHealthUiState.Error(
                    message = e.message ?: "Failed to load source health"
                )
            }
        }
    }

    /**
     * Trigger manual refresh of all sources.
     */
    fun refreshAllSources() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is SourceHealthUiState.Success && currentState.isRefreshing) {
                return@launch // Already refreshing
            }

            // Set refreshing state
            if (currentState is SourceHealthUiState.Success) {
                _uiState.value = currentState.copy(isRefreshing = true, refreshResult = null)
            }

            try {
                val result = repository.refreshAllSources()

                // Update UI with result
                if (currentState is SourceHealthUiState.Success) {
                    _uiState.value = currentState.copy(
                        isRefreshing = false,
                        refreshResult = result,
                        lastRefreshAt = result.completedAt
                    )
                }
            } catch (e: Exception) {
                if (currentState is SourceHealthUiState.Success) {
                    _uiState.value = currentState.copy(
                        isRefreshing = false,
                        refreshResult = null
                    )
                }
            }
        }
    }

    /**
     * Clear refresh result notification.
     */
    fun clearRefreshResult() {
        val currentState = _uiState.value
        if (currentState is SourceHealthUiState.Success) {
            _uiState.value = currentState.copy(refreshResult = null)
        }
    }
}

/**
 * UI state for Source Health screen.
 */
sealed interface SourceHealthUiState {
    data object Loading : SourceHealthUiState

    data class Success(
        val totalSources: Int,
        val activeCount: Int,
        val degradedCount: Int,
        val disabledCount: Int,
        val lastRefreshAt: Instant?,
        val sources: List<SourceHealthUiModel>,
        val isRefreshing: Boolean,
        val refreshResult: RefreshResult?
    ) : SourceHealthUiState

    data class Error(val message: String) : SourceHealthUiState
}
