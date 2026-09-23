package com.crosslens.app.data.repository

import com.crosslens.app.core.model.Article
import com.crosslens.app.core.model.DigestPoint
import com.crosslens.app.core.model.Source
import com.crosslens.app.core.model.SourceDigest
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Generates mock AI source digests for demo purposes.
 * In production, this would call an actual AI model with appropriate safeguards.
 */
@Singleton
class SourceDigestGenerator @Inject constructor() {

    /**
     * Generate a deterministic demo digest for a story's articles.
     * This is mock analysis - not actual AI processing.
     */
    fun generateDigest(
        storyId: String,
        articles: List<Article>,
        sources: Map<String, Source>
    ): SourceDigest {
        // Demo: Create deterministic digest based on article count and content
        val sourceIds = articles.map { it.sourceId }.distinct()

        val agreements = mutableListOf<DigestPoint>()
        val differences = mutableListOf<DigestPoint>()
        val missingEvidence = mutableListOf<String>()

        // Demo logic: Simple pattern matching on headlines and excerpts
        when {
            articles.size >= 3 -> {
                // Multiple sources - show both agreement and differences
                agreements.add(
                    DigestPoint(
                        observation = "All sources report the main event occurred and involved key stakeholders",
                        supportingSourceIds = sourceIds
                    )
                )

                // Check for differing emphasis in excerpts
                val skepticalArticles = articles.filter {
                    it.originalExcerpt.contains("question", ignoreCase = true) ||
                    it.originalExcerpt.contains("skeptic", ignoreCase = true) ||
                    it.originalExcerpt.contains("concern", ignoreCase = true)
                }

                if (skepticalArticles.isNotEmpty()) {
                    differences.add(
                        DigestPoint(
                            observation = "Some sources emphasize implementation challenges and skepticism",
                            supportingSourceIds = skepticalArticles.map { it.sourceId }
                        )
                    )
                }

                val optimisticArticles = articles.filter {
                    it.originalExcerpt.contains("commit", ignoreCase = true) ||
                    it.originalExcerpt.contains("unite", ignoreCase = true) ||
                    it.originalExcerpt.contains("celebrate", ignoreCase = true)
                }

                if (optimisticArticles.isNotEmpty() && skepticalArticles.isNotEmpty()) {
                    differences.add(
                        DigestPoint(
                            observation = "Other sources focus on positive commitments and cooperation",
                            supportingSourceIds = optimisticArticles.map { it.sourceId }
                        )
                    )
                }

                missingEvidence.add("Long-term effectiveness of proposed measures")
                missingEvidence.add("Specific implementation timelines and mechanisms")
            }
            articles.size == 2 -> {
                agreements.add(
                    DigestPoint(
                        observation = "Both sources confirm the event and key participants",
                        supportingSourceIds = sourceIds
                    )
                )
                missingEvidence.add("Additional perspectives from other regions")
                missingEvidence.add("Detailed analysis of stakeholder positions")
            }
            else -> {
                agreements.add(
                    DigestPoint(
                        observation = "Limited coverage available for comprehensive comparison",
                        supportingSourceIds = sourceIds
                    )
                )
                missingEvidence.add("Multiple source perspectives")
                missingEvidence.add("Cross-regional coverage")
            }
        }

        return SourceDigest(
            storyId = storyId,
            summary = generateSummary(agreements, differences, articles.size),
            agreements = agreements,
            differences = differences,
            missingEvidence = missingEvidence,
            sourcesCovered = sourceIds,
            generatedTime = Instant.now(),
            isDemo = true,
            methodVersion = "demo-v1"
        )
    }

    private fun generateSummary(
        agreements: List<DigestPoint>,
        differences: List<DigestPoint>,
        articleCount: Int
    ): String {
        return when {
            differences.isNotEmpty() && agreements.isNotEmpty() ->
                "Based on $articleCount sources: Core facts are consistently reported, but sources differ in their emphasis on implementation challenges versus commitments."
            agreements.isNotEmpty() ->
                "Based on $articleCount sources: Coverage shows general agreement on key facts and developments."
            else ->
                "Limited coverage available for comprehensive comparison across sources."
        }
    }
}
