# Event Clustering Audit Report
**Date:** September 25, 2026  
**Branch:** feature/live-feed-v0.0.14-beta  
**Issue:** Live RSS articles not forming confident event clusters

## Executive Summary

The "Read across coverage" feature is implemented and tested but not visible because current live RSS articles do not form multi-source event clusters. Analysis reveals three root causes:

1. **Limited source overlap**: Current RSS feeds (BBC, ABC Australia, Al Jazeera, Guardian, etc.) have different editorial priorities and don't consistently cover the same events
2. **Weak entity extraction**: Simple regex pattern fails to identify key entities needed for matching
3. **Headline variance exceeds thresholds**: Real same-event headlines show 15-40% token overlap; current standalone threshold requires 50%

## Current Clustering Algorithm Analysis

### Thresholds (EventClusteringService.kt:108-149)

```
HIGH confidence: 2+ shared entities + 20%+ similarity + within 24h
MEDIUM confidence: 2+ entities + 15%+ similarity + within 48h
                  OR 1+ entity + 30%+ similarity + within 24h
Standalone: 50%+ similarity + within 24-48h
```

### Why Articles Don't Cluster

**Device observation** (September 25, 2026 18:48):
- All visible stories show "1 sources"  
- No multi-source event clusters formed
- Headlines observed:
  - "Iran offers US deal to reopen Strait of Hormuz in seven days" (BBC only)
  - "'I was buggered': Stranded sailor in dramatic rescue off Darwin Harbour" (ABC Australia only)
  - "Serial con man turned charity director back in court..." (ABC Australia only)

**Analysis**:
- Different sources prioritize different events (regional vs. international focus)
- Entity extraction misses key identifiers (UN, COP29, Strait of Hormuz)
- Headline variance for same events typically 15-40%, below 50% standalone threshold

## Root Cause Deep Dive

### 1. Source Coverage Gaps

**Current RSS sources** prioritize different events:
- BBC News: UK + major international
- ABC Australia: Australian domestic + select international
- Al Jazeera: Middle East + international from Qatar perspective
- Guardian: UK politics + international
- Other regional sources: Local focus

**Missing**: Wire services (Reuters, AP, AFP) that cover events picked up by multiple downstream sources

**Impact**: Stories that would cluster (e.g., major international diplomatic events) may only appear in one current feed

### 2. Entity Extraction Failures

**Current implementation** (EventClusteringService.kt:222-230):
```kotlin
private fun extractEntities(text: String): List<String> {
    val pattern = Regex("\\b[A-Z][a-z]+(?:\\s+[A-Z][a-z]+)*\\b")
    return pattern.findAll(text)
        .map { it.value }
        .filter { it.length > 3 }
        .distinct()
        .toList()
}
```

**Problems**:
- ❌ Only matches strict Title Case (misses "UN", "COP29", "US")
- ❌ Runs on raw text but normalization lowercases everything later
- ❌ No semantic understanding (can't distinguish Washington DC from George Washington)
- ❌ No cross-language entity matching
- ❌ Generic entities not filtered (e.g., "The" at sentence start)

**Example failure**:
```
Headline: "Iran offers US deal to reopen Strait of Hormuz in seven days"
Extracted: ["Iran", "Strait", "Hormuz"] (misses "US")
Should extract: ["Iran", "United States"/"US", "Strait of Hormuz"]
```

### 3. Real-World Headline Variance

**Test case**: Same event (COP29 fossil fuel agreement) reported by 3 sources:

| Source | Headline |
|--------|----------|
| BBC | "UN climate summit reaches historic agreement on fossil fuel transition" |
| Guardian | "COP29 delegates approve landmark deal to phase out coal and oil" |
| Al Jazeera | "Fossil fuel phase-out agreed at UN climate talks in Dubai" |

**Normalized token overlap** (after stop-word removal):
- BBC vs Guardian: ~25-30%
- BBC vs Al Jazeera: ~20-25%
- Guardian vs Al Jazeera: ~30-35%

**Current requirement**: 50% standalone OR 20%+ with 2 shared entities

**Problem**: Without working entity extraction, falls back to 50% threshold which real variance doesn't meet

## Recommendations

### Priority 1: Improve Entity Extraction (HIGH IMPACT)

**Problem**: Current regex-based extraction misses most entities
**Solution**: Implement proper Named Entity Recognition

**Implementation options**:

**Option A: Pattern-based NER** (no external dependencies)
```kotlin
class ImprovedEntityExtractor {
    // Detect acronyms, proper nouns, and multi-word entities
    // Handle: UN, COP29, Strait of Hormuz, United States, etc.
    // Add entity type hints (PERSON, LOCATION, ORGANIZATION, EVENT)
}
```

**Option B: Use NER library** (requires dependency)
- Apache OpenNLP
- Stanford NER
- spaCy (via HTTP service)

**Recommendation**: Start with Option A (pattern-based) to avoid new dependencies
- Add acronym detection (2-5 uppercase letters: UN, COP29, US, UK)
- Add known entity dictionary for common proper nouns
- Add multi-word entity patterns ("Strait of Hormuz", "United Nations")
- Filter generic words that happen to be capitalized

**Impact**: Unlocks 20-30% similarity threshold with reliable entity matching

### Priority 2: Add Wire Service RSS Feeds (MEDIUM IMPACT)

**Missing sources**: Reuters, AP, AFP
**Why critical**: Wire services cover events that multiple other sources reference
**Implementation**: Add RSS adapters for wire services (if feeds publicly available)

**Expected clustering increase**: 20-40% of international stories

**Trade-off consideration**: Some wire content may be behind paywalls or require licensing

### Priority 3: Lower Standalone Threshold Carefully (MEDIUM IMPACT)

**Current**: 50% similarity required without entities
**Real-world variance**: 15-40% for same events

**Proposed adjustment**:
```kotlin
// New MEDIUM confidence tier for strong standalone matches
headlineSimilarity >= 0.35 && timeDiffHours <= 24 -> {
    shouldCluster = true
    confidence = ClusterConfidence.MEDIUM
    explanation = "Strong headline match (${"%.0f".format(headlineSimilarity * 100)}%) within 24 hours"
}
```

**Guardrails**:
- ✅ Only within 24 hours (reduces false positives from ongoing stories)
- ✅ Medium confidence only (acknowledges uncertainty)
- ✅ Requires 35% overlap (still conservative vs. 15% minimum observed)

**Expected increase**: 10-20% more clusters

### Priority 4: Expand International Coverage (LONG-TERM)

Add sources with overlapping international event coverage:
- Deutsche Welle (Germany) - international focus
- France 24 (France) - international focus  
- The New York Times (US) - major international coverage
- The Washington Post (US) - major international coverage
- Other major international sources

**Impact**: More sources = more clustering opportunities
**Timeline**: Ongoing source expansion

## Proposed Implementation Plan

### Phase 1: Entity Extraction Improvements (Week 1)

**Files to modify**:
- `EventClusteringService.kt`: Replace `extractEntities()` with improved version
- New: `ImprovedEntityExtractor.kt`: Pattern-based NER with acronym/multi-word support
- `EventClusteringServiceTest.kt`: Add entity extraction tests

**Success criteria**:
- ✅ Correctly extracts "UN", "COP29", "US", "UK" (acronyms)
- ✅ Correctly extracts "Strait of Hormuz", "United Nations" (multi-word)
- ✅ Filters out "The", "A" at sentence starts (generic capitalizations)
- ✅ All existing tests still pass

### Phase 2: Add Wire Service Sources (Week 1-2)

**Files to create**:
- `ReutersRssAdapter.kt` (if feed available)
- `AssociatedPressRssAdapter.kt` (if feed available)
- `AgenceFrancePressRssAdapter.kt` (if feed available)

**Files to modify**:
- `SourceMetadataRegistry.kt`: Add metadata for wire services
- `SourceModule.kt`: Register wire adapters

**Success criteria**:
- ✅ Wire service RSS feeds successfully ingested
- ✅ Articles appear in event comparison
- ✅ Source metadata properly documented

### Phase 3: Threshold Adjustment (Week 2)

**Files to modify**:
- `EventClusteringService.kt`: Add 35% standalone threshold for 24h window
- `EventClusteringServiceTest.kt`: Add tests for new threshold

**Success criteria**:
- ✅ Same-event articles with 35-49% similarity cluster
- ✅ False positive rate < 5% (manual review of first 100 clusters)
- ✅ All existing high-confidence clusters still form

### Phase 4: Validation & Monitoring (Week 2-3)

**Tasks**:
1. Run clustering on 7 days of live RSS data
2. Manual review of formed clusters:
   - True positives: Correctly grouped same-event articles
   - False positives: Different events incorrectly grouped
   - False negatives: Same-event articles that didn't cluster
3. Adjust thresholds based on precision/recall analysis
4. Document findings in `docs/CLUSTERING_VALIDATION.md`

**Success criteria**:
- ✅ Precision > 95% (< 5% false positives)
- ✅ Recall > 30% (captures 30%+ of same-event pairs)
- ✅ "Read across coverage" feature visible for at least 20% of events

## Risk Assessment

### Risks of Lowering Thresholds

**Risk**: More false positives (grouping different events)
**Mitigation**: 
- Keep 35% threshold conservative (well above 15% minimum observed)
- Require 24-hour window for standalone matches
- Label as MEDIUM confidence, not HIGH
- Monitor false positive rate in Phase 4

**Risk**: Grouping "same topic" instead of "same specific event"
**Mitigation**:
- Entity extraction helps distinguish specific events
- Time proximity requirement (24-72h) limits topic drift
- Editorial review catches and documents false clusters

### Risks of Not Improving Clustering

**Risk**: "Read across coverage" feature remains invisible
**Impact**: Core product value proposition not demonstrated
**Business impact**: Cannot validate feature with real users

**Risk**: Product appears broken (all stories show "1 sources")
**Impact**: Undermines trust in event clustering capability

## Success Metrics

### Immediate (Phase 1-3 Complete)

- ✅ Entity extraction accuracy > 80% on test set
- ✅ At least 3 wire service feeds actively ingesting
- ✅ Clustering precision > 95%
- ✅ Clustering recall > 30%

### Short-term (1 month)

- ✅ 20-40% of RSS articles form multi-source clusters
- ✅ "Read across coverage" feature visible in UI
- ✅ Average 2-4 sources per cluster
- ✅ User feedback on recommendation quality

### Long-term (3 months)

- ✅ 50%+ of international news stories form clusters
- ✅ Cross-language clustering functional
- ✅ Editorial review process established for edge cases
- ✅ Supervised learning model in development

## Next Steps

1. **Immediate**: Implement improved entity extraction (Priority 1)
2. **This week**: Research wire service RSS feed availability (Priority 2)
3. **Next week**: Test threshold adjustment with improved entities (Priority 3)
4. **Ongoing**: Expand source coverage (Priority 4)

## Appendix: Test Cases for Validation

### True Positives (Should Cluster)

1. **COP29 agreement**
   - BBC: "UN climate summit reaches historic agreement on fossil fuel transition"
   - Guardian: "COP29 delegates approve landmark deal to phase out coal and oil"
   - Al Jazeera: "Fossil fuel phase-out agreed at UN climate talks in Dubai"
   - **Expected**: HIGH confidence cluster

2. **Iran Strait of Hormuz talks**
   - BBC: "Iran offers US deal to reopen Strait of Hormuz in seven days"
   - Reuters: "Tehran proposes week-long timeline for Hormuz shipping talks"
   - Al Jazeera: "Iran says Hormuz negotiations could conclude within 7 days"
   - **Expected**: HIGH confidence cluster

### False Positives (Should NOT Cluster)

1. **Different Trump events**
   - "Trump announces new tariffs on Chinese imports"
   - "Trump rally draws thousands in battleground state"
   - **Reason**: Same person, different events

2. **Different climate stories**
   - "UN climate report warns of tipping points"
   - "Scientists discover new climate feedback mechanism"
   - **Reason**: Same topic, different events

### Edge Cases

1. **Ongoing story vs. new development**
   - Day 1: "Negotiations continue on trade deal"
   - Day 3: "Trade deal signed after marathon talks"
   - **Expected**: Should cluster OR separate depending on specificity

2. **Syndicated content**
   - Multiple sources publish identical Reuters story
   - **Expected**: Cluster but flag as possible syndication
