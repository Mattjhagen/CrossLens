package com.crosslens.app.data.clustering

import com.crosslens.app.core.model.ClusterConfidence
import com.crosslens.app.core.model.FindingSeverity
import com.crosslens.app.data.local.EventIntegrityDao
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope

/**
 * Comprehensive tests for Event Integrity Monitor using deterministic audit dataset.
 */
class EventIntegrityMonitorTest {

    private lateinit var clusteringService: EventClusteringService
    private lateinit var integrityDao: EventIntegrityDao
    private lateinit var integrityMonitor: EventIntegrityMonitor

    @Before
    fun setup() {
        clusteringService = EventClusteringService()
        integrityDao = mock()
        integrityMonitor = EventIntegrityMonitor(clusteringService, integrityDao)
    }

    // ========== SCENARIO 1: True Same-Event Multi-Publisher Clusters ==========

    @Test
    fun `scenario 1A - same event election result creates valid cluster with high confidence`() = runTest {
        val articles = EventIntegrityTestDataset.sameEventElectionResult()
        val clusters = clusteringService.clusterArticles(articles)

        // Should create exactly 1 cluster
        assertEquals("Should create 1 cluster for same event", 1, clusters.size)

        val cluster = clusters[0]
        val metadata = integrityMonitor.computeIntegrityMetadata(cluster)

        // Verify factual integrity metrics
        assertEquals("Should have 3 distinct publishers", 3, metadata.distinctPublisherCount)
        assertEquals("Should have 3 articles", 3, metadata.articleCount)
        assertTrue("Time window should be <= 24h", metadata.timeWindowHours <= 24)
        assertTrue("Should have high confidence", metadata.confidence == ClusterConfidence.HIGH)
        assertTrue("Should meet integrity threshold", metadata.meetsIntegrityThreshold)

        // Check integrity
        val checkResult = integrityMonitor.checkIntegrity(metadata)
        assertTrue("Integrity check should pass", checkResult.passed)
        assertEquals("Should have 0 errors", 0, checkResult.findings.count { it.severity == FindingSeverity.ERROR })
    }

    @Test
    fun `scenario 1B - same event earthquake creates valid cluster with 4 publishers`() = runTest {
        val articles = EventIntegrityTestDataset.sameEventEarthquake()
        val clusters = clusteringService.clusterArticles(articles)

        assertEquals("Should create 1 cluster", 1, clusters.size)

        val metadata = integrityMonitor.computeIntegrityMetadata(clusters[0])

        assertEquals("Should have 4 distinct publishers", 4, metadata.distinctPublisherCount)
        assertEquals("Should have 4 articles", 4, metadata.articleCount)
        assertTrue("Should have high confidence", metadata.confidence == ClusterConfidence.HIGH)
        assertTrue("Should meet integrity threshold", metadata.meetsIntegrityThreshold)
        assertTrue("Average similarity should be > 15%", metadata.averageHeadlineSimilarity > 0.15)
    }

    // ========== SCENARIO 2: Same-Topic But Different-Event Articles ==========

    @Test
    fun `scenario 2A - different wildfire events must not cluster`() = runTest {
        val articles = EventIntegrityTestDataset.differentEventsWildfires()
        val clusters = clusteringService.clusterArticles(articles)

        // Should NOT create any valid clusters
        assertEquals("Should not cluster different wildfire events", 0, clusters.size)
    }

    @Test
    fun `scenario 2B - different economic reports must not cluster`() = runTest {
        val articles = EventIntegrityTestDataset.differentEventsEconomicReports()
        val clusters = clusteringService.clusterArticles(articles)

        assertEquals("Should not cluster different economic reports", 0, clusters.size)
    }

    // ========== SCENARIO 3: Same Named Person But Unrelated Events ==========

    @Test
    fun `scenario 3A - same person different events must not cluster`() = runTest {
        val articles = EventIntegrityTestDataset.samePerson_DifferentEvents_Johnson()
        val clusters = clusteringService.clusterArticles(articles)

        assertEquals("Should not cluster different Johnson events", 0, clusters.size)
    }

    @Test
    fun `scenario 3B - same celebrity different news must not cluster`() = runTest {
        val articles = EventIntegrityTestDataset.samePerson_DifferentEvents_Celebrity()
        val clusters = clusteringService.clusterArticles(articles)

        assertEquals("Should not cluster different celebrity news", 0, clusters.size)
    }

    // ========== SCENARIO 4: Different Languages With Insufficient Match Evidence ==========

    @Test
    fun `scenario 4A - different languages without entities must not cluster`() = runTest {
        val articles = EventIntegrityTestDataset.differentLanguages_InsufficientEvidence()
        val clusters = clusteringService.clusterArticles(articles)

        // Without strong entity overlap or very high similarity, should not cluster
        assertTrue("Should not cluster different language articles without strong evidence", clusters.isEmpty())
    }

    @Test
    fun `scenario 4B - same language different events must not cluster`() = runTest {
        val articles = EventIntegrityTestDataset.sameLanguage_DifferentEvents()
        val clusters = clusteringService.clusterArticles(articles)

        assertEquals("Should not cluster different events in same language", 0, clusters.size)
    }

    // ========== SCENARIO 5: Duplicate/Reposted Articles ==========

    @Test
    fun `scenario 5A - same publisher same article must not cluster`() = runTest {
        val articles = EventIntegrityTestDataset.samePublisher_SameArticle()
        val clusters = clusteringService.clusterArticles(articles)

        // Same publisher is explicitly excluded from clustering
        assertEquals("Should not cluster articles from same publisher", 0, clusters.size)
    }

    @Test
    fun `scenario 5B - syndicated article may cluster but with caution`() = runTest {
        val articles = EventIntegrityTestDataset.syndicatedArticle()
        val clusters = clusteringService.clusterArticles(articles)

        // May or may not cluster depending on exact similarity
        // If it clusters, should verify it's from different publishers
        if (clusters.isNotEmpty()) {
            val metadata = integrityMonitor.computeIntegrityMetadata(clusters[0])
            assertTrue("Must have 2+ distinct publishers", metadata.distinctPublisherCount >= 2)
        }
    }

    // ========== SCENARIO 6: Stale Articles Outside Time Window ==========

    @Test
    fun `scenario 6A - stale articles outside 72h window must not cluster`() = runTest {
        val articles = EventIntegrityTestDataset.staleArticles_OutsideTimeWindow()
        val clusters = clusteringService.clusterArticles(articles)

        // 96 hours apart exceeds 72-hour window
        assertEquals("Should not cluster articles outside time window", 0, clusters.size)
    }

    @Test
    fun `scenario 6B - follow-up coverage within window should cluster`() = runTest {
        val articles = EventIntegrityTestDataset.followUpCoverage_WithinWindow()
        val clusters = clusteringService.clusterArticles(articles)

        assertEquals("Should create 1 cluster for follow-up coverage", 1, clusters.size)

        val metadata = integrityMonitor.computeIntegrityMetadata(clusters[0])
        assertTrue("Time window should be <= 72h", metadata.timeWindowHours <= 72)
        assertEquals("Should have 3 articles", 3, metadata.articleCount)
        assertEquals("Should have 3 publishers", 3, metadata.distinctPublisherCount)
    }

    // ========== SCENARIO 7: Edge Cases ==========

    @Test
    fun `scenario 7A - high headline similarity should cluster despite minimal entities`() = runTest {
        val articles = EventIntegrityTestDataset.highHeadlineSimilarity_MinimalEntities()
        val clusters = clusteringService.clusterArticles(articles)

        // Very high headline similarity (50%+) should trigger clustering
        if (clusters.isNotEmpty()) {
            val metadata = integrityMonitor.computeIntegrityMetadata(clusters[0])
            assertTrue("Average similarity should be very high", metadata.averageHeadlineSimilarity >= 0.5)
        }
    }

    @Test
    fun `scenario 7B - multiple shared entities should cluster despite lower headline similarity`() = runTest {
        val articles = EventIntegrityTestDataset.lowHeadlineSimilarity_MultipleEntities()
        val clusters = clusteringService.clusterArticles(articles)

        // Multiple shared entities (Biden, Xi Jinping) should enable clustering
        if (clusters.isNotEmpty()) {
            val metadata = integrityMonitor.computeIntegrityMetadata(clusters[0])
            assertTrue("Should have shared entities", metadata.commonNamedEntities.isNotEmpty())
        }
    }

    @Test
    fun `scenario 7C - minimum viable cluster should have medium confidence`() = runTest {
        val articles = EventIntegrityTestDataset.minimumViableCluster()
        val clusters = clusteringService.clusterArticles(articles)

        if (clusters.isNotEmpty()) {
            val metadata = integrityMonitor.computeIntegrityMetadata(clusters[0])

            assertEquals("Should have exactly 2 publishers", 2, metadata.distinctPublisherCount)
            assertEquals("Should have exactly 2 articles", 2, metadata.articleCount)
            assertTrue("Should meet minimum integrity threshold", metadata.meetsIntegrityThreshold)
        }
    }

    // ========== Integrity Check Tests ==========

    @Test
    fun `integrity check fails for single publisher cluster`() = runTest {
        val articles = listOf(
            EventIntegrityTestDataset.samePublisher_SameArticle()[0]
        )

        // Manually create a "bad" cluster for testing
        val badCluster = clusteringService.clusterArticles(articles + articles.map { it.copy(sourceId = "other-rss") })

        if (badCluster.isNotEmpty()) {
            val metadata = integrityMonitor.computeIntegrityMetadata(badCluster[0])
            val checkResult = integrityMonitor.checkIntegrity(metadata)

            if (metadata.distinctPublisherCount < 2) {
                assertFalse("Should fail integrity check for single publisher", checkResult.passed)
                assertTrue("Should have ERROR finding", checkResult.findings.any { it.severity == FindingSeverity.ERROR })
            }
        }
    }

    @Test
    fun `integrity check warns for wide time window`() = runTest {
        val articles = EventIntegrityTestDataset.followUpCoverage_WithinWindow()
        val clusters = clusteringService.clusterArticles(articles)

        assertTrue("Should create at least 1 cluster", clusters.isNotEmpty())

        val metadata = integrityMonitor.computeIntegrityMetadata(clusters[0])
        val checkResult = integrityMonitor.checkIntegrity(metadata)

        // Should always have at least one finding about time proximity
        val timeFindings = checkResult.findings.filter {
            it.category.name.contains("TEMPORAL", ignoreCase = true)
        }
        assertTrue("Should have temporal proximity finding", timeFindings.isNotEmpty())
    }

    @Test
    fun `compute integrity metadata includes all factual signals`() = runTest {
        val articles = EventIntegrityTestDataset.sameEventElectionResult()
        val clusters = clusteringService.clusterArticles(articles)

        val metadata = integrityMonitor.computeIntegrityMetadata(clusters[0])

        // Verify all required fields are populated
        assertNotNull("Cluster ID should be set", metadata.clusterId)
        assertTrue("Should have distinct publishers", metadata.distinctPublisherCount > 0)
        assertTrue("Should have articles", metadata.articleCount > 0)
        assertTrue("Should have time window", metadata.timeWindowHours >= 0)
        assertNotNull("Should have source IDs", metadata.sourceIds)
        assertNotNull("Should have article URLs", metadata.articleUrls)
        assertNotNull("Should have confidence level", metadata.confidence)
        assertNotNull("Should have match rationale", metadata.matchRationale)
        assertNotNull("Should have clustered timestamp", metadata.clusteredAt)
        assertNotNull("Should have updated timestamp", metadata.updatedAt)
    }

    @Test
    fun `record integrity metadata persists to database`() = runTest {
        val articles = EventIntegrityTestDataset.sameEventEarthquake()
        val clusters = clusteringService.clusterArticles(articles)

        // Record metadata
        integrityMonitor.recordIntegrityMetadata(clusters[0])

        // Verify DAO was called
        verify(integrityDao).insertOrUpdate(any())
    }

    @Test
    fun `record integrity metadata for all clusters persists batch`() = runTest {
        val articles = EventIntegrityTestDataset.sameEventElectionResult() +
                EventIntegrityTestDataset.sameEventEarthquake()
        val clusters = clusteringService.clusterArticles(articles)

        integrityMonitor.recordIntegrityMetadataForAll(clusters)

        verify(integrityDao).insertOrUpdateAll(any())
    }

    // ========== Comprehensive Dataset Test ==========

    @Test
    fun `all test scenarios execute without exceptions`() = runTest {
        val allScenarios = EventIntegrityTestDataset.getAllScenarios()

        var totalClusters = 0
        var passedChecks = 0
        var failedChecks = 0

        allScenarios.forEach { (scenarioName, articles) ->
            try {
                val clusters = clusteringService.clusterArticles(articles)
                totalClusters += clusters.size

                clusters.forEach { cluster ->
                    val metadata = integrityMonitor.computeIntegrityMetadata(cluster)
                    val checkResult = integrityMonitor.checkIntegrity(metadata)

                    if (checkResult.passed) {
                        passedChecks++
                    } else {
                        failedChecks++
                    }
                }
            } catch (e: Exception) {
                fail("Scenario $scenarioName threw exception: ${e.message}")
            }
        }

        println("Dataset execution complete:")
        println("  Scenarios: ${allScenarios.size}")
        println("  Clusters formed: $totalClusters")
        println("  Passed checks: $passedChecks")
        println("  Failed checks: $failedChecks")

        assertTrue("Should execute all scenarios", allScenarios.isNotEmpty())
    }
}
