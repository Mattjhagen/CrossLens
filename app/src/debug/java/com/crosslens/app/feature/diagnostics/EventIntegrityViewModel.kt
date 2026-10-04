package com.crosslens.app.feature.diagnostics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crosslens.app.data.clustering.EventIntegrityMonitor
import com.crosslens.app.data.local.EventIntegrityDao
import com.crosslens.app.data.local.toDomainModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Event Integrity Monitor (DEBUG only).
 */
@HiltViewModel
class EventIntegrityViewModel @Inject constructor(
    private val integrityDao: EventIntegrityDao,
    private val integrityMonitor: EventIntegrityMonitor
) : ViewModel() {

    private val _uiState = MutableStateFlow<EventIntegrityUiState>(EventIntegrityUiState.Loading)
    val uiState: StateFlow<EventIntegrityUiState> = _uiState.asStateFlow()

    init {
        loadIntegrityData()
    }

    private fun loadIntegrityData() {
        viewModelScope.launch {
            try {
                // Get recent clusters (last 50)
                val entities = integrityDao.getRecent(50)
                val metadataList = entities.map { it.toDomainModel() }

                // Check integrity for each
                val displayData = metadataList.map { metadata ->
                    EventIntegrityDisplayData(
                        metadata = metadata,
                        checkResult = integrityMonitor.checkIntegrity(metadata)
                    )
                }

                _uiState.value = EventIntegrityUiState.Success(displayData)
            } catch (e: Exception) {
                _uiState.value = EventIntegrityUiState.Error(
                    "Failed to load integrity data: ${e.message}"
                )
            }
        }
    }

    /**
     * Refresh integrity data.
     */
    fun refresh() {
        _uiState.value = EventIntegrityUiState.Loading
        loadIntegrityData()
    }
}
