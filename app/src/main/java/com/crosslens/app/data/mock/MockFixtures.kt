package com.crosslens.app.data.mock

import com.crosslens.app.data.local.entity.*
import java.time.Instant
import java.time.temporal.ChronoUnit

object MockFixtures {
    private val baseTime = Instant.parse("2026-09-20T12:00:00Z")

    val sources = listOf(
        SourceEntity(
            id = "bbc",
            name = "BBC News",
            homepage = "https://www.bbc.com/news",
            countryCodes = listOf("GB"),
            regionIds = listOf("europe"),
            defaultLanguages = listOf("en")
        ),
        SourceEntity(
            id = "lemonde",
            name = "Le Monde",
            homepage = "https://www.lemonde.fr",
            countryCodes = listOf("FR"),
            regionIds = listOf("europe"),
            defaultLanguages = listOf("fr")
        ),
        SourceEntity(
            id = "aljazeera",
            name = "Al Jazeera",
            homepage = "https://www.aljazeera.com",
            countryCodes = listOf("QA"),
            regionIds = listOf("middle_east"),
            defaultLanguages = listOf("ar", "en")
        ),
        SourceEntity(
            id = "nyt",
            name = "The New York Times",
            homepage = "https://www.nytimes.com",
            countryCodes = listOf("US"),
            regionIds = listOf("north_america"),
            defaultLanguages = listOf("en")
        ),
        SourceEntity(
            id = "globe",
            name = "The Globe and Mail",
            homepage = "https://www.theglobeandmail.com",
            countryCodes = listOf("CA"),
            regionIds = listOf("north_america"),
            defaultLanguages = listOf("en", "fr")
        ),
        SourceEntity(
            id = "yomiuri",
            name = "読売新聞",
            homepage = "https://www.yomiuri.co.jp",
            countryCodes = listOf("JP"),
            regionIds = listOf("asia"),
            defaultLanguages = listOf("ja")
        )
    )

    val stories = listOf(
        StoryEntity(
            id = "climate-summit-2026",
            title = "Global Leaders Convene for Emergency Climate Summit",
            summary = "Representatives from over 150 countries gathered in Geneva for an emergency session addressing accelerated warming trends and renewable energy commitments.",
            eventTime = baseTime.minus(2, ChronoUnit.DAYS),
            updatedTime = baseTime.minus(1, ChronoUnit.HOURS),
            topicIds = listOf("environment", "politics"),
            eventCountryCodes = listOf("CH", "GLOBAL"),
            articleIds = listOf("climate-bbc", "climate-lemonde", "climate-aljazeera", "climate-nyt"),
            claimIds = listOf("claim-climate-1", "claim-climate-2"),
            lensGapScore = 68,
            lensGapStatus = "AVAILABLE",
            lensGapIsDemo = true
        ),
        StoryEntity(
            id = "tech-regulation",
            title = "EU Proposes New AI Regulation Framework",
            summary = "European Commission unveils comprehensive artificial intelligence regulation proposal focusing on transparency, safety, and fundamental rights protection.",
            eventTime = baseTime.minus(1, ChronoUnit.DAYS),
            updatedTime = baseTime.minus(3, ChronoUnit.HOURS),
            topicIds = listOf("technology", "regulation"),
            eventCountryCodes = listOf("BE", "EU"),
            articleIds = listOf("tech-bbc", "tech-lemonde", "tech-nyt"),
            claimIds = listOf("claim-tech-1"),
            lensGapScore = 42,
            lensGapStatus = "AVAILABLE",
            lensGapIsDemo = true
        ),
        StoryEntity(
            id = "trade-agreement",
            title = "North American Trade Negotiation Update",
            summary = "Canada, US, and Mexico reach preliminary understanding on digital trade provisions amid ongoing negotiations.",
            eventTime = baseTime.minus(6, ChronoUnit.HOURS),
            updatedTime = baseTime.minus(2, ChronoUnit.HOURS),
            topicIds = listOf("economics", "trade"),
            eventCountryCodes = listOf("CA", "US", "MX"),
            articleIds = listOf("trade-nyt", "trade-globe"),
            claimIds = emptyList(),
            lensGapScore = null,
            lensGapStatus = "INSUFFICIENT_COVERAGE",
            lensGapIsDemo = true
        )
    )

    val articles = listOf(
        ArticleEntity(
            id = "climate-bbc",
            storyId = "climate-summit-2026",
            sourceId = "bbc",
            originalUrl = "https://demo.example/bbc-climate",
            publishedTime = baseTime.minus(2, ChronoUnit.DAYS),
            originalLanguage = "en",
            originalHeadline = "World Leaders Unite for Climate Action in Geneva",
            originalExcerpt = "More than 150 nations have committed to accelerated emission reduction targets following two days of intensive negotiations at the emergency climate summit.",
            attribution = "BBC News Staff",
            isDemo = true
        ),
        ArticleEntity(
            id = "climate-lemonde",
            storyId = "climate-summit-2026",
            sourceId = "lemonde",
            originalUrl = "https://demo.example/lemonde-climat",
            publishedTime = baseTime.minus(2, ChronoUnit.DAYS).plus(3, ChronoUnit.HOURS),
            originalLanguage = "fr",
            originalHeadline = "Sommet climatique à Genève : engagements ambitieux mais questions sur la mise en œuvre",
            originalExcerpt = "Les dirigeants mondiaux ont présenté des objectifs de réduction des émissions, mais les observateurs soulignent l'absence de mécanismes de contrôle contraignants.",
            attribution = "Le Monde",
            isDemo = true
        ),
        ArticleEntity(
            id = "climate-nyt",
            storyId = "climate-summit-2026",
            sourceId = "nyt",
            originalUrl = "https://demo.example/nyt-climate",
            publishedTime = baseTime.minus(2, ChronoUnit.DAYS).plus(5, ChronoUnit.HOURS),
            originalLanguage = "en",
            originalHeadline = "Climate Summit Yields Promises, Implementation Questions Remain",
            originalExcerpt = "While delegates celebrated new renewable energy commitments, environmental groups expressed skepticism about enforcement mechanisms.",
            attribution = "New York Times Climate Team",
            isDemo = true
        ),
        ArticleEntity(
            id = "tech-bbc",
            storyId = "tech-regulation",
            sourceId = "bbc",
            originalUrl = "https://demo.example/bbc-ai",
            publishedTime = baseTime.minus(1, ChronoUnit.DAYS),
            originalLanguage = "en",
            originalHeadline = "EU Unveils Landmark AI Regulation Proposal",
            originalExcerpt = "The European Commission's proposal would require transparency in AI systems and prohibit certain high-risk applications.",
            attribution = "BBC Technology",
            isDemo = true
        )
    )

    val translations = listOf(
        TranslationEntity(
            id = "trans-lemonde-en",
            articleId = "climate-lemonde",
            sourceLanguage = "fr",
            targetLanguage = "en",
            translatedHeadline = "Geneva Climate Summit: Ambitious Commitments but Questions About Implementation",
            translatedExcerpt = "World leaders presented emission reduction targets, but observers highlight the absence of binding oversight mechanisms.",
            status = "AVAILABLE",
            method = "demo_manual",
            provider = "Demo",
            generatedTime = baseTime
        )
    )

    val claims = listOf(
        ClaimEntity(
            id = "claim-climate-1",
            storyId = "climate-summit-2026",
            statement = "Over 150 nations committed to new emission reduction targets",
            assessment = "CORROBORATED",
            supportingArticleIds = listOf("climate-bbc", "climate-lemonde", "climate-nyt"),
            contradictingArticleIds = emptyList(),
            assessmentProvenance = "Multiple independent sources confirm attendance and commitment announcements"
        ),
        ClaimEntity(
            id = "claim-climate-2",
            storyId = "climate-summit-2026",
            statement = "Binding enforcement mechanisms were established",
            assessment = "DISPUTED",
            supportingArticleIds = emptyList(),
            contradictingArticleIds = listOf("climate-lemonde", "climate-nyt"),
            assessmentProvenance = "Sources report absence of binding oversight"
        )
    )

    val frameObservations = listOf(
        FrameObservationEntity(
            id = "frame-climate-1",
            storyId = "climate-summit-2026",
            articleIds = listOf("climate-bbc"),
            emphasizedActors = listOf("World leaders", "Nations"),
            emphasizedClaims = listOf("Unity", "Commitment"),
            languageObservation = "Optimistic framing emphasizing cooperation",
            sentimentObservation = "Positive",
            evidenceReferences = listOf("climate-bbc"),
            methodVersion = "demo-v1"
        ),
        FrameObservationEntity(
            id = "frame-climate-2",
            storyId = "climate-summit-2026",
            articleIds = listOf("climate-lemonde", "climate-nyt"),
            emphasizedActors = listOf("Environmental observers", "Skeptics"),
            emphasizedClaims = listOf("Implementation questions", "Enforcement gaps"),
            languageObservation = "Critical framing questioning effectiveness",
            sentimentObservation = "Skeptical",
            evidenceReferences = listOf("climate-lemonde", "climate-nyt"),
            methodVersion = "demo-v1"
        )
    )
}
