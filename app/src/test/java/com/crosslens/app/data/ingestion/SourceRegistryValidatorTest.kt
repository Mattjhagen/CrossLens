package com.crosslens.app.data.ingestion

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.temporal.ChronoUnit

class SourceRegistryValidatorTest {

    private lateinit var registry: InMemorySourceRegistry
    private lateinit var validator: SourceRegistryValidator
    private val now = Instant.now()

    @Before
    fun setup() {
        registry = InMemorySourceRegistry.createMockRegistry(now)
        validator = SourceRegistryValidator(registry, allowDemoSources = true)
    }

    @Test
    fun `allows demo source in prototype mode`() {
        val result = validator.checkEligibility("bbc-demo", IntakeMethod.DEMO_FIXTURE)

        assertTrue(result is SourceEligibilityResult.Eligible)
        val eligible = result as SourceEligibilityResult.Eligible
        assertEquals("bbc-demo", eligible.entry.sourceId)
        assertEquals(SourceStatus.DEMO_ONLY, eligible.entry.status)
    }

    @Test
    fun `blocks demo source in production mode`() {
        val prodValidator = SourceRegistryValidator(registry, allowDemoSources = false)
        val result = prodValidator.checkEligibility("bbc-demo", IntakeMethod.DEMO_FIXTURE)

        assertTrue(result is SourceEligibilityResult.Ineligible)
        val ineligible = result as SourceEligibilityResult.Ineligible
        assertEquals(IneligibilityReason.DEMO_ONLY_IN_PRODUCTION, ineligible.reason)
    }

    @Test
    fun `allows approved source with matching intake method`() {
        val result = validator.checkEligibility("approved-rss", IntakeMethod.RSS_WITH_EXCERPT)

        assertTrue(result is SourceEligibilityResult.Eligible)
        val eligible = result as SourceEligibilityResult.Eligible
        assertEquals(SourceStatus.APPROVED_LINK_AND_EXCERPT, eligible.entry.status)
    }

    @Test
    fun `blocks pending review source`() {
        val result = validator.checkEligibility("pending-source", IntakeMethod.RSS_WITH_EXCERPT)

        assertTrue(result is SourceEligibilityResult.Ineligible)
        val ineligible = result as SourceEligibilityResult.Ineligible
        assertEquals(IneligibilityReason.PENDING_REVIEW, ineligible.reason)
        assertTrue(ineligible.details.contains("pending review"))
    }

    @Test
    fun `blocks rejected source`() {
        val result = validator.checkEligibility("rejected-source", IntakeMethod.FAIR_USE_PREVIEW)

        assertTrue(result is SourceEligibilityResult.Ineligible)
        val ineligible = result as SourceEligibilityResult.Ineligible
        assertEquals(IneligibilityReason.REJECTED, ineligible.reason)
        assertTrue(ineligible.details.contains("Terms of service prohibit"))
    }

    @Test
    fun `blocks suspended source`() {
        val result = validator.checkEligibility("suspended-source", IntakeMethod.RSS_WITH_EXCERPT)

        assertTrue(result is SourceEligibilityResult.Ineligible)
        val ineligible = result as SourceEligibilityResult.Ineligible
        assertEquals(IneligibilityReason.SUSPENDED, ineligible.reason)
        assertTrue(ineligible.details.contains("suspended"))
    }

    @Test
    fun `blocks source not in registry`() {
        val result = validator.checkEligibility("unknown-source", IntakeMethod.RSS_WITH_EXCERPT)

        assertTrue(result is SourceEligibilityResult.Ineligible)
        val ineligible = result as SourceEligibilityResult.Ineligible
        assertEquals(IneligibilityReason.NOT_IN_REGISTRY, ineligible.reason)
        assertTrue(ineligible.details.contains("not found in registry"))
    }

    @Test
    fun `blocks expired approval`() {
        val result = validator.checkEligibility("expired-approval", IntakeMethod.LICENSED_API)

        assertTrue(result is SourceEligibilityResult.Ineligible)
        val ineligible = result as SourceEligibilityResult.Ineligible
        assertEquals(IneligibilityReason.APPROVAL_EXPIRED, ineligible.reason)
        assertTrue(ineligible.details.contains("expired"))
    }

    @Test
    fun `blocks intake method mismatch`() {
        // Source permits RSS_WITH_EXCERPT but adapter uses LICENSED_API
        val result = validator.checkEligibility("approved-rss", IntakeMethod.LICENSED_API)

        assertTrue(result is SourceEligibilityResult.Ineligible)
        val ineligible = result as SourceEligibilityResult.Ineligible
        assertEquals(IneligibilityReason.INTAKE_METHOD_MISMATCH, ineligible.reason)
        assertTrue(ineligible.details.contains("permits RSS_WITH_EXCERPT"))
        assertTrue(ineligible.details.contains("adapter uses LICENSED_API"))
    }

    @Test
    fun `validates attribution requirements - HTTPS required`() {
        val entry = registry.getEntry("bbc-demo")!!
        val validRecord = SourceArticleRecord(
            url = "https://example.com/article",
            publishedAt = now,
            languageTag = "en",
            headline = "Test",
            excerpt = "Test"
        )

        val result = validator.validateAttribution(entry, validRecord)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `rejects attribution with non-HTTPS link when required`() {
        val entry = registry.getEntry("bbc-demo")!!
        val invalidRecord = SourceArticleRecord(
            url = "http://example.com/article",
            publishedAt = now,
            languageTag = "en",
            headline = "Test",
            excerpt = "Test"
        )

        val result = validator.validateAttribution(entry, invalidRecord)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("requires HTTPS") == true)
    }

    @Test
    fun `registry returns all entries`() {
        val entries = registry.getAllEntries()
        assertEquals(7, entries.size)

        val sourceIds = entries.map { it.sourceId }.toSet()
        assertTrue(sourceIds.contains("bbc-demo"))
        assertTrue(sourceIds.contains("pending-source"))
        assertTrue(sourceIds.contains("rejected-source"))
        assertTrue(sourceIds.contains("suspended-source"))
        assertTrue(sourceIds.contains("approved-rss"))
        assertTrue(sourceIds.contains("expired-approval"))
    }

    @Test
    fun `registry returns only eligible sources`() {
        val eligible = registry.getEligibleSources()

        // Should include DEMO_ONLY and APPROVED sources only
        assertTrue(eligible.any { it.sourceId == "bbc-demo" })
        assertTrue(eligible.any { it.sourceId == "lemonde-demo" })
        assertTrue(eligible.any { it.sourceId == "approved-rss" })

        // Should not include pending, rejected, or suspended (even if expired)
        assertFalse(eligible.any { it.sourceId == "pending-source" })
        assertFalse(eligible.any { it.sourceId == "rejected-source" })
        assertFalse(eligible.any { it.sourceId == "suspended-source" })

        // Expired approval is still APPROVED status, so included (expiry checked at validation time)
        assertTrue(eligible.any { it.sourceId == "expired-approval" })
    }

    @Test
    fun `registry entries have required fields`() {
        val entry = registry.getEntry("bbc-demo")!!

        assertNotNull(entry.sourceId)
        assertNotNull(entry.displayName)
        assertNotNull(entry.homepage)
        assertNotNull(entry.status)
        assertNotNull(entry.permittedIntakeMethod)
        assertNotNull(entry.attributionRequirements)
        assertNotNull(entry.lastReviewedAt)
        assertNotNull(entry.reviewedBy)
        assertNotNull(entry.reviewNotes)

        assertTrue(entry.attributionRequirements.sourceName.isNotBlank())
    }

    @Test
    fun `demo sources are clearly labeled`() {
        val bbcEntry = registry.getEntry("bbc-demo")!!
        val lemondeEntry = registry.getEntry("lemonde-demo")!!

        assertTrue(bbcEntry.displayName.contains("Demo"))
        assertTrue(lemondeEntry.displayName.contains("Demo"))
        assertTrue(bbcEntry.reviewNotes.contains("Fictional demo"))
        assertTrue(lemondeEntry.reviewNotes.contains("Fictional demo"))
        assertEquals(SourceStatus.DEMO_ONLY, bbcEntry.status)
        assertEquals(SourceStatus.DEMO_ONLY, lemondeEntry.status)
    }

    @Test
    fun `approved source has proper review documentation`() {
        val entry = registry.getEntry("approved-rss")!!

        assertEquals(SourceStatus.APPROVED_LINK_AND_EXCERPT, entry.status)
        assertNotNull(entry.lastReviewedAt)
        assertNotNull(entry.reviewedBy)
        assertTrue(entry.reviewNotes.contains("explicitly permits"))
        assertTrue(entry.eligibleForClustering)
        assertNull(entry.approvalExpiresAt) // No expiry for standard approval
    }

    @Test
    fun `rejected source has documented reason`() {
        val entry = registry.getEntry("rejected-source")!!

        assertEquals(SourceStatus.REJECTED, entry.status)
        assertTrue(entry.reviewNotes.contains("Terms of service"))
        assertFalse(entry.eligibleForClustering)
    }

    @Test
    fun `suspended source has suspension reason`() {
        val entry = registry.getEntry("suspended-source")!!

        assertEquals(SourceStatus.SUSPENDED, entry.status)
        assertTrue(entry.reviewNotes.contains("Attribution requirements violated"))
        assertFalse(entry.eligibleForClustering)
    }
}
