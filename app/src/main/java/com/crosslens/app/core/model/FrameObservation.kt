package com.crosslens.app.core.model

data class FrameObservation(
    val id: String,
    val storyId: String,
    val articleIds: List<String>,
    val emphasizedActors: List<String>,
    val emphasizedClaims: List<String>,
    val languageObservation: String?,
    val sentimentObservation: String?,
    val evidenceReferences: List<String>,
    val methodVersion: String,
    val confidence: String?,
    val omissionHypothesis: OmissionHypothesis?
)

data class OmissionHypothesis(
    val description: String,
    val comparisonScope: String
)
