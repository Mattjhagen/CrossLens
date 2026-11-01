package com.crosslens.app.data.ingestion

import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant
import java.util.Locale

class ProductionFeedTest {
    private val parser = RssParser()

    @Test fun `declared language tags support script subtags and unknown without guessing`() {
        assertEquals("zh-Hant-TW", LanguageTags.normalize("zh-Hant-TW"))
        assertEquals("und", LanguageTags.normalize("und"))
        assertNull(LanguageTags.normalize("not_a_language"))
    }

    @Test fun `Atom alternate links language and publication timestamp are preserved`() {
        val result = parser.parse("""<feed xmlns="http://www.w3.org/2005/Atom" xml:lang="ar"><entry>
            <title>Original headline</title><link rel="self" href="https://publisher.example/api/1"/>
            <link rel="alternate" href="https://publisher.example/1"/><summary>Attributed excerpt</summary>
            <published>2026-10-31T12:00:00Z</published><updated>2026-11-01T12:00:00Z</updated>
            </entry></feed>""", "publisher", "Publisher").getOrThrow().single()
        assertEquals("https://publisher.example/1", result.link)
        assertEquals("ar", result.languageTag)
        assertEquals(Instant.parse("2026-10-31T12:00:00Z"), result.pubDate)
    }

    @Test fun `DTD and entity declarations are rejected`() {
        assertTrue(parser.parse("""<!DOCTYPE rss [<!ENTITY leak SYSTEM "file:///etc/passwd">]><rss/>""", "id", "name").isFailure)
    }

    @Test fun `RSS dates parse independently of device language`() {
        val previous = Locale.getDefault()
        try {
            Locale.setDefault(Locale.JAPANESE)
            assertNotNull(parser.parse("""<rss><channel><item><title>Headline</title><link>https://publisher.example/a</link>
                <pubDate>Sat, 31 Oct 2026 12:00:00 +0000</pubDate></item></channel></rss>""", "id", "name").getOrThrow().single().pubDate)
        } finally { Locale.setDefault(previous) }
    }

    @Test fun `undated feed items never get fabricated publication timestamps`() = runTest {
        val client = OkHttpClient.Builder().addInterceptor { chain ->
            Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1).code(200).message("OK")
                .body("""<rss><channel><item><title>No date</title><link>https://publisher.example/a</link></item></channel></rss>""".toResponseBody()).build()
        }.build()
        try {
            assertTrue(RssSourceAdapter("id", "name", "https://publisher.example/rss", client).fetchArticles().isEmpty())
        } finally { client.dispatcher.executorService.shutdown(); client.connectionPool.evictAll() }
    }

    @Test fun `oversized response is rejected before parsing`() = runTest {
        val client = OkHttpClient.Builder().addInterceptor { chain ->
            Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1).code(200).message("OK")
                .body("x".repeat(5 * 1024 * 1024 + 1).toResponseBody()).build()
        }.build()
        try {
            assertTrue(RssSourceAdapter("id", "name", "https://publisher.example/rss", client).fetchArticles().isEmpty())
        } finally { client.dispatcher.executorService.shutdown(); client.connectionPool.evictAll() }
    }
}
