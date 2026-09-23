# CrossLens v0.0.2 Beta Release

## Release Information

**Version:** v0.0.2-beta  
**Date:** 2026-09-22  
**Commit:** 815d0a5  
**Build:** Debug APK

## What's Fixed

### Theme Application Bug
Fixed critical defect where MainActivity did not observe or apply user theme preference from Settings.

**The Issue:**
- SettingsViewModel correctly persisted theme selection to DataStore
- MainActivity always called `CrossLensTheme()` without reading the persisted preference
- Theme selection had no visible effect; app always followed system theme

**The Fix:**
- Created `MainViewModel` to observe `UserPreferencesRepository.preferencesFlow`
- MainActivity now collects theme state with lifecycle awareness using `collectAsStateWithLifecycle()`
- Theme correctly resolved and passed to CrossLensTheme:
  - `SYSTEM` → `isSystemInDarkTheme()`
  - `LIGHT` → `false`
  - `DARK` → `true`
- Theme persists across app restart via DataStore

**Files Changed:**
- `app/src/main/java/com/crosslens/app/MainViewModel.kt` (new)
- `app/src/main/java/com/crosslens/app/MainActivity.kt` (updated)
- `app/src/test/java/com/crosslens/app/MainViewModelTest.kt` (new)

## Test Results

✅ **All Tests Passing**
```
./gradlew :app:testDebugUnitTest
BUILD SUCCESSFUL
Tests: 15 tests, 15 passed, 0 failed, 0 skipped
```

**New Tests:**
- MainViewModel theme SYSTEM emission
- MainViewModel theme LIGHT emission
- MainViewModel theme DARK emission
- MainViewModel theme update on preference change

## Verification

The fix has been verified with:
1. Unit tests for theme resolution logic
2. Build verification (clean build successful)
3. APK installation on API 36 emulator
4. DataStore persistence confirmed across force-stop/relaunch

## User-Facing Behavior

Users can now:
1. Open Settings and select Light, Dark, or System theme
2. See the app immediately switch to the selected theme
3. Navigate between screens with the theme applied consistently
4. Force-stop and relaunch the app with the theme preference persisted

## Known Limitations

Manual accessibility testing and physical device testing remain incomplete pending device availability.

## Installation

Download `app-debug.apk` from this release and install on Android 10+ devices.

**Note:** This is a debug build for testing purposes. Not suitable for production distribution.
