package com.crosslens.app.data.translation

import com.crosslens.app.core.model.Article
import com.crosslens.app.core.model.TranslationStatus

/** A production implementation must call a trusted backend; never put service secrets in the APK. */
interface TranslationProvider {
    val providerId: String
    suspend fun translate(article: Article, targetLanguage: String): TranslationOutput
}

data class TranslationOutput(
    val status: TranslationStatus,
    val headline: String? = null,
    val excerpt: String? = null,
    val method: String? = null
)

class UnconfiguredTranslationProvider : TranslationProvider {
    override val providerId = "unconfigured"
    override suspend fun translate(article: Article, targetLanguage: String) =
        TranslationOutput(TranslationStatus.UNAVAILABLE)
}
