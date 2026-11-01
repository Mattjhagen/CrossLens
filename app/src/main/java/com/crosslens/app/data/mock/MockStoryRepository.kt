package com.crosslens.app.data.mock

import androidx.room.withTransaction
import com.crosslens.app.core.model.*
import com.crosslens.app.data.local.*
import com.crosslens.app.data.local.dao.*
import com.crosslens.app.data.repository.StoryRepository
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockStoryRepository @Inject constructor(
    private val storyDao: StoryDao,
    private val articleDao: ArticleDao,
    private val claimDao: ClaimDao,
    private val frameObservationDao: FrameObservationDao,
    private val database: CrossLensDatabase,
    private val digestGenerator: com.crosslens.app.data.repository.SourceDigestGenerator
) : StoryRepository {

    suspend fun seedData() {
        database.withTransaction {
            // Fixtures are an offline fallback, never a reason to delete cached journalism.
            // Existing fixture IDs are retained so saved stories and reading progress survive.
            val existingIds = storyDao.getAllStories().map { it.id }.toSet()
            val missingStories = MockFixtures.stories.filter { it.id !in existingIds }
            val existingArticleIds = articleDao.getArticlesByIds(MockFixtures.articles.map { it.id }).map { it.id }.toSet()
            val articles = MockFixtures.articles.filter { it.id !in existingArticleIds }
            val existingTranslationIds = database.translationDao()
                .getTranslationsByArticleIds(MockFixtures.articles.map { it.id }).map { it.id }.toSet()
            val existingClaimIds = database.claimDao().getClaimsByIds(MockFixtures.claims.map { it.id }).map { it.id }.toSet()
            val existingObservationIds = MockFixtures.stories.flatMap {
                database.frameObservationDao().getObservationsByStory(it.id)
            }.map { it.id }.toSet()
            val existingSourceIds = database.sourceDao()
                .getSourcesByIds(MockFixtures.sources.map { it.id }).map { it.id }.toSet()
            database.sourceDao().insertSources(MockFixtures.sources.filter { it.id !in existingSourceIds })
            storyDao.insertStories(missingStories)
            articleDao.insertArticles(articles)
            database.translationDao().insertTranslations(MockFixtures.translations.filter { it.id !in existingTranslationIds })
            database.claimDao().insertClaims(MockFixtures.claims.filter { it.id !in existingClaimIds })
            database.frameObservationDao().insertObservations(MockFixtures.frameObservations.filter { it.id !in existingObservationIds })
        }
    }

    override fun observeStories(): Flow<List<Story>> {
        return storyDao.observeAllStories().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun observeStory(storyId: String): Flow<Story?> {
        return storyDao.observeStoryById(storyId).map { it?.toDomain() }
    }

    override fun observeLocalStories(locationId: String): Flow<List<Story>> {
        return combine(
            database.sourceDao().observeAllSources(),
            database.storyDao().observeAllStories()
        ) { sources, stories ->
            // Find local sources for this location
            val localSourceIds = sources
                .filter { it.isLocal && it.localLocationId == locationId }
                .map { it.id }
                .toSet()

            // Filter stories that have at least one article from a local source at this location
            stories.filter { story ->
                val articles = database.articleDao().getArticlesByIds(story.articleIds)
                articles.any { it.sourceId in localSourceIds }
            }.map { it.toDomain() }
        }
    }

    override suspend fun getStory(storyId: String): Story? {
        return storyDao.getStoryById(storyId)?.toDomain()
    }

    override suspend fun getArticlesForStory(storyId: String): List<Article> {
        return articleDao.observeArticlesByStory(storyId)
            .map { entities -> entities.map { it.toDomain() } }
            .first()
    }

    override suspend fun getClaimsForStory(storyId: String): List<Claim> {
        val story = getStory(storyId) ?: return emptyList()
        return claimDao.getClaimsByIds(story.claimIds).map { it.toDomain() }
    }

    override suspend fun getFrameObservationsForStory(storyId: String): List<FrameObservation> {
        return frameObservationDao.getObservationsByStory(storyId).map { it.toDomain() }
    }

    override suspend fun getArticle(articleId: String): Article? {
        return articleDao.getArticlesByIds(listOf(articleId)).firstOrNull()?.toDomain()
    }

    override suspend fun getSourceDigest(storyId: String): SourceDigest? {
        val articles = getArticlesForStory(storyId)
        if (articles.isEmpty()) return null

        val sourceIds = articles.map { it.sourceId }.distinct()
        val sources = database.sourceDao().getSourcesByIds(sourceIds)
            .associate { it.id to it.toDomain() }

        return digestGenerator.generateDigest(storyId, articles, sources)
    }

    override suspend fun refresh(): Result<Unit> {
        // Mock mode: refresh restores fixture data
        return runCatching { seedData() }
    }
}
