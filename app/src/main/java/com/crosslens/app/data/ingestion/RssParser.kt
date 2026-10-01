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
            factory.isNamespaceAware = true
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
            var currentImageUrl: String? = null

            while (eventType != XmlPullParser.END_DOCUMENT && items.size < maxItems) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        val tagName = parser.name?.lowercase()
                        val namespace = parser.namespace

                        when (tagName) {
                            "item" -> {
                                inItem = true
                                currentTitle = null
                                currentLink = null
                                currentDescription = null
                                currentPubDate = null
                                currentCategory = null
                                currentImageUrl = null
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
                                    val rawDescription = readText(parser)
                                    currentDescription = sanitizeHtml(rawDescription).take(maxDescriptionLength)
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
                            "enclosure" -> {
                                // Extract image from RSS enclosure tag
                                if (inItem && currentImageUrl == null) {
                                    val type = parser.getAttributeValue(null, "type")
                                    if (type?.startsWith("image/") == true) {
                                        val url = parser.getAttributeValue(null, "url")
                                        if (url?.startsWith("https://") == true) {
                                            currentImageUrl = url
                                        }
                                    }
                                }
                            }
                            "thumbnail" -> {
                                // Media RSS thumbnail (media:thumbnail)
                                // Example: <media:thumbnail url="https://..."/>
                                // Check if this is from the media namespace
                                val isMediaNamespace = namespace != null && namespace.contains("search.yahoo.com/mrss")
                                if (inItem && currentImageUrl == null && isMediaNamespace) {
                                    val url = parser.getAttributeValue(null, "url")
                                    if (url?.startsWith("https://") == true) {
                                        currentImageUrl = url
                                    }
                                }
                            }
                            "content" -> {
                                // Media RSS content (media:content)
                                // Examples:
                                // - <media:content url="https://..." width="700"/> (Guardian)
                                // - <media:content url="https://..." medium="image" height="1800"/> (NYTimes)
                                // Check if this is from the media namespace
                                val isMediaNamespace = namespace != null && namespace.contains("search.yahoo.com/mrss")
                                if (inItem && currentImageUrl == null && isMediaNamespace) {
                                    val url = parser.getAttributeValue(null, "url")
                                    if (url?.startsWith("https://") == true) {
                                        // Accept if explicitly marked as image, or if no type/medium specified
                                        val medium = parser.getAttributeValue(null, "medium")
                                        val type = parser.getAttributeValue(null, "type")

                                        val isExplicitImage = medium == "image" || type?.startsWith("image/") == true
                                        val hasNoTypeInfo = medium == null && type == null

                                        // Accept media:content with url if it's explicitly image or has no type info
                                        if (isExplicitImage || hasNoTypeInfo) {
                                            currentImageUrl = url
                                        }
                                    }
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
                                        category = currentCategory?.trim(),
                                        imageUrl = currentImageUrl?.trim()
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

    /**
     * Sanitize HTML content from RSS titles and descriptions.
     * Decodes nested/escaped entities, then removes tags to produce clean plain text.
     * Safe for JVM unit tests - no Android dependencies.
     */
    private fun sanitizeHtml(html: String): String {
        if (html.isBlank()) return ""

        return try {
            var text = html

            // Decode HTML entities first (up to 3 passes for nested/double-escaped content)
            // Example: &amp;lt;i&amp;gt; → &lt;i&gt; → <i>
            for (i in 0 until 3) {
                val decoded = decodeHtmlEntities(text)
                if (decoded == text) break // No more changes
                text = decoded
            }

            // Remove script, style, and noscript blocks with their content
            text = safeReplace(text, "<script[^>]*>.*?</script>", "", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
            text = safeReplace(text, "<style[^>]*>.*?</style>", "", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
            text = safeReplace(text, "<noscript[^>]*>.*?</noscript>", "", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))

            // Remove HTML comments
            text = safeReplace(text, "<!--.*?-->", "", setOf(RegexOption.DOT_MATCHES_ALL))

            // Convert block-level tags to spaces before removing (preserves readability)
            val blockTags = listOf("p", "div", "br", "hr", "h1", "h2", "h3", "h4", "h5", "h6",
                                   "li", "tr", "td", "th", "blockquote", "pre", "article", "section")
            for (tag in blockTags) {
                text = safeReplace(text, "<$tag[^>]*>", " ", setOf(RegexOption.IGNORE_CASE))
                text = safeReplace(text, "</$tag>", " ", setOf(RegexOption.IGNORE_CASE))
            }

            // Remove all remaining HTML tags (inline tags like <i>, <b>, <span>, etc.)
            text = safeReplace(text, "<[^>]+>", "")

            // Handle escaped backslash sequences like \<p\> (some feeds double-escape)
            text = safeReplace(text, """\\<[^>]*\\>""", " ")
            text = safeReplace(text, """\\<[^>]*>""", " ")
            text = safeReplace(text, """<[^>]*\\>""", " ")

            // Normalize whitespace
            text = safeReplace(text, "\\s+", " ").trim()

            text
        } catch (e: Exception) {
            // If sanitization fails completely, return trimmed original
            html.trim()
        }
    }

    /**
     * Safely replace regex pattern, catching any regex exceptions.
     */
    private fun safeReplace(input: String, pattern: String, replacement: String, options: Set<RegexOption> = emptySet()): String {
        return try {
            input.replace(Regex(pattern, options), replacement)
        } catch (e: Exception) {
            input
        }
    }

    /**
     * Decode common HTML entities to their character equivalents.
     */
    private fun decodeHtmlEntities(text: String): String {
        return try {
            var decoded = text

            // Named entities (most common first)
            val namedEntities = mapOf(
                "&amp;" to "&",
                "&lt;" to "<",
                "&gt;" to ">",
                "&quot;" to "\"",
                "&apos;" to "'",
                "&nbsp;" to " ",
                "&ndash;" to "–",
                "&mdash;" to "—",
                "&hellip;" to "…",
                "&lsquo;" to "'",
                "&rsquo;" to "'",
                "&ldquo;" to """,
                "&rdquo;" to """,
                "&bull;" to "•",
                "&middot;" to "·",
                "&copy;" to "©",
                "&reg;" to "®",
                "&trade;" to "™",
                "&euro;" to "€",
                "&pound;" to "£",
                "&yen;" to "¥"
            )

            for ((entity, char) in namedEntities) {
                decoded = decoded.replace(entity, char, ignoreCase = true)
            }

            // Decode numeric entities (&#123; and &#xAB;)
            decoded = safeReplace(decoded, "&#(\\d+);") { matchResult ->
                val code = matchResult.groupValues[1].toIntOrNull()
                if (code != null && code in 32..65535) { // Valid Char range, excluding control chars
                    try {
                        code.toChar().toString()
                    } catch (e: Exception) {
                        matchResult.value
                    }
                } else {
                    matchResult.value
                }
            }

            decoded = safeReplace(decoded, "&#[xX]([0-9a-fA-F]+);") { matchResult ->
                val code = matchResult.groupValues[1].toIntOrNull(16)
                if (code != null && code in 32..65535) { // Valid Char range, excluding control chars
                    try {
                        code.toChar().toString()
                    } catch (e: Exception) {
                        matchResult.value
                    }
                } else {
                    matchResult.value
                }
            }

            decoded
        } catch (e: Exception) {
            text
        }
    }

    /**
     * Safely replace regex with transform function.
     */
    private fun safeReplace(input: String, pattern: String, transform: (MatchResult) -> CharSequence): String {
        return try {
            input.replace(Regex(pattern), transform)
        } catch (e: Exception) {
            input
        }
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
    val category: String?,
    val imageUrl: String? // HTTPS URL to article image from enclosure or media:content
)
