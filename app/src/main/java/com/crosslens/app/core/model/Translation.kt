package com.crosslens.app.core.model

import java.time.Instant

data class Translation(
    val id: String,
    val articleId: String,
    val sourceLanguage: String,
    val targetLanguage: String,
    val translatedHeadline: String?,
    val translatedExcerpt: String?,
    val status: TranslationStatus,
    val method: String?, // e.g., "demo_manual", "google_translate"
    val provider: String?,
    val generatedTime: Instant?,
    val originalContentRevision: String?
)

enum class TranslationStatus {
    NOT_REQUESTED,
    PENDING,
    AVAILABLE,
    FAILED,
    UNAVAILABLE
}
