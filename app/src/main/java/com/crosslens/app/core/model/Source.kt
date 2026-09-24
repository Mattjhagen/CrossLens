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
    val editorialContext: EditorialContext?,
    val localSourceMetadata: LocalSourceMetadata?
)

/**
 * Metadata specific to local news sources.
 *
 * A source's city/region provides geographic context but does not imply
 * political affiliation or editorial reliability.
 */
data class LocalSourceMetadata(
    val localLocationId: String, // References LocalLocation.id
    val publisherType: PublisherType,
    val isDemo: Boolean = true
)

enum class PublisherType {
    LOCAL_NEWSPAPER,    // Traditional local newspaper
    LOCAL_TV,          // Local television station
    LOCAL_RADIO,       // Local radio station
    LOCAL_DIGITAL,     // Digital-only local outlet
    COMMUNITY_NEWS     // Community-focused publication
}

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
