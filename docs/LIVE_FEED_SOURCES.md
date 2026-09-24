# CrossLens Live Feed Sources

**Version:** 0.0.14-beta  
**Last Updated:** 2026-09-24

## Overview

CrossLens v0.0.14-beta introduces live RSS feed ingestion from approved international news publishers. This document lists each source, its feed URL, permissions, and known limitations.

## Current Sources

### 1. BBC News (United Kingdom)

- **Feed URL:** `https://feeds.bbci.co.uk/news/rss.xml`
- **Language:** English (en-GB)
- **Publishing Region:** United Kingdom / Europe
- **Update Frequency:** TTL 15 minutes (per feed metadata)
- **Content Provided:** Title, description, link, publication date, media thumbnails
- **Terms Reference:** https://www.bbc.co.uk/usingthebbc/terms-of-use/#15metadataandrssfeeds
- **Attribution Requirements:**
  - Copyright: © British Broadcasting Corporation
  - Requires source attribution and original article link
  - Feed explicitly provides RSS metadata for reuse
- **Limitations:**
  - Excerpt-only; full article requires visiting original URL
  - Some articles may require subscription/UK location
  - Feed is UK-focused with international coverage

### 2. Al Jazeera English (Qatar)

- **Feed URL:** `https://www.aljazeera.com/xml/rss/all.xml`
- **Language:** English
- **Publishing Region:** Qatar / Middle East
- **Update Frequency:** Frequent (cache headers present)
- **Content Provided:** Title, description, link, publication date, categories
- **Terms Reference:** Feed copyright states "© 2026 Al Jazeera Media Network"
- **Attribution Requirements:**
  - Copyright: © Al Jazeera Media Network
  - WordPress-powered RSS with standard reuse expectations
  - Original article link required
- **Limitations:**
  - Excerpt-only; full article at original URL
  - English-language edition of Arabic-origin content
  - International focus with Middle East emphasis

### 3. Deutsche Welle (Germany)

- **Feed URL:** `https://rss.dw.com/xml/rss-en-all`
- **Language:** English
- **Publishing Region:** Germany / Europe
- **Update Frequency:** TTL 10 minutes
- **Content Provided:** Title, description, link, publication date, categories
- **Terms Reference:** Feed copyright states "© DW"
- **Attribution Requirements:**
  - Copyright: 2026 DW
  - Public RSS feed with CORS-friendly headers
  - Original article link required
- **Limitations:**
  - English edition; German, Spanish, and Arabic variants available
  - German public broadcaster perspective
  - International coverage with European focus

### 4. France 24 (France)

- **Feed URL:** `https://www.france24.com/en/rss`
- **Language:** English
- **Publishing Region:** France / Europe
- **Update Frequency:** 5-minute cache (per cache-control header)
- **Content Provided:** Title, description, link, publication date, categories, creator, media thumbnails
- **Terms Reference:** Standard RSS 2.0 with full metadata
- **Attribution Requirements:**
  - Well-structured RSS with creator attribution
  - Original article link required
  - Includes article categories for context
- **Limitations:**
  - English edition; French, Spanish, and Arabic variants available
  - French international news perspective
  - International coverage with French/European context

## Caching Strategy

CrossLens implements an offline-first caching strategy:

1. **Immediate Display:** Cached live data is shown instantly on app launch
2. **Background Refresh:** If cache is older than 15 minutes, refresh in background
3. **Pull-to-Refresh:** User-triggered refresh always forces live fetch
4. **Offline Availability:** Most recent successful cache remains available offline
5. **Fallback:** Mock demo data shown only when no live cache exists AND all sources fail

## Per-Source Failure Isolation

Each RSS source is fetched independently with:
- 10-second read timeout
- 5-second connection timeout
- 5MB maximum response size
- Graceful failure handling (empty list on error)
- One unavailable source never blocks others

## Privacy & Safety

- **No accounts or authentication:** All feeds are public
- **No location collection:** Source region is publishing context only
- **No advertising IDs or passive tracking**
- **No credentials in repository:** All sources use public URLs
- **Request headers:** User-Agent identifies as "CrossLens/0.0.14-beta (Android)"
- **HTTPS only:** All feeds and original article URLs use HTTPS

## Feed State Indicators

The app displays the current feed state clearly:

- **Live Feed:** Just fetched from sources
- **Cached Feed:** Displaying locally cached data (offline or stale)
- **Demo Fallback:** All sources unavailable, showing mock fixtures
- **Last Updated:** Real timestamp of last successful fetch
- **Source Count:** Number of sources that responded successfully

## Known Limitations

1. **No automatic event clustering:** v0.0.14 shows a chronological feed; articles are not yet matched across sources as covering the same event
2. **Headline-level only:** RSS provides title and excerpt; full article opens in-app browser
3. **No paywalled content bypass:** Original reporting may require publisher subscription
4. **English-first:** All four sources provide English feeds; multilingual expansion deferred
5. **No source metadata inference:** Source country is publishing location only, not a proxy for political stance or national viewpoint
6. **No editorial analysis:** Lens Gap, claims, and framing observations remain mock-only for v0.0.14

## Future Considerations

- **Multilingual variants:** DW, France 24, and Al Jazeera publish feeds in German, French, Spanish, and Arabic
- **Licensed API integration:** Architecture supports coexistence of RSS and licensed content APIs
- **Event clustering:** Cross-source matching can be added without changing feed infrastructure
- **Publisher agreements:** Additional sources require individual legal review and terms verification

## Technical Implementation

- **Parser:** Custom RSS 2.0 parser with robust error handling
- **Architecture:** `RssSourceAdapter` implements existing `SourceAdapter` interface
- **Repository:** `LiveStoryRepository` manages caching and fallback logic
- **Database:** Room entities store live feed metadata and articles
- **UI:** Feed state indicator in HomeScreen header

## Maintenance

RSS feeds may change structure, move URLs, or update terms of use. Monitor:
- Feed availability (HTTP status)
- Parse errors (malformed XML)
- Terms of use updates
- Attribution requirement changes

Last verified: 2026-09-24
