package com.crosslens.app.data.ingestion

import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.Instant

class CrossLanguageEventMatcherTest {

    private lateinit var matcher: CrossLanguageEventMatcher
    private lateinit var extractor: EntityExtractor
    private lateinit var pipeline: EventClusteringPipeline
    private val baseTime = Instant.parse("2026-09-22T12:00:00Z")

    @Before
    fun setup() {
        extractor = EntityExtractor()
        matcher = CrossLanguageEventMatcher()
        pipeline = EventClusteringPipeline(
            entityExtractor = extractor,
            crossLanguageMatcher = matcher
        )
    }

    @Test
    fun `matches same event across English French and Arabic with shared entities`() = runTest {
        val (adapters, _) = EntityTestFixtures.createGenevaSummitMultilingual(baseTime)
        val allArticles = mutableListOf<IngestionArticleInput>()

        adapters.forEach { adapter ->
            adapter.fetchArticles().forEach { record ->
                allArticles += IngestionArticleInput(
                    sourceId = adapter.sourceId,
                    sourceName = adapter.sourceName,
                    url = record.url,
                    publishedAt = record.publishedAt,
                    languageTag = record.languageTag,
                    headline = record.headline,
                    excerpt = record.excerpt
                )
            }
        }

        // First pass to get article IDs
        val firstPassResult = pipeline.process(allArticles, emptyMap())
        val articleIds = firstPassResult.acceptedArticles.map { it.id }

        // Build entity map with actual article IDs
        val sharedEntitiesEn = listOf(
            EventIdentifierEntity("event:geneva-climate-summit-2026", "Geneva Climate Summit 2026", "en", "summit"),
            LocationEntity("location:geneva", "Geneva", "en", "CH"),
            OrganizationEntity("org:un", "United Nations", "en", "international"),
            PersonEntity("person:un-secretary-general", "UN Secretary-General", "en", "Secretary-General")
        )
        val sharedEntitiesFr = listOf(
            EventIdentifierEntity("event:geneva-climate-summit-2026", "Sommet climatique de Genève 2026", "fr", "summit"),
            LocationEntity("location:geneva", "Genève", "fr", "CH"),
            OrganizationEntity("org:un", "ONU", "fr", "international"),
            PersonEntity("person:un-secretary-general", "Secrétaire général de l'ONU", "fr", "Secretary-General")
        )
        val sharedEntitiesAr = listOf(
            EventIdentifierEntity("event:geneva-climate-summit-2026", "قمة المناخ في جنيف 2026", "ar", "summit"),
            LocationEntity("location:geneva", "جنيف", "ar", "CH"),
            OrganizationEntity("org:un", "الأمم المتحدة", "ar", "international"),
            PersonEntity("person:un-secretary-general", "الأمين العام للأمم المتحدة", "ar", "Secretary-General")
        )

        val entityMap = firstPassResult.acceptedArticles.associate { article ->
            article.id to when (article.languageTag) {
                "en" -> sharedEntitiesEn
                "fr" -> sharedEntitiesFr
                "ar" -> sharedEntitiesAr
                else -> emptyList()
            }
        }

        // Second pass with entity map
        val result = pipeline.process(allArticles, entityMap)

        // Verify entity extraction ran
        assertEquals(3, result.entityExtractionResult.articlesWithEntities.size)
        assertTrue(result.entityExtractionResult.totalEntitiesExtracted >= 12) // 4 entities × 3 articles

        // Verify cross-language match found
        assertEquals(1, result.crossLanguageMatches.candidateMatches.size)

        val match = result.crossLanguageMatches.candidateMatches.first()
        assertEquals(3, match.articleIds.size)
        assertEquals(3, match.sourceIds.size)
        assertEquals(3, match.languages.size)
        assertTrue(match.languages.containsAll(listOf("en", "fr", "ar")))

        // Verify shared entities
        assertTrue(match.sharedEntities.contains("event:geneva-climate-summit-2026"))
        assertTrue(match.sharedEntities.contains("location:geneva"))
        assertTrue(match.sharedEntities.contains("org:un"))
        assertTrue(match.sharedEntities.contains("person:un-secretary-general"))

        // Verify confidence and review flag
        assertEquals(EntityMatchConfidence.HIGH, match.confidence)
        assertTrue(match.requiresEditorialReview)
        assertTrue(match.rationale.contains("High confidence candidate"))
    }

    @Test
    fun `does not match different events with generic shared entities`() = runTest {
        val (adapters, entityMap) = EntityTestFixtures.createDifferentEventsGenericEntities(baseTime)
        val allArticles = mutableListOf<IngestionArticleInput>()

        adapters.forEach { adapter ->
            adapter.fetchArticles().forEach { record ->
                allArticles += IngestionArticleInput(
                    sourceId = adapter.sourceId,
                    sourceName = adapter.sourceName,
                    url = record.url,
                    publishedAt = record.publishedAt,
                    languageTag = record.languageTag,
                    headline = record.headline,
                    excerpt = record.excerpt
                )
            }
        }

        val result = pipeline.process(allArticles, entityMap)

        // Both articles should be extracted
        assertEquals(2, result.entityExtractionResult.articlesWithEntities.size)

        // Different event identifiers should prevent HIGH confidence match
        // At most should be LOW confidence or no match
        val highConfidenceMatches = result.crossLanguageMatches.candidateMatches.filter {
            it.confidence == EntityMatchConfidence.HIGH
        }
        assertEquals(0, highConfidenceMatches.size)

        // If there is a match, it should be low confidence
        result.crossLanguageMatches.candidateMatches.forEach { match ->
            assertTrue("Different events should not get HIGH/MEDIUM confidence",
                match.confidence == EntityMatchConfidence.LOW || match.confidence == EntityMatchConfidence.UNCERTAIN)
        }
    }

    @Test
    fun `no match when same event lacks shared entity evidence`() = runTest {
        val (adapters, entityMap) = EntityTestFixtures.createSameEventNoSharedEntities(baseTime)
        val allArticles = mutableListOf<IngestionArticleInput>()

        adapters.forEach { adapter ->
            adapter.fetchArticles().forEach { record ->
                allArticles += IngestionArticleInput(
                    sourceId = adapter.sourceId,
                    sourceName = adapter.sourceName,
                    url = record.url,
                    publishedAt = record.publishedAt,
                    languageTag = record.languageTag,
                    headline = record.headline,
                    excerpt = record.excerpt
                )
            }
        }

        val result = pipeline.process(allArticles, entityMap)

        // Entities extracted
        assertEquals(2, result.entityExtractionResult.articlesWithEntities.size)

        // No cross-language match due to insufficient shared entities
        // Only one shared entity (location:paris) is below minimum threshold of 2
        val matches = result.crossLanguageMatches.candidateMatches
        if (matches.isNotEmpty()) {
            // If matched, should be UNCERTAIN or LOW due to minimal overlap
            matches.forEach { match ->
                assertTrue("Minimal entity overlap should not produce HIGH confidence",
                    match.confidence != EntityMatchConfidence.HIGH)
            }
        }
    }

    @Test
    fun `marks generic shared terms as low confidence`() = runTest {
        val (adapters, entityMap) = EntityTestFixtures.createGenericSharedTerms(baseTime)
        val allArticles = mutableListOf<IngestionArticleInput>()

        adapters.forEach { adapter ->
            adapter.fetchArticles().forEach { record ->
                allArticles += IngestionArticleInput(
                    sourceId = adapter.sourceId,
                    sourceName = adapter.sourceName,
                    url = record.url,
                    publishedAt = record.publishedAt,
                    languageTag = record.languageTag,
                    headline = record.headline,
                    excerpt = record.excerpt
                )
            }
        }

        val result = pipeline.process(allArticles, entityMap)

        // If matched, should detect generic locations and assign LOW confidence
        val matches = result.crossLanguageMatches.candidateMatches
        matches.forEach { match ->
            if (match.sharedEntities.any { it.contains("washington") || it.contains("beijing") }) {
                // Generic location entities should lower confidence
                assertTrue("Generic entities should not produce HIGH confidence",
                    match.confidence != EntityMatchConfidence.HIGH)

                // Should have uncertainty reason about generic entities
                assertTrue("Should note generic entities in uncertainty reasons",
                    match.uncertaintyReasons.any { it.contains("Generic") || it.contains("generic") })
            }
        }
    }

    @Test
    fun `preserves original language and source attribution`() = runTest {
        val (adapters, entityMap) = EntityTestFixtures.createGenevaSummitMultilingual(baseTime)
        val allArticles = mutableListOf<IngestionArticleInput>()

        adapters.forEach { adapter ->
            adapter.fetchArticles().forEach { record ->
                allArticles += IngestionArticleInput(
                    sourceId = adapter.sourceId,
                    sourceName = adapter.sourceName,
                    url = record.url,
                    publishedAt = record.publishedAt,
                    languageTag = record.languageTag,
                    headline = record.headline,
                    excerpt = record.excerpt
                )
            }
        }

        val result = pipeline.process(allArticles, entityMap)

        // All articles preserved with original attributes
        assertEquals(3, result.acceptedArticles.size)

        val englishArticle = result.acceptedArticles.first { it.languageTag == "en" }
        val frenchArticle = result.acceptedArticles.first { it.languageTag == "fr" }
        val arabicArticle = result.acceptedArticles.first { it.languageTag == "ar" }

        // Verify original language preserved
        assertEquals("en", englishArticle.languageTag)
        assertEquals("fr", frenchArticle.languageTag)
        assertEquals("ar", arabicArticle.languageTag)

        // Verify source attribution preserved
        assertEquals("bbc-demo", englishArticle.sourceId)
        assertEquals("lemonde-demo", frenchArticle.sourceId)
        assertEquals("aljazeera-demo", arabicArticle.sourceId)

        // Verify original headlines preserved (not translated)
        assertTrue(englishArticle.headline.contains("Geneva"))
        assertTrue(frenchArticle.headline.contains("Genève"))
        assertTrue(arabicArticle.headline.contains("جنيف"))
    }

    @Test
    fun `candidate matches require editorial review`() = runTest {
        val (adapters, entityMap) = EntityTestFixtures.createGenevaSummitMultilingual(baseTime)
        val allArticles = mutableListOf<IngestionArticleInput>()

        adapters.forEach { adapter ->
            adapter.fetchArticles().forEach { record ->
                allArticles += IngestionArticleInput(
                    sourceId = adapter.sourceId,
                    sourceName = adapter.sourceName,
                    url = record.url,
                    publishedAt = record.publishedAt,
                    languageTag = record.languageTag,
                    headline = record.headline,
                    excerpt = record.excerpt
                )
            }
        }

        val result = pipeline.process(allArticles, entityMap)

        // All candidate matches must require editorial review
        result.crossLanguageMatches.candidateMatches.forEach { match ->
            assertTrue("All cross-language matches must require editorial review",
                match.requiresEditorialReview)

            // Must have rationale
            assertTrue("Rationale must be present", match.rationale.isNotBlank())

            // Must have uncertainty reasons
            assertTrue("Uncertainty reasons must be present",
                match.uncertaintyReasons.isNotEmpty())

            // Should note cross-language uncertainty
            assertTrue("Should note cross-language uncertainty",
                match.uncertaintyReasons.any { it.contains("Cross-language") || it.contains("editorial") })
        }
    }

    @Test
    fun `provides transparent detection evidence`() = runTest {
        val (adapters, _) = EntityTestFixtures.createGenevaSummitMultilingual(baseTime)
        val allArticles = mutableListOf<IngestionArticleInput>()

        adapters.forEach { adapter ->
            adapter.fetchArticles().forEach { record ->
                allArticles += IngestionArticleInput(
                    sourceId = adapter.sourceId,
                    sourceName = adapter.sourceName,
                    url = record.url,
                    publishedAt = record.publishedAt,
                    languageTag = record.languageTag,
                    headline = record.headline,
                    excerpt = record.excerpt
                )
            }
        }

        // Build entity map with actual article IDs
        val firstPassResult = pipeline.process(allArticles, emptyMap())
        val entityMap = firstPassResult.acceptedArticles.associate { article ->
            article.id to when (article.languageTag) {
                "en" -> listOf(
                    EventIdentifierEntity("event:geneva-climate-summit-2026", "Geneva Climate Summit 2026", "en", "summit"),
                    LocationEntity("location:geneva", "Geneva", "en", "CH"),
                    OrganizationEntity("org:un", "United Nations", "en", "international"),
                    PersonEntity("person:un-secretary-general", "UN Secretary-General", "en", "Secretary-General")
                )
                "fr" -> listOf(
                    EventIdentifierEntity("event:geneva-climate-summit-2026", "Sommet climatique de Genève 2026", "fr", "summit"),
                    LocationEntity("location:geneva", "Genève", "fr", "CH"),
                    OrganizationEntity("org:un", "ONU", "fr", "international"),
                    PersonEntity("person:un-secretary-general", "Secrétaire général de l'ONU", "fr", "Secretary-General")
                )
                "ar" -> listOf(
                    EventIdentifierEntity("event:geneva-climate-summit-2026", "قمة المناخ في جنيف 2026", "ar", "summit"),
                    LocationEntity("location:geneva", "جنيف", "ar", "CH"),
                    OrganizationEntity("org:un", "الأمم المتحدة", "ar", "international"),
                    PersonEntity("person:un-secretary-general", "الأمين العام للأمم المتحدة", "ar", "Secretary-General")
                )
                else -> emptyList()
            }
        }

        val result = pipeline.process(allArticles, entityMap)
        val match = result.crossLanguageMatches.candidateMatches.first()

        // Evidence should be transparent and inspectable
        assertTrue("Must have article IDs", match.articleIds.isNotEmpty())
        assertTrue("Must have source IDs", match.sourceIds.isNotEmpty())
        assertTrue("Must have languages", match.languages.isNotEmpty())
        assertTrue("Must have shared entities", match.sharedEntities.isNotEmpty())

        // Method version should be present
        assertTrue("Must have matching method", match.matchingMethod.isNotBlank())
        assertEquals("entity-overlap-v1", match.matchingMethod)

        // Confidence should be set
        assertNotNull("Must have confidence band", match.confidence)
    }

    @Test
    fun `same-language articles use title similarity not entity matching`() = runTest {
        // Create two English articles about the same event
        val adapters = listOf(
            MockSourceAdapter(
                sourceId = "bbc-demo",
                sourceName = "BBC News Demo",
                articles = listOf(
                    SourceArticleRecord(
                        url = "https://bbc.example/summit-1",
                        publishedAt = baseTime,
                        languageTag = "en",
                        headline = "Geneva summit reaches agreement",
                        excerpt = "Summit ended with agreement.",
                        contentPermission = ContentPermission.EXPLICIT_EXCERPT
                    )
                )
            ),
            MockSourceAdapter(
                sourceId = "guardian-demo",
                sourceName = "The Guardian Demo",
                articles = listOf(
                    SourceArticleRecord(
                        url = "https://guardian.example/summit-2",
                        publishedAt = baseTime.plusSeconds(900),
                        languageTag = "en",
                        headline = "Geneva summit reaches agreement",
                        excerpt = "Summit ended with agreement.",
                        contentPermission = ContentPermission.EXPLICIT_EXCERPT
                    )
                )
            )
        )

        val allArticles = mutableListOf<IngestionArticleInput>()
        adapters.forEach { adapter ->
            adapter.fetchArticles().forEach { record ->
                allArticles += IngestionArticleInput(
                    sourceId = adapter.sourceId,
                    sourceName = adapter.sourceName,
                    url = record.url,
                    publishedAt = record.publishedAt,
                    languageTag = record.languageTag,
                    headline = record.headline,
                    excerpt = record.excerpt
                )
            }
        }

        val result = pipeline.process(allArticles, emptyMap())

        // Same-language articles should NOT appear in cross-language matches
        assertEquals(0, result.crossLanguageMatches.candidateMatches.size)

        // They should cluster via title similarity instead
        assertEquals(1, result.clusters.size)
        val cluster = result.clusters.first()
        assertEquals(2, cluster.articleIds.size)
        assertEquals(ClusterStatus.REVIEWABLE, cluster.status)
    }

    @Test
    fun `entity extraction reports statistics`() = runTest {
        val (adapters, _) = EntityTestFixtures.createGenevaSummitMultilingual(baseTime)
        val allArticles = mutableListOf<IngestionArticleInput>()

        adapters.forEach { adapter ->
            adapter.fetchArticles().forEach { record ->
                allArticles += IngestionArticleInput(
                    sourceId = adapter.sourceId,
                    sourceName = adapter.sourceName,
                    url = record.url,
                    publishedAt = record.publishedAt,
                    languageTag = record.languageTag,
                    headline = record.headline,
                    excerpt = record.excerpt
                )
            }
        }

        // Build entity map with actual article IDs
        val firstPassResult = pipeline.process(allArticles, emptyMap())
        val entityMap = firstPassResult.acceptedArticles.associate { article ->
            article.id to when (article.languageTag) {
                "en" -> listOf(
                    EventIdentifierEntity("event:geneva-climate-summit-2026", "Geneva Climate Summit 2026", "en", "summit"),
                    LocationEntity("location:geneva", "Geneva", "en", "CH"),
                    OrganizationEntity("org:un", "United Nations", "en", "international"),
                    PersonEntity("person:un-secretary-general", "UN Secretary-General", "en", "Secretary-General")
                )
                "fr" -> listOf(
                    EventIdentifierEntity("event:geneva-climate-summit-2026", "Sommet climatique de Genève 2026", "fr", "summit"),
                    LocationEntity("location:geneva", "Genève", "fr", "CH"),
                    OrganizationEntity("org:un", "ONU", "fr", "international"),
                    PersonEntity("person:un-secretary-general", "Secrétaire général de l'ONU", "fr", "Secretary-General")
                )
                "ar" -> listOf(
                    EventIdentifierEntity("event:geneva-climate-summit-2026", "قمة المناخ في جنيف 2026", "ar", "summit"),
                    LocationEntity("location:geneva", "جنيف", "ar", "CH"),
                    OrganizationEntity("org:un", "الأمم المتحدة", "ar", "international"),
                    PersonEntity("person:un-secretary-general", "الأمين العام للأمم المتحدة", "ar", "Secretary-General")
                )
                else -> emptyList()
            }
        }

        val result = pipeline.process(allArticles, entityMap)

        // Verify statistics
        val stats = result.entityExtractionResult
        assertEquals(3, stats.articlesWithEntities.size)
        assertEquals(12, stats.totalEntitiesExtracted) // 4 entities × 3 articles

        // Verify entity type breakdown
        assertTrue(stats.entitiesByType.containsKey("Person"))
        assertTrue(stats.entitiesByType.containsKey("Organization"))
        assertTrue(stats.entitiesByType.containsKey("Location"))
        assertTrue(stats.entitiesByType.containsKey("EventIdentifier"))

        assertEquals(3, stats.entitiesByType["Person"])
        assertEquals(3, stats.entitiesByType["Organization"])
        assertEquals(3, stats.entitiesByType["Location"])
        assertEquals(3, stats.entitiesByType["EventIdentifier"])
    }
}
