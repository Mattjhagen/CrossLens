package com.crosslens.app.data.ingestion

import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * A source registry entry defines whether a news source is eligible for ingestion
 * and under what content-use terms. This is separate from display metadata to
 * ensure policy changes don't silently alter reader-facing source information.
 *
 * Every source must be explicitly reviewed and approved before it can emit articles
 * into the ingestion pipeline. DEMO_ONLY sources are clearly labeled and never
 * represent actual publisher agreements.
 */
data class SourceRegistryEntry(
    /** Unique source identifier matching the adapter's sourceId */
    val sourceId: String,

    /** Display name for logging and audit (may differ from reader-facing name) */
    val displayName: String,

    /** Publisher homepage URL */
    val homepage: String,

    /** Content-use eligibility status */
    val status: SourceStatus,

    /** Permitted intake method (must match adapter's actual method) */
    val permittedIntakeMethod: IntakeMethod,

    /** Required attribution format for link-and-excerpt display */
    val attributionRequirements: AttributionRequirements,

    /** When this entry was last reviewed by a human */
    val lastReviewedAt: Instant,

    /** Reviewer who approved/rejected this source */
    val reviewedBy: String,

    /** Review notes explaining eligibility decision */
    val reviewNotes: String,

    /**
     * If true, this source's articles can be clustered with other sources.
     * If false, articles appear only in single-source clusters (useful for sources
     * with specific content-use restrictions or questionable reliability).
     */
    val eligibleForClustering: Boolean = true,

    /**
     * Optional: when approval expires and requires re-review.
     * Used for time-limited permissions or trial integrations.
     */
    val approvalExpiresAt: Instant? = null
)

/**
 * Source eligibility status for ingestion.
 */
enum class SourceStatus {
    /** Fictional demo source for testing; never claims real publisher permission */
    DEMO_ONLY,

    /** Source identified but not yet reviewed for content-use eligibility */
    PENDING_REVIEW,

    /** Approved for link-and-excerpt ingestion under documented terms */
    APPROVED_LINK_AND_EXCERPT,

    /** Reviewed and rejected (no permission, violates terms, or unreliable) */
    REJECTED,

    /** Previously approved but suspended (terms violation, permission revoked, etc.) */
    SUSPENDED
}

/**
 * Permitted content intake method. Adapter's actual method must match registry entry.
 */
enum class IntakeMethod {
    /** Fictional demo data only; no real content source */
    DEMO_FIXTURE,

    /** RSS feed with explicit excerpt permission */
    RSS_WITH_EXCERPT,

    /** Licensed API with link-and-excerpt rights */
    LICENSED_API,

    /** Fair use preview (headline + brief excerpt within legal limits) */
    FAIR_USE_PREVIEW
}

/**
 * Attribution requirements for displaying link-and-excerpt content.
 */
data class AttributionRequirements(
    /** Required source name format (e.g., "BBC News", "The New York Times") */
    val sourceName: String,

    /** Link to original article required? */
    val linkRequired: Boolean = true,

    /** Custom attribution text (e.g., "via BBC News API") */
    val customAttribution: String? = null,

    /** Logo or branding requirements */
    val brandingNotes: String? = null
)

/**
 * Result of checking whether a source is eligible to emit articles.
 */
sealed class SourceEligibilityResult {
    data class Eligible(val entry: SourceRegistryEntry) : SourceEligibilityResult()
    data class Ineligible(val reason: IneligibilityReason, val details: String) : SourceEligibilityResult()
}

enum class IneligibilityReason {
    NOT_IN_REGISTRY,
    DEMO_ONLY_IN_PRODUCTION,
    PENDING_REVIEW,
    REJECTED,
    SUSPENDED,
    APPROVAL_EXPIRED,
    INTAKE_METHOD_MISMATCH
}

/**
 * Validates source eligibility and enforces content-use policy.
 */
class SourceRegistryValidator(
    private val registry: SourceRegistry,
    private val allowDemoSources: Boolean = true
) {
    /**
     * Check if a source adapter is eligible to emit articles.
     *
     * @param sourceId Source identifier from the adapter
     * @param intakeMethod Adapter's actual intake method
     * @return Eligible with entry, or Ineligible with reason
     */
    fun checkEligibility(sourceId: String, intakeMethod: IntakeMethod): SourceEligibilityResult {
        val entry = registry.getEntry(sourceId)
            ?: return SourceEligibilityResult.Ineligible(
                IneligibilityReason.NOT_IN_REGISTRY,
                "Source '$sourceId' not found in registry"
            )

        // Check status eligibility
        when (entry.status) {
            SourceStatus.DEMO_ONLY -> {
                if (!allowDemoSources) {
                    return SourceEligibilityResult.Ineligible(
                        IneligibilityReason.DEMO_ONLY_IN_PRODUCTION,
                        "Demo sources are not allowed in production mode"
                    )
                }
            }
            SourceStatus.PENDING_REVIEW -> {
                return SourceEligibilityResult.Ineligible(
                    IneligibilityReason.PENDING_REVIEW,
                    "Source '${entry.displayName}' is pending review"
                )
            }
            SourceStatus.REJECTED -> {
                return SourceEligibilityResult.Ineligible(
                    IneligibilityReason.REJECTED,
                    "Source '${entry.displayName}' was rejected: ${entry.reviewNotes}"
                )
            }
            SourceStatus.SUSPENDED -> {
                return SourceEligibilityResult.Ineligible(
                    IneligibilityReason.SUSPENDED,
                    "Source '${entry.displayName}' is suspended: ${entry.reviewNotes}"
                )
            }
            SourceStatus.APPROVED_LINK_AND_EXCERPT -> {
                // Continue to other checks
            }
        }

        // Check if approval has expired
        entry.approvalExpiresAt?.let { expiresAt ->
            if (Instant.now().isAfter(expiresAt)) {
                return SourceEligibilityResult.Ineligible(
                    IneligibilityReason.APPROVAL_EXPIRED,
                    "Approval for '${entry.displayName}' expired on $expiresAt"
                )
            }
        }

        // Check intake method matches
        if (entry.permittedIntakeMethod != intakeMethod) {
            return SourceEligibilityResult.Ineligible(
                IneligibilityReason.INTAKE_METHOD_MISMATCH,
                "Source '${entry.displayName}' permits ${entry.permittedIntakeMethod} but adapter uses $intakeMethod"
            )
        }

        return SourceEligibilityResult.Eligible(entry)
    }

    /**
     * Validate that an article record satisfies the source's attribution requirements.
     */
    fun validateAttribution(entry: SourceRegistryEntry, record: SourceArticleRecord): Result<Unit> {
        val reqs = entry.attributionRequirements

        if (reqs.linkRequired && !record.url.startsWith("https://")) {
            return Result.failure(IllegalArgumentException(
                "Source '${entry.displayName}' requires HTTPS links"
            ))
        }

        // Could add more checks: custom attribution present, headline format, etc.

        return Result.success(Unit)
    }
}

/**
 * In-memory source registry. In production, this would query a database or config service.
 */
interface SourceRegistry {
    fun getEntry(sourceId: String): SourceRegistryEntry?
    fun getAllEntries(): List<SourceRegistryEntry>
    fun getEligibleSources(): List<SourceRegistryEntry>
}

/**
 * In-memory registry implementation for testing and prototyping.
 */
class InMemorySourceRegistry(
    private val entries: Map<String, SourceRegistryEntry>
) : SourceRegistry {
    override fun getEntry(sourceId: String): SourceRegistryEntry? = entries[sourceId]
    override fun getAllEntries(): List<SourceRegistryEntry> = entries.values.toList()
    override fun getEligibleSources(): List<SourceRegistryEntry> = entries.values.filter {
        it.status == SourceStatus.APPROVED_LINK_AND_EXCERPT || it.status == SourceStatus.DEMO_ONLY
    }

    companion object {
        /**
         * Creates a mock registry with examples of each status.
         * NO entry here represents an actual publisher agreement.
         */
        fun createMockRegistry(now: Instant = Instant.now()): InMemorySourceRegistry {
            val entries = mapOf(
                "bbc-demo" to SourceRegistryEntry(
                    sourceId = "bbc-demo",
                    displayName = "BBC News (Demo)",
                    homepage = "https://bbc.example",
                    status = SourceStatus.DEMO_ONLY,
                    permittedIntakeMethod = IntakeMethod.DEMO_FIXTURE,
                    attributionRequirements = AttributionRequirements(
                        sourceName = "BBC News Demo",
                        linkRequired = true,
                        customAttribution = "Demo content for testing only"
                    ),
                    lastReviewedAt = now.minus(30, ChronoUnit.DAYS),
                    reviewedBy = "system",
                    reviewNotes = "Fictional demo source for prototype testing",
                    eligibleForClustering = true
                ),
                "lemonde-demo" to SourceRegistryEntry(
                    sourceId = "lemonde-demo",
                    displayName = "Le Monde (Demo)",
                    homepage = "https://lemonde.example",
                    status = SourceStatus.DEMO_ONLY,
                    permittedIntakeMethod = IntakeMethod.DEMO_FIXTURE,
                    attributionRequirements = AttributionRequirements(
                        sourceName = "Le Monde Demo",
                        linkRequired = true,
                        customAttribution = "Demo content for testing only"
                    ),
                    lastReviewedAt = now.minus(30, ChronoUnit.DAYS),
                    reviewedBy = "system",
                    reviewNotes = "Fictional demo source for prototype testing",
                    eligibleForClustering = true
                ),
                "guardian-demo" to SourceRegistryEntry(
                    sourceId = "guardian-demo",
                    displayName = "The Guardian (Demo)",
                    homepage = "https://guardian.example",
                    status = SourceStatus.DEMO_ONLY,
                    permittedIntakeMethod = IntakeMethod.DEMO_FIXTURE,
                    attributionRequirements = AttributionRequirements(
                        sourceName = "The Guardian Demo",
                        linkRequired = true,
                        customAttribution = "Demo content for testing only"
                    ),
                    lastReviewedAt = now.minus(30, ChronoUnit.DAYS),
                    reviewedBy = "system",
                    reviewNotes = "Fictional demo source for syndication testing",
                    eligibleForClustering = true
                ),
                "reuters-demo" to SourceRegistryEntry(
                    sourceId = "reuters-demo",
                    displayName = "Reuters (Demo)",
                    homepage = "https://reuters.example",
                    status = SourceStatus.DEMO_ONLY,
                    permittedIntakeMethod = IntakeMethod.DEMO_FIXTURE,
                    attributionRequirements = AttributionRequirements(
                        sourceName = "Reuters Demo",
                        linkRequired = true,
                        customAttribution = "Demo content for testing only"
                    ),
                    lastReviewedAt = now.minus(30, ChronoUnit.DAYS),
                    reviewedBy = "system",
                    reviewNotes = "Fictional demo source for syndication testing",
                    eligibleForClustering = true
                ),
                "ft-demo" to SourceRegistryEntry(
                    sourceId = "ft-demo",
                    displayName = "Financial Times (Demo)",
                    homepage = "https://ft.example",
                    status = SourceStatus.DEMO_ONLY,
                    permittedIntakeMethod = IntakeMethod.DEMO_FIXTURE,
                    attributionRequirements = AttributionRequirements(
                        sourceName = "Financial Times Demo",
                        linkRequired = true,
                        customAttribution = "Demo content for testing only"
                    ),
                    lastReviewedAt = now.minus(30, ChronoUnit.DAYS),
                    reviewedBy = "system",
                    reviewNotes = "Fictional demo source for syndication testing",
                    eligibleForClustering = true
                ),
                "wapo-demo" to SourceRegistryEntry(
                    sourceId = "wapo-demo",
                    displayName = "Washington Post (Demo)",
                    homepage = "https://washingtonpost.example",
                    status = SourceStatus.DEMO_ONLY,
                    permittedIntakeMethod = IntakeMethod.DEMO_FIXTURE,
                    attributionRequirements = AttributionRequirements(
                        sourceName = "Washington Post Demo",
                        linkRequired = true,
                        customAttribution = "Demo content for testing only"
                    ),
                    lastReviewedAt = now.minus(30, ChronoUnit.DAYS),
                    reviewedBy = "system",
                    reviewNotes = "Fictional demo source for syndication testing",
                    eligibleForClustering = true
                ),
                "pending-source" to SourceRegistryEntry(
                    sourceId = "pending-source",
                    displayName = "Pending Review Source",
                    homepage = "https://pending.example",
                    status = SourceStatus.PENDING_REVIEW,
                    permittedIntakeMethod = IntakeMethod.RSS_WITH_EXCERPT,
                    attributionRequirements = AttributionRequirements(sourceName = "Pending Source"),
                    lastReviewedAt = now.minus(7, ChronoUnit.DAYS),
                    reviewedBy = "reviewer-1",
                    reviewNotes = "Awaiting legal review of RSS terms",
                    eligibleForClustering = false
                ),
                "rejected-source" to SourceRegistryEntry(
                    sourceId = "rejected-source",
                    displayName = "Rejected Source",
                    homepage = "https://rejected.example",
                    status = SourceStatus.REJECTED,
                    permittedIntakeMethod = IntakeMethod.FAIR_USE_PREVIEW,
                    attributionRequirements = AttributionRequirements(sourceName = "Rejected Source"),
                    lastReviewedAt = now.minus(14, ChronoUnit.DAYS),
                    reviewedBy = "reviewer-2",
                    reviewNotes = "Terms of service prohibit aggregation",
                    eligibleForClustering = false
                ),
                "suspended-source" to SourceRegistryEntry(
                    sourceId = "suspended-source",
                    displayName = "Suspended Source",
                    homepage = "https://suspended.example",
                    status = SourceStatus.SUSPENDED,
                    permittedIntakeMethod = IntakeMethod.RSS_WITH_EXCERPT,
                    attributionRequirements = AttributionRequirements(sourceName = "Suspended Source"),
                    lastReviewedAt = now.minus(3, ChronoUnit.DAYS),
                    reviewedBy = "reviewer-1",
                    reviewNotes = "Attribution requirements violated; suspended pending resolution",
                    eligibleForClustering = false
                ),
                "approved-rss" to SourceRegistryEntry(
                    sourceId = "approved-rss",
                    displayName = "Approved RSS Source",
                    homepage = "https://approved-rss.example",
                    status = SourceStatus.APPROVED_LINK_AND_EXCERPT,
                    permittedIntakeMethod = IntakeMethod.RSS_WITH_EXCERPT,
                    attributionRequirements = AttributionRequirements(
                        sourceName = "Approved RSS Source",
                        linkRequired = true,
                        customAttribution = "via RSS feed"
                    ),
                    lastReviewedAt = now.minus(60, ChronoUnit.DAYS),
                    reviewedBy = "reviewer-1",
                    reviewNotes = "RSS feed explicitly permits link-and-excerpt use",
                    eligibleForClustering = true
                ),
                "expired-approval" to SourceRegistryEntry(
                    sourceId = "expired-approval",
                    displayName = "Expired Approval Source",
                    homepage = "https://expired.example",
                    status = SourceStatus.APPROVED_LINK_AND_EXCERPT,
                    permittedIntakeMethod = IntakeMethod.LICENSED_API,
                    attributionRequirements = AttributionRequirements(sourceName = "Expired Source"),
                    lastReviewedAt = now.minus(120, ChronoUnit.DAYS),
                    reviewedBy = "reviewer-2",
                    reviewNotes = "Trial API access granted for 90 days",
                    eligibleForClustering = true,
                    approvalExpiresAt = now.minus(30, ChronoUnit.DAYS) // Expired
                )
            )
            return InMemorySourceRegistry(entries)
        }
    }
}
