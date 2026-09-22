package com.crosslens.app.core.model

data class Perspective(
    val id: String,
    val scopeArticleIds: List<String>,
    val dimension: PerspectiveDimension,
    val descriptiveLabel: String,
    val evidenceReferences: List<String>,
    val attribution: String,
    val uncertainty: String?
)

enum class PerspectiveDimension {
    GEOGRAPHIC,
    POLITICAL,
    INSTITUTIONAL
}
