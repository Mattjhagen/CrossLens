# Claude CLI runbook: build, check, audit, and hand off

Use this runbook to execute the [Android build guide](ANDROID_BUILD_GUIDE.md) and [design direction](DESIGN_DIRECTION.md). Those documents define the product and implementation requirements; this document defines the order of work, quality gates, audit evidence, and recovery process. The repository starts with documentation only. Do not run Gradle commands until the project and wrapper exist.

## 1. Start Claude in the repository

Install the prerequisites in the build guide, then enter your checkout:

```sh
cd CrossLens
claude
```

Paste this instruction into the interactive session:

```text
Read CLAUDE.md, README.md, docs/ANDROID_BUILD_GUIDE.md,
docs/DESIGN_DIRECTION.md, and docs/CLAUDE_BUILD_RUNBOOK.md.
Execute runbook steps 2–11 in order to implement the complete Android
skeleton. Keep docs/BUILD_STATUS.md current. Use each step's quality gate,
record real evidence, fix failures, and continue without asking me to
approve routine implementation decisions. Do not stop after planning.

Treat gorgeous original editorial design as a required deliverable.
Keep the app mock-data-first and offline. Run the architecture, data,
design, accessibility, security, dependency, and performance audits.
Do not invent test results or mark blocked checks as passed. If a missing
tool/device blocks a check, continue independent work, record the exact
blocker and recovery command, and leave the affected gate incomplete.

Do not add live services or publish a release. Preserve unrelated work.
Finish with the APK location, screenshots, test/audit report, unresolved
findings, and an honest ready/incomplete assessment.
```

Alternatively, start a session with the instruction directly:

```sh
claude "Read CLAUDE.md and docs/CLAUDE_BUILD_RUNBOOK.md. Execute steps 2–11, implement the app, fix quality failures, and document verification evidence."
```

To resume after an interruption, enter the same checkout and run:

```sh
claude -c
```

Then ask Claude to read `docs/BUILD_STATUS.md`, inspect the current files, and resume the first incomplete gate. Reuse valid evidence for unchanged work; rerun affected checks after fixes. Never bypass CLI permission controls to make the run unattended. See the [official CLI reference](https://code.claude.com/docs/en/cli-reference) for session commands.

## 2. Inspect and establish the build ledger

**Do:** Inspect Git state, existing code, available Java/Android tools, SDK packages, and connected devices. Preserve existing changes. Resolve a compatible stable toolchain using official Android/Kotlin/library documentation; record exact versions and reference links. Identify missing prerequisites without claiming they are installed.

Create `docs/BUILD_STATUS.md` with a table containing:

| Step | Status | Evidence | Blocker / next action |
| --- | --- | --- | --- |
| 2–11, one row per step | NOT STARTED / IN PROGRESS / PASS / FAIL / BLOCKED | Command result, test report, screenshot, or audit finding | Concrete action |

Record the working revision and a brief summary of uncommitted changes associated with the evidence. Include a requirements-to-test checklist linking the build guide's acceptance criteria to the step that verifies each one. Keep machine-specific paths and secrets out of tracked documentation.

**Gate:** Existing work is understood, the toolchain choice is documented, and every requirement has a planned check. Missing environment prerequisites are explicit.

## 3. Create the runnable foundation and visual system

**Do:** Implement build-guide Phase 1: complete wrapper, pinned versions, Hilt setup, single app module, minimum API 29, navigation shell, and custom Material 3 theme. Establish color/type/spacing/motion tokens and build representative lead-story and source-pane components. Use real bundled demo assets or deliberate text-only compositions, not permanent gray placeholders.

**Check:**

```sh
./gradlew --version
./gradlew :app:assembleDebug :app:lintDebug
```

Inspect a rendered Home preview or emulator screen for editorial hierarchy, original identity, long headlines, and light/dark contrast. Record asset provenance in `docs/ASSET_CREDITS.md` as assets are introduced.

**Gate:** The foundation compiles, relevant lint findings are resolved, and the visual direction is represented by actual components. A design plan alone does not pass the visual check. Do not build more feature layers on top of a broken foundation.

## 4. Implement offline data and test its contracts

**Do:** Implement build-guide Phase 2, including deterministic fictional fixtures, Room relationships and idempotent seeding, repository interfaces, Hilt mock bindings, DataStore preferences/reading state, and the inactive Retrofit/OkHttp boundary.

**Check:** Write targeted tests for seed idempotency, stable relationships, original/translated text separation, translation fallback, fixture evidence references, and persistence across repository recreation. Use coroutine test dispatchers for asynchronous logic. Run the unit tests; run Room instrumentation tests when a device is available.

```sh
./gradlew :app:testDebugUnitTest
./gradlew :app:connectedDebugAndroidTest
```

**Gate:** No duplicate seed records; fixture refresh preserves saved stories/preferences; source/event geography stays distinct; mock repositories have no runtime dependency on network success. An unavailable emulator blocks the Room device gate, not independent UI implementation.

## 5. Build the complete reading experience

**Do:** Implement Home → Story → CrossLens and back, source selection, original/demo-translated content, evidence explanations, save/unsave, continue reading, and the finite mock edition. Implement all loading, empty, error/retry, not-found, and unavailable states. Add the subtle source-pane transition and reduced-motion alternative.

**Check:** Unit-test ViewModel state transitions. Add Compose navigation and state tests, including an invalid story ID and retry recovery. Verify rapid source switching does not leave the title, article body, and source attribution out of sync. Inspect rendered screens as features land.

**Gate:** The reading loop works, demo status is unambiguous, evidence remains reachable, and the visual components feel consistent across screens. No placeholder destinations or inert primary actions remain.

## 6. Complete discovery, settings, and continuity

**Do:** Implement Explore, all three sorts, combined filters, source preferences, language/translation behavior, theme, reduced motion, Saved filtering, and last-opened story behavior exactly as specified in the build guide/design brief.

**Check:** Test filter intersections, ties, null scores, zero enabled sources, missing translations, filtered saved stories, missing last-opened stories, and score invalidation after sampled sources are removed. Confirm the cover respects the chosen sort. Verify preferences, saved IDs, and continue reading after a process restart; rotation alone is not a persistence check.

**Gate:** All five screens are functional; personalization changes visible behavior predictably; the fixed Lens Gap sample is never presented as valid for different source selections.

## 7. Run the automated verification gate

**Do:** Run the complete applicable suite against the integrated app. Do not add empty tests or assertions that merely reproduce the implementation to inflate coverage.

```sh
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
adb devices
./gradlew :app:connectedDebugAndroidTest
```

Inspect generated reports and failures rather than relying on the last console line. Record report paths and test counts from actual output. Typical report directories are `app/build/reports/tests/`, `app/build/reports/androidTests/`, and the lint report under `app/build/reports/`; confirm actual paths for the selected toolchain.

**Gate:** Build, unit tests, lint, and instrumented tests pass. Do not broadly suppress lint or delete failing tests to pass. Explain narrowly justified suppressions. Device-dependent checks remain BLOCKED if no device is available.

## 8. Audit the implementation as a reviewer

Switch from building to reviewing the actual code and rendered behavior. Record findings before fixing them; this is a separate review pass, not a claim of an independent external audit. Create `docs/QUALITY_REPORT.md` with one section per audit below.

| Audit | Inspect and verify | Required evidence |
| --- | --- | --- |
| Architecture | ViewModels expose immutable state; composables do not perform repository I/O; domain models stay separate from persistence/network DTOs; cancellation/lifecycle handling and DI bindings are correct; no main-thread disk/network work. | Specific file references, relevant tests, and findings or a scoped no-findings statement. |
| Data and editorial integrity | Fictional labels, translation provenance, attributed claims, valid evidence IDs, null/unavailable scores, sample-aware ranking, no fabricated freshness or regional generalizations. | Edge-case test results plus screenshots of disclosures and unavailable states. |
| Design and originality | All five screens meet the design brief; hierarchy, image crops, whitespace, typography, empty states, and custom identity are deliberate; flip-inspired motion supports comparison. | Inspected light/dark screenshots with concise observations and corrections. |
| Accessibility | TalkBack reading order/labels, focus, controls usable without gestures, font scaling, contrast, RTL text, narrow layouts, and both system-disabled and in-app reduced motion. | Device/emulator matrix and manual observations; identify any checks not performed. Automated semantics tests alone do not prove accessibility. |
| Security and privacy | Merged manifest permissions and exported components, intent/URL handling, app-private storage, logging, tracked secrets/signing material, cleartext/TLS settings, debug-only controls, unnecessary analytics/SDKs. Accept only intended HTTP(S) article links; handle invalid links safely. | Manifest/code review with file references and results of available secret checks. Never copy discovered secrets into reports. Document expected launcher exports; do not blindly disable every exported component. |
| Dependencies and build supply chain | Pinned direct/resolved dependencies, reputable repositories, wrapper integrity/checksum configuration, asset/library licenses, and known advisories for the actual resolved versions. | Dependency inventory, official advisory/source links with review date, scanner name/version if used, and concrete findings. Lint is not a vulnerability scan; an unavailable advisory check must be reported as incomplete. |
| Performance and offline operation | Cold launch, scrolling, image memory, pane switching under rapid input, repeated navigation, and no network attempts from mock repositories. | Device/API/build type, profiling or observation method, actual results, and limitations. Airplane-mode success alone does not prove zero attempted requests; inspect DI/call paths and add a fail-on-use remote test double where appropriate. |

For dependency inspection, use the generated app's actual resolvable configuration. For the planned no-flavor project:

```sh
./gradlew :app:dependencies --configuration debugRuntimeClasspath
```

Keep observations proportional to the evidence. Debug-build timings are exploratory, not production performance claims. No finding is not proof that an app is vulnerability-free. Real backend authentication, production privacy review, and release signing are outside this skeleton audit.

**Gate:** Every audit has a recorded outcome and evidence, not just a checked box. Findings have severity, impact, reproduction/evidence, and an actionable fix.

## 9. Fix findings and recheck affected behavior

Use this triage rubric in `docs/QUALITY_REPORT.md`:

- **P0 — Critical:** exposed credentials, destructive data loss, or a comparable urgent issue. Fix immediately; do not distribute the affected build.
- **P1 — High:** crashes in primary flows, incorrect source/evidence attribution, broken persistence, misleading comparison results, or an accessibility barrier blocking a core flow. Blocks milestone readiness.
- **P2 — Medium:** meaningful but non-blocking visual, usability, or maintainability defect. Fix within scope; if deferred, record a concrete reason and follow-up.
- **P3 — Low:** small polish improvement. Address where practical and record remaining items.

For each finding record: ID, severity, affected file/screen, evidence, fix, regression check, and status (OPEN / FIXED / DEFERRED). Re-run the affected tests and manually re-inspect changed visuals. Re-run the integrated automated suite once fixes are complete. Avoid repeated full runs with no relevant changes.

**Gate:** No open P0/P1 findings, no failed mandatory acceptance checks, and no unresolved design/accessibility requirement disguised as a low-priority improvement. Deferred P2/P3 items are visible in the handoff. A mandatory check blocked by the environment leaves readiness incomplete.

## 10. Perform the end-to-end device acceptance pass

Install and open the debug app on a development emulator/device:

```sh
./gradlew :app:installDebug
adb shell am start -n com.crosslens.app/.MainActivity
```

Use a dedicated test emulator for fresh-install checks; uninstalling or clearing storage erases its saved app data. Keep the persistence check separate from the fresh-install check.

Walk through this sequence and record the outcome:

1. Open the mock edition; inspect the cover, supporting layouts, honest timestamps, and finite ending.
2. Open a story, save it, enter comparison, switch sources, inspect evidence, and return.
3. Select available and unavailable translations; confirm original text and status remain clear.
4. Exercise every sort and combined Explore filters, including a no-results recovery.
5. Disable sampled sources; verify comparison score invalidation and visible coverage counts. Disable all sources and recover through settings.
6. Change theme and reduced motion; inspect all five screens and loading/empty/error states.
7. Force-stop and relaunch without clearing storage; confirm preferences, saves, and last-opened story persist.
8. Disable connectivity; relaunch and repeat the primary reading loop. Article browser links are external navigation, not a required offline content feature.
9. Check large text, a compact display, RTL content, TalkBack, and source controls without swipe gestures.
10. Capture reviewed screenshots under `docs/screenshots/`. Cover all screens in light/dark mode and representative edge states; record device dimensions/API. Exercise API 29 and a current supported API when available; explicitly record gaps.

**Gate:** The device checklist and the design brief's acceptance gate pass with recorded observations. Missing screenshots or inaccessible device checks remain incomplete, not implied successes.

## 11. Prepare the handoff and reproducible checks

Update README with actual setup, toolchain, build/run commands, APK path, screenshots, implemented features, and remaining limitations. Complete `docs/BUILD_STATUS.md` and `docs/QUALITY_REPORT.md`.

The quality report must include:

- Scope and tested revision/working-tree state, environment, device/API matrix, and date.
- Every command run, result, test counts, and report locations; clearly separate not-run checks.
- Audit findings, fixes, regression evidence, and remaining risks/blockers.
- Links to reviewed screenshots and asset credits.
- A final **READY FOR SKELETON REVIEW** or **INCOMPLETE** decision with reasons. This is not production-release certification.

Add a minimal GitHub Actions workflow that uses the pinned compatible JDK/Android toolchain to run debug assembly, unit tests, and lint, and uploads useful test/lint reports. Keep emulator checks local unless CI emulator support is configured and actually verified. Use minimal workflow permissions and no secrets for the mock build. Do not claim CI passed until an actual run has completed successfully; if it has not run, record that separately from local results.

Review the diff, check formatting, and verify no local paths, credentials, build directories, or unrelated changes are included:

```sh
git diff --check
git status --short
```

If committing was authorized for the implementation session, stage only reviewed app/docs/CI files and create a descriptive commit. Push only when authorized. Do not publish an APK, create a release, or deploy services as part of this runbook.

**Final response format:** implemented experience; APK and screenshot locations; passed checks; incomplete checks; audit findings fixed/remaining; readiness decision. Never mark the milestone complete while a mandatory gate is failed or blocked.
