# Instructions: Create GitHub Release v0.0.2-beta

## Prerequisites
✅ Code committed and pushed to main (commit 815d0a5)  
✅ Git tag `v0.0.2-beta` created and pushed  
✅ APK built at `app/build/outputs/apk/debug/app-debug.apk` (57MB)  
✅ Release notes prepared in `RELEASE_NOTES_v0.0.2.md`

## Steps to Create Release on GitHub

### 1. Navigate to Releases
Go to: https://github.com/Mattjhagen/CrossLens/releases

### 2. Click "Draft a new release"

### 3. Configure Release Settings

**Choose a tag:**
- Select existing tag: `v0.0.2-beta`

**Release title:**
```
v0.0.2-beta - Theme Application Fix
```

**Description:**
Copy the contents from `RELEASE_NOTES_v0.0.2.md` or use the following:

```markdown
## What's Fixed

### Theme Application Bug
Fixed critical defect where MainActivity did not observe or apply user theme preference from Settings.

**The Issue:**
- SettingsViewModel correctly persisted theme selection to DataStore
- MainActivity always called `CrossLensTheme()` without reading the persisted preference
- Theme selection had no visible effect; app always followed system theme

**The Fix:**
- Created `MainViewModel` to observe `UserPreferencesRepository.preferencesFlow`
- MainActivity now collects theme state with lifecycle awareness
- Theme correctly resolved: SYSTEM → isSystemInDarkTheme(), LIGHT → false, DARK → true
- Theme persists across app restart via DataStore

## Test Results

✅ **All Tests Passing**
- 15 unit tests: 15 passed, 0 failed, 0 skipped
- New MainViewModel tests for theme resolution (4 tests)
- Build verification successful

## User-Facing Behavior

Users can now:
1. Open Settings and select Light, Dark, or System theme
2. See the app immediately switch to the selected theme
3. Navigate between screens with the theme applied consistently
4. Force-stop and relaunch the app with the theme preference persisted

## Installation

Download `app-debug.apk` from this release and install on Android 10+ devices.

**Note:** This is a debug build for testing purposes. Not suitable for production distribution.

**Files Changed:**
- `app/src/main/java/com/crosslens/app/MainViewModel.kt` (new)
- `app/src/main/java/com/crosslens/app/MainActivity.kt` (updated)
- `app/src/test/java/com/crosslens/app/MainViewModelTest.kt` (new)

**Commit:** 815d0a5
```

### 4. Upload APK Asset

Click "Attach binaries by dropping them here or selecting them"

**File to upload:**
`app/build/outputs/apk/debug/app-debug.apk`

**Rename to:** `CrossLens-v0.0.2-beta-debug.apk`

### 5. Set Release Options

- ☑️ **Set as a pre-release** (check this box - it's a beta)
- ☐ Set as the latest release (leave unchecked)

### 6. Publish Release

Click "Publish release"

## Verification

After publishing:
1. Verify the release appears at https://github.com/Mattjhagen/CrossLens/releases
2. Verify the APK is downloadable
3. Verify the tag `v0.0.2-beta` is linked correctly

## Alternative: Using GitHub CLI

If you have `gh` CLI installed, you can create the release with:

```bash
gh release create v0.0.2-beta \
  app/build/outputs/apk/debug/app-debug.apk#CrossLens-v0.0.2-beta-debug.apk \
  --title "v0.0.2-beta - Theme Application Fix" \
  --notes-file RELEASE_NOTES_v0.0.2.md \
  --prerelease
```

## Done!

The release will be available for download and testing.
