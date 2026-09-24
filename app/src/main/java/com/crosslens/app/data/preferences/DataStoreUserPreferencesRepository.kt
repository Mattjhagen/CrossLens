package com.crosslens.app.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.crosslens.app.core.model.Theme
import com.crosslens.app.core.model.TranslationPreference
import com.crosslens.app.core.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStoreUserPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : UserPreferencesRepository {

    private object PreferencesKeys {
        val READING_LANGUAGE = stringPreferencesKey("reading_language")
        val HOME_COUNTRY = stringPreferencesKey("home_country")
        val HOME_REGION = stringPreferencesKey("home_region")
        val ENABLED_SOURCES = stringSetPreferencesKey("enabled_sources")
        val TRANSLATION_PREF = stringPreferencesKey("translation_preference")
        val THEME = stringPreferencesKey("theme")
        val REDUCED_MOTION = booleanPreferencesKey("reduced_motion")
        val DEMO_LOCAL_LOCATION = stringPreferencesKey("demo_local_location")
    }

    override val preferencesFlow: Flow<UserPreferences> = dataStore.data.map { prefs ->
        UserPreferences(
            readingLanguage = prefs[PreferencesKeys.READING_LANGUAGE] ?: "en",
            homeCountry = prefs[PreferencesKeys.HOME_COUNTRY],
            homeRegion = prefs[PreferencesKeys.HOME_REGION],
            enabledSourceIds = prefs[PreferencesKeys.ENABLED_SOURCES] ?: emptySet(),
            translationPreference = prefs[PreferencesKeys.TRANSLATION_PREF]?.let {
                TranslationPreference.valueOf(it)
            } ?: TranslationPreference.AUTO,
            theme = prefs[PreferencesKeys.THEME]?.let { Theme.valueOf(it) } ?: Theme.SYSTEM,
            reducedMotion = prefs[PreferencesKeys.REDUCED_MOTION] ?: false,
            demoLocalLocation = prefs[PreferencesKeys.DEMO_LOCAL_LOCATION]
        )
    }

    override suspend fun updateReadingLanguage(language: String) {
        dataStore.edit { it[PreferencesKeys.READING_LANGUAGE] = language }
    }

    override suspend fun updateHomeCountry(countryCode: String?) {
        dataStore.edit {
            if (countryCode == null) it.remove(PreferencesKeys.HOME_COUNTRY)
            else it[PreferencesKeys.HOME_COUNTRY] = countryCode
        }
    }

    override suspend fun updateHomeRegion(regionId: String?) {
        dataStore.edit {
            if (regionId == null) it.remove(PreferencesKeys.HOME_REGION)
            else it[PreferencesKeys.HOME_REGION] = regionId
        }
    }

    override suspend fun updateEnabledSources(sourceIds: Set<String>) {
        dataStore.edit { it[PreferencesKeys.ENABLED_SOURCES] = sourceIds }
    }

    override suspend fun updateTranslationPreference(preference: TranslationPreference) {
        dataStore.edit { it[PreferencesKeys.TRANSLATION_PREF] = preference.name }
    }

    override suspend fun updateTheme(theme: Theme) {
        dataStore.edit { it[PreferencesKeys.THEME] = theme.name }
    }

    override suspend fun updateReducedMotion(enabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.REDUCED_MOTION] = enabled }
    }

    override suspend fun updateDemoLocalLocation(locationId: String?) {
        dataStore.edit {
            if (locationId == null) it.remove(PreferencesKeys.DEMO_LOCAL_LOCATION)
            else it[PreferencesKeys.DEMO_LOCAL_LOCATION] = locationId
        }
    }
}
