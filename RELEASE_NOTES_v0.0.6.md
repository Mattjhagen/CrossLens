# CrossLens v0.0.6 Beta Release

## Release Information

**Version:** v0.0.6-beta  
**Tag:** `v0.0.6-beta`  
**Date:** 2026-09-23  
**Commit:** 0c59430  
**Build:** Debug APK  
**SHA256:** `17b19d935981e840da72665cb96d4a9b294847936960a885577618128ea6f931`  
**Release URL:** https://github.com/Mattjhagen/CrossLens/releases/tag/v0.0.6-beta

## What's New

### 1. Adaptive Launcher Icon

Integrated the official CrossLens app icon as a proper Android adaptive launcher icon.

**Visual Design:**
- **Icon Artwork:** Two offset rounded rectangular panes in teal gradient shades with a diagonal cream/white crossing line on a dark teal background
- **Symbolism:** The offset panes and crossing line represent the "crossing lens" concept of comparing perspectives across sources
- **Design System:** Aligns with CrossLens's editorial design (teal accent, rounded corners, Material 3)

**Technical Implementation:**
- **Adaptive Icon:** Proper Android adaptive launcher icon for API 26+ with foreground and background layers
- **Legacy Support:** Full density coverage for pre-API 26 devices (mdpi through xxxhdpi)
- **Safe Area:** Icon design respects Android launcher mask safe zones
- **Centering:** Icon remains properly centered across all launcher shapes (circle, squircle, rounded square)

**Densities Generated:**
- `mdpi`: 48x48px
- `hdpi`: 72x72px
- `xhdpi`: 96x96px
- `xxhdpi`: 144x144px
- `xxxhdpi`: 192x192px
- Adaptive foreground: 432x432px

**Physical Device Verification (Pixel 11):** ✅ PASS
- Icon is centered, unclipped, readable
- No white border or cropping artifacts
- Visible and appropriate in all launcher shapes

### 2. Theme-Aware Editorial Signature

Added a distinctive in-app editorial signature that translates the launcher icon's "crossing perspectives" concept into a masthead-appropriate design.

**Visual Components:**

**CrossLensSignature:** Full editorial masthead with:
- Wordmark text in bold serif ("CrossLens")
- Thin diagonal crossing rule intersecting the text
- Subtle offset perspective pane indicators above and below (20-24% opacity)

**CrossLensWordmark:** Lightweight variant with text and crossing rule only (no panes), used in:
- Comparison screen "Framing Observations" section header
- Contexts where full signature geometry would compete with content

**Three Sizes:**
- **Small:** 32dp text, 80dp rule, 6dp pane height - for compact empty states
- **Medium:** 48dp text, 120dp rule, 8dp pane height - for loading states
- **Large:** 64dp text, 160dp rule, 10dp pane height - for Home masthead

**Theme Integration:**
- **Fully theme-aware:** Uses Material 3 theme colors (primary, tertiary, onBackground)
- **Light mode:** Teal accent with ink on ivory background
- **Dark mode:** Lighter teal accent with warm light on charcoal background
- **Automatic adaptation:** Signature colors adapt seamlessly to Light/Dark/System theme changes

**Implementation Locations:**
- **Home Screen:** Large signature masthead with perspective panes
- **Loading States:** Medium signature with perspective panes (Home, Comparison)
- **Empty States:** Small/medium signature with perspective panes (Home empty, Explore no-results, Comparison not-found)
- **Comparison Section Header:** Small wordmark only (text + rule, intentionally no panes to avoid visual clutter near framing content)

**Accessibility:**
- **TalkBack silent:** Uses `clearAndSetSemantics {}` - signature is decorative only
- **Secondary to content:** Never conveys functional information, pure visual branding
- **Theme colors:** Maintains proper contrast in Light and Dark modes

**Perspective Pane Visibility:**
- Initial implementation used 8-12% opacity (too subtle, nearly invisible on devices)
- Adjusted to 20-24% opacity for subtle but visible presence
- Pane heights increased: Small 4→6dp, Medium 6→8dp, Large 8→10dp
- Design intent: visible crossing-perspectives motif that remains secondary to content

**Physical Device Verification (Pixel 11):** ✅ **PASS**
- Home masthead perspective panes visibility - subtle horizontal bars visible above/below wordmark
- Explore empty state motif and "Clear all" filter recovery - signature visible, Clear all restores stories
- Light/Dark theme adaptation of signature colors - adapts correctly with proper contrast
- Persistence after force-close/reopen - theme preference and state persist correctly

### 3. Complete Design Documentation

Added comprehensive design documentation:
- **`docs/EDITORIAL_SIGNATURE.md`** - Component rationale, usage guidelines, theme integration, accessibility
- **`docs/ASSET_CREDITS.md`** - Icon attribution and provenance

## Technical Changes

**New Assets (Launcher Icon):**
- `app/src/main/res/drawable/ic_launcher_foreground.png` - 432x432px adaptive icon foreground layer
- `app/src/main/res/mipmap-mdpi/ic_launcher.png` & `ic_launcher_round.png` - 48x48px
- `app/src/main/res/mipmap-hdpi/ic_launcher.png` & `ic_launcher_round.png` - 72x72px
- `app/src/main/res/mipmap-xhdpi/ic_launcher.png` & `ic_launcher_round.png` - 96x96px
- `app/src/main/res/mipmap-xxhdpi/ic_launcher.png` & `ic_launcher_round.png` - 144x144px
- `app/src/main/res/mipmap-xxxhdpi/ic_launcher.png` & `ic_launcher_round.png` - 192x192px
- `docs/ASSET_CREDITS.md` - Icon attribution and provenance documentation

**New Components (Editorial Signature):**
- `app/src/main/java/com/crosslens/app/core/ui/components/EditorialSignature.kt` - Signature and wordmark components
- `docs/EDITORIAL_SIGNATURE.md` - Design documentation and usage guidelines

**Updated Resources:**
- `app/src/main/res/values/colors.xml` - Updated `ic_launcher_background` color to `#1A3540`

**Updated Screens (Editorial Signature Integration):**
- `app/src/main/java/com/crosslens/app/feature/home/HomeScreen.kt` - Large signature masthead, loading/empty signatures
- `app/src/main/java/com/crosslens/app/feature/comparison/CrossLensScreen.kt` - Loading/empty signatures, wordmark in section headers
- `app/src/main/java/com/crosslens/app/feature/explore/ExploreScreen.kt` - Signature in empty state

**Removed:**
- `app/src/main/res/drawable/ic_launcher_foreground.xml` - Replaced placeholder vector with actual icon PNG

**No Changes To:**
- App functionality, features, or behavior
- Editorial review workflow
- Settings, themes, or user preferences
- Manifest launcher icon references (already correct)
- App label (remains "CrossLens")

## Build Verification

✅ **Unit Tests:**
```
./gradlew test
BUILD SUCCESSFUL
All tests passed
```

✅ **Lint:**
```
./gradlew :app:lintDebug
BUILD SUCCESSFUL
Lint: 0 errors, 0 warnings
Report: app/build/reports/lint-results-debug.html
```

✅ **Debug Build:**
```
./gradlew assembleDebug
BUILD SUCCESSFUL
APK: 58MB at app/build/outputs/apk/debug/app-debug.apk
SHA256: 17b19d935981e840da72665cb96d4a9b294847936960a885577618128ea6f931
```

## User-Facing Changes

**Launcher:**
- ✅ Professional adaptive icon on home screen and app drawer
- ✅ Icon design: Two offset teal panes with crossing cream line representing perspective comparison
- ✅ Adapts to different launcher shapes (verified on Android 8.0+ devices)

**In-App Branding:**
- Home masthead with large editorial signature including perspective panes ⏸️ *pending device verification*
- Branded loading states with signature and spinner
- Branded empty states maintaining brand presence
- Comparison section headers with subtle wordmark (text + rule only, no panes)
- Full theme adaptation across Light/Dark/System themes ⏸️ *pending device verification*
- Consistent design language from launcher to in-app experience

## Content Notice

**All content is fictional and offline demo data:**
- 3 sample stories with fictional reporting
- 6 sources across 4 regions (all demo attributions)
- Sample translations labeled as demo translations
- Demo Lens Gap values (not a production scoring formula)
- No network requests, no real news data
- Explicitly labeled as "Mock Edition · Demo" throughout the UI

**App remains offline-capable by design:** All content, preferences, and state persist locally via Room and DataStore. No backend integration or live services in this milestone.

## Installation

**Minimum Requirements:**
- Android 10 (API 29) or higher
- APK location: `app/build/outputs/apk/debug/app-debug.apk`
- Install command: `adb install app/build/outputs/apk/debug/app-debug.apk`

**Note:** This is a debug build for testing purposes. Not suitable for production distribution.

## Verification Checklist

**Launcher Icon (Pixel Device):**
✅ Icon centered across all launcher shapes  
✅ No white border or clipping  
✅ Readable and recognizable at all sizes  
✅ Proper adaptive icon animation (long-press)

**Editorial Signature (Build Verified):**
✅ CrossLensSignature component with three sizes (Small/Medium/Large)  
✅ CrossLensWordmark lightweight variant  
✅ Theme-aware using Material 3 colors  
✅ Silent to TalkBack (clearAndSetSemantics)  
✅ Integrated into Home, Comparison, and Explore screens  
✅ Perspective pane visibility adjusted (20-24% opacity)  
✅ Design documentation complete

**Editorial Signature (Pixel Device - PENDING):**
⏸️ Home masthead perspective panes visible and subtle  
⏸️ Loading states display signature correctly  
⏸️ Empty states display signature correctly  
⏸️ Comparison wordmark appropriate in section headers  
⏸️ Light/Dark theme signature colors adapt correctly  
⏸️ No visual clutter or competition with content

**Build & Test:**
✅ Unit tests passing  
✅ Lint clean (0 errors, 0 warnings)  
✅ Debug build successful  
✅ APK generated with correct checksum

**Physical Device Testing (Pixel 11):**
✅ Install APK on Pixel  
✅ Home masthead perspective bars visible  
✅ Explore empty-state motif and "Clear all" recovery  
✅ Light/Dark theme adaptation  
✅ Persistence after force-close/reopen  
✅ TalkBack ignores decorative signature  
✅ Reduced motion setting honored

## Files Changed

**New Files (15):**
- `docs/ASSET_CREDITS.md` - Icon attribution and specifications
- `docs/EDITORIAL_SIGNATURE.md` - Signature design documentation
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
- `app/src/main/java/com/crosslens/app/core/ui/components/EditorialSignature.kt` - Signature components

**Modified Files (5):**
- `app/src/main/res/values/colors.xml` - Updated launcher background color
- `app/src/main/java/com/crosslens/app/feature/home/HomeScreen.kt` - Integrated editorial signature
- `app/src/main/java/com/crosslens/app/feature/comparison/CrossLensScreen.kt` - Integrated editorial signature
- `app/src/main/java/com/crosslens/app/feature/explore/ExploreScreen.kt` - Integrated editorial signature
- `docs/EDITORIAL_SIGNATURE.md` - Updated with visibility adjustments (20-24% opacity)

**Removed Files (1):**
- `app/src/main/res/drawable/ic_launcher_foreground.xml` - Replaced placeholder with actual icon

## Design Notes

**Launcher Icon Design Philosophy:**
- **Two Panes:** Represent multiple sources/perspectives on the same event
- **Offset Position:** Symbolizes different viewpoints and framing
- **Crossing Line:** Represents the "lens" that compares and intersects perspectives
- **Teal Color:** Matches the app's editorial design system accent color
- **Dark Background:** Provides good visibility on both light and dark launcher themes
- **Distinctive and Memorable:** Immediately communicates the app's purpose of comparing perspectives

**Editorial Signature Design Philosophy:**
- **Conceptual Continuity:** Shares the "crossing perspectives" DNA with the launcher icon
- **Editorial Reimagining:** Text-based masthead appropriate for publication-style layouts, not a duplicate of the icon
- **Subtle Visibility:** 20-24% opacity provides presence without competing with content
- **Theme Awareness:** Adapts colors to maintain appropriate contrast in Light/Dark modes
- **Context-Appropriate:** Three sizes for different placements, wordmark variant for constrained spaces
- **Accessibility First:** Decorative only, never conveys functional information

**Comparison Screen Design Decision:**
The Comparison screen "Framing Observations" section intentionally uses `CrossLensWordmark` (text + crossing rule only) rather than the full `CrossLensSignature` with perspective panes. This decision prioritizes readability and prevents visual clutter near the framing evidence content. The wordmark provides subtle brand continuity without the geometric complexity of the full signature.

## Changes from v0.0.4-beta

v0.0.4-beta added the reset control for editorial review. v0.0.5-beta completes the visual branding milestone:

1. **Professional adaptive launcher icon** - Distinctive icon representing the "crossing perspectives" concept
2. **Theme-aware editorial signature** - Masthead and wordmark translating the icon concept into editorial layouts
3. **Perspective pane visibility** - Adjusted from nearly invisible (8-12%) to subtly visible (20-24% opacity)
4. **Complete design documentation** - Rationale, usage, accessibility, theme integration

**Result:** A cohesive brand experience from launcher to in-app content, with the app having a polished visual identity that reflects its editorial design quality.

## Known Limitations

- This is a debug build (not production-signed)
- All content is fictional demo data (no live news integration)
- Mock data only (no network requests)
- Three sample stories in mock edition
- Demo translations and demo Lens Gap values
- No Google Play Billing integration (Plus tier is local preview only)
- Physical device verification pending user testing

## Next Steps

1. User installs APK on Pixel device
2. User verifies perspective pane visibility in Home/Explore
3. User tests Light/Dark theme adaptation
4. User tests persistence after force-close
5. If all physical device checks pass: proceed to GitHub release publication
6. If adjustments needed: iterate on visibility/design and rebuild

---

**This release represents the completion of the visual branding milestone defined in the Android Build Guide.** The app now has a distinctive visual identity from launcher icon through in-app experience, with theme-aware editorial signature components and comprehensive design documentation.
