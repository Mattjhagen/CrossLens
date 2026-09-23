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
- Detects suspected wire-copy and syndicated content after deduplication, before entity extraction
- Uses normalized title exact match and excerpt fingerprint overlap
- Conservative confidence bands: HIGH, MEDIUM, LOW, UNCERTAIN
- Cross-language matches marked UNCERTAIN (may be translation or syndication)
- All articles and source attributions preserved; detection is transparent evidence
- Every syndication group requires editorial review
- Labeled as SUSPECTED syndication, never confirmed without explicit wire attribution

✅ **Entity extraction** (`EntityExtraction.kt`)
- Mock-only prototype using explicit entity metadata from test fixtures
- NO NLP models, external APIs, or automated entity recognition
- Extracts Person, Organization, Location, Date, and EventIdentifier entities
- Each entity has stable ID for cross-language matching (e.g., "person:biden", "location:geneva")
- Entity display names localized per source language
- Extraction method versioning for auditability (`mock-metadata-v1`)

✅ **Cross-language event matching** (`CrossLanguageEventMatcher.kt`)
- Finds CANDIDATE event matches across languages using entity overlap
- Minimum 2 shared entities + 30% overlap score required
- Confidence bands: HIGH (event identifier + 3+ entities), MEDIUM (2+ diverse entity types), LOW (minimal overlap), UNCERTAIN (insufficient evidence)
- Same-language articles use existing title similarity clustering (not entity matching)
- All matches labeled as CANDIDATES requiring editorial review
- Never claims confirmed same-event without review
- Transparent uncertainty reasons for each match

✅ **Ingestion service** (`IngestionService.kt`)
- Orchestrates: adapters → registry validation → deduplication → syndication detection → entity extraction → cross-language matching → clustering → editorial review → persistence
- Persists only approved clusters to Room
- Records all decisions (approved/rejected/deferred) for audit
- Handles adapter errors gracefully without failing entire batch
- Maintains decision statistics
- Passes mock entity map through pipeline for testing

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

- **Mock-only entity extraction**: Entities come from explicit test metadata, not NLP. Production requires licensed entity extraction API or self-hosted model.
- **No automated entity recognition**: Cannot extract entities from raw article text without NLP integration.
- **Entity ID stability**: Mock entity IDs (e.g., "person:biden") must be maintained consistently across languages and sources. Production needs entity resolution system.
- **Generic entity handling**: Common entities (e.g., "location:washington", "location:beijing") may create false positive matches across unrelated events. Detector flags but cannot fully prevent.
- **Multilingual entity matching**: Uses exact entity ID matches only. Cannot handle alternate transliterations, name variations, or entity disambiguation.
- **Candidate matches require review**: All cross-language matches are CANDIDATES. Cannot automatically determine same-event vs. different-event.
- **No semantic understanding**: Uses entity overlap count, not contextual understanding of entity roles or event relationships.
- **Syndication + entity overlap**: Articles can be BOTH syndicated AND entity-matched. Editorial review must consider both signals for independent-source counting.
- **Short entity lists**: Articles with <2 entities cannot be cross-language matched, even if same event.
- **Multilingual syndication detection**: Cross-language matches are marked UNCERTAIN because the detector cannot distinguish between independent translation and wire-service syndication without semantic understanding.
- **Short excerpts**: Brief excerpts (<100 characters) may have coincidental overlap; marked LOW confidence
- **No explicit wire attribution parsing**: Cannot confirm wire service unless metadata is present
- **Manual review required**: All syndication groups AND entity matches require editorial review; no automatic independent-source determination

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

## Entity extraction and cross-language matching workflow

After syndication detection and before event clustering, the pipeline runs entity extraction and cross-language matching:

1. **Entity extraction**: For each article, extract entities from explicit mock metadata:
   - Person entities (e.g., "person:biden" → "Joe Biden" / "Joe Biden" / "جو بايدن")
   - Organization entities (e.g., "org:un" → "United Nations" / "ONU" / "الأمم المتحدة")
   - Location entities (e.g., "location:geneva" → "Geneva" / "Genève" / "جنيف")
   - Date entities (e.g., "date:2026-09-22")
   - EventIdentifier entities (e.g., "event:geneva-climate-summit-2026")

2. **Cross-language matching**: Find articles with shared entity IDs across different languages:
   - Minimum 2 shared entities required
   - Minimum 30% overlap score (shared / min(set sizes))
   - Within 72-hour time window
   - Only cross-language pairs (same-language uses title similarity)

3. **Confidence bands**:
   - HIGH: Event identifier + 3+ diverse entities (likely same event)
   - MEDIUM: 2+ diverse entity types (person + org + location)
   - LOW: Minimal overlap or generic entities only
   - UNCERTAIN: Insufficient evidence

4. **Candidate match result**: Each match includes:
   - Article IDs and source IDs
   - Shared entity IDs list
   - Language pair/triple
   - Confidence band
   - Matching method version
   - Editorial review flag (always true)
   - Rationale explaining the match
   - Uncertainty reasons (e.g., "Cross-language match requires verification", "Generic entities may appear in unrelated events")

**Editorial review decisions**:
- **Confirmed same event**: Reviewers can confirm cross-language coverage of the same event and count independent sources.
- **Different events**: Reviewers can override false positives where shared entities coincidentally appear (e.g., both mention "Washington" and "China" but different topics).
- **Syndication vs. same event**: Articles can be BOTH syndicated AND entity-matched. Syndicated French translation of English wire = 1 independent source, not 2.

**Important**: Entity matching produces CANDIDATE matches requiring editorial review. It does NOT automatically determine same-event, independent reporting, factuality, political orientation, or Lens Gap.

## Syndication detection workflow

After canonical URL deduplication and before entity extraction, the pipeline runs syndication detection:

1. **Exact title match**: Articles with identical normalized titles (punctuation removed, lowercase) from different sources are grouped as HIGH confidence suspected syndication (same language) or UNCERTAIN (different languages).
2. **Excerpt fingerprint**: Articles with ≥80% longest-common-substring overlap in excerpts ≥100 characters are grouped as MEDIUM confidence (same language) or UNCERTAIN (different languages).
3. **Transparent evidence**: Each group records detection method, confidence band, article IDs, source IDs, and rationale for editorial review.
4. **Preserved attribution**: All articles remain in the pipeline with their source attributions intact; syndication detection is evidence, not a filter.

**Editorial review decisions**:
- **Confirmed syndication**: Reviewers can confirm wire-copy or AP/Reuters syndication and adjust independent-source count for the cluster.
- **Independent reporting**: Reviewers can override false positives where articles are independent despite text similarity (e.g., press release quotes).
- **Uncertain translation**: Cross-language matches require reviewer judgment about whether it's syndication or independent translation.

**Important**: Syndication detection labels matches as SUSPECTED until editorial review. It does NOT automatically reduce a cluster's independent-source count or remove articles from clustering. Event clusters count all contributing sources; approved clusters may have a separate editorial-verified independent-source count if syndication is confirmed.

## Editorial Review UI

✅ **Editorial review screen** (`EditorialReviewScreen.kt`)
- Accessible from Settings with clear entry point
- Demo workflow notice explaining fictional prototype nature
- Three pending candidates (cross-language, syndication, cluster)
- Pending candidates list with expandable details
- Review history with audit trail (approved/rejected/deferred)
- Statistics dashboard showing review counts
- Full evidence display: sources, languages, shared entities, uncertainty reasons
- Decision workflow: approve/reject/defer with optional editorial notes
- **Reset control:** "Reset demo reviews" button with confirmation dialog
  - Deletes all local demo review decisions and notes
  - Restores original three candidates for re-testing
  - Does not affect reader-facing stories, settings, or subscriptions
  - Persists correctly: candidates remain pending after app restart until reviewed
- All user-facing strings externalized to string resources
- Comprehensive ViewModel tests covering state transitions, evidence preservation, and reset functionality

✅ **Offline editorial workflow**
- Mock candidates generated from demo data (cross-language matches, syndication detection)
- All reviews persisted to Room database for audit trail
- Review decisions affect which candidates appear as pending
- Stats updated in real-time as reviews are submitted
- Complete separation from live ingestion (no network calls)

**Important boundaries:**
- This is a **fictional offline prototype** demonstrating the editorial review workflow
- All candidates are mock data from test fixtures, not real ingestion results
- Reviews are stored locally and do not affect production systems
- No automated approval or live data ingestion
- Production implementation requires:
  - Live ingestion pipeline connected to source adapters
  - Real-time candidate generation from actual article ingestion
  - Authenticated reviewer identity and permissions system
  - Review queue management and assignment
  - Integration with story publication workflow
  - Audit logging with timestamps and reviewer attribution

## Next steps

1. ~~Build a source registry with licensing/attribution requirements~~ ✅ Complete
2. ~~Add wire-copy detection to avoid counting syndicated reports as independent sources~~ ✅ Complete
3. ~~Integrate entity extraction to improve cross-language clustering~~ ✅ Complete (mock-only prototype; production NLP integration pending)
4. ~~Build editorial review UI for reviewing candidates~~ ✅ Complete (offline demo workflow; production integration pending)
5. Integrate licensed or self-hosted NLP for automated entity extraction from article text
6. Add entity resolution system for handling name variations and transliterations
7. Connect editorial review UI to live ingestion pipeline:
   - Real-time candidate generation from source adapters
   - Review queue with assignment and priority
   - Authenticated reviewer roles and permissions
   - Approved cluster publication to story database
8. Add explicit wire attribution parsing from article metadata (AP, Reuters, AFP tags)
9. Add claims extraction and frame observation generation for approved stories
10. Build source registry persistence (currently in-memory mock)
11. Add registry admin UI for managing source approvals and suspensions
12. Add entity/syndication interaction tests showing combined decision workflows
