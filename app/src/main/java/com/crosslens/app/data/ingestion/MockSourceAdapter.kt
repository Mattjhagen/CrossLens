package com.crosslens.app.data.ingestion

import java.time.Instant

/**
 * Mock source adapter that emits fictional demo articles for testing the ingestion pipeline.
 * Real adapters would integrate with approved RSS feeds, APIs, or licensed content sources.
 */
class MockSourceAdapter(
    override val sourceId: String,
    override val sourceName: String,
    private val articles: List<SourceArticleRecord>
) : SourceAdapter {
    override suspend fun fetchArticles(): List<SourceArticleRecord> = articles

    companion object {
        /**
         * Creates a BBC mock adapter with fictional climate coverage.
         */
        fun createBBC(baseTime: Instant): MockSourceAdapter {
            return MockSourceAdapter(
                sourceId = "bbc",
                sourceName = "BBC News",
                articles = listOf(
                    SourceArticleRecord(
                        url = "https://bbc.example/climate-summit-breakthrough",
                        publishedAt = baseTime,
                        languageTag = "en",
                        headline = "Climate summit reaches historic agreement in Geneva",
                        excerpt = "World leaders at the Geneva climate summit announced a breakthrough agreement on emission targets, marking a significant step in international climate policy.",
                        contentPermission = ContentPermission.EXPLICIT_EXCERPT
                    )
                )
            )
        }

        /**
         * Creates a Le Monde mock adapter with fictional climate coverage in French.
         */
        fun createLeMonde(baseTime: Instant): MockSourceAdapter {
            return MockSourceAdapter(
                sourceId = "lemonde",
                sourceName = "Le Monde",
                articles = listOf(
                    SourceArticleRecord(
                        url = "https://lemonde.example/sommet-climat-geneve",
                        publishedAt = baseTime.plusSeconds(3600),
                        languageTag = "fr",
                        headline = "Le sommet de Genève aboutit à un accord historique sur le climat",
                        excerpt = "Les dirigeants mondiaux réunis à Genève ont annoncé un accord historique sur les objectifs d'émissions, représentant une avancée majeure dans la politique climatique internationale.",
                        contentPermission = ContentPermission.EXPLICIT_EXCERPT
                    )
                )
            )
        }

        /**
         * Creates an Al Jazeera mock adapter with fictional climate coverage in Arabic.
         */
        fun createAlJazeera(baseTime: Instant): MockSourceAdapter {
            return MockSourceAdapter(
                sourceId = "aljazeera",
                sourceName = "Al Jazeera",
                articles = listOf(
                    SourceArticleRecord(
                        url = "https://aljazeera.example/geneva-climate-agreement",
                        publishedAt = baseTime.plusSeconds(7200),
                        languageTag = "ar",
                        headline = "قمة جنيف تتوصل إلى اتفاق مناخي تاريخي",
                        excerpt = "أعلن قادة العالم في قمة جنيف للمناخ عن اتفاق تاريخي بشأن أهداف الانبعاثات، مما يمثل خطوة مهمة في السياسة المناخية الدولية.",
                        contentPermission = ContentPermission.EXPLICIT_EXCERPT
                    )
                )
            )
        }

        /**
         * Creates a New York Times mock adapter with unrelated tech coverage.
         */
        fun createNYT(baseTime: Instant): MockSourceAdapter {
            return MockSourceAdapter(
                sourceId = "nyt",
                sourceName = "The New York Times",
                articles = listOf(
                    SourceArticleRecord(
                        url = "https://nytimes.example/ai-regulation-debate",
                        publishedAt = baseTime,
                        languageTag = "en",
                        headline = "Congress debates new framework for AI regulation",
                        excerpt = "Federal lawmakers are considering comprehensive legislation to regulate artificial intelligence development and deployment across multiple sectors.",
                        contentPermission = ContentPermission.FAIR_USE_PREVIEW
                    )
                )
            )
        }

        /**
         * Creates adapters that will produce a multi-source climate cluster and a single-source tech story.
         */
        fun createTestSet(baseTime: Instant): List<MockSourceAdapter> {
            return listOf(
                createBBC(baseTime),
                createLeMonde(baseTime),
                createAlJazeera(baseTime),
                createNYT(baseTime)
            )
        }
    }
}
