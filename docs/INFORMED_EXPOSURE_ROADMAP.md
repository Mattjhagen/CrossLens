# Informed Exposure: Implementation Roadmap

**Principle**: CrossLens optimizes for informed exposure, not agreement or time spent.

This document tracks how current features align with product principles and what work is required to fully realize the informed-exposure vision.

---

## Current State: Alignment Assessment

### ✅ Strong Foundation (Already Built)

These aspects of the current implementation strongly align with informed-exposure principles:

1. **No Engagement Optimization**
   - Clustering based on objective similarity (entities, headlines, time)
   - No ranking by predicted clicks or controversy
   - No amplification of outrage or polarizing content
   - **Status**: Principle fully implemented ✅

2. **No Personalization Bubbles**
   - Default feed shows all sources equally
   - No filtering based on reading history
   - No algorithmic predictions of user preferences
   - **Status**: Principle fully implemented ✅

3. **Clear Publisher Attribution**
   - Every article shows publisher name, country, language
   - SourceMetadata includes ownership and editorial description
   - Provenance documented for all sources
   - **Status**: Principle fully implemented ✅

4. **No Ideological Labels**
   - Sources described by factual attributes (country, language, ownership)
   - No "left/right" or "bias" scoring
   - No claims about reliability or truthfulness
   - **Status**: Principle fully implemented ✅

5. **Safety-Compliant Language**
   - Never claims articles are true/false/biased
   - Preserves original headlines and content
   - Clear disclaimers about comparison purpose
   - **Status**: Principle fully implemented ✅

6. **Conservative Clustering**
   - High thresholds for grouping articles (prevents false matches)
   - Requires 2+ distinct publishers
   - 72-hour time window maximum
   - **Status**: Principle fully implemented ✅

---

## ⚠️ Partial Implementation (Needs Enhancement)

These features have the right foundation but need work to fully align:

### 1. Event Cards Show Source Count, Not Coverage Structure

**Current**:
- Event cards display "4 sources" badge
- Shows total publisher count only
- No breakdown of coverage dimensions

**Needed**:
- Coverage map showing: local, international, wire, specialist
- Visual distinction between comprehensive vs limited coverage
- Gap indicators when dimensions are missing

**Priority**: HIGH  
**Complexity**: Medium  
**Timeline**: Next feature cycle

---

### 2. Gap Notices Are Implicit, Not Explicit

**Current**:
- Single-source events show "1 sources" (subtle)
- No explicit warnings about incomplete coverage
- Users must infer coverage limitations

**Needed**:
- Explicit gap notices: "⚠️ Only English-language coverage"
- "No local reporting found yet" when event has no local sources
- "Single source - coverage may be incomplete" for 1-publisher events
- Temporal gaps: "Coverage from last 24 hours only"

**Priority**: HIGH  
**Complexity**: Medium  
**Timeline**: Next feature cycle

---

### 3. No "Read Across Coverage" Diversity Action

**Current**:
- Comparison screen shows all sources equally
- No diversity-optimized selection for quick reading
- Users must manually scan for source diversity

**Needed**:
- "Read Across Coverage" button on event cards
- Selects 3-4 sources maximizing diversity (different regions/languages/types)
- Explains selection: "🇮🇪 Local: Irish Times", "🌍 International: BBC"
- Shows why diversity matters for this event

**Priority**: MEDIUM  
**Complexity**: Medium  
**Timeline**: 2-3 feature cycles

---

### 4. No Per-Article Appearance Explanation

**Current**:
- Event comparison footer explains clustering logic generally
- No explanation of why each specific article appears

**Needed**:
- Above each article: "📍 Local reporting from event location"
- Tags: "🌍 International perspective", "📡 Wire coverage"
- Diversity rationale: "Included for geographic diversity"
- Missing perspective callouts: "No coverage from [Region] yet"

**Priority**: MEDIUM  
**Complexity**: Low  
**Timeline**: Next feature cycle

---

## ❌ Not Yet Implemented (Required for Full Alignment)

These features are not present but are required by the principles:

### 1. Coverage Map Visualization

**What**: Visual representation of coverage structure on event cards

**Components**:
- Icons/badges for coverage dimensions present:
  - 📍 Local reporting
  - 🌍 International reporting
  - 📡 Wire/public-service
  - 🔬 Specialist/investigative
- Visual completeness indicator
- One-tap drill-down into each dimension

**Example**:
```
Event: "Climate Summit Reaches Agreement"
Coverage: 🌍 5 international • 📡 2 wire • 📍 0 local
Gap: "No local reporting from Geneva yet"
```

**Priority**: HIGH  
**Complexity**: High  
**Timeline**: 2-3 feature cycles  
**Blockers**: Need source classification infrastructure

---

### 2. Source Classification System

**What**: Tag sources as local/international/wire/specialist for events

**Components**:
- Source type taxonomy: wire, public-broadcaster, newspaper, specialist
- Event-relative classification: local (from event location) vs international
- Specialist detection: domain expertise, primary sources, investigative
- Dynamic classification based on event context

**Example**:
```
Event in Paris:
- Le Monde: LOCAL, newspaper
- BBC: INTERNATIONAL, public-broadcaster
- Al Jazeera: INTERNATIONAL, wire
- France 24: LOCAL, public-broadcaster
```

**Priority**: HIGH  
**Complexity**: High  
**Timeline**: 2-3 feature cycles  
**Dependencies**: SourceMetadata expansion, event location extraction

---

### 3. Verification Status and Disputed Claims

**What**: Distinguish verified reporting from unverified claims

**Components**:
- Verification status labels: "Reported by multiple sources", "Unverified claim", "Disputed"
- Consensus detection: Multiple sources reporting same facts
- Dispute detection: Sources making conflicting claims
- Attribution clarity: "Source A claims X" vs "Multiple sources report X"

**Rules**:
- Never pair verified reporting with unverified claims as equal
- Show when claims lack verification
- Distinguish consensus from dispute
- Avoid false balance

**Priority**: MEDIUM  
**Complexity**: Very High  
**Timeline**: 4-6 feature cycles  
**Blockers**: NLP for claim extraction, verification database

---

### 4. Reporting vs Commentary Distinction

**What**: Label articles as reporting, analysis, or opinion

**Components**:
- Content type detection (reporting vs opinion)
- Labels on article cards and comparison view
- Separate reporting from commentary in coverage counts
- Filter option: "Show only reporting"

**Challenge**: RSS feeds often don't distinguish types

**Priority**: LOW  
**Complexity**: High  
**Timeline**: 6+ feature cycles  
**Alternative**: Rely on publisher editorial policy documentation

---

### 5. Corrections Policy Tracking

**What**: Document and display publisher corrections policies

**Components**:
- Corrections policy URL in SourceMetadata
- "How this publisher handles corrections" link
- Source quality signals based on policy presence
- Corrections history tracking (future)

**Priority**: MEDIUM  
**Complexity**: Medium  
**Timeline**: 3-4 feature cycles  
**Work**: Research corrections policies for all 22 sources

---

### 6. Diversity and Completion Metrics

**What**: Measure product health by informed-exposure criteria

**Metrics to Track**:
- Distinct publishers per event (goal: 3+)
- Languages represented per event (goal: 2+)
- Local-source presence (% events with local coverage)
- Cross-source reading (% users who open 2+ sources per event)
- Coverage-map completeness (% events with local + international + wire)
- Gap visibility (% incomplete events with gap notices shown)

**Dashboard Sections**:
- Event clustering health
- Source diversity trends
- User reading patterns (cross-source behavior)
- Coverage gap analysis

**Priority**: HIGH  
**Complexity**: Medium  
**Timeline**: 2-3 feature cycles  
**Blockers**: Analytics infrastructure

---

## Feature Priorities for Informed Exposure

**REVISED PRIORITIZATION** (2026-09-25, updated)

Build transparency features now using available metadata. The 90% health gate is a **release gate**, not a development blocker.

---

### Priority 1: Gap Notices + "Why This Appears" Explanations
**Goal**: Make coverage limitations and grouping logic transparent

**Status**: READY TO BUILD NOW (uses existing metadata)

**Why Build This First**:
- Makes current 77% source limitation visible to users
- Uses existing metadata (country, language, time)
- No infrastructure dependencies
- High transparency value, low complexity
- Aligns with informed-exposure principle (show gaps plainly)

**Target**: Event comparison UI (where users see multiple sources)

**Components**:

1. **Coverage Gap Notices** (event comparison screen)
   - "⚠️ Only English-language coverage" when all sources share language
   - "Single source - coverage may be incomplete" on single-publisher events
   - "No local reporting found yet" when event has no sources from event location
   - Language gaps: "No French coverage available" (if event is in France)
   - Temporal gaps: "Coverage from last 24 hours only"

2. **"Why This Appears" Explanations** (per article in comparison)
   - Simple, factual labels based on existing metadata
   - "From [Publisher] ([Country])" - already present
   - "Published [timestamp]" - show why it's in 72h window
   - "Different publisher from [other sources in cluster]" - shows diversity
   - NO classification labels without provenance (see constraint below)

**Implementation**:
- Use existing SourceMetadata (country, language)
- Use clustering data (time window, publisher diversity)
- Use simple language detection (infer from RSS feed)
- NO new classification system yet

**Timeline**: START NOW (immediate next work)  
**Complexity**: Low (uses existing data)  
**Release Requirement**: None (improves current experience)

---

### Priority 2: Source Health Gate (Release Gate)
**Goal**: Achieve 90% source success rate for production release

**Status**: In progress, does NOT block Priority 1 development

**Current**: 17/22 sources (77%)  
**Target**: 20/22 sources (90%+)  
**Timeline**: 3-5 days (parallel with Priority 1 development)

**Actions**:
- Identify 5 failing sources by name
- Diagnose each failing source (cURL, format validation)
- Fix or disable to reach 90%+
- Validate 48-hour stability
- Document all fixes and disabled sources

**Role**: RELEASE GATE, not development blocker
- Priority 1 features can be built and tested at 77%
- Gap notices will make 77% limitation visible (good!)
- Must reach 90% before production release
- Allows parallel development and infrastructure work

---

### Priority 3: "Read Across Coverage" Action
**Goal**: Enable one-tap access to maximally diverse sources

**Target**: Event cards and event comparison screen

**Algorithm** (diversity-maximizing selection):
```
Select 3-4 sources from event that maximize:
1. Geographic diversity (different countries)
2. Language diversity (different languages if available)
3. Publisher type diversity (mix of source types)
4. Existing metadata only - no unverified classification
```

**UI**:
- Button on event card: "Read Across Coverage (3 sources)"
- Selection explanation: "Selected for geographic diversity"
- Show each source with country/language: "🇬🇧 BBC (UK, English)"
- Link directly to original articles in external browser

**Provenance Constraint**:
- Only use metadata with documented provenance
- Country: from SourceMetadata (documented ownership location)
- Language: from RSS feed language tags or sourceId inference
- Publisher type: ONLY if documented in SourceMetadata editorial description
- DO NOT infer labels like "wire service" without documented provenance

**Timeline**: 1-2 cycles after Priority 2  
**Complexity**: Medium (selection algorithm + UI)

---

### Priority 4: Coverage Map Visualization
**Goal**: Visual representation of coverage structure

**DEFERRED until**:
- ✅ 90% source health sustained
- ✅ 25+ reliable sources active
- ✅ Sufficient source diversity (3+ regions, 3+ languages)
- ✅ Classification provenance documented for ALL sources

**Rationale**:
- Coverage map is only valuable with diverse, stable source base
- Requires classification labels with clear provenance
- Premature to build visualization on 22 sources (many still failing)

**When Ready, Include**:
- Icons for provenance-backed dimensions only
- Clear "How we classify sources" documentation
- Each label traceable to documented fact
- No inferred or algorithmic classifications

**Timeline**: Deferred to 4+ cycles out  
**Complexity**: High (requires classification infrastructure)

---

### Deprioritized (No Timeline)

**Source Classification System**:
- BLOCKED until provenance documented for all sources
- Cannot add labels like "wire service", "local reporting" without documented basis
- Each classification must be inspectable and verified
- Requires research phase to document provenance for all 22+ sources

**Diversity Metrics Dashboard**:
- Valuable but not user-facing
- Build when analytics infrastructure ready
- Not blocking other features

**User Filtering**:
- Deferred until user research shows need
- Default broad view is working correctly

**Verification Status Labels**:
- Long-term feature requiring NLP infrastructure
- Not critical for informed exposure (attribution sufficient for now)

**Reporting vs Commentary Labels**:
- RSS feeds often don't distinguish
- Low value without comprehensive publisher data

---

## Critical Constraint: Provenance for All Labels

**RULE**: Do not add source classification labels unless each label has clear, inspectable provenance.

### What This Means

**Permitted** (with documentation):
- **Country**: From documented publisher headquarters/ownership location
  - Example: "BBC (UK)" - documented as British public broadcaster in BBC Royal Charter
  - Provenance: SourceMetadata.country with documented ownership

- **Language**: From RSS feed language tags or publisher documentation
  - Example: "Le Monde (French)" - RSS feed declares language="fr"
  - Provenance: RSS metadata or publisher's stated language

- **Publisher Type**: ONLY when documented in official publisher description
  - Example: "Al Jazeera - State-funded international news service"
  - Provenance: SourceMetadata.editorialDescription with documented provenance field

**Prohibited** (without provenance):
- **"Wire service"** - Cannot infer without documented evidence of wire service status
  - BAD: Assuming Reuters is wire service because it seems like one
  - GOOD: Documenting Reuters' official self-description as news agency

- **"Local reporting"** - Cannot infer without documented presence in event location
  - BAD: Assuming source is "local" because country matches event location
  - GOOD: Documenting publisher has reporters/bureau in event location

- **"Investigative journalism"** - Cannot infer without documented investigative team
  - BAD: Labeling long articles as "investigative"
  - GOOD: Publisher documents investigative unit and this article is from that unit

- **"Primary source"** - Cannot infer without documented direct access
  - BAD: Assuming article is primary source because it's detailed
  - GOOD: Article explicitly states "our reporters witnessed" with byline

### Why This Matters

**Without provenance**:
- Labels become editorial judgments, not facts
- Users cannot verify claims about sources
- Classifications appear arbitrary or biased
- Violates transparency principle

**With provenance**:
- Every label traceable to documented fact
- Users can inspect classification basis
- Classifications are verifiable and consistent
- Maintains trust and transparency

### Implementation Standard

When adding ANY classification to a source:

1. **Document the basis**: What official source confirms this classification?
2. **Record provenance**: Where does this information come from?
3. **Make it inspectable**: Can users see the documentation?
4. **Apply consistently**: Same standard for all sources, all regions

**Example - Good Implementation**:
```kotlin
SourceMetadata(
    publisherName = "Al Jazeera",
    country = "Qatar",
    editorialDescription = "State-funded international news service",
    descriptionProvenance = "Al Jazeera corporate profile",
    sourceType = "international-broadcaster", // ONLY because documented
    sourceTypeProvenance = "Al Jazeera About page: 'international broadcaster'"
)
```

**Example - Bad Implementation**:
```kotlin
SourceMetadata(
    publisherName = "The Guardian",
    sourceType = "left-leaning newspaper", // PROHIBITED - ideological inference
    sourceType = "quality journalism", // PROHIBITED - subjective judgment
    sourceType = "wire service" // PROHIBITED - no documented evidence
)
```

### Before Adding Classifications

**Research phase required**:
1. Review each source's official About page
2. Document official self-descriptions
3. Verify against third-party documentation (e.g., Reuters Institute)
4. Record provenance for every classification
5. Build reviewable classification database
6. Only then expose labels to users

**Timeline**: Research phase for 22+ sources is substantial work (weeks, not days). Do not rush this.

---

## Implementation Strategy (Revised)

### Build Order Rationale

**Priority 1 (Gap Notices + Explanations)** comes first:
- Low complexity, high impact
- Uses existing data (source counts, languages, clustering)
- No infrastructure dependencies
- Makes current 77% limitation visible (aligns with informed-exposure principle)
- Builds trust through transparency
- Can ship immediately without waiting for health gate

**Priority 2 (Source Health Gate)** runs in parallel as release gate:
- Works on infrastructure while Priority 1 develops
- 77% is acceptable for development and testing
- Must reach 90%+ before production release
- Does NOT block feature development
- Timeline: 3-5 days parallel to Priority 1

**Priority 3 ("Read Across Coverage")** enables key use case:
- Deliberate diversity exposure is core to informed reading
- Can build with existing metadata (country, language)
- Does not require classification system
- High user value for cross-source reading
- Start after Priority 1 ships

**Priority 4 (Coverage Map)** deferred until:
- Stable source base (25+ sources at 90%+)
- Classification provenance documented
- Sufficient diversity to make map meaningful
- Premature with current 22-source base

**Parallel Development**:
- Build Priority 1 features now (gap notices, explanations)
- Fix source health in parallel (release gate)
- Do not let infrastructure work block product development
- Gap notices make 77% visible, which is good transparency

### Technology Dependencies

**No Blockers**:
- Gap notices (use existing data)
- Per-article explanation (use existing metadata)
- Corrections policy tracking (research + documentation)

**Moderate Dependencies**:
- Coverage map visualization (needs source classification)
- Diversity metrics (needs analytics infrastructure)
- "Read Across Coverage" (needs classification + selection algorithm)

**Major Dependencies**:
- Verification status (needs NLP, claim extraction, verification DB)
- Reporting vs commentary (needs content analysis or publisher data)

### Incremental Delivery

Each phase delivers user-visible value:
- **Phase 1**: Users see when coverage is incomplete
- **Phase 2**: Users understand coverage structure
- **Phase 3**: Users can easily read across diverse sources
- **Phase 4**: Users distinguish verified reporting from claims

No phase depends on all prior phases completing - some can run in parallel.

---

## Success Criteria

### Phase 1 Success
- [ ] 100% of single-source events show gap notice
- [ ] 100% of language-limited events show language notice
- [ ] Dashboard tracks publishers per event and cross-source reading

### Phase 2 Success
- [ ] All sources classified by type (wire/broadcaster/newspaper/specialist)
- [ ] Event cards show coverage map (local/international/wire counts)
- [ ] 80%+ of major events have 2+ coverage dimensions

### Phase 3 Success
- [ ] "Read Across Coverage" available on all multi-source events
- [ ] Selection algorithm maximizes diversity (verified via metrics)
- [ ] Users open 2+ sources per event 40%+ of the time (up from baseline)

### Phase 4 Success
- [ ] Consensus events labeled: "Reported by N sources"
- [ ] Disputed events labeled: "Conflicting claims"
- [ ] Unverified claims attributed clearly with source
- [ ] Zero false-balance pairings (verified vs unverified as equals)

---

## Anti-Patterns to Avoid

While building these features, explicitly avoid:

### ❌ Engagement Optimization
- Do NOT rank by click-through rate
- Do NOT amplify controversial events
- Do NOT optimize for time-on-site
- Do NOT A/B test for session length

### ❌ Personalization Creep
- Do NOT use reading history to filter events
- Do NOT predict user preferences algorithmically
- Do NOT create echo chambers
- Do NOT hide sources user "doesn't like"

### ❌ False Balance
- Do NOT pair verified reporting with unverified claims as equals
- Do NOT treat all sources as equally credible by default
- Do NOT hide verification status to seem "neutral"
- Do NOT present conspiracy theories alongside reporting

### ❌ Hidden Decisions
- Do NOT make algorithmic choices without explanation
- Do NOT use opaque ranking or selection
- Do NOT hide coverage gaps to seem complete
- Do NOT optimize silently for metrics users can't see

---

## Measurement Framework

### Primary Metrics (Optimize For)
1. **Distinct publishers per event** (target: 3+ for major events)
2. **Cross-source reading rate** (% users opening 2+ sources)
3. **Coverage completeness** (% events with local + international + wire)
4. **Gap visibility** (% incomplete events with notices shown)
5. **Languages represented** (2+ languages for global events)

### Health Metrics (Monitor, Don't Optimize)
- Time spent reading original sources (external)
- Return rate for news checking (not for engagement)
- Diversity of sources opened per user

### Anti-Metrics (Never Optimize)
- Total time in app
- Click-through rate on any content
- Return rate after controversial content
- Personalization filter accuracy

---

## Review Schedule

- **Weekly**: Feature alignment check during development
- **Per Release**: Principle compliance audit before shipping
- **Quarterly**: Metrics review (are we optimizing the right things?)
- **Annually**: Principle evolution (do principles need updating?)

---

## Conclusion (Revised)

Current implementation has a **strong foundation** for informed exposure:
- ✅ No engagement optimization
- ✅ No personalization bubbles  
- ✅ Clear attribution
- ✅ Conservative clustering

**Revised implementation sequence** (parallel development, release gate):

1. **Gap Notices + Explanations** - START NOW
   - Make coverage limitations visible
   - Explain why articles are grouped
   - Use existing data only (no new classification)
   - Ships immediately (no release gate dependency)

2. **Source Health Gate (Release Gate)** - Parallel, 3-5 days
   - Fix failing sources to reach 90%+ success rate
   - Validate 48-hour stability
   - Does NOT block Priority 1 development
   - Blocks production release only

3. **"Read Across Coverage" Action** - After Priority 1 ships
   - Enable one-tap diverse source selection
   - Use documented metadata only (country, language)
   - Core informed-exposure use case

4. **Coverage Map (Deferred)** - When source base mature
   - Wait for 25+ stable sources
   - Document classification provenance first
   - Build visualization only on verified data

**Key Change**: Health gate is a **release gate**, not a development blocker. Build transparency features now; they make the current 77% limitation visible, which aligns with informed-exposure principles.

**Critical constraint**: No classification labels without clear, inspectable provenance. Research phase required before adding "wire service", "local reporting", or similar labels.

**Timeline**: Gap notices + explanations start now (immediate work). Source health fixes run in parallel. "Read Across Coverage" follows. Coverage visualization deferred until source base and provenance research complete.

**Principle commitment**: When principles conflict with convenience or engagement, **principles win**. When tempted to add unverified labels, **document provenance first**. When infrastructure work would block transparency, **build transparency anyway**.

---

**Last Updated**: 2026-09-25  
**Status**: Roadmap established, Phase 1 planning next  
**Owner**: Product team (guided by principles, not metrics)
