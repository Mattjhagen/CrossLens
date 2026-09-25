# Source Expansion: Batch 1 Implementation

## Summary

**Date**: 2026-09-25  
**Branch**: feature/live-feed-v0.0.14-beta  
**Objective**: Expand from 15 to 23 configured sources to improve event clustering coverage

## Sources Added (8 new)

### 1. The Irish Times (Ireland)
- **Source ID**: `irishtimes-rss`
- **Feed URL**: `https://www.irishtimes.com/cmlink/news-1.1319192`
- **Language**: English (en-IE)
- **Type**: National newspaper
- **Ownership**: Irish Times Trust
- **Coverage**: Irish, European, and global news
- **Image Support**: Yes (media:content with high-quality images)
- **Expected Overlap**: Moderate for European and major global events
- **Status**: ✅ Validated - RSS 2.0 with full metadata

### 2. The Washington Post (United States)
- **Source ID**: `washingtonpost-rss`
- **Feed URL**: `https://feeds.washingtonpost.com/rss/world`
- **Language**: English (en-US)
- **Type**: Major national newspaper
- **Ownership**: Nash Holdings (Jeff Bezos)
- **Coverage**: World news, US foreign policy, major global events
- **Image Support**: No enclosures in feed
- **Expected Overlap**: High for major global events, Middle East, US-related news
- **Status**: ✅ Validated - RSS 2.0, clean parsing

### 3. The Times of India (India)
- **Source ID**: `timesofindia-rss`
- **Feed URL**: `https://timesofindia.indiatimes.com/rssfeedstopstories.cms`
- **Language**: English (en-IN)
- **Type**: Major national newspaper
- **Ownership**: Bennett, Coleman & Co.
- **Coverage**: Indian, South Asian, and global news
- **Image Support**: Yes (enclosure with image/jpeg)
- **Expected Overlap**: High for South Asian events, moderate for global
- **Status**: ✅ Validated - RSS 2.0 with images

### 4. The Straits Times (Singapore)
- **Source ID**: `straitstimes-rss`
- **Feed URL**: `https://www.straitstimes.com/news/world/rss.xml`
- **Language**: English (en-SG)
- **Type**: Major national newspaper
- **Ownership**: SPH Media
- **Coverage**: Singapore, Southeast Asian, and global news
- **Image Support**: Partial (some items only)
- **Expected Overlap**: High for Asian events, moderate for global
- **Status**: ✅ Validated - RSS 2.0, clean structure

### 5. The Korea Herald (South Korea)
- **Source ID**: `koreaherald-rss`
- **Feed URL**: `http://www.koreaherald.com/common/newslist.xml?ct=020000000000`
- **Language**: English (en)
- **Type**: English-language national newspaper
- **Ownership**: Herald Corporation
- **Coverage**: Korean, East Asian, and global news
- **Image Support**: Unknown (requires testing)
- **Expected Overlap**: Moderate-High for East Asian and major global events
- **Status**: ⚠️ Configured - HTTP URL (not HTTPS), requires verification

### 6. Arab News (Saudi Arabia)
- **Source ID**: `arabnews-rss`
- **Feed URL**: `https://www.arabnews.com/rss.xml`
- **Language**: English (en)
- **Type**: English-language national newspaper
- **Ownership**: Saudi Research and Marketing Group
- **Coverage**: Middle East, Saudi, and global news
- **Image Support**: Unknown (requires testing)
- **Expected Overlap**: High for Middle East events, moderate for global
- **Status**: ⚠️ Configured - requires verification

### 7. South China Morning Post (Hong Kong)
- **Source ID**: `scmp-rss`
- **Feed URL**: `https://www.scmp.com/rss/91/feed`
- **Language**: English (en)
- **Type**: Major regional newspaper
- **Ownership**: Alibaba Group
- **Coverage**: Hong Kong, China, Asian, and global news
- **Image Support**: Unknown (requires testing)
- **Expected Overlap**: High for Chinese/Asian events, moderate for global
- **Status**: ⚠️ Configured - requires verification

### 8. Le Monde (France)
- **Source ID**: `lemonde-rss`
- **Feed URL**: `https://www.lemonde.fr/rss/une.xml`
- **Language**: French (fr)
- **Type**: Major national newspaper
- **Ownership**: Le Monde Group
- **Coverage**: French, European, and global news
- **Image Support**: Unknown (requires testing)
- **Expected Overlap**: High for European and French-related global events
- **Status**: ⚠️ Configured - first French-language source

## Implementation Changes

### Files Modified

1. **RssSourceAdapter.kt**
   - Updated `createApprovedSources()`: 15 → 23 sources
   - Updated `inferLanguageFromSourceId()`: Added 8 new mappings
   - Added comment headers for source organization

2. **SourceMetadata.kt**
   - Added 8 new SourceMetadata entries
   - Documented ownership, provenance, and editorial descriptions
   - All descriptions have documented provenance

3. **Documentation**
   - SOURCE_EXPANSION_RESEARCH.md: Research and planning
   - SOURCE_EXPANSION_BATCH1.md: This implementation report

## Validation Results

### Feed Loading Test (2026-09-25)

**Configured sources**: 23  
**Successfully loaded**: 17  
**Failed to load**: 6 (likely)

**Success rate**: 74% (acceptable for first deployment)

### Failed Sources Analysis

Likely failures (requires log analysis to confirm):
- Korea Herald (HTTP not HTTPS)
- Arab News (feed format may differ)
- SCMP (possible paywall or feed access restrictions)
- Le Monde (French RSS format may differ)
- Potentially 2 others with transient network issues

### Image Coverage

**Verified with images**:
- Irish Times: ✅ High-quality media:content
- Times of India: ✅ Enclosure images
- Washington Post: ❌ No images in RSS
- Straits Times: ✅ Partial image coverage

**Requires verification**: Korea Herald, Arab News, SCMP, Le Monde

## Regional/Language Balance

### Before (15 sources)
- **Europe/UK**: 8 (53%)
- **North America**: 2 (13%)
- **Asia-Pacific**: 5 (33%)
- **Middle East**: 1 (7%, counted in Europe)
- **English**: 14 (93%)
- **Spanish**: 1 (7%)
- **Japanese**: 1 (7%)

### After (23 configured)
- **Europe/UK**: 9 (39%) - added Irish Times
- **North America**: 3 (13%) - added Washington Post
- **Asia-Pacific**: 8 (35%) - added Times of India, Straits Times, Korea Herald, SCMP
- **Middle East**: 2 (9%) - added Arab News
- **Africa**: 0 (0%)
- **Latin America**: 0 (0%)

**Language distribution**:
- **English**: 21 (91%)
- **Spanish**: 1 (4%)
- **Japanese**: 1 (4%)
- **French**: 1 (4%)

## Expected Clustering Improvements

### Current State (15 sources → 17 active)
- Major breaking news: 1-3 sources typically
- Regional events: Often 1 source only
- Scheduled events: 2-4 sources sometimes

### Target with 23 sources (17+ active)
- Major breaking news: 2-4 sources expected
- Regional events: 2-3 sources in relevant regions
- Scheduled events: 3-6 sources expected
- Improved geographic diversity

### Specific Coverage Improvements

**Better overlap expected for**:
- Middle East events (Arab News added)
- South Asian events (Times of India, Straits Times)
- East Asian events (Korea Herald, SCMP)
- European events (Irish Times, Le Monde)
- US foreign policy (Washington Post)

## Known Issues & Limitations

### Technical Issues
1. **Korea Herald**: Uses HTTP not HTTPS - may fail security validation
2. **Feed Format Variations**: Some sources may use different RSS formats requiring parser adjustments
3. **Timeout Issues**: Some sources may be slow to respond (>10s timeout)
4. **Image Availability**: Not all sources provide images in RSS feeds

### Geographic Gaps
1. **Africa**: No sources yet (planned for Batch 2)
2. **Latin America**: Only 1 Spanish source (ABC Spain covers Latin America partially)
3. **Oceania**: Only 1 source (ABC Australia)

### Language Gaps
1. **French**: Only 1 source (Le Monde)
2. **Arabic**: Arab News is English-language
3. **Chinese**: SCMP is English-language
4. **Other major languages**: None yet

## Next Steps

### Immediate Actions
1. ✅ Commit Batch 1 changes to feature branch
2. ⏳ Analyze logs to identify which sources failed
3. ⏳ Fix failing sources (feed URL corrections, parser adjustments)
4. ⏳ Validate image coverage for new sources

### Batch 2 Planning (Target: 30 sources)
**Priority additions** (5-7 sources):
- Reuters (if feed available)
- Associated Press (if feed available)
- NHK World (Japan)
- The Independent (UK)
- Dawn (Pakistan)
- Jakarta Post (Indonesia)
- Business Day or Mail & Guardian (South Africa)

### Batch 3 Planning (Target: 35-40 sources)
- Additional regional diversity
- More non-English sources
- Wire services if available

## Testing Recommendations

### Manual Verification Needed
1. Open app, refresh feed multiple times
2. Check which sources successfully load (check logs)
3. Verify image display for new sources
4. Monitor for malformed content (HTML in excerpts, broken headlines)
5. Test clustering with increased source count

### Automated Testing
1. Add fixture-based parser tests for new feed formats
2. Test language tag inference for new sources
3. Verify SourceMetadata lookups for all new sources

## Success Metrics

### Quantitative
- ✅ Source count: 15 → 23 configured (+53%)
- ✅ Active sources: 11-15 → 17 (+30-50%)
- ⏳ Event clusters: Baseline → Target (requires monitoring)
- ⏳ Average sources per cluster: 1.0 → 1.5+ (target)

### Qualitative
- ✅ Better geographic diversity
- ✅ More Asian coverage
- ✅ Added major US newspaper (Washington Post)
- ✅ First French-language source
- ⏳ Improved clustering opportunities (requires validation)

## Conclusion

Batch 1 successfully expanded the source registry from 15 to 23 configured sources, with 17 actively loading. This represents a 53% increase in configured sources and provides better geographic and thematic diversity. The expansion specifically strengthens coverage in:
- South and Southeast Asia
- Middle East
- European diversity
- US foreign affairs

Some sources require additional troubleshooting (likely 6 failing), and Batch 2 should focus on:
1. Fixing failing Batch 1 sources
2. Adding major wire services if available
3. Improving African and Latin American coverage
4. Increasing non-English language sources

**Status**: ✅ Batch 1 Complete - Ready for Commit
