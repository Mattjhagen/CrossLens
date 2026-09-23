# CrossLens Physical Device Acceptance Tests

**Device:** Physical Pixel  
**APK:** v0.0.2-beta (app-debug.apk)  
**Date:** 2026-09-22

## Prerequisites

- [ ] Install v0.0.2-beta APK on physical Pixel
- [ ] Device fully charged or plugged in
- [ ] Screenshots folder ready: `docs/screenshots/device-tests/`

## Test 1: Large Font Scaling

**Objective:** Verify app remains usable at maximum font size without clipping, overlap, or inaccessible controls.

### Setup
1. Open device Settings → Display → Font size
2. Set to largest available size
3. Also check Settings → Display → Display size (set to largest)
4. Return to Home screen

### Test Steps

**Home Screen:**
- [ ] Launch CrossLens
- [ ] Verify story titles are fully readable (no clipping)
- [ ] Verify story summaries wrap correctly
- [ ] Verify metadata (sources, dates, Lens Gap) displays without overlap
- [ ] Tap "Mock Edition · Demo" text - should be touchable
- [ ] Scroll through story list - no content truncated
- [ ] Screenshot: `01_home_large_font.png`

**Story Screen:**
- [ ] Tap any story
- [ ] Verify headline readable and wrapped
- [ ] Verify summary text readable
- [ ] Verify "Compare perspectives" button fully visible and tappable
- [ ] Verify Sources section labels readable
- [ ] Scroll through entire story - no clipping
- [ ] Screenshot: `02_story_large_font.png`

**CrossLens Comparison:**
- [ ] Tap "Compare perspectives"
- [ ] Verify source tabs readable and tappable
- [ ] Verify article content readable
- [ ] Verify prev/next buttons accessible
- [ ] Verify back button tappable
- [ ] Screenshot: `03_crosslens_large_font.png`

**Explore Screen:**
- [ ] Navigate back to Home
- [ ] Tap Explore icon (top left compass)
- [ ] Verify filter chips readable and tappable
- [ ] Verify "Clear all filters" visible when filters active
- [ ] Verify story cards readable
- [ ] Screenshot: `04_explore_large_font.png`

**Settings Screen:**
- [ ] Navigate to Settings (gear icon)
- [ ] Verify all section headers readable
- [ ] Verify theme selection labels readable (System/Light/Dark)
- [ ] Verify selected theme clearly indicated
- [ ] Verify "Reduced motion" toggle label readable
- [ ] Verify "Preview Plus" card text readable
- [ ] Verify "Reset to Free" button visible and tappable
- [ ] Screenshot: `05_settings_large_font.png`

**Paywall:**
- [ ] Return to Home
- [ ] Tap Explore icon, select "All sources" filter (triggers paywall)
- [ ] Verify paywall headline readable
- [ ] Verify feature list readable
- [ ] Verify "Preview Plus" button tappable
- [ ] Verify "Not now" button tappable
- [ ] Screenshot: `06_paywall_large_font.png`

### Findings
Record any issues:
- Clipped text:
- Overlapping controls:
- Inaccessible buttons:

---

## Test 2: TalkBack Accessibility

**Objective:** Verify screen reader provides useful navigation, clear labels, and logical reading order.

### Setup
1. Reset font size to default first
2. Enable TalkBack: Settings → Accessibility → TalkBack → Toggle ON
3. Tutorial will appear - complete or skip
4. TalkBack gestures:
   - Swipe right: Next item
   - Swipe left: Previous item
   - Double-tap: Activate
   - Two-finger swipe down: Dismiss/back

### Test Steps

**Home Screen Navigation:**
- [ ] Launch CrossLens with TalkBack active
- [ ] Listen to first focused element - should be meaningful
- [ ] Swipe right through elements in logical order:
  - Explore button with clear label
  - Settings button with clear label
  - "CrossLens" heading
  - "Mock Edition · Demo" labeled
  - First story title
  - Story summary
  - Story metadata (sources, date, Lens Gap)
- [ ] Verify no "unlabeled button" or "button" without description
- [ ] Double-tap a story - should navigate to Story screen
- [ ] Screenshot: `07_talkback_home.png` (optional - TalkBack UI visible)

**Story Screen:**
- [ ] Verify back button labeled "Navigate up" or similar
- [ ] Swipe through: headline, summary, "Compare perspectives" button
- [ ] Verify Sources section announces as heading
- [ ] Verify each source link has clear label (outlet name + "Demo link")
- [ ] Double-tap back button - should return to Home

**Settings Screen:**
- [ ] Navigate to Settings
- [ ] Verify section headers announced as headings
- [ ] Swipe to theme options:
  - Verify "System", "Light", "Dark" labeled clearly
  - Verify current selection state announced ("selected" or checked state)
- [ ] Swipe to "Reduced motion" toggle
  - Verify label clear
  - Verify switch state announced (on/off)
- [ ] Verify "Preview Plus" card content readable
- [ ] Verify navigation remains logical top-to-bottom

**Explore & Paywall:**
- [ ] Navigate to Explore
- [ ] Verify filter chips announced with labels
- [ ] Trigger paywall by selecting Plus-only filter
- [ ] Verify paywall content readable in order
- [ ] Verify "Preview Plus" and "Not now" buttons clearly labeled
- [ ] Double-tap "Not now" - should dismiss

### Findings
Record any issues:
- Unlabeled buttons/icons:
- Poor reading order:
- Missing state announcements:
- Inaccessible interactions:

### Cleanup
Disable TalkBack: Settings → Accessibility → TalkBack → Toggle OFF

---

## Test 3: RTL (Arabic) Layout

**Objective:** Verify Arabic text displays correctly and layout mirrors appropriately.

### Setup
1. Device Settings → System → Languages & input → Languages
2. Add Arabic (العربية) if not present
3. Move Arabic to top of list (becomes primary)
4. Device UI should mirror (back button on right, etc.)

### Test Steps

**Home Screen:**
- [ ] Launch CrossLens
- [ ] Verify layout mirrors (Settings icon on left, Explore on right)
- [ ] Verify story cards readable
- [ ] Check if any Arabic content in mock stories displays properly
- [ ] Verify no text overflow or reversed punctuation issues
- [ ] Screenshot: `08_rtl_home.png`

**Story Screen:**
- [ ] Open a story
- [ ] Verify back button on right side
- [ ] Verify headline/summary readable if Arabic
- [ ] Verify "Compare perspectives" button mirrored correctly
- [ ] Verify Sources list layout sensible
- [ ] Screenshot: `09_rtl_story.png`

**CrossLens Comparison:**
- [ ] Open comparison view
- [ ] Verify source tabs flow right-to-left
- [ ] Verify prev/next buttons mirrored (next on left, prev on right)
- [ ] Verify article text readable
- [ ] Screenshot: `10_rtl_crosslens.png`

**Settings:**
- [ ] Navigate to Settings
- [ ] Verify back button on right
- [ ] Verify theme options layout correctly
- [ ] Verify toggle switches on correct side
- [ ] Screenshot: `11_rtl_settings.png`

### Findings
Record any issues:
- Layout not mirrored:
- Text overflow or clipping:
- Incorrect punctuation direction:
- Control placement issues:

### Cleanup
1. Return to Settings → System → Languages
2. Move English back to top
3. Remove Arabic or move down in list

---

## Test 4: Offline Persistence

**Objective:** Verify app works offline and persists state across force-stop.

### Setup
1. Ensure app has been launched at least once with network
2. Set a specific theme (e.g., Dark)
3. Enable "Preview Plus" in Settings
4. Open a story and scroll partway (Continue Reading)
5. Save story to reading list if that feature exists

### Test Steps

**Initial State Setup:**
- [ ] Launch app (network available)
- [ ] Settings → Set theme to "Dark"
- [ ] Settings → Tap "Preview Plus"
- [ ] Verify Plus access granted
- [ ] Navigate Home → Open second story in list
- [ ] Scroll to middle of story content
- [ ] Note story ID/title: _______________

**Enable Airplane Mode:**
- [ ] Swipe down notifications
- [ ] Enable Airplane Mode
- [ ] Verify Wi-Fi and cellular data off

**Offline App Launch:**
- [ ] Navigate back to Home
- [ ] Verify stories still visible (loaded from Room database)
- [ ] Verify Dark theme still applied
- [ ] Tap Explore → Select Plus filter
- [ ] Verify filter works (Plus access persisted)
- [ ] Screenshot: `12_offline_explore.png`

**Force Stop & Relaunch:**
- [ ] Settings → Apps → CrossLens → Force Stop
- [ ] Confirm force stop
- [ ] Wait 3 seconds
- [ ] Relaunch CrossLens from launcher
- [ ] Verify app launches successfully offline
- [ ] Verify Dark theme persisted
- [ ] Settings → Verify Plus access still shows as previewed
- [ ] Navigate to story from earlier - verify continues where left off
- [ ] Screenshot: `13_offline_relaunch.png`

**Return Online:**
- [ ] Disable Airplane Mode
- [ ] Return to app
- [ ] Verify continues working normally
- [ ] Screenshot: `14_back_online.png`

### Findings
Record any issues:
- App crashes offline:
- State not persisted:
- Continue reading lost:
- Theme reverted:
- Plus access lost:

---

## Test Summary Template

After completing all tests, fill out:

```
## Device Acceptance Results

**Device:** Pixel [model]
**Android Version:** [version]
**APK:** v0.0.2-beta
**Test Date:** 2026-09-22

### Test 1: Large Font Scaling
**Status:** [ ] PASS / [ ] FAIL
**Issues Found:** [list or "None"]
**Screenshots:** 01-06

### Test 2: TalkBack
**Status:** [ ] PASS / [ ] FAIL
**Issues Found:** [list or "None"]
**Screenshots:** 07

### Test 3: RTL Layout
**Status:** [ ] PASS / [ ] FAIL
**Issues Found:** [list or "None"]
**Screenshots:** 08-11

### Test 4: Offline Persistence
**Status:** [ ] PASS / [ ] FAIL
**Issues Found:** [list or "None"]
**Screenshots:** 12-14

### Overall Assessment
**Ready for Production Review:** [ ] YES / [ ] NO

**Blockers:** [list any remaining issues]
```
