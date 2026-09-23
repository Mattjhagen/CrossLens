# Ingestion prototype

This milestone adds an offline, deterministic ingestion prototype. It accepts only source-supplied **HTTPS link-and-excerpt records**. It does not fetch pages, scrape feeds, store full copyrighted article bodies, call translation services, determine outlet ideology, or calculate Lens Gap.

## What it proves

1. A source record can be normalized into a stable, reviewable article candidate.
2. Tracking parameters are removed from URLs, so the same article is not counted twice because a feed added `utm_*`, `fbclid`, or `gclid`.
3. Candidate articles are grouped only when their normalized headlines overlap and their publication times are within a fixed 72-hour window.
4. Each proposal retains every contributing article and source ID. Two or more distinct sources make a cluster **reviewable**, never automatically factual or unbiased.

## What it deliberately does not prove

Headline similarity is a triage aid, not semantic event understanding. It can miss multilingual coverage and can make false matches. Wire-copy detection, source licensing/terms review, article retrieval, entity extraction, human moderation, translation, claims, and qualitative comparison labels remain future work.

The code is in `app/src/main/java/com/crosslens/app/data/ingestion/EventClusteringPipeline.kt`. Its focused tests cover URL canonicalization, duplicate detection, related coverage, and unrelated coverage. The next implementation should feed approved source adapters into this pipeline, show editorial reviewers the proposals, and only then persist approved clusters into Room.
