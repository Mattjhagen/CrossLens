package com.crosslens.app.data.local

import com.crosslens.app.core.model.*
import com.crosslens.app.data.local.entity.*
import java.time.Instant

fun StoryEntity.toDomain(lensGapDetails: String? = null): Story {
    val lensGap = if (lensGapStatus == "AVAILABLE" && lensGapScore != null) {
        LensGapAssessment(
            storyId = id,
            status = LensGapStatus.AVAILABLE,
            score = lensGapScore,
            components = listOf("Coverage breadth", "Framing differences"),
            sampleSourceIds = emptyList(),
            sampleArticleIds = articleIds,
            coverageWindow = "48 hours",
            confidence = "Demo illustrative",
            limitations = "Fixed demo assessment",
            methodVersion = "demo-v1",
            generatedTime = Instant.now(),
            isDemo = lensGapIsDemo
        )
    } else null

    return Story(
        id = id,
        title = title,
        summary = summary,
        eventTime = eventTime,
        updatedTime = updatedTime,
        topicIds = topicIds,
        eventCountryCodes = eventCountryCodes,
        articleIds = articleIds,
        claimIds = claimIds,
        lensGapAssessment = lensGap
    )
}

fun SourceEntity.toDomain() = Source(
    id = id,
    name = name,
    homepage = homepage,
    countryCodes = countryCodes,
    regionIds = regionIds,
    defaultLanguages = defaultLanguages,
    ownershipInfo = null,
    editorialContext = null
)

fun ArticleEntity.toDomain() = Article(
    id = id,
    storyId = storyId,
    sourceId = sourceId,
    originalUrl = originalUrl,
    publishedTime = publishedTime,
    originalLanguage = originalLanguage,
    originalHeadline = originalHeadline,
    originalExcerpt = originalExcerpt,
    attribution = attribution,
    contentUseMetadata = ContentUseMetadata(
        isDemo = isDemo,
        isDemoPlaceholder = true
    )
)

fun TranslationEntity.toDomain() = Translation(
    id = id,
    articleId = articleId,
    sourceLanguage = sourceLanguage,
    targetLanguage = targetLanguage,
    translatedHeadline = translatedHeadline,
    translatedExcerpt = translatedExcerpt,
    status = TranslationStatus.valueOf(status),
    method = method,
    provider = provider,
    generatedTime = generatedTime,
    originalContentRevision = null
)

fun ClaimEntity.toDomain() = Claim(
    id = id,
    storyId = storyId,
    statement = statement,
    assessment = ClaimAssessment.valueOf(assessment),
    supportingArticleIds = supportingArticleIds,
    contradictingArticleIds = contradictingArticleIds,
    assessmentProvenance = assessmentProvenance
)

fun FrameObservationEntity.toDomain() = FrameObservation(
    id = id,
    storyId = storyId,
    articleIds = articleIds,
    emphasizedActors = emphasizedActors,
    emphasizedClaims = emphasizedClaims,
    languageObservation = languageObservation,
    sentimentObservation = sentimentObservation,
    evidenceReferences = evidenceReferences,
    methodVersion = methodVersion,
    confidence = null,
    omissionHypothesis = null
)
