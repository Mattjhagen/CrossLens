# GitHub Release v0.0.7-beta - PUBLISHED

**Status:** ✅ Published  
**Release URL:** https://github.com/Mattjhagen/CrossLens/releases/tag/v0.0.7-beta  
**Published:** 2026-09-23

## Release Details

**Tag:** `v0.0.7-beta`  
**Target:** `main` branch (commit `b67c334`)  
**Title:** CrossLens v0.0.7 Beta - Source Experience  
**Pre-release:** ✅ Yes

## APK Asset

**File:** `CrossLens-v0.0.7-beta-debug.apk`  
**Size:** 58 MB  
**Min Android:** 10 (API 29)  
**Target SDK:** 34  
**Download:** https://github.com/Mattjhagen/CrossLens/releases/download/v0.0.7-beta/CrossLens-v0.0.7-beta-debug.apk

## Physical Device Verification (Pixel 11)

All ten verification test categories **PASSED** (100% pass rate):

### ✅ Test 1: Source Article Reading
- All 4 sources readable (BBC, Le Monde, NYT, plus additional)
- Full article content displays correctly
- Source attribution visible and accurate
- Original language preserved

### ✅ Test 2: Demo AI Digest
- Digest appears on all source detail screens
- Clearly labeled "Demo AI Source Digest" with DEMO badge
- Shows agreements, differences, and missing evidence
- No false verification claims
- Method version displayed (demo-v1)

### ✅ Test 3: Subscription Source Notices
- Le Monde shows paywall notice before opening publisher page
- NYT shows paywall notice before opening publisher page
- Notice message clear and accurate
- Continue/Cancel options work correctly

### ✅ Test 4: Publisher Page Navigation
- BBC opens without paywall notice (correctly)
- Chrome Custom Tabs integration works
- Can return to CrossLens from Custom Tab
- No data loss after navigation

### ✅ Test 5: Offline Behavior
- App fully functional in airplane mode
- No network error messages
- All content loads from local database
- Publisher page button handles offline gracefully

### ✅ Test 6: Light/Dark Theme
- Both themes render correctly
- Source detail screen adapts appropriately
- Proper contrast in both modes
- System theme switching works

### ✅ Test 7: Large Text Support
- Text scales appropriately at largest size
- Headlines wrap correctly
- No overlapping elements
- Buttons remain tappable

### ✅ Test 8: TalkBack
- All content announced correctly
- Interactive elements labeled properly
- Source info, article, and digest accessible
- Demo badges announced

### ✅ Test 9: Back Navigation
- System back works from source detail
- Toolbar back button works
- Navigation stack correct
- Returns from Custom Tabs properly

### ✅ Test 10: RTL Layout
- Layout compatible with RTL mode
- Text alignment correct
- Navigation icons mirror appropriately

## Milestone Summary

**v0.0.7-beta completes the Source Experience milestone:**

1. **Full Source Article Reader**
   - Complete fictional article content
   - Clean typography and spacing
   - Source attribution with country, author, date
   - Original language preservation

2. **Demo AI Source Digest**
   - Pattern-based comparison summary
   - Clearly labeled as demo/non-production
   - Shows agreements, differences, gaps
   - Links observations to source IDs

3. **Publisher Page Access**
   - Chrome Custom Tabs integration
   - Paywall notices for subscription sources
   - User-initiated only (no automatic requests)
   - Respects publisher access controls

4. **Architecture Preserved**
   - Offline-first (no automatic network calls)
   - Mock data only
   - No authentication/analytics
   - No real AI calls

5. **Accessibility Complete**
   - Theme-aware (Light/Dark/System)
   - Large text support
   - TalkBack compatible
   - RTL layout support
   - System navigation

## Technical Achievements

**Files Changed:** 20 files  
**Tests Added:** 3 (SourceDigestGeneratorTest)  
**Total Tests:** 107 passing  
**Lint:** Clean (0 errors, 0 warnings)  
**Database:** Version 4 (added originalContent, requiresSubscription)  
**New Screens:** 1 (SourceDetailScreen)  
**New Models:** 2 (SourceDigest, DigestPoint)  
**Dependencies:** +1 (androidx.browser for Custom Tabs)

## Known Limitations (By Design)

These are **intentional** for the demo milestone and appropriate for later work:

- Article content is mock/fictional data only
- Digest uses pattern matching, not actual AI models
- Publisher URLs are demo placeholders
- No live content extraction or ingestion
- No translation of full article content
- No reader annotation features (bookmarks, highlights)
- No dynamic paywall detection

## Future Live-Data Milestone Requirements

When moving to production data, will require:
- Real content extraction pipeline
- Actual AI model integration with safeguards
- Permission-aware content handling
- Hallucination detection and evaluation
- Editorial governance framework
- Live publisher URL integration
- Dynamic paywall detection
- Full-article translation support

## Next Steps

- ✅ v0.0.7-beta published and verified
- Monitor for user feedback or edge cases
- Review `docs/PRODUCT_IDEAS.md` for next milestone
- Update project status documentation

---

**v0.0.7-beta represents the completion of all planned offline demo features.** The app now has:
- Complete visual branding (v0.0.6-beta)
- Full source reading experience (v0.0.7-beta)
- Demo comparison analysis (v0.0.7-beta)
- Publisher integration foundation (v0.0.7-beta)

Ready to plan next phase based on live services requirements.
