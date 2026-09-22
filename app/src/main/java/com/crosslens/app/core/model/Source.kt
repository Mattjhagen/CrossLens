package com.crosslens.app.core.model

import java.time.Instant

data class Source(
    val id: String,
    val name: String,
    val homepage: String,
    val countryCodes: List<String>,
    val regionIds: List<String>,
    val defaultLanguages: List<String>,
    val ownershipInfo: OwnershipInfo?,
    val editorialContext: EditorialContext?
)

data class OwnershipInfo(
    val description: String,
    val citations: List<String>,
    val reviewDate: Instant
)

data class EditorialContext(
    val description: String,
    val citations: List<String>,
    val reviewDate: Instant
)
