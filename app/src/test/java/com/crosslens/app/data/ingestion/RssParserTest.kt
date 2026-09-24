package com.crosslens.app.data.ingestion

import org.junit.Test
import org.junit.Assert.*

class RssParserTest {

    private val parser = RssParser()

    @Test
    fun `parse valid RSS feed`() {
        val validRss = """
            <?xml version="1.0" encoding="UTF-8"?>
            <rss version="2.0">
                <channel>
                    <title>Test Feed</title>
                    <item>
                        <title>Test Article 1</title>
                        <link>https://example.com/article1</link>
                        <description>This is a test article</description>
                        <pubDate>Thu, 24 Sep 2026 12:00:00 GMT</pubDate>
                        <category>News</category>
                    </item>
                    <item>
                        <title>Test Article 2</title>
                        <link>https://example.com/article2</link>
                        <description>Another test article</description>
                        <pubDate>Thu, 24 Sep 2026 13:00:00 GMT</pubDate>
                    </item>
                </channel>
            </rss>
        """.trimIndent()

        val result = parser.parse(validRss, "test-source", "Test Source")

        assertTrue(result.isSuccess)
        val items = result.getOrThrow()
        assertEquals(2, items.size)
        assertEquals("Test Article 1", items[0].title)
        assertEquals("https://example.com/article1", items[0].link)
        assertEquals("This is a test article", items[0].description)
        assertEquals("News", items[0].category)
        assertNotNull(items[0].pubDate)
    }

    @Test
    fun `parse RSS with missing pubDate`() {
        val rssWithoutDate = """
            <?xml version="1.0" encoding="UTF-8"?>
            <rss version="2.0">
                <channel>
                    <item>
                        <title>Article Without Date</title>
                        <link>https://example.com/article</link>
                        <description>No publication date</description>
                    </item>
                </channel>
            </rss>
        """.trimIndent()

        val result = parser.parse(rssWithoutDate, "test-source", "Test Source")

        assertTrue(result.isSuccess)
        val items = result.getOrThrow()
        assertEquals(1, items.size)
        assertNull(items[0].pubDate)
    }

    @Test
    fun `parse RSS with missing required fields`() {
        val invalidRss = """
            <?xml version="1.0" encoding="UTF-8"?>
            <rss version="2.0">
                <channel>
                    <item>
                        <description>Missing title and link</description>
                    </item>
                    <item>
                        <title>Missing Link</title>
                    </item>
                    <item>
                        <link>https://example.com/missing-title</link>
                    </item>
                </channel>
            </rss>
        """.trimIndent()

        val result = parser.parse(invalidRss, "test-source", "Test Source")

        assertTrue(result.isSuccess)
        val items = result.getOrThrow()
        // All items should be filtered out due to missing required fields
        assertEquals(0, items.size)
    }

    @Test
    fun `parse malformed XML`() {
        val malformedXml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <rss version="2.0">
                <channel>
                    <item>
                        <title>Unclosed tag
                        <link>https://example.com/article</link>
                    </item>
        """.trimIndent()

        val result = parser.parse(malformedXml, "test-source", "Test Source")

        assertTrue(result.isFailure)
    }

    @Test
    fun `parse RSS with CDATA sections`() {
        val rssWithCData = """
            <?xml version="1.0" encoding="UTF-8"?>
            <rss version="2.0">
                <channel>
                    <item>
                        <title><![CDATA[Article with CDATA]]></title>
                        <link>https://example.com/article</link>
                        <description><![CDATA[Description with <b>HTML</b>]]></description>
                        <pubDate>Thu, 24 Sep 2026 12:00:00 GMT</pubDate>
                    </item>
                </channel>
            </rss>
        """.trimIndent()

        val result = parser.parse(rssWithCData, "test-source", "Test Source")

        assertTrue(result.isSuccess)
        val items = result.getOrThrow()
        assertEquals(1, items.size)
        assertEquals("Article with CDATA", items[0].title)
    }

    @Test
    fun `parse RSS with duplicate items`() {
        val rssWithDuplicates = """
            <?xml version="1.0" encoding="UTF-8"?>
            <rss version="2.0">
                <channel>
                    <item>
                        <title>Same Article</title>
                        <link>https://example.com/article</link>
                        <description>First occurrence</description>
                        <pubDate>Thu, 24 Sep 2026 12:00:00 GMT</pubDate>
                    </item>
                    <item>
                        <title>Same Article</title>
                        <link>https://example.com/article</link>
                        <description>Second occurrence</description>
                        <pubDate>Thu, 24 Sep 2026 12:00:00 GMT</pubDate>
                    </item>
                </channel>
            </rss>
        """.trimIndent()

        val result = parser.parse(rssWithDuplicates, "test-source", "Test Source")

        assertTrue(result.isSuccess)
        val items = result.getOrThrow()
        // Parser doesn't deduplicate - that's repository's job
        assertEquals(2, items.size)
    }

    @Test
    fun `parse RSS respects max items limit`() {
        val manyItems = buildString {
            appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")
            appendLine("""<rss version="2.0"><channel>""")
            repeat(100) { index ->
                appendLine("""<item>""")
                appendLine("""<title>Article $index</title>""")
                appendLine("""<link>https://example.com/article$index</link>""")
                appendLine("""<description>Description $index</description>""")
                appendLine("""</item>""")
            }
            appendLine("""</channel></rss>""")
        }

        val result = parser.parse(manyItems, "test-source", "Test Source")

        assertTrue(result.isSuccess)
        val items = result.getOrThrow()
        // Should respect maxItems limit (50 by default)
        assertEquals(50, items.size)
    }

    @Test
    fun `parse RSS truncates long descriptions`() {
        val longDescription = "A".repeat(1000)
        val rss = """
            <?xml version="1.0" encoding="UTF-8"?>
            <rss version="2.0">
                <channel>
                    <item>
                        <title>Article</title>
                        <link>https://example.com/article</link>
                        <description>$longDescription</description>
                    </item>
                </channel>
            </rss>
        """.trimIndent()

        val result = parser.parse(rss, "test-source", "Test Source")

        assertTrue(result.isSuccess)
        val items = result.getOrThrow()
        // Description should be truncated to maxDescriptionLength (500 by default)
        assertEquals(500, items[0].description.length)
    }

    @Test
    fun `parse empty RSS feed`() {
        val emptyRss = """
            <?xml version="1.0" encoding="UTF-8"?>
            <rss version="2.0">
                <channel>
                    <title>Empty Feed</title>
                </channel>
            </rss>
        """.trimIndent()

        val result = parser.parse(emptyRss, "test-source", "Test Source")

        assertTrue(result.isSuccess)
        val items = result.getOrThrow()
        assertEquals(0, items.size)
    }
}
