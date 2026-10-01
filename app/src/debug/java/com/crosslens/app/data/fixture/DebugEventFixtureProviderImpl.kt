package com.crosslens.app.data.fixture

import com.crosslens.app.data.ingestion.ContentPermission
import com.crosslens.app.data.ingestion.SourceArticleRecord
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DEBUG BUILD ONLY: Provides test fixture for Read Across Coverage verification.
 *
 * Creates a deterministic 4-article event representing the same specific
 * international news story from 4 distinct publishers.
 *
 * This implementation is ONLY compiled into debug builds. Release builds
 * use the no-op implementation that returns empty lists.
 *
 * Event: France-Germany Defense Treaty signed in Paris by Macron and Scholz
 * - 4 distinct publishers (US, UK, France, Germany)
 * - All articles describe the same specific treaty signing
 * - Designed to pass conservative clustering thresholds:
 *   * 2+ shared entities: "Emmanuel Macron", "Olaf Scholz", "France", "Germany", "Paris"
 *   * High headline similarity (20%+ after normalization)
 *   * Within 24 hours
 *   * 4 distinct publishers
 *
 * Verification Purpose:
 * - Prove that fixture forms ONE 4-publisher cluster
 * - Test Read Across Coverage UI with multi-source event
 * - Verify event comparison shows all 4 articles
 * - Confirm attribution preservation
 * - Validate factual explanations (country, language, source type)
 */
@Singleton
class DebugEventFixtureProviderImpl @Inject constructor() : DebugEventFixtureProvider {

    companion object {
        // Fixture articles use timestamps 2 hours ago to appear fresh in feed
        private val BASE_TIME: Instant = Instant.now().minus(2, ChronoUnit.HOURS)

        // Fixture URLs use test domain to prevent accidental real navigation
        private const val FIXTURE_URL_BASE = "https://test.crosslens.fixture"
    }

    override fun getReadAcrossFixture(): List<SourceArticleRecord> {
        return listOf(
            // Article 1: BBC (UK, English)
            // Entities: Emmanuel Macron, Olaf Scholz, France, Germany, Paris
            // Normalized tokens: emmanuel, macron, olaf, scholz, france, germany, sign, defense, treaty, paris
            SourceArticleRecord(
                url = "$FIXTURE_URL_BASE/bbc/france-germany-treaty",
                publishedAt = BASE_TIME,
                languageTag = "en-GB",
                headline = "Emmanuel Macron and Olaf Scholz sign new France-Germany defense treaty in Paris",
                excerpt = "French President Emmanuel Macron and German Chancellor Olaf Scholz signed a landmark bilateral defense treaty in Paris on Tuesday. The agreement strengthens military cooperation between France and Germany, establishing joint command structures and shared defense procurement. Both leaders hailed the treaty as a cornerstone of European security architecture.",
                contentPermission = ContentPermission.EXPLICIT_EXCERPT,
                imageUrl = null,
                sourceId = "bbc-news-rss"
            ),

            // Article 2: New York Times (US, English)
            // Entities: France, Germany, Emmanuel Macron, Olaf Scholz, Paris
            // Normalized tokens: france, germany, defense, pact, macron, scholz, paris, sign
            SourceArticleRecord(
                url = "$FIXTURE_URL_BASE/nyt/france-germany-pact",
                publishedAt = BASE_TIME.plus(20, ChronoUnit.MINUTES),
                languageTag = "en-US",
                headline = "France and Germany seal defense pact as Macron and Scholz meet in Paris",
                excerpt = "President Emmanuel Macron of France and Chancellor Olaf Scholz of Germany formalized a sweeping defense agreement during a Paris summit Tuesday. The pact commits both nations to joint military planning, shared defense technology development, and mutual security guarantees. Analysts view the treaty as Europe's most significant defense partnership since the Cold War.",
                contentPermission = ContentPermission.EXPLICIT_EXCERPT,
                imageUrl = null,
                sourceId = "nytimes-rss"
            ),

            // Article 3: Deutsche Welle (Germany, English)
            // Entities: Olaf Scholz, Emmanuel Macron, Germany, France, Paris
            // Normalized tokens: scholz, macron, sign, germany, france, defense, agreement, paris
            SourceArticleRecord(
                url = "$FIXTURE_URL_BASE/dw/scholz-macron-treaty",
                publishedAt = BASE_TIME.plus(35, ChronoUnit.MINUTES),
                languageTag = "en",
                headline = "Scholz and Macron sign historic Germany-France defense agreement in Paris",
                excerpt = "Chancellor Olaf Scholz and President Emmanuel Macron signed a comprehensive defense treaty in the French capital on Tuesday morning. The Germany-France agreement includes provisions for joint rapid reaction forces, integrated air defense systems, and coordinated defense industry investments. German officials described the pact as a strategic milestone for European defense autonomy.",
                contentPermission = ContentPermission.EXPLICIT_EXCERPT,
                imageUrl = null,
                sourceId = "dw-rss"
            ),

            // Article 4: The Guardian (UK, English)
            // Entities: Emmanuel Macron, Olaf Scholz, Paris, France, Germany
            // Normalized tokens: macron, scholz, unveil, france, germany, defense, treaty, paris
            SourceArticleRecord(
                url = "$FIXTURE_URL_BASE/guardian/macron-scholz-pact",
                publishedAt = BASE_TIME.plus(50, ChronoUnit.MINUTES),
                languageTag = "en-GB",
                headline = "Macron and Scholz unveil France-Germany defense treaty at Paris ceremony",
                excerpt = "Emmanuel Macron and Olaf Scholz presented a new bilateral defense treaty during a formal ceremony in Paris on Tuesday. The France-Germany pact establishes unprecedented military integration between the two nations, including joint command structures and shared procurement programs. Security experts called the treaty the most ambitious Franco-German defense initiative in decades.",
                contentPermission = ContentPermission.EXPLICIT_EXCERPT,
                imageUrl = null,
                sourceId = "guardian-rss"
            )
        )
    }
}
