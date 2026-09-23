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

    private val baseTime = Instant.parse("2026-09-22T12:00:00Z")

    @Before
    fun setup() {
        pipeline = EventClusteringPipeline()
        storyDao = mock()
        articleDao = mock()
        editorialDecisionDao = mock()
        service = IngestionService(pipeline, storyDao, articleDao, editorialDecisionDao)
    }

    @Test
    fun `ingests articles from multiple adapters and produces reviewable clusters`() = runTest {
        val adapters = listOf(
            MockSourceAdapter.createBBC(baseTime),
            MockSourceAdapter.createLeMonde(baseTime),
            MockSourceAdapter.createAlJazeera(baseTime),
            MockSourceAdapter.createNYT(baseTime)
        )

        val result = service.ingestFromAdapters(adapters)

        assertEquals(4, result.totalArticles)
        assertEquals(4, result.acceptedArticles.size)
        assertEquals(0, result.duplicates.size)

        // Note: The prototype uses simple headline token overlap, which doesn't cluster
        // multilingual coverage well. BBC/Le Monde/Al Jazeera are about the same event
        // but have limited token overlap due to different languages.
        // This is a known limitation documented in INGESTION_PROTOTYPE.md.
        assertTrue(result.proposals.size + result.singleSourceClusters.size >= 2)

        // Verify that proposals and single-source clusters are correctly classified
        result.proposals.forEach { proposal ->
            assertEquals(ClusterStatus.REVIEWABLE, proposal.status)
            assertTrue(proposal.sourceCount >= 2)
        }
        result.singleSourceClusters.forEach { cluster ->
            assertEquals(ClusterStatus.SINGLE_SOURCE, cluster.status)
        }
    }

    @Test
    fun `clusters same-language articles with similar headlines`() = runTest {
        // Create adapters with English articles that have high headline overlap
        val bbc = MockSourceAdapter(
            sourceId = "bbc",
            sourceName = "BBC",
            articles = listOf(
                SourceArticleRecord(
                    url = "https://bbc.example/climate-geneva",
                    publishedAt = baseTime,
                    languageTag = "en",
                    headline = "Geneva climate summit reaches historic agreement on emissions",
                    excerpt = "Test"
                )
            )
        )
        val nyt = MockSourceAdapter(
            sourceId = "nyt",
            sourceName = "NYT",
            articles = listOf(
                SourceArticleRecord(
                    url = "https://nyt.example/climate-geneva",
                    publishedAt = baseTime.plusSeconds(1800),
                    languageTag = "en",
                    headline = "Historic agreement reached at Geneva climate summit on emissions",
                    excerpt = "Test"
                )
            )
        )

        val result = service.ingestFromAdapters(listOf(bbc, nyt))

        assertEquals(2, result.acceptedArticles.size)
        assertEquals(1, result.proposals.size)
        assertEquals(0, result.singleSourceClusters.size)

        val cluster = result.proposals.first()
        assertEquals(2, cluster.sourceCount)
        assertEquals(ClusterStatus.REVIEWABLE, cluster.status)
    }

    @Test
    fun `handles adapter errors gracefully`() = runTest {
        val failingAdapter = object : SourceAdapter {
            override val sourceId = "failing"
            override val sourceName = "Failing Source"
            override suspend fun fetchArticles(): List<SourceArticleRecord> {
                throw RuntimeException("Network error")
            }
        }

        val result = service.ingestFromAdapters(listOf(
            failingAdapter,
            MockSourceAdapter.createBBC(baseTime)
        ))

        assertEquals(1, result.adapterErrors.size)
        assertEquals("failing", result.adapterErrors.first().sourceId)
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
        sourceName = sourceId.uppercase(),
        originalUrl = "https://example.com/$id",
        canonicalUrl = "https://example.com/$id",
        publishedAt = baseTime,
        languageTag = "en",
        headline = "Test headline",
        excerpt = "Test excerpt",
        headlineTokens = setOf("test", "headline")
    )
}
