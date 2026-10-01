package com.crosslens.app.data.fixture

import com.crosslens.app.data.ingestion.SourceArticleRecord
import javax.inject.Inject
import javax.inject.Singleton

/**
 * RELEASE BUILD ONLY: No-op implementation of fixture provider.
 *
 * This implementation is compiled into release builds and returns empty lists.
 * No test fixtures are included in production builds.
 */
@Singleton
class DebugEventFixtureProviderImpl @Inject constructor() : DebugEventFixtureProvider {

    /**
     * Returns empty list in release builds.
     * Test fixtures are never included in production.
     */
    override fun getReadAcrossFixture(): List<SourceArticleRecord> {
        return emptyList()
    }
}
