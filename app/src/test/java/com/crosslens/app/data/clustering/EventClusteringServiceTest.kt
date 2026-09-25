package com.crosslens.app.data.clustering

import com.crosslens.app.core.model.ClusterConfidence
import com.crosslens.app.data.ingestion.ContentPermission
import com.crosslens.app.data.ingestion.SourceArticleRecord
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.time.Instant
import java.time.temporal.ChronoUnit

class EventClusteringServiceTest {

    private lateinit var service: EventClusteringService

    @Before
    fun setup() {
        service = EventClusteringService()
    }

    // ========== TRUE MATCHES ==========

    @Test
    fun `cluster articles about the same specific event`() {
        val now = Instant.now()
        val articles = listOf(
            createArticle(
                sourceId = "bbc-demo",
                headline = "Emmanuel Macron elected France president in historic victory",
                excerpt = "Emmanuel Macron defeated Marine Le Pen to become France's youngest president",
                time = now
            ),
            createArticle(
                sourceId = "guardian-demo",
                headline = "Emmanuel Macron wins France presidency defeating Marine Le Pen",
                excerpt = "Centrist Emmanuel Macron has won the French presidential election",
                time = now.plus(2, ChronoUnit.HOURS)
            ),
            createArticle(
                sourceId = "lemonde-demo",
                headline = "Macron becomes France president after defeating Le Pen",
                excerpt = "Emmanuel Macron and Marine Le Pen were the final candidates",
                time = now.plus(1, ChronoUnit.HOURS)
            )
        )

        val clusters = service.clusterArticles(articles)

        assertTrue("Should create at least 1 cluster", clusters.size >= 1)
        val cluster = clusters[0]
        assertTrue("Should include at least 2 articles", cluster.articles.size >= 2)
        assertTrue("Should have at least 2 distinct publishers", cluster.publisherCount >= 2)
        assertTrue("Should be valid cluster", cluster.isValid)
    }

    @Test
    fun `cluster two articles with high headline similarity`() {
        val now = Instant.now()
        val articles = listOf(
            createArticle(
                sourceId = "reuters-demo",
                headline = "Breaking: Earthquake strikes Japan's northern coast",
                time = now
            ),
            createArticle(
                sourceId = "bbc-demo",
                headline = "Earthquake hits Japan northern coastline",
                time = now.plus(30, ChronoUnit.MINUTES)
            )
        )

        val clusters = service.clusterArticles(articles)

        assertEquals("Should create 1 cluster", 1, clusters.size)
        assertEquals("Should include both articles", 2, clusters[0].articles.size)
        assertTrue("Should be valid (2 distinct publishers)", clusters[0].isValid)
    }

    // ========== NEAR-MATCHES THAT MUST STAY SEPARATE ==========

    @Test
    fun `do not cluster articles about same person in different events`() {
        val now = Instant.now()
        val articles = listOf(
            createArticle(
                sourceId = "bbc-demo",
                headline = "Prime Minister Johnson announces new climate policy",
                time = now
            ),
            createArticle(
                sourceId = "guardian-demo",
                headline = "Johnson faces criticism over healthcare funding cuts",
                time = now.plus(5, ChronoUnit.HOURS)
            )
        )

        val clusters = service.clusterArticles(articles)

        // Should NOT cluster - different events, just same person
        assertEquals("Should not create any valid clusters", 0, clusters.size)
    }

    @Test
    fun `do not cluster articles about same country in different events`() {
        val now = Instant.now()
        val articles = listOf(
            createArticle(
                sourceId = "nytimes-demo",
                headline = "China reports strong economic growth for third quarter",
                time = now
            ),
            createArticle(
                sourceId = "guardian-demo",
                headline = "China launches new space station module",
                time = now.plus(1, ChronoUnit.HOURS)
            )
        )

        val clusters = service.clusterArticles(articles)

        // Should NOT cluster - different events, just same country
        assertEquals("Should not create any valid clusters", 0, clusters.size)
    }

    @Test
    fun `do not cluster articles about similar topics but different specifics`() {
        val now = Instant.now()
        val articles = listOf(
            createArticle(
                sourceId = "bbc-demo",
                headline = "Wildfire destroys 500 homes in California",
                time = now
            ),
            createArticle(
                sourceId = "guardian-demo",
                headline = "Wildfire threatens evacuations in Oregon",
                time = now.plus(2, ChronoUnit.HOURS)
            )
        )

        val clusters = service.clusterArticles(articles)

        // Should NOT cluster - similar topic (wildfires) but different locations/events
        assertEquals("Should not create any valid clusters", 0, clusters.size)
    }

    // ========== DUPLICATE LINKS ==========

    @Test
    fun `do not cluster articles from same publisher`() {
        val now = Instant.now()
        val articles = listOf(
            createArticle(
                sourceId = "bbc-demo",
                headline = "Storm brings heavy flooding to coastal areas",
                url = "https://bbc.example/article1",
                time = now
            ),
            createArticle(
                sourceId = "bbc-demo",
                headline = "Flooding from storm causes widespread damage",
                url = "https://bbc.example/article2",
                time = now.plus(1, ChronoUnit.HOURS)
            )
        )

        val clusters = service.clusterArticles(articles)

        // Should NOT cluster - same publisher
        assertEquals("Should not cluster same publisher", 0, clusters.size)
    }

    // ========== MULTILINGUAL COVERAGE ==========

    @Test
    fun `cluster multilingual articles when confident about same event`() {
        val now = Instant.now()
        val articles = listOf(
            createArticle(
                sourceId = "bbc-demo",
                headline = "Nobel Prize awarded to climate change researchers",
                languageTag = "en",
                time = now
            ),
            createArticle(
                sourceId = "lemonde-demo",
                headline = "Nobel Prize climate change research award",
                languageTag = "fr",
                time = now.plus(1, ChronoUnit.HOURS)
            )
        )

        val clusters = service.clusterArticles(articles)

        // Should cluster if headline similarity is high enough despite different languages
        // (In this test, normalized English headlines should still match)
        assertEquals("Should create 1 cluster", 1, clusters.size)
        assertEquals("Should include both articles", 2, clusters[0].articles.size)
    }

    // ========== STALE ARTICLES ==========

    @Test
    fun `do not cluster articles too far apart in time`() {
        val now = Instant.now()
        val articles = listOf(
            createArticle(
                sourceId = "bbc-demo",
                headline = "President announces new economic stimulus package",
                time = now
            ),
            createArticle(
                sourceId = "guardian-demo",
                headline = "President unveils economic stimulus plan",
                time = now.plus(4, ChronoUnit.DAYS) // 4 days apart
            )
        )

        val clusters = service.clusterArticles(articles)

        // Should NOT cluster - too far apart (>72 hours)
        assertEquals("Should not cluster stale articles", 0, clusters.size)
    }

    @Test
    fun `cluster articles within reasonable time window`() {
        val now = Instant.now()
        val articles = listOf(
            createArticle(
                sourceId = "reuters-demo",
                headline = "Stock market crashes amid banking crisis concerns",
                time = now
            ),
            createArticle(
                sourceId = "ft-demo",
                headline = "Stock markets crash on banking crisis fears",
                time = now.plus(48, ChronoUnit.HOURS) // 2 days
            )
        )

        val clusters = service.clusterArticles(articles)

        // Should cluster - within 72-hour window and high similarity
        assertEquals("Should create 1 cluster", 1, clusters.size)
    }

    // ========== ONE-SOURCE EVENTS ==========

    @Test
    fun `do not create cluster with single source`() {
        val now = Instant.now()
        val articles = listOf(
            createArticle(
                sourceId = "bbc-demo",
                headline = "Local mayor resigns amid corruption allegations",
                time = now
            )
        )

        val clusters = service.clusterArticles(articles)

        assertEquals("Should not cluster single article", 0, clusters.size)
    }

    @Test
    fun `require at least two distinct publishers for valid cluster`() {
        val now = Instant.now()
        val articles = listOf(
            createArticle(
                sourceId = "bbc-demo",
                headline = "Tech company announces major layoffs",
                time = now
            ),
            createArticle(
                sourceId = "bbc-demo",
                headline = "Tech firm to lay off thousands of employees",
                time = now.plus(1, ChronoUnit.HOURS)
            ),
            createArticle(
                sourceId = "bbc-demo",
                headline = "Layoffs announced at major tech company",
                time = now.plus(2, ChronoUnit.HOURS)
            )
        )

        val clusters = service.clusterArticles(articles)

        // Should NOT create valid cluster - all same publisher
        assertEquals("Should not create cluster with single publisher", 0, clusters.size)
    }

    // ========== NORMALIZATION TESTS ==========

    @Test
    fun `normalize headlines removes punctuation and stop words`() {
        val headline1 = "Breaking: President Trump announces new policy"
        val headline2 = "President Trump's new policy announcement"

        val normalized1 = service.normalizeHeadline(headline1)
        val normalized2 = service.normalizeHeadline(headline2)

        assertTrue("Should remove 'Breaking:' prefix", !normalized1.contains("breaking"))
        assertTrue("Should normalize to lowercase", !normalized1.contains("Trump"))
        assertTrue("Normalized should contain 'trump'", normalized1.contains("trump"))
        assertTrue("Should have some overlap", normalized1.split(" ").any { it in normalized2.split(" ") })
    }

    @Test
    fun `headline normalization handles various prefixes`() {
        val testCases = listOf(
            "Breaking: Major earthquake" to "major earthquake",
            "LIVE: Election results coming in" to "election results coming",
            "Opinion: Why we need climate action" to "need climate action",
            "Analysis: The economic impact" to "economic impact"
        )

        testCases.forEach { (input, expected) ->
            val normalized = service.normalizeHeadline(input)
            // Check that key words from expected are in normalized
            expected.split(" ").forEach { word ->
                assertTrue("'$word' should be in normalized '$normalized'",
                    normalized.contains(word) || word.length <= 2)
            }
        }
    }

    // ========== CONFIDENCE LEVELS ==========

    @Test
    fun `assign HIGH confidence for strong matches with multiple sources`() {
        val now = Instant.now()
        val articles = listOf(
            createArticle("bbc-demo", "Space station collision avoided", now),
            createArticle("guardian-demo", "Space station avoids collision", now.plus(1, ChronoUnit.HOURS)),
            createArticle("reuters-demo", "Collision avoided at space station", now.plus(2, ChronoUnit.HOURS))
        )

        val clusters = service.clusterArticles(articles)

        assertEquals(1, clusters.size)
        assertEquals(ClusterConfidence.HIGH, clusters[0].confidence)
    }

    @Test
    fun `assign MEDIUM confidence for two-source matches`() {
        val now = Instant.now()
        val articles = listOf(
            createArticle("nytimes-demo", "Court rules in favor of plaintiffs", now),
            createArticle("wapo-demo", "Plaintiffs win court ruling", now.plus(3, ChronoUnit.HOURS))
        )

        val clusters = service.clusterArticles(articles)

        assertEquals(1, clusters.size)
        assertEquals(ClusterConfidence.MEDIUM, clusters[0].confidence)
    }

    // ========== EDGE CASES ==========

    @Test
    fun `handle empty article list`() {
        val clusters = service.clusterArticles(emptyList())
        assertEquals("Should return empty list", 0, clusters.size)
    }

    @Test
    fun `handle single article`() {
        val article = createArticle("bbc-demo", "Single article test", Instant.now())
        val clusters = service.clusterArticles(listOf(article))
        assertEquals("Should return empty list", 0, clusters.size)
    }

    @Test
    fun `preserve all original article data in cluster`() {
        val now = Instant.now()
        val articles = listOf(
            createArticle(
                sourceId = "bbc-demo",
                headline = "Original Headline One",
                excerpt = "Original excerpt text one",
                url = "https://bbc.example/article1",
                imageUrl = "https://images.example/image1.jpg",
                time = now
            ),
            createArticle(
                sourceId = "guardian-demo",
                headline = "Original Headline Two",
                excerpt = "Original excerpt text two",
                url = "https://guardian.example/article2",
                imageUrl = "https://images.example/image2.jpg",
                time = now.plus(1, ChronoUnit.HOURS)
            )
        )

        val clusters = service.clusterArticles(articles)

        assertEquals(1, clusters.size)
        val cluster = clusters[0]

        // Verify all original data is preserved
        val article1 = cluster.articles.find { it.sourceId == "bbc-demo" }
        assertNotNull(article1)
        assertEquals("Original Headline One", article1?.headline)
        assertEquals("Original excerpt text one", article1?.excerpt)
        assertEquals("https://bbc.example/article1", article1?.url)
        assertEquals("https://images.example/image1.jpg", article1?.imageUrl)

        val article2 = cluster.articles.find { it.sourceId == "guardian-demo" }
        assertNotNull(article2)
        assertEquals("Original Headline Two", article2?.headline)
        assertEquals("https://guardian.example/article2", article2?.url)
    }

    // ========== HELPER METHODS ==========

    private fun createArticle(
        sourceId: String,
        headline: String,
        time: Instant,
        excerpt: String = "Article excerpt",
        url: String = "https://example.com/article",
        imageUrl: String? = null,
        languageTag: String = "en"
    ): SourceArticleRecord {
        return SourceArticleRecord(
            sourceId = sourceId,
            url = url,
            publishedAt = time,
            languageTag = languageTag,
            headline = headline,
            excerpt = excerpt,
            contentPermission = ContentPermission.EXPLICIT_EXCERPT,
            imageUrl = imageUrl
        )
    }
}
