# Play release checklist

2026-11-01. **No upload or publication authorized/performed. Not ready for a testing-track upload yet: build gates have not run successfully.** The first eligible destination after build/security/upgrade checks is internal testing. Closed and production release need further evidence below.

## Build and artifact gates

- [ ] Install JDK 17 and Android SDK platform 36, build-tools 35.0.0, platform-tools; use Gradle wrapper 8.11.1/AGP 8.10.1. Validate retained Kotlin/KSP/Hilt/Compose/Room versions as a complete matrix in CI. No blind “latest version” claim.
- [ ] Run `./gradlew clean`, `:app:assembleDebug`, `:app:testDebugUnitTest`, `:app:lintDebug`, `:app:assembleRelease`, `:app:bundleRelease`, `:app:lintRelease` separately and archive results.
- [ ] Run `python3 scripts/verify_release_artifacts.py`. It checks ZIP integrity/required entries and writes SHA-256; it does not validate signatures, manifest policy values or runtime behavior.
- [ ] Confirm `com.crosslens.app`, minSdk 29, targetSdk 36, versionCode 16 (or greater than every previously uploaded Play code), candidate `1.0.0-rc01`; choose final production version label only after acceptance.
- [ ] Inspect merged release manifest, permissions, exported components, release DEX and resolved SDK advisories. Verify no preview unlock, debug fixtures, production secrets or reflection-only audit behavior survives release optimization.
- [ ] Validate AAB with official bundletool; inspect base manifest/version/target; inspect any native libraries and verify [16 KiB page support](https://developer.android.com/guide/practices/page-sizes) on an appropriate emulator/device.
- [ ] Preserve R8 mapping at `app/build/outputs/mapping/release/mapping.txt`; verify optimized release startup and network ingestion. Do not assume debug success proves R8 behavior.
- [ ] Owner supplies existing upload key securely; enable Play App Signing under owner control. See [signing guide](RELEASE_SIGNING.md). Never replace an existing signing key to make a build pass.
- [ ] Verify APK with `apksigner verify --verbose --print-certs`; verify AAB upload signature using `jarsigner -verify -verbose -certs`; compare expected public certificate fingerprint. No debug key on release.

[Play requires API 36 for current phone/tablet submissions.](https://developer.android.com/google/play/requirements/target-sdk) Artifact paths once built: `app/build/outputs/apk/debug/app-debug.apk`, unsigned release APK under `app/build/outputs/apk/release/`, `app/build/outputs/bundle/release/app-release.aab`. **None was generated in this session.**

## Product and integration gates

- [ ] Run added seed/cache/transaction/identity/translation/billing/navigation regression tests. Run migration validation for all evidenced schema versions and APK-to-APK updates with existing reading data. Recover schema 7 or confirm it was never distributed.
- [ ] Verify real RSS/Atom ingestion end-to-end on device. Document configured publishers, returned original URLs/dates/languages, observed unavailable sources and cache behavior. Public-feed rights and image licenses need per-publisher review; no full-article scraping.
- [ ] Explicitly label demo local news, translations, Lens Gap and editorial review. Default live reporting has no verified claims/framing analysis or numeric score. Review cluster false positives, syndication and changing membership with real data.
- [ ] Decide whether launch includes only free reading while Plus is unavailable. Default release stays Free with purchasing disabled. Do not market active subscriptions/translations/AI analysis unless configured and verified.
- [ ] To enable Plus later: Play products/base plans/offers, licensed-test accounts, authenticated backend verification, package/product/account binding, RTDN reconciliation, durable acknowledgement retries, grace/hold/cancel/refund/expiry tests and server-controlled entitlements. The dormant gateway alone is insufficient.
- [ ] To enable translation: select approved backend/provider, define content/privacy terms, wire provider in DI, verify actual detection/provenance/cache/RTL/original switching. Current language attribution uses feed/publisher metadata; automated text-language detection is not implemented.
- [ ] Decide Firebase crash-reporting/consent policy. Baseline integration is unconfigured and collection disabled. Configure matching application variants only after privacy/network review. Mapping upload is explicitly optional.

## Device acceptance matrix

- [ ] API 29 and API 36 phones; tablet/foldable; compact and landscape windows; light/dark; 200% font/display scaling; Arabic RTL; TalkBack and keyboard navigation; reduced motion and system animations disabled.
- [ ] Cold launch and scroll profiling, image failure/slow network, peak memory, battery/background work, foreground/background transitions and offline relaunch.
- [ ] Save → refresh → process death/relaunch → saved article; continue reading; reader swipes without refresh resets; Compare → source link → Back; missing browser/invalid/deep-linked IDs; preferences after update.
- [ ] Offline refresh, partial/total feed outage, malformed/oversized feeds, unknown dates, canceled refresh, database write failure and retry. Verify cached reporting stays accessible and timestamps remain truthful.
- [ ] Confirm local-data deletion with confirmation; verify backup/transfer policy on device. No device test was performed during this session.

## Play Console declarations

- [ ] Owner account verification, developer identity/contact/entity URL and application ownership.
- [ ] Publish reviewed [privacy policy](PRIVACY_POLICY_DRAFT.md) at public HTTPS URL; configure in-app link. Complete [Data Safety](DATA_SAFETY_WORKSHEET.md) using built-release traffic and SDK inventory.
- [ ] Complete News & Magazines declaration. Ensure sources, dates, ownership/contact information and original reporting routes are accessible. [News policy requires accessible contact information](https://support.google.com/googleplay/android-developer/answer/9935326); [declaration guidance](https://support.google.com/googleplay/android-developer/answer/16189314) covers aggregators too.
- [ ] Complete IARC content-rating questionnaire using actual news content (including possible violence/politics/disturbing content), target audience, countries, ads declaration, app-access instructions and any sensitive-content declarations. Do not preassign “Everyone” without review.
- [ ] Choose News & Magazines category, support contact, copyright/content-use permissions and distribution territories. App has no login requirement in default configuration.

## Listing copy — draft for the actual configured launch

**App name:** CrossLens

**Short description (65 characters):** Compare global news coverage and keep original reporting in view.

**Full description:**

One event. A world of perspectives.

CrossLens brings reporting from different publishers into an editorial reading experience. Explore cached headlines and publisher-provided excerpts, follow original article links, and compare available coverage with source, country, language and publication-time context.

Save stories for later, continue reading where you left off, and search downloaded headlines and summaries. Cached reporting remains available offline; refreshing feeds, loading new images and opening publisher pages need an internet connection. Coverage depends on available feeds and may be incomplete or out of date. Publisher subscriptions may be required on external sites.

Related coverage is grouped automatically and may contain mistakes. Publisher location is context, not a political label. CrossLens does not currently provide verified claim analysis, live translation, or a production Lens Gap score. Demonstration material, when shown, is labeled. CrossLens Plus purchases are unavailable in this version.

Review this wording after real-device ingestion verification and the owner's free/Plus launch decision. Do not publish promises for inactive features.

**Candidate release notes:**

- More reliable cached reading and saved-story retention.
- Publisher country/language filters and search across cached news.
- Clearer original reporting and comparison limitations.
- Android 16 targeting and release/security groundwork.

Only use notes after the candidate passes its checks.

## Listing assets

- [ ] Final launcher/store icon (512×512 store asset), brand ownership and any font/image credits reviewed.
- [ ] Feature graphic 1024×500, PNG/JPEG without alpha, original CrossLens branding; no fake scores, prices, testimonials or unavailable capabilities. See [official preview-asset guidance](https://support.google.com/googleplay/android-developer/answer/9866151).
- [ ] Capture at least two authentic phone screenshots of the verified release; target a fuller set covering Home, story, comparison, Explore, Saved, Settings, dark mode and a wide-screen layout. Use real permitted content or visibly labeled demo fixtures; do not pass historical beta captures off as this release.
- [ ] Review screenshot resolution/aspect ratio against current Console requirements, captions/alt text, copyright, readability and privacy. Tablet/foldable assets should show actual layouts where distributed.

## Internal testing

After every build/signing/security gate passes, owner manually uploads signed AAB to internal testing, adds approved testers and shares opt-in link. Install through Play, verify certificate/update compatibility and Play-generated split behavior, run the device matrix and inspect pre-launch reports. Do not use internal app sharing as proof of production signing or policy acceptance.

## Closed testing and staged production

If the owner has a personal account created after 13 November 2023, [Google's current requirement](https://support.google.com/googleplay/android-developer/answer/14151465) includes at least 12 opted-in testers continuously for 14 days before applying for production access; confirm actual account eligibility in Console. Keep real testing observations and fix defects; do not substitute enrollment for evidence.

After closed/internal findings are resolved, owner reviews final declarations, rating, rights, privacy and signed artifact hash. Confirm no critical/high open issues and current target/API/SDK policy. Plan a staged rollout (for example 5% → 20% → 50% → 100%) with explicit stability/feedback checkpoints, a halt procedure and previous-release reference. Percentages are a proposal, not configured rollout. No automatic publishing or merging is part of this work.
