package com.crosslens.app.data.ingestion

import org.junit.Assert.assertThrows
import org.junit.Test
import java.time.Instant

class SourceAdapterValidatorTest {

    @Test
    fun `validates adapter with proper configuration`() {
        val adapter = TestAdapter("bbc", "BBC News")
        SourceAdapterValidator.validate(adapter) // Should not throw
    }

    @Test
    fun `rejects adapter with blank sourceId`() {
        val adapter = TestAdapter("", "BBC News")
        assertThrows(IllegalArgumentException::class.java) {
            SourceAdapterValidator.validate(adapter)
        }
    }

    @Test
    fun `rejects adapter with blank sourceName`() {
        val adapter = TestAdapter("bbc", "")
        assertThrows(IllegalArgumentException::class.java) {
            SourceAdapterValidator.validate(adapter)
        }
    }

    @Test
    fun `validates record with HTTPS URL`() {
        val record = SourceArticleRecord(
            url = "https://example.com/article",
            publishedAt = Instant.now(),
            languageTag = "en",
            headline = "Test headline",
            excerpt = "Test excerpt"
        )
        SourceAdapterValidator.validateRecord(record) // Should not throw
    }

    @Test
    fun `rejects record with HTTP URL`() {
        val record = SourceArticleRecord(
            url = "http://example.com/article",
            publishedAt = Instant.now(),
            languageTag = "en",
            headline = "Test headline",
            excerpt = "Test excerpt"
        )
        assertThrows(IllegalArgumentException::class.java) {
            SourceAdapterValidator.validateRecord(record)
        }
    }

    @Test
    fun `rejects record with blank headline`() {
        val record = SourceArticleRecord(
            url = "https://example.com/article",
            publishedAt = Instant.now(),
            languageTag = "en",
            headline = "",
            excerpt = "Test excerpt"
        )
        assertThrows(IllegalArgumentException::class.java) {
            SourceAdapterValidator.validateRecord(record)
        }
    }

    @Test
    fun `validates record with two-letter language tag`() {
        val record = SourceArticleRecord(
            url = "https://example.com/article",
            publishedAt = Instant.now(),
            languageTag = "en",
            headline = "Test",
            excerpt = "Test"
        )
        SourceAdapterValidator.validateRecord(record) // Should not throw
    }

    @Test
    fun `validates record with language and country tag`() {
        val record = SourceArticleRecord(
            url = "https://example.com/article",
            publishedAt = Instant.now(),
            languageTag = "en-GB",
            headline = "Test",
            excerpt = "Test"
        )
        SourceAdapterValidator.validateRecord(record) // Should not throw
    }

    @Test
    fun `rejects record with invalid language tag`() {
        val record = SourceArticleRecord(
            url = "https://example.com/article",
            publishedAt = Instant.now(),
            languageTag = "ENGLISH",
            headline = "Test",
            excerpt = "Test"
        )
        assertThrows(IllegalArgumentException::class.java) {
            SourceAdapterValidator.validateRecord(record)
        }
    }

    private class TestAdapter(
        override val sourceId: String,
        override val sourceName: String
    ) : SourceAdapter {
        override suspend fun fetchArticles(): List<SourceArticleRecord> = emptyList()
    }
}
