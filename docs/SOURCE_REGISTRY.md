# Source Registry and Content-Use Policy

The source registry controls which news sources are eligible for ingestion and under what terms. It enforces content-use permissions, attribution requirements, and intake methods before any article records enter the pipeline.

## Purpose

The registry separates **content-use eligibility** (Can we ingest this source? Under what terms?) from **display metadata** (How do we show this source to readers?). This separation ensures:

1. Policy changes don't silently alter the reader experience
2. Every source has a documented permission basis
3. Ineligible sources are blocked before processing
4. Attribution requirements are consistently enforced
5. Reviewers can inspect why each source was approved or rejected

## Architecture

```
Source Adapter → Registry Validation → Eligibility Check → Clustering Pipeline
                       ↓
                  SourceRegistryValidator
                       ↓
                  SourceRegistry (in-memory)
```

**Key components:**
- `SourceRegistryEntry`: Complete record of a source's eligibility and terms
- `SourceRegistry`: Interface for querying entries (in-memory for prototype)
- `SourceRegistryValidator`: Enforces eligibility and attribution rules
- `IngestionService`: Integrates registry validation into the pipeline

## Source Statuses

### DEMO_ONLY
Fictional demo source for testing. Never claims real publisher permission.
- **Display name**: Must contain "Demo" (e.g., "BBC News Demo")
- **Review notes**: Must state "Fictional demo source"
- **Blocked in production**: Yes (when `allowDemoSources = false`)
- **Use case**: Prototype testing, UI demos, integration tests

### PENDING_REVIEW
Source identified but not yet reviewed for content-use eligibility.
- **Blocked**: Yes
- **Reason**: Human review required before ingestion
- **Next step**: Legal/editorial review of terms and permissions

### APPROVED_LINK_AND_EXCERPT
Approved for link-and-excerpt ingestion under documented terms.
- **Blocked**: No
- **Requirements**: Documented permission basis, attribution rules, intake method
- **Clustering**: Configurable via `eligibleForClustering` flag

### REJECTED
Reviewed and rejected; not eligible for ingestion.
- **Blocked**: Yes
- **Reason**: No permission, terms violation, unreliable, or other documented reason
- **Review notes**: Must explain why rejected
- **Permanent**: Requires re-review to change status

### SUSPENDED
Previously approved but suspended due to terms violation or permission revocation.
- **Blocked**: Yes
- **Reason**: Documented in review notes (e.g., "Attribution requirements violated")
- **Temporary**: Can be restored to APPROVED after issue resolution

## Intake Methods

The **permitted intake method** must match the adapter's actual implementation:

### DEMO_FIXTURE
Fictional mock data only. No real content source.
- Used by: `MockSourceAdapter`
- Permission basis: N/A (fictional test data)

### RSS_WITH_EXCERPT
RSS/Atom feed with explicit excerpt permission.
- Permission basis: RSS feed terms or publisher agreement
- Content: Link + headline + excerpt from feed
- Attribution: Source name + link to original

### LICENSED_API
Licensed API with link-and-excerpt rights.
- Permission basis: API license agreement
- Content: Link + metadata as permitted by license
- Attribution: Per license terms (may require custom attribution)

### FAIR_USE_PREVIEW
Fair use preview within legal limits.
- Permission basis: Fair use doctrine
- Content: Link + headline + brief excerpt
- Attribution: Source name + link to original
- Restrictions: Short excerpts only, no full text

**Intake method mismatch**: If the adapter uses a different method than the registry permits, the source is blocked with `IneligibilityReason.INTAKE_METHOD_MISMATCH`.

## Attribution Requirements

Each registry entry specifies exact attribution requirements:

```kotlin
AttributionRequirements(
    sourceName = "BBC News Demo",            // Required display name
    linkRequired = true,                     // Must link to original?
    customAttribution = "via RSS feed",      // Optional custom text
    brandingNotes = "Use BBC logo"           // Optional branding rules
)
```

The validator checks:
- HTTPS links when `linkRequired = true`
- Custom attribution present if specified
- Correct source name format

**Attribution source**: The ingestion service uses the registry's `attributionRequirements.sourceName`, NOT the adapter's `sourceName`, ensuring consistency even if the adapter changes.

## Eligibility Checks

`SourceRegistryValidator.checkEligibility(sourceId, intakeMethod)` returns:

### Eligible
```kotlin
SourceEligibilityResult.Eligible(entry: SourceRegistryEntry)
```
Source can emit articles. Proceed to fetch and validate.

### Ineligible
```kotlin
SourceEligibilityResult.Ineligible(
    reason: IneligibilityReason,
    details: String
)
```

**Ineligibility reasons:**
- `NOT_IN_REGISTRY`: Source not found; add to registry before ingesting
- `DEMO_ONLY_IN_PRODUCTION`: Demo source blocked in production mode
- `PENDING_REVIEW`: Human review required
- `REJECTED`: Permanently rejected with documented reason
- `SUSPENDED`: Temporarily suspended with documented reason
- `APPROVAL_EXPIRED`: Time-limited approval expired; requires re-review
- `INTAKE_METHOD_MISMATCH`: Adapter uses different method than registry permits

## Approval Expiry

Some sources have time-limited approvals:

```kotlin
approvalExpiresAt = Instant.now().plus(90, ChronoUnit.DAYS)
```

**Use cases:**
- Trial API access with fixed duration
- Periodic re-review requirements
- Experimental integrations

When the current time exceeds `approvalExpiresAt`, the source is blocked with `IneligibilityReason.APPROVAL_EXPIRED`, even if status is still `APPROVED_LINK_AND_EXCERPT`.

## Clustering Eligibility

The `eligibleForClustering` flag controls whether articles from this source can be combined with other sources:

- `true`: Articles can be clustered with other sources (default)
- `false`: Articles appear only in single-source clusters

**When to set false:**
- Source has content-use restrictions against aggregation
- Source reliability is uncertain (pending verification)
- Editorial policy requires isolated presentation

## Review Workflow

Before a source becomes `APPROVED_LINK_AND_EXCERPT`, a reviewer must:

1. **Verify permission basis**
   - Review RSS terms, API license, or fair use justification
   - Document permission source in review notes
   - Confirm no terms-of-service violations

2. **Define attribution requirements**
   - Exact source name format
   - Link requirements (required or optional)
   - Custom attribution text if needed
   - Branding or logo requirements

3. **Specify intake method**
   - Which technical method is permitted
   - Must match adapter implementation
   - Document any restrictions

4. **Set clustering eligibility**
   - Can articles be combined with other sources?
   - Are there aggregation restrictions?

5. **Record review metadata**
   - Review date (`lastReviewedAt`)
   - Reviewer identifier (`reviewedBy`)
   - Review notes explaining decision

6. **Set expiry if needed**
   - Time-limited trials
   - Periodic re-review schedules

## Production vs. Prototype Mode

The registry validator has an `allowDemoSources` flag:

**Prototype mode** (`allowDemoSources = true`):
- Demo sources are eligible
- Used for testing and development
- Default for CrossLens prototype

**Production mode** (`allowDemoSources = false`):
- Demo sources blocked with `DEMO_ONLY_IN_PRODUCTION`
- Only approved real sources eligible
- Used when deploying to real users

## Mock Registry

`InMemorySourceRegistry.createMockRegistry()` provides example entries:

| Source ID | Status | Notes |
|-----------|--------|-------|
| `bbc-demo` | DEMO_ONLY | Fictional BBC test data |
| `lemonde-demo` | DEMO_ONLY | Fictional Le Monde test data |
| `pending-source` | PENDING_REVIEW | Awaiting legal review of RSS terms |
| `rejected-source` | REJECTED | Terms of service prohibit aggregation |
| `suspended-source` | SUSPENDED | Attribution requirements violated |
| `approved-rss` | APPROVED | RSS feed permits link-and-excerpt |
| `expired-approval` | APPROVED (expired) | Trial API access expired 30 days ago |

**NO entry represents an actual publisher agreement.** All entries are for testing the eligibility system only.

## Important Disclaimers

### Source inclusion does NOT imply:

❌ **Editorial endorsement** – We do not certify source quality or accuracy  
❌ **Political classification** – We do not label sources as left/center/right  
❌ **Factual guarantee** – We do not verify claims; we show coverage  
❌ **Publisher agreement** – We do not agree with views expressed  
❌ **Country representation** – One source does not represent a nation's media  

### What the registry DOES represent:

✅ **Content-use permission** – This source permits link-and-excerpt use  
✅ **Attribution compliance** – We display required attribution  
✅ **Intake method** – We use the permitted technical method  
✅ **Review status** – A human reviewed eligibility on this date  

The registry is a **content-use policy layer**, not an editorial judgment. Approved sources have documented permission to be ingested; that's all.

## Future Work

1. **Persistent storage**: Move from in-memory to Room/database
2. **Admin UI**: Build interface for managing approvals and suspensions
3. **Audit trail**: Track all status changes and reviewer actions
4. **Automated checks**: Flag expiring approvals or missing reviews
5. **Batch operations**: Approve/suspend multiple sources at once
6. **Export/import**: Share registry configuration across environments
7. **Source catalog integration**: Link registry to display source metadata
