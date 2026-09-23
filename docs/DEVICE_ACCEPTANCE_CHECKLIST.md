# CrossLens Device Acceptance Checklist

**Date:** 2026-09-22  
**Device:** sdk_gphone64_arm64 (Emulator)  
**API Level:** 36 (Android 15)  
**Build:** app-debug.apk (post-fix)

## Pre-Test Setup

- [ ] Install fresh APK: `adb install -r app/build/outputs/apk/debug/app-debug.apk`
- [ ] Verify clean app data (or uninstall/reinstall)
- [ ] Note current build SHA

## Core Functionality Tests

### 1. First Launch & Data Seeding
- [ ] App launches successfully
- [ ] Home screen shows 3 mock stories
- [ ] "Mock Edition · Demo" label visible
- [ ] No network requests made (airplane mode test)
- [ ] Database seeded idempotently

### 2. Navigation Flow
- [ ] Home → Story Detail works
- [ ] Story → CrossLens works  
- [ ] Home → Settings works (top bar icon)
- [ ] Home → Explore works (top bar icon)
- [ ] Back button returns from each screen

### 3. Story Detail Screen
- [ ] Headline displays
- [ ] Claims section shows with assessments (REPORTED, CORROBORATED, etc.)
- [ ] Sources section lists all articles for story
- [ ] "View in CrossLens" button works
- [ ] Save button toggles (verify icon changes)
- [ ] Original article link shows demo notice
- [ ] Demo labels present

### 4. CrossLens Free Tier (PRIMARY FIX TEST)
- [ ] Opens to source 1/3
- [ ] Source pane shows: source name, country, original text, framing observations
- [ ] Previous/Next buttons work for first 2 sources
- [ ] Attempting to view 3rd source shows paywall
- [ ] Paywall displays: title, benefits list, "Preview Plus" button, "Maybe Later" button
- [ ] "Maybe Later" dismisses paywall
- [ ] **"Preview Plus" button on paywall ENABLES Plus access** (PRIMARY FIX)
- [ ] After enabling, can view all 3 sources
- [ ] Source flip animation works (or snaps if reduced motion on)

### 5. Settings Plus Control (PRIMARY FIX TEST)
- [ ] Settings shows Access Tier card
- [ ] Default state: "Free" with "Preview Plus" button
- [ ] **Pressing "Preview Plus" in Settings ENABLES Plus access** (PRIMARY FIX)
- [ ] After enabling, Settings shows "Plus (Demo)" with "Reset to Free" button
- [ ] **Return to CrossLens - can now access all sources without paywall** (PRIMARY FIX)
- [ ] "Reset to Free" button works
- [ ] After reset, CrossLens paywall returns for 3rd source

### 6. Explore Screen
- [ ] Shows region filter chips (All, North America, Europe, etc.)
- [ ] Shows topic filter chips (All, Technology, etc.)
- [ ] Selecting filters updates story list
- [ ] "Clear all" removes filters
- [ ] AND logic works (region + topic both applied)

### 7. Theme & Preferences
- [ ] Settings theme dropdown works (System, Light, Dark)
- [ ] Light mode: ivory background, near-black text
- [ ] Dark mode: charcoal background, warm light text
- [ ] Theme changes persist after switching screens
- [ ] Reduced Motion toggle works
- [ ] With reduced motion ON, source flip snaps instead of animating

## Persistence Tests (CRITICAL)

### 8. App Restart - Settings Persistence
- [ ] In Settings, enable "Preview Plus"
- [ ] Verify Plus active (Settings shows "Plus (Demo)")
- [ ] Force-stop app: `adb shell am force-stop com.crosslens.app`
- [ ] Relaunch app
- [ ] **Open Settings - verify still shows "Plus (Demo)"** (PERSISTENCE TEST)
- [ ] Open CrossLens - verify can access all sources without paywall
- [ ] Reset to Free in Settings
- [ ] Force-stop and relaunch
- [ ] Verify Settings shows "Free"

### 9. App Restart - Saved Stories
- [ ] Save a story from Story Detail
- [ ] Return to Home - verify "Continue Reading" section shows saved story
- [ ] Force-stop app: `adb shell am force-stop com.crosslens.app`
- [ ] Relaunch app
- [ ] **Verify saved story still in "Continue Reading"**

### 10. App Restart - Theme Preference
- [ ] Change theme to "Dark" in Settings
- [ ] Force-stop app
- [ ] Relaunch app
- [ ] **Verify app opens in dark mode**

## Accessibility Tests

### 11. Large Font Scaling
- [ ] Open device Settings → Display → Font size
- [ ] Set to "Largest"
- [ ] Return to CrossLens app
- [ ] Verify all text scales properly
- [ ] No text truncation or overlap
- [ ] Touch targets still accessible
- [ ] Reset font size to default

### 12. RTL Layout (if locale available)
- [ ] Device Settings → System → Languages → Add Arabic
- [ ] Set Arabic as primary language
- [ ] Return to CrossLens app
- [ ] Navigate to story "Climate Adaptation Strategies..."
- [ ] Open CrossLens view
- [ ] **Verify Arabic translation displays right-to-left**
- [ ] Verify UI layout mirrors correctly
- [ ] Navigation icons in correct RTL positions
- [ ] Reset to English

### 13. Accessibility Scanner (if available)
- [ ] Enable Accessibility Scanner in device settings
- [ ] Scan Home screen
- [ ] Scan Story Detail screen
- [ ] Scan CrossLens screen
- [ ] Scan Paywall modal
- [ ] Scan Settings screen
- [ ] Note any contrast, touch target, or label issues
- [ ] Fix P0 issues, document P1+ issues

### 14. TalkBack (if available)
- [ ] Enable TalkBack in device settings
- [ ] Navigate through Home screen with TalkBack
- [ ] Verify all elements have meaningful labels
- [ ] Test Story Detail screen navigation
- [ ] Test paywall modal (especially "Preview Plus" button)
- [ ] Test Settings controls (switches, dropdowns)
- [ ] Disable TalkBack

## Edge Cases

### 15. Network Conditions
- [ ] Enable airplane mode
- [ ] App continues to function (all data is local)
- [ ] Article links show error appropriately if clicked
- [ ] No crash or hang waiting for network

### 16. Empty States
- [ ] (Not applicable - mock data always present)

### 17. Long Press / Gestures
- [ ] No unintended gestures trigger actions
- [ ] All interactions via explicit buttons work

## Pass Criteria

**Mandatory for Release:**
- [ ] All "PRIMARY FIX" items pass
- [ ] All persistence tests pass
- [ ] No crashes during test run
- [ ] Theme changes work and persist
- [ ] Saved stories persist across restart
- [ ] Plus access persists across restart (Settings and Paywall both enable it)

**Recommended but Not Blocking:**
- [ ] Large font scaling works
- [ ] RTL layout works (if locale available)
- [ ] Accessibility Scanner finds no P0 issues
- [ ] TalkBack works (if available)

## Test Results

**Date Completed:** _____________  
**Tester:** _____________  
**Pass/Fail:** _____________  
**Blocking Issues Found:** _____________  
**Notes:** _____________
