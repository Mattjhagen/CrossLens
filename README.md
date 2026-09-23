# CrossLens

**One event. A world of perspectives.**

CrossLens is an Android news app for understanding how the same event is reported across countries, languages, and institutions. Readers explore clustered stories, compare source coverage, and read translations while retaining a clear path to the original reporting.

The goal is informed comparison: show where reporting overlaps, where emphasis differs, and what evidence supports those observations. A source's country is context, not a proxy for its politics or the views of an entire population.

> **Project status:** ✅ **Initial Android skeleton complete.** The app runs offline with mock data, demonstrates all five screens, and implements the Free/Plus access model. Live sources, translation services, and automated analysis are future work.

## Quick Start

### Prerequisites

- **Android Studio** with SDK Platform 29+ and Build Tools 34.0.0+
- **JDK 21** (Azul Zulu recommended)
- **Git**

### Build and Run

```sh
git clone https://github.com/Mattjhagen/CrossLens.git
cd CrossLens

# Build debug APK
./gradlew :app:assembleDebug

# Run tests and lint
./gradlew :app:testDebugUnitTest :app:lintDebug

# Install on connected device/emulator
./gradlew :app:installDebug
adb shell am start -n com.crosslens.app.debug/.MainActivity
```

**APK Location:** `app/build/outputs/apk/debug/app-debug.apk` (56MB)

### Build Results

- **Build:** ✅ SUCCESS (Gradle 8.9, AGP 8.5.2, Kotlin 1.9.24)
- **Unit Tests:** ✅ 5/5 passed
- **Lint:** ✅ 0 errors, 0 warnings
- **Quality Audit:** See [QUALITY_REPORT.md](docs/QUALITY_REPORT.md)

## Product principles

- **Global by design:** organize coverage by source, country, region, language, topic, and institutional context.
- **Evidence stays visible:** link summaries and comparison observations to the articles supporting them; distinguish reported claims from established facts.
- **Translation with provenance:** preserve original text, label translated text, and disclose translation availability and method.
- **Multiple dimensions of perspective:** geographic, political, and institutional context can coexist. Avoid a universal left/center/right classification.
- **Explain the comparison:** readers should understand the reasons behind a future Lens Gap result and its limitations.

## Design is a core feature

**CrossLens should be gorgeous and make readers want to return.** The visual direction takes inspiration from the owner's love of Flipboard's editorial feel and tactile browsing, expressed through an original CrossLens identity: expressive typography, generous imagery, varied magazine compositions, warm surfaces, and restrained perspective-switch motion.

The first skeleton must already feel thoughtfully designed. Material 3 is the foundation for a custom visual system, and every screen—including empty states and Settings—gets the same care. Subtle flip-inspired transitions should support comparing sources, with accessible controls and reduced-motion alternatives.

Build reasons to return from the start: a finite mock edition, locally saved stories, a continue-reading shortcut, and useful invitations to explore another perspective. Live briefings, followed topics, and meaningful update notifications come later. Return visits should come from beauty, trust, and discovery.

Read [the design direction and visual acceptance gate](docs/DESIGN_DIRECTION.md) alongside the build guide. It defines screen composition, motion, original identity, and the visual evidence required at handoff.

## Free reading and CrossLens Plus

CrossLens will offer a useful free experience and an optional **CrossLens Plus** upgrade. Free readers can discover stories, read event context and claims, save stories, and compare two source perspectives. Plus unlocks the full set of sources and available translations for a story, complete Lens Gap evidence, and deeper discovery controls.

The first Android skeleton contains a clearly labeled local **Preview Plus** entitlement so the paywall and locked states can be designed and tested without processing purchases. It does not contain Google Play Billing, real prices, trials, or payment data. See [the access model](docs/MONETIZATION.md) for the complete free/Plus boundary, paywall behavior, and production requirements.

## The first five screens

| Screen | Initial skeleton experience |
| --- | --- |
| **Home** | Clustered mock stories with title, summary, topic, countries, source count, timestamp, and clearly labeled demo comparison data. Sort by latest, coverage breadth, or demo Lens Gap. |
| **Story** | Event overview, attributed claims, sources, original article links, and a route into comparison. Distinguish disputed claims and missing evidence. |
| **CrossLens** | Compare selected source coverage of one event: original and mock-translated headlines/excerpts, framing observations, and supporting references. Stack cards on narrow screens. |
| **Explore** | Filter mock stories by region, country, topic, and original article language; combine filters and clear them. |
| **Settings** | Persist preferred reading language, home country/region, source selections, translation display preference, and theme. |

Home, Explore, and Settings are top-level destinations. Story and CrossLens are detail destinations addressed by stable story IDs, with predictable back navigation.

## What's Implemented

### Screens
- **Home:** Story list with save/unsave, continue reading, mock edition label
- **Story:** Event summary, attributed claims, sources, comparison action
- **CrossLens:** Source-by-source comparison with flip animation, paywall gating
- **Explore:** Region/topic filters with combined AND logic
- **Settings:** Theme selection, reduced motion, Plus preview/reset, editorial review access
- **Editorial Review (Demo):** Offline workflow prototype for reviewing mock ingestion candidates

### Features
- ✅ Offline-first: All data seeded from mock fixtures in Room
- ✅ Free/Plus access: Free users see first 2 sources, Plus unlocks all
- ✅ DataStore persistence: Saved stories, last-opened, preferences
- ✅ Custom Material 3 theme: Editorial design with serif/sans typography
- ✅ Reduced motion: Animations respect user preference
- ✅ Multilingual fixtures: English, French, Arabic, Japanese samples
- ✅ Editorial review workflow: Offline demo for reviewing cross-language matches and syndication candidates

### Mock Data
- 3 story clusters (climate summit, AI regulation, trade)
- 6 sources across 4 regions (BBC, Le Monde, Al Jazeera, NYT, Globe and Mail, 読売新聞)
- Demo Lens Gap assessments clearly labeled
- Claims with CORROBORATED/DISPUTED status
- Frame observations with evidence references
- Editorial review candidates (cross-language matches, syndication detection)

## Technical Foundation

| Area | Implementation |
| --- | --- |
| Platform | Native Android, Kotlin, minimum API 29 (Android 10) |
| UI | Jetpack Compose, Material 3, Navigation Compose |
| Architecture | MVVM with immutable StateFlow UI state |
| DI | Hilt with repository interfaces |
| Database | Room 2.6.1 with idempotent seeding |
| Preferences | DataStore 1.1.1 |
| Networking | Retrofit/OkHttp defined but inactive in mock mode |
| Build | Gradle 8.9, AGP 8.5.2, Kotlin 1.9.24, KSP |
| Testing | JUnit 4, Mockito-Kotlin, Coroutines Test, Turbine |

**Documented Toolchain:** See [BUILD_STATUS.md](docs/BUILD_STATUS.md) for complete version matrix

```text
Compose screen → ViewModel → repository interface
                                   ↓
                          mock repository → Room → bundled fixtures
                                   ↓ later
                          live repository → Retrofit/OkHttp → backend
```

Room supplies observable local data. ViewModels never depend on fixture files, Retrofit DTOs, or database entities. Dependency injection chooses the implementation, so future ingestion and translation services can replace mocks without rewriting screens.

## Data model: events, reporting, and interpretation

These are proposed domain contracts, not existing classes. Use stable IDs, UTC instants, ISO country codes, and BCP 47 language tags. Regions are explicit groupings; sources may cover multiple countries and articles may use a different language from their publisher's default.

| Model | Essential fields and relationships |
| --- | --- |
| `Story` | ID, title, event summary, event time (optional), updated time, topic IDs, event country codes, article IDs, claim IDs; optional Lens Gap assessment |
| `Source` | ID, name, homepage, country codes, region IDs, default languages, optional ownership/funding and editorial-context records with citations and review dates |
| `Article` | ID, story ID, source ID, original URL, published time, original language, original headline and excerpt; attribution and content-use metadata |
| `Translation` | ID, article ID, source/target language, translated headline/excerpt, status (`NOT_REQUESTED`, `PENDING`, `AVAILABLE`, `FAILED`, `UNAVAILABLE`), method/provider, generation time, original-content revision |
| `Claim` | ID, story ID, statement, assessment (`REPORTED`, `CORROBORATED`, `DISPUTED`, `UNASSESSED`), supporting and contradicting article references, assessment provenance |
| `FrameObservation` | ID, story ID, article IDs, emphasized actors/claims, language or sentiment observations, evidence references, method/version and confidence; optional omission hypothesis with comparison scope |
| `Perspective` | ID, scope/article IDs, dimension (`GEOGRAPHIC`, `POLITICAL`, `INSTITUTIONAL`), descriptive label, evidence references, attribution and uncertainty |
| `LensGapAssessment` | Story ID, status, nullable score, component explanations, source/article sample IDs, coverage window, confidence/limitations, method version, generated time, `isDemo` |
| `UserPreferences` | Reading language, home country/region, enabled source IDs, translation preference, theme, reduced-motion preference |
| `ReadingState` | Locally persisted saved story IDs and last-opened story ID; independent of seeded content |
| `Entitlement` | Active tier (`FREE` or local `PLUS_DEMO`), access-state source, and update time; never a real purchase record in the skeleton |

Keep original and translated content separate. Missing translations fall back to the original with a visible explanation. A corroborated claim still needs evidence; an apparent omission means “not found in this analyzed sample,” not proof of intentional suppression. Keep event location distinct from the country of a reporting source.

## Future signature feature: Lens Gap

**Lens Gap asks: how differently is this event being covered in the available reporting?**

A future assessment may describe differences in emphasized claims, selected actors, terminology, and the distribution of attention across sources. Each result should reveal its sampled articles, geographic/language coverage, time window, explanatory components, and concrete evidence. Broad regional descriptions must identify the sampled publications and avoid implying a single regional viewpoint.

A possible future display is **“Lens Gap: 72/100 — divergent emphasis.”** This is a proposed index, not a probability, factuality score, or claim that 72% of reporting is biased. The scoring method, thresholds, confidence calibration, and minimum evidence requirements remain research work.

The skeleton uses deterministic fixture assessments labeled **“Demo Lens Gap — illustrative.”** It implements no scoring algorithm. Stories without sufficient comparison data show **“Not enough coverage”** with a null score, never a misleading zero. Demo sorting places unavailable scores last and explains that scores are simulated.

Before real ranking ships, evaluate translation artifacts, duplicate/syndicated reporting, uneven source sampling, time-window effects, and uncertainty. More articles from the same syndicated report must not masquerade as independent agreement. Readers should be able to inspect the explanation and report a misleading comparison.

## Roadmap

1. ✅ **Android skeleton:** Five screens, local fixtures, offline persistence, preferences, navigation, meaningful tests, and a mock free/Plus access experience. *(Complete)*
2. ✅ **Editorial review prototype:** Offline workflow demonstrating human review of ingestion candidates (cross-language matches, syndication detection). *(Complete - demo only)*
3. **Source ingestion:** Backend API, source catalog, attribution/content permissions, article normalization, event clustering, and deduplication. Connect editorial review to live ingestion pipeline.
4. **Translations:** Provider abstraction, cache/version handling, original-text access, explicit failure states, and multilingual/RTL validation.
5. **Explainable comparisons:** Evidence-backed frame observations and a human-reviewed Lens Gap evaluation set before production scoring.
6. **Discovery experiments:** Evaluate coverage breadth, cross-country differences, and “outside my usual sources” sorting with transparent controls.

Accounts, real subscriptions/billing, scraping, production backend deployment, live translation, and automated political labels are outside the initial build.

Future concepts such as reader-controlled personalization, relevant
notifications, local reporting, and AI-assisted editorial triage are tracked
in the [product ideas backlog](docs/PRODUCT_IDEAS.md). They are not currently
implemented.

## Project Structure

```
app/src/main/java/com/crosslens/app/
├── CrossLensApplication.kt         # Hilt app, initializes data seeding
├── MainActivity.kt                 # Edge-to-edge Compose host
├── core/
│   ├── model/                      # Domain models (Story, Article, Claim, etc.)
│   └── ui/
│       ├── theme/                  # Material 3 custom theme
│       └── PaywallSheet.kt        # Reusable Plus paywall
├── data/
│   ├── local/
│   │   ├── entity/                # Room entities
│   │   ├── dao/                   # Room DAOs
│   │   ├── CrossLensDatabase.kt   # Database definition
│   │   ├── Converters.kt          # Type converters
│   │   └── Mappers.kt             # Entity → Domain
│   ├── mock/
│   │   ├── MockFixtures.kt        # Fixture data
│   │   └── Mock*Repository.kt     # Mock implementations
│   ├── preferences/               # DataStore repositories
│   └── repository/                # Repository interfaces
├── di/                            # Hilt modules
├── feature/
│   ├── home/                      # HomeScreen + ViewModel
│   ├── story/                     # StoryScreen + ViewModel
│   ├── comparison/                # CrossLensScreen + ViewModel
│   ├── explore/                   # ExploreScreen + ViewModel
│   └── settings/                  # SettingsScreen + ViewModel
└── navigation/                    # NavHost and destinations
```

## Known Limitations

### Skeleton Scope
- **No live backend:** All data is seeded from fixtures
- **No real translations:** Demo translations are manually created samples
- **No Lens Gap algorithm:** Scores are fixed demo values
- **No Google Play Billing:** Plus access is local preview only
- **No network requests:** Mock mode works offline
- **No bundled imagery:** Text-only compositions

### Device Testing
- **Not tested on physical device** (no device available during build)
- **No emulator screenshots captured**
- **TalkBack not verified**
- **Performance not profiled**

See [QUALITY_REPORT.md](docs/QUALITY_REPORT.md) for complete audit results and blocked checks.

## Development References

- [BUILD_STATUS.md](docs/BUILD_STATUS.md) - Build verification and step status
- [QUALITY_REPORT.md](docs/QUALITY_REPORT.md) - Complete audit with 6 reviews
- [ANDROID_BUILD_GUIDE.md](docs/ANDROID_BUILD_GUIDE.md) - Implementation phases
- [DESIGN_DIRECTION.md](docs/DESIGN_DIRECTION.md) - Visual design requirements
- [MONETIZATION.md](docs/MONETIZATION.md) - Free/Plus access model
- [Android Architecture Guide](https://developer.android.com/topic/architecture) - MVVM best practices

## Contributing

This is a demonstration project for the CrossLens concept. The skeleton implements:
1. ✅ Five-screen navigation
2. ✅ Room database with observable queries
3. ✅ Free/Plus access model
4. ✅ Custom editorial design
5. ✅ Offline-first architecture

Future work includes backend integration, live translations, and Lens Gap analysis algorithm.
