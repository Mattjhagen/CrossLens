# CrossLens v0.0.8 Beta Release - Personal Relevance Feedback

## Release Information

**Version:** v0.0.8-beta  
**Date:** 2026-09-23  
**Milestone:** Personal Relevance Feedback  
**Build:** Debug APK  

## What's New

### 1. Personal Relevance Feedback

Help CrossLens understand your interests with simple, transparent feedback controls.

**Features:**
- **"Show more like this" / "Show less like this" actions** - Available when reading full source articles
- **Topic and region-based feedback** - Preferences are specific to observable article metadata
- **Local-only storage** - All feedback is stored locally using DataStore, no account required
- **Transparent purpose** - Clear explanation that these choices will shape future recommendations when that feature is introduced
- **No impact on evidence** - Feedback does not affect source evidence, framing, or editorial review decisions

**Privacy and safety:**
- Does not infer political beliefs, sensitive traits, or editorial truth from reading behavior
- Based only on observable article metadata (topic, region)
- Completely reversible - change your mind anytime
- Reset all preferences with one action

### 2. Preferences Management in Settings

Review and manage your saved preferences in one place.

**Features:**
- **Personal Preferences section** - New dedicated section in Settings
- **Grouped display** - See all "Show more" and "Show less" preferences organized by type
- **Individual removal** - Remove any specific preference
- **Reset all** - Clear all saved preferences at once
- **Clear explanations** - Understand what each preference means and how it will be used

**What you see:**
- Topic preferences (Technology, Environment, Economics, etc.)
- Region preferences (Europe, North America, Middle East, Asia, etc.)
- When each preference was created
- Easy-to-understand labels and formatting

### 3. Reversible Feedback

Your choices are never locked in.

**How it works:**
- Selecting "Show more like this" on an article about Technology and Europe saves those preferences
- Selecting "Show less like this" on a similar article updates your preferences
- You can change between "more" and "less" as many times as you want
- Remove individual preferences or reset everything in Settings

### 4. Future-Ready Design

This milestone lays the foundation for personalized recommendations while maintaining editorial integrity.

**What this means:**
- When recommendation features are introduced in the future, they will use your saved preferences
- Source evidence, framing analysis, and editorial review remain independent of personalization
- Recommendations will surface stories you might find interesting, not alter the truth of coverage
- You'll be able to see why a story was recommended and adjust preferences accordingly

## Technical Changes

**New Domain Models:**
- `PersonalRelevancePreference` - Stores user feedback on article relevance
- `PreferenceType` - MORE or LESS enum
- `DimensionType` - TOPIC or REGION enum
- `ArticleRelevanceContext` - Context for creating preferences from articles

**New Components:**
- `PersonalRelevanceRepository` interface and DataStore implementation
- Feedback controls in `SourceDetailScreen`
- Personal Preferences section in `SettingsScreen`
- Preference chip UI component with remove action

**Repository Layer:**
- `DataStorePersonalRelevanceRepository` - Persists preferences using DataStore
- Methods: addPreference, removePreference, clearAll, getPreferences
- Automatic replacement of existing preferences for same dimension value (enables reversibility)
- Serialization format for storing complex preferences in DataStore

**ViewModel Updates:**
- `SourceDetailViewModel` - Added onShowMoreLikeThis and onShowLessLikeThis actions
- Exposes existing preferences for current article context
- Determines relevant topics and regions from article and source metadata
- `SettingsViewModel` - Added preferences management methods

**Dependency Injection:**
- Added `PersonalRelevanceRepository` binding in RepositoryModule

## User-Facing Changes

**In Source Detail View:**
- New "Personal Recommendations" card after article content
- "Show more like this" and "Show less like this" buttons
- Current preference status indicator (when applicable)
- Clear explanation of what feedback does and doesn't affect
- Note: "Based on this article's topics and region"

**In Settings:**
- New "Personal Preferences" section
- List of saved preferences grouped by "Show more" and "Show less"
- Each preference shows: dimension type (Topic/Region), formatted name, remove button
- "Reset all preferences" button (when preferences exist)
- Empty state with guidance when no preferences saved
- Explanation text about future use and editorial boundaries

**Navigation Flow:**
```
Home → Story → Comparison → Source Detail → [Show more/less like this]
                                                    ↓
                                            Preference saved locally
                                                    ↓
Settings → Personal Preferences → [View and manage all feedback]
```

## Build Verification

✅ **Unit Tests:**
```
./gradlew test
BUILD SUCCESSFUL
115 tests passed (including 8 new DataStorePersonalRelevanceRepositoryTest tests)
```

**Test coverage includes:**
- Adding preferences for topics and regions
- Reversibility (changing preference type updates existing)
- Multiple preferences for different dimensions
- Remove preference
- Clear all preferences
- Persistence across repository recreation

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
APK location: app/build/outputs/apk/debug/app-debug.apk
```

## Data and Privacy Notice

**All preferences are stored locally:**
- No account, sign-in, or network request required
- Preferences persist across app restarts using DataStore
- No analytics, tracking, or external reporting
- No cloud sync or backup (preferences stay on this device)
- Uninstalling the app removes all saved preferences

**What feedback is based on:**
- Story topics (environment, technology, economics, etc.)
- Geographic regions associated with source outlets (Europe, North America, etc.)

**What feedback is NOT based on:**
- Political ideology, party affiliation, or bias labels
- Specific sources or publications you read
- Reading time, scroll depth, or other behavioral signals
- Personal demographic information
- Claims you agree or disagree with

**Clearly labeled throughout:**
- Explanation in feedback card: "Your choices will shape future recommendations when that feature is introduced"
- Note in Settings: "They don't affect which sources or evidence you see in story comparisons"
- All preferences show dimension type and value

## Accessibility and Design

**Maintained standards:**
- ✅ Theme-aware (Light/Dark/System themes)
- ✅ TalkBack compatible with clear content descriptions
- ✅ Large text support
- ✅ RTL layout support
- ✅ Accessible touch targets (44dp minimum)
- ✅ Keyboard navigation support
- ✅ Clear action labels ("Show more like this" not just "More")

**Editorial design preserved:**
- Feedback card uses consistent Material 3 styling
- Preference chips match existing design language
- Settings section integrates seamlessly with existing preferences
- Explanation text uses appropriate typography hierarchy
- Clear visual distinction between "Show more" and "Show less"

## Limitations and Future Work

**Current limitations (by design for this milestone):**
- No recommendation algorithm or personalized feeds yet
- No account sync or cross-device preferences
- Preferences based only on topic and region (no source-level preferences)
- No preference strength or weighting (just MORE or LESS)
- No explanation of why a specific preference was inferred from an article
- No feedback on the usefulness of recommendations (since recommendations don't exist yet)

**Future personalization milestone considerations:**
- Actual recommendation algorithm using saved preferences
- Personalized "For You" or "Recommended" feed
- Transparency features: "Recommended because you showed interest in [Technology, Europe]"
- Feedback loop: rate recommendations, refine preferences
- More granular controls: preference strength, topic subcategories
- Account-based sync across devices (with clear opt-in)
- Analytics and evaluation (with user consent and privacy review)
- Source-level preferences (show more from specific outlets)

**Editorial and ethical boundaries:**
- Personalization must remain separate from editorial truth, evidence evaluation, and fact-checking
- Preferences should not create filter bubbles that hide important public-interest coverage
- Users must be able to understand, inspect, and override any personalized ranking
- Diversity of perspectives should be preserved even with personalization active
- Platform must not manipulate preferences or recommendations for political or commercial purposes

## Changes from v0.0.7-beta

v0.0.7-beta completed the Source Experience milestone with full article reading, AI digest, and publisher page access. v0.0.8-beta adds personal relevance feedback:

1. **Feedback controls** - "Show more like this" and "Show less like this" on source detail screen
2. **Local preference storage** - DataStore-backed repository for persistence
3. **Settings management** - View, remove, and reset saved preferences
4. **Transparent design** - Clear explanation of purpose and editorial boundaries
5. **Offline-first preservation** - All features work without network requests

**Result:** Readers can now provide explicit feedback on article relevance, building a foundation for future personalized recommendations while maintaining the app's offline-first, demo-data architecture and editorial integrity.

## Installation

**Minimum Requirements:**
- Android 10 (API 29) or higher
- APK location: `app/build/outputs/apk/debug/app-debug.apk`
- Install command: `adb install app/build/outputs/apk/debug/app-debug.apk`

**Note:** This is a debug build for testing purposes. Not suitable for production distribution.

---

**This release completes the Personal Relevance Feedback milestone**, enabling readers to provide transparent, reversible feedback on article interests that will shape future recommendations when that feature is introduced, while preserving the app's offline-first architecture and maintaining strict separation between personalization and editorial integrity.
