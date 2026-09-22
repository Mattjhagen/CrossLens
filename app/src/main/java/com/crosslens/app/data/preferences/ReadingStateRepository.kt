package com.crosslens.app.data.preferences

import com.crosslens.app.core.model.ReadingState
import kotlinx.coroutines.flow.Flow

interface ReadingStateRepository {
    val readingStateFlow: Flow<ReadingState>
    suspend fun saveStory(storyId: String)
    suspend fun unsaveStory(storyId: String)
    suspend fun updateLastOpened(storyId: String)
}
