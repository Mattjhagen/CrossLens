# Ingestion prototype

This milestone adds an offline, deterministic ingestion prototype. It accepts only source-supplied **HTTPS link-and-excerpt records**. It does not fetch pages, scrape feeds, store full copyrighted article bodies, call translation services, determine outlet ideology, or calculate Lens Gap.

## What it proves

1. A source record can be normalized into a stable, reviewable article candidate.
2. Tracking parameters are removed from URLs, so the same article is not counted twice because a feed added `utm_*`, `fbclid`, or `gclid`.
3. Candidate articles are grouped only when their normalized headlines overlap and their publication times are within a fixed 72-hour window.
4. Each proposal retains every contributing article and source ID. Two or more distinct sources make a cluster **reviewable**, never automatically factual or unbiased.

## What it deliberately does not prove

Headline similarity is a triage aid, not semantic event understanding. It can miss multilingual coverage and can make false matches. Wire-copy detection, source licensing/terms review, article retrieval, entity extraction, human moderation, translation, claims, and qualitative comparison labels remain future work.

The code is in `app/src/main/java/com/crosslens/app/data/ingestion/EventClusteringPipeline.kt`. Its focused tests cover URL canonicalization, duplicate detection, related coverage, and unrelated coverage.

## Implementation status

✅ **Source adapter contracts** (`SourceAdapter.kt`)
- Enforces HTTPS-only URLs and explicit content permissions
- Validates adapter configuration and article records
- Documents three permission models: explicit excerpt, fair use preview, licensed content

✅ **Editorial review model** (`EditorialReview.kt`)
- Editorial decisions: APPROVED, REJECTED, DEFERRED
- Requires reason for rejections and deferrals
- Validates multi-source requirement for approved clusters
- Creates audit trail for all decisions

✅ **Syndication detection** (`SyndicationDetector.kt`)
- Detects suspected wire-copy and syndicated content after deduplication, before clustering
- Uses normalized title exact match and excerpt fingerprint overlap
- Conservative confidence bands: HIGH, MEDIUM, LOW, UNCERTAIN
- Cross-language matches marked UNCERTAIN (may be translation or syndication)
- All articles and source attributions preserved; detection is transparent evidence
- Every syndication group requires editorial review
- Labeled as SUSPECTED syndication, never confirmed without explicit wire attribution

✅ **Ingestion service** (`IngestionService.kt`)
- Orchestrates: adapters → registry validation → deduplication → syndication detection → clustering → editorial review → persistence
- Persists only approved clusters to Room
- Records all decisions (approved/rejected/deferred) for audit
- Handles adapter errors gracefully without failing entire batch
- Maintains decision statistics

✅ **Room persistence**
- `EditorialDecisionEntity` stores audit trail
- `EditorialDecisionDao` provides decision queries
- Database version bumped to 2 with fallback to destructive migration

✅ **Mock source adapters** (`MockSourceAdapter.kt`)
- BBC, Le Monde, Al Jazeera, NYT fixtures
- Multilingual test data (English, French, Arabic)
- Different content permissions for testing
- Demonstrates both clustering and single-source scenarios

✅ **Comprehensive tests**
- Adapter validation (HTTPS, language tags, blank fields)
- Editorial validation (decision requirements, cluster quality)
- Full ingestion pipeline integration
- Same-language clustering verification
- Multilingual limitations documented
- Error handling and statistics

## Source adapter boundary

Source adapters emit **link-and-excerpt records only**. They MUST NOT:
- Scrape or fetch full article bodies
- Make unauthorized requests
- Bypass content permissions
- Store copyrighted content beyond permitted excerpts

Each adapter declares its source ID, validates its configuration, and returns empty lists on transient failures rather than throwing exceptions.

## Editorial review workflow

1. **Ingest**: `IngestionService.ingestFromAdapters(adapters)` returns proposals
2. **Review**: Editorial reviewer examines each `EventClusterProposal`
3. **Decide**: Create `EditorialDecision` with APPROVED/REJECTED/DEFERRED + reason
4. **Persist**: Approved clusters become stories via `persistApprovedCluster()`
5. **Audit**: All decisions recorded via `recordDecision()` for transparency

Only approved multi-source clusters enter the database. Every story has a traceable decision record showing which articles were clustered, who approved it, and why.

## Known limitations

- **Multilingual clustering**: Token-based similarity has limited effectiveness across languages. BBC/Le Monde/Al Jazeera articles about the same Geneva climate summit may not cluster due to vocabulary differences. This is a documented prototype limitation.
- **Multilingual syndication detection**: Cross-language matches are marked UNCERTAIN because the detector cannot distinguish between independent translation and wire-service syndication without semantic understanding.
- **No semantic understanding**: Uses headline token overlap and excerpt fingerprint, not entity recognition or event understanding
- **Short excerpts**: Brief excerpts (<100 characters) may have coincidental overlap; marked LOW confidence
- **No explicit wire attribution parsing**: Cannot confirm wire service unless metadata is present
- **Manual review required**: All syndication groups require editorial review; no automatic independent-source determination

## Source registry and content-use policy

✅ **Source registry model** (`SourceRegistry.kt`)
- Separates policy/eligibility from reader-facing display metadata
- Required fields: source ID, display name, homepage, status, permitted intake method, attribution requirements, review date, reviewer, and review notes
- Five statuses: `DEMO_ONLY`, `PENDING_REVIEW`, `APPROVED_LINK_AND_EXCERPT`, `REJECTED`, `SUSPENDED`
- Four intake methods: `DEMO_FIXTURE`, `RSS_WITH_EXCERPT`, `LICENSED_API`, `FAIR_USE_PREVIEW`
- Attribution requirements: source name format, link required, custom attribution, branding notes
- Optional approval expiry for time-limited permissions or trials
- Eligibility for clustering flag (allows restricting sources from multi-source clusters)

✅ **Policy enforcement** (`SourceRegistryValidator`)
- Checks eligibility before any adapter can emit articles
- Validates intake method matches registry entry
- Validates attribution requirements (HTTPS links, custom attribution)
- Blocks ineligible sources with clear reasons: not in registry, demo-only in production, pending review, rejected, suspended, approval expired, intake method mismatch
- Production mode flag to block demo sources

✅ **Mock registry** (`InMemorySourceRegistry.createMockRegistry()`)
- Example entries covering all statuses:
  - `bbc-demo`, `lemonde-demo`: DEMO_ONLY with explicit "Fictional demo source" labels
  - `pending-source`: PENDING_REVIEW awaiting legal review
  - `rejected-source`: REJECTED due to terms of service prohibition
  - `suspended-source`: SUSPENDED for attribution violations
  - `approved-rss`: APPROVED_LINK_AND_EXCERPT with documented RSS permission
  - `expired-approval`: APPROVED but with expired time-limited trial
- NO entry represents an actual publisher agreement

✅ **Integration** (`IngestionService`)
- Registry validation runs before adapter.fetchArticles()
- Ineligible sources blocked with detailed reasons
- Attribution from registry, not adapter (ensures consistency)
- Adapter errors still caught gracefully after eligibility check

✅ **Comprehensive tests** (`SourceRegistryValidatorTest`, updated `IngestionServiceTest`)
- Eligibility decisions for all statuses
- Intake method mismatch detection
- Expired approval blocking
- Attribution validation (HTTPS requirements)
- Demo source blocking in production mode
- Registry query methods (getAllEntries, getEligibleSources)
- Integration with ingestion pipeline
- Error handling for approved sources that fail during fetch

## Registry review workflow

Before a source becomes `APPROVED_LINK_AND_EXCERPT`, a human reviewer must verify:

1. **Content-use permission**: RSS terms, API license, or fair use basis documented
2. **Attribution requirements**: Exact source name format and link requirements
3. **Intake method**: Which technical method is permitted and matches implementation
4. **Review notes**: Why this source is approved and any restrictions
5. **Clustering eligibility**: Whether articles can be combined with other sources
6. **Expiry date** (if applicable): Time-limited trials or review intervals

**Demo sources** are explicitly labeled as fictional test data. They have `DEMO_ONLY` status and "Demo" in their display name. They never claim to represent real publisher agreements.

**Source inclusion does NOT imply**:
- Editorial endorsement or quality certification
- Political classification (left/center/right)
- Factual accuracy guarantee
- Agreement with the publisher's views
- Representation of an entire country's media landscape

The registry documents content-use eligibility only. Display metadata (regions, languages, editorial context) lives in `SourceEntity` so policy changes don't silently alter the reader experience.

## Syndication detection workflow

After canonical URL deduplication and before event clustering, the pipeline runs syndication detection:

1. **Exact title match**: Articles with identical normalized titles (punctuation removed, lowercase) from different sources are grouped as HIGH confidence suspected syndication (same language) or UNCERTAIN (different languages).
2. **Excerpt fingerprint**: Articles with ≥80% longest-common-substring overlap in excerpts ≥100 characters are grouped as MEDIUM confidence (same language) or UNCERTAIN (different languages).
3. **Transparent evidence**: Each group records detection method, confidence band, article IDs, source IDs, and rationale for editorial review.
4. **Preserved attribution**: All articles remain in the pipeline with their source attributions intact; syndication detection is evidence, not a filter.

**Editorial review decisions**:
- **Confirmed syndication**: Reviewers can confirm wire-copy or AP/Reuters syndication and adjust independent-source count for the cluster.
- **Independent reporting**: Reviewers can override false positives where articles are independent despite text similarity (e.g., press release quotes).
- **Uncertain translation**: Cross-language matches require reviewer judgment about whether it's syndication or independent translation.

**Important**: Syndication detection labels matches as SUSPECTED until editorial review. It does NOT automatically reduce a cluster's independent-source count or remove articles from clustering. Event clusters count all contributing sources; approved clusters may have a separate editorial-verified independent-source count if syndication is confirmed.

## Next steps

1. ~~Build a source registry with licensing/attribution requirements~~ ✅ Complete
2. ~~Add wire-copy detection to avoid counting syndicated reports as independent sources~~ ✅ Complete (detection only; editorial review integration pending)
3. Integrate entity extraction to improve cross-language clustering and syndication detection
4. Build an editorial review UI (currently tested via service layer only)
5. Add explicit wire attribution parsing from article metadata (AP, Reuters, AFP tags)
6. Add claims extraction and frame observation generation for approved stories
7. Build source registry persistence (currently in-memory mock)
8. Add registry admin UI for managing source approvals and suspensions
9. Add editorial UI for reviewing and confirming/rejecting syndication groups
