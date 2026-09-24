# CrossLens v0.0.9-beta Device Testing Instructions

**Version:** v0.0.9-beta  
**Commit:** 7275c32  
**Feature:** Local Sources  
**Device:** Pixel (Android 10+)  
**APK:** Already installed on your device

---

## Quick Test Overview

Test the 4 demo local locations (Seattle, Paris, London, Toronto) and verify:
- Manual location selection works
- Local filtering shows correct stories
- Demo content is clearly labeled
- Privacy: no location tracking, no accounts
- Works offline, accessible, theme-compatible

---

## Test Steps

### 1. SELECT DEMO LOCATIONS

**1A. Access Location Picker**
- Tap **Settings** (top-right)
- Scroll to **"Local News"** section
- ✓ Card says "Demo Local News" with explanation
- ✓ "Choose location" button visible

**1B. Select Seattle**
- Tap "Choose location"
- ✓ Dialog shows 4 cities: Seattle, Paris, London, Toronto
- Tap **Seattle**
- ✓ Dialog closes automatically
- ✓ Card shows "Seattle, Washington" with × button
- ✓ Button now says "Change location"

**1C-E. Test Other Locations**
- Change to **Paris** → ✓ Shows "Paris, Île-de-France"
- Change to **London** → ✓ Shows "London, England"
- Change to **Toronto** → ✓ Shows "Toronto, Ontario"

---

### 2. FIND LOCAL STORIES

**With Toronto selected:**

**2A. Toggle Local Filter**
- Press **Back** to Home
- Tap **location icon** (leftmost in toolbar)
- ✓ Icon changes from outlined to filled
- ✓ Shows only Toronto story: "Toronto Tech Sector Sees Record Investment"

**2B. Open Local Story**
- Tap the Toronto tech story
- ✓ Summary mentions "$2.5 billion in tech sector investment"
- ✓ Shows 2 sources: Toronto Star, CP24
- Tap "Read full article" on **Toronto Star**
- ✓ Source Detail opens with full article
- ✓ **"Demo Content" badge visible**
- ✓ Article content is local (Toronto-focused)

**2C. Test Other Cities**
- Back to Home → Settings → change to **Seattle** → filter local
- ✓ Shows: "Seattle Transit Expansion Plans Advance"

- Change to **Paris** → filter local
- ✓ Shows: "Paris Expands Cycling Infrastructure"

- Change to **London** → filter local
- ✓ Shows: "London Announces Affordable Housing Initiative"

---

### 3. VERIFY SOURCE METADATA

**Seattle Sources:**
- Open Seattle transit story
- Read full article on **Seattle Times - Local**
- ✓ Source name: "The Seattle Times - Local"
- ✓ Country: US
- ✓ Language: en
- ✓ **Demo Content badge** visible
- ✓ Demo URL pattern: demo.example/*

- Check **KING 5 News** article
- ✓ Local TV source, Demo Content labeled

**Paris Sources (French):**
- Change to Paris, open cycling story
- Open **Le Parisien** article
- ✓ Country: FR
- ✓ Language: fr
- ✓ **Article content in French** (headline, body text)
- ✓ Demo Content badge visible

**London & Toronto:**
- Verify both have Demo Content badges
- Verify realistic local focus in articles

---

### 4. CHANGE AND CLEAR LOCATION

**4A. Clear Location**
- Settings → Local News
- Tap **× button** next to location name
- ✓ Location clears immediately
- ✓ Shows "Choose location" button again

**4B. Empty State**
- Back to Home
- Tap location icon
- ✓ Shows empty state: "No local stories available"
- ✓ Message: "Choose a demo location in Settings..."
- ✓ "Open Settings" button visible
- Tap "Open Settings" → ✓ Navigates to Settings

---

### 5. FORCE-CLOSE & PERSISTENCE

**5A. With Location Selected**
- Choose **Seattle** in Settings
- Back to Home, verify local filter works
- **Force-close app:** Recent apps → swipe away CrossLens
- Reopen from launcher
- ✓ Settings still shows "Seattle, Washington"
- ✓ Local filter still works

**5B. With Location Cleared**
- Clear location (×)
- Force-close
- Reopen
- ✓ Settings shows no location selected

**5C. Multiple Restarts**
- Choose Paris → force-close → reopen
- ✓ Paris still selected
- Change to Toronto → force-close → reopen
- ✓ Toronto still selected

---

### 6. PRIVACY CHECKS

**6A. No Location Permission**
- Device Settings → Apps → CrossLens → Permissions
- ✓ **No location permission** in list

**6B. No Auto-Detection**
- Enable device location services
- Open CrossLens → Settings → Local News
- ✓ Still shows manual "Choose location"
- ✓ No "Use my location" option

**6C. No Account**
- Check all screens
- ✓ **No sign-in prompts**
- ✓ No account buttons
- ✓ All features work without account

**6D. No Live Network**
- Open local story and article
- ✓ Instant loading (no network delay)
- ✓ Demo URLs clearly marked

**6E. No Notifications**
- Device Settings → Apps → CrossLens → Notifications
- ✓ No location-based notification channels

---

### 7. ACCESSIBILITY TESTS

**7A. Offline Operation**
- Enable **Airplane mode**
- Force-close and reopen
- Navigate: Settings → change location → Home → local story → article
- ✓ **Everything works offline**
- ✓ No errors or connection messages

**7B. Light/Dark Themes**
- Device Settings → Display → Theme → **Light**
- Check Settings Local News card, Location picker
- ✓ All text readable with good contrast

- Switch to **Dark** theme
- ✓ Location picker cards dark with light text
- ✓ All icons/text visible

**7C. Large Text**
- Device Settings → Display → Font size → **Largest**
- Check Settings → Local News
- ✓ Text scales properly
- ✓ "Seattle, Washington" doesn't truncate
- ✓ Buttons remain tappable

- Open local story and article
- ✓ Article content scales appropriately

**7D. RTL Layout**
*(May be BLOCKED if Arabic not available)*
- Device Settings → Languages → Add Arabic
- Reopen CrossLens
- ✓ Layout mirrors correctly (location icon moves right)
- ✓ Settings card mirrors (× on left)

**7E. TalkBack**
- Device Settings → Accessibility → TalkBack → **Enable**
- Navigate with swipes

Settings → Local News:
- Focus "Choose location" button
- ✓ Announces: "Choose location, Button"

- Open picker, focus Seattle
- ✓ Announces: "Seattle. Washington, US. Button"

Home → Location icon:
- ✓ Announces filter state clearly

**Disable TalkBack after test**

**7F. Back Navigation**
- Settings → Choose location → Seattle → Back
- ✓ Returns to Settings (dialog auto-closed)

- Home → Local story → Source detail → Back → Back
- ✓ Returns to Story, then Home

- Location picker → Back
- ✓ Dialog dismisses, stays in Settings

---

## Report Format

For each test section, report:
- **PASS** - Works as expected
- **FAIL** - Issue found (describe what happened)
- **BLOCKED** - Cannot test (explain why)

### Example Report:
```
1A-E (Select Locations): PASS - All 4 cities selectable
2A-C (Local Stories): PASS - Each city shows correct story
3 (Source Metadata): PASS - Demo labels visible, French in Paris articles
4 (Change/Clear): PASS - Immediate updates, empty state works
5 (Persistence): PASS - Location survives force-close
6A (Privacy): PASS - No location permission
6B-E (Privacy): PASS - All privacy checks confirmed
7A (Offline): PASS - Works in airplane mode
7B (Themes): PASS - Both themes readable
7C (Large Text): PASS - Scales properly
7D (RTL): BLOCKED - Arabic not available
7E (TalkBack): FAIL - Location icon missing description
7F (Navigation): PASS - Back works correctly
```

---

## Quick Visual Checklist

- [ ] All 4 locations selectable
- [ ] Each location shows 1 local story when filtered
- [ ] Demo Content badges visible on all local sources
- [ ] Paris articles in French
- [ ] Demo URLs visible (demo.example/*)
- [ ] × button clears location
- [ ] Persists after force-close
- [ ] No location permission
- [ ] Works offline
- [ ] Light/dark themes both work
- [ ] Large text scales
- [ ] TalkBack works

---

**When testing is complete, report results and I'll publish the v0.0.9-beta GitHub release.**
