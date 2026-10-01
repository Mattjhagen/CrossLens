# Batch 2 Source Validation Results
**Date:** September 29, 2026  
**Branch:** feature/live-feed-v0.0.14-beta  
**Status:** ✅ VALIDATION COMPLETE

## Executive Summary

**Validated:** 3 sources ready to add (UPI, Financial Times, El País)  
**Rejected:** 7 sources due to technical/policy issues  
**New Total:** 23 active sources (up from 20)  
**Expected Impact:** Enable three-source clustering on major international events

## ✅ VALIDATED SOURCES (3)

### 1. UPI (United Press International)
**Feed URL:** `https://rss.upi.com/news/news.rss`  
**Publisher:** United Press International, Inc.  
**Country:** United States  
**Language:** English (en-US)

**Validation Results:**
```
HTTP/2 200
Content-Type: application/rss+xml
Format: RSS 2.0
HTTPS Links: ✅ Yes
Media RSS: ✅ Yes (media:content, media:thumbnail)
Recent Articles: ✅ Within hours
```

**Sample Headlines (Sept 29, 2026):**
- "Supreme Court allows 'third-country' deportations to continue" ✅ **OVERLAP VERIFIED** (matches France 24 + Arab News cluster)
- "Tropical Storm Rachel nears hurricane strength"
- "Man City found to have artificially boosted finances by more than £900mn"

**Coverage Scope:**
- US domestic news (HIGH)
- Major international events (MEDIUM-HIGH)
- Wire service style reporting

**Expected Overlap:** MEDIUM-HIGH for US news and major international events  
**Decision:** ✅ **ADD TO BATCH 2**

---

### 2. Financial Times
**Feed URL:** `https://www.ft.com/rss/home/international`  
**Publisher:** The Financial Times Ltd  
**Country:** United Kingdom  
**Language:** English (en-GB)

**Validation Results:**
```
HTTP/2 200 (via 301 redirect from https://www.ft.com/?format=rss)
Content-Type: application/rss+xml
Format: RSS 2.0
HTTPS Links: ✅ Yes
Media RSS: ✅ Yes (media:thumbnail)
Recent Articles: ✅ Within hours
```

**Sample Headlines (Sept 29, 2026):**
- "US 30-year Treasury yield hits highest since 2002"
- "Bond investors become oil traders as Iran war drives yields"
- "Trump praises tech bosses' 'tremendous self-regulation' of AI"
- "Pentagon awards Boeing $20bn contract for Navy stealth fighter jet"
- "Man City found to have artificially boosted finances by more than £900mn" ✅ **Potential overlap with UPI**

**Coverage Scope:**
- Business/financial news (VERY HIGH)
- International politics (HIGH)
- Technology/innovation (HIGH)
- UK domestic (MEDIUM)

**Expected Overlap:** HIGH for major political/economic events  
**Decision:** ✅ **ADD TO BATCH 2**

---

### 3. El País
**Feed URL:** `https://feeds.elpais.com/mrss-s/pages/ep/site/elpais.com/portada`  
**Publisher:** PRISA Media  
**Country:** Spain  
**Language:** Spanish (es)

**Validation Results:**
```
HTTP/2 200
Content-Type: application/rss+xml
Format: RSS 2.0
HTTPS Links: ✅ Yes
Media RSS: ✅ Yes
Recent Articles: ✅ Within hours
```

**Sample Headlines (Sept 29, 2026):**
- "El Gobierno aprueba la protección frente a los desahucios hasta 2030..." (Spain domestic - housing policy)
- "El PP rechaza los decretos de vivienda..." (Spain politics)

**Coverage Scope:**
- Spanish domestic news (VERY HIGH)
- Latin American coverage (HIGH)
- European affairs (MEDIUM-HIGH)
- Major international events (MEDIUM)

**Language Diversity:** ✅ Adds Spanish-language source (second after ABC Spain)  
**Expected Overlap:** MEDIUM-HIGH for major international events, Spanish/European perspective  
**Decision:** ✅ **ADD TO BATCH 2**

## ❌ REJECTED SOURCES (7)

### 1. NHK World (Japan)
**Feed URL:** `https://www3.nhk.or.jp/rss/news/cat0.xml`  
**Status:** HTTP 200 but **HTTP-only article links**

**Rejection Reason:**
```xml
<link>http://www3.nhk.or.jp/news/html/20260808/k10015199841000.html</link>
```
- Article URLs use HTTP protocol (not HTTPS)
- Violates app security policy (HTTPS-only sources)
- Articles dated August 8 (potentially stale feed)

**Decision:** ❌ REJECT - Security policy violation

---

### 2. The Independent (UK)
**Feed URL:** `https://www.independent.co.uk/rss`  
**Status:** HTTP 200 but **returns binary gzip data**

**Rejection Reason:**
- Response is compressed binary data, not parseable XML
- No RSS structure detected
- Cannot extract articles

**Decision:** ❌ REJECT - Format incompatible

---

### 3. TRT World (Turkey)
**Feed URL:** `https://www.trtworld.com/feed/rss/news`  
**Status:** HTTP 404 Not Found

**Rejection Reason:**
- Feed endpoint does not exist
- No alternative URLs found

**Decision:** ❌ REJECT - Feed unavailable

---

### 4. Xinhua News (China)
**Feed URL:** `http://www.xinhuanet.com/english/rss/worldrss.xml`  
**Status:** HTTP 200 but **HTTP-only article links**

**Rejection Reason:**
- Feed URL is HTTP (not HTTPS)
- Article links are HTTP-only
- Violates security policy

**Decision:** ❌ REJECT - Security policy violation

---

### 5. Haaretz (Israel)
**Feed URL:** `https://www.haaretz.com/cmlink/1.628331`  
**Status:** HTTP 301 redirect (destination unclear)

**Rejection Reason:**
- Redirect destination not resolved
- Feed structure unknown

**Decision:** ❌ SKIP - Unable to validate

---

### 6. Japan Times (topstories feed)
**Feed URL:** `https://www.japantimes.co.jp/feed/topstories`  
**Status:** HTTP 301 redirect

**Rejection Reason:**
- Already have Japan Times main feed configured
- Redundant source (same publisher)

**Decision:** ❌ SKIP - Duplicate publisher

---

### 7. Reuters
**Status:** HTTP 401 Unauthorized (DataDome bot protection)

**Rejection Reason:**
- Bot protection blocks automated access
- No public RSS available

**Decision:** ❌ REJECT - Previously rejected in wire service research

## Summary Statistics

| Category | Count |
|----------|-------|
| **Candidates Tested** | 11 |
| **Validated** | 3 |
| **Rejected** | 7 |
| **Skipped** | 1 |
| **Success Rate** | 27% |

## Geographic & Language Coverage

**New Geographic Coverage:**
- ✅ Adds second Spanish source (El País joins ABC Spain)
- ✅ Adds business/financial perspective (Financial Times)
- ✅ Adds wire service (UPI)

**Language Diversity:**
- English: 21 sources (91%)
- Spanish: 2 sources (9%) - ABC Spain, El País
- French: 1 source (Le Monde)

**Regional Balance (23 total):**
- Europe/UK: 9 sources (39%) - BBC, Guardian, Spiegel, DW, France24, swissinfo, Irish Times, Le Monde, **FT**
- North America: 3 sources (13%) - NYT, WashPost, CBC, **UPI**
- Asia-Pacific: 5 sources (22%) - ABC AU, Japan Times, Hindu, Straits Times, Channel NewsAsia
- Middle East: 2 sources (9%) - Al Jazeera, Arab News
- International: 4 sources (17%) - **FT**, **UPI**, **El País**, ABC Spain

## Expected Clustering Impact

**Before Batch 2:** 20 sources  
**After Batch 2:** 23 sources (+15%)

**Expected Improvements:**
- **US domestic news:** 3-4 sources (UPI, NYT, WashPost, possibly FT)
- **Major international events:** 3-5 sources (varied publishers)
- **Business/financial news:** 2-3 sources (FT + existing business coverage)
- **Spanish/Latin America:** 2 sources (El País, ABC Spain)

**Three-Source Cluster Scenarios:**

**Scenario 1: Major US political event**
- UPI, NYT, WashPost + potentially BBC, Guardian, FT

**Scenario 2: International economic/political event**
- FT, NYT, WashPost, BBC, Guardian, DW, France24

**Scenario 3: European event**
- FT, BBC, Guardian, DW, France24, Irish Times, Le Monde, El País

## Validation Methodology

**Technical Checks:**
1. ✅ HTTP 200 response
2. ✅ Valid RSS 2.0 XML format
3. ✅ HTTPS article links (security requirement)
4. ✅ Recent articles (within 24-48 hours)
5. ✅ Parseable pubDate fields
6. ✅ Descriptive headlines and excerpts

**Content Checks:**
1. ✅ Official publisher RSS feed
2. ✅ International or major domestic coverage
3. ✅ Independent publisher (no duplicates)
4. ✅ Suitable for event clustering

**Policy Checks:**
1. ✅ HTTPS-only sources
2. ✅ Link-and-excerpt use acceptable
3. ✅ No authentication required
4. ✅ No bot protection blocking access

## Implementation Plan

**Step 1:** Add 3 validated sources to `RssSourceAdapter.kt`
- UPI: `upi-rss`
- Financial Times: `ft-rss`  
- El País: `elpais-rss`

**Step 2:** Update source count documentation
- Active: 23 sources (was 20)
- Disabled: 3 sources (Korea Herald, Asahi, SCMP)
- Total configured: 26 sources

**Step 3:** Run health diagnostic
- Verify all 3 new sources fetch successfully
- Check for errors or failures
- Measure success rate

**Step 4:** Monitor for three-source clusters
- Refresh feed and check clustering results
- Look for events covered by 3+ distinct publishers
- Verify "Read Across Coverage" feature activation

## Risk Assessment

**Low Risk:**
- All 3 sources validated with cURL tests
- All use standard RSS 2.0 format
- All have HTTPS links
- All have recent articles

**Medium Risk:**
- Financial Times has paywall for full articles (but RSS excerpts are public)
- El País is Spanish-language (needs translation for English readers)

**Mitigations:**
- RSS excerpt use is standard and acceptable
- Spanish sources add language diversity (CrossLens principle)
- Link to original article preserved for readers

## Next Steps

1. ⏳ Add 3 sources to RssSourceAdapter
2. ⏳ Build and test compilation
3. ⏳ Run source health diagnostic
4. ⏳ Verify feed refresh with new sources
5. ⏳ Monitor for three-source event clusters
6. ⏳ Test "Read Across Coverage" feature
7. ⏳ Document results in expansion report

## Conclusion

✅ **Batch 2 validation COMPLETE**  
✅ **3 high-quality sources validated**  
✅ **Ready to implement**  
✅ **Expected to enable three-source clustering**

**Status:** Moving to Task 8 (Implementation)
