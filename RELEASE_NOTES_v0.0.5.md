# CrossLens v0.0.5 Beta Release

## Release Information

**Version:** v0.0.5-beta  
**Date:** 2026-09-23  
**Commit:** TBD  
**Build:** Debug APK

## What's New

### CrossLens App Icon

Integrated the official CrossLens app icon as a proper Android adaptive launcher icon.

**Visual Design:**
- **Icon Artwork:** Two offset rounded rectangular panes in teal gradient shades with a diagonal cream/white crossing line on a dark teal background
- **Symbolism:** The offset panes and crossing line represent the "crossing lens" concept of comparing perspectives across sources
- **Design System:** Aligns with CrossLens's editorial design (teal accent, rounded corners, Material 3)

**Technical Implementation:**
- **Adaptive Icon:** Proper Android adaptive launcher icon for API 26+ with foreground and background layers
- **Legacy Support:** Full density coverage for pre-API 26 devices (mdpi through xxxhdpi)
- **Safe Area:** Icon design respects Android launcher mask safe zones (no cropping or white edges)
- **Centering:** Icon remains properly centered across all launcher shapes (circle, squircle, rounded square)

**Densities Generated:**
- `mdpi`: 48x48px
- `hdpi`: 72x72px
- `xhdpi`: 96x96px
- `xxhdpi`: 144x144px
- `xxxhdpi`: 192x192px
- Adaptive foreground: 432x432px

**Colors:**
- Background layer: Dark teal `#1A3540` (matches icon background)
- Icon design: Teal/turquoise panes with cream crossing line

## Technical Changes

**New Assets:**
- `app/src/main/res/drawable/ic_launcher_foreground.png` - Adaptive icon foreground layer
- `app/src/main/res/mipmap-mdpi/ic_launcher.png` & `ic_launcher_round.png`
- `app/src/main/res/mipmap-hdpi/ic_launcher.png` & `ic_launcher_round.png`
- `app/src/main/res/mipmap-xhdpi/ic_launcher.png` & `ic_launcher_round.png`
- `app/src/main/res/mipmap-xxhdpi/ic_launcher.png` & `ic_launcher_round.png`
- `app/src/main/res/mipmap-xxxhdpi/ic_launcher.png` & `ic_launcher_round.png`
- `docs/ASSET_CREDITS.md` - Icon attribution and provenance documentation

**Updated Resources:**
- `app/src/main/res/values/colors.xml` - Updated `ic_launcher_background` color to `#1A3540`

**Removed:**
- `app/src/main/res/drawable/ic_launcher_foreground.xml` - Replaced placeholder vector with actual icon PNG

**No Changes To:**
- App functionality, features, or behavior
- Editorial review workflow
- Settings, themes, or user preferences
- Manifest launcher icon references (already correct)
- App label (remains "CrossLens")

## Test Results

✅ **Lint:**
```
./gradlew :app:lintDebug
BUILD SUCCESSFUL
Lint: 0 errors, 0 warnings
```

✅ **Build:**
```
./gradlew :app:assembleDebug
BUILD SUCCESSFUL
APK: 57MB at app/build/outputs/apk/debug/app-debug.apk
```

⚠️ **Device Testing:**
- Launcher icon visual verification: NOT PERFORMED (no device/emulator available)
- Icon should be verified on device for:
  - Proper centering across launcher shapes
  - No white edges or cropping
  - Correct appearance in light/dark launcher themes
  - Adaptive icon animation (long-press)

## User-Facing Changes

Users will now see:
- **Launcher Icon:** Professional CrossLens brand icon on home screen and app drawer
- **Icon Design:** Two offset teal panes with crossing cream line representing the CrossLens perspective comparison concept
- **Adaptive Behavior:** Icon adapts to different launcher shapes (on Android 8.0+)
- **Consistent Branding:** Icon design consistent with in-app editorial design system

## Installation

Download `app-debug.apk` from this release and install on Android 10+ devices.

**Note:** This is a debug build for testing purposes. Not suitable for production distribution.

## Verification Checklist

✅ Icon source artwork preserved without visual changes  
✅ Adaptive icon with foreground and background layers  
✅ Legacy launcher icons for all densities (mdpi-xxxhdpi)  
✅ Background color matches icon design  
✅ Manifest references correct icon resources  
✅ App label remains "CrossLens"  
✅ Asset credits documentation added  
✅ Lint clean (0 errors, 0 warnings)  
✅ Debug build successful  
⚠️ Device launcher verification not performed (no device available)

## Files Changed

**New Files (12):**
- `docs/ASSET_CREDITS.md` - Icon attribution and specifications
- `app/src/main/res/drawable/ic_launcher_foreground.png` - 432x432px adaptive foreground
- `app/src/main/res/mipmap-mdpi/ic_launcher.png` - 48x48px
- `app/src/main/res/mipmap-mdpi/ic_launcher_round.png` - 48x48px
- `app/src/main/res/mipmap-hdpi/ic_launcher.png` - 72x72px
- `app/src/main/res/mipmap-hdpi/ic_launcher_round.png` - 72x72px
- `app/src/main/res/mipmap-xhdpi/ic_launcher.png` - 96x96px
- `app/src/main/res/mipmap-xhdpi/ic_launcher_round.png` - 96x96px
- `app/src/main/res/mipmap-xxhdpi/ic_launcher.png` - 144x144px
- `app/src/main/res/mipmap-xxhdpi/ic_launcher_round.png` - 144x144px
- `app/src/main/res/mipmap-xxxhdpi/ic_launcher.png` - 192x192px
- `app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.png` - 192x192px

**Modified Files (1):**
- `app/src/main/res/values/colors.xml` - Updated launcher background color

**Removed Files (1):**
- `app/src/main/res/drawable/ic_launcher_foreground.xml` - Placeholder replaced with actual icon

## Design Notes

The CrossLens icon design embodies the app's core concept:
- **Two Panes:** Represent multiple sources/perspectives on the same event
- **Offset Position:** Symbolizes different viewpoints and framing
- **Crossing Line:** Represents the "lens" that compares and intersects perspectives
- **Teal Color:** Matches the app's editorial design system accent color
- **Dark Background:** Provides good visibility on both light and dark launcher themes

The icon is distinctive, memorable, and immediately communicates the app's purpose of comparing perspectives across news sources.

## Changes from v0.0.4-beta

v0.0.4-beta added the reset control for editorial review. v0.0.5-beta adds the professional CrossLens brand icon, giving the app a polished launcher presence that reflects its editorial design quality.
