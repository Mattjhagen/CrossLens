package com.crosslens.app.data.mock

import com.crosslens.app.core.model.Translation
import com.crosslens.app.data.local.dao.TranslationDao
import com.crosslens.app.data.local.toDomain
import com.crosslens.app.data.repository.TranslationRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockTranslationRepository @Inject constructor(
    private val translationDao: TranslationDao
) : TranslationRepository {

    override suspend fun getTranslation(articleId: String, targetLanguage: String): Translation? {
        return translationDao.getTranslation(articleId, targetLanguage)?.toDomain()
    }

    override suspend fun getTranslationsForArticles(articleIds: List<String>): List<Translation> {
        return translationDao.getTranslationsByArticleIds(articleIds).map { it.toDomain() }
    }
}
