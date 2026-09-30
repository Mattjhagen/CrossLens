# Read Across Coverage Feature

## Overview

The "Read across coverage" feature helps users broaden their understanding of one event through documented source diversity, without creating echo chambers or implying that every claim has equal evidentiary weight.

## Implementation (v0.0.14-beta)

### What It Does

When viewing an event comparison screen with multiple sources covering the same event, users will see a "Read across coverage" action that reveals 2-4 additional perspectives selected for documented diversity:

- **Geographic diversity**: Local or geographically relevant reporting, different countries/regions
- **Language diversity**: Original reporting in different languages when translated titles/excerpts are available
- **Source type diversity**: Different institutional contexts (public broadcaster, wire service, local publisher, etc.)

### What It Does NOT Do

- ❌ Infer or display political ideology
- ❌ Claim sources are neutral, biased, true, false, or representative of a nation
- ❌ Show unrelated articles just to increase diversity
- ❌ Use user's prior clicks, engagement history, or political preferences

### Selection Algorithm

The `ReadAcrossCoverageRecommender` scores candidate articles based on documented metadata:

**Diversity scoring:**
- Different country from shown sources: +3 points
- Different language from shown sources: +2 points
- Public broadcaster or wire service (from documented editorial description): +2 points
- Non-Western country (proxy for geographic diversity): +1 point
- Stable ID-based tiebreaker for deterministic ordering

**Selection:**
- Takes top 2-4 ranked candidates based on available diversity
- Generates factual explanation for each recommendation:
  - "Reporting from Ukraine"
  - "Original reporting in Spanish"
  - "Public broadcaster"
  - "Different publisher covering the same event"

### User Interface

**Action card** (appears between event header and article cards):
```
┌─────────────────────────────────────────────────────┐
│ Read across coverage                                │
│ 3 additional sources covering this event      [View]│
└─────────────────────────────────────────────────────┘
```

**Bottom sheet** (when user taps "View"):
```
┌─────────────────────────────────────────────────────┐
│ Read across coverage                                │
│                                                     │
│ Additional perspectives on the same event from      │
│ diverse sources                                     │
│                                                     │
│ ┌─────────────────────────────────────────────────┐│
│ │ Al Jazeera              Qatar • Arabic    2h ago││
│ │ ┌─────────────────────────────────────────────┐ ││
│ │ │ Reporting from Qatar • Public broadcaster  │ ││
│ │ └─────────────────────────────────────────────┘ ││
│ │ Iran proposes new timeline for Strait agreement││
│ │ Tehran officials said discussions continue...   ││
│ └─────────────────────────────────────────────────┘│
│                                                     │
│ ┌─────────────────────────────────────────────────┐│
│ │ Deutsche Welle          Germany • English 3h ago││
│ │ ┌─────────────────────────────────────────────┐ ││
│ │ │ Different country • Public broadcaster     │ ││
│ │ └─────────────────────────────────────────────┘ ││
│ │ Strait of Hormuz talks show signs of progress  ││
│ │ International mediators report constructive...  ││
│ └─────────────────────────────────────────────────┘│
│                                                     │
│ About these recommendations                         │
│ Articles are selected from the same event cluster   │
│ based on documented source diversity: different     │
│ countries, languages, and source types. We do not   │
│ infer political ideology or claim any source is     │
│ more truthful than another.                         │
└─────────────────────────────────────────────────────┘
```

### When It Appears

The feature only appears when:
1. ✅ Event has multiple sources (2+ distinct publishers)
2. ✅ Additional qualifying articles exist beyond those currently shown
3. ✅ Recommendations can be generated based on documented diversity

If no additional qualifying articles exist, the feature is hidden and users see the existing coverage-gap notice on the event comparison screen.

## Current Status

**Implementation:** ✅ Complete
**Unit tests:** ✅ 13 tests passing
**Build:** ✅ Successful
**APK:** ✅ Installed on Pixel device

**Visibility in current build:** ⚠️ Not yet visible in UI

The feature is implemented and tested but not visible in the current live RSS feed because:

1. **Current data source:** Live RSS feeds contain single-source articles, not multi-source event clusters
2. **Feature requirement:** Needs event clustering pipeline to group articles from different sources about the same specific event
3. **Next step:** Event clustering pipeline (planned) will create confident multi-source clusters, enabling this feature

### Example Event Cluster (when available)

```
Event: "Iran proposes 7-day timeline for Strait of Hormuz agreement"

Sources:
- BBC News (United Kingdom, English)
- Al Jazeera (Qatar, Arabic/English)
- Deutsche Welle (Germany, English)
- The Guardian (United Kingdom, English)
- Le Monde (France, French)

User viewing: BBC News, The Guardian

Recommendations shown:
1. Al Jazeera - "Reporting from Qatar • Public broadcaster"
2. Deutsche Welle - "Different country • Public broadcaster"  
3. Le Monde - "Original reporting in French"
```

## Technical Details

### Files

- `ReadAcrossCoverageRecommender.kt`: Recommendation algorithm
- `EventComparisonScreen.kt`: UI components (action card, bottom sheet)
- `EventComparisonViewModel.kt`: State management
- `ReadAcrossCoverageRecommenderTest.kt`: 13 unit tests

### Dependencies

- `Article` (core model): Event membership, language, timestamps
- `SourceMetadata`: Country, language, editorial description with provenance
- `EventComparisonUiState`: Recommendations list

### Integration Points

1. **Event clustering pipeline** (future): Must create confident event clusters with 2+ distinct publishers
2. **Source metadata registry**: Provides documented publisher information
3. **Translation system** (future): Enables cross-language recommendations

## Testing

Run tests:
```bash
./gradlew :app:testDebugUnitTest --tests "*ReadAcrossCoverageRecommenderTest*"
```

**Test coverage:**
- ✅ Empty recommendations when no additional articles
- ✅ Excludes already-shown articles
- ✅ Prioritizes different country
- ✅ Prioritizes different language
- ✅ Prioritizes public broadcasters
- ✅ Handles single-source events
- ✅ Returns 2-4 articles based on availability
- ✅ Generates factual explanations (country, language, source type)
- ✅ Excludes unrelated articles (different events)
- ✅ Handles missing metadata gracefully
- ✅ Maintains stable ordering
- ✅ Prefers wire services and international news services

## Future Enhancements

1. **Local source preference**: When event location is known, prioritize local reporting from that region
2. **Translation integration**: Show "Translated from X" when available
3. **User preferences**: Optional "prefer sources I haven't read before" setting
4. **Coverage gaps**: Explicitly note when important geographic regions or languages are missing
5. **Historical comparison**: Show how coverage evolved over time across sources

## Design Principles

This feature follows CrossLens's core principles:

✅ **Global by design**: Organize by source, country, region, language, institutional context  
✅ **Evidence stays visible**: Link recommendations to documented source metadata  
✅ **Translation with provenance**: Show original language, label translations  
✅ **Multiple dimensions**: Geographic, linguistic, institutional context coexist  
✅ **Explain the comparison**: Users understand why each article was recommended

## References

- Product principles: `README.md` § Product principles
- Source metadata: `app/src/main/java/com/crosslens/app/data/ingestion/SourceMetadata.kt`
- Event clustering model: `app/src/main/java/com/crosslens/app/core/model/EventCluster.kt`
