# Wire Service RSS Research - Results
**Date:** September 29, 2026  
**Status:** ✅ RESEARCH COMPLETE

## Executive Summary

**Findings:**
- ✅ **UPI**: Public RSS available, HTTPS, RSS 2.0 with images - **READY TO ADD**
- ❌ **Reuters**: Protected by DataDome bot detection (HTTP 401)
- ❌ **AP News**: Protected by Cloudflare challenge (HTTP 403)
- ❌ **AFP**: No public RSS found

**Recommendation:** Add UPI in Batch 2, focus on high-overlap newspapers for additional coverage

## Detailed Results

### ✅ United Press International (UPI) - VALIDATED

**Feed URL:** `https://rss.upi.com/news/news.rss`  
**Status:** ✅ **READY TO INTEGRATE**

**Validation Results:**
```
HTTP/2 200
Content-Type: application/rss+xml
HTTPS: ✅ Yes
Format: RSS 2.0
Namespace: Media RSS (images)
Last Updated: 2026-09-29 20:17:41 -0400
```

**Sample Content:**
- Supreme Court third-country deportations story (✅ **OVERLAP with France 24, Arab News cluster!**)
- Tropical Storm Rachel (weather)
- American Idol contestant murder trial (US news)
- Mexico missing students (Latin America)
- Six Flags coaster closure (US news)

**Technical Details:**
- ✅ HTTPS links
- ✅ Media RSS images (`media:content`, `media:thumbnail`)
- ✅ Valid pubDate fields
- ✅ Descriptive excerpts
- ✅ Recent articles (within hours)
- ✅ No authentication required
- ✅ Fast response (< 1 second)

**Content Use Terms:**
```
Copyright (c) 2026 United Press International, Inc. All Rights Reserved.
```
- Link-and-excerpt use standard for RSS feeds
- Attribution via source name + link
- No explicit aggregation restrictions found

**Expected Overlap:**
- **HIGH** for US domestic news
- **MEDIUM** for major international events
- Verified overlap: Supreme Court deportations story appeared in UPI + France 24 + Arab News cluster

**Decision:** **ADD TO BATCH 2** ✅

---

### ❌ Reuters - NOT AVAILABLE

**Attempted URL:** `https://www.reuters.com/rssFeed/worldNews`  
**Status:** ❌ **BLOCKED**

**Validation Results:**
```
HTTP/2 401 Unauthorized
Server: CloudFront
X-Datadome: protected
```

**Issue:** DataDome bot protection blocking automated access

**Alternative Attempts:**
- `https://www.reuters.com/tools/rss` - Also HTTP 401
- Various topic URLs - All protected

**Diagnosis:**
- Reuters implements aggressive bot protection
- Public RSS may exist but requires browser-like access patterns
- Likely intended for individual users, not aggregators

**Decision:** **NOT AVAILABLE** - Bot protection makes automated access unreliable

**Impact:** High - Reuters would have been highest-value wire service

---

### ❌ Associated Press (AP) - NOT AVAILABLE

**Attempted URL:** `https://apnews.com/hub/ap-top-news`  
**Status:** ❌ **BLOCKED**

**Validation Results:**
```
HTTP/2 403 Forbidden
Server: Cloudflare
CF-Mitigated: challenge
```

**Issue:** Cloudflare challenge/protection

**Alternative Attempts:**
- `https://feeds.apnews.com/rss/topnews` - No response
- `https://apnews.com/rss` - 403 Forbidden

**Diagnosis:**
- AP protects content with Cloudflare challenges
- RSS feeds may exist for partners only
- Public access appears restricted

**Decision:** **NOT AVAILABLE** - Access restrictions prevent integration

**Impact:** High - AP would have been critical for US + international coverage

---

### ❌ Agence France-Presse (AFP) - NOT FOUND

**Attempted URL:** `https://www.afp.com/feed`  
**Status:** ❌ **NOT FOUND**

**Diagnosis:**
- AFP corporate site focused on B2B content licensing
- No public RSS feeds discovered
- Likely available only to media partners

**Decision:** **NOT AVAILABLE** - No public RSS found

**Impact:** Medium - Would have provided French perspective, but have Le Monde

---

### ✅ Bloomberg News - NOT TESTED

**Status:** NOT TESTED (subscription-based)

**Reason:** Bloomberg is known to require subscription for content access  
**Priority:** LOW - Business-focused, likely paywalled

## Summary Table

| Wire Service | HTTP Status | Format | HTTPS | Decision | Expected Overlap |
|--------------|-------------|--------|-------|----------|------------------|
| **UPI** | 200 | RSS 2.0 | ✅ | ✅ **ADD** | Medium-High |
| Reuters | 401 | N/A | ❌ | ❌ BLOCKED | Very High |
| AP | 403 | N/A | ❌ | ❌ BLOCKED | Very High |
| AFP | 404 | N/A | ❌ | ❌ NOT FOUND | High |

## Impact Assessment

### Wire Service Availability: 25%
- **Available:** 1 of 4 major wire services (UPI)
- **Blocked:** 2 of 4 (Reuters, AP)
- **Not Found:** 1 of 4 (AFP)

### Clustering Impact

**With UPI:**
- Add 1 wire service covering US + major international events
- Verified overlap on Supreme Court deportations story
- Expected to increase 3+ source clusters for US domestic news
- Moderate increase for major international events

**Without Reuters/AP:**
- Miss highest-overlap potential sources
- Will rely on major newspapers for multi-source coverage
- Need to compensate with more international newspapers

## Batch 2 Strategy Adjustment

### Original Plan (with wire services):
- Add Reuters, AP, AFP (3 wire services)
- Add 2-3 newspapers
- **Total:** 25-26 sources

### Revised Plan (UPI only):
- Add UPI (1 wire service)
- Add 6-8 high-overlap international newspapers
- Focus on sources that cover major events
- **Total:** 27-29 sources

## High-Overlap Newspaper Candidates

To compensate for missing Reuters/AP, prioritize newspapers with broad international coverage:

### Priority 1: Verified Available (from existing research)
1. ✅ Financial Times - Business + international
2. ✅ The Independent (UK) - International coverage
3. ✅ NHK World (Japan) - Asia-Pacific + international
4. ✅ RTE News (Ireland) - European + international

### Priority 2: To Validate
5. 🔍 TRT World (Turkey) - Middle East + international
6. 🔍 Xinhua News (China) - Asia + international perspective
7. 🔍 Yomiuri Shimbun (Japan) - Japanese + international
8. 🔍 El País (Spain) - Spanish + Latin American coverage

## Implementation Plan

### Batch 2 Source List (Revised)

**Wire Service (1):**
1. UPI - US + international

**High-Overlap Newspapers (6-7):**
2. Financial Times - Business + international
3. The Independent (UK) - UK + international
4. NHK World (Japan) - Asia-Pacific
5. TRT World (Turkey) - Middle East (if validated)
6. El País (Spain) - Spanish + Latin America
7. One additional Asia source (Xinhua or Yomiuri if validated)

**Expected Result:** 26-27 total sources

### Clustering Expectations

**With 26-27 sources (including UPI):**

**Major breaking news:**
- 3-5 sources within 24 hours (up from 2-3)
- UPI + existing newspapers

**Scheduled events (summits, elections):**
- 4-6 sources within 24 hours (up from 2-4)

**Regional events:**
- 2-4 sources within 48 hours (stable)

**US domestic news:**
- 3-4 sources (UPI, NYT, WashPost, possibly Independent)

## Recommendations

1. **Immediate:** Add UPI to Batch 2 (validated and ready)

2. **Short-term:** Validate 6-7 high-overlap newspapers to compensate for missing Reuters/AP

3. **Medium-term:** Monitor for Reuters/AP public RSS availability changes

4. **Long-term:** Consider alternative wire services:
   - Inter Press Service (IPS)
   - Kyodo News (Japan)
   - TASS (Russia) - if politically acceptable

## Success Criteria Met

✅ **At least 1 wire service validated** (UPI)  
✅ **Wire service has public RSS** (UPI HTTPS)  
✅ **RSS format compatible** (RSS 2.0 with images)  
✅ **Content overlap verified** (Supreme Court story)  
✅ **Alternative strategy defined** (high-overlap newspapers)

## Conclusion

**Wire service research COMPLETE:**
- UPI validated and ready to add
- Reuters/AP unavailable due to bot protection
- Revised Batch 2 strategy: 1 wire service + 6-7 newspapers
- Expected outcome: 26-27 sources with moderate clustering improvement

**Status:** Task 3 complete, proceeding to Task 2 (fixes) and Task 4 (Batch 2 expansion)
