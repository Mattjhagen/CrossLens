# Production readiness audit

Date: 2026-11-01. Baseline: versionCode 15 / 0.0.15-beta. **NOT production ready.** Historical passing test claims do not describe this session.

| Severity | Finding / affected files | Required resolution |
|---|---|---|
| Critical | MockStoryRepository.seedData deletes every story, article and source on startup, including live cache | Transactional idempotent fixture insert; never delete live/user data |
| High | LiveStoryRepository deletes live cache before successful persistence, concurrent refreshes race, shared fetch counters race | Serialize refresh; atomic commit; aggregate results without shared counters |
| High | LiveStoryRepository randomizes story/article IDs on every refresh | Stable URL-derived identifiers and retention for saved/deep-linked content |
| High | RssSourceAdapter substitutes current time for missing dates | Skip undated records until nullable-date model/UI exists; never invent publication times |
| High | RSS HTTP response unclosed on error; reads entire body before size check; blocking execute ignores coroutine cancellation | Close every response; bound decompressed bytes; cancellable async call and total timeout |
| High | DatabaseModule destructive upgrade fallback, no historical schema exports tracked | Remove destructive fallback; recover historical schemas and validate every shipped-version migration before release |
| High | Firebase plugins require absent google-services.json; Application assumes initialized Firebase | Optional explicit Firebase configuration; collection disabled by default; verify configured variant separately |
| High | Preview entitlement persisted locally in release; enum parsing can crash startup | Release fail-closed to Free; debug-only preview; tolerant enum parsing |
| High | SourceDigestGenerator fabricates agreement for arbitrary live coverage | Disable generated demo digest for real articles |
| High | Public feed availability mistaken for permission in EXPLICIT_EXCERPT metadata | Publication-specific content rights review; no unlicensed full text; preserve attribution |
| Medium | Sources persisted with empty geography and invalid homepage | Populate registry geography/language and feed-derived homepage |
| Medium | SourceHealthScreen uses lifecycle-unaware collection; broad catches swallow cancellation | Lifecycle-aware collection; rethrow cancellation in suspend paths |
| Medium | External browser fallback may throw ActivityNotFoundException; incoming schemes not centrally restricted | Shared HTTPS browser helper with graceful failure |
| Medium | API 34 / AGP 8.5.2 cannot satisfy current Play submission | API 36, compatible AGP/Gradle; Android 15/16 behavior regression checks |
| Medium | No CI, unsigned release blocked, wrapper lacks distribution checksum | Reproducible debug/test/lint/release pipeline; checksummed artifacts |
| Medium | Translation is fixture-only, billing absent, no trustworthy production entitlement | Explicit disabled production states; provider contracts and server verification design |
| Medium | Source filters/topics for live reporting incomplete; claims/framing evidence unavailable | Honest absence states; add supported metadata filters; no invented analysis |
| Medium | No device/performance/accessibility evidence for current revision | API 29/36 upgrade, offline/process death, RTL/font scaling/TalkBack/tablet tests |

## Verification categories

- **Verified by source inspection:** native Compose/MVVM/Hilt architecture, Room v11, DataStore reading/preferences; live RSS path wired through DI; null live Lens Gap scores; existing clustering tests and comparison screens.
- **Runtime verified this session:** none yet; no JDK/SDK/device.
- **Demo-only:** fixture news, local news, translations, editorial review, personal recommendations, Lens Gap values, Preview Plus, generated digest.
- **External blockers:** source usage rights, translation service, Play products and verification backend, Firebase project/consent decision, owner signing key, Play account/policy declarations and public privacy URL.
- **Release blockers:** compile/test/lint/release verification, tested non-destructive historical migrations, truthful live comparison/translation behavior, source permissions, accessibility/device/performance matrix, signed AAB and Play declarations.

## Official references reviewed

- [Play target SDK](https://developer.android.com/google/play/requirements/target-sdk): API 36 for new phone/tablet submissions and updates from August 31, 2026.
- [AGP 8.10 compatibility](https://developer.android.com/build/releases/agp-8-10-0-release-notes): API 36, Gradle 8.11.1, JDK 17.
- [Android 16 behavior changes](https://developer.android.com/about/versions/16/behavior-changes-16): edge-to-edge, predictive back, adaptive large-screen layout must be device-tested.
- [Billing releases](https://developer.android.com/google/play/billing/release-notes): 9.1.0 is currently documented; no real purchases enabled.

## Candidate disposition after implementation

All “fixed” statements below mean implemented and source-reviewed; Android compilation/runtime proof is still absent.

| Baseline finding | Candidate disposition |
|---|---|
| Destructive startup seeding | Fixed in source; transactional insert of only missing fixture IDs. Added idempotence/live-cache test. |
| Non-atomic/concurrent refresh and lost saved references | Mutex/transaction, deterministic identities, saved/last-opened retention and failed-refresh state implemented. Added real-Room rollback/retention tests. Changed cluster membership may create a new cluster ID; saved previous clusters remain retained. |
| Destructive upgrades | Removed fallback; reconstructed migration paths for evidenced schemas 1–6,8–10→11. Nine SQLite structure/data-preservation checks passed. Actual Room validation and installed APK upgrades remain required. No schema-7 evidence exists. |
| Invented publication time and guessed unknown language | Undated reporting excluded; unknown source language is `und`, not silently English. Feed/publisher metadata used where available. Automated text-language detection is not implemented. |
| RSS bounds/resources/cancellation/Atom | Implemented bounded cancellable HTTP, resource closure, HTTPS, transient retry and Atom support; tests added, not run. |
| Fictional digest on real journalism; live URLs treated as placeholders | Real articles rejected by demo generator; production scores cannot enter fixture mapper; live URL metadata corrected. |
| Lost reader position after background refresh | Fixed non-terminating collectors; snapshot loading plus SavedStateHandle restoration and last-opened updates. Regression test added, not run. |
| Release preview entitlement and preference parsing crash | Debug-only preview; release Free; invalid access/theme/translation preference values safely fall back. Tests added, not run. |
| Firebase clean-checkout build/startup risk | Plugins explicitly optional; initialized Firebase guard and automatic collection false. Configured variant/privacy verification remains blocked. |
| Main-thread work/lifecycle | Room calls remain suspend; network/clustering use IO; source-health lifecycle collection and cancellation propagation improved. New connectivity callback is subscription-owned. No runtime StrictMode proof yet. |
| External URLs and backups | Shared HTTPS helper with browser failure handling; cleartext denied, transfer/cloud exclusions and confirmed local deletion. Runtime verification pending. |
| SDK/toolchain/CI | API 36, AGP 8.10.1, Gradle 8.11.1 + official checksum, code 16; CI and unsigned R8 release checks implemented. Retained dependency matrix and added Billing/WorkManager/Robolectric require actual resolution/build testing. |
| Explore/Home/UI | Region mismatch fixed using reporting-source geography; country/language search/filter controls, Saved/Continue reading, wide comparison pairing, reading-language preference and unavailable-evidence disclosures added. Visual/accessibility behavior unverified. |
| Translation | Provider/cache abstraction implemented with provider/revision/freshness checks, original preservation, explicit unavailable/failure; default provider disabled; fixture translations preserved. No live translation or automatic language detection claim. |
| Billing | SDK 9.1.0 dormant gateway, pending/restore/verification/acknowledgement/close paths and pure subscription status policy implemented. No app DI binding, actual verification backend, Play products or paid-entitlement activation. Cannot claim real billing works. |

## Verification ledger

| Evidence | Actual result |
|---|---|
| All six requested Gradle commands | Attempted, each exit 1: JAVA_HOME unset and java absent. None reached configuration/compilation. |
| connectedDebugAndroidTest, lintRelease | Same environment failure. No emulator/device/adb available. |
| New/modified JUnit, Robolectric and Compose tests | Written, **not executed**. No passing Android/JVM test count claimed. |
| XML/TOML/YAML/Python syntax | Parsed successfully; final checks recorded in verification directory. |
| Migration SQL on reconstructed historical SQLite fixtures | Nine paths passed table structure and sample-article preservation checks; not Room validation. |
| Artifact verifier | Rejects missing/corrupt archives; accepts synthetic APK/AAB ZIP fixtures and writes SHA-256. No real app artifacts tested. |
| Limited current tracked-text secret scan | No private-key/AWS access-ID/GitHub-token pattern matches. No full history/advisory scan. |
| git diff --check | Passed on reviewed changes. |
| Performance/TalkBack/RTL/font scaling/large screens/offline process death | Not run. Existing screenshots are historical, not evidence for this revision. |
| Live end-to-end ingestion / Firebase / Play Billing / translation | Not verified. Provider/backend integration defaults disabled as documented. |
| GitHub CI/PR | Workflow written; local gh authentication invalid and shell dependency network unavailable. No successful CI run or PR claimed. |

## Phase status and remaining work

1. **Audit:** Source-focused audit and dependency inventory completed for build/DI/persistence/ingestion/entitlement and selected reading/navigation screens. A full dynamic audit is incomplete.
2. **Architecture:** Cache integrity, cancellation, WorkManager and connectivity implemented; partial-source retention and cache lifecycle need real-network/performance evaluation. Old unsaved records from a failing publisher can disappear after another publisher succeeds; persisted saved/last-opened records remain protected.
3. **News:** Working integration path exists in source, now hardened for RSS/Atom. End-to-end availability, publisher terms/attribution rights, region balance and malformed-source behavior remain unverified. Syndication/editorial prototypes are not a complete production deduplication review pipeline.
4. **Comparison:** Original attributed coverage and wide paired layouts supported; no verified claim extraction, framing assessment, AI interpretation or real Lens Gap methodology. Grouping remains heuristic and explicitly uncertain.
5. **Translation:** Abstraction/cache/failure states implemented; provider, automatic detection and multilingual device validation incomplete.
6. **UI:** Targeted fixes implemented. Complete source-preference consistency, home-country/region controls, full error-retry flows and the requested visual/accessibility matrix remain open. Live topic metadata is incomplete.
7. **Plus:** Dormant SDK integration and subscription-policy tests added. Server verification, RTDN, durable acknowledgement, account binding, production entitlement integration and licensed Play testing are blockers. Free/Plus boundary needs a final audit across newer navigator/event-comparison routes before any monetized launch.
8. **Security/privacy:** Source review and drafts delivered; actual release/network/dependency/backup/deletion checks incomplete. Public privacy and news-entity contact pages must be supplied.
9. **Testing/performance:** Expanded tests written; all Android execution and performance profiling blocked. Existing source-health instrumentation also exercises real source refresh and needs a connected network or deterministic DI replacements when running its full journey.
10. **Release:** CI and unsigned release verification path implemented, listing/checklists drafted. No AAB/APK generated or signed. Owner must confirm version code against Console and signing lineage.
11. **Final verification:** Required commands actually attempted; blocked before Gradle. Critical release gate remains open.
12. **Handoff:** Logical commits use writable temporary Git metadata because original `.git` is read-only. See progress log for push/bundle outcome. No merge/publication.

## Readiness decision

**Not ready for production or closed testing. Not yet verified ready for an internal-testing upload.** After builds, lint, tests, release startup, upgrade integrity and signing pass, internal testing is the next appropriate destination. All remaining product/policy/rights/accessibility and service gates must be resolved before broader release. The requested full production objective is incomplete; this candidate is a reviewable implementation with explicit blockers, not release certification.

## Owner configuration and artifacts

- Provide an Android-capable runner with dependency-network access and run the exact command ledger; review CI result and all new tests.
- Supply existing upload keystore securely and confirm Play App Signing/version history. Do not generate replacement credentials to bypass a failure.
- Host owner-reviewed privacy and public contact/entity pages; configure `crosslens.privacyPolicyUrl` and `crosslens.contactUrl`. Complete actual Data Safety, news declaration, rating, audience and territory decisions.
- Approve each feed/image usage right and verify real ingestion. Choose translation backend/detection and paid-service launch policy; leave unconfigured integrations disabled.
- For billing, configure Play products and authenticated server verification/RTDN/durable acknowledgement before wiring the gateway or paid entitlements.
- For Firebase, configure correct packages only if approved; re-audit traffic/consent and mapping upload before enabling diagnostics.
- Recover any distributed schema-7 database/APK or confirm no version-7 release exists; validate upgrades from all actual beta distributions.
- Expected future artifacts: `app/build/outputs/apk/debug/app-debug.apk`, release APK under `app/build/outputs/apk/release/`, `app/build/outputs/bundle/release/app-release.aab`, mapping at `app/build/outputs/mapping/release/mapping.txt`, integrity ledger at `app/build/outputs/artifact-sha256.txt`. **All app artifact paths are currently absent.**

### Final GitHub outcome

Read-only GitHub connector authentication succeeded, but branch creation was rejected by automatic approval enforcement (`requires approval`, session policy `never`). No remote branch/PR/CI run exists. The earlier invalid-CLI status is not the only blocker. Current remote main's extra owner document was preserved unchanged. Local feature commits and portable bundle/patch handoff are described in [GitHub handoff](GITHUB_PRODUCTION_HANDOFF.md). The inherited wrapper JAR was replaced with the official 8.11.1 binary after published-hash verification; Java was absent throughout, so no unverified wrapper was executed.
