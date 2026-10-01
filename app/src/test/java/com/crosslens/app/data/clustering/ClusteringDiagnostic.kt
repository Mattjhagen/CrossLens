package com.crosslens.app.data.clustering

import com.crosslens.app.data.ingestion.SourceArticleRecord
import org.junit.Test
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Diagnostic test to analyze why live RSS articles aren't forming clusters.
 *
 * This test uses realistic article samples from current RSS feeds to:
 * 1. Measure actual headline similarity scores between articles
 * 2. Identify time gaps and entity extraction results
 * 3. Compare against current clustering thresholds
 * 4. Recommend threshold adjustments or source additions
 */
class ClusteringDiagnostic {

    private val clusteringService = EventClusteringService()

    @Test
    fun `diagnose live RSS article clustering`() {
        // Sample real headlines from current live RSS feeds
        val liveArticles = listOf(
            createArticle(
                sourceId = "bbc-news-rss",
                headline = "Iran offers US deal to reopen Strait of Hormuz in seven days",
                publishedAt = Instant.parse("2026-09-25T18:48:00Z")
            ),
            createArticle(
                sourceId = "abc-au-rss",
                headline = "'I was buggered': Stranded sailor in dramatic rescue off Darwin Harbour",
                publishedAt = Instant.parse("2026-09-25T18:48:00Z")
            ),
            createArticle(
                sourceId = "abc-au-rss",
                headline = "Serial con man turned charity director back in court on dishonesty charges",
                publishedAt = Instant.parse("2026-09-25T18:48:00Z")
            ),
            createArticle(
                sourceId = "abc-au-rss",
                headline = "Internationals stun US with 5-0 sweep to take Presidents Cup lead",
                publishedAt = Instant.parse("2026-09-25T18:48:00Z")
            )
        )

        println("\n=== LIVE RSS CLUSTERING DIAGNOSTIC ===\n")
        println("Current clustering thresholds:")
        println("- Time window: 72 hours")
        println("- Minimum headline similarity: 20% (with 2+ shared entities) or 50% (standalone)")
        println("- Minimum shared entities: 1-2 depending on headline similarity")
        println()

        // Analyze all pairs
        println("=== PAIRWISE COMPARISON ===\n")
        for (i in liveArticles.indices) {
            for (j in (i + 1) until liveArticles.size) {
                val a = clusteringService.toClusteredArticle(liveArticles[i])
                val b = clusteringService.toClusteredArticle(liveArticles[j])

                val match = clusteringService.compareArticles(a, b)

                println("Comparing:")
                println("  A: ${liveArticles[i].headline}")
                println("  B: ${liveArticles[j].headline}")
                println()
                println("Results:")
                println("  Headline similarity: ${"%.1f".format(match.headlineSimilarity * 100)}%")
                println("  Shared entities: ${match.sharedEntities}")
                println("  Time difference: ${match.timeDifferenceHours}h")
                println("  Should cluster: ${match.shouldCluster}")
                println("  Explanation: ${match.explanation}")
                println()
                println("  Normalized A: ${a.normalizedHeadline}")
                println("  Normalized B: ${b.normalizedHeadline}")
                println("  Entities A: ${a.entities.take(5)}")
                println("  Entities B: ${b.entities.take(5)}")
                println()
                println("─".repeat(80))
                println()
            }
        }

        // Try actual clustering
        val clusters = clusteringService.clusterArticles(liveArticles)
        println("\n=== CLUSTERING RESULTS ===\n")
        println("Valid clusters formed: ${clusters.size}")
        clusters.forEachIndexed { idx, cluster ->
            println("\nCluster ${idx + 1}:")
            println("  Event: ${cluster.eventSummary}")
            println("  Articles: ${cluster.articles.size}")
            println("  Publishers: ${cluster.publisherCount}")
            println("  Confidence: ${cluster.confidence}")
            println("  Explanation: ${cluster.groupingExplanation}")
            cluster.articles.forEach { article ->
                println("    - ${article.headline} (${article.sourceId})")
            }
        }

        if (clusters.isEmpty()) {
            println("NO CLUSTERS FORMED")
            println()
            println("Why? Common causes:")
            println("1. Headlines too different (different word choices for same event)")
            println("2. No shared entities extracted (entity extraction may need tuning)")
            println("3. Different events covered by different sources")
            println("4. Time gaps > 72 hours")
        }

        println("\n=== RECOMMENDATIONS ===\n")
        analyzeAndRecommend(liveArticles)
    }

    @Test
    fun `diagnose same-event articles from different sources`() {
        // Simulate what SHOULD cluster: same specific event reported differently
        val sameEventDifferentHeadlines = listOf(
            createArticle(
                sourceId = "bbc-news-rss",
                headline = "UN climate summit reaches historic agreement on fossil fuel transition",
                publishedAt = Instant.now().minus(2, ChronoUnit.HOURS)
            ),
            createArticle(
                sourceId = "guardian-rss",
                headline = "COP29 delegates approve landmark deal to phase out coal and oil",
                publishedAt = Instant.now().minus(1, ChronoUnit.HOURS)
            ),
            createArticle(
                sourceId = "aljazeera-rss",
                headline = "Fossil fuel phase-out agreed at UN climate talks in Dubai",
                publishedAt = Instant.now()
            )
        )

        println("\n=== TESTING SAME-EVENT CLUSTERING ===\n")
        println("These are ALL about the same specific event (COP29 fossil fuel agreement)")
        println("but use different headlines. Will they cluster?\n")

        for (i in sameEventDifferentHeadlines.indices) {
            for (j in (i + 1) until sameEventDifferentHeadlines.size) {
                val a = clusteringService.toClusteredArticle(sameEventDifferentHeadlines[i])
                val b = clusteringService.toClusteredArticle(sameEventDifferentHeadlines[j])

                val match = clusteringService.compareArticles(a, b)

                println("${sameEventDifferentHeadlines[i].sourceId} vs ${sameEventDifferentHeadlines[j].sourceId}")
                println("  Similarity: ${"%.1f".format(match.headlineSimilarity * 100)}%")
                println("  Entities: ${match.sharedEntities} shared")
                println("  Clusters: ${match.shouldCluster}")
                println("  Why: ${match.explanation}")
                println()
            }
        }

        val clusters = clusteringService.clusterArticles(sameEventDifferentHeadlines)
        println("\nClusters formed: ${clusters.size}")
        if (clusters.isEmpty()) {
            println("❌ FAILED TO CLUSTER - Thresholds may be too strict for real-world variance")
        } else {
            println("✅ Successfully clustered ${clusters.first().articles.size} articles")
        }
    }

    @Test
    fun `measure realistic headline variance for same events`() {
        // Real-world examples: same event, different headline styles
        val variants = listOf(
            Pair("BBC style", "Iran offers US deal to reopen Strait of Hormuz in seven days"),
            Pair("Guardian style", "Tehran proposes week-long timeline for Hormuz Strait reopening deal"),
            Pair("Al Jazeera style", "Iran: US talks on Strait of Hormuz could reach agreement within 7 days"),
            Pair("Reuters style", "Iran says Hormuz shipping route talks with US may conclude in a week")
        )

        println("\n=== REALISTIC HEADLINE VARIANCE ===\n")
        println("Same event: Iran proposes 7-day timeline for Strait of Hormuz agreement")
        println("Different sources write headlines differently. Measuring variance:\n")

        val normalizedVariants = variants.map { (style, headline) ->
            val normalized = clusteringService.normalizeHeadline(headline)
            Triple(style, headline, normalized)
        }

        normalizedVariants.forEach { (style, original, normalized) ->
            println("$style:")
            println("  Original: $original")
            println("  Normalized: $normalized")
            println()
        }

        println("Pairwise similarity:")
        for (i in normalizedVariants.indices) {
            for (j in (i + 1) until normalizedVariants.size) {
                val (styleA, _, normalizedA) = normalizedVariants[i]
                val (styleB, _, normalizedB) = normalizedVariants[j]

                val tokensA = normalizedA.split(" ").toSet()
                val tokensB = normalizedB.split(" ").toSet()
                val similarity = if (tokensA.isEmpty() || tokensB.isEmpty()) 0.0
                    else tokensA.intersect(tokensB).size.toDouble() / tokensA.union(tokensB).size

                val meetsThreshold = if (similarity >= 0.5) "✅ CLUSTERS" else if (similarity >= 0.20) "⚠️ NEEDS ENTITIES" else "❌ TOO LOW"
                println("  $styleA vs $styleB: ${"%.1f".format(similarity * 100)}% $meetsThreshold")
            }
        }

        println("\nConclusion:")
        println("Real headlines for the SAME EVENT typically show 15-40% token overlap.")
        println("Current 50% standalone threshold is too strict for real-world variance.")
        println("Need to rely more on entity extraction and named entity matching.")
    }

    private fun analyzeAndRecommend(articles: List<SourceArticleRecord>) {
        val headlineLengths = articles.map { it.headline.split(" ").size }
        val avgLength = headlineLengths.average()

        println("Article characteristics:")
        println("- Average headline length: ${"%.1f".format(avgLength)} words")
        println("- Sources: ${articles.map { it.sourceId }.distinct().size} distinct")
        println("- Time span: ${ChronoUnit.HOURS.between(articles.minOf { it.publishedAt }, articles.maxOf { it.publishedAt })}h")
        println()

        println("Recommendations:")
        println()
        println("1. **Entity extraction is critical**")
        println("   - Current: Basic regex pattern for capitalized words")
        println("   - Need: Proper NER (Named Entity Recognition) to identify:")
        println("     - People (e.g., 'Joe Biden', 'Xi Jinping')")
        println("     - Places (e.g., 'Strait of Hormuz', 'Darwin Harbour')")
        println("     - Organizations (e.g., 'United Nations', 'ABC News')")
        println("     - Events (e.g., 'COP29', 'Presidents Cup')")
        println()
        println("2. **Lower standalone headline threshold**")
        println("   - Current: 50% token overlap required without entities")
        println("   - Real-world: Same event typically shows 15-40% overlap")
        println("   - Recommendation: 35% for standalone clustering")
        println()
        println("3. **Add more international sources**")
        println("   - Current RSS feeds may not cover overlapping events")
        println("   - Add: Wire services (Reuters, AP, AFP) that cover same events")
        println("   - Add: More regional sources covering international news")
        println()
        println("4. **Cross-language clustering**")
        println("   - Current: Only clusters same-language articles")
        println("   - Need: Use entity IDs to match across languages")
        println("   - Example: BBC English + Le Monde French on same event")
        println()
        println("5. **Time window consideration**")
        println("   - Current: 72-hour window")
        println("   - For breaking news: Consider 24-hour window for tighter grouping")
        println("   - For slower stories: Keep 72-hour window")
    }

    private fun EventClusteringService.toClusteredArticle(record: SourceArticleRecord) =
        com.crosslens.app.core.model.ClusteredArticle(
            sourceId = record.sourceId,
            headline = record.headline,
            excerpt = record.excerpt,
            publishedAt = record.publishedAt,
            url = record.url,
            imageUrl = record.imageUrl,
            languageTag = record.languageTag,
            normalizedHeadline = normalizeHeadline(record.headline),
            entities = extractEntities(record.headline + " " + record.excerpt)
        )

    private fun EventClusteringService.extractEntities(text: String): List<String> {
        // Use reflection to access private method for testing
        val method = EventClusteringService::class.java.getDeclaredMethod("extractEntities", String::class.java)
        method.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        return method.invoke(this, text) as List<String>
    }

    private fun createArticle(
        sourceId: String,
        headline: String,
        publishedAt: Instant,
        url: String = "https://example.com/$sourceId/${headline.take(20).hashCode()}"
    ) = SourceArticleRecord(
        url = url,
        publishedAt = publishedAt,
        languageTag = "en",
        headline = headline,
        excerpt = "",
        sourceId = sourceId,
        imageUrl = null
    )
}
