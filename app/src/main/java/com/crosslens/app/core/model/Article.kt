package com.crosslens.app.core.model

import java.time.Instant

data class Article(
    val id: String,
    val storyId: String,
    val sourceId: String,
    val originalUrl: String,
    val publishedTime: Instant,
    val originalLanguage: String, // BCP 47 language tag
    val originalHeadline: String,
    val originalExcerpt: String,
    val originalContent: String, // Full article content
    val attribution: String,
    val contentUseMetadata: ContentUseMetadata,
    val requiresSubscription: Boolean = false // Whether source likely has paywall
)

data class ContentUseMetadata(
    val isDemo: Boolean,
    val isDemoPlaceholder: Boolean
)
