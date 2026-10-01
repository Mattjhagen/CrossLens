# Event Clustering Documentation

## Overview

CrossLens uses conservative event clustering to group articles from multiple publishers when they report on the same specific event. This feature allows readers to compare how different sources cover the same story.

## Key Principles

1. **Same Specific Event Only**: Articles are grouped only when they describe THE SAME SPECIFIC EVENT
2. **Conservative Matching**: When uncertain, articles remain separate
3. **Multiple Publisher Requirement**: Valid clusters must have articles from at least 2 distinct publishers
4. **Original Data Preservation**: All original headlines, excerpts, timestamps, and source attribution are preserved
5. **Transparent Grouping**: Each cluster includes an explanation of why articles were grouped

## Grouping Rules

### Matching Criteria

Articles are compared using multiple signals:

#### 1. Headline Similarity
- Normalized using lowercase, punctuation removal, and stop-word filtering
- Measured using Jaccard similarity (token overlap)
- Higher similarity indicates same event

#### 2. Named Entity Overlap
- Extracts people, places, and organizations from text
- Shared entities suggest same event coverage
- Currently uses simple capitalization heuristics (production would use NER model)

#### 3. Time Proximity
- Articles about same event typically published within hours
- Maximum 72-hour window for clustering
- Closer timestamps increase match confidence

#### 4. Publisher Diversity
- Articles from same publisher never cluster together
- Prevents duplicate/update articles within single source

### Confidence Levels

**HIGH Confidence**:
- 2+ shared entities + 20%+ headline similarity + within 24 hours
- 50%+ headline similarity + within 24 hours (even without entities)
- 3+ publishers in cluster

**MEDIUM Confidence**:
- 2+ shared entities + 15%+ headline similarity + within 48 hours
- 1 shared entity + 30%+ headline similarity + within 24 hours
- 50%+ headline similarity + within 48 hours
- 2 publishers in cluster

**LOW Confidence**:
- Rarely used; most borderline cases remain unclustered

### What Does NOT Trigger Clustering

- **Same person in different events**: "Biden announces climate policy" vs "Biden visits NATO summit"
- **Same country in different events**: "France economic report" vs "France Olympics preparation"
- **Same broad topic**: "California wildfire" vs "Oregon wildfire"
- **Similar but distinct events**: Different elections, different meetings, different incidents
- **Articles more than 72 hours apart**: Likely different stages/follow-ups

## Current Thresholds

```kotlin
HIGH Confidence:
- 2+ entities && similarity >= 0.20 && time <= 24h
- similarity >= 0.50 && time <= 24h

MEDIUM Confidence:
- 2+ entities && similarity >= 0.15 && time <= 48h
- 1+ entities && similarity >= 0.30 && time <= 24h
- similarity >= 0.50 && time <= 48h

Time Limits:
- Maximum clustering window: 72 hours
- Preferred clustering window: 24-48 hours
```

## Source Metadata Policy

### What We Show

CrossLens displays factual source context to help readers understand where coverage originates:

- Publisher name
- Country/region of origin
- Primary language
- Homepage URL
- Editorial description (when documented)

### What We DO NOT Show

- **Political ideology labels**: We never assign "left", "right", "liberal", "conservative", etc.
- **Bias ratings**: We do not rate sources as "biased" or "unbiased"
- **Truthfulness claims**: We never label a source or article as "true", "false", "reliable", or "unreliable"

### Editorial Descriptions

Editorial descriptions are shown ONLY when:
1. They are factual (not evaluative)
2. They have documented provenance
3. The provenance is displayed to readers

Examples of acceptable descriptions:
- "British public service broadcaster" (from BBC Royal Charter)
- "State-funded international news service" (from corporate profile)
- "Owned by [Company Name]" (from documented ownership)

Examples of UNACCEPTABLE descriptions:
- "Reliable news source"
- "Left-leaning publication"
- "Known for balanced reporting"
- Any undocumented claim

## Safety Guidelines

### What Clustering Does NOT Claim

1. **Never claims an article is true or false**
2. **Never claims a publisher is reliable or unreliable**
3. **Never claims a source is biased or unbiased**
4. **Never claims a nation has one perspective**
5. **Never claims clustering represents "all views"**
6. **Never claims one article is more accurate than another**

### Language to Avoid

**NEVER say**:
- "This source is more reliable"
- "This coverage is biased"
- "This represents the French perspective"
- "These sources agree on the facts"
- "This is the truth"

**SAFE alternatives**:
- "3 sources reported this event"
- "Published in France, UK, and Qatar"
- "Available in English, French, and Arabic"
- "Different wording in headlines"
- "Published within 2 hours of each other"

## Implementation Details

### Database Schema

**EventClusterEntity**:
- `id`: Unique cluster identifier
- `eventSummary`: Human-readable event description (derived from article headlines)
- `eventTime`: Earliest publication timestamp
- `clusteredAt`: When cluster was created
- `confidence`: HIGH, MEDIUM, or LOW
- `groupingExplanation`: Why articles were grouped
- `commonEntities`: Shared named entities
- `publisherCount`: Number of distinct publishers
- `imageUrl`: Primary image (from first article)
- `articleIds`: References to articles in cluster

### Clustering Pipeline

1. **Fetch Phase**: RSS adapters fetch latest articles
2. **Balance Phase**: Limit to 5 articles per publisher to prevent single-source flooding
3. **Clustering Phase**: EventClusteringService applies matching rules
4. **Persistence Phase**: 
   - Valid clusters (2+ publishers) saved as EventClusterEntity
   - Corresponding StoryEntity created with multiple articleIds
   - Unclustered articles saved as individual stories
5. **Cleanup Phase**: Clusters older than 7 days are removed

### Repository Layer

- **EventClusterRepository**: Manages clustering and persistence
- **LiveStoryRepository**: Integrates clustering into feed refresh
- **EventClusteringService**: Core matching logic

## Limitations

### Current Limitations

1. **Simple Entity Extraction**: Uses capitalization heuristics instead of proper NER
2. **English-Optimized**: Stop words and normalization tuned for English
3. **No Semantic Understanding**: Can't detect paraphrasing or conceptual similarity
4. **No Cross-Lingual Clustering**: English and French articles about same event won't cluster unless headlines are very similar
5. **No Duplicate Detection**: Same article republished by partner sites may not be detected
6. **No Follow-up Detection**: Updates/follow-ups to same story treated as new events
7. **Limited Source Coverage**: Only RSS feeds from configured publishers

### Known Edge Cases

1. **Breaking News Clusters**: Early reports may cluster before full event details emerge
2. **Translated Headlines**: Direct translations may not match due to word choice
3. **Regional Variants**: Same event with different regional focus may not cluster
4. **Scheduled Events**: Announcements vs actual event may cluster incorrectly
5. **Namesakes**: Different people/places with same name may create false entity overlap

### Future Improvements

1. **Proper NER Model**: Use production NLP for entity extraction
2. **Multilingual Support**: Cross-language clustering using translation or embeddings
3. **Semantic Similarity**: Use embeddings to detect paraphrasing
4. **Follow-up Detection**: Link updates to original stories
5. **Duplicate Detection**: Identify syndicated/republished content
6. **User Feedback**: Allow readers to report bad clusters
7. **Source Expansion**: Add more publishers and languages

## Manual QA Checklist

### Before Each Release

- [ ] **True Matches**: Verify at least 3 real multi-publisher events clustered correctly
- [ ] **False Negatives**: Check feed for obvious same-event articles that didn't cluster
- [ ] **False Positives**: Verify no different-event articles are clustered together
- [ ] **Source Attribution**: All articles show correct publisher name, country, language
- [ ] **Original Data**: Headlines, timestamps, excerpts match original source
- [ ] **Image Display**: Images appear for clustered events when available
- [ ] **Time Filtering**: No articles >72 hours apart are clustered
- [ ] **Publisher Diversity**: No same-publisher articles in a cluster
- [ ] **Safety Language**: No "truth", "bias", "reliable" language anywhere
- [ ] **Editorial Descriptions**: All descriptions have documented provenance
- [ ] **National Representation**: No language claiming "France says" or "US perspective"
- [ ] **Accessibility**: Screen reader announces source count, publisher names
- [ ] **Dark/Light Themes**: Event cards readable in both modes
- [ ] **Error States**: App handles empty clusters, network failures gracefully
- [ ] **Persistence**: Clusters survive app restart (cached behavior)

### Test Scenarios

1. **Same Event, Multiple Publishers**: Breaking news should cluster within minutes
2. **Same Person, Different Events**: Should remain separate
3. **Same Country, Different Events**: Should remain separate  
4. **Similar Topics**: Wildfires in different states should remain separate
5. **One-Source Events**: Should appear as individual cards, not clusters
6. **Stale Articles**: 4-day-old articles should not cluster with new ones
7. **Mixed Languages**: English/French coverage may remain separate (expected)
8. **Entity Overlap**: Verify shared people/places drive clustering
9. **Headline Variants**: Different phrasing of same event should cluster
10. **Time Proximity**: Articles published 1-2 hours apart should cluster more easily

## Metrics to Monitor

### Quality Metrics
- Average publisher count per cluster
- Confidence distribution (HIGH vs MEDIUM vs LOW)
- Cluster size distribution (2 publishers vs 3+ publishers)
- Unclustered article percentage

### Performance Metrics
- Clustering time per refresh
- Database query performance
- Memory usage during clustering
- Feed refresh latency

### User-Facing Metrics
- Clusters displayed vs individual stories
- Time to display event comparison
- Image load success rate
- Accessibility score

## Version History

- **v0.0.15-beta** (2026-09-25): Initial event clustering implementation
  - EventClusteringService with conservative matching
  - EventClusterEntity database schema
  - EventClusterRepository persistence layer
  - LiveStoryRepository integration
  - SourceMetadataRegistry with 15 publishers
  - Comprehensive safety guidelines
  - Test coverage for true matches, false negatives, edge cases
