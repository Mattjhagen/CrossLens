package com.crosslens.app.feature.sourcehealth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crosslens.app.data.repository.RefreshResult
import com.crosslens.app.data.repository.SourceHealthRepository
import com.crosslens.app.data.repository.SourceHealthStatus
import com.crosslens.app.data.repository.SourceHealthUiModel
import com.crosslens.app.util.SourceHealthDiagnostics
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

    private val _isRefreshing = MutableStateFlow(false)
    private val _refreshResult = MutableStateFlow<RefreshResult?>(null)

    init {
        // DIAGNOSTIC: Verify logging works
        android.util.Log.d("SourceHealth:Debug", "═══ ViewModel CREATED ═══")
        android.util.Log.d("SourceHealth:Debug", "  Time: ${System.currentTimeMillis()}")

        loadSourceHealth()
    }

    private fun loadSourceHealth() {
        viewModelScope.launch {
            try {
                // Load persisted state first
                repository.loadPersistedState()

                // Observe health data and combine with refresh state
                repository.observeAllSourceHealth().collect { sources ->
                    // DIAGNOSTIC: Log Flow emission
                    SourceHealthDiagnostics.logViewModelFlowEmission(
                        sourceCount = sources.size,
                        lastRefreshTimestamp = sources.maxOfOrNull { it.updatedAt }?.toEpochMilli()
                    )

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
                        isRefreshing = _isRefreshing.value,
                        refreshResult = _refreshResult.value
                    )

                    // DIAGNOSTIC: Log UI state update
                    SourceHealthDiagnostics.logViewModelStateUpdate(
                        activeCount = activeCount,
                        degradedCount = degradedCount,
                        disabledCount = disabledCount,
                        lastRefreshTimestamp = lastRefresh?.toEpochMilli(),
                        isRefreshing = _isRefreshing.value
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
     *
     * This method sets refresh state flags and calls repository.refreshAllSources(),
     * which updates the database. The Flow collection in loadSourceHealth() will
     * automatically pick up database changes and update the UI state with fresh
     * source data, counts, and timestamps.
     */
    fun refreshAllSources() {
        // CRITICAL TEST: Direct log to verify method is called
        android.util.Log.wtf("SourceHealth:Debug", "╔═══════════════════════════════════════")
        android.util.Log.wtf("SourceHealth:Debug", "║ refreshAllSources() CALLED!!!")
        android.util.Log.wtf("SourceHealth:Debug", "║ Thread: ${Thread.currentThread().name}")
        android.util.Log.wtf("SourceHealth:Debug", "╚═══════════════════════════════════════")

        viewModelScope.launch {
            if (_isRefreshing.value) {
                android.util.Log.wtf("SourceHealth:Debug", "  Already refreshing, aborting")
                return@launch // Already refreshing
            }

            // DIAGNOSTIC: Log refresh start
            SourceHealthDiagnostics.logViewModelRefreshStart()

            // Set refreshing state
            _isRefreshing.value = true
            _refreshResult.value = null
            updateRefreshFlags()

            try {
                val result = repository.refreshAllSources()

                // DIAGNOSTIC: Log refresh end
                SourceHealthDiagnostics.logViewModelRefreshEnd(
                    successCount = result.successCount,
                    failureCount = result.failureCount
                )

                // Store refresh result; Flow will update source data automatically
                _refreshResult.value = result
                _isRefreshing.value = false
                updateRefreshFlags()
            } catch (e: Exception) {
                _isRefreshing.value = false
                _refreshResult.value = null
                updateRefreshFlags()
            }
        }
    }

    /**
     * Update UI state with current refresh flags without changing source data.
     * Called after refresh state changes to propagate isRefreshing/refreshResult.
     */
    private fun updateRefreshFlags() {
        val currentState = _uiState.value
        if (currentState is SourceHealthUiState.Success) {
            _uiState.value = currentState.copy(
                isRefreshing = _isRefreshing.value,
                refreshResult = _refreshResult.value
            )
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
