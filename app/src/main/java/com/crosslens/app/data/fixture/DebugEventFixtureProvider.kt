package com.crosslens.app.data.fixture

import com.crosslens.app.data.ingestion.SourceArticleRecord

/**
 * Provider interface for debug-only test fixtures.
 *
 * This interface has different implementations in debug vs release builds:
 * - Debug: Provides test fixtures for verification
 * - Release: No-op implementation (empty list)
 *
 * Fixtures are NEVER included in release builds.
 */
interface DebugEventFixtureProvider {

    /**
     * Get debug test fixture articles for Read Across Coverage verification.
     *
     * Returns a set of 4 articles representing the same specific event from
     * 4 distinct publishers. Designed to test:
     * - Event clustering algorithm with conservative thresholds
     * - Event comparison UI showing all sources from cluster
     * - Read Across Coverage feature visibility and recommendations
     * - Attribution preservation across sources
     * - Factual explanations based on documented metadata
     *
     * The fixture is engineered to pass production clustering rules:
     * - 2+ shared named entities across all articles
     * - 20%+ headline similarity (Jaccard) after normalization
     * - All articles within 24 hours
     * - 4 distinct publishers (minimum for multi-source event)
     *
     * @return List of fixture articles in debug builds, empty list in release builds
     */
    fun getReadAcrossFixture(): List<SourceArticleRecord>
}
