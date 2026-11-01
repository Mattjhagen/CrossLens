package com.crosslens.app.data.translation

import androidx.room.withTransaction
import com.crosslens.app.core.model.*
import com.crosslens.app.data.local.CrossLensDatabase
import com.crosslens.app.data.local.entity.TranslationEntity
import com.crosslens.app.data.local.toDomain
import com.crosslens.app.data.repository.TranslationRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import java.security.MessageDigest
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CachedTranslationRepository @Inject constructor(
    private val database: CrossLensDatabase,
    private val provider: TranslationProvider
) : TranslationRepository {
    private val mutex = Mutex()

    override suspend fun getTranslation(articleId: String, targetLanguage: String): Translation? = mutex.withLock {
        val article = database.articleDao().getArticlesByIds(listOf(articleId)).firstOrNull()?.toDomain()
            ?: return@withLock null
        val cached = database.translationDao().getTranslation(articleId, targetLanguage)
        if (article.contentUseMetadata.isDemo) return@withLock cached?.toDomain()
        val revision = contentRevision(article)
        val cacheId = "translation_v1_${articleId}_${targetLanguage}_$revision"
        if (cached?.id == cacheId && cached.provider == provider.providerId &&
            cached.status == TranslationStatus.AVAILABLE.name &&
            cached.generatedTime?.let { Duration.between(it, Instant.now()).seconds in 0..604800 } == true) {
            return@withLock cached.toDomain().copy(originalContentRevision = revision)
        }
        val output = try {
            withTimeoutOrNull(15_000) { provider.translate(article, targetLanguage) }
                ?: TranslationOutput(TranslationStatus.FAILED)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            TranslationOutput(TranslationStatus.FAILED)
        }
        val valid = output.status != TranslationStatus.AVAILABLE ||
            (!output.headline.isNullOrBlank() && !output.method.isNullOrBlank() && provider.providerId != "unconfigured")
        val status = if (valid) output.status else TranslationStatus.FAILED
        val translation = Translation(cacheId, articleId, article.originalLanguage, targetLanguage,
            if (status == TranslationStatus.AVAILABLE) output.headline else null,
            if (status == TranslationStatus.AVAILABLE) output.excerpt else null,
            status, output.method, provider.providerId, if (status == TranslationStatus.AVAILABLE) Instant.now() else null, revision)
        // Never persist transient failures/unavailable state as if it were a generated translation.
        if (status == TranslationStatus.AVAILABLE) database.withTransaction {
            database.translationDao().deleteTranslation(articleId, targetLanguage)
            database.translationDao().insertTranslations(listOf(TranslationEntity(translation.id, articleId,
                article.originalLanguage, targetLanguage, translation.translatedHeadline, translation.translatedExcerpt,
                status.name, translation.method, translation.provider, translation.generatedTime)))
        }
        translation
    }

    override suspend fun getTranslationsForArticles(articleIds: List<String>): List<Translation> {
        return database.translationDao().getTranslationsByArticleIds(articleIds).mapNotNull { cached ->
            val article = database.articleDao().getArticlesByIds(listOf(cached.articleId)).firstOrNull()?.toDomain()
                ?: return@mapNotNull null
            if (article.contentUseMetadata.isDemo) cached.toDomain()
            else if (cached.id.endsWith("_${contentRevision(article)}") && cached.provider == provider.providerId &&
                cached.generatedTime?.let { Duration.between(it, Instant.now()).seconds in 0..604800 } == true)
                cached.toDomain().copy(originalContentRevision = contentRevision(article))
            else null
        }
    }

    companion object {
        fun contentRevision(article: Article): String = MessageDigest.getInstance("SHA-256")
            .digest("${article.originalLanguage}\n${article.originalHeadline}\n${article.originalExcerpt}".toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
    }
}
