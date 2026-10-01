# Article Navigator Acceptance Audit

**Date:** 2026-09-30  
**Branch:** `feature/live-feed-v0.0.14-beta`  
**Commits Audited:** `4cd9989`, `6f345b6`, `2268e34`, `401035c`, `dd12945`  
**Status:** ❌ **FAILED - RELEASE BLOCKERS FOUND**

## Executive Summary

The article navigator implementation has **critical defects** that prevent release:

1. **❌ BLOCKER**: Gesture detection conflicts with article scrolling, making long articles unreadable
2. **❌ BLOCKER**: Device testing screenshots show wrong screen (EventComparisonScreen, not ArticleNavigatorScreen)
3. **❌ BLOCKER**: Zero unit test coverage for complex navigation logic
4. **⚠️ CONCERN**: Inconsistent eligibility threshold (2 vs 3 publishers) with Read Across Coverage

## Detailed Findings

### 1. Critical Bug: Gesture/Scroll Conflict ❌

**Location:** `ArticleNavigatorScreen.kt:155-210`

**Issue:** The parent Column applies `.pointerInput(navigationState)` with `detectDragGestures` that captures ALL vertical drag gestures, but the child article content has `.verticalScroll(rememberScrollState())` at line 293.

**Impact:**
- User attempts to scroll article content vertically
- Parent gesture detector consumes the drag event with `change.consume()`
- If drag exceeds 50px threshold, triggers feed navigation instead of scrolling
- **Makes it impossible to read articles longer than one screen**

**Evidence:**
```kotlin
// Line 155-158: Parent captures ALL gestures
Column(
    modifier = Modifier
        .fillMaxSize()
        .pointerInput(navigationState) {
            detectDragGestures(...) { change, dragAmount ->
                change.consume() // <-- Prevents child scroll
                // Line 170-208: Detects vertical swipes for feed navigation
```

```kotlin
// Line 289-293: Child expects to scroll
Column(
    modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState()) // <-- Can't scroll!
```

**Correct Implementation Options:**
1. Only detect gestures on non-scrollable areas (top bar, margins)
2. Use nested scroll cooperation (detect gestures only at scroll boundaries)
3. Require explicit swipe gesture (e.g., two-finger swipe) for feed navigation
4. Use horizontal-only gesture detection, remove vertical feed navigation

**Recommended Fix:** Option 2 - Detect vertical swipes only when article scroll is at top/bottom edge.

---

### 2. Critical Documentation Defect: Wrong Screenshots ❌

**Location:** `docs/screenshots/article-navigator/*.png`

**Issue:** All 6 screenshots labeled as "article navigator" actually show **EventComparisonScreen**, not ArticleNavigatorScreen.

**Evidence:**

| Screenshot | Expected (ArticleNavigator) | Actual (EventComparison) |
|------------|----------------------------|--------------------------|
| Top bar content | CrossLens signature + position ("1 of 12") | "Event Comparison" title + back arrow |
| Navigation | No back button, tap top to refresh | Back arrow to exit |
| Screen type | Full-screen article with swipe gestures | Stacked article cards |

**Screenshot Analysis:**
- `02_article_navigator_first.png`: Shows "Event Comparison" header at top
- `03_horizontal_swipe_next.png`: Shows "Event Comparison" header at top
- `04_vertical_swipe_next_story.png`: Shows "Event Comparison" header at top
- `05_tap_top_return_first.png`: Shows "Event Comparison" header at top
- `06_boundary_last_in_cluster.png`: Shows "Event Comparison" header at top

**Code Proof:**
```kotlin
// ArticleNavigatorScreen.kt:234-240 - Expected UI
CrossLensSignature(size = SignatureSize.Small)
Text(text = "${navigationState.currentFeedPosition + 1} of ${navigationState.totalFeedItems}")

// EventComparisonScreen.kt:51 - Actual screenshots
title = { Text("Event Comparison") }
```

**Impact:**
- Device testing never reached ArticleNavigatorScreen
- All documented "verification" is invalid
- Unknown if feature actually works on device
- Commit dd12945 claims "all requirements met" but provides no evidence

**Required Action:** Capture actual ArticleNavigatorScreen screenshots showing:
1. CrossLens signature + position indicator in top bar
2. Horizontal swipe between publishers in same cluster
3. Vertical swipe between different stories
4. Boundary feedback overlays
5. Single-source story behavior (no horizontal navigation)

---

### 3. Critical Gap: Zero Test Coverage ❌

**Location:** `app/src/test/java/com/crosslens/app/feature/articlenavigator/`

**Issue:** Test directory exists but is empty. **No unit tests** for ArticleNavigatorViewModel or navigation logic.

**Missing Coverage:**
- ✗ Feed position tracking (currentFeedIndex)
- ✗ Cluster position tracking (currentArticleIndex)
- ✗ Navigation state updates (hasNext/hasPrevious flags)
- ✗ Horizontal navigation eligibility (2+ distinct publishers)
- ✗ Boundary conditions (first/last in cluster/feed)
- ✗ Article loading and error states
- ✗ Feed refresh behavior
- ✗ Return-to-first navigation
- ✗ Story position reset when changing stories (vertical nav)
- ✗ Edge cases (empty feed, single-source stories, invalid IDs)

**Comparison:**
- Read Across Coverage: 19 unit tests
- Article Navigator: **0 unit tests**

**Required Tests:**
```kotlin
// Critical scenarios that MUST be tested:
- Navigate horizontal within 4-source cluster
- Navigate vertical across feed stories
- Reset to first article when changing stories
- Boundary detection (first/last in cluster)
- Boundary detection (first/last in feed)
- Single-source story (no horizontal nav)
- Two-source cluster (horizontal nav enabled)
- Invalid story ID (error state)
- Empty feed (error state)
- Return to first and refresh
```

**Impact:**
- No regression protection
- No proof of correctness
- Complex state machine logic untested
- High risk of bugs in production

---

### 4. Product Inconsistency: Eligibility Threshold ⚠️

**Location:** 
- `ArticleNavigatorViewModel.kt:125-138`
- `ReadAcrossCoverageRecommender.kt:27`

**Issue:** Article navigator allows horizontal navigation with **2 distinct publishers**, but Read Across Coverage requires **3 distinct publishers**.

**Evidence:**
```kotlin
// ArticleNavigatorViewModel.kt:125-138
val hasHorizontalNavigation = story.isEventCluster && currentClusterArticles.size >= 2
val distinctPublishers = currentClusterArticles.mapNotNull { it.metadata?.publisherName }.distinct().size
// Allows navigation with distinctPublishers >= 2

// ReadAcrossCoverageRecommender.kt:27
const val MIN_DISTINCT_PUBLISHERS = 3
```

**User Experience Impact:**
- 2-publisher event: horizontal swipes work in article navigator
- Same 2-publisher event: no Read Across Coverage recommendations shown
- Inconsistent definition of "confident event cluster"

**Documentation States:**
- ARTICLE_NAVIGATOR.md: "Only for confident event clusters with 2+ distinct publishers"
- Commit 4cd9989: "Read Across Coverage only appears for confident multi-source events" (requires 3)

**Recommended Resolution:**
Use **consistent threshold of 3 distinct publishers** for both features:
1. Update ArticleNavigatorViewModel.kt line 125: `>= 3` instead of `>= 2`
2. Update ARTICLE_NAVIGATOR.md: "3+ distinct publishers" instead of "2+"
3. Add test coverage for 2-publisher rejection and 3-publisher acceptance

**Alternative:** If 2 publishers is intentional for article navigator:
1. Document the difference explicitly
2. Explain why article nav has lower bar than Read Across Coverage
3. Update "confident event cluster" terminology to distinguish thresholds

---

### 5. Additional Code Issues

#### 5.1 Missing Reduced Motion Support
- **Location:** ArticleNavigatorScreen.kt:254-287
- **Issue:** AnimatedContent always uses slide/fade animations
- **Expected:** Respect system reduced-motion setting (accessibility)
- **Documentation Claims:** "Reduced Motion: Animations respect system reduced-motion settings (when implemented in future)"
- **Status:** Not implemented, but documentation suggests it should be

#### 5.2 Misleading Card Text
- **Location:** EventClusterCard.kt:123
- **Issue:** Card shows "Tap to compare coverage" text
- **Reality:** Card navigates to ArticleNavigatorScreen (full-screen reader), not comparison screen
- **Fix:** Update text to "Tap to read coverage" or "Tap to explore event"

#### 5.3 Hard-Coded Values
- **Location:** ArticleNavigatorScreen.kt
- **Issue:** Magic numbers throughout (50px threshold, 300ms animation, 1000ms auto-dismiss, 72dp top bar)
- **Better:** Extract to constants with documented rationale

---

## Navigation Integration Verification ✅

**Status:** PASS

Navigation is correctly wired:
- HomeScreen routes to `CrossLensDestination.ArticleNavigator.createRoute(storyId)`
- Both EventClusterCard and StoryCard call `onArticleNavigatorClick(story.id)`
- ArticleNavigatorScreen properly registered in NavHost with storyId + optional articleId params
- Navigation arguments correctly parsed in composable

**Code Evidence:**
```kotlin
// CrossLensNavHost.kt:46-48
onArticleNavigatorClick = { storyId ->
    navController.navigate(CrossLensDestination.ArticleNavigator.createRoute(storyId))
}

// CrossLensNavHost.kt:129-147
composable(
    route = CrossLensDestination.ArticleNavigator.route,
    arguments = listOf(...)
) { ... }

// HomeScreen.kt:283-292
items(state.stories) { story ->
    if (story.isEventCluster) {
        EventClusterCard(story = story, onClick = { onArticleNavigatorClick(story.id) })
    } else {
        StoryCard(story = story, onClick = { onArticleNavigatorClick(story.id) })
    }
}
```

---

## Gesture Implementation Review ✅ (with blocker)

**Status:** Partially correct, but BLOCKED by scroll conflict

**Correctly Implemented:**
- ✅ detectDragGestures API used properly
- ✅ 50px threshold for swipe detection
- ✅ Primary direction detection (horizontal vs vertical)
- ✅ Direction-aware animations (LEFT/RIGHT/UP/DOWN)
- ✅ Boundary feedback with auto-dismiss
- ✅ Navigation state checks before triggering nav
- ✅ Gesture consumed to prevent propagation

**Issues:**
- ❌ Conflicts with nested vertical scroll (BLOCKER)
- ⚠️ No gesture disambiguation (scroll vs swipe intent)
- ⚠️ No visual feedback during drag (only on completion)

---

## Build Verification ✅

**Status:** PASS

```bash
$ ./gradlew :app:assembleDebug
BUILD SUCCESSFUL in 843ms

$ ./gradlew :app:testDebugUnitTest
BUILD SUCCESSFUL in 21s
# Note: 0 article navigator tests ran

$ ls -lh app/build/outputs/apk/debug/app-debug.apk
-rw-r--r-- 1 matt staff 61M Sep 30 18:32 app-debug.apk
```

---

## Product Requirements Verification

From user request:

| Requirement | Status | Evidence |
|-------------|--------|----------|
| Horizontal swipes move only across articles in same confident event cluster | ⚠️ Implemented but wrong threshold (2 vs 3) | ViewModel:125-138 |
| Vertical swipes move through feed sequence | ❌ BLOCKED - conflicts with scroll | Screen:189-208 |
| Top navigation reset refreshes and returns to first article | ❓ Untested, no screenshots | Screen:212-244 |
| Boundary behavior clear, no unexpected wrap | ❓ Untested, no screenshots | Screen:248-251 |
| Attribution, links, timestamps, language, images, transparency intact | ❓ Untested, no screenshots | Screen:300-437 |
| Non-clustered and two-source articles cannot horizontally navigate into unrelated content | ⚠️ Two-source CAN navigate (threshold=2) | ViewModel:138 |
| Gestures work with accessibility and reduced-motion settings | ❌ Reduced motion not implemented | Missing |

---

## Release Blockers Summary

### Must Fix Before Merge:

1. **Fix gesture/scroll conflict** - Article content must be readable
2. **Add unit test coverage** - Minimum 10 tests for navigation logic
3. **Capture actual screenshots** - Prove ArticleNavigatorScreen works on device
4. **Resolve eligibility threshold** - Align with Read Across Coverage (3 publishers) or document difference

### Should Fix:

5. Implement reduced-motion accessibility support
6. Update EventClusterCard text ("compare" → "read")
7. Extract hard-coded constants
8. Add gesture visual feedback (swipe preview)

---

## Recommended Actions

### Immediate (Before Merge):

```kotlin
// 1. Fix scroll conflict in ArticleNavigatorScreen.kt
// Remove .pointerInput from outer Column
// Add gesture detection only to top bar and bottom hints

// 2. Add ArticleNavigatorViewModelTest.kt
class ArticleNavigatorViewModelTest {
    @Test fun `horizontal navigation requires 2+ distinct publishers`()
    @Test fun `vertical navigation resets to first article in story`()
    @Test fun `boundary detection prevents invalid navigation`()
    // ... 7 more critical tests
}

// 3. Manually test on Pixel device
- Install debug APK
- Tap event cluster card from home feed
- Verify CrossLens signature + position indicator in top bar
- Test horizontal swipes (Financial Times → France 24 → ...)
- Test vertical swipes (Paramount story → next story)
- Test article scrolling (must work without triggering navigation)
- Capture screenshots of actual ArticleNavigatorScreen

// 4. Decide eligibility threshold
Option A: Change to 3 publishers for consistency
Option B: Keep 2 publishers, document rationale
```

### Follow-Up (After Merge):

5. Implement reduced-motion support with LocalAccessibilityManager
6. Add swipe preview/peek gesture for better UX
7. Add preloading for next/previous articles
8. Add instrumentation tests for navigation flows
9. Profile gesture performance and animation smoothness

---

## Audit Conclusion

**Status:** ❌ **NOT READY FOR RELEASE**

The article navigator implementation has critical defects:
- Broken article scrolling (can't read long articles)
- Invalid device testing evidence (wrong screenshots)
- No test coverage for complex navigation logic
- Product inconsistency with related features

**Cannot merge** until blockers are resolved with verified fixes and actual device evidence.

---

## Audit Artifacts

- **Code Review:** Complete
- **Documentation Review:** Complete
- **Screenshot Analysis:** Complete
- **Test Coverage Analysis:** Complete
- **Build Verification:** Complete
- **Device Testing:** ❌ Invalid (wrong screen captured)

---

**Auditor:** Claude Sonnet 4.5  
**Date:** 2026-09-30  
**Next Review:** After fixes applied and new screenshots captured
