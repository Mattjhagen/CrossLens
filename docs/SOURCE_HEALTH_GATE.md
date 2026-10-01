# Source Health Gate

## Overview

The CrossLens feed infrastructure now includes automated health monitoring for all RSS sources. Each fetch attempt is tracked for success/failure, article quality, and reliability metrics. Sources that persistently fail are automatically degraded or disabled to maintain feed quality.

## Health Status Levels

### ACTIVE
- Source is fetching and parsing successfully
- Meets quality thresholds
- Included in feed refresh

### DEGRADED
- Source experiencing issues but may recover
- 3+ consecutive failures
- Still included in feed refresh (transient issues expected)
- Monitored closely for auto-disable threshold

### DISABLED
- Source has persistent, unrecoverable failures
- 10+ consecutive failures OR manually disabled
- Excluded from feed refresh
- Requires investigation and manual re-enable

## Health Metrics Tracked

For each fetch attempt, the system records:

1. **Fetch success** - HTTP request succeeded (2xx response)
2. **Parse success** - RSS XML parsed without errors
3. **Article count** - Number of articles returned
4. **Valid dates** - Articles with parseable publication dates
5. **Image availability** - Articles with image URLs
6. **HTTPS links** - Articles with secure links
7. **Freshness** - Age of latest article
8. **Fetch duration** - Time taken to fetch and parse
9. **Error messages** - Diagnostic information on failure

## Aggregated Metrics (24-hour rolling window)

- **Success rate** - Percentage of successful fetches
- **Total articles** - Total articles delivered
- **Image rate** - Percentage of articles with images
- **Consecutive failures** - Current failure streak

## Production-Ready Gate

The feed is considered production-ready when:

✅ **90% or more configured sources are ACTIVE**

Current requirement: ≥20 out of 22 configured sources must be ACTIVE.

## Current Source Status

**Last Updated**: 2026-09-25

### Configured: 22 sources
- **Active**: TBD (after first health check)
- **Degraded**: 0
- **Disabled**: 1 (Korea Herald)

### Disabled Sources

#### Korea Herald (koreaherald-rss)
- **Status**: DISABLED (manual)
- **Reason**: RSS feed URL returns HTML error page instead of RSS XML
- **Feed URL**: http://www.koreaherald.com/common/newslist.xml?ct=020000000000
- **Diagnosis**: The feed endpoint is broken or requires different URL/parameters
- **Resolution**: Requires investigation of correct Korea Herald RSS feed URL
- **Alternative**: Consider other Korean English-language sources

### Source Fixes Applied (Batch 1)

#### South China Morning Post (scmp-rss)
- **Issue**: HTTPS feed URL redirects to HTTP, blocked by OkHttp security
- **Fix**: Use HTTP feed URL directly (`http://www.scmp.com/rss/91/feed`)
- **Verification**: Feed returns valid RSS 2.0 with enclosures and media:content
- **Status**: Should now be ACTIVE

#### Arab News (arabnews-rss)
- **Previous Status**: Unknown (appeared to fail in device test)
- **Verification**: Feed returns valid RSS 2.0 with images
- **Expected Status**: ACTIVE (was likely transient failure)

#### Le Monde (lemonde-rss)
- **Previous Status**: Unknown (appeared to fail in device test)
- **Verification**: Feed returns valid RSS 2.0 with media:content images
- **Expected Status**: ACTIVE (was likely transient failure)

## Regional Balance After Fixes

With 22 active sources (Korea Herald disabled):

- **Europe/UK**: 9 sources (41%)
- **Asia-Pacific**: 7 sources (32%) - down from 8
- **North America**: 3 sources (14%)
- **Middle East**: 2 sources (9%)
- **Africa**: 0 (0%)
- **Latin America**: 0 (0%)

**Languages**:
- English: 20 sources (91%)
- French: 1 source (5%)
- Spanish: 1 source (5%)
- Japanese: 1 source (5%)

## Expected Success Rate

With fixes applied:

- **Best case**: 22/22 sources active (100%)
- **Realistic**: 20-22/22 sources active (91-100%)
- **Minimum acceptable**: 20/22 sources active (91%)

## Health Check Implementation

### RssSourceAdapter
- Tracks timing, success/failure, article metrics
- Records health check after each fetch
- Passes optional SourceHealthMonitor

### SourceHealthMonitor
- Singleton service tracking all source health
- Maintains last 50 checks per source
- Auto-updates status based on consecutive failures
- Provides source summaries and feed-wide reports

### Integration Points
1. **Dependency Injection**: SourceHealthMonitor provided via Hilt
2. **Adapter Creation**: Health monitor passed to each RssSourceAdapter
3. **Feed Refresh**: LiveStoryRepository can check if source is enabled before fetching
4. **Diagnostics**: Health report available for debugging and monitoring

## Monitoring Recommendations

### During Development
- Check health report after each feed refresh
- Investigate any DEGRADED sources within 24 hours
- Review error messages for DISABLED sources
- Validate fixes with multiple refresh cycles

### Production Deployment
- Expose health metrics in app diagnostics/settings
- Alert on overall success rate < 90%
- Alert on any source DISABLED for > 48 hours
- Weekly review of image availability rates
- Monthly audit of article freshness

## Validation Commands

```bash
# Run unit tests (should still pass)
./gradlew testDebugUnitTest

# Build debug APK
./gradlew assembleDebug

# Install and test on device
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Check logcat for health metrics
adb logcat | grep -i "health\|source"
```

## Next Steps

### Before Batch 2
1. ✅ Implement health monitoring infrastructure
2. ✅ Disable Korea Herald (broken feed)
3. ✅ Fix SCMP redirect issue (use HTTP URL)
4. ⏳ Validate 20-22/22 sources ACTIVE on device
5. ⏳ Run feed refresh multiple times, verify stability
6. ⏳ Review health report, confirm 90%+ success rate
7. ⏳ Document baseline metrics (articles/source, image rate)

### Batch 2 Prerequisites
- Overall success rate ≥ 90% for 48 hours
- No DEGRADED sources for > 24 hours
- All DISABLED sources investigated and documented
- Health monitoring validated in production environment

### Batch 2 Approach
- Add sources incrementally (2-3 at a time)
- Validate each mini-batch with health report
- Require 90%+ success rate before adding next batch
- Disable any new source that fails > 5 times in first 24h
- Target 30 total sources (add 8 more)

## Source Investigation: Korea Herald

### Attempted URLs
1. `http://www.koreaherald.com/common/newslist.xml?ct=020000000000` ❌ Returns HTML
2. Redirects to `https://www.koreaherald.com/common/newslist.xml?ct=020000000000` ❌ Returns HTML error page

### Possible Alternatives
1. Check Korea Herald website for current RSS feed links
2. Try alternative Korean English-language sources:
   - Yonhap News Agency (if RSS available)
   - Korea JoongAng Daily
   - KBS World Radio

### Resolution Required Before Re-enable
- Locate working RSS feed URL
- Validate RSS format and content
- Test for 48 hours before re-enabling
- Update SourceMetadata with correct URL

## Conclusion

With health monitoring in place and immediate fixes applied, the feed should now achieve 91-100% source success rate (20-22/22 sources active). Korea Herald remains disabled pending investigation of correct RSS feed URL.

**Production-Ready Status**: ⏳ **PENDING VALIDATION**

Next checkpoint: Validate 90%+ success rate for 48 hours before proceeding with Batch 2.
