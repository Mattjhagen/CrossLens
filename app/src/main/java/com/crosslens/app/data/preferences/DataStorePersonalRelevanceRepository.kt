package com.crosslens.app.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.crosslens.app.core.model.DimensionType
import com.crosslens.app.core.model.PersonalRelevancePreference
import com.crosslens.app.core.model.PreferenceType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Stores personal relevance preferences in DataStore.
 *
 * Each preference is stored as a simple delimited string:
 * "id|preferenceType|dimensionType|dimensionValue|epochSecond"
 */
@Singleton
class DataStorePersonalRelevanceRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : PersonalRelevanceRepository {

    private object PreferencesKeys {
        val RELEVANCE_PREFERENCES = stringSetPreferencesKey("personal_relevance_preferences")
    }

    override val preferencesFlow: Flow<List<PersonalRelevancePreference>> = dataStore.data.map { prefs ->
        val serialized = prefs[PreferencesKeys.RELEVANCE_PREFERENCES] ?: emptySet()
        serialized.mapNotNull { parsePreference(it) }
    }

    override suspend fun addPreference(
        preferenceType: PreferenceType,
        dimensionType: DimensionType,
        dimensionValue: String
    ) {
        dataStore.edit { prefs ->
            val current = prefs[PreferencesKeys.RELEVANCE_PREFERENCES] ?: emptySet()

            // Remove any existing preference for this dimension value (to handle reversibility)
            val filtered = current.filterNot { serialized ->
                parsePreference(serialized)?.let {
                    it.dimensionType == dimensionType && it.dimensionValue == dimensionValue
                } ?: false
            }.toSet()

            // Add new preference
            val newPreference = PersonalRelevancePreference(
                id = UUID.randomUUID().toString(),
                preferenceType = preferenceType,
                dimensionType = dimensionType,
                dimensionValue = dimensionValue,
                createdTime = Instant.now()
            )

            prefs[PreferencesKeys.RELEVANCE_PREFERENCES] = filtered + serializePreference(newPreference)
        }
    }

    override suspend fun removePreference(preferenceId: String) {
        dataStore.edit { prefs ->
            val current = prefs[PreferencesKeys.RELEVANCE_PREFERENCES] ?: emptySet()
            val filtered = current.filterNot { serialized ->
                parsePreference(serialized)?.id == preferenceId
            }.toSet()
            prefs[PreferencesKeys.RELEVANCE_PREFERENCES] = filtered
        }
    }

    override suspend fun clearAll() {
        dataStore.edit { prefs ->
            prefs.remove(PreferencesKeys.RELEVANCE_PREFERENCES)
        }
    }

    override suspend fun getPreferences(): List<PersonalRelevancePreference> {
        var result: List<PersonalRelevancePreference> = emptyList()
        dataStore.data.map { prefs ->
            val serialized = prefs[PreferencesKeys.RELEVANCE_PREFERENCES] ?: emptySet()
            result = serialized.mapNotNull { parsePreference(it) }
        }
        return result
    }

    private fun serializePreference(preference: PersonalRelevancePreference): String {
        return "${preference.id}|${preference.preferenceType.name}|${preference.dimensionType.name}|${preference.dimensionValue}|${preference.createdTime.epochSecond}"
    }

    private fun parsePreference(serialized: String): PersonalRelevancePreference? {
        return try {
            val parts = serialized.split("|")
            if (parts.size != 5) return null

            PersonalRelevancePreference(
                id = parts[0],
                preferenceType = PreferenceType.valueOf(parts[1]),
                dimensionType = DimensionType.valueOf(parts[2]),
                dimensionValue = parts[3],
                createdTime = Instant.ofEpochSecond(parts[4].toLong())
            )
        } catch (e: Exception) {
            null // Ignore malformed preferences
        }
    }
}
