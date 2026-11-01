# Security and privacy audit

2026-11-01. Source review plus limited static checks; **not a penetration test or production certification**. All Android runtime checks are blocked by missing JDK/SDK/device. See [actual results](verification/2026-11-01/RESULTS.md).

| Severity | Area / files | Disposition and evidence still needed |
|---|---|---|
| Critical | Startup data loss: `data/mock/MockStoryRepository.kt` | Replaced delete-all seeding with transactional missing-fixture insertion. Real-Room regression added; not run. |
| High | Cache replacement: `data/repository/LiveStoryRepository.kt` | Mutex, cancellation propagation, transaction, stable IDs, saved/last-read retention. SQLite transaction behavior is tested in added Robolectric cases but not executed here. |
| High | Database upgrades: `di/DatabaseModule.kt`, `data/local/ProductionMigrations.kt` | Destructive fallback removed. Reconstructed migration paths from evidenced schemas 1–6 and 8–10 to 11. Nine SQLite structure/preservation checks passed; Room and actual-beta upgrade tests remain blocked. Schema 7 must be recovered or confirmed never distributed. |
| High | Feed resource exhaustion and cancellation: `data/ingestion/RssSourceAdapter.kt` | Reads at most 5 MiB decompressed, closes responses, total call deadline, cancellable callbacks, one bounded transient retry. HTTPS redirects cannot downgrade. JVM tests added, not executed. |
| High | XML/attribution: `RssParser.kt`, `RssSourceAdapter.kt` | Reject DTD/entity declarations; preserve source publication dates, skip undated records, RSS/Atom metadata. Feed availability is NOT a content license; review each source's terms before commercial distribution. |
| High | Paid entitlement: `data/preferences/DataStoreEntitlementRepository.kt` | Release always Free; stored preview cannot unlock production. New Billing gateway is NOT wired into UI/DI. No backend verifier or products configured; real billing remains blocked. |
| High | Crash reporting: `CrossLensApplication.kt`, `app/build.gradle.kts`, manifest | Optional Firebase build flag, initialized-app guard, collection defaults false including manifest auto-collection. No analytics SDK explicitly requested. Configured Firebase traffic must be inspected before changing privacy claims or enabling collection. |
| High | False journalism analysis: `SourceDigestGenerator.kt`, `data/local/Mappers.kt` | Real reporting rejected by demo digest; only demo scores render. Live comparison says claim/framing evidence unavailable. No claim-verification or Lens Gap algorithm represented as production. |
| Medium | Browser navigation: `core/navigation/PublisherLinks.kt` | Shared HTTPS-only external/custom-tab helper; rejects user-info/custom schemes; handles absent browser/security errors with a visible message. No WebView or arbitrary JavaScript bridge. Device-test restricted handlers. |
| Medium | Exported components and permissions: manifest | MainActivity is the intentional launcher entry point. No app-defined exported data services/receivers. Explicit INTERNET and ACCESS_NETWORK_STATE only; WorkManager/Billing/Firebase add SDK permissions/components in merged manifests. Review final release manifest before submission. No location, contacts, camera, storage, ad-ID or notification permission requested by app source. |
| Medium | Backup/deletion: manifest, `res/xml/data_extraction_rules.xml`, Settings | Backup false; cloud/device transfer exclusions for local storage. Confirmed clearApplicationUserData action removes local app data; no deletion was performed in this session. No accounts or remote reader-data deletion API exists. OEM backup and deletion behavior need device checks. |
| Medium | Sensitive logging/R8: `LiveStoryRepository.kt`, `ClusteringAuditor.kt` | Reflection-heavy clustering auditor now debug-only; release cannot rely on private method names surviving R8. Source-health diagnostics debug by default. No purchase-token logging added; owner must keep verifier logs redacted. Review actual release Logcat and Crashlytics keys. |
| Medium | Preferences: DataStore repositories | Unknown stored theme/translation/access enums now fall back safely. Preferences/cache remain app-private, unencrypted at application layer. Do not store tokens, payment information or provider secrets there. |
| Medium | Dependency security | Pinned catalog and fixed literal versions; no dynamic versions. Existing Compose/Room/Hilt/Kotlin/Firebase dependencies are older; no resolved dependency graph, SDK Index review or automated vulnerability scan was available. Do not interpret this as “no known CVEs.” |
| Medium | News policy/public pages | Settings supports owner-configured HTTPS privacy/contact URLs; both unavailable by default. Published policy/contact pages, news declaration and content rights remain release blockers. |

## Checks actually performed

- Reviewed manifest, networking, persistence, exported launcher, browser routes, Firebase initialization, entitlement writes, signing build logic, backup policies and R8 reflection path.
- Current tracked text scan for private-key headers, AWS access-key IDs and GitHub-token patterns: no matches. This is a limited working-tree pattern scan, not a complete history/entropy audit.
- No signing material or google-services.json is tracked. `keystore.properties.example` contains placeholders. Local credentials were not printed, generated, replaced or uploaded.
- Wrapper distribution SHA-256 pinned using [Gradle's official checksum list](https://gradle.org/release-checksums/).
- XML/TOML/YAML parsing and diff whitespace checks passed. No APK/DEX vulnerability or exported-component runtime inspection was possible.

## Required follow-up

Run tests/lint on the pinned toolchain, inspect dependency advisories and Play SDK Index, validate merged release manifest, inspect real network traffic in default and configured-Firebase builds, exercise missing browser/captive portal/timeout/oversized XML, test actual upgrade data preservation and deletion. Review public-feed usage rights and owner privacy/contact identity. Billing must stay disconnected until authenticated server verification, RTDN reconciliation, durable acknowledgement and Play license testing exist.

Final wrapper check: inherited wrapper JAR hash was not on Gradle's published list; replaced with the official v8.11.1 JAR obtained from gradle/gradle, and verified `2db75c40782f5e8ba1fc278a5574bab070adccb2d21ca5a6e5ed840888448046` against the published wrapper checksum. The Java-less environment never executed the inherited JAR. Added Gson anonymous TypeToken preservation for Room converter reflection under R8 full mode using [official Gson guidance](https://google.github.io/gson/Troubleshooting.html); optimized runtime check remains mandatory.
