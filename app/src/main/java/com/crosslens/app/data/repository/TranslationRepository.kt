package com.crosslens.app.data.repository

import com.crosslens.app.core.model.Translation

interface TranslationRepository {
    suspend fun getTranslation(articleId: String, targetLanguage: String): Translation?
    suspend fun getTranslationsForArticles(articleIds: List<String>): List<Translation>
}
