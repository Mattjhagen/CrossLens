package com.crosslens.app.data.ingestion

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class EventClusteringPipelineTest {
    private val time = Instant.parse("2026-09-22T12:00:00Z")

    @Test fun `removes tracking parameters before duplicate detection`() {
        val result = EventClusteringPipeline().process(listOf(
            article("bbc", "https://news.example/story?id=42&utm_source=feed", "Climate summit reaches agreement"),
            article("mirror", "https://news.example/story?id=42&fbclid=ignored", "Climate summit reaches agreement")
        ))
        assertEquals(1, result.acceptedArticles.size)
        assertEquals(1, result.duplicates.size)
        assertEquals(DuplicateReason.CANONICAL_URL, result.duplicates.single().reason)
    }

    @Test fun `groups similar coverage from different sources into reviewable proposal`() {
        val result = EventClusteringPipeline().process(listOf(
            article("bbc", "https://bbc.example/climate", "Climate summit reaches agreement in Geneva"),
            article("lemonde", "https://lemonde.example/climat", "Geneva climate summit agreement reached")
        ))
        assertEquals(1, result.clusters.size)
        assertEquals(ClusterStatus.REVIEWABLE, result.clusters.single().status)
        assertEquals(2, result.clusters.single().sourceCount)
    }

    @Test fun `keeps unrelated coverage in separate clusters`() {
        val result = EventClusteringPipeline().process(listOf(
            article("bbc", "https://bbc.example/climate", "Climate summit reaches agreement in Geneva"),
            article("nyt", "https://nyt.example/markets", "Markets fall after central bank announcement")
        ))
        assertEquals(2, result.clusters.size)
        assertTrue(result.clusters.all { it.status == ClusterStatus.SINGLE_SOURCE })
    }

    private fun article(sourceId: String, url: String, headline: String) = IngestionArticleInput(
        sourceId = sourceId, sourceName = sourceId, url = url, publishedAt = time,
        languageTag = "en", headline = headline, excerpt = "Demo excerpt"
    )
}
