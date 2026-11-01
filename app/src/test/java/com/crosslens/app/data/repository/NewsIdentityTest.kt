package com.crosslens.app.data.repository

import org.junit.Assert.*
import org.junit.Test

class NewsIdentityTest {
    @Test fun `tracking and fragment changes retain article identity`() {
        assertEquals(NewsIdentity.articleId("https://EXAMPLE.com/a?utm_source=rss&id=42#top"),
            NewsIdentity.articleId("https://example.com/a?id=42"))
    }
    @Test fun `meaningful query and path case remain distinct`() {
        assertNotEquals(NewsIdentity.articleId("https://example.com/a?id=1"), NewsIdentity.articleId("https://example.com/a?id=2"))
        assertNotEquals(NewsIdentity.articleId("https://example.com/A"), NewsIdentity.articleId("https://example.com/a"))
    }
    @Test fun `cluster identity is independent of fetch order and duplicates`() {
        val a = "https://one.example/a"
        val b = "https://two.example/b"
        assertEquals(NewsIdentity.storyId(listOf(a,b)), NewsIdentity.storyId(listOf(b,a,a)))
    }
    @Test fun `saved cluster article relationships have distinct row identities`() {
        assertNotEquals(NewsIdentity.articleId("https://example.com/a", "old"),
            NewsIdentity.articleId("https://example.com/a", "new"))
    }
}
