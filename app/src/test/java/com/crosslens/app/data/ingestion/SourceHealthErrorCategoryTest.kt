package com.crosslens.app.data.ingestion

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Tests for SourceHealthErrorCategory classification.
 * Verifies that error messages are correctly categorized based on technical evidence only.
 */
class SourceHealthErrorCategoryTest {

    @Test
    fun `categorizes timeout errors as NETWORK_ERROR`() {
        assertEquals(
            SourceHealthErrorCategory.NETWORK_ERROR,
            categorizeError("Timeout after 10000ms")
        )
        assertEquals(
            SourceHealthErrorCategory.NETWORK_ERROR,
            categorizeError("SocketTimeoutException: timeout")
        )
    }

    @Test
    fun `categorizes connection errors as NETWORK_ERROR`() {
        assertEquals(
            SourceHealthErrorCategory.NETWORK_ERROR,
            categorizeError("Connection refused")
        )
        assertEquals(
            SourceHealthErrorCategory.NETWORK_ERROR,
            categorizeError("UnreachableHostException: unable to connect")
        )
    }

    @Test
    fun `categorizes HTTP errors correctly`() {
        assertEquals(
            SourceHealthErrorCategory.HTTP_ERROR,
            categorizeError("HTTP 404: Not Found")
        )
        assertEquals(
            SourceHealthErrorCategory.HTTP_ERROR,
            categorizeError("HTTP 500: Internal Server Error")
        )
    }

    @Test
    fun `categorizes parse errors correctly`() {
        assertEquals(
            SourceHealthErrorCategory.PARSE_ERROR,
            categorizeError("Parse error: malformed XML")
        )
        assertEquals(
            SourceHealthErrorCategory.PARSE_ERROR,
            categorizeError("XML parsing failed")
        )
    }

    @Test
    fun `categorizes empty feed errors correctly`() {
        assertEquals(
            SourceHealthErrorCategory.EMPTY_FEED,
            categorizeError("Empty feed returned")
        )
    }

    @Test
    fun `categorizes duplicate-only errors correctly`() {
        assertEquals(
            SourceHealthErrorCategory.DUPLICATE_ONLY,
            categorizeError("All articles are duplicates")
        )
    }

    @Test
    fun `categorizes stale feed errors correctly`() {
        assertEquals(
            SourceHealthErrorCategory.STALE_FEED,
            categorizeError("Stale feed - no new articles")
        )
    }

    @Test
    fun `categorizes date errors correctly`() {
        assertEquals(
            SourceHealthErrorCategory.INVALID_DATES,
            categorizeError("Invalid date format")
        )
        assertEquals(
            SourceHealthErrorCategory.INVALID_DATES,
            categorizeError("Missing date in articles")
        )
    }

    @Test
    fun `categorizes attribution errors correctly`() {
        assertEquals(
            SourceHealthErrorCategory.ATTRIBUTION_ERROR,
            categorizeError("Missing attribution")
        )
        assertEquals(
            SourceHealthErrorCategory.ATTRIBUTION_ERROR,
            categorizeError("HTTPS link required")
        )
    }

    @Test
    fun `returns UNKNOWN for null error message`() {
        assertEquals(
            SourceHealthErrorCategory.UNKNOWN,
            categorizeError(null)
        )
    }

    @Test
    fun `returns UNKNOWN for unrecognized errors`() {
        assertEquals(
            SourceHealthErrorCategory.UNKNOWN,
            categorizeError("Something went wrong")
        )
    }

    @Test
    fun `categorization is case-insensitive`() {
        assertEquals(
            SourceHealthErrorCategory.NETWORK_ERROR,
            categorizeError("TIMEOUT after 5000ms")
        )
        assertEquals(
            SourceHealthErrorCategory.HTTP_ERROR,
            categorizeError("http 404: not found")
        )
    }

    @Test
    fun `never categorizes based on non-technical factors`() {
        // These should NOT be special categories - they're UNKNOWN
        assertEquals(
            SourceHealthErrorCategory.UNKNOWN,
            categorizeError("Biased source detected")
        )
        assertEquals(
            SourceHealthErrorCategory.UNKNOWN,
            categorizeError("Low quality content")
        )
        assertEquals(
            SourceHealthErrorCategory.UNKNOWN,
            categorizeError("Unpopular source")
        )
        assertEquals(
            SourceHealthErrorCategory.UNKNOWN,
            categorizeError("Political orientation mismatch")
        )
    }
}
