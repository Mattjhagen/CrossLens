# Wire Service RSS Feed Research
**Date:** September 29, 2026  
**Purpose:** Identify publicly accessible RSS feeds from major wire services  
**Priority:** CRITICAL for three-source event clustering

## Why Wire Services Matter

Wire services are the **highest priority** for achieving 3+ source clusters because:

1. **Maximum overlap potential** - Cover all major breaking news events
2. **Primary sources** - Many publishers syndicate wire content
3. **Event anchors** - Natural clustering points for multiple downstream sources
4. **Global reach** - Cover international events from all regions
5. **Consistent quality** - Professional reporting with reliable metadata

**Expected Impact:** Adding 2-3 wire services could enable:
- 3-5 source clusters for major breaking news
- 4-8 source clusters for scheduled events (summits, elections)
- Foundation for "Read Across Coverage" feature validation

## Target Wire Services

### 1. Reuters
**Status:** 🔍 **RESEARCH NEEDED**

**Publisher:** Thomson Reuters Corporation  
**Country:** United Kingdom (global operations)  
**Coverage:** Global news, business, politics, technology  
**Expected Overlap:** VERY HIGH - covers all major events

**Known RSS Endpoints:**
- ❓ `https://www.reutersagency.com/feed/` - Agency site (subscription required?)
- ❓ `https://www.reuters.com/rssFeed/...` - Public site (various topics)
- 🔍 Need to investigate: Public RSS availability and terms

**Validation Steps:**
```bash
# Test various potential Reuters RSS feeds
curl -v "https://www.reuters.com/rssFeed/worldNews" \
  -H "User-Agent: CrossLens/0.0.14-beta (Android)"

# Check for:
# - HTTP 200 response
# - Valid RSS 2.0 XML
# - HTTPS links
# - Recent articles
# - Terms of service compliance
```

**Research Questions:**
- Does Reuters offer public RSS feeds?
- Are they HTTPS?
- What are the content use terms?
- Rate limiting or attribution requirements?
- Which topic feeds are available?

**Priority:** **CRITICAL** - Reuters is the single most valuable addition

---

### 2. Associated Press (AP)
**Status:** 🔍 **RESEARCH NEEDED**

**Publisher:** Associated Press (cooperative)  
**Country:** United States (global coverage)  
**Coverage:** Global news, politics, business, sports  
**Expected Overlap:** VERY HIGH - covers all major events

**Known RSS Endpoints:**
- ❓ `https://feeds.apnews.com/...` - Subdomain exists
- ❓ `https://apnews.com/...` - Main site
- ❓ AP typically provides RSS to partners, unclear if public

**Validation Steps:**
```bash
# Test AP News RSS
curl -v "https://apnews.com/rss" \
  -H "User-Agent: CrossLens/0.0.14-beta (Android)"

# Try common RSS patterns
curl -I "https://feeds.apnews.com/rss/topnews"
curl -I "https://apnews.com/feed"
```

**Research Questions:**
- Does AP offer free public RSS feeds?
- License/attribution requirements for link-and-excerpt use?
- Rate limiting policies?
- Available topic categories?

**Priority:** **CRITICAL** - AP is essential for US + international coverage

---

### 3. Agence France-Presse (AFP)
**Status:** 🔍 **RESEARCH NEEDED**

**Publisher:** Agence France-Presse (French state-owned)  
**Country:** France (global operations)  
**Coverage:** International news, politics, culture  
**Expected Overlap:** HIGH - major international events

**Known RSS Endpoints:**
- ❓ `https://www.afp.com/...` - Corporate site
- ❓ AFP primarily sells content to media organizations
- ❓ Public RSS availability unclear

**Validation Steps:**
```bash
# Check for AFP public RSS
curl -v "https://www.afp.com/feed" \
  -H "User-Agent: CrossLens/0.0.14-beta (Android)"
```

**Research Questions:**
- Does AFP provide public RSS feeds?
- English-language availability?
- Content use terms for aggregators?
- Attribution requirements?

**Priority:** **HIGH** - Valuable for French perspective + international coverage

---

### 4. Bloomberg News
**Status:** 🔍 **RESEARCH POSSIBLE**

**Publisher:** Bloomberg L.P.  
**Country:** United States  
**Coverage:** Business, finance, markets, politics  
**Expected Overlap:** MEDIUM-HIGH for business/political events

**Known RSS Endpoints:**
- ❓ `https://www.bloomberg.com/...` - Subscription site
- ❓ Bloomberg typically requires subscription for full content
- ⚠️ May not offer free public RSS

**Priority:** **MEDIUM** - Valuable but may have paywall/subscription barriers

---

### 5. United Press International (UPI)
**Status:** ✅ **LIKELY AVAILABLE**

**Publisher:** UPI (independent news agency)  
**Country:** United States  
**Coverage:** US + international news  
**Expected Overlap:** MEDIUM - smaller operation than Reuters/AP

**Known RSS Endpoints:**
- 🔍 `https://www.upi.com/rss/...` - Site has RSS links
- Likely publicly available

**Validation Steps:**
```bash
curl -v "https://www.upi.com/rss/news" \
  -H "User-Agent: CrossLens/0.0.14-beta (Android)"
```

**Priority:** **MEDIUM** - Good backup if Reuters/AP unavailable

## Research Methodology

### Phase 1: Discovery (1-2 hours)

**For each wire service:**

1. **Check main website** for RSS icons/links
2. **Search for** `"[service name] RSS feed"` + `"public"` / `"free"`
3. **Try common RSS patterns:**
   - `/rss`, `/feed`, `/rss.xml`
   - `/rss/[category]`
   - `feeds.[domain].com`
4. **Check robots.txt** for feed URLs: `curl https://example.com/robots.txt`
5. **Review terms of service** for RSS/content use policies

### Phase 2: Validation (30 min per source)

**For each discovered feed:**

```bash
# 1. Test accessibility
curl -v "FEED_URL" \
  -H "User-Agent: CrossLens/0.0.14-beta (Android)" \
  -o feed_test.xml

# 2. Check format
head -50 feed_test.xml

# 3. Validate structure
grep -E "<rss|<item>|<title>|<link>|<pubDate>" feed_test.xml

# 4. Check for HTTPS links
grep "<link>" feed_test.xml | head -10

# 5. Verify recent articles
grep "<pubDate>" feed_test.xml | head -10
```

**Validation Checklist:**
- ✅ Returns HTTP 200
- ✅ Valid RSS 2.0 XML format
- ✅ Contains `<item>` entries
- ✅ Items have title, link, pubDate, description
- ✅ Links are HTTPS
- ✅ pubDate values are recent (within 24 hours)
- ✅ No authentication required
- ✅ Fetch completes in < 10 seconds
- ✅ Content use terms allow link-and-excerpt

### Phase 3: Terms Review (30 min per source)

**For each validated feed:**

1. **Locate terms of service** - Usually in footer or `/terms`
2. **Check for RSS-specific terms** - Some sites have separate RSS terms
3. **Identify restrictions:**
   - ❌ "No aggregation"
   - ❌ "No redistribution"
   - ❌ "Subscribers only"
   - ❌ "Attribution required in specific format"
4. **Document permission basis:**
   - ✅ RSS feed is publicly accessible
   - ✅ Link-and-excerpt permitted
   - ✅ Attribution requirements documented
   - ✅ No technical restrictions violated

## Expected Outcomes

### Best Case Scenario
- ✅ Reuters RSS: Public, HTTPS, multiple topics
- ✅ AP RSS: Public, HTTPS, world news
- ✅ AFP RSS: Public, HTTPS, English available
- **Result:** 3 major wire services added → 25 total sources → High 3+ source clustering

### Realistic Scenario
- ✅ Reuters RSS: Public OR UPI RSS as alternative
- ⚠️ AP RSS: Partner-only (unavailable)
- ⚠️ AFP RSS: Subscription required (unavailable)
- **Result:** 1-2 wire services added → 23-24 total sources → Moderate 3+ source clustering

### Worst Case Scenario
- ❌ Reuters: Subscription/agency only
- ❌ AP: Partner agreements only
- ❌ AFP: No public RSS
- ✅ UPI: Available as fallback
- **Result:** 1 smaller wire service → 23 total sources → Focus on non-wire expansion

## Alternatives if Wire Services Unavailable

If major wire services don't offer public RSS, prioritize these **high-overlap alternatives:**

### Option A: Major International Newspapers (Already Validated)
- ✅ The Washington Post (added in Batch 1)
- ✅ The New York Times (already present)
- ✅ The Guardian (already present)
- 🔍 Financial Times - Business/international focus
- 🔍 The Independent (UK) - International coverage

### Option B: Additional Public Broadcasters
- 🔍 NHK World (Japan) - Asia-Pacific + international
- 🔍 RTE News (Ireland) - European + international
- 🔍 TRT World (Turkey) - Middle East + international
- 🔍 ABC News (US) - US + major international events

### Option C: Regional Clusters
Focus on events likely covered by existing sources:
- Asia-Pacific cluster: ABC AU, Straits Times, Hindu, Japan Times, Channel NewsAsia
- Europe cluster: BBC, Guardian, Spiegel, DW, France24, Le Monde
- Middle East cluster: Al Jazeera, Arab News
- North America cluster: NYT, WashPost, CBC

## Implementation Plan

### Step 1: Research Wire Services (2-3 hours)
- Search for Reuters, AP, AFP public RSS feeds
- Document URLs, formats, terms
- Validate accessibility and content

### Step 2: Validate Feeds (1-2 hours)
- cURL tests for each discovered feed
- Format and content validation
- Terms of service review

### Step 3: Integration Decision (30 min)
- **If 2+ wire services available:** Add immediately after source health fix
- **If 1 wire service available:** Add + focus on high-overlap alternatives
- **If 0 wire services available:** Proceed with Option A/B alternatives

### Step 4: Add to Configuration (30 min)
```kotlin
// In RssSourceAdapter.kt
RssSourceAdapter(
    sourceId = "reuters-rss",
    sourceName = "Reuters",
    feedUrl = "https://www.reuters.com/rssFeed/worldNews", // If validated
    httpClient = httpClient,
    healthMonitor = healthMonitor
),
```

## Success Criteria

**Minimum acceptable outcome:**
- ✅ At least 1 wire service OR 2 high-overlap alternatives identified and validated
- ✅ RSS feeds are public, HTTPS, RSS 2.0 format
- ✅ Content use terms permit link-and-excerpt
- ✅ Feeds return recent articles (within 24h)
- ✅ No authentication or subscription required

**Ideal outcome:**
- ✅ 2-3 major wire services (Reuters, AP, AFP) with public RSS
- ✅ Multiple topic categories available
- ✅ Clear attribution requirements documented
- ✅ High expected event overlap with existing sources

## Risk Assessment

**Low Risk:**
- UPI likely has public RSS
- Many newspapers offer RSS (fallback options exist)
- Worst case: proceed without wire services but with more newspapers

**Medium Risk:**
- Major wire services may require partnership/subscription
- Terms may prohibit aggregation
- Rate limiting may be strict

**High Risk:**
- Proceeding to Batch 2 without validating wire services first
- Adding sources with unclear terms
- Violating content use restrictions

## Timeline

- **Research:** 2-3 hours
- **Validation:** 1-2 hours
- **Terms review:** 1-2 hours
- **Total:** 4-7 hours (can run parallel with source health fixes)

## Blocked By

- None - Research can proceed immediately in parallel with Task 2 (source health fixes)

## Blocks

- Task 4 (Batch 2 expansion) - Need to know wire service availability before finalizing Batch 2 list

## Next Actions

1. ⏳ Search for Reuters public RSS feeds
2. ⏳ Search for AP public RSS feeds
3. ⏳ Validate any discovered feeds with cURL
4. ⏳ Review terms of service for RSS use
5. ⏳ Document findings in research summary
6. ⏳ Make recommendation: Wire services OR alternatives

## Conclusion

Wire services are CRITICAL for achieving 3+ source event clusters. Must research Reuters, AP, AFP availability before finalizing Batch 2 expansion plan. If wire services unavailable, will pivot to high-overlap international newspapers and public broadcasters as alternatives.

**Status:** Task 3 ready to begin (can run parallel with Task 2)  
**Next:** Begin wire service RSS feed discovery
