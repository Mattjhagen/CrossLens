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
            originalContent = """
                More than 150 nations have committed to accelerated emission reduction targets following two days of intensive negotiations at the emergency climate summit in Geneva.

                The unprecedented gathering brought together heads of state, environmental ministers, and climate scientists to address accelerating global warming trends. Summit chair Ambassador Elena Rodriguez called the agreements "a turning point in global climate cooperation."

                Key commitments include pledges to reduce carbon emissions by 40% by 2030, with wealthy nations promising $100 billion annually to support developing countries' transition to renewable energy. Several major economies announced plans to phase out coal power generation within the next decade.

                "This represents the strongest collective action on climate we have seen," said Dr. James Chen, lead climate scientist at the International Panel on Climate Change. "The commitments, if fully implemented, could limit warming to 1.8 degrees Celsius."

                Environmental advocates welcomed the announcements while emphasizing the importance of implementation and accountability measures. "Promises must become action," noted Greenpeace International director Maria Santos.

                The summit concluded with participating nations agreeing to annual progress reviews and the establishment of a climate implementation monitoring body.
            """.trimIndent(),
            attribution = "BBC News Staff",
            isDemo = true,
            requiresSubscription = false
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
            originalContent = """
                Les dirigeants mondiaux ont présenté des objectifs de réduction des émissions, mais les observateurs soulignent l'absence de mécanismes de contrôle contraignants.

                Le sommet d'urgence sur le climat réuni à Genève a abouti à des engagements qui, selon plusieurs experts, manquent de dispositifs contraignants pour assurer leur mise en œuvre effective.

                "Les promesses sont impressionnantes sur le papier", explique Claire Dubois, directrice de recherche au Centre européen d'études climatiques. "Mais sans mécanismes de vérification indépendants et de sanctions en cas de non-respect, l'histoire nous montre que les résultats sont souvent décevants."

                Les pays développés se sont engagés à financer la transition énergétique des pays en développement à hauteur de 100 milliards de dollars par an. Toutefois, des engagements similaires pris lors de précédents sommets n'ont jamais été pleinement honorés.

                Les organisations environnementales saluent néanmoins l'ambition affichée, tout en appelant à une vigilance accrue sur l'application concrète des mesures annoncées. "Nous avons besoin d'action, pas seulement de déclarations," insiste Jean-Paul Martin de Climate Action Network Europe.

                Le prochain rendez-vous climatique majeur aura lieu dans six mois, où les premiers rapports d'avancement devront être présentés.
            """.trimIndent(),
            attribution = "Le Monde",
            isDemo = true,
            requiresSubscription = true // Le Monde typically has a paywall
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
            originalContent = """
                While delegates celebrated new renewable energy commitments, environmental groups expressed skepticism about enforcement mechanisms at the conclusion of the Geneva climate summit.

                The two-day emergency session produced pledges from more than 150 countries to accelerate carbon emission reductions and transition to renewable energy sources. Yet the absence of binding enforcement provisions has raised concerns among climate advocates about whether these commitments will translate into meaningful action.

                "We've heard ambitious promises before," said Dr. Sarah Mitchell, climate policy director at the Environmental Defense Fund. "The critical question is always implementation and accountability."

                Developed nations committed to providing $100 billion annually to help developing countries build renewable energy infrastructure and adapt to climate impacts. However, similar funding commitments from previous climate agreements have fallen short of targets.

                Some participants highlighted positive elements of the agreements, including provisions for annual progress reviews and the creation of an implementation monitoring body. "These accountability measures, while not perfect, represent progress," noted Ambassador Chen Wei, who led negotiations for the Asia-Pacific bloc.

                Climate scientists cautioned that even full implementation of the announced commitments would require additional measures to meet the goals outlined in previous climate agreements. "This is a step forward, but we need to maintain pressure for stronger action," said Dr. Michael Torres of the Intergovernmental Panel on Climate Change.
            """.trimIndent(),
            attribution = "New York Times Climate Team",
            isDemo = true,
            requiresSubscription = true // NYT has a paywall
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
            originalContent = """
                The European Commission has unveiled a comprehensive artificial intelligence regulation proposal that would establish the world's first major regulatory framework for AI systems.

                The proposed legislation categorizes AI applications by risk level and introduces requirements for transparency, safety testing, and human oversight. High-risk AI systems, including those used in critical infrastructure, law enforcement, and hiring decisions, would face strict requirements before deployment.

                "This framework balances innovation with fundamental rights protection," said Commissioner Margrethe Vestager, who presented the proposal. "Europe can lead in setting global standards for trustworthy AI."

                Key provisions include mandatory risk assessments for high-risk AI systems, requirements for human oversight in critical decisions, and prohibitions on certain applications deemed to pose unacceptable risks to safety or rights, such as social scoring systems and real-time biometric identification in public spaces.

                Technology companies expressed mixed reactions. Some industry representatives welcomed clarity on regulatory expectations, while others raised concerns about compliance costs and potential impacts on innovation.

                The proposal now moves to the European Parliament and Council for review and potential amendments. If approved, the regulations would take effect across EU member states within two years, with companies facing substantial fines for non-compliance.
            """.trimIndent(),
            attribution = "BBC Technology",
            isDemo = true,
            requiresSubscription = false
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
