package com.crosslens.app.data.preferences

import com.crosslens.app.core.model.Theme
import com.crosslens.app.core.model.TranslationPreference
import com.crosslens.app.core.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import java.time.Instant

interface UserPreferencesRepository {
    val preferencesFlow: Flow<UserPreferences>
    suspend fun updateReadingLanguage(language: String)
    suspend fun updateHomeCountry(countryCode: String?)
    suspend fun updateHomeRegion(regionId: String?)
    suspend fun updateEnabledSources(sourceIds: Set<String>)
    suspend fun updateTranslationPreference(preference: TranslationPreference)
    suspend fun updateTheme(theme: Theme)
    suspend fun updateReducedMotion(enabled: Boolean)
    suspend fun updateDemoLocalLocation(locationId: String?)
    suspend fun updateShowLocalOnly(enabled: Boolean)
    suspend fun updateLastRefreshedTime(time: Instant)
    suspend fun updateShowForYou(enabled: Boolean)
}
