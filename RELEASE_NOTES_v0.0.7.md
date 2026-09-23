# CrossLens v0.0.7 Beta Release - Source Experience

## Release Information

**Version:** v0.0.7-beta  
**Date:** 2026-09-23  
**Milestone:** Source Experience  
**Build:** Debug APK  
**APK Size:** 58 MB

## What's New

### 1. Full Source Article Reading

Read complete source articles from the comparison view with a polished in-app experience.

**Features:**
- **Full article content:** Access the complete fictional source article, not just the headline and excerpt
- **Source context:** See source name, country, attribution, and publication date
- **Original language preserved:** Articles display in their original language with clear labeling
- **Demo content labeled:** All content clearly marked as fictional demo data
- **Clean typography:** Readable article layout with appropriate spacing and line height

**Navigation:**
- New "Read full article" button in the comparison view source pane
- Opens dedicated source detail screen with full content
- Back navigation returns to comparison

### 2. Demo AI Source Digest

Explore a demo AI-generated digest that summarizes agreement and differences across sources.

**Digest includes:**
- **Summary:** Brief overview of coverage patterns
- **Agreements:** What sources agree on, with supporting source IDs
- **Differences in framing:** How emphasis and perspective differ
- **Missing evidence:** What's not clearly covered across sources
- **Metadata:** Number of sources analyzed, generation method, demo status

**Important:** This is a **deterministic demo feature** using pattern matching, not actual AI analysis. Clearly labeled as "Demo AI Source Digest" with DEMO badge. Does not claim facts are verified or invent production scoring.

### 3. Publisher Page Access

Open the original publisher website when desired, with appropriate notices.

**Features:**
- **In-app browser:** Uses Chrome Custom Tabs for seamless experience with ability to return to CrossLens
- **Paywall notice:** Before opening sources likely to require subscriptions (Le Monde, NYT), shows concise dialog explaining:
  - Publisher may require subscription or have limited access
  - CrossLens does not bypass publisher access controls
  - Option to continue or cancel
- **User-initiated only:** Publisher pages open only when user explicitly taps "Open publisher page" button
- **Fallback:** If Custom Tabs unavailable, falls back to system browser

**Sources with paywall notice:**
- Le Monde (French)
- The New York Times (English)

**Sources without paywall notice:**
- BBC News (English)
- Other demo sources

### 4. Offline-First Preservation

All new features maintain the app's offline-first design.

- **No automatic network requests:** Mock mode never makes network calls
- **Full content in fixtures:** All article content stored locally in Room database
- **Digest generation:** Runs locally using deterministic pattern matching
- **Publisher URLs:** Demo URLs (demo.example/*) included but clearly labeled

## Technical Changes

**New Domain Models:**
- `SourceDigest` - AI digest model with agreements, differences, missing evidence
- `DigestPoint` - Individual observation with supporting source IDs
- Extended `Article` model with `originalContent` and `requiresSubscription` fields
- Extended `ArticleEntity` with corresponding database columns

**New Components:**
- `SourceDetailScreen` - Full article reader with source info and digest
- `SourceDetailViewModel` - Manages article, source, and digest state
- `SourceDigestGenerator` - Mock digest generation service

**Repository Updates:**
- `StoryRepository.getArticle(articleId)` - Fetch single article by ID
- `StoryRepository.getSourceDigest(storyId)` - Generate digest for story
- `SourceRepository.getSource(sourceId)` - Fetch single source by ID

**Navigation:**
- New `SourceDetail` destination with article ID parameter
- Navigation from comparison to source detail
- Full back-stack support

**Dependencies:**
- Added `androidx.browser:browser:1.8.0` for Chrome Custom Tabs support

**Database:**
- Schema version 4 (added `originalContent` and `requiresSubscription` to articles table)
- Mock fixtures updated with full article content for demo stories

## User-Facing Changes

**In Comparison View:**
- New "Read full article" button in each source pane
- Opens full article reader when tapped

**In Source Detail View:**
- Full article text with proper formatting
- Source attribution card with country, author, publish date
- "Open publisher page" button (shows paywall notice for subscription sources)
- Demo AI Source Digest section (when available)
- All content clearly labeled as demo

**Navigation Flow:**
```
Home → Story → Comparison → [Read full article] → Source Detail
                                                    ↓
                                            [Open publisher page]
                                                    ↓
                                            Chrome Custom Tab / Browser
```

## Build Verification

✅ **Unit Tests:**
```
./gradlew test
BUILD SUCCESSFUL
107 tests passed (including new SourceDigestGeneratorTest)
```

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
APK: 58MB at app/build/outputs/apk/debug/app-debug.apk
```

## Content and Data Notice

**All content remains fictional offline demo data:**
- Full article content is fictional and created for demonstration
- AI digest uses deterministic pattern matching, not actual AI models
- Publisher URLs are demo placeholders (demo.example/*)
- No network requests in mock mode
- No real news data or live services

**Clearly labeled throughout:**
- "Demo Content" badges on source info
- "Demo AI Source Digest" with DEMO badge
- "(Demo link notice)" on publisher URLs
- "Demo Ingestion" attribution where applicable

## Accessibility and Design

**Maintained standards:**
- ✅ Theme-aware (Light/Dark/System themes)
- ✅ TalkBack compatible
- ✅ Large text support
- ✅ RTL layout support
- ✅ System back navigation
- ✅ Readable typography with appropriate line height
- ✅ Accessible touch targets (44dp minimum)
- ✅ Proper content descriptions

**Editorial design preserved:**
- Magazine-style typography in article reader
- Material 3 components with custom theme
- Generous spacing and readable measure
- Consistent with existing CrossLens design language

## Limitations and Future Work

**Current limitations (by design for this milestone):**
- Article content is mock data only
- Digest uses pattern matching, not actual AI analysis
- Publisher URLs are demo placeholders
- No live ingestion or content extraction
- No translation of full article content (only headlines/excerpts)
- No bookmark/highlight/annotation features in reader

**Future live-data milestone considerations:**
- Real content extraction and ingestion pipeline
- Actual AI model integration for digest generation with:
  - Permission-aware content handling
  - Hallucination detection and evaluation
  - Translation error checking
  - Editorial governance and quality controls
- Live publisher URL integration
- Paywall detection (vs. current static flag)
- Article-level translations
- Reader features (bookmarks, highlights, notes)

## Changes from v0.0.6-beta

v0.0.6-beta completed the visual branding milestone. v0.0.7-beta adds the source reading experience:

1. **Full article reader** - Complete source content with proper attribution
2. **Demo AI digest** - Pattern-based comparison summary (clearly labeled as demo)
3. **Publisher page access** - In-app browser with paywall notices
4. **Offline preservation** - All features work without network requests

**Result:** Readers can now access complete source articles, see a demo digest comparing coverage, and optionally visit publisher websites, while maintaining the offline-first demo architecture.

## Installation

**Minimum Requirements:**
- Android 10 (API 29) or higher
- APK location: `app/build/outputs/apk/debug/app-debug.apk`
- Install command: `adb install app/build/outputs/apk/debug/app-debug.apk`

**Note:** This is a debug build for testing purposes. Not suitable for production distribution.

---

**This release completes the Source Experience milestone**, providing readers with full article access, demo digest analysis, and publisher page navigation while preserving the app's offline-first, demo-data architecture.
