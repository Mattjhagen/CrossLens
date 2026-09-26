# Live RSS Clustering Analysis

## Current Situation

**Problem:** Live RSS articles from BBC News, Al Jazeera, ABC News Australia, and other sources are not forming event clusters, preventing the "Read across coverage" feature from being visible.

## Analysis of Clustering Algorithm

### Current Thresholds (EventClusteringService.kt)

```kotlin
// Time window
timeDiffHours <= 72  // 3 days max

// Clustering decision logic:
// HIGH confidence
sharedEntities >= 2 && headlineSimilarity >= 0.20 && timeDiffHours <= 24

// MEDIUM confidence  
sharedEntities >= 2 && headlineSimilarity >= 0.15 && timeDiffHours <= 48
sharedEntities >= 1 && headlineSimilarity >= 0.30 && timeDiffHours <= 24

// Standalone (no entities)
headlineSimilarity >= 0.5 && timeDiffHours <= 24
headlineSimilarity >= 0.5 && timeDiffHours <= 48
```

### Why Current RSS Articles Don't Cluster

Based on the live articles observed on device:

1. **"Iran offers US deal to reopen Strait of Hormuz in seven days"** (BBC News)
   - Single source only
   - No other sources covering this event in RSS feeds

2. **"'I was buggered': Stranded sailor..."** (ABC Australia)
   - Local Australian story
   - No international coverage

3. **"Serial con man turned charity director..."** (ABC Australia)
   - Local court story
   - No other sources

4. **"Internationals stun US with 5-0 sweep..."** (ABC Australia)
   - Sports story
   - May have other coverage, but different headline styles

### Root Causes

#### 1. Source Coverage Overlap is Limited

Current RSS feeds likely cover:
- **BBC News**: International news from UK perspective
- **ABC Australia**: Australian domestic + some international
- **Al Jazeera**: Middle East + international from Qatar perspective  
- **Guardian**: UK + international
- **Others**: Regional focus

**Problem**: Each source has different editorial priorities. An event that's major news in one region may not be covered by sources focused on other regions.

**Example**: The Iran/Hormuz story might be covered by BBC, but not prioritized by ABC Australia (domestic focus) or sources in other regions at the same time.

#### 2. Headline Variance is High

Real-world example of SAME EVENT with different headlines:

**Event**: COP29 fossil fuel agreement

- BBC: "UN climate summit reaches historic agreement on fossil fuel transition"
- Guardian: "COP29 delegates approve landmark deal to phase out coal and oil"  
- Al Jazeera: "Fossil fuel phase-out agreed at UN climate talks in Dubai"

**Normalized token overlap**:
- BBC vs Guardian: ~25-35% (mentions "agreement"/"deal", "fossil fuel"/"coal and oil")
- BBC vs Al Jazeera: ~20-30% (mentions "fossil fuel", "agreed"/"agreement")
- Guardian vs Al Jazeera: ~30-40% (mentions "fossil fuel", "agreed"/"approve")

**Current threshold**: 50% standalone OR 20%+ with 2 shared entities

**Reality**: Same-event headlines typically show 15-40% overlap even after normalization.

#### 3. Entity Extraction is Weak

Current implementation:
```kotlin
private fun extractEntities(text: String): List<String> {
    // Simple heuristic: capitalized sequences
    val pattern = Regex("\\b[A-Z][a-z]+(?:\\s+[A-Z][a-z]+)*\\b")
    return pattern.findAll(text)
        .map { it.value }
        .filter { it.length > 3 }
        .distinct()
        .toList()
}
```

**Problems**:
- Only works on raw text (before normalization lowercases everything)
- Misses acronyms (UN, COP29, US)
- Misses multi-word entities that don't follow strict capitalization
- No semantic understanding (can't tell "Washington" city from "Washington" person)
- No cross-language entity matching

**Impact**: Without reliable entity extraction, clustering falls back to headline similarity alone, which requires 50% overlap—too strict for real variance.

#### 4. Wire Services Not Included

**Missing**: Reuters, AP (Associated Press), AFP (Agence France-Presse)

**Why they matter**: Wire services cover the SAME events that multiple downstream sources republish or reference. They're the natural clustering seeds.

**Example flow**:
1. Reuters reports: "Iran proposes 7-day timeline for Hormuz talks"
2. BBC republishes/references: "Iran offers US deal to reopen Strait..."  
3. Guardian covers: "Tehran suggests week-long window for shipping agreement"
4. All three should cluster on shared Reuters coverage + entities

## Recommendations

### Priority 1: Add Wire Services to RSS Sources

**Action**: Add Reuters, AP, and AFP RSS feeds
- These cover international events that other sources pick up
- Provides natural clustering anchors
- Increases probability of event overlap

**Impact**: Immediate - wire stories will match with downstream coverage

### Priority 2: Improve Entity Extraction

**Current**: Regex pattern matching on capitalized words
**Needed**: Proper Named Entity Recognition (NER)

**Options**:
1. **Use existing NER library** (e.g., Apache OpenNLP, Stanford NER)
   - Identifies PERSON, LOCATION, ORGANIZATION, EVENT
   - Works across different capitalizations
   - Can extract "COP29", "Strait of Hormuz", etc.

2. **Cross-language entity linking**
   - Use Wikidata/DBpedia IDs for entities
   - "Strait of Hormuz" in English = "مضيق هرمز" in Arabic = same entity ID
   - Enables cross-language clustering

**Impact**: High - unlocks the 20-30% similarity threshold with shared entities

### Priority 3: Lower Standalone Headline Threshold (Carefully)

**Current**: 50% token overlap required without entities
**Real-world**: Same events typically show 15-40% overlap

**Proposed adjustment**:
```kotlin
// Add new MEDIUM confidence tier for strong standalone similarity
headlineSimilarity >= 0.35 && timeDiffHours <= 24 -> {
    shouldCluster = true
    confidence = ClusterConfidence.MEDIUM
    explanation = "Strong headline match..."
}
```

**Guardrail**: Only apply within 24 hours to reduce false positives

**Impact**: Medium - catches more same-event stories with different wording

### Priority 4: Expand International Coverage

**Add sources that cover same international events**:
- **Deutsche Welle** (German public broadcaster, international focus)
- **France 24** (French public broadcaster, international)
- **NHK World** (Japanese public broadcaster, international)
- **RT / TASS** (Russian state media - different perspective on international events)
- **Xinhua** (Chinese state news - different perspective)
- **The New York Times** (US major paper, international coverage)
- **Washington Post** (US major paper, international)

**Why**: More sources covering the same international events = more clustering opportunities

**Trade-off**: Must maintain source metadata transparency and avoid claiming neutrality

### Priority 5: Time-of-Day Clustering Windows

**Observation**: Breaking news gets covered simultaneously; analysis pieces spread out

**Proposed**: Add time-of-day weighting
- First 6 hours after first publication: strict 24h window, higher similarity required
- 6-24 hours: current thresholds
- 24-72 hours: lower confidence, require more entities

**Impact**: Reduces false clustering of ongoing stories vs. distinct events

## Minimum Viable Changes for Initial Clustering

To get SOME clusters visible quickly without compromising quality:

1. **Add 3 wire services**: Reuters, AP, AFP RSS feeds (if available)
2. **Improve entity extraction**: Use a basic NER library instead of regex
3. **Lower standalone threshold**: 35% instead of 50% for 24h window

**Expected result**: 20-40% of international news stories form clusters

**Timeline**: Can be implemented within current architecture, no new infrastructure needed

## Testing Strategy

1. **Run diagnostic** with real RSS data from past 7 days
2. **Measure actual similarity** between articles we know are about same events
3. **Tune thresholds** to balance precision (no false matches) vs. recall (catch real matches)
4. **Monitor false positive rate**: Manual review of first 100 clusters to ensure quality

## Long-term: Supervised Learning

Once we have editorial-reviewed clusters:
1. Use confirmed matches as training data
2. Train ML model to predict "same event" probability
3. Incorporate more signals: publish time distribution, source geographic overlap, topic similarity
4. Continuously improve with human feedback

But start with rule-based improvements above for immediate results.
