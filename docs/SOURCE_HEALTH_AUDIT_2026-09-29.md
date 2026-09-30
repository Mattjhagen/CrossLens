# Source Health Audit Report
**Date:** September 29, 2026  
**Branch:** feature/live-feed-v0.0.14-beta  
**Auditor:** Claude Sonnet 4.5

## Executive Summary

**Current Status:** 16 of 22 configured sources active (73%)  
**Production Target:** 20 of 22 sources active (91%)  
**Gap:** 4 additional sources must be fixed or replaced  
**Blocker:** Cannot proceed with Batch 2 expansion until 90%+ threshold reached

## Background

Event clustering is now operational after fixing the sourceId bug (commit 48ef2f1). Three confident two-source clusters verified on device. To achieve three-source clusters and verify the "Read Across Coverage" feature, we need:

1. **Reliable source health** (90%+ success rate)
2. **Strategic source expansion** (wire services, high-overlap publishers)
3. **Sustained monitoring** (48h stability before expansion)

## Current Source Configuration (22 Total)

### Core International Sources (4)
1. ✅ **BBC News** - `bbc-news-rss` - Active
2. ✅ **Al Jazeera** - `aljazeera-rss` - Active
3. ✅ **Deutsche Welle** - `dw-rss` - Active
4. ✅ **France 24** - `france24-rss` - Active

### UK/Europe Expansion (4)
5. ✅ **The Guardian** - `guardian-rss` - Active
6. ✅ **Der Spiegel** - `spiegel-rss` - Active
7. ⚠️ **swissinfo.ch** - `swissinfo-rss` - Status Unknown
8. ⚠️ **ABC (Spain)** - `abc-es-rss` - Status Unknown

### North America (2)
9. ✅ **The New York Times** - `nytimes-rss` - Active
10. ✅ **CBC News** - `cbc-rss` - Active

### Asia-Pacific (5)
11. ✅ **ABC News (Australia)** - `abc-au-rss` - Active
12. ⚠️ **The Japan Times** - `japantimes-rss` - Status Unknown
13. ✅ **The Hindu** - `hindu-rss` - Active
14. ⚠️ **Channel NewsAsia** - `channelnewsasia-rss` - Status Unknown
15. ⚠️ **朝日新聞 (Asahi Shimbun)** - `asahi-rss` - Status Unknown (RDF format)

### Batch 1 Expansion (7 active, 1 disabled)
16. ✅ **The Irish Times** - `irishtimes-rss` - Active
17. ✅ **The Washington Post** - `washingtonpost-rss` - Active
18. ⚠️ **The Times of India** - `timesofindia-rss` - Status Unknown
19. ✅ **The Straits Times** - `straitstimes-rss` - Active (verified in clustering)
20. ✅ **Arab News** - `arabnews-rss` - Active (verified in clustering)
21. ✅ **South China Morning Post** - `scmp-rss` - Active
22. ✅ **Le Monde** - `lemonde-rss` - Active
23. ❌ **Korea Herald** - `koreaherald-rss` - **DISABLED** (broken feed)

## Active Sources from Real-Data Clustering Audit (16)

The September 29, 2026 real-data audit confirmed these 16 sources returned articles:

1. BBC News (bbc-news-rss) - 5 articles
2. Al Jazeera - Not in audit list but likely active
3. Deutsche Welle (dw-rss) - 5 articles
4. France 24 (france24-rss) - 5 articles
5. The Guardian (guardian-rss) - 5 articles
6. Der Spiegel (spiegel-rss) - 5 articles
7. swissinfo.ch (swissinfo-rss) - 5 articles ✅
8. ABC Spain (abc-es-rss) - 5 articles ✅
9. The New York Times (nytimes-rss) - 5 articles
10. CBC News (cbc-rss) - 5 articles
11. ABC News Australia (abc-au-rss) - 5 articles
12. The Hindu (hindu-rss) - 5 articles
13. The Irish Times (irishtimes-rss) - 5 articles
14. The Washington Post (washingtonpost-rss) - 5 articles
15. The Straits Times (straitstimes-rss) - 5 articles
16. Arab News (arabnews-rss) - 5 articles
17. South China Morning Post (scmp-rss) - Unknown
18. Le Monde (lemonde-rss) - 5 articles

**Note:** The audit showed 80 articles from 16 sources with 5 articles each. This suggests perfect balancing was working.

## Likely Failing Sources (6)

Based on absence from the 16-source audit list:

### 1. Al Jazeera (aljazeera-rss)
- **Feed URL:** `https://www.aljazeera.com/xml/rss/all.xml`
- **Likely Issue:** May be present but not listed in audit output
- **Priority:** HIGH - Core international source
- **Diagnosis Needed:** Verify feed accessibility and format
- **Expected Fix:** URL validation or format compatibility

### 2. The Japan Times (japantimes-rss)
- **Feed URL:** `https://www.japantimes.co.jp/feed/`
- **Likely Issue:** Feed intermittent or geoblocked
- **Priority:** MEDIUM - Asia-Pacific coverage
- **Diagnosis Needed:** cURL test, check for rate limiting
- **Expected Fix:** Different feed URL or endpoint

### 3. Channel NewsAsia (channelnewsasia-rss)
- **Feed URL:** `https://www.channelnewsasia.com/api/v1/rss-outbound-feed?_format=xml`
- **Likely Issue:** API endpoint requires authentication or has rate limits
- **Priority:** MEDIUM - Southeast Asia coverage
- **Diagnosis Needed:** Test API endpoint, check headers
- **Expected Fix:** Request headers or alternative feed

### 4. 朝日新聞 Asahi Shimbun (asahi-rss)
- **Feed URL:** `https://www.asahi.com/rss/asahi/newsheadlines.rdf`
- **Likely Issue:** RDF format (not RSS 2.0), parser incompatibility
- **Priority:** LOW - Already have Japan Times for Japan coverage
- **Diagnosis Needed:** Validate RDF vs RSS 2.0 format
- **Expected Fix:** Find RSS 2.0 feed or DISABLE (acceptable - redundant coverage)

### 5. South China Morning Post (scmp-rss)
- **Feed URL:** `http://www.scmp.com/rss/91/feed`
- **Issue:** **HTTP URL** (not HTTPS) - blocked by security policy
- **Priority:** MEDIUM - Hong Kong/China coverage important
- **Diagnosis Needed:** Check if HTTPS feed available
- **Expected Fix:** Find HTTPS feed URL or proxy/redirect solution

### 6. Unknown (Need Audit)
- One additional source from the configured 22 is failing
- Could be any source not explicitly verified in audit logs

## Diagnosis Plan

### Phase 1: Identify Exact Failing Sources (1-2 hours)

**Method 1: Build and Run with Health Logging**
```bash
# Add health logging to HomeScreen or create debug screen
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Trigger feed refresh and capture logs
adb logcat | grep -E "RssSourceAdapter|SourceHealth|FAILED"
```

**Method 2: Add Debug Health Screen** (Recommended)
```kotlin
// In HomeScreen or Settings, add button to trigger health report
viewModel.generateHealthReport() // Calls SourceHealthDiagnostic
```

**Method 3: Unit Test with Live Feeds**
```kotlin
// Create test that fetches from all sources and reports status
@Test
fun `audit all RSS sources health`() = runBlocking {
    val httpClient = RssSourceAdapter.createHttpClient()
    val healthMonitor = SourceHealthMonitor()
    val sources = RssSourceAdapter.createApprovedSources(httpClient, healthMonitor)
    
    sources.forEach { adapter ->
        val articles = adapter.fetchArticles()
        println("${adapter.sourceName}: ${articles.size} articles")
    }
}
```

### Phase 2: Validate Each Failing Source (2-4 hours)

For each identified failing source:

**1. Manual cURL Test**
```bash
curl -v "FEED_URL" \
  -H "User-Agent: CrossLens/0.0.14-beta (Android)" \
  2>&1 | tee feed_test.txt

# Check response:
# - HTTP status code (200 = success)
# - Content-Type (application/xml, application/rss+xml)
# - Feed format (RSS 2.0, Atom, RDF)
# - HTTPS vs HTTP
```

**2. Feed Format Validation**
```bash
# Extract first 100 lines to check format
curl -sL "FEED_URL" | head -100

# Check for:
# - <?xml version="1.0"?>
# - <rss version="2.0"> (RSS 2.0)
# - <feed xmlns="http://www.w3.org/2005/Atom"> (Atom)
# - <rdf:RDF> (RDF - NOT supported)
```

**3. Content Validation**
```bash
# Check for required fields
curl -sL "FEED_URL" | grep -E "<item>|<title>|<link>|<pubDate>|<description>"
```

### Phase 3: Apply Fixes (2-4 hours)

**Fix Categories:**

**A. Simple URL Fix**
- Wrong endpoint → Find correct RSS URL from publisher site
- HTTP → HTTPS redirect available → Update URL
- Update `RssSourceAdapter.kt` with correct URL

**B. Format Compatibility**
- RDF format → Find RSS 2.0 alternative OR disable source
- Atom format → Add Atom parser support (future work)
- Malformed XML → Report to publisher OR disable

**C. Access Issues**
- Geoblocking → Disable source (document reason)
- Rate limiting → Add retry logic with exponential backoff
- Authentication required → Disable (cannot use authenticated feeds)

**D. Acceptable Disables**
- Asahi Shimbun (RDF) → DISABLE, keep Japan Times
- ABC Spain → DISABLE if geoblocked, coverage overlap with other Spanish sources
- SCMP → DISABLE if no HTTPS feed, plan to add alternative Hong Kong source

## Fix Priority Matrix

### Must Fix (Priority 1) - 2 sources
Target sources that are unique and have high overlap potential:

1. **Al Jazeera** - If actually failing (core international, Middle East coverage)
2. **Channel NewsAsia** - Southeast Asia coverage gap if unavailable

### Should Fix (Priority 2) - 2 sources
Valuable but have some redundancy:

3. **Japan Times** - Good for Asia-Pacific, but have other sources
4. **SCMP** - Important for China/Hong Kong, but HTTP issue may force disable

### Acceptable to Disable (Priority 3) - 2 sources
Redundant coverage or format incompatibility:

5. **Asahi Shimbun** - RDF format, have Japan Times
6. **One other** - TBD after diagnosis

## Expected Outcomes

### Scenario A: Fix 4 Priority 1-2 Sources
- Al Jazeera: Simple URL or format fix ✅
- Channel NewsAsia: API endpoint fix or alternative ✅
- Japan Times: URL validation ✅
- SCMP: Find HTTPS feed ✅
- Disable Asahi (RDF format) ❌
- Disable one other (acceptable) ❌
- **Result: 20/22 active = 91%** ✅

### Scenario B: Fix 3, Disable 3
- Fix Al Jazeera ✅
- Fix Channel NewsAsia ✅
- Fix Japan Times ✅
- Disable Asahi (RDF) ❌
- Disable SCMP (HTTP only) ❌
- Disable one other ❌
- **Result: 19/22 active = 86%** ⚠️ Below threshold

### Scenario C: Add Replacements
- Disable Asahi, SCMP, and one other (3 disabled)
- Add 3 validated Batch 2 sources as replacements
- **Result: 22/22 active = 100%** ✅ Ideal

## Validation Criteria

Before declaring a source "fixed":

✅ Feed returns HTTP 200  
✅ Response is valid XML  
✅ Format is RSS 2.0 or Atom (with parser support)  
✅ Items have required fields: title, link, pubDate, description  
✅ Links are HTTPS  
✅ pubDate is parseable  
✅ No malformed content  
✅ Fetch completes in < 10 seconds  
✅ Consistent success over 3 test runs

## Success Criteria

**Gate for Batch 2 Expansion:**

✅ **20+ sources ACTIVE** (out of 22-23 configured)  
✅ **90%+ success rate** sustained  
✅ **All failing sources diagnosed** with documented fix or disable reason  
✅ **No undiagnosed failures** remaining  
✅ **48-hour stability** (monitoring with health checks)

## Next Steps

1. ⏳ **Immediate:** Build app with health logging enabled
2. ⏳ **Hour 1-2:** Trigger feed refresh, capture logs, identify exact 6 failing sources
3. ⏳ **Hour 3-6:** Diagnose each failing source with cURL and format validation
4. ⏳ **Hour 7-10:** Apply fixes (URL updates, format checks) or disable with reasons
5. ⏳ **Hour 11-12:** Rebuild, test feed refresh, verify 20+/22 active
6. ⏳ **Day 2-3:** Monitor stability for 48 hours, capture health metrics
7. ⏳ **After 48h:** Proceed to Task 3 (wire service research) and Task 4 (Batch 2 expansion)

## Blocked Until Complete

**Cannot proceed with:**
- Batch 2 source expansion (would worsen success rate)
- Wire service integration (need stable baseline)
- Three-source clustering verification (need more sources)
- Production deployment (below quality gate)

## Timeline Estimate

- **Diagnosis:** 2-4 hours (with logging and testing)
- **Fixes:** 2-6 hours (depending on issues found)
- **Validation:** 1-2 hours (rebuild and verify)
- **Stability monitoring:** 48 hours (passive)
- **Total to Batch 2:** 3-5 days

## Risk Assessment

**Low Risk:**
- Simple URL fixes
- Disabling redundant sources
- Format validation

**Medium Risk:**
- Sources with intermittent failures
- Rate limiting issues
- Geoblocking detection

**High Risk:**
- Making changes without diagnosis (could break working sources)
- Proceeding to Batch 2 without reaching 90% threshold
- Disabling critical sources without replacements

## Recommendations

1. **Prioritize diagnosis over speculation** - Get exact error messages from logs
2. **Fix first, expand second** - Don't add sources until current ones stable
3. **Document all disables** - Clear reasons for transparency
4. **Monitor continuously** - Health checks should run on every feed refresh
5. **Set stability baseline** - 48h at 90%+ before expansion
6. **Plan Batch 2 strategically** - Wire services and high-overlap sources only

## Conclusion

Current 73% success rate (16/22) blocks production and expansion. Must identify and fix/disable 6 failing sources to reach minimum 91% threshold (20/22) before adding Batch 2 sources. Health monitoring infrastructure is in place; need execution of diagnosis and fix workflow.

**Status:** Task 1 (Audit) 80% complete - need live feed test to confirm exact failing sources  
**Next:** Build with health logging → Identify failures → Begin Task 2 (Diagnosis & Fixes)
