# Event Clustering Implementation Status

## Completed (v0.0.15-beta, 2026-09-25)

### Core Infrastructure ✅

- **EventClusteringService**: Full implementation with conservative matching rules
  - Headline similarity using Jaccard distance
  - Named entity extraction and overlap detection
  - Time proximity filtering (72-hour window)
  - Publisher diversity enforcement
  - Confidence levels (HIGH, MEDIUM, LOW)
  - Detailed grouping explanations

- **Database Schema**: EventClusterEntity with Room persistence
  - Cluster metadata (summary, time, confidence)
  - Common entities tracking
  - Publisher count and article references
  - Image URL support
  - Automatic cleanup of 7+ day old clusters

- **Repository Layer**: EventClusterRepository
  - Cluster and persist articles
  - Observe clusters via Flow
  - Query clusters by ID
  - Cleanup operations

- **Feed Integration**: LiveStoryRepository updated
  - EventClusteringService integration
  - Creates clusters during feed refresh
  - Stores EventClusterEntity alongside StoryEntity
  - Handles unclustered articles as individual stories
  - Replaces old similarity grouping with intelligent clustering

- **Source Metadata**: SourceMetadataRegistry
  - 15 live RSS sources documented
  - Country, language, editorial description
  - Documented provenance for all descriptions
  - Demo sources clearly labeled
  - Safety-compliant metadata (no bias/ideology labels)

### Testing ✅

- **Unit Tests**: EventClusteringServiceTest (18 tests, all passing)
  - True matches (same specific event)
  - False negatives (same person/country/topic but different events)
  - Duplicate detection (same publisher)
  - Multilingual scenarios
  - Stale article handling
  - One-source event rejection
  - Confidence level assignment
  - Edge cases (empty lists, single articles)
  - Data preservation verification

- **Repository Tests**: LiveStoryRepositoryTest (all tests passing)
  - Feed refresh with clustering
  - Partial source failures
  - Image preservation
  - Cache behavior

### Documentation ✅

- **EVENT_CLUSTERING.md**: Comprehensive documentation
  - Grouping rules and thresholds
  - Source metadata policy
  - Safety guidelines (what we never claim)
  - Implementation details
  - Current limitations
  - Manual QA checklist
  - Test scenarios
  - Version history

### Matching Thresholds ✅

```
HIGH Confidence:
- 2+ shared entities + 20%+ headline similarity + within 24h
- 50%+ headline similarity + within 24h

MEDIUM Confidence:
- 2+ shared entities + 15%+ headline similarity + within 48h
- 1 shared entity + 30%+ headline similarity + within 24h
- 50%+ headline similarity + within 48h

Absolute Limits:
- Maximum time window: 72 hours
- Minimum publishers: 2 distinct sources
- No same-publisher clustering
```

### Safety Guidelines ✅

Implemented and documented:
- Never claim an article is true/false
- Never claim a source is biased/unbiased or reliable/unreliable
- Never claim a nation has one perspective
- Never infer political ideology
- Only show documented editorial descriptions with provenance
- All original data preserved (headlines, timestamps, excerpts, attribution)

## In Progress / Not Yet Implemented

### UI Components (Next Priority)

- **Event Card Component**: Home feed display
  - [ ] Event title/summary
  - [ ] Source count badge
  - [ ] Publisher names list
  - [ ] Source countries/regions
  - [ ] Source languages
  - [ ] Primary image display
  - [ ] Tap to comparison navigation
  - [ ] Clustering confidence indicator (optional)
  - [ ] Accessibility: screen reader support for source count

- **Event Comparison Screen**:
  - [ ] Screen layout (scrollable, mobile-first)
  - [ ] Header with event summary and metadata
  - [ ] Article cards for each source
    - [ ] Original headline
    - [ ] Publisher name with country/language
    - [ ] Publication timestamp (relative and absolute)
    - [ ] Article excerpt
    - [ ] Article image (if available)
    - [ ] Source editorial description (if documented)
    - [ ] "Open Original" button/link
  - [ ] Grouping explanation footer
  - [ ] Observable comparison details only (no "truth" claims)
  - [ ] Dark/light theme support
  - [ ] Accessibility: proper heading hierarchy, touch targets

- **Navigation**:
  - [ ] EventCluster story type detection in feed
  - [ ] Navigation from home feed to comparison screen
  - [ ] Back navigation with proper state preservation
  - [ ] Deep linking support (optional)

### Data Enhancements

- **Improved Entity Extraction**: (Future)
  - [ ] Replace capitalization heuristics with proper NER model
  - [ ] Language-specific entity recognition
  - [ ] Entity disambiguation

- **Multilingual Clustering**: (Future)
  - [ ] Cross-language headline comparison
  - [ ] Translation-aware similarity
  - [ ] Language-specific normalization

- **Semantic Similarity**: (Future)
  - [ ] Embeddings-based headline comparison
  - [ ] Paraphrase detection
  - [ ] Contextual similarity

### Quality Improvements

- **Clustering Refinement**:
  - [ ] User feedback mechanism (report bad clusters)
  - [ ] Follow-up/update detection
  - [ ] Duplicate article detection (syndication)
  - [ ] Regional variant handling

- **Source Expansion**:
  - [ ] Add more international publishers
  - [ ] Non-English sources
  - [ ] Regional/local sources

## Known Limitations

1. **Entity Extraction**: Simple capitalization-based; may miss entities or create false positives
2. **English-Optimized**: Stop words and normalization tuned for English
3. **No Semantic Understanding**: Can't detect paraphrasing beyond token overlap
4. **No Cross-Lingual Clustering**: French/English coverage of same event won't cluster
5. **No Duplicate Detection**: Syndicated content may appear multiple times
6. **No Follow-up Linking**: Updates to same story treated as new events
7. **Limited Sources**: Only 15 RSS feeds configured

## Testing Status

### Unit Tests: ✅ All Passing
- EventClusteringServiceTest: 18/18 tests passing
- LiveStoryRepositoryTest: All tests passing

### Integration Tests: Pending Device Testing
- [ ] Build and install debug APK on Pixel device
- [ ] Verify real event clustering with live RSS feeds
- [ ] Test event card display in home feed
- [ ] Test comparison screen navigation and display
- [ ] Verify images in event flow
- [ ] Test dark/light theme rendering
- [ ] Verify cached restart behavior
- [ ] Accessibility testing with TalkBack
- [ ] Screenshot capture for documentation

### Manual QA: Pending
- [ ] True match verification (3+ real events)
- [ ] False negative check (missed clusters)
- [ ] False positive check (incorrect clusters)
- [ ] Source attribution accuracy
- [ ] Original data preservation
- [ ] Safety language audit
- [ ] Metadata provenance check

## Next Steps

1. **Build Event Card UI** (HomeScreen.kt or new EventClusterCard.kt)
   - Design card layout showing cluster metadata
   - Implement tap handler for navigation
   - Add accessibility labels

2. **Build Comparison Screen** (new EventComparisonScreen.kt)
   - Layout article cards with source attribution
   - Add "Open Original" deep links
   - Include grouping explanation
   - Implement proper accessibility

3. **Wire Navigation**
   - Update HomeScreen to detect EVENT_CLUSTER stories
   - Create comparison screen route
   - Handle navigation arguments (cluster ID)

4. **Device Testing**
   - Build debug APK: `./gradlew assembleDebug`
   - Install on Pixel: `adb install app/build/outputs/apk/debug/app-debug.apk`
   - Verify real clustering with live feeds
   - Capture screenshots
   - Test accessibility with TalkBack

5. **Documentation**
   - Update README with event clustering feature
   - Add screenshots to docs
   - Document QA results
   - Record any discovered limitations

## Build Commands

```bash
# Run all tests
./gradlew testDebugUnitTest

# Build debug APK
./gradlew assembleDebug

# Install on device
adb install app/build/outputs/apk/debug/app-debug.apk

# View logs
adb logcat | grep CrossLens

# Capture screenshot
adb shell screencap -p /sdcard/screenshot.png
adb pull /sdcard/screenshot.png
```

## Acceptance Criteria

### Must Have (Before Merge)
- ✅ Core clustering logic implemented and tested
- ✅ Database persistence working
- ✅ Feed integration complete
- ✅ Source metadata documented
- ✅ Safety guidelines enforced
- ✅ All unit tests passing
- [ ] Event cards display in home feed
- [ ] Comparison screen functional
- [ ] Real event verification on device
- [ ] Screenshots captured
- [ ] Documentation complete

### Should Have (Near Term)
- [ ] Accessibility tested with TalkBack
- [ ] Dark/light theme verified
- [ ] Performance profiling (clustering time, memory)
- [ ] Edge case testing (empty clusters, network failures)

### Nice to Have (Future)
- Improved entity extraction (NER model)
- Cross-lingual clustering
- Semantic similarity
- User feedback mechanism
- Source expansion (25+ publishers)

## Version

**Current**: v0.0.15-beta (infrastructure complete, UI pending)
**Target**: v0.0.16-beta (full feature with UI)
