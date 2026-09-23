package com.crosslens.app.data.ingestion

import com.crosslens.app.data.local.dao.ArticleDao
import com.crosslens.app.data.local.dao.EditorialDecisionDao
import com.crosslens.app.data.local.dao.StoryDao
import com.crosslens.app.data.local.entity.ArticleEntity
import com.crosslens.app.data.local.entity.EditorialDecisionEntity
import com.crosslens.app.data.local.entity.StoryEntity
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import java.time.Instant

class IngestionServiceTest {

    private lateinit var service: IngestionService
    private lateinit var pipeline: EventClusteringPipeline
    private lateinit var storyDao: StoryDao
    private lateinit var articleDao: ArticleDao
    private lateinit var editorialDecisionDao: EditorialDecisionDao
    private lateinit var registry: InMemorySourceRegistry
    private lateinit var registryValidator: SourceRegistryValidator

    private val baseTime = Instant.parse("2026-09-22T12:00:00Z")

    @Before
    fun setup() {
        pipeline = EventClusteringPipeline()
        storyDao = mock()
        articleDao = mock()
        editorialDecisionDao = mock()
        registry = InMemorySourceRegistry.createMockRegistry(baseTime)
        registryValidator = SourceRegistryValidator(registry, allowDemoSources = true)
        service = IngestionService(pipeline, storyDao, articleDao, editorialDecisionDao, registryValidator)
    }

    @Test
    fun `ingests articles from registry-approved adapters`() = runTest {
        // BBC, Le Monde, and Al Jazeera are in the registry; NYT is not
        val adapters = listOf(
            MockSourceAdapter.createBBC(baseTime),
            MockSourceAdapter.createLeMonde(baseTime),
            MockSourceAdapter.createAlJazeera(baseTime),
            MockSourceAdapter.createNYT(baseTime)
        )

        val result = service.ingestFromAdapters(adapters)

        // BBC, Le Monde, and Al Jazeera should be accepted (all DEMO_ONLY in registry)
        assertEquals(3, result.totalArticles)
        assertEquals(3, result.acceptedArticles.size)
        assertEquals(0, result.duplicates.size)

        // NYT should be blocked as ineligible
        assertEquals(1, result.ineligibleSources.size)
        assertTrue(result.ineligibleSources.all { it.reason == IneligibilityReason.NOT_IN_REGISTRY })

        // Verify accepted sources use registry attribution
        val acceptedSources = result.acceptedArticles.map { it.sourceName }.toSet()
        assertTrue(acceptedSources.contains("BBC News Demo"))
        assertTrue(acceptedSources.contains("Le Monde Demo"))
        assertTrue(acceptedSources.contains("Al Jazeera Demo"))

        // Verify syndication analysis ran
        assertEquals(3, result.syndicationAnalysis.analyzedCount)

        // Verify entity extraction ran
        assertEquals(3, result.entityExtractionResult.articlesWithEntities.size)

        // Verify cross-language matching ran (no matches expected without entity metadata)
        assertNotNull(result.crossLanguageMatches)
    }

    @Test
    fun `clusters same-language articles with similar headlines`() = runTest {
        // Create adapters with English articles that have high headline overlap
        val bbc = MockSourceAdapter(
            sourceId = "bbc-demo",
            sourceName = "BBC Demo",
            articles = listOf(
                SourceArticleRecord(
                    url = "https://bbc.example/climate-geneva",
                    publishedAt = baseTime,
                    languageTag = "en",
                    headline = "Geneva climate summit reaches historic agreement on emissions",
                    excerpt = "Test excerpt for BBC"
                )
            )
        )
        val lemonde = MockSourceAdapter(
            sourceId = "lemonde-demo",
            sourceName = "Le Monde Demo",
            articles = listOf(
                SourceArticleRecord(
                    url = "https://lemonde.example/climate-geneva",
                    publishedAt = baseTime.plusSeconds(1800),
                    languageTag = "en",
                    headline = "Historic agreement reached at Geneva climate summit on emissions",
                    excerpt = "Test excerpt for Le Monde"
                )
            )
        )

        val result = service.ingestFromAdapters(listOf(bbc, lemonde))

        assertEquals(2, result.acceptedArticles.size)
        assertEquals(1, result.proposals.size)
        assertEquals(0, result.singleSourceClusters.size)

        val cluster = result.proposals.first()
        assertEquals(2, cluster.sourceCount)
        assertEquals(ClusterStatus.REVIEWABLE, cluster.status)

        // Syndication analysis should run but not necessarily find matches (different excerpts)
        assertEquals(2, result.syndicationAnalysis.analyzedCount)

        // Entity extraction should run
        assertEquals(2, result.entityExtractionResult.articlesWithEntities.size)
    }

    @Test
    fun `blocks pending review sources`() = runTest {
        val pendingAdapter = object : SourceAdapter {
            override val sourceId = "pending-source"
            override val sourceName = "Pending Source"
            override suspend fun fetchArticles(): List<SourceArticleRecord> {
                return listOf(
                    SourceArticleRecord(
                        url = "https://pending.example/article",
                        publishedAt = baseTime,
                        languageTag = "en",
                        headline = "Test",
                        excerpt = "Test"
                    )
                )
            }
        }

        val result = service.ingestFromAdapters(listOf(pendingAdapter))

        assertEquals(0, result.acceptedArticles.size)
        assertEquals(1, result.ineligibleSources.size)
        assertEquals(IneligibilityReason.PENDING_REVIEW, result.ineligibleSources.first().reason)
    }

    @Test
    fun `blocks rejected sources`() = runTest {
        val rejectedAdapter = object : SourceAdapter {
            override val sourceId = "rejected-source"
            override val sourceName = "Rejected Source"
            override suspend fun fetchArticles(): List<SourceArticleRecord> {
                return listOf(
                    SourceArticleRecord(
                        url = "https://rejected.example/article",
                        publishedAt = baseTime,
                        languageTag = "en",
                        headline = "Test",
                        excerpt = "Test"
                    )
                )
            }
        }

        val result = service.ingestFromAdapters(listOf(rejectedAdapter))

        assertEquals(0, result.acceptedArticles.size)
        assertEquals(1, result.ineligibleSources.size)
        assertEquals(IneligibilityReason.REJECTED, result.ineligibleSources.first().reason)
        assertTrue(result.ineligibleSources.first().details.contains("Terms of service"))
    }

    @Test
    fun `blocks suspended sources`() = runTest {
        val suspendedAdapter = object : SourceAdapter {
            override val sourceId = "suspended-source"
            override val sourceName = "Suspended Source"
            override suspend fun fetchArticles(): List<SourceArticleRecord> {
                return listOf(
                    SourceArticleRecord(
                        url = "https://suspended.example/article",
                        publishedAt = baseTime,
                        languageTag = "en",
                        headline = "Test",
                        excerpt = "Test"
                    )
                )
            }
        }

        val result = service.ingestFromAdapters(listOf(suspendedAdapter))

        assertEquals(0, result.acceptedArticles.size)
        assertEquals(1, result.ineligibleSources.size)
        assertEquals(IneligibilityReason.SUSPENDED, result.ineligibleSources.first().reason)
    }

    @Test
    fun `blocks expired approval sources`() = runTest {
        val expiredAdapter = object : SourceAdapter {
            override val sourceId = "expired-approval"
            override val sourceName = "Expired Source"
            override suspend fun fetchArticles(): List<SourceArticleRecord> {
                return listOf(
                    SourceArticleRecord(
                        url = "https://expired.example/article",
                        publishedAt = baseTime,
                        languageTag = "en",
                        headline = "Test",
                        excerpt = "Test"
                    )
                )
            }
        }

        val result = service.ingestFromAdapters(listOf(expiredAdapter))

        assertEquals(0, result.acceptedArticles.size)
        assertEquals(1, result.ineligibleSources.size)
        assertEquals(IneligibilityReason.APPROVAL_EXPIRED, result.ineligibleSources.first().reason)
    }

    @Test
    fun `handles adapter errors gracefully`() = runTest {
        // Create a failing adapter with a valid registry entry
        val customRegistry = InMemorySourceRegistry(mapOf(
            "bbc-demo" to InMemorySourceRegistry.createMockRegistry(baseTime).getEntry("bbc-demo")!!,
            "failing-approved" to SourceRegistryEntry(
                sourceId = "failing-approved",
                displayName = "Failing Source",
                homepage = "https://failing.example",
                status = SourceStatus.APPROVED_LINK_AND_EXCERPT,
                permittedIntakeMethod = IntakeMethod.RSS_WITH_EXCERPT, // Non-mock adapters use RSS by default
                attributionRequirements = AttributionRequirements(sourceName = "Failing Source"),
                lastReviewedAt = baseTime,
                reviewedBy = "test",
                reviewNotes = "Test",
                eligibleForClustering = true
            )
        ))
        val customValidator = SourceRegistryValidator(customRegistry, allowDemoSources = true)
        val customService = IngestionService(pipeline, storyDao, articleDao, editorialDecisionDao, customValidator)

        val failingAdapter = object : SourceAdapter {
            override val sourceId = "failing-approved"
            override val sourceName = "Failing Source"
            override suspend fun fetchArticles(): List<SourceArticleRecord> {
                throw RuntimeException("Network error")
            }
        }

        val result = customService.ingestFromAdapters(listOf(
            failingAdapter,
            MockSourceAdapter.createBBC(baseTime)
        ))

        assertEquals(1, result.adapterErrors.size)
        assertEquals("failing-approved", result.adapterErrors.first().sourceId)
        assertEquals(1, result.acceptedArticles.size) // BBC still succeeded
    }

    @Test
    fun `persists approved cluster with all entities`() = runTest {
        whenever(storyDao.insertStories(any())).thenReturn(Unit)
        whenever(articleDao.insertArticles(any())).thenReturn(Unit)
        whenever(editorialDecisionDao.insert(any())).thenReturn(Unit)

        val articles = listOf(
            createNormalizedArticle("article-1", "bbc"),
            createNormalizedArticle("article-2", "lemonde")
        )

        val decision = EditorialDecision(
            proposalId = "proposal-1",
            decision = DecisionType.APPROVED,
            reason = "Strong multi-source coverage",
            reviewerId = "reviewer-1",
            decidedAt = baseTime
        )

        val cluster = ApprovedCluster(
            clusterId = "cluster-1",
            title = "Climate summit reaches agreement",
            summary = "World leaders reached historic climate agreement",
            firstPublishedAt = baseTime,
            lastPublishedAt = baseTime.plusSeconds(3600),
            articles = articles,
            decision = decision,
            topicIds = listOf("climate"),
            eventCountryCodes = listOf("CH")
        )

        val result = service.persistApprovedCluster(cluster)

        assertTrue(result.isSuccess)
        assertEquals("cluster-1", result.getOrNull())

        // Verify all entities were persisted in correct order
        val inOrder = inOrder(editorialDecisionDao, articleDao, storyDao)
        inOrder.verify(editorialDecisionDao).insert(any<EditorialDecisionEntity>())
        inOrder.verify(articleDao).insertArticles(any())
        inOrder.verify(storyDao).insertStories(any())
    }

    @Test
    fun `rejects cluster with single source`() = runTest {
        val articles = listOf(
            createNormalizedArticle("article-1", "bbc")
        )

        val decision = EditorialDecision(
            proposalId = "proposal-1",
            decision = DecisionType.APPROVED,
            reason = "",
            reviewerId = "reviewer-1",
            decidedAt = baseTime
        )

        val cluster = ApprovedCluster(
            clusterId = "cluster-1",
            title = "Title",
            summary = "Summary",
            firstPublishedAt = baseTime,
            lastPublishedAt = baseTime,
            articles = articles,
            decision = decision
        )

        val result = service.persistApprovedCluster(cluster)

        assertTrue(result.isFailure)
        verifyNoInteractions(storyDao, articleDao, editorialDecisionDao)
    }

    @Test
    fun `records rejected decision without creating story`() = runTest {
        whenever(editorialDecisionDao.insert(any())).thenReturn(Unit)

        val proposal = EventClusterProposal(
            id = "cluster-1",
            provisionalTitle = "Test story",
            firstPublishedAt = baseTime,
            lastPublishedAt = baseTime,
            articleIds = listOf("article-1", "article-2"),
            sourceIds = listOf("bbc", "lemonde"),
            sourceCount = 2,
            status = ClusterStatus.REVIEWABLE,
            rationale = "Test"
        )

        val decision = EditorialDecision(
            proposalId = "proposal-1",
            decision = DecisionType.REJECTED,
            reason = "Duplicate of existing story",
            reviewerId = "reviewer-1",
            decidedAt = baseTime
        )

        service.recordDecision(proposal, decision)

        verify(editorialDecisionDao).insert(any<EditorialDecisionEntity>())
        verifyNoInteractions(storyDao, articleDao)
    }

    @Test
    fun `tracks decision statistics`() = runTest {
        whenever(editorialDecisionDao.countApproved()).thenReturn(15)
        whenever(editorialDecisionDao.countRejected()).thenReturn(3)

        val stats = service.getDecisionStats()

        assertEquals(15, stats.approved)
        assertEquals(3, stats.rejected)
    }

    private fun createNormalizedArticle(id: String, sourceId: String) = NormalizedArticle(
        id = id,
        sourceId = sourceId,
        sourceName = "$sourceId Demo",
        originalUrl = "https://example.com/$id",
        canonicalUrl = "https://example.com/$id",
        publishedAt = baseTime,
        languageTag = "en",
        headline = "Test headline",
        excerpt = "Test excerpt",
        headlineTokens = setOf("test", "headline")
    )
}
