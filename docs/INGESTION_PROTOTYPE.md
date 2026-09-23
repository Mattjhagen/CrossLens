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

✅ **Ingestion service** (`IngestionService.kt`)
- Orchestrates: adapters → clustering → editorial review → persistence
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
- **No semantic understanding**: Uses headline token overlap, not entity recognition or event understanding
- **No wire-copy detection**: Syndicated articles from the same source are not identified
- **No source catalog integration**: Adapters reference source IDs but don't validate against a source registry
- **Manual review only**: No automated quality scoring or suggested approval

## Next steps

1. Build a source registry with licensing/attribution requirements
2. Add wire-copy detection to avoid counting syndicated reports as independent sources
3. Integrate entity extraction to improve cross-language clustering
4. Build an editorial review UI (currently tested via service layer only)
5. Add claims extraction and frame observation generation for approved stories
