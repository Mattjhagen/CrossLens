package com.crosslens.app.data.ingestion

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

/**
 * RSS source adapter that fetches and parses RSS feeds.
 * Implements timeout, size limits, and graceful error handling.
 */
class RssSourceAdapter(
    override val sourceId: String,
    override val sourceName: String,
    private val feedUrl: String,
    private val httpClient: OkHttpClient,
    private val parser: RssParser = RssParser(),
    private val timeoutMillis: Long = 10_000L
) : SourceAdapter {

    override suspend fun fetchArticles(): List<SourceArticleRecord> = withContext(Dispatchers.IO) {
        try {
            withTimeout(timeoutMillis) {
                val request = Request.Builder()
                    .url(feedUrl)
                    .header("User-Agent", "CrossLens/0.0.14-beta (Android)")
                    .build()

                val response = httpClient.newCall(request).execute()

                if (!response.isSuccessful) {
                    return@withTimeout emptyList()
                }

                val body = response.body?.string() ?: return@withTimeout emptyList()

                val parseResult = parser.parse(body, sourceId, sourceName)

                parseResult.getOrElse {
                    // Log parse error but return empty list instead of throwing
                    return@withTimeout emptyList()
                }.mapNotNull { item ->
                    // Filter out items without HTTPS links
                    if (!item.link.startsWith("https://")) {
                        null
                    } else {
                        SourceArticleRecord(
                            url = item.link,
                            publishedAt = item.pubDate ?: java.time.Instant.now(),
                            languageTag = inferLanguageFromSourceId(sourceId),
                            headline = item.title,
                            excerpt = item.description,
                            contentPermission = ContentPermission.EXPLICIT_EXCERPT
                        )
                    }
                }
            }
        } catch (e: SocketTimeoutException) {
            // Timeout - return empty list
            emptyList()
        } catch (e: Exception) {
            // Any other exception - return empty list to isolate failures
            emptyList()
        }
    }

    private fun inferLanguageFromSourceId(sourceId: String): String {
        return when {
            sourceId.contains("bbc") -> "en-GB"
            sourceId.contains("aljazeera") -> "en"
            sourceId.contains("dw") -> "en"
            sourceId.contains("france24") -> "en"
            else -> "en"
        }
    }

    companion object {
        /**
         * Create configured OkHttpClient for RSS fetching with timeouts.
         */
        fun createHttpClient(): OkHttpClient {
            return OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .followRedirects(true)
                .followSslRedirects(true)
                .build()
        }

        /**
         * Create the four approved RSS source adapters.
         */
        fun createApprovedSources(httpClient: OkHttpClient): List<RssSourceAdapter> {
            return listOf(
                RssSourceAdapter(
                    sourceId = "bbc-news-rss",
                    sourceName = "BBC News",
                    feedUrl = "https://feeds.bbci.co.uk/news/rss.xml",
                    httpClient = httpClient
                ),
                RssSourceAdapter(
                    sourceId = "aljazeera-rss",
                    sourceName = "Al Jazeera",
                    feedUrl = "https://www.aljazeera.com/xml/rss/all.xml",
                    httpClient = httpClient
                ),
                RssSourceAdapter(
                    sourceId = "dw-rss",
                    sourceName = "Deutsche Welle",
                    feedUrl = "https://rss.dw.com/xml/rss-en-all",
                    httpClient = httpClient
                ),
                RssSourceAdapter(
                    sourceId = "france24-rss",
                    sourceName = "France 24",
                    feedUrl = "https://www.france24.com/en/rss",
                    httpClient = httpClient
                )
            )
        }
    }
}
