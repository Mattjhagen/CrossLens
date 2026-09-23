package com.crosslens.app.data.ingestion

import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant

class MockSourceAdapterTest {

    private val baseTime = Instant.parse("2026-09-22T12:00:00Z")

    @Test
    fun `BBC adapter produces valid climate article`() = runTest {
        val adapter = MockSourceAdapter.createBBC(baseTime)

        assertEquals("bbc-demo", adapter.sourceId)
        assertEquals("BBC News Demo", adapter.sourceName)

        val articles = adapter.fetchArticles()
        assertEquals(1, articles.size)

        val article = articles.first()
        assertTrue(article.url.startsWith("https://"))
        assertTrue(article.headline.contains("climate", ignoreCase = true))
        assertTrue(article.headline.contains("Geneva", ignoreCase = true))
        assertEquals("en", article.languageTag)
        assertEquals(ContentPermission.EXPLICIT_EXCERPT, article.contentPermission)

        // Validate passes
        SourceAdapterValidator.validateRecord(article)
    }

    @Test
    fun `Le Monde adapter produces valid French article`() = runTest {
        val adapter = MockSourceAdapter.createLeMonde(baseTime)

        assertEquals("lemonde-demo", adapter.sourceId)
        assertEquals("Le Monde Demo", adapter.sourceName)

        val articles = adapter.fetchArticles()
        assertEquals(1, articles.size)

        val article = articles.first()
        assertTrue(article.url.startsWith("https://"))
        assertEquals("fr", article.languageTag)
        assertTrue(article.headline.contains("Genève", ignoreCase = true))

        SourceAdapterValidator.validateRecord(article)
    }

    @Test
    fun `Al Jazeera adapter produces valid Arabic article`() = runTest {
        val adapter = MockSourceAdapter.createAlJazeera(baseTime)

        assertEquals("aljazeera-demo", adapter.sourceId)
        assertEquals("Al Jazeera Demo", adapter.sourceName)

        val articles = adapter.fetchArticles()
        assertEquals(1, articles.size)

        val article = articles.first()
        assertTrue(article.url.startsWith("https://"))
        assertEquals("ar", article.languageTag)
        assertTrue(article.headline.contains("جنيف"))

        SourceAdapterValidator.validateRecord(article)
    }

    @Test
    fun `NYT adapter produces unrelated tech article`() = runTest {
        val adapter = MockSourceAdapter.createNYT(baseTime)

        assertEquals("nyt-demo", adapter.sourceId)
        assertEquals("NYT Demo", adapter.sourceName)

        val articles = adapter.fetchArticles()
        assertEquals(1, articles.size)

        val article = articles.first()
        assertTrue(article.url.startsWith("https://"))
        assertTrue(article.headline.contains("AI", ignoreCase = true))
        assertEquals(ContentPermission.FAIR_USE_PREVIEW, article.contentPermission)

        SourceAdapterValidator.validateRecord(article)
    }

    @Test
    fun `test set produces four adapters with distinct content`() = runTest {
        val adapters = MockSourceAdapter.createTestSet(baseTime)

        assertEquals(4, adapters.size)
        assertEquals(setOf("bbc-demo", "lemonde-demo", "aljazeera-demo", "nyt-demo"), adapters.map { it.sourceId }.toSet())

        val allArticles = mutableListOf<SourceArticleRecord>()
        adapters.forEach { allArticles += it.fetchArticles() }
        assertEquals(4, allArticles.size)

        // Verify all URLs are distinct
        assertEquals(4, allArticles.map { it.url }.toSet().size)

        // Verify language diversity
        val languages = allArticles.map { it.languageTag }.toSet()
        assertTrue(languages.contains("en"))
        assertTrue(languages.contains("fr"))
        assertTrue(languages.contains("ar"))
    }

    @Test
    fun `adapters respect publication time ordering`() = runTest {
        val adapters = MockSourceAdapter.createTestSet(baseTime)
        val allArticles = mutableListOf<SourceArticleRecord>()
        adapters.forEach { allArticles += it.fetchArticles() }

        // BBC and NYT should be at baseTime
        val bbcArticle = allArticles.first { it.url.contains("bbc") }
        val nytArticle = allArticles.first { it.url.contains("nytimes") }
        assertEquals(baseTime, bbcArticle.publishedAt)
        assertEquals(baseTime, nytArticle.publishedAt)

        // Le Monde should be 1 hour later
        val lemondeArticle = allArticles.first { it.url.contains("lemonde") }
        assertEquals(baseTime.plusSeconds(3600), lemondeArticle.publishedAt)

        // Al Jazeera should be 2 hours later
        val alJazeeraArticle = allArticles.first { it.url.contains("aljazeera") }
        assertEquals(baseTime.plusSeconds(7200), alJazeeraArticle.publishedAt)
    }
}
