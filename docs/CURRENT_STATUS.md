# CrossLens: Current Status & Next Steps

**Date**: 2026-09-25  
**Branch**: feature/live-feed-v0.0.14-beta  
**Status**: Source health gate at 77%, blocking feature expansion

---

## Executive Summary

**Product Direction Established**: CrossLens optimizes for **informed exposure, not agreement or time spent**. Complete principles documented with implementation roadmap.

**Current State**: Source health at 77% (17/22 sources). This is a **release gate**, not a development blocker.

**Next 3 Priorities** (parallel development):
1. **Add gap notices + explanations** (START NOW)
2. **Pass 90% health gate** (parallel, 3-5 days - release gate only)
3. **Build "Read Across Coverage" action** (after #1)

---

## What Was Delivered Today

### 1. Product Principles Documentation ✅

**PRODUCT_PRINCIPLES.md** - Complete specification of informed-exposure principles:
- 9 implementation rules
- Feature decision framework
- Anti-patterns to avoid
- Success metrics (diversity, not engagement)

**Key Principle**: "Every side" means credible, attributable reporting with visible gaps, NOT equal weight to unsupported claims.

### 2. Source Health Monitoring Infrastructure ✅

**Health tracking for all RSS sources**:
- Per-fetch metrics (success/failure, articles, images, errors)
- Auto-status management (ACTIVE/DEGRADED/DISABLED)
- 90% production-ready gate enforced
- Korea Herald disabled (broken feed)
- SCMP redirect fixed

**Current Health**: 17/22 sources (77%) - **BELOW THRESHOLD**

### 3. Focused Implementation Roadmap ✅

**INFORMED_EXPOSURE_ROADMAP.md** with revised priorities:
- Source health gate (BLOCKING)
- Gap notices using existing data
- "Read Across Coverage" diversity action
- Coverage map **deferred** until source base mature

**Critical Constraint**: No classification labels without documented provenance.

---

## Current State

### Source Infrastructure
- **Configured**: 22 sources (Korea Herald disabled)
- **Active**: 17 sources (77%)
- **Target**: 20 sources (90%+)
- **Gap**: 3 additional sources must be fixed

### Regional Balance (22 configured)
- Europe/UK: 9 sources (41%)
- Asia-Pacific: 7 sources (32%)
- North America: 3 sources (14%)
- Middle East: 2 sources (9%)

### Language Distribution
- English: 20 sources (91%)
- French: 1 source (5%)
- Spanish: 1 source (5%)
- Japanese: 1 source (5%)

### Testing
- ✅ 189/189 unit tests passing
- ✅ APK builds successfully
- ✅ Device verified: Feed loading with 17 sources
- ✅ Health monitoring operational

---

## Release Gate: 77% Source Success Rate

**Status**: Below 90% production-ready threshold (release gate, not development blocker)

**Problem**: 5 sources failing, unknown which ones

**Impact**: Blocks production release, does NOT block feature development

**Next Steps** (parallel with Priority 1 development):
1. Add debug logging to identify failing sources by name
2. Diagnose each failing source (cURL tests, format validation)
3. Fix or disable to reach 20/22 (91%)
4. Validate 48-hour stability at 90%+

**Timeline**: 3-5 days (parallel with transparency feature development)

**Likely Failing Sources**:
- Asahi Shimbun (RDF format, not RSS 2.0)
- ABC Spain (possible geoblocking)
- Channel NewsAsia (API endpoint, rate limiting)
- swissinfo.ch (unknown issue)
- One Batch 1 source (device-specific)

---

## Revised Feature Priorities (Parallel Development)

### Priority 1: Gap Notices + "Why This Appears"
**Status**: READY TO START NOW  
**Timeline**: Immediate next work  
**Complexity**: Low (uses existing data)  
**Release Gate**: None (improves current 77% experience)

**Components**:

1. **Coverage Gap Notices** (event comparison screen):
   - "⚠️ Only English-language coverage" (when all sources same language)
   - "Single source - coverage may be incomplete" (1 publisher)
   - "No local reporting found yet" (no sources from event location)
   - Temporal gaps: "Coverage from last 24 hours only"

2. **"Why This Appears" Explanations** (per article):
   - "From [Publisher] ([Country])" - already present
   - "Published [timestamp]" - shows 72h window membership
   - "Different publisher from [others]" - shows diversity
   - NO classification labels without provenance

**Data Sources**: Existing SourceMetadata (country, language), clustering data (time window, publisher diversity)

**Why Build This First**:
- Makes current 77% limitation visible to users (transparency principle)
- No infrastructure dependencies
- Can ship immediately without waiting for health gate
- Gap notices will actually improve current user experience at 77%

### Priority 2: Source Health Gate (Release Gate)
**Status**: IN PROGRESS (parallel with Priority 1)  
**Timeline**: 3-5 days  
**Role**: RELEASE GATE (does not block Priority 1 development)

**Actions**:
- [ ] Add debug logging to identify failing sources
- [ ] Run feed refresh, capture logcat with errors
- [ ] Diagnose each failing source (cURL + format check)
- [ ] Fix or disable sources to reach 90%+
- [ ] Validate stability over 48 hours
- [ ] Document all fixes and disabled sources

**Deliverable**: 20/22 sources active, stable for 48 hours before production release

### Priority 3: "Read Across Coverage" Action
**Status**: PLANNED  
**Timeline**: 1-2 cycles after Priority 2  
**Complexity**: Medium (selection algorithm + UI)

**Feature**:
- Button on event cards: "Read Across Coverage (3 sources)"
- Selects 3-4 sources maximizing diversity (different countries/languages)
- Explains selection: "Selected for geographic diversity"
- Shows each source: "🇬🇧 BBC (UK, English)"
- Links directly to original articles

**Constraint**: Use only documented metadata (country, language). NO inferred labels.

### Priority 4: Coverage Map (DEFERRED)
**Status**: BLOCKED until provenance research complete  
**Timeline**: 4+ cycles out  
**Prerequisites**:
- 25+ reliable sources active
- Classification provenance documented for ALL sources
- Sufficient diversity to make map meaningful

---

## Critical Constraint: Classification Provenance

**RULE**: Do not add source classification labels unless each label has clear, inspectable provenance.

**Permitted** (with documentation):
- **Country**: From documented publisher headquarters/ownership
- **Language**: From RSS feed tags or publisher documentation
- **Publisher Type**: ONLY when documented in official description

**Prohibited** (without provenance):
- "Wire service" - cannot infer without documented evidence
- "Local reporting" - cannot infer without documented bureau presence
- "Investigative" - cannot infer without documented investigative unit
- "Primary source" - cannot infer without explicit article statement

**Why**: Labels without provenance are editorial judgments, not facts. Violates transparency principle. Users cannot verify.

**Timeline Impact**: Source classification requires weeks-long research phase to document provenance for all sources. Do not rush.

---

## What's Working Well ✅

### Strong Foundation for Informed Exposure

Current implementation **already aligns** with core principles:

1. **No engagement optimization** - Objective clustering, no controversy amplification
2. **No personalization bubbles** - Default feed shows all sources equally
3. **Clear attribution** - Publisher, country, language, ownership documented
4. **No ideological labels** - Only factual descriptors
5. **Safety-compliant language** - Never claims truth/bias/reliability
6. **Conservative clustering** - High thresholds prevent false matches

**Takeaway**: Core architecture built correctly from the start. No major refactoring required.

---

## Decisions Made Today

### ✅ Product Direction
- **Informed exposure** is foundational principle
- **Diversity over engagement** in all decisions
- **Transparency over personalization** in all features

### ✅ Implementation Priorities
- **Health gate first** - fix infrastructure before expanding
- **Transparency next** - gap notices with existing data
- **Diversity action third** - "Read Across Coverage" core feature
- **Visualization last** - coverage map deferred until mature

### ✅ Quality Standards
- **Provenance required** for all classification labels
- **No inferred labels** without documented basis
- **Research phase** before adding source classifications
- **Inspection transparency** - users can verify all claims

### ❌ Explicitly Rejected
- Engagement optimization (clicks, time-on-site)
- Personalization based on reading history
- False balance (unverified = verified)
- Hidden algorithmic decisions
- Classification labels without provenance

---

## Metrics Framework

### Primary Metrics (Optimize For)
- Distinct publishers per event (target: 3+)
- Cross-source reading rate (% users opening 2+ sources)
- Coverage completeness (% events with diverse dimensions)
- Gap visibility (% incomplete events with notices)

### Anti-Metrics (Never Optimize)
- Total time spent in app
- Click-through rate on controversial content
- Personalization accuracy
- Return rate after outrage

---

## File Changes Summary

### Code (5 files)
- `SourceHealth.kt` - Health data models (NEW)
- `SourceHealthMonitor.kt` - Tracking service (NEW)
- `RssSourceAdapter.kt` - Health integration, 22 sources, Korea Herald disabled
- `IngestionModule.kt` - Hilt DI for health monitor
- `SourceAdapter.kt` - No changes (staged accidentally, reverted)

### Documentation (6 files)
- `PRODUCT_PRINCIPLES.md` - Complete principle specification (NEW)
- `INFORMED_EXPOSURE_ROADMAP.md` - Phased implementation plan (NEW)
- `SOURCE_HEALTH_GATE.md` - Health monitoring spec (NEW)
- `SOURCE_HEALTH_ANALYSIS.md` - 77% investigation (NEW)
- `SOURCE_HEALTH_IMPLEMENTATION_SUMMARY.md` - Status report (NEW)
- `CURRENT_STATUS.md` - This file (NEW)

### Commits
```
2a33211 docs: revise informed-exposure roadmap with focused priorities
b56fdfc docs: establish informed-exposure product principles
8904f92 docs: add source health analysis and implementation summary
205f6fa feat: add RSS source health monitoring infrastructure
2efd65a feat: expand source registry to 23 international publishers (Batch 1)
```

---

## Next Session: Action Items

### Immediate (Today/Tomorrow)
1. [ ] Add debug logging to RssSourceAdapter health checks
2. [ ] Clear app data, trigger feed refresh
3. [ ] Capture logcat with source-by-source success/failure
4. [ ] Identify exact 5 failing sources by name

### Short-Term (This Week)
5. [ ] Diagnose each failing source (cURL, format validation, error analysis)
6. [ ] Apply fixes (URL corrections, parser updates, or disable)
7. [ ] Rebuild APK, test on device
8. [ ] Verify 20-22/22 sources active (90%+)
9. [ ] Monitor for 48 hours to ensure stability

### After Health Gate Passes
10. [ ] Begin Priority 2: Gap notices + explanations
11. [ ] Design gap notice UI for event comparison screen
12. [ ] Implement using existing metadata (no classification)
13. [ ] Test and ship transparency features

### Long-Term (Weeks/Months)
14. [ ] Research classification provenance for all sources
15. [ ] Document wire/local/investigative labels with evidence
16. [ ] Build "Read Across Coverage" feature
17. [ ] Expand to 30+ sources incrementally
18. [ ] Build coverage map visualization (when ready)

---

## Key Constraints

### Do Not Proceed Until
- ✅ 90%+ source success rate sustained for 48 hours
- ✅ All failing sources diagnosed and documented
- ✅ Health gate status confirmed

### Do Not Build Without
- ✅ Clear alignment with informed-exposure principles
- ✅ Documented provenance for classification labels
- ✅ Transparency in grouping/selection logic
- ✅ Diversity optimization (not engagement optimization)

### Do Not Ship Without
- ✅ Testing on device (not just desktop)
- ✅ All unit tests passing
- ✅ Documentation updated
- ✅ No unverified labels or claims

---

## Success Criteria

### Source Health Gate Passed
- [ ] 20/22 sources ACTIVE (91%+)
- [ ] Zero DEGRADED sources for 24h+
- [ ] All failing sources diagnosed with documented reasons
- [ ] 48-hour stability validation complete

### Gap Notices + Explanations Shipped
- [ ] All single-source events show "incomplete coverage" notice
- [ ] All language-limited events show language gap notice
- [ ] Every article in comparison explains why it appears
- [ ] Zero inferred classification labels

### "Read Across Coverage" Shipped
- [ ] Available on all multi-source events
- [ ] Maximizes diversity (different countries/languages)
- [ ] Explains selection rationale clearly
- [ ] Links to original articles correctly

---

## Questions to Ask Before Any Feature

1. **Does it increase informed exposure?**
   - Does it help users see coverage diversity?
   - Does it make gaps visible?
   - Does it encourage cross-source reading?

2. **Does it avoid false balance?**
   - Does it treat verification status clearly?
   - Does it maintain consistent source standards?
   - Does it attribute claims appropriately?

3. **Does it resist engagement optimization?**
   - Does it avoid amplifying outrage?
   - Does it avoid personalization bubbles?
   - Does it prioritize diversity over clicks?

4. **Is it transparent?**
   - Can users see why content appears?
   - Are gaps plainly shown?
   - Is selection logic clear?

5. **Does it have documented provenance?**
   - Are all labels based on documented facts?
   - Can users verify classifications?
   - Is evidence inspectable?

**If any answer is "no", redesign or reject the feature.**

---

## Status Summary

**Product**: ✅ **Direction established with clear principles**  
**Infrastructure**: ⚠️ **77% source health, below threshold**  
**Roadmap**: ✅ **Focused priorities defined**  
**Blocker**: 🚫 **Must reach 90% before feature expansion**  
**Timeline**: 3-5 days to pass health gate, then proceed with transparency features

**Next Milestone**: Pass 90% source health gate and validate stability.

---

**Last Updated**: 2026-09-25 16:30  
**Prepared By**: Implementation team + Claude Sonnet 4.5  
**Status**: Ready to proceed with health gate diagnostics
