# Entity Extraction and Cross-Language Event Matching

The entity extraction system identifies CANDIDATE event matches across languages using shared entity IDs. It runs after syndication detection and before title-similarity clustering, enabling cross-language coverage identification where headline token overlap would fail.

## Purpose

News outlets worldwide cover the same events in different languages. A Geneva climate summit may be reported by BBC (English), Le Monde (French), and Al Jazeera (Arabic) with completely different vocabulary, making title-similarity clustering ineffective.

Entity extraction provides:
- Stable entity IDs that link across languages ("person:biden", "location:geneva", "org:un")
- Cross-language candidate matches based on shared entities
- Transparent evidence for editorial review
- Conservative confidence bands reflecting match strength

**IMPORTANT**: This produces CANDIDATE matches requiring editorial review. Entity overlap suggests articles may cover the same event, but:
- Does NOT confirm they are the same event
- Does NOT determine independent vs. syndicated reporting
- Does NOT determine factuality or accuracy
- Does NOT determine political orientation or bias
- Does NOT calculate Lens Gap

## Architecture

### Mock-Only Prototype

This is a **mock-only prototype** using explicit entity metadata from test fixtures. It does NOT perform automated entity recognition.

**Production requirements**:
- Licensed entity extraction API (e.g., Google Cloud Natural Language, AWS Comprehend)
- Self-hosted entity recognition model (e.g., spaCy, Stanza, fine-tuned transformer)
- Entity resolution system for handling name variations
- Entity disambiguation for common names (e.g., multiple people named "John Smith")

### Entity Types

```kotlin
sealed class Entity {
    abstract val id: String              // Stable ID for cross-language matching
    abstract val displayName: String     // Localized display name
    abstract val sourceLanguage: String  // Language of this display name
}
```

**Person Entity**
- ID format: `person:<slug>` (e.g., `person:biden`, `person:un-secretary-general`)
- Display names per language: "Joe Biden" (en), "Joe Biden" (fr), "جو بايدن" (ar)
- Optional role: "President", "Prime Minister", "Secretary-General"

**Organization Entity**
- ID format: `org:<slug>` (e.g., `org:un`, `org:european-union`)
- Display names per language: "United Nations" (en), "ONU" (fr), "الأمم المتحدة" (ar)
- Optional type: "government", "ngo", "company", "international"

**Location Entity**
- ID format: `location:<slug>` (e.g., `location:geneva`, `location:washington`)
- Display names per language: "Geneva" (en), "Genève" (fr), "جنيف" (ar)
- Optional ISO country code: "CH", "US", "FR"

**Date Entity**
- ID format: `date:<YYYY-MM-DD>` (e.g., `date:2026-09-22`)
- Display names per language: "September 22, 2026" (en), "22 septembre 2026" (fr)

**EventIdentifier Entity**
- ID format: `event:<slug>` (e.g., `event:geneva-climate-summit-2026`)
- Display names per language: "Geneva Climate Summit 2026" (en), "Sommet climatique de Genève 2026" (fr)
- Optional type: "summit", "election", "crisis", "olympics"

### Entity ID Stability

Entity IDs must be stable across:
- Languages: Same person has same ID in all languages
- Sources: Same organization has same ID across all outlets
- Time: Same location has same ID regardless of when mentioned

**Production challenges**:
- Name variations: "UN" vs. "United Nations" vs. "U.N."
- Transliterations: "Beijing" (en) vs. "Pékin" (fr) vs. "北京" (zh)
- Ambiguity: Multiple people named "John Smith"
- Historical changes: Country names, organizational rebrands

Prod uction requires entity resolution system to canonicalize variants to stable IDs.

## Cross-Language Matching

### Matching Algorithm

1. **Filter**: Only consider articles with at least 2 entities
2. **Language check**: Skip same-language pairs (use title similarity instead)
3. **Time window**: Within 72 hours of each other
4. **Entity overlap**: Calculate shared entity IDs
   - Shared count: Number of entity IDs in both articles
   - Overlap score: `sharedCount / min(leftEntityCount, rightEntityCount)`
5. **Threshold**: Minimum 2 shared entities AND 30% overlap score
6. **Confidence**: Assign band based on entity diversity and count

### Confidence Bands

**HIGH**
- Event identifier entity present
- 3+ shared entities including event ID
- Diverse entity types

**Example**: BBC (en) and Le Monde (fr) both report "Geneva Climate Summit 2026" with shared entities:
- `event:geneva-climate-summit-2026`
- `location:geneva`
- `org:un`
- `person:un-secretary-general`

→ HIGH confidence candidate: Likely same event.

**MEDIUM**
- 2+ diverse entity types (person + org + location)
- 3-4 shared entities without event ID
- No generic-only matches

**Example**: NYT (en) and Le Monde (fr) both report G7 summit with shared:
- `location:brussels`
- `person:us-president`
- `org:g7`

→ MEDIUM confidence candidate: Possible same event, review recommended.

**LOW**
- Minimal overlap (2-3 shared entities)
- Generic entities only (e.g., `location:washington`, `location:beijing`)
- Short entity lists

**Example**: Two articles both mention Washington and China:
- `location:washington`
- `location:beijing`

→ LOW confidence candidate: Weak overlap, likely different events.

**UNCERTAIN**
- <2 shared entities
- Insufficient evidence for candidate

## Candidate Match Model

```kotlin
data class CandidateEventMatch(
    val id: String,                     // Unique candidate ID
    val articleIds: List<String>,       // All articles in candidate
    val sourceIds: List<String>,        // All sources
    val languages: List<String>,        // All languages
    val sharedEntities: List<String>,   // Entity IDs present in 2+ articles
    val confidence: EntityMatchConfidence,
    val matchingMethod: String,         // "entity-overlap-v1"
    val requiresEditorialReview: Boolean, // Always true
    val rationale: String,              // Human-readable explanation
    val uncertaintyReasons: List<String> // Why this needs review
)
```

**Uncertainty reasons** always include:
- "Cross-language match requires editorial verification"
- "Generic entities may appear in unrelated events" (if generic present)
- "Minimal entity overlap (N shared entities)" (if < 3)
- "Multiple languages (N) increase ambiguity" (if > 2)

## Editorial Review Workflow

### Reviewing Candidate Matches

1. **Inspect shared entities**: Which entities are common? Event ID? Generic locations?
2. **Read original articles**: Do they actually cover the same event?
3. **Check publication times**: Close together or days apart?
4. **Assess syndication**: Are articles syndicated + entity-matched?

### Editorial Decisions

**Confirmed same event**
- Articles cover same event with independent reporting
- Count as multiple independent sources
- Example: BBC English + Le Monde French original reporting on Geneva summit

**Different events**
- Articles share generic entities but different events
- Example: Both mention "Washington" and "China" but one is trade deal, other is tech competition

**Syndication + same event**
- Articles are syndicated translations + entity-matched
- Count as ONE independent source, not multiple
- Example: AP wire story republished by BBC (English) and Le Monde (French translation)

**Uncertain**
- Not enough information to confirm
- Flag for later review or additional research

### Independent Source Counting

When syndication and entity matching overlap:

| Syndication | Entity Match | Interpretation | Independent Count |
|-------------|--------------|----------------|-------------------|
| ✗ No | ✓ Yes | Independent cross-language reporting | N sources |
| ✓ Yes | ✗ No | Syndicated, same language | 1 source |
| ✓ Yes | ✓ Yes | Syndicated translation | 1 source |
| ✗ No | ✗ No | Unrelated articles | N sources |

**Key principle**: Syndication status determines independent-source count, not entity overlap.

## Limitations and Edge Cases

### Generic Entities

Common entities create false positives:
- `location:washington`, `location:beijing`, `location:brussels`, `location:moscow`
- `org:us-government`, `org:chinese-government`

Two articles both mentioning Washington and China may be:
- Same US-China event (correct match)
- Different US-China events (false positive)

**Mitigation**: Detector flags generic entities in uncertainty reasons, assigns LOW confidence.

### Short Entity Lists

Articles with <2 entities cannot be cross-language matched, even if same event.

**Example**: Article only mentions "Geneva" and "climate" without named people/organizations.

**Mitigation**: Rely on title similarity clustering for same-language articles.

### Entity Extraction Quality

Mock prototype uses perfect entity metadata. Production NLP will have errors:
- Missed entities (false negatives)
- Incorrect entities (false positives)
- Entity type errors (person tagged as organization)
- Entity disambiguation errors (wrong "John Smith")

**Mitigation**: Conservative thresholds, editorial review, human-in-the-loop validation.

### Multilingual Transliteration

Names transliterate differently across scripts:
- "Beijing" (en) vs. "Pékin" (fr) vs. "北京" (zh)
- "Gaddafi" vs. "Qaddafi" vs. "Kadhafi" (multiple romanizations)

**Mitigation**: Entity resolution system must canonicalize variants to stable IDs.

### Temporal Ambiguity

Same event reported days/weeks apart:
- Initial reports vs. follow-up coverage
- Event unfolding over time

**Current**: 72-hour time window. Articles outside window not matched.

**Production consideration**: Allow configurable windows for ongoing events.

## What Entity Matching Does NOT Do

❌ **Automatically confirm same-event**: All matches are CANDIDATES requiring review  
❌ **Determine independent reporting**: Cannot distinguish syndication from original reporting  
❌ **Verify factuality**: Cannot determine if claims are accurate  
❌ **Determine political bias**: Cannot assess outlet orientation  
❌ **Calculate Lens Gap**: Entity matching is separate from Lens Gap scoring  
❌ **Extract entities from text**: Prototype uses mock metadata; production needs NLP  
❌ **Translate articles**: Original language and text always preserved  

## Testing and Validation

All detection scenarios covered by `CrossLanguageEventMatcherTest.kt`:

1. ✅ Same event across English/French/Arabic with shared entities → HIGH confidence candidate
2. ✅ Different events with generic shared entities → LOW confidence or no match
3. ✅ Same event with no shared entity evidence → No match
4. ✅ Generic shared terms (Washington, China) → LOW confidence, flagged uncertainty
5. ✅ Original language and source attribution preserved
6. ✅ All candidate matches require editorial review
7. ✅ Transparent detection evidence (method, confidence, shared entities)
8. ✅ Same-language articles use title similarity, not entity matching
9. ✅ Entity extraction statistics reported

## Before Production Use

Before deploying entity extraction to production:

1. **Integrate NLP system**:
   - Licensed entity extraction API OR self-hosted model
   - Entity resolution for name variants
   - Entity disambiguation for common names
   - Performance profiling (latency, accuracy)

2. **Entity ID registry**:
   - Canonical entity IDs for common entities
   - Alternate spellings and transliterations
   - Historical name changes (e.g., country renames)
   - Disambiguation rules

3. **Manual audit**:
   - Review 100+ candidate matches to measure precision/recall
   - Tune thresholds (currently 2 entities, 30% overlap)
   - Adjust confidence band definitions based on false positive rate

4. **Editorial UI**:
   - Interface for reviewing candidate matches
   - Display shared entities with context
   - Side-by-side article comparison
   - Decision recording (confirm/reject/uncertain)

5. **Syndication integration**:
   - Combined view of syndication + entity evidence
   - Independent-source counting guidance
   - Decision precedent tracking

6. **Metrics dashboard**:
   - Candidate match rates
   - Confirmation/rejection rates
   - False positive/negative tracking
   - Entity extraction accuracy

7. **Privacy and content limits**:
   - Do not extract or store PII beyond what's in article excerpts
   - Do not infer unpublished relationships between entities
   - Do not create entity profiles from aggregated mentions

## API Example

```kotlin
val extractor = EntityExtractor()
val matcher = CrossLanguageEventMatcher(
    timeWindow = Duration.ofHours(72),
    minimumSharedEntities = 2,
    minimumEntityOverlap = 0.3
)

// Extract entities (mock or NLP)
val articlesWithEntities = extractor.extractBatch(articles, mockEntityMap)

// Find cross-language candidates
val result = matcher.findCandidateMatches(articlesWithEntities)

// Review candidates
result.candidateMatches.forEach { match ->
    println("Candidate: ${match.id}")
    println("Languages: ${match.languages.joinToString()}")
    println("Shared entities: ${match.sharedEntities.joinToString()}")
    println("Confidence: ${match.confidence}")
    println("Rationale: ${match.rationale}")
    println("Uncertainty: ${match.uncertaintyReasons.joinToString()}")
    println("Requires review: ${match.requiresEditorialReview}")
}
```

## See Also

- [Ingestion Prototype](INGESTION_PROTOTYPE.md) - Overview of the full ingestion pipeline
- [Syndication Detection](SYNDICATION_DETECTION.md) - Wire-copy and reprint detection
- [Source Registry](SOURCE_REGISTRY.md) - Content-use policy and source eligibility
- [Editorial Review](INGESTION_PROTOTYPE.md#editorial-review-workflow) - How reviewers approve/reject clusters
