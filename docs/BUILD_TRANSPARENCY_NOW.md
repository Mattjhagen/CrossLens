# Build Transparency Features Now

**Decision**: Don't let the 90% health gate block transparency features. It gates production release, not product development.

---

## What Changed

### Before (Incorrect Approach)
**Priority 1**: Source Health Gate (BLOCKING)
- Fix 5 failing sources first
- Reach 90% success rate
- THEN build transparency features

**Problem**: Infrastructure work blocking product development unnecessarily.

### After (Correct Approach)
**Priority 1**: Gap Notices + Explanations (START NOW)
- Build using existing metadata
- Ship immediately at 77%
- Make current limitations visible

**Priority 2**: Source Health Gate (PARALLEL)
- Fix sources in parallel
- Release gate only, not development blocker
- 90% required for production, not for development

---

## Why This Is Better

### 1. Aligns with Informed-Exposure Principle

**"Show gaps plainly"** is a core principle. Gap notices make the current 77% limitation visible to users, which is exactly what informed exposure requires.

**Before**: Hiding 77% limitation until infrastructure improves  
**After**: Showing 77% limitation transparently with gap notices

### 2. Parallel Development vs Sequential Blocking

**Before**: Wait 3-5 days for health gate, THEN start transparency work  
**After**: Build transparency NOW, fix health in parallel

**Timeline**:
- Transparency features: Start immediately
- Source health fixes: Run in parallel (3-5 days)
- Total time to both complete: Same as health alone

### 3. Gap Notices Improve Current Experience

At 77% source health:
- Some events have limited coverage
- Some events are English-only
- Some events have single sources

**Gap notices make this visible**:
- "⚠️ Only English-language coverage"
- "Single source - coverage may be incomplete"
- "No local reporting found yet"

**This is good transparency**, even at 77%.

### 4. No Infrastructure Dependencies

Gap notices use existing data:
- Source count (from clustering)
- Languages (from SourceMetadata)
- Countries (from SourceMetadata)
- Time windows (from clustering logic)

**No new systems required**. Can build and ship immediately.

---

## What This Means Practically

### Immediate Next Work (Priority 1)

**Build gap notices and explanations now**:

1. **Event Comparison Screen** - Add gap notices:
   - Detect single-source events → "Single source - coverage may be incomplete"
   - Detect single-language events → "⚠️ Only [Language]-language coverage"
   - Detect missing local coverage → "No local reporting from [Location] yet"
   - Detect narrow time window → "Coverage from last 24 hours only"

2. **Per-Article Explanations** - Show why each appears:
   - "From [Publisher] ([Country])"  ← already present
   - "Published [timestamp]"  ← shows time window membership
   - "Different publisher from [others]"  ← shows diversity
   - NO classification labels yet (requires provenance research)

**Data sources**: All existing (SourceMetadata, clustering data, time windows)

**Implementation**: Simple conditional checks, no new infrastructure

**Timeline**: Can start immediately, ship quickly

### Parallel Work (Priority 2)

**Fix source health for release gate**:

1. Add debug logging to identify 5 failing sources
2. Diagnose each (cURL tests, format validation)
3. Fix or disable to reach 90%+
4. Validate 48-hour stability

**Role**: Release gate only, does not block Priority 1

**Timeline**: 3-5 days in parallel

### After Priority 1 Ships (Priority 3)

**Build "Read Across Coverage" action**:
- One-tap diverse source selection
- Uses documented metadata only (country, language)
- No dependency on health gate
- Core informed-exposure feature

---

## Health Gate Role Clarified

### What 90% Health Gate IS:
- ✅ Production release requirement
- ✅ Quality bar for public deployment
- ✅ Stability validation checkpoint
- ✅ Infrastructure maturity gate

### What 90% Health Gate IS NOT:
- ❌ Development blocker
- ❌ Feature design requirement
- ❌ Testing prerequisite
- ❌ Reason to delay transparency work

### 77% Is Acceptable For:
- ✅ Development and testing
- ✅ Feature implementation
- ✅ Internal builds and validation
- ✅ Learning and iteration

### 90% Is Required For:
- ✅ Production release
- ✅ Public deployment
- ✅ App store submission
- ✅ User-facing stability

---

## Transparency at 77%

**The gap notices will show the current reality**:

### Example Event at 77%

**Climate Summit in Geneva**

**Sources**: 2 (The Guardian, BBC)

**Gap Notices Shown**:
- "⚠️ Only English-language coverage"
- "No local reporting from Switzerland yet"
- "2 sources from similar publishers (UK-based)"

**Why This Is Good**:
- Users see coverage limitations plainly
- No false impression of comprehensive coverage
- Aligns with informed-exposure principle
- Makes 77% infrastructure visible (transparency!)

### When Health Reaches 90%

**Same Event with More Sources**

**Sources**: 4 (The Guardian, BBC, swissinfo.ch, Le Monde)

**Gap Notices Updated**:
- ~~"Only English-language coverage"~~  ← Le Monde added French
- ~~"No local reporting from Switzerland"~~  ← swissinfo.ch added
- "4 sources - English (2), French (2)"

**Progressive improvement visible to users**.

---

## Implementation Checklist

### Priority 1: Gap Notices (START NOW)

**Event Comparison Screen**:
- [ ] Detect single-source events (count == 1)
  - Show: "Single source - coverage may be incomplete"
- [ ] Detect single-language events (all sources same language)
  - Show: "⚠️ Only [Language]-language coverage"
- [ ] Detect missing local coverage (no sources from event country)
  - Show: "No local reporting from [Location] yet"
  - Note: Requires event location extraction (may defer)
- [ ] Detect narrow time window (all articles within 24h)
  - Show: "Coverage from last 24 hours only"

**Per-Article Explanations**:
- [ ] Show publisher with country (already present)
- [ ] Show publication timestamp
- [ ] Explain why in 72h window ("Published within clustering window")
- [ ] Highlight publisher diversity ("Different publisher from [others]")

**Data Requirements**:
- ✅ Source count: Available from clustering
- ✅ Languages: Available from SourceMetadata
- ✅ Countries: Available from SourceMetadata  
- ✅ Time window: Available from clustering logic
- ⚠️ Event location: Requires extraction (can defer)

**No Blockers**: All required data available except optional event location.

### Priority 2: Source Health (PARALLEL)

**Does Not Block Priority 1**:
- [ ] Add debug logging
- [ ] Identify failing sources
- [ ] Diagnose and fix
- [ ] Reach 90%+
- [ ] Validate stability

**Timeline**: 3-5 days parallel to Priority 1 development

---

## Success Criteria

### Priority 1 Success
- [ ] Gap notices visible on events with limitations
- [ ] Single-source events show "incomplete coverage" notice
- [ ] Single-language events show language limitation
- [ ] Per-article explanations show why included
- [ ] Zero inferred classification labels (provenance constraint maintained)
- [ ] Ships at any source health level (no dependency on 90%)

### Priority 2 Success (Release Gate)
- [ ] 20/22 sources ACTIVE (91%+)
- [ ] 48-hour stability validated
- [ ] All failing sources diagnosed and documented
- [ ] Ready for production release

### Combined Success
- [ ] Transparency features shipped and improving UX at 77%
- [ ] Source health reaches 90% for production release
- [ ] Both complete in similar timeframe to health-first approach
- [ ] Product development not blocked by infrastructure work

---

## Why This Decision Matters

### Product Principle: Transparency Over Perfection

**Informed exposure means showing coverage limitations plainly**, not hiding them until perfect.

Gap notices at 77% are:
- ✅ Transparent (show what's missing)
- ✅ Honest (don't pretend comprehensive)
- ✅ Educational (help users understand coverage)
- ✅ Aligned with principles (informed exposure)

Waiting for 90% would be:
- ❌ Hiding limitations (not transparent)
- ❌ Pretending more complete than reality
- ❌ Missing opportunity to educate users
- ❌ Blocking valuable transparency work

### Development Velocity: Parallel Over Sequential

**Sequential (Before)**:
```
Days 0-5: Fix health gate (product development blocked)
Days 5-8: Build transparency features
Total: 8 days to both complete
```

**Parallel (After)**:
```
Days 0-3: Build transparency features
Days 0-5: Fix health gate (parallel)
Total: 5 days to both complete
```

**Saves 3 days and delivers transparency sooner.**

### User Impact: Transparency Now vs Later

**Scenario**: User opens app at 77% health

**Without gap notices**:
- Sees "3 sources" on event
- Assumes coverage is comprehensive
- Doesn't know it's all English-only
- Doesn't know local reporting missing
- **False impression of completeness**

**With gap notices**:
- Sees "3 sources"
- Also sees "⚠️ Only English-language coverage"
- Also sees "No local reporting from [Location] yet"
- Understands coverage limitations
- **Accurate impression with visible gaps**

**Gap notices improve the 77% experience immediately.**

---

## Conclusion

**Old Approach**: Block transparency on health gate (sequential)  
**New Approach**: Build transparency now, health gate is release gate (parallel)

**Why Change**:
1. Aligns with "show gaps plainly" principle
2. Parallel development is faster
3. Improves current 77% experience immediately
4. Uses existing data (no infrastructure dependency)
5. Makes limitations visible (good transparency!)

**What This Means**:
- Start building gap notices and explanations NOW
- Fix source health in PARALLEL (for release gate)
- Do not wait for 90% to build transparency
- Ship transparency at any health level
- Require 90% only for production release

**Timeline**:
- Transparency: Start immediately, ship quickly
- Health gate: Fix in parallel (3-5 days)
- Read across: Build after transparency ships
- Coverage map: Defer until provenance documented

**Next Work**: Implement gap notices and per-article explanations using existing metadata. No blockers.

---

**Status**: ✅ **Approach revised, ready to start Priority 1 immediately**  
**Blocker Removed**: Health gate no longer blocks transparency features  
**Next Action**: Begin implementation of gap notices and explanations
