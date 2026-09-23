package com.crosslens.app.data.ingestion

import org.junit.Assert.assertThrows
import org.junit.Test
import java.time.Instant

class EditorialValidatorTest {

    private val now = Instant.now()

    @Test
    fun `validates approved decision without reason`() {
        val decision = EditorialDecision(
            proposalId = "proposal-1",
            decision = DecisionType.APPROVED,
            reason = "",
            reviewerId = "reviewer-1",
            decidedAt = now
        )
        EditorialValidator.validate(decision) // Should not throw
    }

    @Test
    fun `validates approved decision with reason`() {
        val decision = EditorialDecision(
            proposalId = "proposal-1",
            decision = DecisionType.APPROVED,
            reason = "Strong multi-source coverage",
            reviewerId = "reviewer-1",
            decidedAt = now
        )
        EditorialValidator.validate(decision) // Should not throw
    }

    @Test
    fun `rejects rejected decision without reason`() {
        val decision = EditorialDecision(
            proposalId = "proposal-1",
            decision = DecisionType.REJECTED,
            reason = "",
            reviewerId = "reviewer-1",
            decidedAt = now
        )
        assertThrows(IllegalArgumentException::class.java) {
            EditorialValidator.validate(decision)
        }
    }

    @Test
    fun `validates rejected decision with reason`() {
        val decision = EditorialDecision(
            proposalId = "proposal-1",
            decision = DecisionType.REJECTED,
            reason = "Only one unique source",
            reviewerId = "reviewer-1",
            decidedAt = now
        )
        EditorialValidator.validate(decision) // Should not throw
    }

    @Test
    fun `rejects deferred decision without reason`() {
        val decision = EditorialDecision(
            proposalId = "proposal-1",
            decision = DecisionType.DEFERRED,
            reason = "",
            reviewerId = "reviewer-1",
            decidedAt = now
        )
        assertThrows(IllegalArgumentException::class.java) {
            EditorialValidator.validate(decision)
        }
    }

    @Test
    fun `validates approved cluster with multiple sources`() {
        val cluster = ApprovedCluster(
            clusterId = "cluster-1",
            title = "Climate summit agreement",
            summary = "Summary",
            firstPublishedAt = now,
            lastPublishedAt = now,
            articles = listOf(
                createArticle("article-1", "bbc"),
                createArticle("article-2", "lemonde")
            ),
            decision = EditorialDecision(
                proposalId = "proposal-1",
                decision = DecisionType.APPROVED,
                reason = "",
                reviewerId = "reviewer-1",
                decidedAt = now
            )
        )
        EditorialValidator.validateApprovedCluster(cluster) // Should not throw
    }

    @Test
    fun `rejects approved cluster with single article`() {
        val cluster = ApprovedCluster(
            clusterId = "cluster-1",
            title = "Title",
            summary = "Summary",
            firstPublishedAt = now,
            lastPublishedAt = now,
            articles = listOf(
                createArticle("article-1", "bbc")
            ),
            decision = EditorialDecision(
                proposalId = "proposal-1",
                decision = DecisionType.APPROVED,
                reason = "",
                reviewerId = "reviewer-1",
                decidedAt = now
            )
        )
        assertThrows(IllegalArgumentException::class.java) {
            EditorialValidator.validateApprovedCluster(cluster)
        }
    }

    @Test
    fun `rejects approved cluster with articles from same source`() {
        val cluster = ApprovedCluster(
            clusterId = "cluster-1",
            title = "Title",
            summary = "Summary",
            firstPublishedAt = now,
            lastPublishedAt = now,
            articles = listOf(
                createArticle("article-1", "bbc"),
                createArticle("article-2", "bbc")
            ),
            decision = EditorialDecision(
                proposalId = "proposal-1",
                decision = DecisionType.APPROVED,
                reason = "",
                reviewerId = "reviewer-1",
                decidedAt = now
            )
        )
        assertThrows(IllegalArgumentException::class.java) {
            EditorialValidator.validateApprovedCluster(cluster)
        }
    }

    @Test
    fun `rejects approved cluster with blank title`() {
        val cluster = ApprovedCluster(
            clusterId = "cluster-1",
            title = "",
            summary = "Summary",
            firstPublishedAt = now,
            lastPublishedAt = now,
            articles = listOf(
                createArticle("article-1", "bbc"),
                createArticle("article-2", "lemonde")
            ),
            decision = EditorialDecision(
                proposalId = "proposal-1",
                decision = DecisionType.APPROVED,
                reason = "",
                reviewerId = "reviewer-1",
                decidedAt = now
            )
        )
        assertThrows(IllegalArgumentException::class.java) {
            EditorialValidator.validateApprovedCluster(cluster)
        }
    }

    @Test
    fun `rejects approved cluster with rejected decision`() {
        val cluster = ApprovedCluster(
            clusterId = "cluster-1",
            title = "Title",
            summary = "Summary",
            firstPublishedAt = now,
            lastPublishedAt = now,
            articles = listOf(
                createArticle("article-1", "bbc"),
                createArticle("article-2", "lemonde")
            ),
            decision = EditorialDecision(
                proposalId = "proposal-1",
                decision = DecisionType.REJECTED,
                reason = "Not newsworthy",
                reviewerId = "reviewer-1",
                decidedAt = now
            )
        )
        assertThrows(IllegalArgumentException::class.java) {
            EditorialValidator.validateApprovedCluster(cluster)
        }
    }

    private fun createArticle(id: String, sourceId: String) = NormalizedArticle(
        id = id,
        sourceId = sourceId,
        sourceName = sourceId,
        originalUrl = "https://example.com/$id",
        canonicalUrl = "https://example.com/$id",
        publishedAt = now,
        languageTag = "en",
        headline = "Test headline",
        excerpt = "Test excerpt",
        headlineTokens = setOf("test", "headline")
    )
}
