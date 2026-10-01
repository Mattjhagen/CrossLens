package com.crosslens.app.data.repository

import com.crosslens.app.data.ingestion.RssSourceAdapter
import com.crosslens.app.data.ingestion.SourceHealthCheck
import com.crosslens.app.data.ingestion.SourceHealthMonitor
import com.crosslens.app.data.local.dao.SourceHealthDao
import com.crosslens.app.data.local.entity.SourceHealthEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.Instant

/**
 * Tests for SourceHealthRepository.
 * Verifies health tracking, refresh operations, and persistence.
 */
class SourceHealthRepositoryTest {

    private lateinit var repository: SourceHealthRepository
    private lateinit var fakeDao: FakeSourceHealthDao
    private lateinit var healthMonitor: SourceHealthMonitor
    private lateinit var adapters: List<RssSourceAdapter>

    @Before
    fun setup() {
        fakeDao = FakeSourceHealthDao()
        healthMonitor = SourceHealthMonitor(fakeDao)

        // Create test adapters
        val httpClient = OkHttpClient()
        adapters = listOf(
            RssSourceAdapter(
                sourceId = "test-source-1",
                sourceName = "Test Source 1",
                feedUrl = "https://example.com/feed1.xml",
                httpClient = httpClient,
                healthMonitor = healthMonitor
            ),
            RssSourceAdapter(
                sourceId = "test-source-2",
                sourceName = "Test Source 2",
                feedUrl = "https://example.com/feed2.xml",
                httpClient = httpClient,
                healthMonitor = healthMonitor
            )
        )

        repository = SourceHealthRepository(fakeDao, healthMonitor, adapters)
    }

    @Test
    fun `observeAllSourceHealth returns empty list initially`() = runTest {
        val health = repository.observeAllSourceHealth().first()
        assertEquals(0, health.size)
    }

    @Test
    fun `observeAllSourceHealth returns persisted data after health check`() = runTest {
        // Record a health check
        healthMonitor.recordCheck(
            SourceHealthCheck(
                sourceId = "test-source-1",
                checkedAt = Instant.now(),
                fetchSucceeded = true,
                parseSucceeded = true,
                articlesReturned = 10,
                articlesWithValidDates = 10,
                articlesWithImages = 5,
                articlesWithHttpsLinks = 10,
                latestArticleAge = 3600,
                fetchDurationMs = 1000,
                errorMessage = null
            ),
            sourceName = "Test Source 1"
        )

        val health = repository.observeAllSourceHealth().first()
        assertEquals(1, health.size)
        assertEquals("Test Source 1", health[0].sourceName)
        assertEquals(SourceHealthStatus.ACTIVE, health[0].status)
    }

    @Test
    fun `refreshAllSources attempts to refresh configured sources`() = runTest {
        // Note: This will fail because the URLs are fake, but that's expected in tests
        val result = repository.refreshAllSources()

        // Just verify that it returned a result with the right number of sources
        assertEquals(2, result.totalSources)
        assertEquals(2, result.outcomes.size)

        // Verify it tracked success/failure counts
        assertEquals(result.totalSources, result.successCount + result.failureCount)
    }

    @Test
    fun `getProblematicSourcesCount returns zero initially`() = runTest {
        val count = repository.getProblematicSourcesCount()
        assertEquals(0, count)
    }

    @Test
    fun `getProblematicSourcesCount increases after degraded source`() = runTest {
        // Record 3 consecutive failures to make source degraded
        repeat(3) {
            healthMonitor.recordCheck(
                SourceHealthCheck(
                    sourceId = "test-source-1",
                    checkedAt = Instant.now(),
                    fetchSucceeded = false,
                    parseSucceeded = false,
                    articlesReturned = 0,
                    articlesWithValidDates = 0,
                    articlesWithImages = 0,
                    articlesWithHttpsLinks = 0,
                    latestArticleAge = null,
                    fetchDurationMs = 1000,
                    errorMessage = "Network error"
                ),
                sourceName = "Test Source 1"
            )
        }

        val count = repository.getProblematicSourcesCount()
        assertEquals(1, count)
    }

    @Test
    fun `loadPersistedState restores health from DAO`() = runTest {
        // Manually insert health data
        fakeDao.upsert(
            SourceHealthEntity(
                sourceId = "test-source-1",
                sourceName = "Test Source 1",
                status = "DEGRADED",
                lastSuccessAt = Instant.now().toEpochMilli(),
                lastFailureAt = Instant.now().toEpochMilli(),
                consecutiveFailures = 5,
                last24hSuccessRate = 0.5,
                last24hArticleCount = 10,
                lastErrorMessage = "HTTP 500",
                lastErrorCategory = "HTTP_ERROR",
                disabledReason = null,
                updatedAt = Instant.now().toEpochMilli()
            )
        )

        // Load persisted state
        repository.loadPersistedState()

        // Verify it was loaded
        val health = repository.observeAllSourceHealth().first()
        assertEquals(1, health.size)
        assertEquals(SourceHealthStatus.DEGRADED, health[0].status)
        assertEquals(5, health[0].consecutiveFailures)
    }

    @Test
    fun `health status transitions from active to degraded to disabled`() = runTest {
        // Record 1 success - should be ACTIVE
        healthMonitor.recordCheck(
            SourceHealthCheck(
                sourceId = "test-source-1",
                checkedAt = Instant.now(),
                fetchSucceeded = true,
                parseSucceeded = true,
                articlesReturned = 10,
                articlesWithValidDates = 10,
                articlesWithImages = 5,
                articlesWithHttpsLinks = 10,
                latestArticleAge = 3600,
                fetchDurationMs = 1000,
                errorMessage = null
            ),
            sourceName = "Test Source 1"
        )

        var health = repository.observeAllSourceHealth().first()
        assertEquals(SourceHealthStatus.ACTIVE, health[0].status)

        // Record 3 failures - should be DEGRADED
        repeat(3) {
            healthMonitor.recordCheck(
                SourceHealthCheck(
                    sourceId = "test-source-1",
                    checkedAt = Instant.now(),
                    fetchSucceeded = false,
                    parseSucceeded = false,
                    articlesReturned = 0,
                    articlesWithValidDates = 0,
                    articlesWithImages = 0,
                    articlesWithHttpsLinks = 0,
                    latestArticleAge = null,
                    fetchDurationMs = 1000,
                    errorMessage = "Network timeout"
                ),
                sourceName = "Test Source 1"
            )
        }

        health = repository.observeAllSourceHealth().first()
        assertEquals(SourceHealthStatus.DEGRADED, health[0].status)
        assertEquals(3, health[0].consecutiveFailures)

        // Record 7 more failures (total 10) - should be DISABLED
        repeat(7) {
            healthMonitor.recordCheck(
                SourceHealthCheck(
                    sourceId = "test-source-1",
                    checkedAt = Instant.now(),
                    fetchSucceeded = false,
                    parseSucceeded = false,
                    articlesReturned = 0,
                    articlesWithValidDates = 0,
                    articlesWithImages = 0,
                    articlesWithHttpsLinks = 0,
                    latestArticleAge = null,
                    fetchDurationMs = 1000,
                    errorMessage = "Network timeout"
                ),
                sourceName = "Test Source 1"
            )
        }

        health = repository.observeAllSourceHealth().first()
        assertEquals(SourceHealthStatus.DISABLED, health[0].status)
        assertEquals(10, health[0].consecutiveFailures)
    }

    @Test
    fun `source recovers from degraded to active after successful check`() = runTest {
        // Make source degraded
        repeat(3) {
            healthMonitor.recordCheck(
                SourceHealthCheck(
                    sourceId = "test-source-1",
                    checkedAt = Instant.now(),
                    fetchSucceeded = false,
                    parseSucceeded = false,
                    articlesReturned = 0,
                    articlesWithValidDates = 0,
                    articlesWithImages = 0,
                    articlesWithHttpsLinks = 0,
                    latestArticleAge = null,
                    fetchDurationMs = 1000,
                    errorMessage = "Network error"
                ),
                sourceName = "Test Source 1"
            )
        }

        var health = repository.observeAllSourceHealth().first()
        assertEquals(SourceHealthStatus.DEGRADED, health[0].status)

        // Record successful check
        healthMonitor.recordCheck(
            SourceHealthCheck(
                sourceId = "test-source-1",
                checkedAt = Instant.now(),
                fetchSucceeded = true,
                parseSucceeded = true,
                articlesReturned = 10,
                articlesWithValidDates = 10,
                articlesWithImages = 5,
                articlesWithHttpsLinks = 10,
                latestArticleAge = 3600,
                fetchDurationMs = 1000,
                errorMessage = null
            ),
            sourceName = "Test Source 1"
        )

        // Should recover to ACTIVE
        health = repository.observeAllSourceHealth().first()
        assertEquals(SourceHealthStatus.ACTIVE, health[0].status)
        assertEquals(0, health[0].consecutiveFailures)
    }
}

/**
 * Fake DAO for testing.
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
