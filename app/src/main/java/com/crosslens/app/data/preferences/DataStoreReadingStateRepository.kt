package com.crosslens.app.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.crosslens.app.core.model.ReadingState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStoreReadingStateRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : ReadingStateRepository {

    private object Keys {
        val SAVED_STORIES = stringSetPreferencesKey("saved_stories")
        val LAST_OPENED = stringPreferencesKey("last_opened_story")
    }

    override val readingStateFlow: Flow<ReadingState> = dataStore.data.map { prefs ->
        ReadingState(
            savedStoryIds = prefs[Keys.SAVED_STORIES] ?: emptySet(),
            lastOpenedStoryId = prefs[Keys.LAST_OPENED]
        )
    }

    override suspend fun saveStory(storyId: String) {
        dataStore.edit { prefs ->
            val current = prefs[Keys.SAVED_STORIES] ?: emptySet()
            prefs[Keys.SAVED_STORIES] = current + storyId
        }
    }

    override suspend fun unsaveStory(storyId: String) {
        dataStore.edit { prefs ->
            val current = prefs[Keys.SAVED_STORIES] ?: emptySet()
            prefs[Keys.SAVED_STORIES] = current - storyId
        }
    }

    override suspend fun updateLastOpened(storyId: String) {
        dataStore.edit { it[Keys.LAST_OPENED] = storyId }
    }
}
