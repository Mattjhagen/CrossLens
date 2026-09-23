package com.crosslens.app.data.ingestion

import java.time.Instant

/**
 * Test fixtures for syndication detection scenarios.
 * All sources and content are fictional mock data for testing detection logic.
 */
object SyndicationTestFixtures {

    /**
     * Scenario 1: Wire service content republished by two different outlets.
     * Same exact excerpt, same headline, different URLs and sources.
     * Expected: HIGH confidence syndication group.
     */
    fun createWireServiceReprints(baseTime: Instant): List<MockSourceAdapter> {
        val wireExcerpt = "The International Climate Summit in Geneva concluded today with " +
                "a landmark agreement on carbon emission reductions. Participating nations " +
                "committed to reducing emissions by 40% by 2030, according to official documents " +
                "released by the summit organizers. The agreement marks a significant step " +
                "forward in international climate policy coordination."

        return listOf(
            MockSourceAdapter(
                sourceId = "bbc-demo",
                sourceName = "BBC News Demo",
                articles = listOf(
                    SourceArticleRecord(
                        url = "https://bbc.example/geneva-climate-agreement",
                        publishedAt = baseTime,
                        languageTag = "en",
                        headline = "Geneva climate summit reaches landmark agreement",
                        excerpt = wireExcerpt,
                        contentPermission = ContentPermission.EXPLICIT_EXCERPT
                    )
                )
            ),
            MockSourceAdapter(
                sourceId = "guardian-demo",
                sourceName = "The Guardian Demo",
                articles = listOf(
                    SourceArticleRecord(
                        url = "https://guardian.example/climate-summit-agreement",
                        publishedAt = baseTime.plusSeconds(1800),
                        languageTag = "en",
                        headline = "Geneva climate summit reaches landmark agreement",
                        excerpt = wireExcerpt,
                        contentPermission = ContentPermission.EXPLICIT_EXCERPT
                    )
                )
            )
        )
    }

    /**
     * Scenario 2: Near-copy with modified headline but same excerpt.
     * One outlet changed the headline but kept the wire excerpt.
     * Expected: MEDIUM-to-HIGH confidence via excerpt match.
     */
    fun createNearCopyWithChangedHeadline(baseTime: Instant): List<MockSourceAdapter> {
        val sharedExcerpt = "European Union officials announced new regulations on artificial " +
                "intelligence development at a Brussels conference today. The proposed framework " +
                "would require AI systems to undergo safety audits before deployment in critical " +
                "infrastructure sectors. Industry representatives expressed concerns about " +
                "implementation timelines and compliance costs."

        return listOf(
            MockSourceAdapter(
                sourceId = "reuters-demo",
                sourceName = "Reuters Demo",
                articles = listOf(
                    SourceArticleRecord(
                        url = "https://reuters.example/eu-ai-regulations",
                        publishedAt = baseTime,
                        languageTag = "en",
                        headline = "EU proposes new AI safety framework",
                        excerpt = sharedExcerpt,
                        contentPermission = ContentPermission.EXPLICIT_EXCERPT
                    )
                )
            ),
            MockSourceAdapter(
                sourceId = "ft-demo",
                sourceName = "Financial Times Demo",
                articles = listOf(
                    SourceArticleRecord(
                        url = "https://ft.example/brussels-ai-announcement",
                        publishedAt = baseTime.plusSeconds(3600),
                        languageTag = "en",
                        headline = "Brussels unveils regulatory approach to artificial intelligence",
                        excerpt = sharedExcerpt,
                        contentPermission = ContentPermission.EXPLICIT_EXCERPT
                    )
                )
            )
        )
    }

    /**
     * Scenario 3: Independent reporting of the same event.
     * Different perspectives, different excerpts, different details.
     * Expected: Should NOT be grouped as syndication; may cluster as same event.
     */
    fun createIndependentReporting(baseTime: Instant): List<MockSourceAdapter> {
        return listOf(
            MockSourceAdapter(
                sourceId = "nyt-demo",
                sourceName = "NYT Demo",
                articles = listOf(
                    SourceArticleRecord(
                        url = "https://nytimes.example/senate-vote-infrastructure",
                        publishedAt = baseTime,
                        languageTag = "en",
                        headline = "Senate approves infrastructure spending bill",
                        excerpt = "The United States Senate voted 68-32 to approve a comprehensive " +
                                "infrastructure spending package. The bill includes funding for roads, " +
                                "bridges, and broadband expansion. Republican and Democratic leaders " +
                                "celebrated the bipartisan achievement.",
                        contentPermission = ContentPermission.FAIR_USE_PREVIEW
                    )
                )
            ),
            MockSourceAdapter(
                sourceId = "wapo-demo",
                sourceName = "Washington Post Demo",
                articles = listOf(
                    SourceArticleRecord(
                        url = "https://washingtonpost.example/infrastructure-bill-senate",
                        publishedAt = baseTime.plusSeconds(900),
                        languageTag = "en",
                        headline = "Infrastructure bill passes Senate with bipartisan support",
                        excerpt = "After months of negotiations, the Senate passed a major infrastructure " +
                                "bill Tuesday evening. The legislation allocates hundreds of billions for " +
                                "transportation projects and digital infrastructure. Lobbyists and advocacy " +
                                "groups had mixed reactions to the final package.",
                        contentPermission = ContentPermission.FAIR_USE_PREVIEW
                    )
                )
            )
        )
    }

    /**
     * Scenario 4: Multilingual coverage that may or may not be syndicated.
     * Same title structure, different languages.
     * Expected: UNCERTAIN confidence (requires editorial review).
     */
    fun createMultilingualCoverage(baseTime: Instant): List<MockSourceAdapter> {
        return listOf(
            MockSourceAdapter(
                sourceId = "bbc-demo",
                sourceName = "BBC News Demo",
                articles = listOf(
                    SourceArticleRecord(
                        url = "https://bbc.example/olympics-opening",
                        publishedAt = baseTime,
                        languageTag = "en",
                        headline = "Paris Olympics opening ceremony draws global audience",
                        excerpt = "The opening ceremony of the Paris Olympic Games captivated millions " +
                                "of viewers worldwide on Friday evening. The spectacular four-hour event " +
                                "featured performances by French artists and athletes from participating nations.",
                        contentPermission = ContentPermission.EXPLICIT_EXCERPT
                    )
                )
            ),
            MockSourceAdapter(
                sourceId = "lemonde-demo",
                sourceName = "Le Monde Demo",
                articles = listOf(
                    SourceArticleRecord(
                        url = "https://lemonde.example/ceremonie-ouverture-jo",
                        publishedAt = baseTime.plusSeconds(1800),
                        languageTag = "fr",
                        headline = "La cérémonie d'ouverture des JO de Paris attire un public mondial",
                        excerpt = "La cérémonie d'ouverture des Jeux olympiques de Paris a captivé des " +
                                "millions de téléspectateurs dans le monde vendredi soir. L'événement spectaculaire " +
                                "de quatre heures a mis en vedette des artistes français et des athlètes des nations participantes.",
                        contentPermission = ContentPermission.EXPLICIT_EXCERPT
                    )
                )
            )
        )
    }

    /**
     * Scenario 5: Exact canonical URL duplicates with tracking parameters.
     * These should be caught by URL deduplication, not syndication detection.
     * Expected: Deduplicated before syndication analysis runs.
     */
    fun createUrlDuplicates(baseTime: Instant): List<MockSourceAdapter> {
        return listOf(
            MockSourceAdapter(
                sourceId = "bbc-demo",
                sourceName = "BBC News Demo",
                articles = listOf(
                    SourceArticleRecord(
                        url = "https://bbc.example/article-123",
                        publishedAt = baseTime,
                        languageTag = "en",
                        headline = "Breaking news story",
                        excerpt = "This is the original article.",
                        contentPermission = ContentPermission.EXPLICIT_EXCERPT
                    ),
                    SourceArticleRecord(
                        url = "https://bbc.example/article-123?utm_source=twitter&fbclid=abc123",
                        publishedAt = baseTime.plusSeconds(60),
                        languageTag = "en",
                        headline = "Breaking news story",
                        excerpt = "This is the original article.",
                        contentPermission = ContentPermission.EXPLICIT_EXCERPT
                    )
                )
            )
        )
    }

    /**
     * Scenario 6: Short excerpts that should not trigger high-confidence matches.
     * Brief excerpts may have coincidental overlap.
     * Expected: LOW confidence or no match.
     */
    fun createShortExcerpts(baseTime: Instant): List<MockSourceAdapter> {
        return listOf(
            MockSourceAdapter(
                sourceId = "bbc-demo",
                sourceName = "BBC News Demo",
                articles = listOf(
                    SourceArticleRecord(
                        url = "https://bbc.example/brief-1",
                        publishedAt = baseTime,
                        languageTag = "en",
                        headline = "Market update",
                        excerpt = "Stocks rose today on tech gains.",
                        contentPermission = ContentPermission.EXPLICIT_EXCERPT
                    )
                )
            ),
            MockSourceAdapter(
                sourceId = "reuters-demo",
                sourceName = "Reuters Demo",
                articles = listOf(
                    SourceArticleRecord(
                        url = "https://reuters.example/brief-2",
                        publishedAt = baseTime.plusSeconds(300),
                        languageTag = "en",
                        headline = "Markets close higher",
                        excerpt = "Stocks rose today on tech gains.",
                        contentPermission = ContentPermission.EXPLICIT_EXCERPT
                    )
                )
            )
        )
    }
}
