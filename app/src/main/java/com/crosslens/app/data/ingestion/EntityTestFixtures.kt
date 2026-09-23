package com.crosslens.app.data.ingestion

import java.time.Instant

/**
 * Test fixtures for entity extraction and cross-language event matching.
 * All entities and content are fictional mock data for testing detection logic.
 */
object EntityTestFixtures {

    /**
     * Scenario 1: Same event (Geneva Climate Summit) covered in English, French, and Arabic.
     * All three articles share key entities: event identifier, location, organization, person.
     * Expected: HIGH confidence candidate match across all three languages.
     */
    fun createGenevaSummitMultilingual(baseTime: Instant): Pair<List<MockSourceAdapter>, Map<String, List<Entity>>> {
        val adapters = listOf(
            MockSourceAdapter(
                sourceId = "bbc-demo",
                sourceName = "BBC News Demo",
                articles = listOf(
                    SourceArticleRecord(
                        url = "https://bbc.example/geneva-climate-summit",
                        publishedAt = baseTime,
                        languageTag = "en",
                        headline = "Geneva Climate Summit reaches landmark agreement",
                        excerpt = "World leaders at the Geneva Climate Summit announced a breakthrough " +
                                "agreement on emission targets. UN Secretary-General welcomed the deal.",
                        contentPermission = ContentPermission.EXPLICIT_EXCERPT
                    )
                )
            ),
            MockSourceAdapter(
                sourceId = "lemonde-demo",
                sourceName = "Le Monde Demo",
                articles = listOf(
                    SourceArticleRecord(
                        url = "https://lemonde.example/sommet-climat-geneve",
                        publishedAt = baseTime.plusSeconds(1800),
                        languageTag = "fr",
                        headline = "Le sommet de Genève aboutit à un accord historique sur le climat",
                        excerpt = "Les dirigeants mondiaux au Sommet climatique de Genève ont annoncé " +
                                "un accord révolutionnaire sur les objectifs d'émissions. Le Secrétaire général de l'ONU a salué l'accord.",
                        contentPermission = ContentPermission.EXPLICIT_EXCERPT
                    )
                )
            ),
            MockSourceAdapter(
                sourceId = "aljazeera-demo",
                sourceName = "Al Jazeera Demo",
                articles = listOf(
                    SourceArticleRecord(
                        url = "https://aljazeera.example/geneva-climate-agreement",
                        publishedAt = baseTime.plusSeconds(3600),
                        languageTag = "ar",
                        headline = "قمة المناخ في جنيف تحقق اتفاقًا تاريخيًا",
                        excerpt = "أعلن قادة العالم في قمة المناخ في جنيف عن اتفاق تاريخي " +
                                "بشأن أهداف الانبعاثات. رحب الأمين العام للأمم المتحدة بالاتفاق.",
                        contentPermission = ContentPermission.EXPLICIT_EXCERPT
                    )
                )
            )
        )

        val entityMap = mapOf(
            // BBC article entities (English)
            "article-" + "bbc-demo|https://bbc.example/geneva-climate-summit".hashCode().toUInt().toString(16).take(16) to listOf(
                EventIdentifierEntity(
                    id = "event:geneva-climate-summit-2026",
                    displayName = "Geneva Climate Summit 2026",
                    sourceLanguage = "en",
                    eventType = "summit"
                ),
                LocationEntity(
                    id = "location:geneva",
                    displayName = "Geneva",
                    sourceLanguage = "en",
                    countryCode = "CH"
                ),
                OrganizationEntity(
                    id = "org:un",
                    displayName = "United Nations",
                    sourceLanguage = "en",
                    orgType = "international"
                ),
                PersonEntity(
                    id = "person:un-secretary-general",
                    displayName = "UN Secretary-General",
                    sourceLanguage = "en",
                    role = "Secretary-General"
                )
            ),
            // Le Monde article entities (French)
            "article-" + "lemonde-demo|https://lemonde.example/sommet-climat-geneve".hashCode().toUInt().toString(16).take(16) to listOf(
                EventIdentifierEntity(
                    id = "event:geneva-climate-summit-2026",
                    displayName = "Sommet climatique de Genève 2026",
                    sourceLanguage = "fr",
                    eventType = "summit"
                ),
                LocationEntity(
                    id = "location:geneva",
                    displayName = "Genève",
                    sourceLanguage = "fr",
                    countryCode = "CH"
                ),
                OrganizationEntity(
                    id = "org:un",
                    displayName = "ONU",
                    sourceLanguage = "fr",
                    orgType = "international"
                ),
                PersonEntity(
                    id = "person:un-secretary-general",
                    displayName = "Secrétaire général de l'ONU",
                    sourceLanguage = "fr",
                    role = "Secretary-General"
                )
            ),
            // Al Jazeera article entities (Arabic)
            "article-" + "aljazeera-demo|https://aljazeera.example/geneva-climate-agreement".hashCode().toUInt().toString(16).take(16) to listOf(
                EventIdentifierEntity(
                    id = "event:geneva-climate-summit-2026",
                    displayName = "قمة المناخ في جنيف 2026",
                    sourceLanguage = "ar",
                    eventType = "summit"
                ),
                LocationEntity(
                    id = "location:geneva",
                    displayName = "جنيف",
                    sourceLanguage = "ar",
                    countryCode = "CH"
                ),
                OrganizationEntity(
                    id = "org:un",
                    displayName = "الأمم المتحدة",
                    sourceLanguage = "ar",
                    orgType = "international"
                ),
                PersonEntity(
                    id = "person:un-secretary-general",
                    displayName = "الأمين العام للأمم المتحدة",
                    sourceLanguage = "ar",
                    role = "Secretary-General"
                )
            )
        )

        return adapters to entityMap
    }

    /**
     * Scenario 2: Different events that happen to share some generic entities.
     * Brussels EU meeting and Washington congressional hearing both mention "climate" but are different events.
     * Expected: Should NOT be grouped or only LOW confidence.
     */
    fun createDifferentEventsGenericEntities(baseTime: Instant): Pair<List<MockSourceAdapter>, Map<String, List<Entity>>> {
        val adapters = listOf(
            MockSourceAdapter(
                sourceId = "ft-demo",
                sourceName = "Financial Times Demo",
                articles = listOf(
                    SourceArticleRecord(
                        url = "https://ft.example/eu-climate-policy",
                        publishedAt = baseTime,
                        languageTag = "en",
                        headline = "EU announces new climate policy framework in Brussels",
                        excerpt = "European Union officials unveiled a comprehensive climate policy " +
                                "framework at Brussels headquarters on Monday.",
                        contentPermission = ContentPermission.EXPLICIT_EXCERPT
                    )
                )
            ),
            MockSourceAdapter(
                sourceId = "wapo-demo",
                sourceName = "Washington Post Demo",
                articles = listOf(
                    SourceArticleRecord(
                        url = "https://washingtonpost.example/congress-climate-hearing",
                        publishedAt = baseTime.plusSeconds(7200),
                        languageTag = "en",
                        headline = "Congress holds hearing on climate legislation",
                        excerpt = "Members of Congress questioned witnesses on proposed climate " +
                                "legislation during a contentious hearing in Washington.",
                        contentPermission = ContentPermission.FAIR_USE_PREVIEW
                    )
                )
            )
        )

        val entityMap = mapOf(
            "article-" + "ft-demo|https://ft.example/eu-climate-policy".hashCode().toUInt().toString(16).take(16) to listOf(
                EventIdentifierEntity(
                    id = "event:eu-climate-policy-2026",
                    displayName = "EU Climate Policy Announcement 2026",
                    sourceLanguage = "en",
                    eventType = "announcement"
                ),
                LocationEntity(
                    id = "location:brussels",
                    displayName = "Brussels",
                    sourceLanguage = "en",
                    countryCode = "BE"
                ),
                OrganizationEntity(
                    id = "org:european-union",
                    displayName = "European Union",
                    sourceLanguage = "en",
                    orgType = "government"
                )
            ),
            "article-" + "wapo-demo|https://washingtonpost.example/congress-climate-hearing".hashCode().toUInt().toString(16).take(16) to listOf(
                EventIdentifierEntity(
                    id = "event:congress-climate-hearing-2026",
                    displayName = "Congressional Climate Hearing 2026",
                    sourceLanguage = "en",
                    eventType = "hearing"
                ),
                LocationEntity(
                    id = "location:washington",
                    displayName = "Washington",
                    sourceLanguage = "en",
                    countryCode = "US"
                ),
                OrganizationEntity(
                    id = "org:us-congress",
                    displayName = "US Congress",
                    sourceLanguage = "en",
                    orgType = "government"
                )
            )
        )

        return adapters to entityMap
    }

    /**
     * Scenario 3: Same event but no shared entity evidence (different focus/entities).
     * Both about Paris Olympics opening ceremony but one focuses on French president, other on athletes.
     * Expected: No entity-based match (would rely on title similarity clustering instead).
     */
    fun createSameEventNoSharedEntities(baseTime: Instant): Pair<List<MockSourceAdapter>, Map<String, List<Entity>>> {
        val adapters = listOf(
            MockSourceAdapter(
                sourceId = "bbc-demo",
                sourceName = "BBC News Demo",
                articles = listOf(
                    SourceArticleRecord(
                        url = "https://bbc.example/paris-olympics-president",
                        publishedAt = baseTime,
                        languageTag = "en",
                        headline = "French President opens Paris Olympics with rousing speech",
                        excerpt = "The French President delivered an inspiring opening address at the " +
                                "Paris Olympic Games, emphasizing unity and sportsmanship.",
                        contentPermission = ContentPermission.EXPLICIT_EXCERPT
                    )
                )
            ),
            MockSourceAdapter(
                sourceId = "lemonde-demo",
                sourceName = "Le Monde Demo",
                articles = listOf(
                    SourceArticleRecord(
                        url = "https://lemonde.example/jo-paris-athletes",
                        publishedAt = baseTime.plusSeconds(1800),
                        languageTag = "fr",
                        headline = "Les athlètes défilent lors de la cérémonie d'ouverture des JO de Paris",
                        excerpt = "Des milliers d'athlètes du monde entier ont défilé lors de la " +
                                "cérémonie d'ouverture spectaculaire des Jeux olympiques de Paris.",
                        contentPermission = ContentPermission.EXPLICIT_EXCERPT
                    )
                )
            )
        )

        val entityMap = mapOf(
            "article-" + "bbc-demo|https://bbc.example/paris-olympics-president".hashCode().toUInt().toString(16).take(16) to listOf(
                PersonEntity(
                    id = "person:french-president",
                    displayName = "French President",
                    sourceLanguage = "en",
                    role = "President"
                ),
                LocationEntity(
                    id = "location:paris",
                    displayName = "Paris",
                    sourceLanguage = "en",
                    countryCode = "FR"
                )
            ),
            "article-" + "lemonde-demo|https://lemonde.example/jo-paris-athletes".hashCode().toUInt().toString(16).take(16) to listOf(
                EventIdentifierEntity(
                    id = "event:paris-olympics-2026",
                    displayName = "Jeux olympiques de Paris 2026",
                    sourceLanguage = "fr",
                    eventType = "olympics"
                ),
                LocationEntity(
                    id = "location:paris",
                    displayName = "Paris",
                    sourceLanguage = "fr",
                    countryCode = "FR"
                )
                // Note: Only one shared entity (location:paris) - below minimum threshold
            )
        )

        return adapters to entityMap
    }

    /**
     * Scenario 4: Articles sharing very generic entities across languages.
     * Both mention "United States" and "China" but are about different topics.
     * Expected: LOW confidence or no match due to generic entity detection.
     */
    fun createGenericSharedTerms(baseTime: Instant): Pair<List<MockSourceAdapter>, Map<String, List<Entity>>> {
        val adapters = listOf(
            MockSourceAdapter(
                sourceId = "nyt-demo",
                sourceName = "NYT Demo",
                articles = listOf(
                    SourceArticleRecord(
                        url = "https://nytimes.example/us-china-trade",
                        publishedAt = baseTime,
                        languageTag = "en",
                        headline = "US and China reach preliminary trade agreement",
                        excerpt = "United States and Chinese negotiators announced a preliminary " +
                                "agreement on trade terms after months of negotiations.",
                        contentPermission = ContentPermission.FAIR_USE_PREVIEW
                    )
                )
            ),
            MockSourceAdapter(
                sourceId = "lemonde-demo",
                sourceName = "Le Monde Demo",
                articles = listOf(
                    SourceArticleRecord(
                        url = "https://lemonde.example/us-chine-tech",
                        publishedAt = baseTime.plusSeconds(86400), // Next day
                        languageTag = "fr",
                        headline = "Les États-Unis et la Chine en compétition dans le secteur technologique",
                        excerpt = "La rivalité entre les États-Unis et la Chine s'intensifie dans " +
                                "le domaine de l'intelligence artificielle et des semi-conducteurs.",
                        contentPermission = ContentPermission.EXPLICIT_EXCERPT
                    )
                )
            )
        )

        val entityMap = mapOf(
            "article-" + "nyt-demo|https://nytimes.example/us-china-trade".hashCode().toUInt().toString(16).take(16) to listOf(
                EventIdentifierEntity(
                    id = "event:us-china-trade-deal-2026",
                    displayName = "US-China Trade Agreement 2026",
                    sourceLanguage = "en",
                    eventType = "agreement"
                ),
                LocationEntity(
                    id = "location:washington",  // Generic location
                    displayName = "United States",
                    sourceLanguage = "en",
                    countryCode = "US"
                ),
                LocationEntity(
                    id = "location:beijing",  // Generic location
                    displayName = "China",
                    sourceLanguage = "en",
                    countryCode = "CN"
                )
            ),
            "article-" + "lemonde-demo|https://lemonde.example/us-chine-tech".hashCode().toUInt().toString(16).take(16) to listOf(
                EventIdentifierEntity(
                    id = "event:us-china-tech-competition-2026",
                    displayName = "Compétition technologique États-Unis-Chine 2026",
                    sourceLanguage = "fr",
                    eventType = "competition"
                ),
                LocationEntity(
                    id = "location:washington",  // Same generic location
                    displayName = "États-Unis",
                    sourceLanguage = "fr",
                    countryCode = "US"
                ),
                LocationEntity(
                    id = "location:beijing",  // Same generic location
                    displayName = "Chine",
                    sourceLanguage = "fr",
                    countryCode = "CN"
                )
            )
        )

        return adapters to entityMap
    }
}
