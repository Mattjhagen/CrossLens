# CrossLens v0.0.9 Beta Release - Local Sources

## Release Information

**Version:** v0.0.9-beta  
**Date:** 2026-09-23  
**Milestone:** Local Sources  
**Build:** Debug APK  
**APK Size:** 58 MB

## What's New

### 1. Demo Local News Feature

Explore fictional local news stories from demo locations around the world.

**Features:**
- **Manual location selection** - Choose from demo cities: Seattle, Paris, London, Toronto
- **Local source metadata** - Each local source shows its city, region, and publisher type
- **Local story filtering** - Toggle local-only view in Home screen
- **Geographic diversity** - Demo locations span North America and Europe
- **Clear demo labeling** - All local content clearly marked as fictional

**Privacy-first design:**
- No device location tracking
- No IP geolocation
- Manual selection only
- Local-only storage
- No location-based analytics

### 2. Local Filter in Home

One-tap access to local news when you have a location selected.

**How it works:**
- Location icon in Home toolbar (filled when active, outlined when inactive)
- Tap to toggle between all stories and local stories only
- Local stories show only content from sources at your selected location
- Empty state guides you to Settings when no location selected

**What you see:**
- Regular Home feed shows global and national stories
- Local filter shows stories from your selected city/region
- Clear visual indicator of active filter state

### 3. Location Management in Settings

Choose, change, or clear your demo local location anytime.

**Features:**
- **Local News section** in Settings
- **Current location display** with city, region, and country
- **Location picker dialog** showing all available demo cities
- **One-tap removal** - Clear your location selection instantly
- **Preview explanation** - Clear notice that this is a demo feature

**What you see:**
- Seattle, Washington, US
- Paris, Île-de-France, FR
- London, England, GB
- Toronto, Ontario, CA

**Each location has:**
- 2 local sources (newspaper and TV station)
- 1 local story (transit, housing, cycling, or tech topics)
- Full article content and source details
- Integration with existing features (comparison, digest, feedback)

### 4. Local Source Metadata

Local sources are visibly distinct from national/global sources.

**New source fields:**
- **Local city/region** - Geographic coverage area
- **Publisher type** - Local newspaper, TV, radio, digital, or community news
- **Demo label** - Clear indication of fictional demo data

**Publisher types represented:**
- Local Newspaper (Seattle Times, Le Parisien, Evening Standard, Toronto Star)
- Local TV (KING 5, France 3, BBC London, CP24)

**What this means:**
- A source's geography provides context, not a political label
- Local coverage area doesn't imply editorial reliability
- Publisher type describes format, not quality

### 5. Fictional Local Stories

Each demo location includes one local story with realistic coverage.

**Seattle:** Light rail expansion to Ballard and West Seattle neighborhoods  
**Paris:** New protected bike lanes across 12 arrondissements  
**London:** Affordable housing initiative for five boroughs  
**Toronto:** Record tech sector investment creating thousands of jobs

**Story characteristics:**
- 2 sources per story (newspaper + TV coverage)
- Local focus with community impact
- Different topics per location
- Full article content (not just headlines)
- Demo Lens Gap status (insufficient coverage for comparison)

### 6. Integration with Existing Features

Local stories work with all existing CrossLens features.

**What works:**
- Read full local source articles
- Open publisher pages (with demo URLs)
- Use "Show more/less like this" feedback on local topics/regions
- View source details and attribution
- Access Demo AI Source Digest (when available)
- Save local stories for later

**What doesn't change:**
- Comparison features work the same
- Free/Plus access model unchanged
- Offline operation preserved
- Accessibility features maintained

## Technical Changes

**New Domain Models:**
- `LocalLocation` - Demo city with id, name, region, country
- `LocalSourceMetadata` - Local coverage area and publisher type
- `PublisherType` enum - LOCAL_NEWSPAPER, LOCAL_TV, LOCAL_RADIO, LOCAL_DIGITAL, COMMUNITY_NEWS
- `DemoLocalLocations` object - Predefined demo cities (Seattle, Paris, London, Toronto)

**Updated Models:**
- `Source` - Added optional `localSourceMetadata` field
- `UserPreferences` - Added `demoLocalLocation: String?` field
- `SourceEntity` - Added `isLocal`, `localLocationId`, `publisherType` fields

**New Repository Methods:**
- `StoryRepository.observeLocalStories(locationId)` - Filter stories by local location
- `UserPreferencesRepository.updateDemoLocalLocation(locationId)` - Save/clear location selection

**Updated ViewModels:**
- `HomeViewModel` - Added local filtering with `showLocalOnly` state
- `SettingsViewModel` - Added `updateDemoLocalLocation()` method

**New UI Components:**
- `LocalLocationSection` in Settings - Location picker and management
- Local filter button in Home toolbar
- `EmptyLocal` state for Home screen
- Location picker dialog with all demo cities

**Mock Data:**
- 8 new local sources (2 per demo city)
- 4 new local stories (1 per demo city)
- 8 new local articles (2 per story)
- All clearly labeled as demo content

**Database:**
- Schema version 5 (added local source fields to sources table)
- Idempotent seeding includes local sources and stories

## User-Facing Changes

**In Settings:**
- New "Local News" section after Personal Preferences
- "Demo Local News" card with picker
- Current location display with remove button
- "Choose location" / "Change location" button
- Location picker dialog with 4 demo cities
- Explanation text about demo feature and future availability

**In Home:**
- New location icon in toolbar (before Explore and Settings icons)
- Filled icon = local filter active
- Outlined icon = showing all stories
- Tap to toggle between modes
- Empty state when local filter active but no location selected

**Navigation Flow:**
```
Home → [Tap location icon] → Show local stories only
     → [Tap location icon again] → Show all stories

Settings → Local News → Choose location
                     → [Select city]
                     → Location saved

Home → [Tap location icon with location selected] → See local stories
                                                   → Full features available
```

## Build Verification

✅ **Unit Tests:**
```
./gradlew test
BUILD SUCCESSFUL
115 tests passed (all existing tests continue to pass)
```

**Note:** No new tests added for local filtering yet - relies on integration testing

✅ **Lint:**
```
./gradlew :app:lintDebug
BUILD SUCCESSFUL
Lint: 0 errors, 0 warnings
```

✅ **Debug Build:**
```
./gradlew assembleDebug
BUILD SUCCESSFUL
APK: 58 MB at app/build/outputs/apk/debug/app-debug.apk
```

## Data and Privacy Notice

**No location tracking:**
- Does NOT request device location permission
- Does NOT use IP geolocation
- Does NOT access contacts, calendar, or other location hints
- Manual selection only
- Location stored locally in DataStore (device-only)

**All local content is fictional demo data:**
- Local sources are invented for demonstration
- Local stories are fictional scenarios
- Publisher names may resemble real outlets but content is fabricated
- Demo URLs (demo.example/*) clearly indicate fictional content
- No real local news ingestion or scraping

**Clearly labeled throughout:**
- "Demo Local News" in Settings
- "This is a preview feature - live local source availability will vary by location" explanation
- "Demo Content" badges on local source info
- All local sources have isDemo = true

## Accessibility and Design

**Maintained standards:**
- ✅ Theme-aware (Light/Dark/System themes)
- ✅ TalkBack compatible with location picker
- ✅ Large text support
- ✅ RTL layout support
- ✅ Accessible touch targets (44dp minimum)
- ✅ Clear action labels ("Show local stories only" / "Show all stories")
- ✅ Keyboard navigation support

**Editorial design preserved:**
- Location picker uses consistent Material 3 styling
- Location icon follows toolbar design language
- Empty states use CrossLens signature and clear guidance
- Local source badges match existing demo labels
- Settings section integrates seamlessly

## Limitations and Future Work

**Current limitations (by design for this milestone):**
- Only 4 demo locations available
- No real local sources or content
- No automatic location detection
- No location-based notifications
- No local source discovery beyond demo set
- Limited to 1 story per demo location
- No local comparison (insufficient sources per story)
- No "nearby" or distance-based sorting

**Future live-data milestone considerations:**
- Real local source catalog with permission/attribution
- Automatic or assisted location selection (with opt-in)
- Broader geographic coverage (more cities, suburbs, neighborhoods)
- More local sources per location
- Local story comparison when multiple sources available
- Local editions or briefings
- Local breaking news alerts (with notification opt-in)
- Community-contributed local sources (with editorial review)

**Integration with personalization:**
- Current: "Show more like this" on local topics/regions works
- Future: Local stories could appear in personalized recommendations
- Boundary: Personalization suggests local content, doesn't hide important local news

**Live source considerations:**
- Source registry with content-use permissions
- Attribution requirements for local outlets
- Syndication and duplicate detection (local AP/wire stories)
- Paywall/access policies for local sources
- Editorial review of local source quality
- Clear disclosure of coverage limitations

## Changes from v0.0.8-beta

v0.0.8-beta completed the Personal Relevance Feedback milestone with transparent preference controls. v0.0.9-beta adds local news foundation:

1. **Manual location selection** - Choose from 4 demo cities (Seattle, Paris, London, Toronto)
2. **Local source metadata** - Publisher type, local coverage area, demo labels
3. **Local filtering** - Toggle local-only view in Home screen
4. **8 local sources** - 2 per demo city (newspaper + TV)
5. **4 local stories** - 1 per demo city with realistic local coverage
6. **Settings integration** - Location picker and management
7. **Privacy-first** - No location tracking, manual selection only
8. **Full feature integration** - Local stories work with all existing features

**Result:** Readers can now explore fictional local news from demo cities, with clear privacy boundaries and editorial context, while maintaining the app's offline-first, demo-data architecture.

## Installation

**Minimum Requirements:**
- Android 10 (API 29) or higher
- APK location: `app/build/outputs/apk/debug/app-debug.apk`
- Install command: `adb install app/build/outputs/apk/debug/app-debug.apk`

**Note:** This is a debug build for testing purposes. Not suitable for production distribution.

---

**This release completes the Local Sources milestone**, providing a foundation for local news exploration with clear privacy boundaries, geographic diversity, and transparent demo labeling, while preserving the app's offline-first architecture and maintaining strict separation between geography and political/editorial assumptions.
