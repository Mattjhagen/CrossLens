# CrossLens — Android Production Readiness & Google Play Release

You are the lead Android engineer, mobile architect, security reviewer, and release engineer responsible for taking CrossLens from its current beta state to a production-quality Android application.

**Repository:** https://github.com/Mattjhagen/CrossLens  
**Application ID:** `com.crosslens.app`  
**Platform:** Native Android — Kotlin, Jetpack Compose, Material 3  
**Goal:** Prepare CrossLens for a real Google Play production launch, not merely a successful debug build.

## Operating instructions

Work autonomously through the entire project. Do not stop after planning or auditing. Implement fixes, run tests, verify results, and continue to the next phase.

Read all repository instructions and relevant documentation before modifying code, including:

- `README.md`
- `CLAUDE.md`
- `HANDOFF.md`
- `docs/ANDROID_BUILD_GUIDE.md`
- `docs/DESIGN_DIRECTION.md`
- `docs/MONETIZATION.md`
- `docs/QUALITY_REPORT.md`
- `docs/RELEASE_SIGNING.md`

Inspect the actual source code rather than assuming documentation is current.

Preserve existing features, architecture, branding, user data, and working functionality.

Create a feature branch named `feature/android-production-readiness`. Never force-push or overwrite unrelated changes.

Make incremental, logical commits. Keep a running progress log in `docs/PRODUCTION_PROGRESS.md`.

You are authorized to install necessary local development dependencies, modify project files, run builds, execute tests, and create commits. Do not publish the app, deploy paid infrastructure, purchase services, create production credentials, or expose secrets without explicit owner approval.

When external credentials or human decisions are required, document the blocker and continue all independent work.

## Phase 1 — Comprehensive repository audit

Inspect the entire Android application.

Identify:

1. Incomplete or placeholder features.
2. Crashes, lifecycle bugs, and navigation problems.
3. Deprecated APIs and outdated dependencies.
4. Missing error handling and loading states.
5. Performance bottlenecks and memory leaks.
6. Security vulnerabilities.
7. Accessibility problems.
8. Broken or incomplete data persistence.
9. Missing tests.
10. Google Play policy and publishing requirements.

Inspect Gradle, Kotlin, Android Gradle Plugin, Compose, Room, Hilt, Firebase, and all other dependencies.

Update the Android target SDK to the currently required Google Play level, verifying compatibility and behavior changes using official documentation.

Preserve `com.crosslens.app` and existing user-data compatibility.

Document every significant finding with severity, affected files, and recommended resolution.

Fix critical and high-severity issues before moving on.

## Phase 2 — Production architecture

Preserve the existing MVVM architecture, dependency injection, Room database, and DataStore preferences.

Implement or improve:

- Clean repository interfaces.
- Offline-first data synchronization.
- Structured coroutine concurrency.
- Lifecycle-safe state collection.
- Network connectivity monitoring.
- Request cancellation and timeouts.
- Retry behavior with exponential backoff.
- Proper loading, empty, and failure states.
- Safe database migrations.
- Background synchronization using WorkManager when appropriate.
- Reliable caching and cache expiration.

Ensure the application remains useful without an internet connection.

Never perform network or database operations on the main thread.

## Phase 3 — Real news ingestion

CrossLens must evolve beyond fictional demonstration content.

Build a production-ready data integration layer that supports real news sources.

Requirements:

- Retrieve news articles from legitimate, configurable providers.
- Support RSS/Atom and licensed APIs where appropriate.
- Normalize article metadata.
- Preserve original article URLs.
- Record publisher, country, language, and publication timestamp.
- Deduplicate syndicated or repeated articles.
- Group related reporting about the same event.
- Support global and regional news.
- Handle unavailable feeds and malformed responses.
- Store cached content locally.
- Clearly identify unavailable or stale content.

Do not invent articles, sources, quotes, or publication dates.

Do not scrape paywalled content, bypass access restrictions, or reproduce copyrighted full articles without permission.

Use a secure backend where required to protect API keys. Never embed provider secrets inside the APK.

If no production provider or backend is configured, implement the integration interfaces, a working lawful public-feed path where feasible, and an explicitly labeled demo fallback.

Do not claim live production functionality unless real end-to-end ingestion has been verified.

## Phase 4 — CrossLens comparison engine

This is the application's defining feature.

Users should be able to select one news event and compare how different publications report it.

Improve the comparison experience to include:

- Side-by-side reporting.
- Original headlines and attributed excerpts.
- Publisher and geographic context.
- Publication timestamps.
- Differences in emphasis and framing.
- Claims reported by each publication.
- Evidence supporting comparison observations.
- Links to original reporting.
- Clear explanations of unavailable evidence.

Do not assume a publication's country determines its political position.

Do not present AI-generated interpretations as verified facts.

Separate factual observations, editorial interpretations, and uncertain assessments.

Implement a transparent comparison data model that can support future AI analysis without requiring an AI service for basic functionality.

Never manufacture a Lens Gap score. Show scores only when an implemented, documented, evidence-backed methodology supports them.

## Phase 5 — Translation

Prepare CrossLens for genuine multilingual news comparison.

Implement:

- Language detection.
- Translation-provider abstraction.
- Original/translated text switching.
- Translation caching.
- Translation failure handling.
- Source-language attribution.
- Provider and generation metadata.
- RTL language compatibility.

Keep original reporting accessible.

Never silently substitute generated translations for original quotations.

If no translation provider is configured, preserve the existing demo functionality with unmistakable demo labels and a clear production-disabled state.

Do not ship mock translations as genuine translated journalism.

## Phase 6 — UI/UX production polish

Maintain CrossLens's existing premium editorial identity.

The application should feel like a polished news magazine rather than a generic Material Design demo.

Review and improve every screen:

**Home**
- Editorial story layouts.
- Breaking and recent news presentation.
- Loading and refresh states.
- Saved stories.
- Continue reading.
- Clear timestamps and source attribution.

**Story**
- Readable article summaries.
- Publisher information.
- Attributed claims.
- Related coverage.
- Original reporting links.

**CrossLens**
- Smooth perspective switching.
- Side-by-side comparisons where screen size allows.
- Readable source distinctions.
- Translation controls.
- Accessible motion.

**Explore**
- Working country, region, topic, and language filters.
- Search where supported.
- Useful empty states.

**Settings**
- Reading language.
- Region.
- Theme.
- Reduced motion.
- Privacy controls.
- Notification preferences where applicable.
- Subscription management when billing is implemented.

Test small screens, large screens, landscape, dark mode, font scaling, RTL, and screen readers.

Avoid unnecessary redesigns that erase the existing CrossLens identity.

## Phase 7 — CrossLens Plus

Preserve the documented Free and Plus feature boundaries.

Implement a production-ready billing architecture using the currently supported Google Play Billing Library.

Requirements:

- Product configuration abstraction.
- Purchase lifecycle handling.
- Pending purchases.
- Purchase acknowledgment.
- Restore purchases.
- Subscription state handling.
- Grace periods and account holds.
- Cancellation and expiration behavior.
- Server-side purchase verification design.
- Clear subscription disclosures.

Never trust a local boolean as proof of a paid entitlement.

Do not activate real billing until valid Play Console products and verification infrastructure are configured.

Keep preview entitlements confined to development or explicitly labeled demonstration flows.

If billing cannot be completed without owner credentials, implement and test everything possible, document the exact remaining steps, and prevent fake production purchases.

## Phase 8 — Security and privacy

Conduct a thorough security review.

Check:

- Android manifest permissions.
- Exported activities and components.
- Network security configuration.
- HTTPS enforcement.
- API key exposure.
- Sensitive logging.
- DataStore and Room privacy.
- Backup and restore policies.
- Deep links.
- WebView or Custom Tabs behavior.
- Dependency vulnerabilities.
- Firebase configuration.
- Crash-reporting privacy.
- User data deletion behavior.

Use least-privilege permissions.

Avoid unnecessary device identifiers, tracking, or personal data collection.

Prepare an accurate privacy-policy draft and Google Play Data Safety inventory based on actual implemented behavior.

Do not claim zero data collection if Firebase, analytics, or other services transmit information.

## Phase 9 — Testing and performance

Expand automated tests across:

- ViewModels.
- Repository implementations.
- Database migrations.
- News ingestion and deduplication.
- Story clustering.
- Translation state handling.
- Subscription entitlements.
- Navigation.
- Offline behavior.
- Error recovery.

Add instrumentation and Compose UI tests for critical user journeys.

Run all available tests and lint checks.

Verify:

- Cold startup.
- Memory usage.
- Scrolling performance.
- Image loading.
- Network efficiency.
- Battery impact.
- Offline launch.
- Background/foreground transitions.
- Process death and restoration.

Use actual emulator or device testing whenever available.

Do not claim a device test passed if no device was available.

## Phase 10 — Google Play release engineering

Prepare a genuine release pipeline.

Requirements:

- Current compatible Android toolchain.
- Appropriate target SDK.
- Stable application ID.
- Monotonically increasing versionCode.
- Production versionName.
- Release R8 optimization.
- Correct ProGuard/R8 rules.
- Android App Bundle generation.
- Secure release signing.
- Crashlytics mapping upload where configured.
- GitHub Actions CI.
- Automated unit tests and lint.
- Release build verification.
- Artifact integrity checks.

Generate an AAB with:

`./gradlew :app:bundleRelease`

Use existing signing credentials only if securely available.

Never generate or replace an existing production signing key without explicit approval.

If credentials are unavailable, verify an unsigned release artifact or suitable build tasks and provide the exact steps needed to finish signing.

Prepare:

- Google Play listing description.
- Short description.
- Release notes.
- Screenshot checklist.
- Feature graphic requirements.
- Content rating checklist.
- Privacy policy draft.
- Data Safety worksheet.
- Internal testing instructions.
- Closed testing instructions if required.
- Production rollout checklist.

Do not publish to Google Play automatically.

## Phase 11 — Final verification

Execute:

`./gradlew clean`

`./gradlew :app:assembleDebug`

`./gradlew :app:testDebugUnitTest`

`./gradlew :app:lintDebug`

`./gradlew :app:assembleRelease`

`./gradlew :app:bundleRelease`

Run additional instrumentation and security checks where supported.

If a command fails, investigate and fix the underlying issue rather than hiding the failure.

Record the actual command results.

Create:

- `docs/PRODUCTION_READINESS_REPORT.md`
- `docs/PRODUCTION_PROGRESS.md`
- `docs/PLAY_STORE_RELEASE_CHECKLIST.md`
- `docs/SECURITY_AUDIT.md`
- `docs/PRIVACY_POLICY_DRAFT.md`
- `docs/DATA_SAFETY_WORKSHEET.md`

The readiness report must distinguish:

- Verified working functionality.
- Implemented but unverified functionality.
- Demo-only functionality.
- Blocked external integrations.
- Remaining release blockers.

Do not declare the application production-ready while critical blockers remain.

## Phase 12 — GitHub handoff

Review the final diff for unintended changes, secrets, generated artifacts, and regressions.

Commit changes in logical increments.

Push the feature branch to GitHub if authenticated access is available.

Create a pull request against `main` if supported.

Do not merge automatically.

Provide a final summary containing:

1. What was fixed.
2. What was implemented.
3. What was tested and the results.
4. Remaining blockers.
5. Release artifact locations.
6. What the owner must configure.
7. Whether CrossLens is ready for internal testing, closed testing, or production release.

## Execution priority

Prioritize in this order:

1. Crash prevention and build reliability.
2. Security and data integrity.
3. Real news ingestion and attribution.
4. Core CrossLens comparison experience.
5. Offline reliability.
6. UI polish and accessibility.
7. Translation integration.
8. Billing readiness.
9. Performance and testing.
10. Google Play release preparation.

Continue autonomously between phases. Do not ask for approval for ordinary code changes or tests. Ask only when a required decision involves external services, money, credentials, irreversible actions, or product policy.

**Start by inspecting the actual repository and creating the production readiness audit. Then implement the highest-priority fixes and continue through the phases. The objective is a working, tested, maintainable Android application—not just documentation or a checklist.**
