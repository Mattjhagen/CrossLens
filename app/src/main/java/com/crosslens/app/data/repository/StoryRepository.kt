package com.crosslens.app.data.repository

import com.crosslens.app.core.model.*
import kotlinx.coroutines.flow.Flow

interface StoryRepository {
    fun observeStories(): Flow<List<Story>>
    fun observeStory(storyId: String): Flow<Story?>
    suspend fun getStory(storyId: String): Story?
    suspend fun getArticlesForStory(storyId: String): List<Article>
    suspend fun getClaimsForStory(storyId: String): List<Claim>
    suspend fun getFrameObservationsForStory(storyId: String): List<FrameObservation>
    suspend fun refresh(): Result<Unit>
}
