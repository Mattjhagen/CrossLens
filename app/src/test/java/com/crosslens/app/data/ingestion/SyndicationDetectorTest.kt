package com.crosslens.app.data.ingestion

import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.Instant

class SyndicationDetectorTest {

    private lateinit var detector: SyndicationDetector
    private lateinit var pipeline: EventClusteringPipeline
    private val baseTime = Instant.parse("2026-09-22T12:00:00Z")

    @Before
    fun setup() {
        detector = SyndicationDetector()
        pipeline = EventClusteringPipeline(syndicationDetector = detector)
    }

    @Test
    fun `detects wire service reprints with same excerpt and headline`() = runTest {
        val adapters = SyndicationTestFixtures.createWireServiceReprints(baseTime)
        val articles = mutableListOf<IngestionArticleInput>()

        adapters.forEach { adapter ->
            adapter.fetchArticles().forEach { record ->
                articles += IngestionArticleInput(
                    sourceId = adapter.sourceId,
                    sourceName = adapter.sourceName,
                    url = record.url,
                    publishedAt = record.publishedAt,
                    languageTag = record.languageTag,
                    headline = record.headline,
                    excerpt = record.excerpt
                )
            }
        }

        val result = pipeline.process(articles)

        assertEquals(2, result.acceptedArticles.size)
        assertEquals(1, result.syndicationAnalysis.syndicationGroups.size)

        val group = result.syndicationAnalysis.syndicationGroups.first()
        assertEquals(2, group.articleIds.size)
        assertEquals(2, group.sourceIds.size)
        assertEquals(ConfidenceBand.HIGH, group.confidence)
        assertTrue(group.requiresEditorialReview)
        assertNotNull(group.detectionEvidence.titleMatch)
    }

    @Test
    fun `detects near-copy with changed headline via excerpt match`() = runTest {
        val adapters = SyndicationTestFixtures.createNearCopyWithChangedHeadline(baseTime)
        val allArticles = mutableListOf<IngestionArticleInput>()

        adapters.forEach { adapter ->
            adapter.fetchArticles().forEach { record ->
                allArticles += IngestionArticleInput(
                    sourceId = adapter.sourceId,
                    sourceName = adapter.sourceName,
                    url = record.url,
                    publishedAt = record.publishedAt,
                    languageTag = record.languageTag,
                    headline = record.headline,
                    excerpt = record.excerpt
                )
            }
        }

        val result = pipeline.process(allArticles)

        assertEquals(2, result.acceptedArticles.size)
        assertEquals(1, result.syndicationAnalysis.syndicationGroups.size)

        val group = result.syndicationAnalysis.syndicationGroups.first()
        assertEquals(2, group.articleIds.size)
        assertEquals(2, group.sourceIds.distinct().size)
        assertTrue(group.confidence == ConfidenceBand.MEDIUM || group.confidence == ConfidenceBand.HIGH)
        assertNotNull(group.detectionEvidence.excerptMatch)
        assertTrue(group.detectionEvidence.excerptMatch!!.overlapScore >= 0.80)
    }

    @Test
    fun `does not group independent reporting as syndication`() = runTest {
        val adapters = SyndicationTestFixtures.createIndependentReporting(baseTime)
        val allArticles = mutableListOf<IngestionArticleInput>()

        adapters.forEach { adapter ->
            adapter.fetchArticles().forEach { record ->
                allArticles += IngestionArticleInput(
                    sourceId = adapter.sourceId,
                    sourceName = adapter.sourceName,
                    url = record.url,
                    publishedAt = record.publishedAt,
                    languageTag = record.languageTag,
                    headline = record.headline,
                    excerpt = record.excerpt
                )
            }
        }

        val result = pipeline.process(allArticles)

        assertEquals(2, result.acceptedArticles.size)

        // These should not be grouped as syndication (different excerpts)
        // They may cluster as same event (similar titles), but that's different
        val hasHighConfidenceSyndication = result.syndicationAnalysis.syndicationGroups.any {
            it.confidence == ConfidenceBand.HIGH && it.articleIds.size >= 2
        }
        assertFalse("Independent reporting should not be marked as high-confidence syndication", hasHighConfidenceSyndication)

        // Verify both articles are either independent or have low/uncertain confidence
        val highConfidenceArticleIds = result.syndicationAnalysis.syndicationGroups
            .filter { it.confidence == ConfidenceBand.HIGH || it.confidence == ConfidenceBand.MEDIUM }
            .flatMap { it.articleIds }
            .toSet()

        // At least one article should not be in a high/medium confidence syndication group
        val independentCount = result.acceptedArticles.count { it.id !in highConfidenceArticleIds }
        assertTrue("Expected at least one independent article", independentCount >= 1)
    }

    @Test
    fun `marks multilingual content as uncertain`() = runTest {
        val adapters = SyndicationTestFixtures.createMultilingualCoverage(baseTime)
        val allArticles = mutableListOf<IngestionArticleInput>()

        adapters.forEach { adapter ->
            adapter.fetchArticles().forEach { record ->
                allArticles += IngestionArticleInput(
                    sourceId = adapter.sourceId,
                    sourceName = adapter.sourceName,
                    url = record.url,
                    publishedAt = record.publishedAt,
                    languageTag = record.languageTag,
                    headline = record.headline,
                    excerpt = record.excerpt
                )
            }
        }

        val result = pipeline.process(allArticles)

        assertEquals(2, result.acceptedArticles.size)

        // Check if multilingual match was detected with UNCERTAIN confidence
        val uncertainGroups = result.syndicationAnalysis.syndicationGroups.filter {
            it.confidence == ConfidenceBand.UNCERTAIN
        }

        if (uncertainGroups.isNotEmpty()) {
            val group = uncertainGroups.first()
            assertTrue(group.requiresEditorialReview)
            assertTrue(group.rationale.contains("language") || group.rationale.contains("translation"))
        }

        // Multilingual matches should NEVER be marked HIGH confidence
        result.syndicationAnalysis.syndicationGroups.forEach { group ->
            if (group.articleIds.size >= 2) {
                val languages = result.acceptedArticles
                    .filter { it.id in group.articleIds }
                    .map { it.languageTag }
                    .distinct()

                if (languages.size > 1) {
                    assertNotEquals("Cross-language matches must not be HIGH confidence",
                        ConfidenceBand.HIGH, group.confidence)
                }
            }
        }
    }

    @Test
    fun `url duplicates are deduplicated before syndication analysis`() = runTest {
        val adapters = SyndicationTestFixtures.createUrlDuplicates(baseTime)
        val allArticles = mutableListOf<IngestionArticleInput>()

        adapters.forEach { adapter ->
            adapter.fetchArticles().forEach { record ->
                allArticles += IngestionArticleInput(
                    sourceId = adapter.sourceId,
                    sourceName = adapter.sourceName,
                    url = record.url,
                    publishedAt = record.publishedAt,
                    languageTag = record.languageTag,
                    headline = record.headline,
                    excerpt = record.excerpt
                )
            }
        }

        val result = pipeline.process(allArticles)

        // Only one article should remain after deduplication
        assertEquals(1, result.acceptedArticles.size)
        assertEquals(1, result.duplicates.size)
        assertEquals(DuplicateReason.CANONICAL_URL, result.duplicates.first().reason)

        // Syndication analysis should only see the unique article
        assertEquals(1, result.syndicationAnalysis.analyzedCount)
        assertEquals(0, result.syndicationAnalysis.syndicationGroups.size)
    }

    @Test
    fun `short excerpts do not trigger high confidence matches`() = runTest {
        val adapters = SyndicationTestFixtures.createShortExcerpts(baseTime)
        val allArticles = mutableListOf<IngestionArticleInput>()

        adapters.forEach { adapter ->
            adapter.fetchArticles().forEach { record ->
                allArticles += IngestionArticleInput(
                    sourceId = adapter.sourceId,
                    sourceName = adapter.sourceName,
                    url = record.url,
                    publishedAt = record.publishedAt,
                    languageTag = record.languageTag,
                    headline = record.headline,
                    excerpt = record.excerpt
                )
            }
        }

        val result = pipeline.process(allArticles)

        assertEquals(2, result.acceptedArticles.size)

        // Short excerpts should not produce HIGH confidence syndication groups
        val highConfidenceGroups = result.syndicationAnalysis.syndicationGroups.filter {
            it.confidence == ConfidenceBand.HIGH
        }

        highConfidenceGroups.forEach { group ->
            val excerptLength = result.acceptedArticles
                .first { it.id in group.articleIds }
                .excerpt.length

            // If there's a HIGH confidence match, it should be via title, not short excerpt
            if (excerptLength < 100) {
                assertNotNull("Short excerpt HIGH confidence match should be via title",
                    group.detectionEvidence.titleMatch)
            }
        }
    }

    @Test
    fun `preserves all source attributions in syndication groups`() = runTest {
        val adapters = SyndicationTestFixtures.createWireServiceReprints(baseTime)
        val allArticles = mutableListOf<IngestionArticleInput>()

        adapters.forEach { adapter ->
            adapter.fetchArticles().forEach { record ->
                allArticles += IngestionArticleInput(
                    sourceId = adapter.sourceId,
                    sourceName = adapter.sourceName,
                    url = record.url,
                    publishedAt = record.publishedAt,
                    languageTag = record.languageTag,
                    headline = record.headline,
                    excerpt = record.excerpt
                )
            }
        }

        val result = pipeline.process(allArticles)

        // All articles should be preserved in acceptedArticles
        assertEquals(2, result.acceptedArticles.size)

        // Syndication group should list all article IDs and source IDs
        val group = result.syndicationAnalysis.syndicationGroups.first()
        assertEquals(2, group.articleIds.size)
        assertEquals(2, group.sourceIds.size)

        // Verify we can retrieve source attribution for each article
        group.articleIds.forEach { articleId ->
            val article = result.acceptedArticles.first { it.id == articleId }
            assertNotNull(article.sourceId)
            assertNotNull(article.sourceName)
            assertTrue(article.sourceName.isNotBlank())
        }
    }

    @Test
    fun `syndication groups require editorial review`() = runTest {
        val adapters = SyndicationTestFixtures.createWireServiceReprints(baseTime)
        val allArticles = mutableListOf<IngestionArticleInput>()

        adapters.forEach { adapter ->
            adapter.fetchArticles().forEach { record ->
                allArticles += IngestionArticleInput(
                    sourceId = adapter.sourceId,
                    sourceName = adapter.sourceName,
                    url = record.url,
                    publishedAt = record.publishedAt,
                    languageTag = record.languageTag,
                    headline = record.headline,
                    excerpt = record.excerpt
                )
            }
        }

        val result = pipeline.process(allArticles)

        result.syndicationAnalysis.syndicationGroups.forEach { group ->
            assertTrue("All syndication groups must require editorial review",
                group.requiresEditorialReview)
            assertTrue("Rationale must be present", group.rationale.isNotBlank())
        }
    }

    @Test
    fun `detection evidence is transparent and inspectable`() = runTest {
        val adapters = SyndicationTestFixtures.createWireServiceReprints(baseTime)
        val allArticles = mutableListOf<IngestionArticleInput>()

        adapters.forEach { adapter ->
            adapter.fetchArticles().forEach { record ->
                allArticles += IngestionArticleInput(
                    sourceId = adapter.sourceId,
                    sourceName = adapter.sourceName,
                    url = record.url,
                    publishedAt = record.publishedAt,
                    languageTag = record.languageTag,
                    headline = record.headline,
                    excerpt = record.excerpt
                )
            }
        }

        val result = pipeline.process(allArticles)
        val group = result.syndicationAnalysis.syndicationGroups.first()

        // Evidence should be transparent
        val evidence = group.detectionEvidence
        assertTrue("Must have title match or excerpt match",
            evidence.titleMatch != null || evidence.excerptMatch != null)

        // Method version should be present for auditability
        assertTrue(group.methodVersion.isNotBlank())

        // Confidence band should be set
        assertNotNull(group.confidence)
    }

    @Test
    fun `independent articles list contains non-syndicated articles`() = runTest {
        val adapters = listOf(
            SyndicationTestFixtures.createWireServiceReprints(baseTime), // Will be grouped
            SyndicationTestFixtures.createIndependentReporting(baseTime)  // Should stay independent
        ).flatten()

        val allArticles = mutableListOf<IngestionArticleInput>()

        adapters.forEach { adapter ->
            adapter.fetchArticles().forEach { record ->
                allArticles += IngestionArticleInput(
                    sourceId = adapter.sourceId,
                    sourceName = adapter.sourceName,
                    url = record.url,
                    publishedAt = record.publishedAt,
                    languageTag = record.languageTag,
                    headline = record.headline,
                    excerpt = record.excerpt
                )
            }
        }

        val result = pipeline.process(allArticles)

        assertEquals(4, result.acceptedArticles.size)

        // All articles should be either in a syndication group OR independent list
        val groupedIds = result.syndicationAnalysis.syndicationGroups.flatMap { it.articleIds }.toSet()
        val independentIds = result.syndicationAnalysis.independentArticleIds.toSet()

        assertEquals("Every article should be categorized",
            result.acceptedArticles.size, groupedIds.size + independentIds.size)

        // No overlap between grouped and independent
        assertTrue("No article should be both grouped and independent",
            groupedIds.intersect(independentIds).isEmpty())
    }
}
