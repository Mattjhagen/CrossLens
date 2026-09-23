package com.crosslens.app.data.repository

import com.crosslens.app.core.model.Article
import com.crosslens.app.core.model.ContentUseMetadata
import com.crosslens.app.core.model.Source
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.Instant

class SourceDigestGeneratorTest {

    private lateinit var generator: SourceDigestGenerator

    @Before
    fun setup() {
        generator = SourceDigestGenerator()
    }

    @Test
    fun `generateDigest with multiple sources produces agreements and differences`() {
        val articles = listOf(
            createArticle("1", "bbc", "World leaders commit to action"),
            createArticle("2", "nyt", "Questions remain about implementation"),
            createArticle("3", "lemonde", "Skeptics doubt enforcement mechanisms")
        )
        val sources = mapOf(
            "bbc" to createSource("bbc", "BBC"),
            "nyt" to createSource("nyt", "NYT"),
            "lemonde" to createSource("lemonde", "Le Monde")
        )

        val digest = generator.generateDigest("story1", articles, sources)

        assertNotNull(digest)
        assertEquals("story1", digest.storyId)
        assertTrue(digest.isDemo)
        assertTrue(digest.agreements.isNotEmpty())
        assertTrue(digest.differences.isNotEmpty())
        assertTrue(digest.missingEvidence.isNotEmpty())
        assertEquals(3, digest.sourcesCovered.size)
    }

    @Test
    fun `generateDigest with two sources shows limited comparison`() {
        val articles = listOf(
            createArticle("1", "bbc", "Event reported"),
            createArticle("2", "nyt", "Similar coverage")
        )
        val sources = mapOf(
            "bbc" to createSource("bbc", "BBC"),
            "nyt" to createSource("nyt", "NYT")
        )

        val digest = generator.generateDigest("story1", articles, sources)

        assertEquals(2, digest.sourcesCovered.size)
        assertTrue(digest.missingEvidence.contains("Additional perspectives from other regions"))
    }

    @Test
    fun `generateDigest with single source shows limited coverage`() {
        val articles = listOf(
            createArticle("1", "bbc", "Single source coverage")
        )
        val sources = mapOf("bbc" to createSource("bbc", "BBC"))

        val digest = generator.generateDigest("story1", articles, sources)

        assertEquals(1, digest.sourcesCovered.size)
        assertNotNull(digest.summary)
        assertTrue(digest.agreements.isNotEmpty())
        assertTrue(digest.missingEvidence.isNotEmpty())
    }

    private fun createArticle(id: String, sourceId: String, excerpt: String) = Article(
        id = id,
        storyId = "story1",
        sourceId = sourceId,
        originalUrl = "https://example.com/$id",
        publishedTime = Instant.now(),
        originalLanguage = "en",
        originalHeadline = "Headline $id",
        originalExcerpt = excerpt,
        originalContent = "Full content for $id",
        attribution = "Staff",
        contentUseMetadata = ContentUseMetadata(isDemo = true, isDemoPlaceholder = false),
        requiresSubscription = false
    )

    private fun createSource(id: String, name: String) = Source(
        id = id,
        name = name,
        homepage = "https://example.com",
        countryCodes = listOf("US"),
        regionIds = listOf("north_america"),
        defaultLanguages = listOf("en"),
        ownershipInfo = null,
        editorialContext = null
    )
}
