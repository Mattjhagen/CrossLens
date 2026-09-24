package com.crosslens.app.data.repository

import com.crosslens.app.core.model.*
import kotlinx.coroutines.flow.Flow

interface StoryRepository {
    fun observeStories(): Flow<List<Story>>
    fun observeStory(storyId: String): Flow<Story?>
    fun observeLocalStories(locationId: String): Flow<List<Story>> // Get local stories for location
    suspend fun getStory(storyId: String): Story?
    suspend fun getArticlesForStory(storyId: String): List<Article>
    suspend fun getClaimsForStory(storyId: String): List<Claim>
    suspend fun getFrameObservationsForStory(storyId: String): List<FrameObservation>
    suspend fun getArticle(articleId: String): Article? // Get single article by ID
    suspend fun getSourceDigest(storyId: String): SourceDigest? // Get AI digest for story
    suspend fun refresh(): Result<Unit>
}
