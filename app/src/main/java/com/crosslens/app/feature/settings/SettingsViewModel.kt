package com.crosslens.app.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crosslens.app.core.model.AccessTier
import com.crosslens.app.core.model.Theme
import com.crosslens.app.data.preferences.EntitlementRepository
import com.crosslens.app.data.preferences.PersonalRelevanceRepository
import com.crosslens.app.data.preferences.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val entitlementRepository: EntitlementRepository,
    private val personalRelevanceRepository: PersonalRelevanceRepository
) : ViewModel() {

    val userPreferences = userPreferencesRepository.preferencesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val entitlement = entitlementRepository.entitlementFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val personalRelevancePreferences = personalRelevanceRepository.preferencesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateTheme(theme: Theme) {
        viewModelScope.launch {
            userPreferencesRepository.updateTheme(theme)
        }
    }

    fun updateReducedMotion(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.updateReducedMotion(enabled)
        }
    }

    fun updateShowForYou(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.updateShowForYou(enabled)
        }
    }

    fun previewPlus() {
        viewModelScope.launch {
            entitlementRepository.setAccessTier(AccessTier.PLUS_DEMO)
        }
    }

    fun resetToFree() {
        viewModelScope.launch {
            entitlementRepository.setAccessTier(AccessTier.FREE)
        }
    }

    fun removePersonalPreference(preferenceId: String) {
        viewModelScope.launch {
            personalRelevanceRepository.removePreference(preferenceId)
        }
    }

    fun clearAllPersonalPreferences() {
        viewModelScope.launch {
            personalRelevanceRepository.clearAll()
        }
    }

    fun updateDemoLocalLocation(locationId: String?) {
        viewModelScope.launch {
            userPreferencesRepository.updateDemoLocalLocation(locationId)
        }
    }
}
