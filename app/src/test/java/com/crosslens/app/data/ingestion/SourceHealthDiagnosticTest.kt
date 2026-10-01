package com.crosslens.app.data.ingestion

import com.crosslens.app.data.local.dao.SourceHealthDao
import com.crosslens.app.data.local.entity.SourceHealthEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.Test
import java.time.Duration
import java.time.Instant

/**
 * Live diagnostic test for RSS source health.
 *
 * This test fetches from all configured sources and reports their status.
 * Run this to identify which sources are failing and why.
 *
 * Usage:
 * ```
 * ./gradlew :app:testDebugUnitTest --tests "*SourceHealthDiagnosticTest*"
 * ```
 *
 * Note: This makes live HTTP requests and may take 1-2 minutes.
 */
class SourceHealthDiagnosticTest {

    @Test
    fun `audit all RSS sources - live fetch test`() = runBlocking {
        println("═══════════════════════════════════════════════════════════")
        println("RSS SOURCE HEALTH DIAGNOSTIC")
        println("Started: ${Instant.now()}")
        println("═══════════════════════════════════════════════════════════")
        println()

        val httpClient = RssSourceAdapter.createHttpClient()
        val fakeDao = FakeSourceHealthDao()
        val healthMonitor = SourceHealthMonitor(fakeDao)
        val sources = RssSourceAdapter.createApprovedSources(httpClient, healthMonitor)

        println("Testing ${sources.size} configured sources...")
        println()

        val results = mutableListOf<SourceTestResult>()
        val startTime = System.currentTimeMillis()

        sources.forEach { adapter ->
            val result = testSource(adapter)
            results.add(result)
            logSourceResult(result)
        }

        val totalTime = System.currentTimeMillis() - startTime

        println()
        println("═══════════════════════════════════════════════════════════")
        println("SUMMARY")
        println("═══════════════════════════════════════════════════════════")

        val successful = results.count { it.success }
        val failed = results.count { !it.success }
        val successRate = (successful.toDouble() / results.size) * 100

        println("Total Sources: ${results.size}")
        println("Successful: $successful")
        println("Failed: $failed")
        println("Success Rate: ${String.format("%.1f%%", successRate)}")
        println("Total Time: ${totalTime}ms (${totalTime / 1000}s)")
        println()

        if (successRate >= 90.0) {
            println("✅ PRODUCTION READY: Success rate meets 90% threshold")
        } else {
            println("❌ NOT READY: Success rate below 90% threshold")
            println("   Need to fix ${(results.size * 0.9).toInt() - successful} more source(s)")
        }

        println()
        println("───────────────────────────────────────────────────────────")
        println("SUCCESSFUL SOURCES ($successful):")
        println("───────────────────────────────────────────────────────────")
        results.filter { it.success }.forEach { result ->
            println("  ✅ ${result.sourceName}")
            println("     ID: ${result.sourceId}")
            println("     Articles: ${result.articleCount}")
            println("     With Images: ${result.articlesWithImages}")
            println("     Fetch Time: ${result.fetchTimeMs}ms")
            println()
        }

        println()
        println("───────────────────────────────────────────────────────────")
        println("FAILED SOURCES ($failed):")
        println("───────────────────────────────────────────────────────────")
        results.filter { !it.success }.forEach { result ->
            println("  ❌ ${result.sourceName}")
            println("     ID: ${result.sourceId}")
            println("     Error: ${result.errorMessage}")
            println("     Feed URL: ${result.feedUrl}")
            println("     Fetch Time: ${result.fetchTimeMs}ms")
            println()
        }

        println("═══════════════════════════════════════════════════════════")
        println("Completed: ${Instant.now()}")
        println("═══════════════════════════════════════════════════════════")
    }

    private suspend fun testSource(adapter: RssSourceAdapter): SourceTestResult {
        val startTime = System.currentTimeMillis()
        return try {
            val articles = adapter.fetchArticles()
            val fetchTime = System.currentTimeMillis() - startTime

            val articlesWithImages = articles.count { it.imageUrl != null }

            SourceTestResult(
                sourceId = adapter.sourceId,
                sourceName = adapter.sourceName,
                feedUrl = getFeedUrl(adapter),
                success = articles.isNotEmpty(),
                articleCount = articles.size,
                articlesWithImages = articlesWithImages,
                fetchTimeMs = fetchTime,
                errorMessage = if (articles.isEmpty()) "No articles returned" else null
            )
        } catch (e: Exception) {
            val fetchTime = System.currentTimeMillis() - startTime
            SourceTestResult(
                sourceId = adapter.sourceId,
                sourceName = adapter.sourceName,
                feedUrl = getFeedUrl(adapter),
                success = false,
                articleCount = 0,
                articlesWithImages = 0,
                fetchTimeMs = fetchTime,
                errorMessage = "${e.javaClass.simpleName}: ${e.message}"
            )
        }
    }

    private fun logSourceResult(result: SourceTestResult) {
        val status = if (result.success) "✅ SUCCESS" else "❌ FAILED"
        println("$status - ${result.sourceName} (${result.sourceId})")

        if (result.success) {
            println("  Articles: ${result.articleCount}, Images: ${result.articlesWithImages}, Time: ${result.fetchTimeMs}ms")
        } else {
            println("  Error: ${result.errorMessage}")
            println("  Time: ${result.fetchTimeMs}ms")
        }
        println()
    }

    private fun getFeedUrl(adapter: RssSourceAdapter): String {
        // Use reflection to get private feedUrl field
        return try {
            val field = RssSourceAdapter::class.java.getDeclaredField("feedUrl")
            field.isAccessible = true
            field.get(adapter) as String
        } catch (e: Exception) {
            "Unknown"
        }
    }

    data class SourceTestResult(
        val sourceId: String,
        val sourceName: String,
        val feedUrl: String,
        val success: Boolean,
        val articleCount: Int,
        val articlesWithImages: Int,
        val fetchTimeMs: Long,
        val errorMessage: String?
    )
}

/**
 * Fake DAO for testing - doesn't persist anything, just keeps in memory.
 */
private class FakeSourceHealthDao : SourceHealthDao {
    private val storage = mutableMapOf<String, SourceHealthEntity>()

    override suspend fun upsert(health: SourceHealthEntity) {
        storage[health.sourceId] = health
    }

    override suspend fun getHealth(sourceId: String): SourceHealthEntity? {
        return storage[sourceId]
    }

    override suspend fun getAllHealth(): List<SourceHealthEntity> {
        return storage.values.toList()
    }

    override fun observeAllHealth(): Flow<List<SourceHealthEntity>> {
        return flowOf(storage.values.toList())
    }

    override suspend fun getProblematicSourcesCount(): Int {
        return storage.values.count { it.status != "ACTIVE" }
    }

    override suspend fun deleteAll() {
        storage.clear()
    }
}
