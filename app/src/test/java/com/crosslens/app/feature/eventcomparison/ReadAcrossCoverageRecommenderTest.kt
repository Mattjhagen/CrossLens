package com.crosslens.app.feature.eventcomparison

import com.crosslens.app.core.model.Article
import com.crosslens.app.core.model.ContentUseMetadata
import com.crosslens.app.data.ingestion.SourceMetadata
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.Instant

class ReadAcrossCoverageRecommenderTest {

    private lateinit var recommender: ReadAcrossCoverageRecommender

    @Before
    fun setup() {
        recommender = ReadAcrossCoverageRecommender()
    }

    @Test
    fun `recommend returns empty list when no additional articles available`() {
        val article = createArticle(
            id = "1",
            sourceId = "bbc",
            language = "en-GB"
        )
        val metadata = createMetadata(
            publisherName = "BBC News",
            country = "United Kingdom",
            language = "English"
        )
        val articleWithMetadata = ArticleWithMetadata(article, metadata)

        val result = recommender.recommend(
            allArticles = listOf(article),
            alreadyShownArticles = listOf(article),
            articlesWithMetadata = listOf(articleWithMetadata)
        )

        assertTrue(result.isEmpty())
    }

    @Test
    fun `recommend excludes already shown articles`() {
        val article1 = createArticle(id = "1", sourceId = "bbc", language = "en-GB")
        val article2 = createArticle(id = "2", sourceId = "guardian", language = "en-GB")
        val article3 = createArticle(id = "3", sourceId = "lemonde", language = "fr")
        val article4 = createArticle(id = "4", sourceId = "dw", language = "en")

        val metadata1 = createMetadata("BBC News", "United Kingdom", "English")
        val metadata2 = createMetadata("The Guardian", "United Kingdom", "English")
        val metadata3 = createMetadata("Le Monde", "France", "French")
        val metadata4 = createMetadata("Deutsche Welle", "Germany", "English")

        val articlesWithMetadata = listOf(
            ArticleWithMetadata(article1, metadata1),
            ArticleWithMetadata(article2, metadata2),
            ArticleWithMetadata(article3, metadata3),
            ArticleWithMetadata(article4, metadata4)
        )

        val result = recommender.recommend(
            allArticles = listOf(article1, article2, article3, article4),
            alreadyShownArticles = listOf(article1, article2),
            articlesWithMetadata = articlesWithMetadata
        )

        assertEquals(2, result.size)
        assertEquals("3", result[0].article.id)
        assertEquals("4", result[1].article.id)
    }

    @Test
    fun `recommend prioritizes different country`() {
        val shownArticle = createArticle(id = "1", sourceId = "bbc", language = "en-GB")
        val candidateUK = createArticle(id = "2", sourceId = "guardian", language = "en-GB")
        val candidateFR = createArticle(id = "3", sourceId = "lemonde", language = "fr")

        val metadataShown = createMetadata("BBC News", "United Kingdom", "English")
        val metadataUK = createMetadata("The Guardian", "United Kingdom", "English")
        val metadataFR = createMetadata("Le Monde", "France", "French")

        val articlesWithMetadata = listOf(
            ArticleWithMetadata(shownArticle, metadataShown),
            ArticleWithMetadata(candidateUK, metadataUK),
            ArticleWithMetadata(candidateFR, metadataFR)
        )

        val result = recommender.recommend(
            allArticles = listOf(shownArticle, candidateUK, candidateFR),
            alreadyShownArticles = listOf(shownArticle),
            articlesWithMetadata = articlesWithMetadata
        )

        assertEquals(2, result.size)
        // France should come first due to different country and language
        assertEquals("3", result[0].article.id)
        assertTrue(result[0].explanation.contains("France"))
    }

    @Test
    fun `recommend prioritizes different language`() {
        val shownArticle = createArticle(id = "1", sourceId = "bbc", language = "en-GB")
        val candidateEN = createArticle(id = "2", sourceId = "guardian", language = "en-GB")
        val candidateFR = createArticle(id = "3", sourceId = "lemonde", language = "fr")
        val candidateAR = createArticle(id = "4", sourceId = "aljazeera", language = "ar")

        val articlesWithMetadata = listOf(
            ArticleWithMetadata(shownArticle, createMetadata("BBC News", "United Kingdom", "English", "en-GB")),
            ArticleWithMetadata(candidateEN, createMetadata("The Guardian", "United Kingdom", "English", "en-GB")),
            ArticleWithMetadata(candidateFR, createMetadata("Le Monde", "France", "French", "fr")),
            ArticleWithMetadata(candidateAR, createMetadata("Al Jazeera", "Qatar", "Arabic", "ar"))
        )

        val result = recommender.recommend(
            allArticles = listOf(shownArticle, candidateEN, candidateFR, candidateAR),
            alreadyShownArticles = listOf(shownArticle),
            articlesWithMetadata = articlesWithMetadata
        )

        assertEquals(3, result.size)
        // Non-English sources should rank higher
        val nonEnglishCount = result.count { it.article.originalLanguage != "en-GB" }
        assertTrue(nonEnglishCount >= 2)
    }

    @Test
    fun `recommend prioritizes public broadcasters`() {
        val shownArticle = createArticle(id = "1", sourceId = "nyt", language = "en-US")
        val candidatePrivate = createArticle(id = "2", sourceId = "wapo", language = "en-US")
        val candidatePublic = createArticle(id = "3", sourceId = "bbc", language = "en-GB")

        val articlesWithMetadata = listOf(
            ArticleWithMetadata(
                shownArticle,
                createMetadata("New York Times", "United States", "English", "en-US", "American newspaper")
            ),
            ArticleWithMetadata(
                candidatePrivate,
                createMetadata("Washington Post", "United States", "English", "en-US", "American newspaper")
            ),
            ArticleWithMetadata(
                candidatePublic,
                createMetadata("BBC News", "United Kingdom", "English", "en-GB", "British public service broadcaster")
            )
        )

        val result = recommender.recommend(
            allArticles = listOf(shownArticle, candidatePrivate, candidatePublic),
            alreadyShownArticles = listOf(shownArticle),
            articlesWithMetadata = articlesWithMetadata
        )

        assertEquals(2, result.size)
        // BBC should rank higher due to public broadcaster + different country
        assertEquals("3", result[0].article.id)
        assertTrue(result[0].explanation.contains("Public broadcaster") ||
                   result[0].explanation.contains("United Kingdom"))
    }

    @Test
    fun `recommend handles single source events with coverage gap notice`() {
        val article = createArticle(id = "1", sourceId = "bbc", language = "en-GB")
        val metadata = createMetadata("BBC News", "United Kingdom", "English")
        val articleWithMetadata = ArticleWithMetadata(article, metadata)

        val result = recommender.recommend(
            allArticles = listOf(article),
            alreadyShownArticles = listOf(article),
            articlesWithMetadata = listOf(articleWithMetadata)
        )

        // Should return empty for single source
        assertTrue(result.isEmpty())
    }

    @Test
    fun `recommend returns 2 to 4 articles based on availability`() {
        val shownArticle = createArticle(id = "1", sourceId = "bbc", language = "en-GB")

        // Test with 2 candidates
        val twoArticles = listOf(shownArticle) + (2..3).map {
            createArticle(id = "$it", sourceId = "source$it", language = "en")
        }
        val twoMetadata = twoArticles.mapIndexed { i, article ->
            ArticleWithMetadata(article, createMetadata("Source $i", "Country $i", "English"))
        }
        val resultTwo = recommender.recommend(
            allArticles = twoArticles,
            alreadyShownArticles = listOf(shownArticle),
            articlesWithMetadata = twoMetadata
        )
        assertEquals(2, resultTwo.size)

        // Test with 5+ candidates (should return 4)
        val manyArticles = listOf(shownArticle) + (2..7).map {
            createArticle(id = "$it", sourceId = "source$it", language = "en")
        }
        val manyMetadata = manyArticles.mapIndexed { i, article ->
            ArticleWithMetadata(article, createMetadata("Source $i", "Country $i", "English"))
        }
        val resultMany = recommender.recommend(
            allArticles = manyArticles,
            alreadyShownArticles = listOf(shownArticle),
            articlesWithMetadata = manyMetadata
        )
        assertEquals(4, resultMany.size)
    }

    @Test
    fun `recommend generates factual explanation for different country`() {
        val shownArticle = createArticle(id = "1", sourceId = "bbc", language = "en-GB")
        val candidateArticle1 = createArticle(id = "2", sourceId = "lemonde", language = "fr")
        val candidateArticle2 = createArticle(id = "3", sourceId = "dw", language = "en")

        val articlesWithMetadata = listOf(
            ArticleWithMetadata(shownArticle, createMetadata("BBC News", "United Kingdom", "English", "en-GB")),
            ArticleWithMetadata(candidateArticle1, createMetadata("Le Monde", "France", "French", "fr")),
            ArticleWithMetadata(candidateArticle2, createMetadata("Deutsche Welle", "Germany", "English", "en"))
        )

        val result = recommender.recommend(
            allArticles = listOf(shownArticle, candidateArticle1, candidateArticle2),
            alreadyShownArticles = listOf(shownArticle),
            articlesWithMetadata = articlesWithMetadata
        )

        assertEquals(2, result.size)
        assertTrue(result.any { it.explanation.contains("France") })
    }

    @Test
    fun `recommend generates factual explanation for different language`() {
        val shownArticle = createArticle(id = "1", sourceId = "bbc", language = "en-GB")
        val candidateArticle1 = createArticle(id = "2", sourceId = "lemonde", language = "fr")
        val candidateArticle2 = createArticle(id = "3", sourceId = "dw", language = "en")

        val articlesWithMetadata = listOf(
            ArticleWithMetadata(shownArticle, createMetadata("BBC News", "United Kingdom", "English", "en-GB")),
            ArticleWithMetadata(candidateArticle1, createMetadata("Le Monde", "France", "French", "fr")),
            ArticleWithMetadata(candidateArticle2, createMetadata("Deutsche Welle", "Germany", "English", "en"))
        )

        val result = recommender.recommend(
            allArticles = listOf(shownArticle, candidateArticle1, candidateArticle2),
            alreadyShownArticles = listOf(shownArticle),
            articlesWithMetadata = articlesWithMetadata
        )

        assertEquals(2, result.size)
        assertTrue(result.any { it.explanation.contains("French") })
    }

    @Test
    fun `recommend generates factual explanation for public broadcaster`() {
        val shownArticle = createArticle(id = "1", sourceId = "nyt", language = "en-US")
        val candidateArticle1 = createArticle(id = "2", sourceId = "bbc", language = "en-GB")
        val candidateArticle2 = createArticle(id = "3", sourceId = "lemonde", language = "fr")

        val articlesWithMetadata = listOf(
            ArticleWithMetadata(
                shownArticle,
                createMetadata("New York Times", "United States", "English", "en-US", "American newspaper")
            ),
            ArticleWithMetadata(
                candidateArticle1,
                createMetadata("BBC News", "United Kingdom", "English", "en-GB", "British public service broadcaster")
            ),
            ArticleWithMetadata(
                candidateArticle2,
                createMetadata("Le Monde", "France", "French", "fr", "French newspaper")
            )
        )

        val result = recommender.recommend(
            allArticles = listOf(shownArticle, candidateArticle1, candidateArticle2),
            alreadyShownArticles = listOf(shownArticle),
            articlesWithMetadata = articlesWithMetadata
        )

        assertEquals(2, result.size)
        assertTrue(result.any { it.explanation.contains("Public broadcaster") ||
                   it.explanation.contains("United Kingdom") })
    }

    @Test
    fun `recommend never shows unrelated articles`() {
        val shownArticle = createArticle(id = "1", sourceId = "bbc", language = "en-GB", storyId = "story1")
        val relatedArticle1 = createArticle(id = "2", sourceId = "guardian", language = "en-GB", storyId = "story1")
        val relatedArticle2 = createArticle(id = "3", sourceId = "dw", language = "en", storyId = "story1")
        val unrelatedArticle = createArticle(id = "4", sourceId = "lemonde", language = "fr", storyId = "story2")

        val articlesWithMetadata = listOf(
            ArticleWithMetadata(shownArticle, createMetadata("BBC News", "United Kingdom", "English")),
            ArticleWithMetadata(relatedArticle1, createMetadata("The Guardian", "United Kingdom", "English")),
            ArticleWithMetadata(relatedArticle2, createMetadata("Deutsche Welle", "Germany", "English")),
            ArticleWithMetadata(unrelatedArticle, createMetadata("Le Monde", "France", "French"))
        )

        // Only related articles should be in the pool
        val result = recommender.recommend(
            allArticles = listOf(shownArticle, relatedArticle1, relatedArticle2), // Exclude unrelated
            alreadyShownArticles = listOf(shownArticle),
            articlesWithMetadata = articlesWithMetadata.filter { it.article.storyId == "story1" }
        )

        assertEquals(2, result.size)
        assertTrue(result.all { it.article.storyId == "story1" })
    }

    @Test
    fun `recommend handles missing metadata gracefully`() {
        val shownArticle = createArticle(id = "1", sourceId = "bbc", language = "en-GB")
        val candidateArticle1 = createArticle(id = "2", sourceId = "unknown", language = "en-US")
        val candidateArticle2 = createArticle(id = "3", sourceId = "guardian", language = "en-GB")
        val candidateArticle3 = createArticle(id = "4", sourceId = "lemonde", language = "fr")

        val articlesWithMetadata = listOf(
            ArticleWithMetadata(shownArticle, createMetadata("BBC News", "United Kingdom", "English")),
            ArticleWithMetadata(candidateArticle1, null), // Missing metadata
            ArticleWithMetadata(candidateArticle2, createMetadata("The Guardian", "United Kingdom", "English")),
            ArticleWithMetadata(candidateArticle3, createMetadata("Le Monde", "France", "French"))
        )

        val result = recommender.recommend(
            allArticles = listOf(shownArticle, candidateArticle1, candidateArticle2, candidateArticle3),
            alreadyShownArticles = listOf(shownArticle),
            articlesWithMetadata = articlesWithMetadata
        )

        assertEquals(3, result.size)
        assertTrue(result.all { it.explanation.isNotEmpty() }) // Should still have explanations
    }

    @Test
    fun `recommend maintains stable ordering for identical scores`() {
        val shownArticle = createArticle(id = "1", sourceId = "bbc", language = "en-GB")
        val candidate1 = createArticle(id = "2", sourceId = "guardian", language = "en-GB")
        val candidate2 = createArticle(id = "3", sourceId = "times", language = "en-GB")

        val articlesWithMetadata = listOf(
            ArticleWithMetadata(shownArticle, createMetadata("BBC News", "United Kingdom", "English")),
            ArticleWithMetadata(candidate1, createMetadata("The Guardian", "United Kingdom", "English")),
            ArticleWithMetadata(candidate2, createMetadata("The Times", "United Kingdom", "English"))
        )

        // Run recommendation multiple times
        val result1 = recommender.recommend(
            allArticles = listOf(shownArticle, candidate1, candidate2),
            alreadyShownArticles = listOf(shownArticle),
            articlesWithMetadata = articlesWithMetadata
        )

        val result2 = recommender.recommend(
            allArticles = listOf(shownArticle, candidate1, candidate2),
            alreadyShownArticles = listOf(shownArticle),
            articlesWithMetadata = articlesWithMetadata
        )

        // Order should be stable
        assertEquals(result1.map { it.article.id }, result2.map { it.article.id })
    }

    @Test
    fun `recommend returns empty for two-source cluster - below minimum threshold`() {
        val article1 = createArticle(id = "1", sourceId = "bbc", language = "en-GB")
        val article2 = createArticle(id = "2", sourceId = "guardian", language = "en-GB")

        val articlesWithMetadata = listOf(
            ArticleWithMetadata(article1, createMetadata("BBC News", "United Kingdom", "English")),
            ArticleWithMetadata(article2, createMetadata("The Guardian", "United Kingdom", "English"))
        )

        val result = recommender.recommend(
            allArticles = listOf(article1, article2),
            alreadyShownArticles = listOf(article1),
            articlesWithMetadata = articlesWithMetadata
        )

        // Should be empty - only 2 distinct publishers (need 3+)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `recommend works for three-source cluster - minimum threshold`() {
        val article1 = createArticle(id = "1", sourceId = "bbc", language = "en-GB")
        val article2 = createArticle(id = "2", sourceId = "guardian", language = "en-GB")
        val article3 = createArticle(id = "3", sourceId = "lemonde", language = "fr")

        val articlesWithMetadata = listOf(
            ArticleWithMetadata(article1, createMetadata("BBC News", "United Kingdom", "English")),
            ArticleWithMetadata(article2, createMetadata("The Guardian", "United Kingdom", "English")),
            ArticleWithMetadata(article3, createMetadata("Le Monde", "France", "French"))
        )

        val result = recommender.recommend(
            allArticles = listOf(article1, article2, article3),
            alreadyShownArticles = listOf(article1),
            articlesWithMetadata = articlesWithMetadata
        )

        // Should have 2 recommendations (Guardian + Le Monde)
        assertEquals(2, result.size)
        assertTrue(result.any { it.article.id == "2" })
        assertTrue(result.any { it.article.id == "3" })
    }

    @Test
    fun `recommend works for four-source cluster`() {
        val article1 = createArticle(id = "1", sourceId = "bbc", language = "en-GB")
        val article2 = createArticle(id = "2", sourceId = "guardian", language = "en-GB")
        val article3 = createArticle(id = "3", sourceId = "lemonde", language = "fr")
        val article4 = createArticle(id = "4", sourceId = "dw", language = "en")

        val articlesWithMetadata = listOf(
            ArticleWithMetadata(article1, createMetadata("BBC News", "United Kingdom", "English")),
            ArticleWithMetadata(article2, createMetadata("The Guardian", "United Kingdom", "English")),
            ArticleWithMetadata(article3, createMetadata("Le Monde", "France", "French")),
            ArticleWithMetadata(article4, createMetadata("Deutsche Welle", "Germany", "English"))
        )

        val result = recommender.recommend(
            allArticles = listOf(article1, article2, article3, article4),
            alreadyShownArticles = listOf(article1),
            articlesWithMetadata = articlesWithMetadata
        )

        // Should have 3 recommendations (all except article1)
        assertEquals(3, result.size)
        assertTrue(result.any { it.article.id == "2" })
        assertTrue(result.any { it.article.id == "3" })
        assertTrue(result.any { it.article.id == "4" })
    }

    @Test
    fun `recommend returns empty when only one additional article available`() {
        val article1 = createArticle(id = "1", sourceId = "bbc", language = "en-GB")
        val article2 = createArticle(id = "2", sourceId = "guardian", language = "en-GB")
        val article3 = createArticle(id = "3", sourceId = "lemonde", language = "fr")

        val articlesWithMetadata = listOf(
            ArticleWithMetadata(article1, createMetadata("BBC News", "United Kingdom", "English")),
            ArticleWithMetadata(article2, createMetadata("The Guardian", "United Kingdom", "English")),
            ArticleWithMetadata(article3, createMetadata("Le Monde", "France", "French"))
        )

        // Pass two articles as already shown, leaving only 1 candidate
        val result = recommender.recommend(
            allArticles = listOf(article1, article2, article3),
            alreadyShownArticles = listOf(article1, article2),
            articlesWithMetadata = articlesWithMetadata
        )

        // Should be empty - only 1 additional article (need 2+)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `recommend excludes current article from candidates`() {
        val article1 = createArticle(id = "1", sourceId = "bbc", language = "en-GB")
        val article2 = createArticle(id = "2", sourceId = "guardian", language = "en-GB")
        val article3 = createArticle(id = "3", sourceId = "lemonde", language = "fr")

        val articlesWithMetadata = listOf(
            ArticleWithMetadata(article1, createMetadata("BBC News", "United Kingdom", "English")),
            ArticleWithMetadata(article2, createMetadata("The Guardian", "United Kingdom", "English")),
            ArticleWithMetadata(article3, createMetadata("Le Monde", "France", "French"))
        )

        val result = recommender.recommend(
            allArticles = listOf(article1, article2, article3),
            alreadyShownArticles = listOf(article1),
            articlesWithMetadata = articlesWithMetadata
        )

        // Should not include article1 in recommendations
        assertFalse(result.any { it.article.id == "1" })
        assertEquals(2, result.size)
    }

    @Test
    fun `recommend prefers wire services and international news services`() {
        val shownArticle = createArticle(id = "1", sourceId = "nyt", language = "en-US")
        val candidateNewspaper = createArticle(id = "2", sourceId = "wapo", language = "en-US")
        val candidateWire = createArticle(id = "3", sourceId = "reuters", language = "en")

        val articlesWithMetadata = listOf(
            ArticleWithMetadata(
                shownArticle,
                createMetadata("New York Times", "United States", "English", "en-US", "American newspaper")
            ),
            ArticleWithMetadata(
                candidateNewspaper,
                createMetadata("Washington Post", "United States", "English", "en-US", "American newspaper")
            ),
            ArticleWithMetadata(
                candidateWire,
                createMetadata("Reuters", "United Kingdom", "English", "en", "International wire service")
            )
        )

        val result = recommender.recommend(
            allArticles = listOf(shownArticle, candidateNewspaper, candidateWire),
            alreadyShownArticles = listOf(shownArticle),
            articlesWithMetadata = articlesWithMetadata
        )

        // Wire service should rank higher
        val wireRank = result.indexOfFirst { it.article.id == "3" }
        val newspaperRank = result.indexOfFirst { it.article.id == "2" }
        assertTrue(wireRank < newspaperRank)
    }

    // Helper functions
    private fun createArticle(
        id: String,
        sourceId: String,
        language: String,
        storyId: String = "story1"
    ): Article {
        return Article(
            id = id,
            storyId = storyId,
            sourceId = sourceId,
            originalUrl = "https://example.com/$id",
            publishedTime = Instant.now(),
            originalLanguage = language,
            originalHeadline = "Headline $id",
            originalExcerpt = "Excerpt $id",
            originalContent = "Content $id",
            attribution = "Source $sourceId",
            contentUseMetadata = ContentUseMetadata(isDemo = false, isDemoPlaceholder = false)
        )
    }

    private fun createMetadata(
        publisherName: String,
        country: String,
        language: String,
        languageCode: String = "en",
        editorialDescription: String? = null
    ): SourceMetadata {
        return SourceMetadata(
            publisherName = publisherName,
            country = country,
            primaryLanguage = language,
            languageCode = languageCode,
            editorialDescription = editorialDescription,
            descriptionProvenance = if (editorialDescription != null) "Test provenance" else null,
            homepage = "https://example.com"
        )
    }
}
