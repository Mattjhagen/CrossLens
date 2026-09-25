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

### Phase 1: Visibility (Next 2 Cycles)
**Goal**: Make coverage structure and gaps visible

1. **Explicit Gap Notices** (HIGH priority)
   - "Only English-language coverage"
   - "Single source - coverage may be incomplete"
   - "No local reporting found yet"

2. **Per-Article Appearance Explanation** (MEDIUM priority)
   - Tags: local/international/wire
   - "Why this article is included" text

3. **Diversity Metrics Dashboard** (HIGH priority)
   - Track publishers per event
   - Monitor cross-source reading
   - Measure coverage completeness

### Phase 2: Structure (Cycles 3-4)
**Goal**: Show coverage dimensions, not just counts

4. **Source Classification System** (HIGH priority, blocking)
   - Tag sources as wire/broadcaster/newspaper/specialist
   - Classify local vs international per event
   - Expand SourceMetadata with source types

5. **Coverage Map Visualization** (HIGH priority)
   - Visual indicators for coverage dimensions
   - Icons: 📍 local, 🌍 international, 📡 wire
   - Completeness scoring

6. **Corrections Policy Tracking** (MEDIUM priority)
   - Document corrections policies for all sources
   - Add to SourceMetadata
   - Display in source attribution

### Phase 3: Action (Cycles 5-6)
**Goal**: Enable deliberate cross-source reading

7. **"Read Across Coverage" Feature** (MEDIUM priority)
   - Diversity-optimized source selection (3-4 sources)
   - Explains selection: "Local + International + Wire"
   - One-tap access to maximally diverse set

8. **User Filtering with Broad Default** (LOW priority)
   - Allow users to filter by region/language/type
   - Default always shows full coverage
   - One-tap "Reset to Full Coverage" button

### Phase 4: Verification (Cycles 7+)
**Goal**: Distinguish verified reporting from claims

9. **Verification Status Labels** (MEDIUM priority, complex)
   - "Reported by multiple sources" (consensus)
   - "Unverified claim from [Source]"
   - "Disputed" (conflicting claims)

10. **Reporting vs Commentary Labels** (LOW priority)
    - Tag articles as reporting/analysis/opinion
    - Filter option for reporting-only view

---

## Implementation Strategy

### Build Order Rationale

**Phase 1 (Visibility)** comes first because:
- Low complexity, high impact
- Uses existing data (source counts, languages)
- No new infrastructure required
- Immediate alignment with principles

**Phase 2 (Structure)** enables Phase 3:
- Source classification is foundational
- Coverage maps depend on classification
- Metrics track structural diversity

**Phase 3 (Action)** builds on Phase 2:
- "Read Across" needs source classification
- Filtering requires coverage structure
- User actions guided by visible structure

**Phase 4 (Verification)** is most complex:
- Requires NLP and verification databases
- Depends on claim extraction technology
- Can defer without blocking core value

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

## Conclusion

Current implementation has a **strong foundation** for informed exposure:
- ✅ No engagement optimization
- ✅ No personalization bubbles  
- ✅ Clear attribution
- ✅ Conservative clustering

**Next steps** to full alignment:
1. **Phase 1** (2 cycles): Add gap notices, per-article explanations, diversity metrics
2. **Phase 2** (2 cycles): Build source classification, coverage maps, corrections tracking
3. **Phase 3** (2 cycles): Enable "Read Across Coverage" diversity action
4. **Phase 4** (long-term): Add verification status and claim distinction

**Timeline**: Full alignment in 6-8 feature cycles, with incremental value delivered at each phase.

**Principle commitment**: When principles conflict with convenience or engagement, **principles win**.

---

**Last Updated**: 2026-09-25  
**Status**: Roadmap established, Phase 1 planning next  
**Owner**: Product team (guided by principles, not metrics)
