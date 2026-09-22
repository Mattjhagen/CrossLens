package com.crosslens.app.core.model

data class Claim(
    val id: String,
    val storyId: String,
    val statement: String,
    val assessment: ClaimAssessment,
    val supportingArticleIds: List<String>,
    val contradictingArticleIds: List<String>,
    val assessmentProvenance: String
)

enum class ClaimAssessment {
    REPORTED,
    CORROBORATED,
    DISPUTED,
    UNASSESSED
}
