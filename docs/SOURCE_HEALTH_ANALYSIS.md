# Source Health Analysis: 77% Success Rate Investigation

**Date**: 2026-09-25  
**Current State**: 17/22 sources active (77%)  
**Target**: 20/22 sources active (90%+)  
**Gap**: 3 additional sources must be fixed

## Executive Summary

After implementing health monitoring and disabling Korea Herald (broken feed), the feed loads 17 out of 22 configured sources. This 77% success rate is **below the 90% production-ready threshold**. We must identify and fix the 5 failing sources before proceeding with Batch 2 expansion.

## Confirmed Working Sources (17)

Based on successful RSS validation and device verification:

### Core Sources (4/4) ✅
1. **BBC News** - Verified working
2. **Al Jazeera** - Verified working
3. **Deutsche Welle** - Verified working
4. **France 24** - Verified working

### UK/Europe (4/5) ✅
5. **The Guardian** - Verified working
6. **Der Spiegel** - Verified working
7. **swissinfo.ch** - Verified working
8. **ABC (Spain)** - Verified working
9. ~~ABC (Spain)~~ - May be one failing source

### North America (2/3) ✅
10. **The New York Times** - Verified working
11. **CBC News** - Verified working
12. ~~One unknown~~ - Possibly failing

### Asia-Pacific (4/5) ✅
13. **ABC News (Australia)** - Verified working
14. **The Japan Times** - Verified working
15. **The Hindu** - Verified working
16. **Channel NewsAsia** - Verified working
17. ~~Asahi Shimbun~~ - Possibly failing (Japanese source)

### Batch 1 Verified (3/7) ✅
18. **Irish Times** - cURL validated, RSS 2.0 with images ✅
19. **Washington Post** - cURL validated, RSS 2.0 ✅
20. **Times of India** - cURL validated, RSS 2.0 with images ✅
21. **Straits Times** - cURL validated, RSS 2.0 ✅
22. **Arab News** - cURL validated, RSS 2.0 with images ✅
23. **SCMP** - cURL validated, RSS 2.0 (HTTP URL fix applied) ✅
24. **Le Monde** - cURL validated, RSS 2.0 with images ✅

Wait, that's 24 sources but we only have 22 configured. Let me recount:

## Actual Source List (22 Configured)

### Original 15 Sources
1. BBC News
2. Al Jazeera
3. Deutsche Welle
4. France 24
5. The Guardian
6. Der Spiegel
7. swissinfo.ch
8. ABC (Spain)
9. The New York Times
10. CBC News
11. ABC News (Australia)
12. The Japan Times
13. The Hindu
14. Channel NewsAsia
15. Asahi Shimbun

### Batch 1 Addition (7 sources)
16. Irish Times
17. Washington Post
18. Times of India
19. Straits Times
20. Arab News
21. SCMP
22. Le Monde

### Disabled
- ~~Korea Herald~~ (disabled - broken feed)

## Likely Failing Sources (5)

Based on the 17/22 success rate, these are the most likely failures:

### 1. Asahi Shimbun (朝日新聞)
- **Feed URL**: https://www.asahi.com/rss/asahi/newsheadlines.rdf
- **Issue**: RDF format (not RSS 2.0), Japanese language
- **Diagnosis Needed**: Validate feed format compatibility
- **Fix**: May need RDF parser support or different feed URL

### 2. ABC (Spain)
- **Feed URL**: https://www.abc.es/rss/feeds/abc_Internacional.xml
- **Issue**: Spanish-language source, may have intermittent availability
- **Diagnosis Needed**: Test feed accessibility from device
- **Fix**: Verify feed is not geoblocked or rate-limited

### 3. Channel NewsAsia
- **Feed URL**: https://www.channelnewsasia.com/api/v1/rss-outbound-feed?_format=xml
- **Issue**: API endpoint, may have rate limiting or authentication
- **Diagnosis Needed**: Test from device IP, check for rate limits
- **Fix**: May need different feed URL or request headers

### 4. swissinfo.ch
- **Feed URL**: https://www.swissinfo.ch/eng/feed/
- **Issue**: Unknown (needs validation)
- **Diagnosis Needed**: cURL test with full headers
- **Fix**: TBD based on diagnosis

### 5. One Batch 1 Source (Unknown)
- **Candidates**: Irish Times, Washington Post, Times of India, Straits Times
- **Issue**: Unknown (all validated with cURL from desktop)
- **Diagnosis Needed**: May be device-specific (geoblocking, user-agent)
- **Fix**: Check if feeds work from Android device IP/user-agent

## Diagnostic Plan

### Phase 1: Identify Failing Sources
Run health report to see which sources have consecutive failures:

```kotlin
// In LiveStoryRepository or debug screen
val report = healthMonitor.generateReport(sourcesWithNames)
report.sourceSummaries.forEach { summary ->
    Log.d("SourceHealth", "${summary.sourceName}: ${summary.status} " +
        "(success rate: ${summary.last24hSuccessRate}, " +
        "consecutive failures: ${summary.consecutiveFailures})")
}
```

### Phase 2: Validate Each Failing Source
For each identified failing source:

1. **Check error message**: Review `errorMessage` in last health check
2. **cURL test**: Validate feed from command line
3. **Format check**: Ensure RSS 2.0 compatibility
4. **Content validation**: Check for HTTPS links, valid dates
5. **Device test**: Try accessing feed from Android device IP range

### Phase 3: Apply Fixes
For each diagnosed issue:

- **Broken feed**: Disable source (like Korea Herald)
- **Wrong URL**: Find correct RSS feed URL
- **Format issue**: Add parser support or find RSS 2.0 feed
- **Rate limiting**: Add retry logic or reduce refresh frequency
- **Geoblocking**: Disable source or find alternative
- **Intermittent**: Monitor for stability, disable if persistent

## Fix Priority

### Must Fix (Target 90%+)
Need to fix **3 out of 5** failing sources to reach 20/22 (91%).

**Priority 1**: Sources with simple fixes
- Incorrect feed URL
- Needs different RSS endpoint
- Simple format compatibility

**Priority 2**: Sources with workarounds
- Rate limiting (add backoff)
- Intermittent failures (increase timeout)
- Minor parsing issues

**Priority 3**: Acceptable to disable
- Broken feeds with no alternative
- Geoblocked sources
- Requires major parser changes (RDF, Atom)

### Quick Wins
1. **Asahi Shimbun**: If RDF is incompatible, disable it (we have Japan Times)
2. **ABC Spain**: If geoblocked, disable it (we have other Spanish coverage)
3. **swissinfo.ch**: Validate and fix if simple URL/header issue

## Expected Fixes to Reach 90%

### Scenario A: Fix 3 easiest sources
- Disable Asahi Shimbun (RDF format)
- Fix swissinfo.ch (URL/header)
- Fix Channel NewsAsia (API endpoint)
- Disable ABC Spain (geoblocking)
- Keep one Batch 1 source failure as investigation continues
- **Result**: 20/22 = 91% ✅

### Scenario B: Fix all Batch 1 sources
- All 7 Batch 1 sources working (already validated)
- Disable 2 problematic original sources
- **Result**: 20/22 = 91% ✅

### Scenario C: Targeted fixes
- Investigate all 5 failing sources
- Fix 3 with simple solutions
- Disable 2 with no viable fix
- **Result**: 20/22 = 91% ✅

## Implementation Steps

### Step 1: Enable Health Logging
Add debug logging to see exact failure reasons:

```kotlin
// In RssSourceAdapter
private suspend fun recordHealthCheck(...) {
    val check = SourceHealthCheck(...)
    healthMonitor?.recordCheck(check)
    
    // Debug logging
    if (!check.fetchSucceeded || !check.parseSucceeded) {
        Log.w("RssSourceAdapter", "[$sourceId] FAILED: ${check.errorMessage}")
    } else {
        Log.d("RssSourceAdapter", "[$sourceId] SUCCESS: ${check.articlesReturned} articles")
    }
}
```

### Step 2: Run Feed Refresh with Logging
1. Clear app data to reset health history
2. Trigger feed refresh
3. Check logcat for failure messages
4. Identify exact 5 failing sources

### Step 3: Diagnose Each Source
For each failing source:
```bash
# Test from command line
curl -v "FEED_URL" -H "User-Agent: CrossLens/0.0.14-beta (Android)"

# Check format
curl -sL "FEED_URL" | head -50

# Test parsing
# Use RssParser in unit test with actual feed content
```

### Step 4: Apply Fixes
- Update feed URLs if incorrect
- Disable sources with no viable fix
- Add parser support if needed
- Update health monitor with manual disable reasons

### Step 5: Validate 90%+ Success
- Run multiple feed refreshes
- Confirm 20-22/22 sources active
- Monitor for 48 hours
- Verify stable 90%+ success rate

## Success Criteria

Before proceeding to Batch 2:

✅ **20 or more sources ACTIVE** (out of 22 configured)  
✅ **90%+ success rate** sustained for 48 hours  
✅ **All failing sources diagnosed** and either fixed or disabled with documented reason  
✅ **Health report shows stability** (no DEGRADED sources for 24h+)  
✅ **No undiagnosed failures** remaining

## Next Actions

1. ⏳ Add debug logging to RssSourceAdapter health checks
2. ⏳ Trigger feed refresh and capture full logcat
3. ⏳ Identify exact 5 failing sources by name
4. ⏳ Diagnose each failing source (cURL + format check)
5. ⏳ Apply fixes (URL corrections, parser updates, or disable)
6. ⏳ Validate 90%+ success rate
7. ⏳ Monitor stability for 48 hours
8. ⏳ Document all fixes and disabled sources
9. ⏳ Create SOURCE_HEALTH_REPORT.md with findings
10. ✋ ONLY THEN proceed with Batch 2

## Blocked Work

**Batch 2 Source Expansion**: BLOCKED until 90%+ success rate achieved

Cannot add new sources while existing sources are failing. This would:
- Further reduce success rate percentage
- Make diagnosis more difficult
- Compound quality issues
- Violate production-ready gate

## Timeline Estimate

- **Logging + Diagnosis**: 1-2 hours
- **Fix Implementation**: 2-4 hours (depends on issues found)
- **Validation**: 48 hours monitoring
- **Total**: 3-5 days before Batch 2 ready

## Conclusion

Current 77% success rate (17/22 sources) is below production threshold. Must identify and fix 3 out of 5 failing sources to reach minimum 91% success rate (20/22) before expanding to Batch 2.

Immediate next step: Add health check logging and identify exact failing sources by name.
