# CrossLens

**One event. A world of perspectives.**

CrossLens is a native Android news reader for comparing available reporting across publications, countries and languages. Publisher geography is context, never a political classification. The editorial visual identity, Kotlin/Compose MVVM architecture, Hilt, Room and DataStore are preserved.

**Current status: production hardening candidate, not production-ready.** No Android build or test passed in the current environment because Java/Android SDK are absent and dependency downloads are blocked. Read the [production readiness report](docs/PRODUCTION_READINESS_REPORT.md), [progress log](docs/PRODUCTION_PROGRESS.md), and [actual command results](docs/verification/2026-11-01/RESULTS.md) before relying on historical beta reports. No app was published or production signing key generated.

## Implemented candidate behavior

- RSS/Atom ingestion uses configured public feeds, publisher metadata, original links and publication timestamps. Undated entries are excluded rather than assigned invented dates. Public-feed and image redistribution rights still need owner review. This revision's real end-to-end ingestion has not been verified on device.
- Room-backed cached reading, idempotent demo fallback, serialized transactional refresh, deterministic URL identities and saved/last-opened retention. Nine evidenced historical schema upgrade paths are implemented; SQLite checks passed, while Room/device upgrade validation remains pending. Unknown schema 7 is not guessed.
- Story reading and automatic related-coverage grouping, attributed headlines/excerpts, publisher/country/language/time context, original reporting routes and adaptive paired comparison cards. Grouping may be wrong; verified framing/claim analysis and live Lens Gap scores are unavailable.
- Home Saved/Continue reading controls, Explore publisher-country/region/original-language filters and search across cached headlines/summaries. Topic metadata is incomplete for live feeds. Settings retains theme/reduced motion, adds reading language, confirmed local-data deletion and configurable public privacy/contact links.
- Constrained periodic WorkManager refresh, HTTPS-only feed/browser paths, lifecycle-aware network indicator, source health and explicit cached/demo states.
- Production translation provider is unconfigured. Labeled fixture translations remain available; a provider/cache boundary handles revision/provider/freshness, cancellation and failure while preserving originals. Feed/publisher language metadata is used; automatic text-language detection remains future work.
- Release remains Free. Preview Plus is debug-only. A dormant Play Billing 9.1.0 gateway supports product queries, pending state, restore, server-verifier boundary and acknowledgement, but is not connected to app DI/UI. No paid purchases or production entitlement are enabled.
- Firebase is optional and unconfigured by default; crash collection is disabled. Publisher and image requests still transmit network information. See the [privacy draft](docs/PRIVACY_POLICY_DRAFT.md) and [Data Safety worksheet](docs/DATA_SAFETY_WORKSHEET.md).

## Build

Use JDK 17, Android SDK platform 36/build-tools 35.0.0 and the checked-in Gradle 8.11.1 wrapper with AGP 8.10.1. Application ID: `com.crosslens.app`; minimum API 29; target API 36; candidate code 16 / `1.0.0-rc01`. The complete dependency/toolchain matrix still needs successful CI validation.

```sh
./gradlew clean
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
./gradlew :app:lintDebug
./gradlew :app:assembleRelease
./gradlew :app:bundleRelease
./gradlew :app:lintRelease
python3 scripts/verify_release_artifacts.py
```

On a configured emulator/device:

```sh
./gradlew :app:connectedDebugAndroidTest
./gradlew :app:installDebug
adb shell am start -n com.crosslens.app.debug/com.crosslens.app.MainActivity
```

Expected outputs after successful builds: debug APK at `app/build/outputs/apk/debug/app-debug.apk`; release APK under `app/build/outputs/apk/release/`; AAB at `app/build/outputs/bundle/release/app-release.aab`. **These artifacts were not generated in this session.** Release is unsigned without the owner's existing credentials. See [signing instructions](docs/RELEASE_SIGNING.md). CI runs debug/tests/lint and optimized unsigned release verification; no CI success is claimed yet.

## Tests and remaining gates

Added regression coverage for seed idempotence, transactional rollback, stable identity, offline/partial failure, saved content, XML/Atom/dates, translation cache/failure/cancellation, subscription access, navigation restoration and Hilt Compose save/compare/back. Android/JVM tests remain unexecuted. Static XML/TOML/YAML checks, nine SQLite migration-structure/preservation checks and synthetic artifact-validator checks passed. These do not prove APK behavior.

Release blockers include compilation/test/lint/R8/device checks, actual beta upgrades, source rights and real ingestion, accessibility/performance/RTL/form-factor validation, complete dependency security review, public privacy/contact pages, Play declarations and signed artifacts. Translation/billing/Firebase need separate owner-configured services before activation. The full [Play checklist](docs/PLAY_STORE_RELEASE_CHECKLIST.md) includes listing copy, assets, internal/closed testing and rollout instructions.

Product/design references: [design direction](docs/DESIGN_DIRECTION.md), [monetization boundaries](docs/MONETIZATION.md), [build guide](docs/ANDROID_BUILD_GUIDE.md), [security audit](docs/SECURITY_AUDIT.md). The original beta README and its proposed contracts are preserved in [beta history](docs/README_BETA_HISTORY.md); historical test counts and milestone claims are not current evidence.
