# Batch 2 Device Testing Plan
**Date:** September 30, 2026  
**Build:** v0.0.14-beta + Batch 2 (commit ff4a319)  
**Device:** Pixel 11 or emulator  
**Status:** Ready for testing

## Pre-Testing Setup

### 1. Connect Device
```bash
adb devices
# Should show: 66020DLKY0006U device (or similar)
```

### 2. Install Latest APK
```bash
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### 3. Launch App
```bash
adb shell am start -n com.crosslens.app.debug/.MainActivity
```

## Testing Objectives

### Primary Objective
✅ **Find and verify a natural three-source event cluster from live RSS data**

### Secondary Objectives
1. Verify all 23 sources are fetching successfully
2. Confirm "Read Across Coverage" feature appears for 3+ source clusters
3. Validate recommendation quality (same event, not false matches)
4. Test user-facing flow from home → event comparison → read across
5. Capture screenshot evidence

## Test Procedure

### Phase 1: Verify Source Health (5 minutes)

**Step 1.1: Clear app data and cold start**
```bash
adb shell pm clear com.crosslens.app.debug
adb shell am start -n com.crosslens.app.debug/.MainActivity
```

**Step 1.2: Trigger feed refresh**
- Wait for automatic refresh OR
- Pull-to-refresh on home screen

**Step 1.3: Check logcat for source health**
```bash
adb logcat | grep -E "RssSourceAdapter|SourceHealth|FAILED|SUCCESS"
```

**Expected Results:**
- ✅ 23 sources attempt fetch
- ✅ 20-23 sources return articles (target: 90%+)
- ✅ New sources visible: UPI, Financial Times, El País

**Acceptance Criteria:**
- [ ] No crashes during feed refresh
- [ ] At least 20/23 sources successful (87%+)
- [ ] Feed populated with articles from multiple sources
- [ ] "Live Feed • Last refreshed: Just now • X sources" shows 20-23 active

### Phase 2: Find Three-Source Clusters (10-15 minutes)

**Step 2.1: Review home feed for multi-source indicators**
- Scroll through story cards
- Look for "→ 3 sources" or "→ 4 sources" indicators
- Prioritize major international events (political, economic, disasters)

**Step 2.2: Identify candidate clusters**

**High-Probability Event Types:**
- US political news (UPI, NYT, WashPost, BBC, Guardian, FT)
- Major international events (BBC, Guardian, DW, France24, FT, NYT, Al Jazeera)
- Business/economic news (FT, NYT, WashPost, BBC, Guardian)
- European events (FT, BBC, Guardian, DW, France24, Irish Times, Le Monde, El País)

**Current Date:** September 30, 2026  
**Likely Events:** Check headlines for:
- US politics/elections
- International conflicts (Iran, Middle East)
- Economic/market news
- Technology/AI regulation
- Climate/environment

**Step 2.3: Document found clusters**

For each multi-source cluster found, record:
- Event title
- Number of sources
- Source list (publisher names)
- Common entities identified
- Time spread (hours between earliest/latest article)

**Minimum Success Criteria:**
- [ ] Find at least ONE cluster with 3+ distinct publishers
- [ ] All publishers in cluster are independent (not wire service + syndication)
- [ ] Event is clearly the same across all sources

### Phase 3: Verify Event Comparison Screen (5 minutes)

**Step 3.1: Tap on a 3+ source cluster**

**Expected UI:**
- Event comparison screen loads
- Header shows: "X sources reporting this event"
- Coverage gap notice appears
- Each article card shows:
  - Publisher name
  - Country • Language
  - "Why this appears" explanation
  - Article headline
  - Excerpt
  - Original article link

**Step 3.2: Verify clustering accuracy**

**Manual Audit Checklist:**
- [ ] All articles are about the SAME specific event
- [ ] No false matches (different events with similar keywords)
- [ ] Headlines describe same story
- [ ] Time proximity reasonable (within 24-72 hours)
- [ ] Sources are independent publishers

**Step 3.3: Check coverage metadata**

**Verification:**
- [ ] Coverage gap notice accurate (language/publisher count correct)
- [ ] "Why this appears" explanations factual (no ideology claims)
- [ ] Source metadata correct (country, language)
- [ ] Editorial descriptions present with provenance
- [ ] "About This Comparison" section explains clustering

### Phase 4: Test "Read Across Coverage" Feature (5-10 minutes)

**Step 4.1: Look for action card**

**Expected UI (if 5+ total sources in cluster):**
```
┌─────────────────────────────────────────────────────┐
│ Read across coverage                                │
│ X additional sources covering this event      [View]│
└─────────────────────────────────────────────────────┘
```

**Visibility Rules:**
- Feature only appears when additional sources exist beyond those shown
- Initial event comparison shows 2-3 sources
- "Read Across Coverage" recommends 2-4 additional sources
- Total sources in cluster must be 5+ for feature to appear

**Step 4.2: Tap "View" to open recommendations**

**Expected Bottom Sheet:**
- Sheet title: "Read across coverage"
- Explanation text about source diversity
- 2-4 article cards with:
  - Publisher name, country, language
  - "Why recommended" chip (e.g., "Reporting from X", "Different country", "Public broadcaster")
  - Article headline
  - Excerpt
- "About these recommendations" footer
- No ideology/bias labels
- No "truthfulness" claims

**Step 4.3: Verify recommendation quality**

**Manual Audit:**
- [ ] All recommended articles are from the SAME event
- [ ] No false recommendations (different events)
- [ ] Recommendations add diversity (different countries/languages/types)
- [ ] "Why recommended" reasons are factual and documented
- [ ] No circular reasoning ("recommended because diverse" without specifics)

**Step 4.4: Test article links**

- [ ] Tap article card to navigate to original source
- [ ] Link opens correctly (external browser or WebView)
- [ ] URL is HTTPS
- [ ] Attribution visible (source name + link)

### Phase 5: Screenshot Evidence (5 minutes)

**Required Screenshots:**

**Screenshot 1: Home Feed with 3+ Source Cluster**
- Filename: `batch2_home_3source_cluster.png`
- Shows: Story card with "→ 3 sources" (or higher) indicator
- Visible: Event title, timestamp, source count

**Screenshot 2: Event Comparison Screen (3+ sources)**
- Filename: `batch2_event_comparison_3sources.png`
- Shows: Full event comparison with 3+ article cards
- Visible: Header ("X sources"), coverage gap, article cards with metadata

**Screenshot 3: Read Across Coverage (if available)**
- Filename: `batch2_read_across_coverage.png`
- Shows: Bottom sheet with recommendations
- Visible: Recommendation cards, "why recommended" chips, footer text

**Screenshot 4: Source Health Status**
- Filename: `batch2_source_health.png`
- Shows: Feed status footer showing active source count
- Text: "Live Feed • Last refreshed: X • Y sources"

**Capture Commands:**
```bash
# Capture screenshot
adb shell screencap -p /sdcard/screenshot.png
adb pull /sdcard/screenshot.png docs/screenshots/batch2/

# Or use scrcpy for real-time viewing
scrcpy --record=docs/screenshots/batch2/recording.mp4
```

## Test Data Collection

### Clustering Audit Log

For the verified 3+ source cluster, document:

```markdown
## Three-Source Cluster Verification

**Event Title:** [e.g., "Supreme Court ruling on deportations"]

**Date/Time:** [September 30, 2026, HH:MM PDT]

**Sources in Cluster:**
1. [Publisher Name] ([Country] • [Language])
   - Headline: "[exact headline]"
   - Published: [timestamp]
   - URL: [article URL]

2. [Publisher Name] ([Country] • [Language])  
   - Headline: "[exact headline]"
   - Published: [timestamp]
   - URL: [article URL]

3. [Publisher Name] ([Country] • [Language])
   - Headline: "[exact headline]"
   - Published: [timestamp]
   - URL: [article URL]

[+ additional sources if > 3]

**Clustering Metadata:**
- Common entities: [e.g., "Supreme Court", "Trump", "deportation"]
- Time spread: [X hours between earliest and latest]
- Confidence level: [MEDIUM or HIGH]
- Grouping explanation: "[from UI]"

**Verification:**
- [ ] ✅ Same specific event across all sources
- [ ] ✅ No false matches
- [ ] ✅ Publishers are independent
- [ ] ✅ Factual metadata only (no ideology labels)
- [ ] ✅ Coverage gap notice accurate
- [ ] ✅ "Why this appears" explanations factual

**Read Across Coverage (if available):**
- Appeared: [Yes/No]
- Recommendations: [X additional sources]
- Quality: [All same event / Some false / N/A]
```

## Acceptance Criteria

### Must Pass (Blockers)

- [ ] **At least ONE natural 3+ source cluster found in live data**
- [ ] **All sources in cluster are same event (100% precision)**
- [ ] **Event comparison screen loads and displays correctly**
- [ ] **No false matches (different events clustered together)**
- [ ] **No crashes during testing**
- [ ] **Source health > 87% (20+/23 sources active)**

### Should Pass (Important)

- [ ] "Read Across Coverage" appears for suitable clusters
- [ ] Recommendations are same event (if feature appears)
- [ ] All metadata is factual (no ideology/bias labels)
- [ ] Coverage gap notices accurate
- [ ] Original article links work
- [ ] Screenshots captured

### Nice to Have

- [ ] Multiple 3+ source clusters found
- [ ] 4+ source cluster found
- [ ] Read Across Coverage tested end-to-end
- [ ] Cross-language clustering observed (Spanish + English)
- [ ] Business news clustering (FT involvement)

## Troubleshooting

### Issue: No 3+ Source Clusters Found

**Diagnosis:**
1. Check how many total sources are active
   ```bash
   adb logcat | grep "articles returned" | wc -l
   ```
2. Check if articles are being clustered at all
   ```bash
   adb logcat | grep "Clustered articles"
   ```
3. Review recent headlines - are there major events that multiple sources should cover?

**Possible Causes:**
- Too few sources active (< 20)
- No major breaking news today
- Event clustering thresholds too conservative
- New sources haven't had time to fetch

**Mitigations:**
- Wait for breaking news cycle
- Try again in 1-2 hours
- Check specific major event coverage manually

### Issue: False Matches in Cluster

**Diagnosis:**
- Manually review headlines
- Check common entities
- Verify event details

**Action:**
- Document the false match
- Report as clustering algorithm issue
- Do NOT count as successful 3+ cluster

### Issue: Sources Not Fetching

**Diagnosis:**
```bash
adb logcat | grep -E "FAILED|HTTP [4-5][0-9][0-9]"
```

**Common Causes:**
- Network connectivity
- Rate limiting
- Feed temporarily unavailable

**Action:**
- Note which sources failed
- Retry feed refresh
- Document persistent failures

## Success Metrics

**Quantitative:**
- Sources active: X / 23 (target: ≥ 20)
- 3+ source clusters found: X (target: ≥ 1)
- Clustering precision: X% (target: 100%)
- Read Across Coverage accuracy: X% (target: 100% if tested)

**Qualitative:**
- Clustering quality: [Excellent / Good / Fair / Poor]
- User experience: [Smooth / Acceptable / Needs work]
- Metadata quality: [Accurate / Mostly accurate / Inaccurate]

## Next Steps After Testing

### If Successful (≥1 verified 3+ cluster)
1. ✅ Mark Task 5 complete
2. ✅ Document findings in BATCH2_LIVE_VERIFICATION.md
3. ✅ Proceed to Task 6 (Final documentation)
4. ✅ Consider Batch 3 expansion (optional)

### If Unsuccessful (no 3+ clusters)
1. ⚠️ Document findings
2. ⚠️ Analyze why (too few sources, no major events, thresholds)
3. ⚠️ Decide: Wait for news cycle OR Add more sources OR Adjust thresholds
4. ⚠️ Do NOT loosen clustering just to produce 3+ clusters

## Reference

**Current Configuration:**
- Total sources: 23 active (26 configured, 3 disabled)
- Batch 2 additions: UPI, Financial Times, El País
- Expected overlap scenarios documented in BATCH2_VALIDATION_RESULTS.md
- Previous verification: 3 two-source clusters (Sept 29)

**Known Working Clusters (Sept 29):**
- Netanyahu story (Straits Times + Arab News)
- Supreme Court deportations (France 24 + Arab News) ← **UPI also had this story!**
- Morocco PM (Washington Post + Deutsche Welle)

**High-Probability Three-Source Scenarios:**
- Supreme Court deportations: UPI + France 24 + Arab News
- Major US political event: UPI + NYT + WashPost (+FT, BBC)
- International crisis: BBC + Guardian + DW + France24 + FT
- Business/economic: FT + NYT + WashPost + BBC

## Conclusion

This test plan provides systematic verification of Batch 2 source expansion and three-source event clustering. Success requires finding at least one natural 3+ source cluster with 100% precision (no false matches). Follow the procedure, document findings thoroughly, and capture screenshot evidence.

**Testing Window:** Available anytime device is connected  
**Estimated Duration:** 30-40 minutes  
**Prerequisites:** Latest debug APK installed, device connected  
**Output:** Screenshots + verification document + success/failure determination
