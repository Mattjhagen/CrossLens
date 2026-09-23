package com.crosslens.app.core.model

/**
 * AI-generated digest comparing source coverage for one story.
 * This is a demo feature using deterministic mock analysis.
 */
data class SourceDigest(
    val storyId: String,
    val summary: String, // Brief overview of agreement/differences
    val agreements: List<DigestPoint>, // What sources agree on
    val differences: List<DigestPoint>, // How framing differs
    val missingEvidence: List<String>, // What's not covered/unclear
    val sourcesCovered: List<String>, // Source IDs analyzed
    val generatedTime: java.time.Instant,
    val isDemo: Boolean = true, // Always true in mock mode
    val methodVersion: String = "demo-v1"
)

data class DigestPoint(
    val observation: String,
    val supportingSourceIds: List<String> // Which sources support this
)
