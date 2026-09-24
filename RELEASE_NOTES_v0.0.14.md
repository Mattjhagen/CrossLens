# Release Notes: v0.0.14-beta - Live Feed Foundation

**Release Date:** 2026-09-24  
**Status:** Beta - Device testing required before public release

## Overview

CrossLens v0.0.14-beta introduces live RSS feed ingestion from four approved international news sources. The app now fetches real headlines while maintaining offline functionality through intelligent caching and mock data fallback.

## What's New

### Live RSS Feed Integration

- **Four International Sources:**
  - BBC News (UK/Europe) - English
  - Al Jazeera (Qatar/Middle East) - English
  - Deutsche Welle (Germany/Europe) - English
  - France 24 (France/Europe) - English

- **Offline-First Caching:**
  - Cached feed shown instantly on app launch
  - Background refresh when cache > 15 minutes old
  - Pull-to-refresh forces immediate update
  - Most recent successful cache available offline
  - Automatic fallback to mock data when all sources unavailable

- **Feed State Indicators:**
  - Clear "Live Feed" / "Cached Feed" / "Demo Fallback" labels
  - Last updated timestamp
  - Active source count
  - Honest loading and error states

### Technical Improvements

- **Per-Source Failure Isolation:** One unavailable source never blocks others
- **Robust RSS Parsing:** Handles malformed XML, missing dates, and oversized feeds
- **Request Safety:** 10-second timeouts, 5MB size limits, HTTPS-only
- **Privacy-Preserving:** No accounts, location collection, or passive tracking

## What's NOT Included

This is a foundational release. The following remain deferred:

- **No Automatic Event Clustering:** Articles shown chronologically, not matched across sources as covering the same event
- **No Editorial Analysis:** Lens Gap, claims assessment, and framing observations remain mock-only
- **Headline-Level Only:** RSS provides title and excerpt; full articles open in-app browser
- **English-First:** Multilingual feed variants available but not yet integrated
- **No Paywall Bypass:** Original reporting may require publisher subscription

## Architecture

- `RssParser` - Robust RSS 2.0 XML parser with error handling
- `RssSourceAdapter` - Implements existing `SourceAdapter` interface
- `LiveStoryRepository` - Manages caching, background refresh, and fallback logic
- Room database extended with `FeedMetadataEntity` for cache tracking
- HomeViewModel and HomeScreen updated with feed state indicators

## Privacy & Safety

- **Public Feeds Only:** All four sources use publicly documented RSS URLs
- **No Credentials:** No API keys or tokens in repository
- **HTTPS Only:** All requests and links use HTTPS
- **No Passive Tracking:** No analytics, advertising IDs, or location collection
- **Clear Attribution:** Each article shows original publisher and links to source

## Known Issues & Limitations

1. **Database Version Bump:** v6 schema requires fresh install or clears existing data
2. **No Local Stories Integration:** Live feed doesn't yet respect "Local Sources" filter
3. **No Deduplication:** Same article from multiple sources may appear multiple times
4. **Cache Age UI:** Feed freshness indicator polls every 30 seconds (not real-time)
5. **Background Refresh:** Happens on init if cache stale; no WorkManager scheduled refresh yet

## Testing Checklist

Before publishing this release, verify on Pixel device:

### Core Feed Functionality
- [ ] App launches and shows feed (live or cached)
- [ ] Pull-to-refresh fetches new articles
- [ ] Feed state indicator shows correct state (Live/Cached/Demo)
- [ ] Last updated time displays correctly
- [ ] Source count appears when live feed active

### Offline Behavior
- [ ] Enable airplane mode
- [ ] Force stop app
- [ ] Relaunch app - cached feed should appear
- [ ] Feed state shows "Cached Feed"
- [ ] Articles still open in browser (cached)

### Fallback Behavior
- [ ] Clear app data
- [ ] Enable airplane mode
- [ ] Launch app - mock demo data should appear
- [ ] Feed state shows "Demo Fallback"

### Error Handling
- [ ] Enable Wi-Fi but block port 443
- [ ] Pull to refresh - should handle gracefully
- [ ] Verify no crash, appropriate error state

### Accessibility
- [ ] TalkBack announces feed state
- [ ] Pull-to-refresh has content description
- [ ] All new UI elements have accessibility labels

### Existing Features (Regression Testing)
- [ ] Home screen loads stories
- [ ] Story detail screen works
- [ ] Settings screen accessible
- [ ] Local Sources filter works (shows mock data)
- [ ] For You recommendations appear
- [ ] Dark/light theme switching works
- [ ] Reduced motion setting respected

## Build Information

- **Version Code:** 14
- **Version Name:** 0.0.14-beta
- **Min SDK:** 29 (Android 10)
- **Target SDK:** 34
- **APK Location:** `app/build/outputs/apk/debug/app-debug.apk`

## Documentation

- [Live Feed Sources](docs/LIVE_FEED_SOURCES.md) - Complete source documentation
- [Android Build Guide](docs/ANDROID_BUILD_GUIDE.md) - Build instructions
- [Design Direction](docs/DESIGN_DIRECTION.md) - UI/UX guidelines

## Next Steps

After successful Pixel testing:

1. Update this document with test results
2. Create GitHub release with APK attached
3. Write user-facing release announcement
4. Plan v0.0.15 features (event clustering, multilingual, etc.)

## Migration Notes

**For Developers:**
- Database schema updated to v6 (adds `feed_metadata` table)
- `StoryRepository` interface unchanged; implementation swapped to `LiveStoryRepository`
- `MockStoryRepository` still used for seeding and local sources
- New dependencies: None (uses existing OkHttp and Room)

**For Users:**
- Fresh install recommended to avoid schema migration issues
- Existing saved stories and preferences preserved if app not reinstalled
