package com.crosslens.app.data.ingestion

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * RSS 2.0 parser with robust error handling and size limits.
 * Supports standard RSS date formats (RFC 822, RFC 3339).
 */
class RssParser(
    private val maxItems: Int = 50,
    private val maxDescriptionLength: Int = 500
) {
    companion object {
        private const val MAX_FEED_SIZE_BYTES = 5 * 1024 * 1024 // 5MB

        // Common RSS/RFC 822 date formats
        private val RSS_DATE_FORMATTERS = listOf(
            DateTimeFormatter.RFC_1123_DATE_TIME,
            DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss Z"),
            DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss z"),
            DateTimeFormatter.ISO_INSTANT,
            DateTimeFormatter.ISO_OFFSET_DATE_TIME
        )
    }

    /**
     * Parse RSS feed XML into a list of RssFeedItem records.
     * Returns Result.failure for malformed XML or oversized feeds.
     */
    fun parse(xmlContent: String, sourceId: String, sourceName: String): Result<List<RssFeedItem>> {
        return runCatching {
            // Size check
            if (xmlContent.toByteArray().size > MAX_FEED_SIZE_BYTES) {
                throw IllegalArgumentException("Feed exceeds maximum size of ${MAX_FEED_SIZE_BYTES / 1024 / 1024}MB")
            }

            val factory = XmlPullParserFactory.newInstance()
            val parser = factory.newPullParser()
            parser.setInput(StringReader(xmlContent))

            val items = mutableListOf<RssFeedItem>()
            var eventType = parser.eventType

            var inItem = false
            var currentTitle: String? = null
            var currentLink: String? = null
            var currentDescription: String? = null
            var currentPubDate: String? = null
            var currentCategory: String? = null

            while (eventType != XmlPullParser.END_DOCUMENT && items.size < maxItems) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        when (parser.name?.lowercase()) {
                            "item" -> {
                                inItem = true
                                currentTitle = null
                                currentLink = null
                                currentDescription = null
                                currentPubDate = null
                                currentCategory = null
                            }
                            "title" -> {
                                if (inItem) {
                                    currentTitle = readText(parser)
                                }
                            }
                            "link" -> {
                                if (inItem) {
                                    currentLink = readText(parser)
                                }
                            }
                            "description" -> {
                                if (inItem) {
                                    currentDescription = readText(parser).take(maxDescriptionLength)
                                }
                            }
                            "pubdate" -> {
                                if (inItem) {
                                    currentPubDate = readText(parser)
                                }
                            }
                            "category" -> {
                                if (inItem && currentCategory == null) {
                                    currentCategory = readText(parser)
                                }
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (parser.name == "item" && inItem) {
                            // Validate required fields
                            if (!currentTitle.isNullOrBlank() && !currentLink.isNullOrBlank()) {
                                items.add(
                                    RssFeedItem(
                                        sourceId = sourceId,
                                        sourceName = sourceName,
                                        title = currentTitle.trim(),
                                        link = currentLink.trim(),
                                        description = currentDescription?.trim() ?: "",
                                        pubDate = parsePubDate(currentPubDate),
                                        category = currentCategory?.trim()
                                    )
                                )
                            }
                            inItem = false
                        }
                    }
                }
                eventType = parser.next()
            }

            items
        }
    }

    private fun readText(parser: XmlPullParser): String {
        var result = ""
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text ?: ""
            parser.nextTag()
        }
        return result
    }

    private fun parsePubDate(dateString: String?): Instant? {
        if (dateString.isNullOrBlank()) return null

        for (formatter in RSS_DATE_FORMATTERS) {
            try {
                return ZonedDateTime.parse(dateString.trim(), formatter).toInstant()
            } catch (e: DateTimeParseException) {
                continue
            }
        }

        // If all formatters fail, return null rather than throwing
        return null
    }
}

/**
 * Single RSS feed item with metadata.
 */
data class RssFeedItem(
    val sourceId: String,
    val sourceName: String,
    val title: String,
    val link: String,
    val description: String,
    val pubDate: Instant?,
    val category: String?
)
