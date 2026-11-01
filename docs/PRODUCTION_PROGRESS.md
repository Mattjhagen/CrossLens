# Production progress

Started 2026-11-01. Scope: production hardening; no publishing, new signing keys or paid infrastructure authorized.

## Baseline
- Read README, CLAUDE, HANDOFF, Android build guide, design direction, monetization, quality report, signing guide and build runbook. Historical documentation is stale; actual DI uses LiveStoryRepository.
- Original checkout has unrelated untracked `crosslens-codex.log` and `production-prompt.md`; leave untouched.
- `.git` is read-only under the environment policy. Created branch `feature/android-production-readiness` using copied Git metadata at `/tmp/crosslens-production-git`, with this workspace as work tree. Original checkout HEAD remains main. All session Git commands must use `git --git-dir=/tmp/crosslens-production-git --work-tree=/home/matt/CrossLens`.
- No java, Android SDK or adb found. Download probe to Adoptium failed DNS resolution. Build/device verification blocked until toolchain and dependency network access exist.
- Official Google documentation retrieved through web tool: current phone/tablet submission target API 36; AGP 8.10.1 supports API 36 with Gradle 8.11.1 and JDK 17.

## Initial significant findings
See PRODUCTION_READINESS_REPORT.md for audit and disposition. Highest priorities: startup destroys cached content; refresh destroys cache before replacement succeeds; unstable IDs break saved stories; invented dates for undated reporting; uncancellable/unbounded feed HTTP; Firebase initialization without configuration; destructive database upgrades.

## Data integrity and ingestion implementation
- Idempotent transactional fixture seeding preserves live cache and existing IDs.
- Startup seed completes before cache refresh; no constructor-launched refresh job.
- Mutex-protected refresh aggregates fetch results without cross-thread counters. Atomic Room replacement retains saved and last-opened stories, including legacy random IDs. Stable canonical-URL identities preserve repeated-fetch links; changed cluster membership gets a new ID while saved previous clusters remain accessible.
- HTTP callbacks cancel underlying calls; bounded decompressed body reads, closed responses, HTTPS downgrade rejection, call deadline and bounded transient retry.
- RSS/Atom parsing preserves declared language and source timestamps; rejects DTD/entity declarations; undated items are excluded pending nullable-date support.
- Live sources gain registry homepage/geography. Unknown metadata remains absent. No source-wide subscription assumption.
- Real reporting cannot enter the demo digest generator. Live articles no longer carry demo-placeholder URL metadata. Non-demo Lens Gap values cannot render via the fixture mapper.
- Removed destructive migration fallback. This is a data-loss guard, NOT a complete migration solution: historical-version upgrades remain blocked until tested migrations are recovered.
- Added real-Room Robolectric regression tests for seed idempotence, rollback, cache retention, partial/offline fetch, stable identity and cancellation; new parser/adapter and identity tests. These have NOT run: JDK/SDK unavailable.

## Release safety, architecture and UI implementation
- Target/compile API 36, AGP 8.10.1, Gradle 8.11.1 with official distribution SHA-256; application ID unchanged, versionCode increased 15→16, candidate versionName 1.0.0-rc01. This version is not approved for rollout; owner must compare against highest Play Console code.
- Optional Firebase build integration; runtime crash collection defaults off; no Firebase config needed for baseline. Mapping upload requires separate explicit property. Unsigned optimized release builds allowed without generating keys.
- Added connected/battery-not-low periodic WorkManager cache refresh (6h, exponential retry capped to 3 retries), lifecycle-scoped connectivity banner, lifecycle-safe source health collection and cancellation propagation.
- Hardened HTTPS publisher navigation against unsupported schemes and absent browsers; manifest denies cleartext; backup/transfer rules exclude local storage.
- Preview Plus is debug-only and release entitlement always Free. Invalid stored enums fall back safely. Release paywall explicitly says purchases unavailable.
- Added dormant Billing 9.1.0 gateway with products, pending purchases, restore, verifier boundary, post-verification acknowledgement, cancellation and close handling. Pure subscription policy covers grace/hold/canceled/expiry/revocation. Not bound into application DI; no real billing or paid entitlement enabled.
- Translation provider/cache implementation keeps original text, validates revision/provider/7-day freshness, handles failures/cancellation, and defaults real reporting to UNAVAILABLE. Cache revision is encoded in deterministic translation IDs to preserve schema v11. Demo translations retain their existing explicit provenance.
- Added publisher-country/original-language Explore filters and local search; corrected region matching. Added Saved/Continue reading controls; retained original editorial styling and wide-screen paired comparison cards. Live comparison explicitly discloses missing claims/framing evidence and unavailable translation.
- Added reading-language preference and confirmed local data deletion in Settings. No data was deleted during this work.
- Found reflection-based clustering audit on production ingestion path; gated it to debug to avoid R8-renamed private-method failures and verbose release logging.
- Existing Hilt instrumentation test lacked dependencies and test runner: added both plus offline save/compare/back smoke test. Existing SourceHealth unit test now uses a local HTTP interceptor rather than real network.
- Added CI for debug, unit tests, lint, optimized unsigned APK/AAB, reports and integrity checks. CI has not run.

## Actual verification status
- All six requested Gradle commands, connected instrumentation and release lint attempted: each exits 1 before Gradle starts because JAVA_HOME/java is missing. Logs: `docs/verification/2026-11-01/`.
- XML parsing, TOML parsing, workflow YAML parsing, Python artifact-script syntax and `git diff --check` passed. These are static checks, not Android build/test evidence.
- `gh auth status` reports invalid credentials. No authenticated PR creation available. Push/portable handoff results recorded at completion.

## Recovered migration paths
- Recovered entity history for schema versions 1–6 and 8–11. The committed history jumps 6→8; version 7 has no schema evidence and remains unsupported pending recovery of an actually distributed APK/database or confirmation it was never distributed.
- Implemented direct non-destructive migrations from evidenced versions to 11. Added historical SQL fixtures with commit provenance and a Robolectric test that will ask Room to validate every upgraded schema and preserved article.
- Executed migration SQL using Python SQLite against all nine historical fixture schemas: structure and representative article preservation passed for 1,2,3,4,5,6,8,9,10→11. This does NOT prove Room runtime validation or actual installed-beta compatibility; those tests remain blocked by missing JDK/SDK.
- Fixed article navigator's non-terminating initialization/refresh collectors, persisted reader position in SavedStateHandle and updated continue-reading state. Added regression test for background emission and restored position; not executed.

## Final review additions
- Seed repair now fills missing fixture child rows without overwriting existing rows, so an older interrupted seed can recover. Regression added.
- Declared language tags normalize BCP 47 script/region metadata; unknown language stays `und`. No automated text detection is claimed.
- Public privacy/contact build configuration and disabled-unavailable Settings links added. Owner URLs remain required before release.
- Added publisher-geography/language/search regression, release preview fail-closed test, and failed-refresh timestamp test. All Kotlin tests remain unexecuted.
- Artifact verifier exercised with missing output, valid synthetic ZIPs and corrupt ZIP: expected results passed. These synthetic files were isolated in temporary storage, not presented as real Android artifacts.
- Security/privacy/Data Safety/store drafts completed; README now describes the actual candidate. Original README retained as beta history, historical quality/build/handoff reports labeled as superseded.
- Final R8 review found missing preservation of anonymous Gson TypeToken subclasses used by Room list converters with Gson 2.10.1. Added official documented keep rules and a converter round-trip regression; optimized runtime validation remains blocked.
- Wrapper integrity check found the inherited JAR SHA-256 did not match Gradle's published checksums. Retrieved the official v8.11.1 wrapper via authenticated GitHub connector and verified SHA-256 `2db75c40782f5e8ba1fc278a5574bab070adccb2d21ca5a6e5ed840888448046` before replacing it. No unverified wrapper JAR was executed (Java unavailable).
- GitHub connector authentication is available despite invalid CLI authentication. Remote main is one owner-document commit ahead (`Production_Readiness.md`); branch transfer will use that current remote base and preserve the document. Connector transfer/CI results will be recorded after completion.

## GitHub handoff outcome
- Read-only connector access confirmed remote main has only the additional owner document; no unrelated remote changes touched.
- GitHub `create_branch` was rejected by automatic approval enforcement: tool requires approval, session policy is `never`. No remote branch, PR or CI run was created. Did not attempt a different mutation path to bypass this rejection.
- Prepared draft PR body and exact handoff instructions. Portable Git bundle/patch and checksums are generated under ignored `artifacts/` after final commits. These are source handoff artifacts; no APK/AAB exists.
- Work remains incomplete for launch: no Java/Android execution, no runtime ingestion/translation/billing/device/performance proof, remaining policy/source-rights/configuration and routing gates listed in readiness report.
