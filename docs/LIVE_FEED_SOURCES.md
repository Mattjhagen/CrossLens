# CrossLens Live Feed Sources

**Version:** 0.0.16-beta (Batch 3)  
**Last Updated:** 2026-10-04

## Overview

CrossLens introduces live RSS feed ingestion from 28 approved international news publishers spanning North America, Europe, Middle East, Asia-Pacific, Africa, and Eastern Europe. This document lists each source, its feed URL, permissions, and known limitations.

**Batch 3 Update (October 2026):** Added 5 sources for geographic diversification - Daily Maverick (South Africa), Euronews, RFI English, SCMP (Hong Kong, new URL), and The Moscow Times (Russia). All sources validated on Pixel 11 with 28/28 active, 0 failures.

## Current Sources (28 Total)

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

### 5. The Guardian (United Kingdom)

- **Feed URL:** `https://www.theguardian.com/world/rss`
- **Language:** English (en-GB)
- **Publishing Region:** United Kingdom / Europe
- **Content Provided:** Title, description, link, publication date
- **Attribution Requirements:**
  - Copyright notice present in feed
  - Original article link required
- **Limitations:**
  - World news section feed
  - Full articles require visiting original URL

### 6. The New York Times (United States)

- **Feed URL:** `https://rss.nytimes.com/services/xml/rss/nyt/World.xml`
- **Language:** English (en-US)
- **Publishing Region:** United States / North America
- **Content Provided:** Title, description, link, publication date
- **Attribution Requirements:**
  - Official public RSS feed
  - CORS-enabled, original article link required
- **Limitations:**
  - World news section feed
  - Most articles require subscription

### 7. CBC News (Canada)

- **Feed URL:** `https://www.cbc.ca/webfeed/rss/rss-topstories`
- **Language:** English (en-CA)
- **Publishing Region:** Canada / North America
- **Content Provided:** Title, description, link, publication date
- **Attribution Requirements:**
  - Canadian public broadcaster
  - Original article link required
- **Limitations:**
  - Top stories feed includes domestic and international

### 8. ABC News Australia (Australia)

- **Feed URL:** `https://www.abc.net.au/news/feed/51120/rss.xml`
- **Language:** English (en-AU)
- **Publishing Region:** Australia / Asia-Pacific
- **Content Provided:** Title, description, link, publication date
- **Attribution Requirements:**
  - Australian public broadcaster
  - Original article link required
- **Limitations:**
  - Mix of domestic and Asia-Pacific regional coverage

### 9. The Japan Times (Japan)

- **Feed URL:** `https://www.japantimes.co.jp/feed/`
- **Language:** English
- **Publishing Region:** Japan / Asia
- **Content Provided:** Title, description, link, publication date
- **Attribution Requirements:**
  - Japan's English-language newspaper
  - Original article link required
- **Limitations:**
  - English edition; focus on Japan and East Asian news

### 10. The Hindu (India)

- **Feed URL:** `https://www.thehindu.com/news/national/feeder/default.rss`
- **Language:** English (en-IN)
- **Publishing Region:** India / South Asia
- **Content Provided:** Title, description, link, publication date
- **Attribution Requirements:**
  - Major Indian newspaper
  - Original article link required
- **Limitations:**
  - National news section; mix of domestic and international

### 11. Der Spiegel International (Germany)

- **Feed URL:** `https://www.spiegel.de/international/index.rss`
- **Language:** English
- **Publishing Region:** Germany / Europe
- **Content Provided:** Title, description, link, publication date
- **Attribution Requirements:**
  - German news magazine English edition
  - Original article link required
- **Limitations:**
  - English translation of German content
  - Continental European perspective

### 12. Channel NewsAsia (Singapore)

- **Feed URL:** `https://www.channelnewsasia.com/api/v1/rss-outbound-feed?_format=xml`
- **Language:** English (en-SG)
- **Publishing Region:** Singapore / Southeast Asia
- **Content Provided:** Title, description, link, publication date
- **Attribution Requirements:**
  - Singapore public broadcaster
  - Original article link required
- **Limitations:**
  - Southeast Asian regional focus

### 13. swissinfo.ch (Switzerland)

- **Feed URL:** `https://www.swissinfo.ch/eng/feed/`
- **Language:** English
- **Publishing Region:** Switzerland / Europe
- **Content Provided:** Title, description, link, publication date
- **Attribution Requirements:**
  - Swiss public broadcaster
  - WordPress VIP platform, original article link required
- **Limitations:**
  - 301 redirect from /eng/rss/feed/ to /eng/feed/
  - Neutral Swiss perspective

### 14. ABC Spain (Spain)

- **Feed URL:** `https://www.abc.es/rss/feeds/abc_Internacional.xml`
- **Language:** Spanish (es)
- **Publishing Region:** Spain / Europe
- **Content Provided:** Title, description, link, publication date
- **Attribution Requirements:**
  - Spanish newspaper international section
  - Original article link required
- **Limitations:**
  - Spanish-language headlines displayed untranslated
  - International section feed

### 15. Asahi Shimbun (Japan)

- **Feed URL:** `https://www.asahi.com/rss/asahi/newsheadlines.rdf`
- **Language:** Japanese (ja)
- **Publishing Region:** Japan / Asia
- **Content Provided:** Title, description, link, publication date
- **Attribution Requirements:**
  - Major Japanese newspaper
  - Original article link required
- **Limitations:**
  - Japanese-language headlines displayed untranslated
  - Native Japanese perspective

## Geographic Distribution

- **North America:** 2 sources (NY Times, CBC)
- **Europe:** 7 sources (BBC, DW, France 24, Guardian, Der Spiegel, swissinfo.ch, ABC Spain)
- **Middle East:** 1 source (Al Jazeera)
- **Asia-Pacific:** 5 sources (Japan Times, The Hindu, Channel NewsAsia, ABC Australia, Asahi Shimbun)

## Language Distribution

- **English:** 13 sources
- **Spanish:** 1 source (ABC Spain)
- **Japanese:** 1 source (Asahi Shimbun)

## Per-Publisher Balancing

To prevent single-publisher domination:
- Maximum 5 articles per publisher in visible feed
- Chronological sorting preserved within each publisher's quota
- Ensures diverse representation across all 15 sources

## Conservative Story Grouping

Articles are grouped into stories only when high confidence exists:
- **Normalized title similarity** > 70% (Jaccard similarity of word sets)
- **Published within 6 hours** of each other
- **Same canonical URL** (exact match)

When uncertain, articles remain separate. Each story shows "N sources" where N = number of grouped articles.

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
- **Request headers:** User-Agent identifies as "CrossLens/0.0.14-beta (Android; +https://crosslens.org)"
- **HTTPS only:** All feeds and original article URLs use HTTPS

## Feed State Indicators

The app displays the current feed state clearly:

- **Live Feed:** Just fetched from sources
- **Cached Feed:** Displaying locally cached data (offline or stale)
- **Demo Fallback:** All sources unavailable, showing mock fixtures
- **Last Updated:** Real timestamp of last successful fetch
- **Source Count:** Number of sources that responded successfully

## Known Limitations

1. **Conservative grouping only:** Articles are grouped conservatively; some matching stories may remain separate to avoid false positives
2. **Headline-level only:** RSS provides title and excerpt; full article opens in-app browser
3. **No paywalled content bypass:** Original reporting may require publisher subscription (especially NY Times, Guardian)
4. **Mostly English:** 13 of 15 sources are English; Spanish and Japanese headlines displayed untranslated with language tags
5. **No source metadata inference:** Source country is publishing location only, not a proxy for political stance or national viewpoint
6. **No editorial analysis:** Lens Gap, claims, and framing observations remain mock-only for v0.0.14

## Future Considerations

- **Improved clustering:** Machine learning or LLM-based semantic similarity for better cross-source matching
- **Multilingual variants:** DW, France 24, and Al Jazeera publish feeds in German, French, Spanish, and Arabic
- **Licensed API integration:** Architecture supports coexistence of RSS and licensed content APIs
- **Additional sources:** African, Latin American, and Eastern European coverage gaps
- **Publisher agreements:** New sources require individual legal review and terms verification

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

## Batch 3 Sources (5 Additional - October 2026)

### 24. Daily Maverick (South Africa)

- **Feed URL:** `https://www.dailymaverick.co.za/dmrss/`
- **Language:** English
- **Publishing Region:** South Africa / Africa
- **Source Type:** Online news publication
- **Attribution Requirements:** Source name, original article link required
- **Limitations:** Excerpt-only; full article at original URL
- **Added:** Batch 3 expansion (October 2026)
- **Geographic Gap Filled:** First African source

### 25. Euronews (France/Pan-European)

- **Feed URL:** `https://www.euronews.com/rss`
- **Language:** English
- **Publishing Region:** Pan-European (France-based)
- **Source Type:** International news channel
- **Attribution Requirements:** Source name, original article link required
- **Limitations:** English edition; multiple language variants available
- **Added:** Batch 3 expansion (October 2026)

### 26. RFI English (France)

- **Feed URL:** `https://www.rfi.fr/en/rss`
- **Language:** English
- **Publishing Region:** France / International
- **Source Type:** Public international broadcaster
- **Coverage:** International affairs, strong African coverage
- **Attribution Requirements:** Source name, original article link required
- **Limitations:** English service; French/Spanish/Portuguese variants available
- **Added:** Batch 3 expansion (October 2026)

### 27. South China Morning Post (Hong Kong)

- **Feed URL:** `https://www.scmp.com/rss/4/feed`
- **Language:** English
- **Publishing Region:** Hong Kong SAR
- **Source Type:** Daily newspaper (English edition)
- **Attribution Requirements:** Source name, original article link required
- **Limitations:** Subscription-based access for some content
- **Added:** Batch 3 expansion (October 2026) - Recovers Batch 1 disabled source with new feed URL
- **Note:** Previous feed URL (/rss/91/feed) had HTTPS redirect loop

### 28. The Moscow Times (Russia)

- **Feed URL:** `https://www.themoscowtimes.com/rss/news`
- **Language:** English
- **Publishing Region:** Russia / Eastern Europe
- **Source Type:** English-language news website
- **Attribution Requirements:** Source name, original article link required
- **Limitations:** Independent editorial stance
- **Added:** Batch 3 expansion (October 2026)
- **Geographic Gap Filled:** First Eastern European source

---

## Batch 3 Summary

**Total Active Sources:** 28  
**New Sources Added:** 5  
**Geographic Expansion:** Africa (1), Eastern Europe (1), Additional Europe (2), Asia-Pacific recovery (1)  
**Validation:** All 5 sources tested on Pixel 11 - 28/28 active, 0 failures  
**RSS Availability:** 50% success rate (5/10 candidates) due to industry-wide RSS discontinuation (2024-2026)

**Sources Rejected in Batch 3:**
- Jakarta Post (Indonesia) - RSS endpoint removed (404)
- Bangkok Post (Thailand) - RSS discontinued (404)
- Philippine Daily Inquirer (Philippines) - Bot protection (403)
- Folha de S.Paulo (Brazil) - No valid main feed
- Clarín (Argentina) - Access blocked (403)

**See:** `docs/BATCH3_VALIDATION_RESULTS.md` for technical validation evidence.

---

## Portfolio Statistics (28 Sources)

### Geographic Distribution
- Europe/UK: 10 sources (36%)
- Asia-Pacific: 9 sources (32%)
- North America: 3 sources (11%)
- Middle East: 2 sources (7%)
- Africa: 1 source (4%) ← NEW
- Eastern Europe: 1 source (4%) ← NEW
- Latin America: 0 sources (0%)

### Language Distribution
- English: 25 sources (89%)
- Spanish: 2 sources (7%)
- French: 1 source (4%)

### Source Types
- Public Broadcasters: 11 (BBC, CBC, ABC, DW, France 24, swissinfo, RFI, etc.)
- Daily Newspapers: 12 (NYT, Guardian, FT, SCMP, etc.)
- Digital News Sites: 3 (Daily Maverick, Moscow Times, Euronews)
- Wire Services: 1 (UPI)
- News Magazines: 1 (Der Spiegel International)

---

## Known Limitations

### Geographic Gaps
- **Latin America:** No working RSS from tested Brazilian/Argentine sources
- **Southeast Asia:** No working RSS from Indonesia, Thailand, Philippines
- **Sub-Saharan Africa:** Only 1 source (South Africa)
- **Middle East:** Limited to 2 sources (Al Jazeera, Arab News)

### Language Diversity
- Heavy English dominance (89%)
- Limited non-English sources (2 Spanish, 1 French)
- No Portuguese sources (Folha de S.Paulo rejected due to no valid feed)

### RSS Availability Crisis (2026)
Between 2024-2026, many publishers discontinued RSS feeds:
- Bot protection increasing (30% of rejections)
- Endpoint removal (40% of rejections)  
- API-only access (20% of rejections)
- Success rate: 50% for Batch 3 candidates

Public broadcasters and international news services more likely to maintain RSS. Regional commercial newspapers increasingly discontinuing RSS access.

---

## Content Use Policy

All sources in this list are configured for **link-and-excerpt** display only:
- Headlines displayed
- Brief excerpts shown
- Original article link always provided
- Full content requires visiting publisher URL
- Attribution always displayed
- No article text reproduction beyond excerpt

CrossLens does not claim comprehensive coverage, balanced perspective, or editorial neutrality. Sources are selected based on technical RSS availability, not ideology or bias assessment.

---

**Last Updated:** October 4, 2026  
**Next Review:** Batch 4 planning (pending RSS availability improvements)
