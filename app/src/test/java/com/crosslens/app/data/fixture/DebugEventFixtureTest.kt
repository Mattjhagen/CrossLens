package com.crosslens.app.data.fixture

import com.crosslens.app.data.clustering.EventClusteringService
import org.junit.Assert.*
import org.junit.Test

/**
 * Tests for debug event fixture provider.
 *
 * Verifies:
 * - Fixture articles represent same specific event
 * - All articles have distinct publishers
 * - Attribution and URLs are realistic
 * - Timestamps are recent (for feed freshness)
 * - All required fields are populated
 */
class DebugEventFixtureTest {

    @Test
    fun `fixture returns 4 articles for Read Across Coverage testing`() {
        val provider = DebugEventFixtureProviderImpl()
        val fixture = provider.getReadAcrossFixture()

        assertEquals("Should return exactly 4 articles", 4, fixture.size)
    }

    @Test
    fun `all fixture articles have distinct source IDs`() {
        val provider = DebugEventFixtureProviderImpl()
        val fixture = provider.getReadAcrossFixture()

        val sourceIds = fixture.map { it.sourceId }
        val distinctSourceIds = sourceIds.distinct()

        assertEquals(
            "All source IDs should be distinct (4 different publishers)",
            sourceIds.size,
            distinctSourceIds.size
        )
    }

    @Test
    fun `all fixture articles have same event theme`() {
        val provider = DebugEventFixtureProviderImpl()
        val fixture = provider.getReadAcrossFixture()

        // All articles should mention France-Germany defense treaty
        fixture.forEach { article ->
            val combinedText = article.headline + " " + article.excerpt
            assertTrue(
                "Article should mention France-Germany defense treaty: ${article.headline}",
                (combinedText.contains("France", ignoreCase = false) ||
                 combinedText.contains("Germany", ignoreCase = false)) &&
                (combinedText.contains("defense", ignoreCase = true) ||
                 combinedText.contains("defence", ignoreCase = true) ||
                 combinedText.contains("treaty", ignoreCase = true) ||
                 combinedText.contains("pact", ignoreCase = true) ||
                 combinedText.contains("agreement", ignoreCase = true))
            )
        }
    }

    @Test
    fun `fixture articles are about same international event`() {
        val provider = DebugEventFixtureProviderImpl()
        val fixture = provider.getReadAcrossFixture()

        // Verify all articles are about the same specific event (France-Germany treaty signing)
        // This is the key requirement - not just same topic, but same specific event
        val macronCount = fixture.count { article ->
            val text = article.headline + " " + article.excerpt
            text.contains("Macron", ignoreCase = false)
        }
        val scholzCount = fixture.count { article ->
            val text = article.headline + " " + article.excerpt
            text.contains("Scholz", ignoreCase = false)
        }

        assertTrue(
            "All articles should mention Macron (key event participant)",
            macronCount == 4
        )
        assertTrue(
            "All articles should mention Scholz (key event participant)",
            scholzCount == 4
        )
    }

    @Test
    fun `fixture includes English language articles only`() {
        val provider = DebugEventFixtureProviderImpl()
        val fixture = provider.getReadAcrossFixture()

        val languages = fixture.map { it.languageTag }.distinct()

        assertTrue(
            "All articles should be in English for entity extraction",
            languages.all { it.startsWith("en") }
        )
    }

    @Test
    fun `fixture includes diverse countries`() {
        val provider = DebugEventFixtureProviderImpl()
        val fixture = provider.getReadAcrossFixture()

        // Verify we have 4 distinct international sources
        val expectedSources = setOf(
            "bbc-news-rss",      // UK
            "nytimes-rss",       // US
            "dw-rss",            // Germany
            "guardian-rss"       // UK
        )

        val actualSources = fixture.map { it.sourceId }.toSet()
        assertEquals(
            "Fixture should include exactly 4 distinct sources",
            expectedSources,
            actualSources
        )
    }

    @Test
    fun `all fixture articles have HTTPS URLs`() {
        val provider = DebugEventFixtureProviderImpl()
        val fixture = provider.getReadAcrossFixture()

        fixture.forEach { article ->
            assertTrue(
                "URL should start with https://: ${article.url}",
                article.url.startsWith("https://")
            )
        }
    }

    @Test
    fun `all fixture URLs use test domain`() {
        val provider = DebugEventFixtureProviderImpl()
        val fixture = provider.getReadAcrossFixture()

        fixture.forEach { article ->
            assertTrue(
                "Fixture URL should use test.crosslens.fixture domain: ${article.url}",
                article.url.contains("test.crosslens.fixture")
            )
        }
    }

    @Test
    fun `all fixture articles have distinct URLs`() {
        val provider = DebugEventFixtureProviderImpl()
        val fixture = provider.getReadAcrossFixture()

        val urls = fixture.map { it.url }
        val distinctUrls = urls.distinct()

        assertEquals(
            "All URLs should be distinct",
            urls.size,
            distinctUrls.size
        )
    }

    @Test
    fun `all fixture articles have recent timestamps`() {
        val provider = DebugEventFixtureProviderImpl()
        val fixture = provider.getReadAcrossFixture()

        val now = java.time.Instant.now()
        val fourHoursAgo = now.minus(4, java.time.temporal.ChronoUnit.HOURS)

        fixture.forEach { article ->
            assertTrue(
                "Article timestamp should be within last 4 hours (for feed freshness): ${article.publishedAt}",
                article.publishedAt.isAfter(fourHoursAgo)
            )
            assertTrue(
                "Article timestamp should not be in the future: ${article.publishedAt}",
                article.publishedAt.isBefore(now.plus(1, java.time.temporal.ChronoUnit.MINUTES))
            )
        }
    }

    @Test
    fun `all fixture articles have headlines`() {
        val provider = DebugEventFixtureProviderImpl()
        val fixture = provider.getReadAcrossFixture()

        fixture.forEach { article ->
            assertFalse(
                "Headline should not be empty: ${article.sourceId}",
                article.headline.isBlank()
            )
            assertTrue(
                "Headline should be substantial (>20 chars): ${article.headline}",
                article.headline.length > 20
            )
        }
    }

    @Test
    fun `all fixture articles have excerpts`() {
        val provider = DebugEventFixtureProviderImpl()
        val fixture = provider.getReadAcrossFixture()

        fixture.forEach { article ->
            assertFalse(
                "Excerpt should not be empty: ${article.sourceId}",
                article.excerpt.isBlank()
            )
            assertTrue(
                "Excerpt should be substantial (>100 chars): ${article.sourceId}",
                article.excerpt.length > 100
            )
        }
    }

    @Test
    fun `fixture articles have chronological spread`() {
        val provider = DebugEventFixtureProviderImpl()
        val fixture = provider.getReadAcrossFixture()

        val timestamps = fixture.map { it.publishedAt }.sorted()
        val earliest = timestamps.first()
        val latest = timestamps.last()

        val spreadMinutes = java.time.Duration.between(earliest, latest).toMinutes()

        assertTrue(
            "Articles should have temporal spread (15+ minutes)",
            spreadMinutes >= 15
        )
        assertTrue(
            "Articles should be within reasonable window (< 2 hours)",
            spreadMinutes < 120
        )
    }

    @Test
    fun `fixture designed for Read Across Coverage testing`() {
        val provider = DebugEventFixtureProviderImpl()
        val fixture = provider.getReadAcrossFixture()

        // For event clustering to work:
        // - Need 2+ distinct publishers (have 4)
        // - Need shared entities and headline similarity
        // - Need articles within reasonable time window

        assertEquals(
            "Should have exactly 4 articles (minimum viable multi-source cluster)",
            4,
            fixture.size
        )

        val distinctPublishers = fixture.map { it.sourceId }.distinct().size
        assertTrue(
            "Should have 4 distinct publishers",
            distinctPublishers == 4
        )
    }

    @Test
    fun `fixture articles form one valid 4-publisher cluster with production algorithm`() {
        val provider = DebugEventFixtureProviderImpl()
        val fixture = provider.getReadAcrossFixture()

        // Use production EventClusteringService with exact same thresholds
        val clusteringService = EventClusteringService()
        val clusters = clusteringService.clusterArticles(fixture)

        // CRITICAL: Fixture must form exactly ONE cluster with all 4 articles
        assertEquals(
            "Fixture should form exactly 1 cluster (all articles about same event)",
            1,
            clusters.size
        )

        val cluster = clusters.first()
        assertEquals(
            "Cluster should contain all 4 fixture articles",
            4,
            cluster.articles.size
        )
        assertEquals(
            "Cluster should have 4 distinct publishers",
            4,
            cluster.publisherCount
        )
        assertTrue(
            "Cluster should be valid (2+ articles from 2+ publishers)",
            cluster.isValid
        )

        // Verify all fixture articles are in the cluster
        val clusterUrls = cluster.articles.map { it.url }.toSet()
        val fixtureUrls = fixture.map { it.url }.toSet()
        assertEquals(
            "All fixture articles should be in the cluster",
            fixtureUrls,
            clusterUrls
        )
    }

    @Test
    fun `fixture passes conservative clustering thresholds for all pairs`() {
        val provider = DebugEventFixtureProviderImpl()
        val fixture = provider.getReadAcrossFixture()

        val clusteringService = EventClusteringService()

        // Convert to ClusteredArticle for comparison
        val articles = fixture.map { record ->
            com.crosslens.app.core.model.ClusteredArticle(
                sourceId = record.sourceId,
                headline = record.headline,
                excerpt = record.excerpt,
                publishedAt = record.publishedAt,
                url = record.url,
                imageUrl = record.imageUrl,
                languageTag = record.languageTag,
                normalizedHeadline = clusteringService.normalizeHeadline(record.headline),
                entities = extractEntities(record.headline + " " + record.excerpt)
            )
        }

        // Verify every pair should cluster
        for (i in articles.indices) {
            for (j in i + 1 until articles.size) {
                val match = clusteringService.compareArticles(articles[i], articles[j])
                assertTrue(
                    "Articles ${articles[i].sourceId} and ${articles[j].sourceId} should cluster: ${match.explanation}",
                    match.shouldCluster
                )
                assertTrue(
                    "Match should have at least 2 shared entities: ${match.sharedEntities}",
                    match.sharedEntities >= 2
                )
                assertTrue(
                    "Match should have >= 20% headline similarity: ${"%.1f".format(match.headlineSimilarity * 100)}%",
                    match.headlineSimilarity >= 0.20
                )
            }
        }
    }

    // Helper method that mirrors EventClusteringService entity extraction
    private fun extractEntities(text: String): List<String> {
        val pattern = Regex("\\b[A-Z][a-z]+(?:\\s+[A-Z][a-z]+)*\\b")
        return pattern.findAll(text)
            .map { it.value }
            .filter { it.length > 3 }
            .distinct()
            .toList()
    }
}
