# CrossLens privacy policy — owner review draft

**Not a published policy.** Before release, replace the bracketed owner details, confirm every statement against a built release and third-party traffic, and host an accessible public HTTPS page. Effective date: [owner to set]. Controller/developer: [legal name, postal/contact details]. Privacy contact: [email and public contact-page URL].

CrossLens helps you read and compare reporting from different publishers. This draft describes the default configuration of the production-readiness candidate. It does not cover future accounts, paid subscriptions, live translation services or optional crash reporting without an updated disclosure.

## Information stored on your device

CrossLens stores cached news metadata, publisher-provided headlines and short excerpts, source-health information, saved-story identifiers, your last-opened story, reading language, display preferences and any explicit personalization choices in app-private Room/DataStore storage. A manually selected demo city is a reading preference; the application does not request GPS or precise location. Local demo content and translations are labeled.

The default application does not create a CrossLens account or request your name, email, contacts, advertising identifier or payment-card information. Local state is not treated as proof of a purchase. Android OS and app-store services may independently process information under their own policies.

## Internet connections and publishers

Refreshing news contacts configured RSS/Atom publishers. Loading an article image contacts its publisher or image host. These hosts receive network information such as IP address and request metadata, including the requested resource and app/browser information. CrossLens does not control their logging, retention or privacy practices. Original article links open a publisher page in a browser or Custom Tab, where the publisher may use cookies, subscriptions or analytics under its own policy. Public feeds and external pages are not operated by CrossLens.

Background cache refresh may occur periodically when Android permits it, connectivity is available and battery conditions allow. Cached content remains available without a connection. Publisher links require internet connectivity.

## Crash reporting and service integrations

Crashlytics code is included, but Firebase is unconfigured by default and automatic crash collection is disabled in the manifest and application code. This does not justify a blanket “zero data collection” claim: publisher/image requests still transmit network information. If the owner configures Firebase or enables crash reporting, this policy and Data Safety answers must be revised after verifying actual SDK behavior. Crash diagnostics can include stack traces, device/app state and installation identifiers; see the configured service's documentation before activation.

The translation provider is unconfigured for real journalism. Originals remain accessible. Fixture translations are demonstrations. The Play Billing integration is inactive and no real purchases are available. Configuring either service requires additional disclosures about submitted article text, purchase tokens, entitlement verification, retention and service providers.

## Purpose, sharing and retention

Local information supports reading continuity, saved stories, cached/offline access and explicit display/discovery preferences. This candidate has no application backend receiving reader preferences. We do not describe independent publisher requests as confidential or promise a publisher's retention period. [Owner must confirm whether any deployed services, support tools or legal retention obligations change these statements.]

Unsaved live feed entries may be replaced during successful refresh. Saved and last-opened stories are retained locally. Translation cache entries have a seven-day freshness policy when a provider is configured; fixture translations are separate. Source-health metadata is retained locally until reset or app-data deletion. No claim is made that every expired record is immediately physically erased from SQLite or image caches.

## Your choices and deletion

Use Settings to change reading/display preferences and remove personalization choices. “Delete local app data” asks for confirmation, removes app-private data and closes the app. Android Settings → Apps → CrossLens → Storage → Clear storage provides another local reset. Uninstalling removes local app-private data under normal Android behavior. CrossLens disables app backup and excludes local storage from its declared cloud/device-transfer rules; device-vendor behavior should be checked before making guarantees.

There is no CrossLens cloud account in this version. Local deletion does not delete independent publisher/browser history or Google Play purchase records. If you contact the developer, [owner must define support-data retention and deletion process].

## Children, international processing and changes

[Owner must define target audience, distribution regions, applicable age requirements and support/data-rights process.] Publisher/image/service requests may be processed in other countries. This candidate contains general news and is not yet assigned a content rating. Changes to enabled services require an updated policy and in-app disclosures as applicable.

Questions or privacy requests: [owner's public HTTPS contact page and email].
