package com.crosslens.app.data.ingestion

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.SocketTimeoutException
import java.time.Duration
import java.time.Instant
import java.util.concurrent.TimeUnit

/**
 * RSS source adapter that fetches and parses RSS feeds.
 * Implements timeout, size limits, graceful error handling, and health monitoring.
 */
class RssSourceAdapter(
    override val sourceId: String,
    override val sourceName: String,
    private val feedUrl: String,
    private val httpClient: OkHttpClient,
    private val parser: RssParser = RssParser(),
    private val timeoutMillis: Long = 10_000L,
    private val healthMonitor: SourceHealthMonitor? = null
) : SourceAdapter {

    override suspend fun fetchArticles(): List<SourceArticleRecord> = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        var fetchSucceeded = false
        var parseSucceeded = false
        var parsedItems: List<RssFeedItem> = emptyList()
        var errorMessage: String? = null

        try {
            withTimeout(timeoutMillis) {
                val request = Request.Builder()
                    .url(feedUrl)
                    .header("User-Agent", "CrossLens/0.0.14-beta (Android; +https://crosslens.org)")
                    .build()

                val response = httpClient.newCall(request).execute()

                if (!response.isSuccessful) {
                    errorMessage = "HTTP ${response.code}: ${response.message}"
                    recordHealthCheck(startTime, fetchSucceeded, parseSucceeded, emptyList(), errorMessage)
                    return@withTimeout emptyList()
                }

                fetchSucceeded = true

                val body = response.body?.string()
                if (body == null) {
                    errorMessage = "Empty response body"
                    recordHealthCheck(startTime, fetchSucceeded, parseSucceeded, emptyList(), errorMessage)
                    return@withTimeout emptyList()
                }

                val parseResult = parser.parse(body, sourceId, sourceName)

                parseResult.onSuccess {
                    parsedItems = it
                    parseSucceeded = true
                }.onFailure {
                    errorMessage = "Parse error: ${it.message}"
                }

                if (!parseSucceeded) {
                    recordHealthCheck(startTime, fetchSucceeded, parseSucceeded, emptyList(), errorMessage)
                    return@withTimeout emptyList()
                }

                val articles = parsedItems.mapNotNull { item ->
                    // Filter out items without HTTPS links
                    if (!item.link.startsWith("https://")) {
                        null
                    } else {
                        SourceArticleRecord(
                            url = item.link,
                            publishedAt = item.pubDate ?: Instant.now(),
                            languageTag = inferLanguageFromSourceId(sourceId),
                            headline = item.title,
                            excerpt = item.description,
                            contentPermission = ContentPermission.EXPLICIT_EXCERPT,
                            imageUrl = item.imageUrl,
                            sourceId = sourceId  // ✅ SET SOURCE ID
                        )
                    }
                }

                recordHealthCheck(startTime, fetchSucceeded, parseSucceeded, articles, null)
                articles
            }
        } catch (e: SocketTimeoutException) {
            errorMessage = "Timeout after ${timeoutMillis}ms"
            recordHealthCheck(startTime, fetchSucceeded, parseSucceeded, emptyList(), errorMessage)
            emptyList()
        } catch (e: Exception) {
            errorMessage = "Exception: ${e.javaClass.simpleName}: ${e.message}"
            recordHealthCheck(startTime, fetchSucceeded, parseSucceeded, emptyList(), errorMessage)
            emptyList()
        }
    }

    private suspend fun recordHealthCheck(
        startTime: Long,
        fetchSucceeded: Boolean,
        parseSucceeded: Boolean,
        articles: List<SourceArticleRecord>,
        errorMessage: String?
    ) {
        healthMonitor?.recordCheck(
            SourceHealthCheck(
                sourceId = sourceId,
                checkedAt = Instant.now(),
                fetchSucceeded = fetchSucceeded,
                parseSucceeded = parseSucceeded,
                articlesReturned = articles.size,
                articlesWithValidDates = articles.count { it.publishedAt != null },
                articlesWithImages = articles.count { it.imageUrl != null },
                articlesWithHttpsLinks = articles.size, // All pass the HTTPS filter
                latestArticleAge = articles.maxOfOrNull {
                    Duration.between(it.publishedAt, Instant.now()).seconds
                },
                fetchDurationMs = System.currentTimeMillis() - startTime,
                errorMessage = errorMessage
            )
        )
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
            sourceId.contains("irishtimes") -> "en-IE"
            sourceId.contains("washingtonpost") -> "en-US"
            sourceId.contains("timesofindia") -> "en-IN"
            sourceId.contains("independent") -> "en-GB"
            sourceId.contains("straitstimes") -> "en-SG"
            sourceId.contains("koreaherald") -> "en"
            sourceId.contains("arabnews") -> "en"
            sourceId.contains("scmp") -> "en"
            sourceId.contains("lemonde") -> "fr"
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
         * Create the approved RSS source adapters with global coverage and health monitoring.
         * Currently: 22 active sources (Korea Herald disabled due to broken feed).
         */
        fun createApprovedSources(
            httpClient: OkHttpClient,
            healthMonitor: SourceHealthMonitor? = null
        ): List<RssSourceAdapter> {
            return listOf(
                // Core international sources (4)
                RssSourceAdapter(
                    sourceId = "bbc-news-rss",
                    sourceName = "BBC News",
                    feedUrl = "https://feeds.bbci.co.uk/news/rss.xml",
                    httpClient = httpClient,
                    healthMonitor = healthMonitor
                ),
                RssSourceAdapter(
                    sourceId = "aljazeera-rss",
                    sourceName = "Al Jazeera",
                    feedUrl = "https://www.aljazeera.com/xml/rss/all.xml",
                    httpClient = httpClient,
                    healthMonitor = healthMonitor
                ),
                RssSourceAdapter(
                    sourceId = "dw-rss",
                    sourceName = "Deutsche Welle",
                    feedUrl = "https://rss.dw.com/xml/rss-en-all",
                    httpClient = httpClient,
                    healthMonitor = healthMonitor
                ),
                RssSourceAdapter(
                    sourceId = "france24-rss",
                    sourceName = "France 24",
                    feedUrl = "https://www.france24.com/en/rss",
                    httpClient = httpClient,
                    healthMonitor = healthMonitor
                ),
                // Expanded sources - UK/Europe
                RssSourceAdapter(
                    sourceId = "guardian-rss",
                    sourceName = "The Guardian",
                    feedUrl = "https://www.theguardian.com/world/rss",
                    httpClient = httpClient,
                    healthMonitor = healthMonitor
                ),
                RssSourceAdapter(
                    sourceId = "spiegel-rss",
                    sourceName = "Der Spiegel International",
                    feedUrl = "https://www.spiegel.de/international/index.rss",
                    httpClient = httpClient,
                    healthMonitor = healthMonitor
                ),
                RssSourceAdapter(
                    sourceId = "swissinfo-rss",
                    sourceName = "swissinfo.ch",
                    feedUrl = "https://www.swissinfo.ch/eng/feed/",
                    httpClient = httpClient,
                    healthMonitor = healthMonitor
                ),
                RssSourceAdapter(
                    sourceId = "abc-es-rss",
                    sourceName = "ABC (Spain)",
                    feedUrl = "https://www.abc.es/rss/feeds/abc_Internacional.xml",
                    httpClient = httpClient,
                    healthMonitor = healthMonitor
                ),
                // North America
                RssSourceAdapter(
                    sourceId = "nytimes-rss",
                    sourceName = "The New York Times",
                    feedUrl = "https://rss.nytimes.com/services/xml/rss/nyt/World.xml",
                    httpClient = httpClient,
                    healthMonitor = healthMonitor
                ),
                RssSourceAdapter(
                    sourceId = "cbc-rss",
                    sourceName = "CBC News",
                    feedUrl = "https://www.cbc.ca/webfeed/rss/rss-topstories",
                    httpClient = httpClient,
                    healthMonitor = healthMonitor
                ),
                // Asia-Pacific
                RssSourceAdapter(
                    sourceId = "abc-au-rss",
                    sourceName = "ABC News (Australia)",
                    feedUrl = "https://www.abc.net.au/news/feed/51120/rss.xml",
                    httpClient = httpClient,
                    healthMonitor = healthMonitor
                ),
                RssSourceAdapter(
                    sourceId = "japantimes-rss",
                    sourceName = "The Japan Times",
                    feedUrl = "https://www.japantimes.co.jp/feed/",
                    httpClient = httpClient,
                    healthMonitor = healthMonitor
                ),
                RssSourceAdapter(
                    sourceId = "hindu-rss",
                    sourceName = "The Hindu",
                    feedUrl = "https://www.thehindu.com/news/national/feeder/default.rss",
                    httpClient = httpClient,
                    healthMonitor = healthMonitor
                ),
                RssSourceAdapter(
                    sourceId = "channelnewsasia-rss",
                    sourceName = "Channel NewsAsia",
                    feedUrl = "https://www.channelnewsasia.com/api/v1/rss-outbound-feed?_format=xml",
                    httpClient = httpClient,
                    healthMonitor = healthMonitor
                ),
                RssSourceAdapter(
                    sourceId = "asahi-rss",
                    sourceName = "朝日新聞 (Asahi Shimbun)",
                    feedUrl = "https://www.asahi.com/rss/asahi/newsheadlines.rdf",
                    httpClient = httpClient,
                    healthMonitor = healthMonitor
                ),
                // Batch 1 Expansion - Additional international coverage (7 active, 1 disabled)
                RssSourceAdapter(
                    sourceId = "irishtimes-rss",
                    sourceName = "The Irish Times",
                    feedUrl = "https://www.irishtimes.com/cmlink/news-1.1319192",
                    httpClient = httpClient,
                    healthMonitor = healthMonitor
                ),
                RssSourceAdapter(
                    sourceId = "washingtonpost-rss",
                    sourceName = "The Washington Post",
                    feedUrl = "https://feeds.washingtonpost.com/rss/world",
                    httpClient = httpClient,
                    healthMonitor = healthMonitor
                ),
                RssSourceAdapter(
                    sourceId = "timesofindia-rss",
                    sourceName = "The Times of India",
                    feedUrl = "https://timesofindia.indiatimes.com/rssfeedstopstories.cms",
                    httpClient = httpClient,
                    healthMonitor = healthMonitor
                ),
                RssSourceAdapter(
                    sourceId = "straitstimes-rss",
                    sourceName = "The Straits Times",
                    feedUrl = "https://www.straitstimes.com/news/world/rss.xml",
                    httpClient = httpClient,
                    healthMonitor = healthMonitor
                ),
                // Korea Herald DISABLED - feed returns HTML error page instead of RSS
                // RssSourceAdapter(
                //     sourceId = "koreaherald-rss",
                //     sourceName = "The Korea Herald",
                //     feedUrl = "http://www.koreaherald.com/common/newslist.xml?ct=020000000000",
                //     httpClient = httpClient,
                //     healthMonitor = healthMonitor
                // ),
                RssSourceAdapter(
                    sourceId = "arabnews-rss",
                    sourceName = "Arab News",
                    feedUrl = "https://www.arabnews.com/rss.xml",
                    httpClient = httpClient,
                    healthMonitor = healthMonitor
                ),
                RssSourceAdapter(
                    sourceId = "scmp-rss",
                    sourceName = "South China Morning Post",
                    feedUrl = "http://www.scmp.com/rss/91/feed", // HTTP: HTTPS redirects to HTTP, blocked by OkHttp
                    httpClient = httpClient,
                    healthMonitor = healthMonitor
                ),
                RssSourceAdapter(
                    sourceId = "lemonde-rss",
                    sourceName = "Le Monde",
                    feedUrl = "https://www.lemonde.fr/rss/une.xml",
                    httpClient = httpClient,
                    healthMonitor = healthMonitor
                )
            )
        }
    }
}
