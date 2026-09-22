# Build the CrossLens Android skeleton with Claude Code

## Outcome

Deliver an installable Android debug app that demonstrates the five-screen CrossLens experience using local fictional news fixtures. Implement the boundaries needed for future global sources, translations, and explainable comparisons. No live backend, API key, or translation subscription is required.

Visual quality is part of this outcome. Follow [the design direction](DESIGN_DIRECTION.md) for an original editorial identity, subtle flip-inspired motion, local reading continuity, and the required visual review.

This guide is an implementation handoff. The repository initially contains documentation only; commands beginning with `./gradlew` become available after Phase 1 creates the project and wrapper.

For the execution sequence, copy-and-paste session prompt, step-level gates, audit rubric, and final device checklist, follow [the Claude CLI runbook](CLAUDE_BUILD_RUNBOOK.md). Its steps expand the six implementation phases below.

## 1. Prepare the workstation

1. Install Android Studio and use its SDK Manager to install a stable Android SDK, platform tools, and an emulator image. Create an API 29+ emulator or enable USB debugging on an Android 10+ device.
2. Install/authenticate Claude Code using the [official setup instructions](https://code.claude.com/docs/en/setup). Confirm `claude --version` works, then start `claude` and complete sign-in if needed.
3. Have Git and a JDK compatible with the selected Android Gradle Plugin available. Claude should choose and document the compatible versions in Phase 1; Android Studio and command-line Gradle must use the same compatible JDK.
4. Clone this repository, or enter an existing checkout. Keep the Android project at the repository root.

```sh
git clone https://github.com/Mattjhagen/CrossLens.git
cd CrossLens
claude
```

Android Studio can create the machine-local `local.properties` SDK path when opening the generated project. Keep that file untracked. Build dependency downloads require internet access; the installed mock app does not.

## 2. Give Claude this kickoff prompt

```text
Read CLAUDE.md, README.md, docs/ANDROID_BUILD_GUIDE.md, and
docs/DESIGN_DIRECTION.md. Build the
initial CrossLens Android skeleton through all six phases in this guide.
First inspect the repository and available Android/JDK tools. Then choose
and document a compatible stable toolchain and implement the app, rather
than stopping at a plan.

Use Kotlin, Jetpack Compose, Material 3, MVVM, Coroutines/Flow, Hilt,
Retrofit/OkHttp, Room, and DataStore preferences. Start with a single app
module, application ID com.crosslens.app, and minSdk 29. This application
ID is a development default and does not establish domain ownership.

Implement Home, Story, CrossLens comparison, Explore, and Settings with
stable-ID navigation, immutable UI state, and mock repositories backed
by deterministically seeded Room data. Include explicit loading, empty,
error, unavailable-translation, and unavailable-comparison states.

Use fictional multilingual fixtures. Clearly label demo content,
translations, and Lens Gap assessments. Keep networking inactive and
implement no real Lens Gap algorithm. Include original-text fallback,
source attribution, filtering/sorting, persisted preferences, and
accessible light/dark layouts.

Make the app gorgeous from this first milestone: original editorial
layouts, expressive typography, bundled imagery, custom Material 3 tokens,
and a subtle flip-inspired perspective transition. Use Flipboard as a
reference for editorial qualities, never copy its branding or exact UI.
Include a finite mock edition, persisted saved stories and continue reading,
and accessible reduced motion. Complete the design brief's visual gate.

Work through the acceptance checklist, run the relevant build and test
commands, and fix failures. If the environment blocks a check, state
exactly what is missing and provide the command to rerun. Update the
README with actual setup, chosen versions, run instructions, test results,
and remaining limitations. Stop at the skeleton milestone.
```

The CLI also accepts this shorter initial prompt:

```sh
claude "Read CLAUDE.md, README.md, and docs/ANDROID_BUILD_GUIDE.md. Implement and verify the complete initial Android skeleton described there."
```

To resume the latest session from this checkout, use `claude -c`. These commands use the documented [Claude Code CLI interface](https://code.claude.com/docs/en/cli-reference).

## 3. Build phases

### Phase 1 — Runnable foundation

- Generate Gradle Kotlin DSL settings/build files, a version catalog, and a complete Gradle wrapper including its JAR, properties, Unix executable, and Windows script.
- Pin compatible stable Android Gradle Plugin, Kotlin, Compose BOM/compiler integration, KSP, Hilt, Room, Retrofit/OkHttp, Navigation Compose, lifecycle, and DataStore dependencies. Record JDK, Gradle, compile SDK, target SDK, and minimum SDK.
- Set up Hilt application/activity integration, a custom Material 3 theme with reusable editorial design tokens, edge-to-edge insets, and navigation placeholders for the five destinations. Establish the lead-story and source-pane visual components using the design brief.
- Add Android-appropriate ignores for local properties, IDE machine state, build output, credentials, and signing material.
- Verify the first debug APK builds before adding the data layer.

Suggested organization (packages within one module; not separate Gradle modules):

```text
app/src/main/java/com/crosslens/app/
  CrossLensApplication.kt
  MainActivity.kt
  di/
  core/model/
  core/ui/
  data/local/          # Room entities, DAOs, database, seed versioning
  data/mock/           # fixtures, scenarios, mock repository implementations
  data/remote/         # future API contract and DTO mappings
  data/repository/     # repository interfaces and mappings
  data/preferences/   # DataStore implementation
  feature/home/
  feature/story/
  feature/comparison/
  feature/explore/
  feature/settings/
  navigation/
```

### Phase 2 — Domain contracts and offline data

- Implement the proposed README models with typed IDs/enums where helpful and optional values for unknown metadata. Model article language independently of source defaults, and reporting geography independently of event geography.
- Define `StoryRepository` (observable lists/detail and refresh), `SourceRepository`, `TranslationRepository`, and `PreferencesRepository` contracts. Keep Lens Gap assessments part of fixture story data for now.
- Add Room entities, relationships, transactional seeding, and Flow queries. Seed once by fixture version and ensure restarts do not duplicate rows or overwrite user preferences. Refresh in mock mode deterministically restores/reads the bundled dataset without network access.
- Bind mock implementations with Hilt. Define a minimal future Retrofit API/DTO boundary and OkHttp provision, using a reserved `.invalid` base URL if one is necessary. Never invoke it in mock mode; no live repository is required yet.
- Add a `ReadingStateRepository` backed by DataStore for saved story IDs and last-opened story ID. Keep this state separate from fixture seeding and preserve it on refresh.
- Keep I/O off the main thread. Inject a clock/dispatchers where needed for deterministic tests; use a fixed fixture reference time rather than random generated dates.

Fixture requirements:

- At least six fictional story clusters, eight fictional sources across at least six countries and four regions, and three original languages, including an RTL language.
- At least two story clusters with three or more sources and contrasting, evidence-linked framing observations.
- Include an available demo translation, missing translation, simulated failed translation, disputed claim, sparse-coverage story with no score, equal-score sorting ties, and a broad-agreement demo assessment.
- Use original fictional text and reserved example URLs. Mark article links as demo links in the UI; do not present them as real reporting. Use one shared region taxonomy and country-code mapping.
- Supply deterministic loading/empty/error scenarios through test fakes or a debug-only scenario selector; users must have a retry or reset path where applicable.

### Phase 3 — Reading and comparison

- Home uses the design brief's masthead, lead story, varied supporting layouts, finite mock edition, active sort, reporting-country/source counts, and demo-data disclosure. Tap a story to open Story by ID. Add save/unsave, a Saved filter, and a persisted continue-reading shortcut; handle empty or filtered saved content as described in the design brief.
- Story shows event summary, attributed claims and their assessment states, source coverage, article links, and a comparison action. Unknown IDs display an actionable not-found state.
- CrossLens compares at least two selected articles, showing publisher/country, publication time, original language, original vs translated text, and cited framing observations. Use stacked cards on phones and a readable wider layout when space permits.
- Implement the brief's subtle tilt/slide source-pane transition with visible source-switch controls and reduced-motion support. Bundle credited imagery/fonts and design intentional no-image and long-headline layouts.
- Every screen uses a ViewModel with immutable `StateFlow` UI state and explicit user actions. Collect with lifecycle awareness. Keep filtering/comparison logic out of composables.
- Open article URLs through an external browser intent, handling an absent URL or unavailable browser gracefully. Identify placeholder links before opening them.
- Show all numeric Lens Gap values with an adjacent demo label. The explanation lists illustrative components, sample sources, and limitations. No score means insufficient evidence, not agreement.

### Phase 4 — Discovery and preferences

- Explore combines selected region/country/topic/original-language filters with AND between dimensions and OR within one dimension. Region/country/language filters match source articles; topic filters match story topics. Include a clear-all action and a no-results state.
- Home supports Latest (updated time descending), Coverage breadth (distinct reporting countries descending), and Demo Lens Gap (score descending, unavailable last). Use updated time then stable story ID as tie-breakers where needed.
- Settings persists reading language, home country/region, enabled sources, translation display preference, system/light/dark theme, and reduced-motion preference in DataStore.
- Apply source preferences consistently to lists and article comparisons; a story is visible if at least one enabled source remains. Zero enabled sources shows a recovery action. Home-country preference provides context for future discovery and must not silently hide other countries.
- Since demo scores describe fixed fixture samples, show “Comparison unavailable for this source selection” and exclude the score from ranking if source preferences remove any sampled articles. Do not recompute a score or attach the old score to a different sample.
- Language/translation preference selects available fixture translations, with original-text fallback and a visible status. UI localization is separate and can remain English in this milestone.
- Preserve navigation/back behavior and selections across rotation; persist preferences across process restarts.

### Phase 5 — Validation and accessibility

- Unit-test filter combinations, sort ties/null scores, selected-source effects, translation fallback/status handling, and ViewModel loading/success/empty/error/retry transitions using coroutine test utilities.
- Test Room seeding idempotency and article/source/translation relationships with an instrumented in-memory database. Test persisted preferences across repository recreation.
- Add Compose navigation smoke tests for Home → Story → CrossLens → Back, Explore filtering, and Settings changes.
- Verify light/dark themes, large fonts, screen-reader labels, touch targets, insets, and readable RTL sample text. Keep visible copy in resources and avoid color-only status indicators.
- Test save/unsave and continue-reading persistence, missing/filtered last-opened stories, and Saved filter interactions. Perform the design brief's visual acceptance gate and capture representative screenshots; verify source switching with animations disabled and with reduced motion enabled.
- Run in airplane mode after installation and confirm all five screens, seeded data, and settings work without a backend.

### Phase 6 — Handoff

Update README with actual toolchain versions, import/build/run instructions, reviewed screenshots and asset credits (or an explicit visual-review blocker), and the implemented vs planned feature boundary. Report checks actually executed, any blocked checks, the debug APK path, and remaining work. Keep future ingestion, translation providers, and Lens Gap scoring in the roadmap.

## 4. Build and verify the generated project

Run from the repository root after the wrapper exists (Windows: use `gradlew.bat`):

```sh
./gradlew --version
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

With a running emulator or connected device:

```sh
adb devices
./gradlew :app:connectedDebugAndroidTest
./gradlew :app:installDebug
adb shell am start -n com.crosslens.app/.MainActivity
```

Expected APK location for this single-module, no-flavor build:
`app/build/outputs/apk/debug/app-debug.apk`.

If the SDK is missing, install the documented packages and configure `local.properties` through Android Studio. If Java/Gradle compatibility fails, align the terminal and IDE JDK with the documented toolchain. If no emulator/device is available, report instrumentation and visual/offline checks as **not run**; a successful unit-test run does not replace them.

## 5. Acceptance checklist

- [ ] The design brief's visual acceptance gate is complete: original editorial identity, polished five-screen layouts, inspected screenshots, offline assets, accessible motion, and no unfinished visual placeholders.
- [ ] Saved stories and continue reading survive restart; the finite mock edition is honestly labeled.
- [ ] A fresh checkout builds with the documented toolchain and committed wrapper.
- [ ] Debug APK installs and opens on API 29+.
- [ ] All five screens work with stable-ID navigation and correct back behavior.
- [ ] Room seed is deterministic and idempotent; preferences survive relaunch.
- [ ] Filters, three sort modes, source selections, and no-results recovery work.
- [ ] Original text remains accessible; demo translations and failures are explicit.
- [ ] Demo Lens Gap explanations are visible; unavailable scores remain null and sort last; source filtering never misrepresents a score's sample.
- [ ] Loading, empty, failure/retry, and not-found states are exercised.
- [ ] Mock mode works offline and issues no network requests.
- [ ] Light/dark, large text, accessibility labels, and RTL content are checked.
- [ ] Unit tests, Room/preferences tests, navigation tests, and lint pass, or each blocked check is explicitly reported.
- [ ] README distinguishes implemented behavior from planned features and includes reproducible build/run commands.

Completion means a demonstrable Android skeleton, not a production newsroom or a validated media-analysis system.
