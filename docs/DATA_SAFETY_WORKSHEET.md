# Google Play Data Safety worksheet

2026-11-01. **Draft inventory, not submitted answers.** Final declarations require a built release, resolved SDK dependency graph, network capture and owner review. Do not copy historical “no network” or “zero collection” claims.

| Implemented path | Data and destination | Local / transmitted | Purpose and controls | Declaration review |
|---|---|---|---|---|
| Room cache | Headlines, excerpts, URLs, publisher metadata, source health, dates, event grouping | Local app-private | Reading/offline cache; reset/delete local app data | Device-only storage is distinct from off-device collection |
| Reading preferences | Saved IDs, last story, language/theme, reduced motion, manually selected demo city, explicit topic feedback | Local DataStore | User-requested reading continuity/personalization; clear controls | No GPS permission. Do not call a manual city precise GPS data |
| RSS/Atom fetch | IP address, requested feed, HTTPS request metadata, app User-Agent → publishers | Transmitted, foreground and constrained periodic work | Fetch news metadata; cache used offline | Review whether IP implies approximate location/device info and whether direct third-party requests count as collection/sharing for the final configuration; determine processing/retention with providers |
| Coil images | IP address, image URL/request metadata → publisher/CDN/image host | Transmitted when images load | Display article images; local image cache | Inventory every approved image host and their practices; no zero-collection claim |
| Publisher browser / Custom Tab | Browser/network metadata and any publisher-site cookies/data | External website/browser | User-requested original reporting | Review Data Safety treatment of external pages; link relevant publisher policies, do not guarantee their behavior |
| Firebase Crashlytics | Potential crash logs, stack traces, app/device info and installation identifiers | Default build unconfigured; collection false | Optional diagnostics only after owner review | If configured, inspect SDK behavior even before enabling reporting. Reassess diagnostics/device-ID declarations, retention and consent; do not answer “not collected” just because UI lacks an account |
| Translation | No production provider; labeled local fixture translations | Local now | Future service may receive original article text/target language | Review backend/provider processing, retention, generation provenance and region before activation |
| Billing | SDK packaged; no gateway instance bound to app; purchases disabled | No initiated purchase/verification requests now | Future purchase tokens/product IDs → trusted backend/Play | Review financial/purchase-history/account identifier categories when enabled; no payment-card handling planned in app |
| WorkManager | Scheduling metadata local; invokes existing feed connections | Local scheduling + above feed traffic | Six-hour opportunistic cache refresh, network/battery constraints | No push/advertising/notification permission requested by app source |

## Owner decisions and checks

- Identify legal developer/controller, public privacy URL, public contact/entity URL, audience and countries. Configure Settings links using `-Pcrosslens.privacyPolicyUrl=... -Pcrosslens.contactUrl=...`.
- Review [Google's Data Safety guidance](https://support.google.com/googleplay/android-developer/answer/10787469) against actual processing, including SDKs and publishers. Decide categories, required/optional, sharing exemptions, purposes, encryption in transit and deletion statements using evidence.
- HTTPS is enforced by app/manifest source; do not attest all SDK traffic is encrypted until captured and verified.
- No reader account creation exists; re-evaluate account-deletion requirements if accounts are introduced. Confirm local deletion on device; independent browser/Play records remain separate.
- No resolved dependency security/SDK Index assessment or network capture ran here. Final form answers, retention periods and vendor contracts are **blocked owner work**, not completed compliance.
