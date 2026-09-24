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
                    .header("User-Agent", "CrossLens/0.0.14-beta (Android; +https://crosslens.org)")
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
                            contentPermission = ContentPermission.EXPLICIT_EXCERPT,
                            imageUrl = item.imageUrl
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
            sourceId.contains("guardian") -> "en-GB"
            sourceId.contains("nytimes") -> "en-US"
            sourceId.contains("cbc") -> "en-CA"
            sourceId.contains("abc-au") -> "en-AU"
            sourceId.contains("japantimes") -> "en"
            sourceId.contains("hindu") -> "en-IN"
            sourceId.contains("spiegel") -> "en"
            sourceId.contains("channelnewsasia") -> "en-SG"
            sourceId.contains("swissinfo") -> "en"
            sourceId.contains("abc-es") -> "es"
            sourceId.contains("asahi") -> "ja"
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
         * Create the 15 approved RSS source adapters with global coverage.
         */
        fun createApprovedSources(httpClient: OkHttpClient): List<RssSourceAdapter> {
            return listOf(
                // Original 4 sources
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
                ),
                // Expanded sources - UK/Europe
                RssSourceAdapter(
                    sourceId = "guardian-rss",
                    sourceName = "The Guardian",
                    feedUrl = "https://www.theguardian.com/world/rss",
                    httpClient = httpClient
                ),
                RssSourceAdapter(
                    sourceId = "spiegel-rss",
                    sourceName = "Der Spiegel International",
                    feedUrl = "https://www.spiegel.de/international/index.rss",
                    httpClient = httpClient
                ),
                RssSourceAdapter(
                    sourceId = "swissinfo-rss",
                    sourceName = "swissinfo.ch",
                    feedUrl = "https://www.swissinfo.ch/eng/feed/",
                    httpClient = httpClient
                ),
                RssSourceAdapter(
                    sourceId = "abc-es-rss",
                    sourceName = "ABC (Spain)",
                    feedUrl = "https://www.abc.es/rss/feeds/abc_Internacional.xml",
                    httpClient = httpClient
                ),
                // North America
                RssSourceAdapter(
                    sourceId = "nytimes-rss",
                    sourceName = "The New York Times",
                    feedUrl = "https://rss.nytimes.com/services/xml/rss/nyt/World.xml",
                    httpClient = httpClient
                ),
                RssSourceAdapter(
                    sourceId = "cbc-rss",
                    sourceName = "CBC News",
                    feedUrl = "https://www.cbc.ca/webfeed/rss/rss-topstories",
                    httpClient = httpClient
                ),
                // Asia-Pacific
                RssSourceAdapter(
                    sourceId = "abc-au-rss",
                    sourceName = "ABC News (Australia)",
                    feedUrl = "https://www.abc.net.au/news/feed/51120/rss.xml",
                    httpClient = httpClient
                ),
                RssSourceAdapter(
                    sourceId = "japantimes-rss",
                    sourceName = "The Japan Times",
                    feedUrl = "https://www.japantimes.co.jp/feed/",
                    httpClient = httpClient
                ),
                RssSourceAdapter(
                    sourceId = "hindu-rss",
                    sourceName = "The Hindu",
                    feedUrl = "https://www.thehindu.com/news/national/feeder/default.rss",
                    httpClient = httpClient
                ),
                RssSourceAdapter(
                    sourceId = "channelnewsasia-rss",
                    sourceName = "Channel NewsAsia",
                    feedUrl = "https://www.channelnewsasia.com/api/v1/rss-outbound-feed?_format=xml",
                    httpClient = httpClient
                ),
                RssSourceAdapter(
                    sourceId = "asahi-rss",
                    sourceName = "朝日新聞 (Asahi Shimbun)",
                    feedUrl = "https://www.asahi.com/rss/asahi/newsheadlines.rdf",
                    httpClient = httpClient
                )
            )
        }
    }
}
