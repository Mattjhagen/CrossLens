# Batch 3 Source Candidate Research

**Date:** 2026-10-04  
**Branch:** feature/source-portfolio-v2  
**Target:** Select 6-10 candidates for technical validation  
**Priority:** Fill geographic/language gaps identified in current portfolio audit

## Selection Criteria

1. **Geographic Priority:** Africa, Southeast Asia, Latin America, Eastern Europe
2. **Technical Requirements:** HTTPS RSS 2.0 feed, recent articles, parseable format
3. **Event Clustering Potential:** Likely to cover major international events
4. **Language Diversity:** Prioritize non-English where available
5. **No Bias/Ideology Labels:** Selection based on RSS availability and documented publisher location only

## Batch 3 Candidates (10 Selected)

### Priority 1: Africa

#### Candidate 1: Daily Maverick (South Africa)
- **Publisher:** Daily Maverick (Pty) Ltd
- **Documented Location:** South Africa
- **Language:** English
- **Feed URL (to validate):** `https://www.dailymaverick.co.za/dmrss/`
- **Source Type:** Online news publication
- **Expected Overlap:** Moderate for major international events, high for African regional
- **Rationale:** Fills Africa gap, English-language, established digital news outlet
- **Validation Priority:** HIGH

#### Candidate 2: News24 (South Africa)
- **Publisher:** Media24 (Naspers)
- **Documented Location:** South Africa
- **Language:** English
- **Feed URL (to validate):** `https://feeds.news24.com/articles/news24/TopStories/rss`
- **Source Type:** News website
- **Expected Overlap:** Moderate for major international events
- **Rationale:** Alternative South African source, commercial news site
- **Validation Priority:** MEDIUM

### Priority 2: Southeast Asia Expansion

#### Candidate 3: The Jakarta Post (Indonesia)
- **Publisher:** PT Niskala Media Tenggara
- **Documented Location:** Indonesia
- **Language:** English
- **Feed URL (to validate):** `https://www.thejakartapost.com/rss`
- **Source Type:** English-language daily newspaper
- **Expected Overlap:** Moderate for major international events, high for Southeast Asia
- **Rationale:** Fills Indonesia gap, largest economy in Southeast Asia, English RSS available
- **Validation Priority:** HIGH

#### Candidate 4: Bangkok Post (Thailand)
- **Publisher:** Post Publishing PCL
- **Documented Location:** Thailand
- **Language:** English
- **Feed URL (to validate):** `https://www.bangkokpost.com/rss/data/news.xml`
- **Source Type:** English-language daily newspaper
- **Expected Overlap:** Moderate for major international events, high for Southeast Asia
- **Rationale:** Fills Thailand gap, major regional economy
- **Validation Priority:** HIGH

#### Candidate 5: Philippine Daily Inquirer (Philippines)
- **Publisher:** Inquirer Interactive, Inc.
- **Documented Location:** Philippines
- **Language:** English
- **Feed URL (to validate):** `https://newsinfo.inquirer.net/feed`
- **Source Type:** Daily newspaper (online edition)
- **Expected Overlap:** Moderate for major international events
- **Rationale:** Fills Philippines gap, English-language market
- **Validation Priority:** MEDIUM

### Priority 3: Latin America

#### Candidate 6: Folha de S.Paulo (Brazil)
- **Publisher:** Grupo Folha
- **Documented Location:** Brazil
- **Language:** Portuguese (pt-BR)
- **Feed URL (to validate):** `https://www1.folha.uol.com.br/rss/`
- **Source Type:** Daily newspaper
- **Expected Overlap:** Moderate for major international events, high for Latin America
- **Rationale:** Fills Latin America gap, adds Portuguese language, largest Latin American country
- **Validation Priority:** HIGH

#### Candidate 7: Clarín (Argentina)
- **Publisher:** Grupo Clarín
- **Documented Location:** Argentina
- **Language:** Spanish (es-AR)
- **Feed URL (to validate):** `https://www.clarin.com/rss/lo-ultimo/`
- **Source Type:** Daily newspaper
- **Expected Overlap:** Moderate for major international events, high for Latin America
- **Rationale:** Spanish-language from Latin America (distinct from Spain sources)
- **Validation Priority:** MEDIUM

### Priority 4: Additional Middle East

#### Candidate 8: Haaretz (Israel)
- **Publisher:** Haaretz Group
- **Documented Location:** Israel
- **Language:** English
- **Feed URL (to validate):** `https://www.haaretz.com/cmlink/1.628331` or `https://www.haaretz.com/srv/RSSXML`
- **Source Type:** Daily newspaper (English edition)
- **Expected Overlap:** High for Middle East events, moderate for major international
- **Rationale:** Additional Middle East perspective, region of significant international coverage
- **Validation Priority:** MEDIUM
- **Note:** Batch 2 showed HTTP 301 redirect, needs re-validation

#### Candidate 9: The National (UAE)
- **Publisher:** Abu Dhabi Media
- **Documented Location:** United Arab Emirates
- **Language:** English
- **Feed URL (to validate):** `https://www.thenationalnews.com/rss.xml`
- **Source Type:** Daily newspaper (English edition)
- **Expected Overlap:** High for Middle East events, moderate for international
- **Rationale:** Gulf region perspective, English-language
- **Validation Priority:** MEDIUM

### Priority 5: Eastern Europe

#### Candidate 10: The Moscow Times (Russia)
- **Publisher:** Independent media outlet
- **Documented Location:** Russia (operates independently)
- **Language:** English
- **Feed URL (to validate):** `https://www.themoscowtimes.com/rss/news`
- **Source Type:** English-language news website
- **Expected Overlap:** Moderate-high for major international events, high for Eastern Europe
- **Rationale:** Fills Eastern Europe gap, English RSS available
- **Validation Priority:** MEDIUM
- **Note:** Check editorial independence status

## Alternate Candidates (Backup)

### East Asia Alternative (if primary candidates fail)

**Taiwan News (Taiwan)**
- **Feed URL:** `https://www.taiwannews.com.tw/rss/`
- **Language:** English
- **Rationale:** Alternative East Asian source, technology/trade hub

**Yonhap News Agency (South Korea)**
- **Feed URL:** `https://en.yna.co.kr/RSS/` (to validate)
- **Language:** English
- **Rationale:** Wire service, alternative to disabled Korea Herald

### Additional Southeast Asia

**Vietnam News (Vietnam)**
- **Feed URL:** `https://vietnamnews.vn/rss/` (to validate)
- **Language:** English
- **Rationale:** Additional Southeast Asia coverage

### Additional Africa

**The Star (Kenya)**
- **Feed URL:** `https://www.the-star.co.ke/feed/` (to validate)
- **Language:** English
- **Rationale:** East African perspective

## Geographic Distribution After Batch 3 (if all 10 enabled)

**Target distribution with 33 active sources:**

- **Europe/UK:** 8 sources (24%)
- **North America:** 3 sources (9%)
- **Asia-Pacific:** 11 sources (33%) ← +3 (Jakarta Post, Bangkok Post, Philippine Inquirer)
- **Middle East:** 4 sources (12%) ← +2 (Haaretz, The National)
- **Africa:** 1-2 sources (3-6%) ← +1-2 (Daily Maverick, News24)
- **Latin America:** 2 sources (6%) ← +2 (Folha, Clarín)
- **Eastern Europe:** 1 source (3%) ← +1 (Moscow Times)

## Language Distribution After Batch 3 (if all 10 enabled)

- **English:** 27-28 sources (82-85%)
- **Spanish:** 3 sources (9%) ← +1 (Clarín)
- **French:** 1 source (3%)
- **Portuguese:** 1 source (3%) ← +1 NEW (Folha)

## Technical Validation Plan

For each candidate, validate in order of priority:

1. **HTTPS Fetch Test:**
   ```bash
   curl -I -L "https://..."
   ```
   - Expect: HTTP 200, Content-Type: application/rss+xml or text/xml

2. **RSS Format Test:**
   ```bash
   curl -L "https://..." | head -50
   ```
   - Expect: `<rss version="2.0">` (not `<rdf:RDF>`, not `<feed>`)
   - Expect: `<channel>`, `<item>`, `<title>`, `<link>`, `<pubDate>`

3. **HTTPS Link Test:**
   - Extract sample `<link>` element
   - Verify starts with `https://` (not `http://`)

4. **Freshness Test:**
   - Extract sample `<pubDate>`
   - Verify within last 24-48 hours

5. **Parser Compatibility Test:**
   - Test with RssParser.parse()
   - Verify no parse errors
   - Verify article records created

6. **Source Metadata Test:**
   - Verify sourceId can be set
   - Verify languageTag can be inferred
   - Verify compatible with SourceHealthMonitor

## Decision Criteria

**ENABLE if:**
- ✅ All 6 validation tests pass
- ✅ Fills identified geographic/language gap
- ✅ Has event clustering potential (covers major international events)

**EXCLUDE if:**
- ❌ HTTP-only feed or article links (security policy violation)
- ❌ Non-RSS 2.0 format (RDF, Atom without conversion)
- ❌ Stale feed (no articles in 7+ days)
- ❌ Parse errors (malformed XML, incompatible structure)
- ❌ 404/401/403 errors
- ❌ Redirect loop or access denied

## Expected Outcome

- **Best Case:** 8-10 sources enabled (target: 31-33 active sources)
- **Likely Case:** 6-8 sources enabled (target: 29-31 active sources)
- **Minimum Case:** 4-6 sources enabled (target: 27-29 active sources)

## Documentation Requirements

For each candidate, document:
- Feed URL tested
- HTTP response status
- RSS format validation result
- Sample headline (proof of freshness)
- HTTPS link verification
- Parser compatibility test result
- **Decision:** ENABLED or EXCLUDED
- **Reason:** Exact technical evidence for exclusion

---

**Next Step:** Execute technical validation tests for all 10 candidates and document results in BATCH3_VALIDATION_RESULTS.md.
