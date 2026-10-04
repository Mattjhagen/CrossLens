# Batch 3 Source Validation Results

**Date:** October 4, 2026  
**Branch:** feature/source-portfolio-v2  
**Status:** ✅ VALIDATION COMPLETE

## Executive Summary

**Validated:** 5 sources ready to add  
**Rejected:** 5 sources due to technical/availability issues  
**New Total:** 28 active sources (up from 23)  
**Geographic Additions:** Africa (1), Eastern Europe (1), Additional Europe (2), East Asia recovery (1)

## RSS Availability Challenge (2026)

Testing revealed that many publishers have discontinued or restricted RSS feeds since 2024-2025:
- **Bot Protection:** Many sites return 403 errors for automated access
- **Feed Removal:** Several documented RSS URLs now return 404
- **API-Only:** Some publishers moved to paid API-only access
- **URL Changes:** Feed endpoints changed without redirects

This significantly limits Batch 3 expansion compared to initial research expectations.

## ✅ VALIDATED SOURCES (5)

### 1. Daily Maverick (South Africa)
**Feed URL:** `https://www.dailymaverick.co.za/dmrss/`  
**Publisher:** Daily Maverick (Pty) Ltd  
**Country:** South Africa  
**Language:** English

**Validation Results:**
```
HTTP/2 200
Content-Type: application/xml
Format: RSS 2.0 ✅
HTTPS Links: Yes ✅
Fresh Articles: Yes ✅ (Oct 4, 2026, 15:11 GMT)
```

**Sample Headlines:**
- "SA's GBV crisis can't be solved in court when it starts in the classroom"
- "In defiance of chaos — why your choice in this election matters"

**Coverage Scope:** South African domestic (HIGH), African regional (MEDIUM), Major international events (MEDIUM)

**Decision:** ✅ **ADD TO BATCH 3**  
**Rationale:** Fills Africa gap, English-language, verified working RSS with fresh content

---

### 2. Euronews
**Feed URL:** `https://www.euronews.com/rss`  
**Publisher:** Euronews  
**Country:** France (pan-European service)  
**Language:** English

**Validation Results:**
```
HTTP/2 200
Format: RSS 2.0 ✅
HTTPS Links: Yes ✅
Fresh Articles: Yes ✅ (Oct 4, 2026, 18:00 +0200)
```

**Coverage Scope:** European affairs (VERY HIGH), Major international events (HIGH)

**Decision:** ✅ **ADD TO BATCH 3**  
**Rationale:** Adds pan-European perspective, high event overlap potential, multilingual service

---

### 3. RFI English (Radio France Internationale)
**Feed URL:** `https://www.rfi.fr/en/rss`  
**Publisher:** Radio France Internationale  
**Country:** France  
**Language:** English

**Validation Results:**
```
HTTP/2 200
Format: RSS 2.0 ✅
HTTPS Links: Yes ✅
Fresh Articles: Yes ✅ (Oct 4, 2026, 15:17 GMT)
```

**Coverage Scope:** French/Francophone affairs (HIGH), African coverage (HIGH), Major international events (HIGH)

**Decision:** ✅ **ADD TO BATCH 3**  
**Rationale:** International public broadcaster, strong African coverage (complements Daily Maverick), English service

---

### 4. South China Morning Post (Hong Kong)
**Feed URL:** `https://www.scmp.com/rss/4/feed`  
**Publisher:** South China Morning Post Publishers Ltd  
**Country:** Hong Kong SAR  
**Language:** English

**Validation Results:**
```
HTTP/2 200
Format: RSS 2.0 ✅
HTTPS Links: Yes ✅
Fresh Articles: Yes ✅ (Oct 4, 2026, 14:00 UTC)
```

**Coverage Scope:** Hong Kong/China affairs (VERY HIGH), Asian regional (HIGH), Major international events (HIGH)

**Decision:** ✅ **ADD TO BATCH 3**  
**Rationale:** Recovers disabled Batch 1 source (new URL), fills East Asia gap, major regional outlet

**Technical Note:** Previous URL (`http://www.scmp.com/rss/91/feed`) had HTTPS redirect loop. New feed ID `/rss/4/feed` works correctly with HTTPS.

---

### 5. The Moscow Times
**Feed URL:** `https://www.themoscowtimes.com/rss/news`  
**Publisher:** The Moscow Times  
**Country:** Russia  
**Language:** English

**Validation Results:**
```
HTTP/2 200
Content-Type: text/xml;charset=UTF-8
Format: RSS 2.0 ✅
HTTPS Links: Yes ✅
Fresh Articles: Yes ✅ (Oct 4, 2026, 18:01 +0300)
```

**Sample Headlines:**
- "Russia Says It Hit Kyiv Bridge, 2 Vessels in Black Sea"
- "OPEC+ Agrees to Keep November Oil Output Targets Steady"
- "Germany's Merz Arrives in Kyiv to the Sound of Sirens and Explosions"

**Coverage Scope:** Russian affairs (VERY HIGH), Eastern European affairs (HIGH), Major international events (HIGH)

**Decision:** ✅ **ADD TO BATCH 3**  
**Rationale:** Fills Eastern Europe gap, English-language independent news outlet, high event overlap potential

**Editorial Note:** Independent media outlet operating from Russia with editorial independence.

---

## ❌ REJECTED SOURCES (5)

### 1. The Jakarta Post (Indonesia)
**Feed URL:** `https://www.thejakartapost.com/rss`  
**Status:** HTTP 404 Not Found

**Rejection Reason:**
- RSS feed endpoint removed or relocated
- Returns HTML 404 page instead of RSS
- No alternative URL found

**Decision:** ❌ REJECT - Feed unavailable

---

### 2. Bangkok Post (Thailand)
**Feed URL:** `https://www.bangkokpost.com/rss/data/news.xml`  
**Status:** HTTP 404 Not Found

**Rejection Reason:**
- RSS endpoint returns 404
- Alternative `/rss` redirects to HTML paywall page
- Feed appears discontinued

**Decision:** ❌ REJECT - Feed unavailable

---

### 3. Philippine Daily Inquirer (Philippines)
**Feed URL:** `https://newsinfo.inquirer.net/feed`  
**Status:** HTTP 403 Forbidden (Bot Protection)

**Rejection Reason:**
- Cloudflare bot protection blocks automated access
- RSS format detected behind protection wall
- Cannot reliably fetch in production environment

**Decision:** ❌ REJECT - Bot protection prevents reliable access

---

### 4. Folha de S.Paulo (Brazil)
**Feed URL:** `https://www1.folha.uol.com.br/rss/`  
**Status:** HTTP 200 but returns HTML directory listing

**Rejection Reason:**
- Returns HTML page listing RSS categories, not actual RSS feed
- No single main RSS feed identified
- Alternative URLs tested returned 404

**Decision:** ❌ REJECT - No valid main feed found

---

### 5. Clarín (Argentina)
**Feed URL:** `https://www.clarin.com/rss/lo-ultimo/`  
**Status:** HTTP 403 Forbidden

**Rejection Reason:**
- Bot protection or access restrictions
- Returns HTML error page instead of RSS

**Decision:** ❌ REJECT - Access blocked

---

## Additional Candidates Tested

### Tested and Rejected

**Haaretz (Israel)**
- Feed URL: `https://www.haaretz.com/srv/RSSXML`
- Status: HTTP 404 Not Found
- Decision: ❌ REJECT

**The National (UAE)**
- Feed URL: `https://www.thenationalnews.com/rss.xml`
- Status: HTTP 404 Not Found
- Decision: ❌ REJECT

**News24 (South Africa - alternative)**
- Feed URL: `https://feeds.news24.com/articles/news24/TopStories/rss`
- Status: HTTP 403 Forbidden
- Decision: ❌ REJECT

**Vietnam News**
- Feed URL: `https://vietnamnews.vn/rss/home.rss`
- Status: HTTP 200 but empty/invalid content
- Decision: ❌ REJECT

**PBS NewsHour (US)**
- Feed URL: `https://www.pbs.org/newshour/feeds/rss/headlines`
- Status: HTTP 200 but complex feed structure requiring custom parser
- Decision: ❌ SKIP - Would require parser changes

**NPR World (US)**
- Feed URL: `https://feeds.npr.org/1004/rss.xml`
- Status: HTTP 200 but complex feed structure requiring custom parser
- Decision: ❌ SKIP - Would require parser changes

---

## Summary Statistics

### Sources by Status
- **Validated and Added:** 5 sources
- **Rejected (Technical Failure):** 5 sources
- **Skipped (Parser Incompatible):** 2 sources
- **Total Candidates Tested:** 12+

### New Geographic Distribution (28 total sources)

**Before Batch 3 (23 sources):**
- Europe/UK: 8 (35%)
- North America: 3 (13%)
- Asia-Pacific: 8 (35%)
- Middle East: 2 (9%)
- Africa: 0 (0%)
- Eastern Europe: 0 (0%)

**After Batch 3 (28 sources):**
- Europe/UK: 10 (36%) ← +2 (Euronews, RFI)
- North America: 3 (11%)
- Asia-Pacific: 9 (32%) ← +1 (SCMP recovered)
- Middle East: 2 (7%)
- Africa: 1 (4%) ← +1 NEW (Daily Maverick)
- Eastern Europe: 1 (4%) ← +1 NEW (Moscow Times)
- Latin America: 0 (0%) ← No working feeds found

### New Language Distribution (28 total sources)

- English: 25 sources (89%)
- Spanish: 2 sources (7%)
- French: 1 source (4%)
- Portuguese: 0 sources (0%) ← Folha rejected

### Expected Event Clustering Impact

With 28 sources:
- **Major breaking news:** 4-6 sources within 24 hours (improved from 3-5)
- **Scheduled events:** 5-8 sources within 24 hours (improved from 4-8)
- **Regional events:** 2-5 sources within 48 hours (improved from 2-4)
- **African regional:** 2-3 sources (NEW: Daily Maverick + RFI Africa coverage)
- **Eastern European:** 2-3 sources (NEW: Moscow Times + existing European sources)

---

## Technical Validation Evidence

### Validation Methodology

For each candidate, executed:

```bash
# 1. HTTPS Fetch Test
curl -I -L "https://..."

# 2. RSS Format Test
curl -L "https://..." | head -50 | grep "<rss"

# 3. HTTPS Link Test
curl -L "https://..." | grep "<link>https://"

# 4. Freshness Test
curl -L "https://..." | grep "<pubDate>"

# 5. Sample Content Extraction
curl -L "https://..." | grep "<title>"
```

### Validation Results Summary

| Source | HTTPS | RSS 2.0 | HTTPS Links | Fresh | Decision |
|--------|-------|---------|-------------|-------|----------|
| Daily Maverick | ✅ | ✅ | ✅ | ✅ | ENABLED |
| Euronews | ✅ | ✅ | ✅ | ✅ | ENABLED |
| RFI English | ✅ | ✅ | ✅ | ✅ | ENABLED |
| SCMP | ✅ | ✅ | ✅ | ✅ | ENABLED |
| Moscow Times | ✅ | ✅ | ✅ | ✅ | ENABLED |
| Jakarta Post | ✅ | ❌ 404 | N/A | N/A | EXCLUDED |
| Bangkok Post | ✅ | ❌ 404 | N/A | N/A | EXCLUDED |
| Inquirer | ✅ | ❌ 403 | N/A | N/A | EXCLUDED |
| Folha | ✅ | ❌ HTML | N/A | N/A | EXCLUDED |
| Clarín | ✅ | ❌ 403 | N/A | N/A | EXCLUDED |

---

## Implementation Requirements

### RssSourceAdapter Updates

Add to `createApprovedSources()`:

```kotlin
// Batch 3 Expansion - Geographic diversification (5 active)
RssSourceAdapter(
    sourceId = "dailymaverick-rss",
    sourceName = "Daily Maverick",
    feedUrl = "https://www.dailymaverick.co.za/dmrss/",
    httpClient = httpClient,
    healthMonitor = healthMonitor
),
RssSourceAdapter(
    sourceId = "euronews-rss",
    sourceName = "Euronews",
    feedUrl = "https://www.euronews.com/rss",
    httpClient = httpClient,
    healthMonitor = healthMonitor
),
RssSourceAdapter(
    sourceId = "rfi-rss",
    sourceName = "RFI English",
    feedUrl = "https://www.rfi.fr/en/rss",
    httpClient = httpClient,
    healthMonitor = healthMonitor
),
RssSourceAdapter(
    sourceId = "scmp-rss",
    sourceName = "South China Morning Post",
    feedUrl = "https://www.scmp.com/rss/4/feed",
    httpClient = httpClient,
    healthMonitor = healthMonitor
),
RssSourceAdapter(
    sourceId = "themoscowtimes-rss",
    sourceName = "The Moscow Times",
    feedUrl = "https://www.themoscowtimes.com/rss/news",
    httpClient = httpClient,
    healthMonitor = healthMonitor
)
```

### Language Inference Updates

Add to `inferLanguageFromSourceId()`:

```kotlin
sourceId.contains("dailymaverick") -> "en"  // South Africa
sourceId.contains("euronews") -> "en"  // Pan-European English
sourceId.contains("rfi") -> "en"  // France (English service)
sourceId.contains("scmp") -> "en"  // Hong Kong
sourceId.contains("themoscowtimes") -> "en"  // Russia (English)
```

### Source Count Update

Update comment in RssSourceAdapter.kt:
```kotlin
/**
 * Create the approved RSS source adapters with global coverage and health monitoring.
 * Currently: 28 active sources (5 disabled: Asahi Shimbun, Korea Herald, original SCMP URL,
 * Jakarta Post, Bangkok Post, Philippine Inquirer, Folha, Clarín).
 * Batch 3 adds: Daily Maverick, Euronews, RFI English, SCMP (new URL), Moscow Times.
 * See docs/BATCH3_VALIDATION_RESULTS.md.
 */
```

---

## Known Limitations

1. **Latin America Gap Remains:** No working RSS feeds found for Brazilian or Argentine sources tested
2. **Southeast Asia Limited:** No working feeds for Indonesia, Thailand, Philippines (all rejected)
3. **Bot Protection Trend:** Increasing number of publishers blocking automated RSS access
4. **Feed Stability:** RSS availability declining industry-wide (2024-2026)
5. **Language Diversity:** Still heavily English-dominant (89%), limited non-English sources

---

## Next Steps

1. ✅ Add 5 validated sources to RssSourceAdapter
2. ✅ Update language inference logic
3. ✅ Add unit tests for new sources
4. Build and install debug APK on Pixel 11
5. Run live ingestion and verify all 28 sources fetch successfully
6. Audit multi-publisher event cluster containing Batch 3 source
7. Capture device validation screenshots
8. Run full test suite
9. Document final results and commit to feature/source-portfolio-v2

---

**Validation Complete:** 5 sources enabled, 28 total active sources achieved.
