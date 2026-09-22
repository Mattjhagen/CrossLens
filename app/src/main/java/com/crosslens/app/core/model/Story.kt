package com.crosslens.app.core.model

import java.time.Instant

data class Story(
    val id: String,
    val title: String,
    val summary: String,
    val eventTime: Instant?,
    val updatedTime: Instant,
    val topicIds: List<String>,
    val eventCountryCodes: List<String>,
    val articleIds: List<String>,
    val claimIds: List<String>,
    val lensGapAssessment: LensGapAssessment?
)

data class LensGapAssessment(
    val storyId: String,
    val status: LensGapStatus,
    val score: Int?, // 0-100, null if insufficient data
    val components: List<String>,
    val sampleSourceIds: List<String>,
    val sampleArticleIds: List<String>,
    val coverageWindow: String,
    val confidence: String,
    val limitations: String,
    val methodVersion: String,
    val generatedTime: Instant,
    val isDemo: Boolean
)

enum class LensGapStatus {
    AVAILABLE,
    INSUFFICIENT_COVERAGE,
    PENDING,
    UNAVAILABLE
}
