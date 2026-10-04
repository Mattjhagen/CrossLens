package com.crosslens.app.data.clustering

import com.crosslens.app.data.ingestion.ContentPermission
import com.crosslens.app.data.ingestion.SourceArticleRecord
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Deterministic audit dataset for Event Integrity Monitor testing.
 *
 * Covers all critical scenarios:
 * 1. True same-event, multi-publisher clusters (should PASS)
 * 2. Same-topic but different-event articles (must stay SEPARATE)
 * 3. Same named person but unrelated events (must stay SEPARATE)
 * 4. Different languages with insufficient match evidence (must stay SEPARATE)
 * 5. Duplicate/reposted articles (should DEDUPLICATE)
 * 6. Stale articles outside time window (must stay SEPARATE)
 */
object EventIntegrityTestDataset {

    private val baseTime = Instant.parse("2026-10-04T12:00:00Z")

    // ========== SCENARIO 1: True Same-Event Multi-Publisher Clusters ==========

    /**
     * Scenario 1A: High-confidence match - election result.
     * Expected: Single cluster with HIGH confidence, 3 publishers
     */
    fun sameEventElectionResult(): List<SourceArticleRecord> = listOf(
        createArticle(
            sourceId = "bbc-rss",
            headline = "Emmanuel Macron elected France president in historic victory over Marine Le Pen",
            excerpt = "Emmanuel Macron has won the French presidential election, defeating Marine Le Pen with 66% of the vote",
            time = baseTime
        ),
        createArticle(
            sourceId = "guardian-rss",
            headline = "Macron wins France presidency defeating Le Pen in historic election",
            excerpt = "Centrist Emmanuel Macron has won the French presidential election against Marine Le Pen",
            time = baseTime.plus(1, ChronoUnit.HOURS)
        ),
        createArticle(
            sourceId = "nytimes-rss",
            headline = "Emmanuel Macron defeats Marine Le Pen to become France president",
            excerpt = "Emmanuel Macron and Marine Le Pen faced off in the final round of France's presidential election",
            time = baseTime.plus(2, ChronoUnit.HOURS)
        )
    )

    /**
     * Scenario 1B: Natural disaster with multiple reporters.
     * Expected: Single cluster with HIGH confidence, 4 publishers
     */
    fun sameEventEarthquake(): List<SourceArticleRecord> = listOf(
        createArticle(
            sourceId = "reuters-rss",
            headline = "Magnitude 7.8 earthquake strikes Turkey and Syria, hundreds dead",
            excerpt = "A powerful earthquake hit southeastern Turkey and northern Syria early Monday morning",
            time = baseTime
        ),
        createArticle(
            sourceId = "bbc-rss",
            headline = "Turkey Syria earthquake: 7.8 magnitude quake kills hundreds",
            excerpt = "Hundreds of people have been killed and thousands injured after a major earthquake struck Turkey and Syria",
            time = baseTime.plus(30, ChronoUnit.MINUTES)
        ),
        createArticle(
            sourceId = "aljazeera-rss",
            headline = "Powerful 7.8 earthquake hits Turkey and Syria, killing hundreds",
            excerpt = "A 7.8-magnitude earthquake has struck Turkey and Syria, collapsing buildings and killing hundreds",
            time = baseTime.plus(45, ChronoUnit.MINUTES)
        ),
        createArticle(
            sourceId = "ft-rss",
            headline = "Turkey and Syria rocked by 7.8 magnitude earthquake",
            excerpt = "A devastating earthquake of magnitude 7.8 struck southeastern Turkey and northern Syria",
            time = baseTime.plus(1, ChronoUnit.HOURS)
        )
    )

    // ========== SCENARIO 2: Same-Topic But Different-Event Articles ==========

    /**
     * Scenario 2A: Wildfire reports from different locations.
     * Expected: NO cluster - different wildfires, just similar topic
     */
    fun differentEventsWildfires(): List<SourceArticleRecord> = listOf(
        createArticle(
            sourceId = "bbc-rss",
            headline = "California wildfire destroys 500 homes in Los Angeles County",
            excerpt = "A fast-moving wildfire has destroyed hundreds of homes in Los Angeles County, California",
            time = baseTime
        ),
        createArticle(
            sourceId = "guardian-rss",
            headline = "Oregon wildfire forces evacuations of thousands in Portland suburbs",
            excerpt = "Wildfire threatens Portland suburbs as thousands evacuate due to dangerous fire conditions in Oregon",
            time = baseTime.plus(2, ChronoUnit.HOURS)
        )
    )

    /**
     * Scenario 2B: Economic reports from different countries.
     * Expected: NO cluster - different economies, just similar topic
     */
    fun differentEventsEconomicReports(): List<SourceArticleRecord> = listOf(
        createArticle(
            sourceId = "ft-rss",
            headline = "China reports strong economic growth of 6.3% for third quarter",
            excerpt = "China's economy grew at 6.3% in the third quarter, beating expectations",
            time = baseTime
        ),
        createArticle(
            sourceId = "nytimes-rss",
            headline = "India economic growth accelerates to 7.8% as manufacturing surges",
            excerpt = "India's economy accelerated to 7.8% growth driven by strong manufacturing sector",
            time = baseTime.plus(1, ChronoUnit.HOURS)
        )
    )

    // ========== SCENARIO 3: Same Named Person But Unrelated Events ==========

    /**
     * Scenario 3A: Same political leader in different policy announcements.
     * Expected: NO cluster - different events, just same person
     */
    fun samePerson_DifferentEvents_Johnson(): List<SourceArticleRecord> = listOf(
        createArticle(
            sourceId = "bbc-rss",
            headline = "Prime Minister Johnson announces ambitious new climate policy targets",
            excerpt = "UK Prime Minister Boris Johnson unveiled sweeping climate policy changes today",
            time = baseTime
        ),
        createArticle(
            sourceId = "guardian-rss",
            headline = "Johnson faces criticism over controversial healthcare funding cuts",
            excerpt = "Prime Minister Johnson is under fire for proposed healthcare budget reductions",
            time = baseTime.plus(5, ChronoUnit.HOURS)
        )
    )

    /**
     * Scenario 3B: Same celebrity in different news contexts.
     * Expected: NO cluster - different events, just same person
     */
    fun samePerson_DifferentEvents_Celebrity(): List<SourceArticleRecord> = listOf(
        createArticle(
            sourceId = "bbc-rss",
            headline = "Taylor Swift announces world tour dates for 2027",
            excerpt = "Pop star Taylor Swift revealed dates for her upcoming 2027 world tour",
            time = baseTime
        ),
        createArticle(
            sourceId = "reuters-rss",
            headline = "Taylor Swift donates $5 million to education charity",
            excerpt = "Singer Taylor Swift has donated $5 million to support education initiatives",
            time = baseTime.plus(3, ChronoUnit.HOURS)
        )
    )

    // ========== SCENARIO 4: Different Languages With Insufficient Match Evidence ==========

    /**
     * Scenario 4A: Similar events in different languages without entity extraction.
     * Expected: NO cluster - insufficient evidence across language barrier
     */
    fun differentLanguages_InsufficientEvidence(): List<SourceArticleRecord> = listOf(
        createArticle(
            sourceId = "bbc-rss",
            headline = "President announces new economic reform package",
            excerpt = "The president outlined a comprehensive economic reform package in today's speech",
            time = baseTime,
            languageTag = "en"
        ),
        createArticle(
            sourceId = "elpais-rss",
            headline = "Presidente anuncia reformas económicas importantes",
            excerpt = "El presidente presentó reformas económicas en su discurso de hoy",
            time = baseTime.plus(1, ChronoUnit.HOURS),
            languageTag = "es"
        )
    )

    /**
     * Scenario 4B: Same-language articles about different events (control).
     * Expected: NO cluster - different events, same language
     */
    fun sameLanguage_DifferentEvents(): List<SourceArticleRecord> = listOf(
        createArticle(
            sourceId = "nytimes-rss",
            headline = "Senate passes infrastructure bill after months of debate",
            excerpt = "The US Senate passed a $1 trillion infrastructure bill today",
            time = baseTime,
            languageTag = "en"
        ),
        createArticle(
            sourceId = "reuters-rss",
            headline = "Federal Reserve raises interest rates by 0.5 percentage points",
            excerpt = "The Federal Reserve announced a half-point interest rate increase today",
            time = baseTime.plus(2, ChronoUnit.HOURS),
            languageTag = "en"
        )
    )

    // ========== SCENARIO 5: Duplicate/Reposted Articles ==========

    /**
     * Scenario 5A: Same article from the same publisher.
     * Expected: NO cluster - same publisher, automatic deduplication
     */
    fun samePublisher_SameArticle(): List<SourceArticleRecord> = listOf(
        createArticle(
            sourceId = "bbc-rss",
            headline = "Breaking: Major tech company announces layoffs of 10,000 employees",
            excerpt = "A major technology company announced plans to lay off 10,000 employees",
            time = baseTime
        ),
        createArticle(
            sourceId = "bbc-rss",
            headline = "Breaking: Major tech company announces layoffs of 10,000 employees",
            excerpt = "A major technology company announced plans to lay off 10,000 employees",
            time = baseTime.plus(10, ChronoUnit.MINUTES)
        )
    )

    /**
     * Scenario 5B: Wire service article republished by multiple outlets.
     * Expected: Cluster may form but should note syndication concern
     */
    fun syndicatedArticle(): List<SourceArticleRecord> = listOf(
        createArticle(
            sourceId = "upi-rss",
            headline = "Study finds Mediterranean diet reduces heart disease risk by 30 percent",
            excerpt = "New research shows Mediterranean diet significantly reduces cardiovascular disease risk",
            time = baseTime
        ),
        createArticle(
            sourceId = "reuters-rss",
            headline = "Study finds Mediterranean diet reduces heart disease risk by 30%",
            excerpt = "New research shows Mediterranean diet significantly reduces cardiovascular disease risk",
            time = baseTime.plus(5, ChronoUnit.MINUTES)
        )
    )

    // ========== SCENARIO 6: Stale Articles Outside Time Window ==========

    /**
     * Scenario 6A: Same event but articles published 4 days apart (exceeds 72h window).
     * Expected: NO cluster - outside 72-hour time window
     */
    fun staleArticles_OutsideTimeWindow(): List<SourceArticleRecord> = listOf(
        createArticle(
            sourceId = "nytimes-rss",
            headline = "Space agency announces plans for Mars mission in 2030",
            excerpt = "NASA revealed detailed plans for a manned Mars mission launching in 2030",
            time = baseTime
        ),
        createArticle(
            sourceId = "bbc-rss",
            headline = "Space agency announces plans for Mars mission in 2030",
            excerpt = "NASA has announced plans to send astronauts to Mars by 2030",
            time = baseTime.plus(96, ChronoUnit.HOURS) // 4 days - exceeds 72h window
        )
    )

    /**
     * Scenario 6B: Follow-up articles about same event (within window).
     * Expected: Single cluster - follow-up coverage still within time window
     */
    fun followUpCoverage_WithinWindow(): List<SourceArticleRecord> = listOf(
        createArticle(
            sourceId = "guardian-rss",
            headline = "Hurricane Ian makes landfall in Florida with 155mph winds",
            excerpt = "Hurricane Ian made landfall in Florida as a Category 4 storm with devastating winds",
            time = baseTime
        ),
        createArticle(
            sourceId = "nytimes-rss",
            headline = "Hurricane Ian devastates Florida coast with powerful winds",
            excerpt = "Hurricane Ian struck Florida's coast with winds of 155mph causing widespread damage",
            time = baseTime.plus(2, ChronoUnit.HOURS)
        ),
        createArticle(
            sourceId = "reuters-rss",
            headline = "Hurricane Ian aftermath: Florida surveys damage from powerful storm",
            excerpt = "Florida is assessing damage after Hurricane Ian made landfall as a Category 4 hurricane",
            time = baseTime.plus(48, ChronoUnit.HOURS) // 2 days - still within 72h window
        )
    )

    // ========== SCENARIO 7: Edge Cases ==========

    /**
     * Scenario 7A: Very high headline similarity but minimal shared entities.
     * Expected: Should cluster based on exceptional headline match (50%+ threshold)
     */
    fun highHeadlineSimilarity_MinimalEntities(): List<SourceArticleRecord> = listOf(
        createArticle(
            sourceId = "bbc-rss",
            headline = "Breaking: Massive explosion rocks downtown area",
            excerpt = "A massive explosion has been reported in the downtown area",
            time = baseTime
        ),
        createArticle(
            sourceId = "reuters-rss",
            headline = "Breaking: Massive explosion rocks downtown district",
            excerpt = "Reports of a large explosion in the city's downtown district",
            time = baseTime.plus(15, ChronoUnit.MINUTES)
        )
    )

    /**
     * Scenario 7B: Low headline similarity but multiple shared entities.
     * Expected: Should cluster based on entity overlap + reasonable similarity
     */
    fun lowHeadlineSimilarity_MultipleEntities(): List<SourceArticleRecord> = listOf(
        createArticle(
            sourceId = "guardian-rss",
            headline = "President Biden and Xi Jinping hold virtual summit on trade tensions",
            excerpt = "US President Joe Biden and Chinese President Xi Jinping met virtually to discuss trade relations",
            time = baseTime
        ),
        createArticle(
            sourceId = "ft-rss",
            headline = "Biden-Xi talks focus on resolving US-China trade disputes",
            excerpt = "Joe Biden and Xi Jinping discussed trade tensions between the United States and China",
            time = baseTime.plus(1, ChronoUnit.HOURS)
        )
    )

    /**
     * Scenario 7C: Minimum viable cluster (2 publishers, decent match).
     * Expected: Should cluster but with MEDIUM confidence
     */
    fun minimumViableCluster(): List<SourceArticleRecord> = listOf(
        createArticle(
            sourceId = "reuters-rss",
            headline = "Supreme Court overturns landmark abortion rights ruling",
            excerpt = "The US Supreme Court has overturned Roe v. Wade, the landmark abortion rights decision",
            time = baseTime
        ),
        createArticle(
            sourceId = "nytimes-rss",
            headline = "Supreme Court overturns Roe v Wade abortion rights ruling",
            excerpt = "In a historic decision, the Supreme Court overturned Roe v Wade",
            time = baseTime.plus(30, ChronoUnit.MINUTES)
        )
    )

    // ========== Helper Functions ==========

    private fun createArticle(
        sourceId: String,
        headline: String,
        excerpt: String = "Default excerpt for testing",
        time: Instant,
        url: String = "https://example.com/${headline.hashCode()}",
        languageTag: String = "en"
    ): SourceArticleRecord {
        return SourceArticleRecord(
            url = url,
            publishedAt = time,
            languageTag = languageTag,
            headline = headline,
            excerpt = excerpt,
            contentPermission = ContentPermission.EXPLICIT_EXCERPT,
            imageUrl = null,
            sourceId = sourceId
        )
    }

    /**
     * Get all test scenarios as a map for easy test iteration.
     */
    fun getAllScenarios(): Map<String, List<SourceArticleRecord>> = mapOf(
        "1A_same_event_election" to sameEventElectionResult(),
        "1B_same_event_earthquake" to sameEventEarthquake(),
        "2A_different_wildfires" to differentEventsWildfires(),
        "2B_different_economic" to differentEventsEconomicReports(),
        "3A_same_person_johnson" to samePerson_DifferentEvents_Johnson(),
        "3B_same_person_celebrity" to samePerson_DifferentEvents_Celebrity(),
        "4A_different_languages" to differentLanguages_InsufficientEvidence(),
        "4B_same_language_different" to sameLanguage_DifferentEvents(),
        "5A_same_publisher" to samePublisher_SameArticle(),
        "5B_syndicated" to syndicatedArticle(),
        "6A_stale_outside_window" to staleArticles_OutsideTimeWindow(),
        "6B_followup_within_window" to followUpCoverage_WithinWindow(),
        "7A_high_similarity" to highHeadlineSimilarity_MinimalEntities(),
        "7B_multiple_entities" to lowHeadlineSimilarity_MultipleEntities(),
        "7C_minimum_viable" to minimumViableCluster()
    )
}
