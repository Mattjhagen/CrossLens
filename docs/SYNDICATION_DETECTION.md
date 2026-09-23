# Wire-Copy and Syndication Detection

The syndication detector identifies suspected wire-service reprints and syndicated content across articles from different sources. It runs after canonical URL deduplication and before event clustering, providing transparent evidence for editorial review.

## Purpose

News organizations frequently republish wire-service content (AP, Reuters, AFP) or share syndicated reporting. When multiple outlets run the same wire story, it appears as multi-source coverage, but the underlying reporting is from a single origin.

The syndication detector:
- Identifies suspected reprints based on text similarity evidence
- Preserves all articles and source attributions
- Provides confidence bands and transparent detection evidence
- Requires editorial review before any claims about source independence

**IMPORTANT**: This detector makes NO claims about confirmed syndication. All cross-domain matches are labeled SUSPECTED until a human reviewer confirms the relationship.

## Detection Methods

### Method 1: Exact Normalized Title Match

**When**: Two or more articles from different sources have identical titles after normalization (punctuation removed, lowercase, whitespace normalized).

**Evidence**:
- Normalized title string
- Match type: EXACT_NORMALIZED
- Article IDs and source IDs

**Confidence**:
- HIGH: Same language, exact title match
- UNCERTAIN: Different languages (may be translation or syndication)

**Example**: Both BBC and Guardian run "Geneva climate summit reaches landmark agreement" with identical wording → HIGH confidence suspected syndication.

### Method 2: Excerpt Fingerprint Overlap

**When**: Two or more articles from different sources have ≥80% longest-common-substring overlap in excerpts ≥100 characters.

**Evidence**:
- Excerpt fingerprint (SHA-256 hash of first 150 chars)
- Overlap score (0.0 to 1.0)
- Excerpt length

**Confidence**:
- MEDIUM: Same language, high overlap, long excerpt
- LOW: Same language, high overlap, short excerpt (<100 chars)
- UNCERTAIN: Different languages

**Example**: Reuters and Financial Times both run a 200-character excerpt with 85% identical text → MEDIUM confidence suspected syndication.

### Method 3: Wire Attribution (Future)

**When**: Article metadata contains explicit wire-service attribution (e.g., "AP", "Reuters", "AFP").

**Evidence**:
- Wire source identifier
- Found in metadata/byline

**Confidence**: HIGH (confirmed wire-service content)

**Status**: Not yet implemented. Requires metadata parsing integration.

## Confidence Bands

### HIGH
- Same language, exact title match
- Future: Explicit wire attribution in metadata

**Interpretation**: Strong evidence of syndication. Likely the same wire story or direct reprint.

### MEDIUM
- Same language, ≥80% excerpt overlap, long excerpt (≥100 chars)

**Interpretation**: Probable syndication. May be shared quotes or press release language, but high similarity suggests common source.

### LOW
- Same language, high overlap, short excerpt (<100 chars)

**Interpretation**: Weak evidence. Short excerpts may have coincidental overlap (e.g., "The president announced today...").

### UNCERTAIN
- Cross-language match (any similarity level)

**Interpretation**: Cannot distinguish between:
- Independent translations of the same wire story
- Independent reporting translated into multiple languages
- Syndicated content translated by different outlets

**Requires editorial review to determine relationship.**

## Syndication Groups

A **syndication group** is a set of articles suspected to be reprints or wire-copy based on detection evidence.

Each group includes:
- **Article IDs**: All suspected reprints
- **Source IDs**: All outlets running the suspected reprint
- **Detection evidence**: What triggered the match (title, excerpt, wire attribution)
- **Confidence band**: HIGH, MEDIUM, LOW, or UNCERTAIN
- **Method version**: For auditing (e.g., "v1.0-title-fingerprint")
- **Requires editorial review**: Always `true`
- **Rationale**: Human-readable explanation

**All articles remain in the pipeline.** Syndication groups are evidence for reviewers, not filters.

## Editorial Review Workflow

When a syndication group is detected:

1. **Review detection evidence**: Check normalized title, excerpt overlap, confidence band.
2. **Inspect original articles**: Read headlines, excerpts, and bylines to confirm relationship.
3. **Determine relationship**:
   - **Confirmed syndication**: Same wire story or direct reprint → Reduce independent-source count
   - **Independent reporting**: False positive (similar quotes, press release language) → Keep all sources as independent
   - **Uncertain translation**: Cross-language match needs deeper investigation

4. **Document decision**: Record whether syndication was confirmed, rejected, or remains uncertain.
5. **Adjust cluster metadata**: If syndication confirmed, store both:
   - **Total source count**: All outlets running the story
   - **Independent source count**: Outlets with original reporting (excluding reprints)

## Limitations and Edge Cases

### Multilingual Detection

Cross-language matches are always marked UNCERTAIN because the detector cannot distinguish:
- BBC English + Le Monde French translation of AP wire story → Syndication
- BBC original English + Le Monde original French reporting → Independent

**Resolution**: Requires entity extraction, semantic understanding, or manual review.

### Short Excerpts

Excerpts <100 characters may have coincidental overlap:
- "The Senate voted today on the infrastructure bill."
- "The Senate voted today on the spending package."

**Mitigation**: Marked LOW confidence. Editorial review should check full context.

### Press Release Quotes

Multiple outlets may quote the same press release verbatim without syndication:
- Company announcement: "We achieved record revenue of $5.2 billion."
- All outlets run identical quote in their own articles.

**Mitigation**: Detect as MEDIUM confidence, but editorial review can confirm independent reporting using shared quotes.

### Translated Wire Stories

AP publishes English wire story. Reuters publishes French translation. Both are wire-service content but different languages.

**Current behavior**: May not be detected (different language triggers UNCERTAIN even if matched).

**Future solution**: Entity extraction and semantic similarity across languages.

## What Syndication Detection Does NOT Do

❌ **Automatically reduce source counts**: Detection is evidence only; reviewers decide.  
❌ **Remove articles from clustering**: All articles remain available for event clustering.  
❌ **Claim confirmed syndication**: All groups are SUSPECTED until editorial review.  
❌ **Determine original source**: Cannot identify which outlet published first without timestamps and deeper analysis.  
❌ **Label outlets as "unreliable"**: Reprinting wire stories is standard journalism practice.  
❌ **Calculate Lens Gap**: Syndication detection is separate from Lens Gap scoring.

## Integration with Event Clustering

Syndication detection runs **before** event clustering:

1. Normalize articles
2. Deduplicate by canonical URL
3. **Detect syndication** ← You are here
4. Cluster by title similarity and time window
5. Editorial review of clusters
6. Persistence of approved clusters

**Event clusters count all sources**, including suspected reprints. The syndication analysis is attached to the ingestion result for reviewers to inspect.

**Approved clusters** may store two counts:
- `sourceCount`: Total outlets (including reprints)
- `independentSourceCount`: Editorial-verified independent reporting (if syndication confirmed)

The reader-facing UI can choose which count to display based on product requirements.

## Testing and Validation

All detection scenarios are covered by `SyndicationDetectorTest.kt`:

1. ✅ Wire service reprints (same excerpt, same headline) → HIGH confidence
2. ✅ Near-copy with changed headline (same excerpt) → MEDIUM confidence
3. ✅ Independent reporting (different excerpts) → No match
4. ✅ Multilingual content → UNCERTAIN confidence
5. ✅ URL duplicates → Deduplicated before syndication runs
6. ✅ Short excerpts → LOW confidence or no match
7. ✅ Attribution preservation → All articles remain with full source info
8. ✅ Editorial review flag → All groups require review
9. ✅ Transparent evidence → Detection method and confidence inspectable

## Before Production Use

Before deploying syndication detection to production:

1. **Manual audit**: Review 100+ detected groups to measure precision/recall.
2. **Tune thresholds**: Adjust excerpt overlap threshold (currently 80%) based on false positive rate.
3. **Add wire attribution parsing**: Integrate byline/metadata parsing for HIGH confidence wire detection.
4. **Entity extraction**: Add entity-based matching for cross-language syndication.
5. **Editorial UI**: Build interface for reviewing and confirming/rejecting syndication groups.
6. **Persistent decisions**: Store editorial decisions about syndication in database.
7. **Metrics dashboard**: Track detection rates, confirmation rates, false positive rates.

## API Example

```kotlin
val detector = SyndicationDetector(
    minimumExcerptLength = 100,
    excerptOverlapThreshold = 0.80
)

val result = detector.analyze(normalizedArticles)

// Inspect syndication groups
result.syndicationGroups.forEach { group ->
    println("Group: ${group.id}")
    println("Confidence: ${group.confidence}")
    println("Articles: ${group.articleIds.size} from ${group.sourceIds.size} sources")
    println("Evidence: ${group.detectionEvidence}")
    println("Rationale: ${group.rationale}")
}

// Check independent articles
println("Independent articles: ${result.independentArticleIds.size}")
```

## See Also

- [Ingestion Prototype](INGESTION_PROTOTYPE.md) - Overview of the full ingestion pipeline
- [Source Registry](SOURCE_REGISTRY.md) - Content-use policy and source eligibility
- [Editorial Review](INGESTION_PROTOTYPE.md#editorial-review-workflow) - How reviewers approve/reject clusters
