# CrossLens Product Principles

## Core Principle: Optimize for Informed Exposure

**CrossLens should optimize for informed exposure, not agreement or time spent.**

"Every side" cannot mean giving unsupported claims equal weight. It should mean giving people a fair view of credible, attributable reporting and clearly showing where coverage is thin, disputed, or missing.

The goal is not to tell readers what is true. It is to make the evidence, attribution, and limits of available coverage visible enough for them to think clearly.

---

## Implementation Rules

### 1. Default Feeds Show Mixed Source Contexts

**Principle**: No personalized political lanes. Default view shows broad coverage diversity.

**Rules**:
- Default feed presents events from multiple regions, languages, and publisher types
- No algorithmic filtering based on user's prior reading patterns alone
- No optimization for engagement metrics (clicks, time spent, shares)
- No outrage amplification or controversy scoring
- Users can filter sources, but **default is broad with one-tap reset**

**Current Implementation**:
- ✅ Live feed shows all sources without personalization
- ✅ No engagement optimization in clustering or ranking
- ⚠️ No user filtering yet (good - forces broad exposure)
- ❌ No one-tap reset needed (filtering not implemented)

**Future Work**:
- When filtering is added, make default broad and add "Reset to Full Coverage" button
- Ensure filters are opt-in, not algorithmic predictions

---

### 2. Event Cards Show Coverage Maps

**Principle**: Show source diversity structure, not just article count.

**Coverage Map Dimensions**:
- **Local reporting**: Sources from the event's location/country
- **International reporting**: Sources from other regions covering the event
- **Wire/public-service reporting**: News agencies and public broadcasters
- **Specialist coverage**: Domain experts, primary sources, investigative teams

**Rules**:
- Event cards display coverage structure, not just "N sources"
- Show which dimensions are present vs missing
- Distinguish between comprehensive coverage and single-dimension coverage
- Make coverage composition visible at a glance

**Current Implementation**:
- ✅ Event cards show source count ("4 sources")
- ✅ Country/language shown in comparison screen
- ❌ No coverage map visualization
- ❌ No local vs international distinction
- ❌ No wire/specialist identification

**Future Work**:
- Add coverage map visualization to event cards
- Tag sources as local/international/wire/specialist
- Show icons or badges for coverage dimensions present
- Example: "🌍 3 international • 📡 1 wire • 📰 0 local"

---

### 3. Show Coverage Gaps Plainly

**Principle**: Make absence of coverage visible, not just presence.

**Gap Types**:
- **Geographic**: "No local reporting found yet"
- **Language**: "Only English-language coverage"
- **Publisher diversity**: "One publisher so far"
- **Source type**: "No wire service coverage"
- **Temporal**: "Coverage from last 24 hours only"

**Rules**:
- Display gap notices on event cards and comparison screens
- Use neutral, factual language (not alarmist)
- Update gap notices as coverage expands
- Show "Coverage may be incomplete" when appropriate

**Current Implementation**:
- ❌ No gap notices on event cards
- ❌ No "Only N sources" warnings
- ❌ No language coverage warnings
- ⚠️ Single-source events show "1 sources" (implicit gap, not explicit)

**Future Work**:
- Add gap detection logic based on event type and expected coverage
- Display gap notices: "⚠️ Only English-language coverage"
- Show temporal gap: "No coverage older than 24h"
- Flag single-publisher events: "Single source - coverage may be incomplete"

---

### 4. "Read Across Coverage" Action

**Principle**: Deliberate exposure to different contexts, not algorithmic selection.

**Rules**:
- Provide explicit "Read across coverage" action on events
- Select sources from **different** regions, languages, ownership structures
- Prioritize maximum diversity in selection
- Explain why each source was selected: "Local reporting from...", "International perspective from...", "Public broadcaster coverage from..."
- User controls selection, not personalization algorithm

**Current Implementation**:
- ✅ Event comparison screen shows all sources
- ✅ Each source attributed with country/language
- ❌ No "Read across coverage" shortcut
- ❌ No diversity-optimized selection for quick reading
- ❌ No explanation of why sources are grouped together

**Future Work**:
- Add "Read Across Coverage" button on event cards
- Implement diversity-maximizing selection algorithm (different regions/languages/types)
- Show 3-4 sources with explicit labels: "🇮🇪 Local: Irish Times", "🌍 International: BBC", "📡 Wire: Al Jazeera"
- Add explanation text: "Selected for geographic and editorial diversity"

---

### 5. No Optimization for Clicks, Outrage, or Prior Preferences

**Principle**: Quality over engagement. Informed exposure over personalization.

**Prohibited**:
- Ranking events by predicted engagement
- Amplifying controversial or polarizing content
- Using prior reading history to filter future content
- A/B testing for time-on-site or session length
- Clickbait headlines or sensational framing

**Permitted**:
- Ranking by recency and confidence (objective metrics)
- Showing events with high source diversity (coverage quality)
- Filtering by explicit user-selected interests (transparent, opt-in)
- Measuring diversity and completion (see Metrics section)

**Current Implementation**:
- ✅ No engagement optimization
- ✅ No personalization based on reading history
- ✅ No clickbait amplification
- ✅ Clustering based on objective similarity, not controversy
- ✅ Ranking by recency and source count (not engagement)

**Future Work**:
- Explicitly document anti-engagement-optimization in architecture
- Add diversity score to ranking (favor well-covered events)
- Monitor for accidental engagement patterns in analytics

---

### 6. Explain Why Each Article Appears

**Principle**: Transparency in grouping and selection logic.

**Explanation Types**:
- **Same event**: "Grouped with N other articles reporting on the same event"
- **Different publisher**: "From [Publisher] ([Country]) - different from [Other Publisher]"
- **Local context**: "Local reporting from event location"
- **International perspective**: "International coverage from [Region]"
- **Wire/specialist**: "Wire service coverage" / "Investigative reporting"
- **Missing perspective**: "No coverage yet from [Region/Type]"

**Rules**:
- Every grouped article shows grouping reason
- Event comparison screen explains clustering logic
- Coverage gaps explicitly noted
- No hidden algorithmic decisions

**Current Implementation**:
- ✅ Event comparison footer explains clustering
- ✅ Publisher attribution with country/language shown
- ❌ No per-article explanation of why it's included
- ❌ No labels for local vs international vs wire
- ❌ No explicit "different publisher" callouts

**Future Work**:
- Add explanation text above each article in comparison view
- Tag articles: "🌍 International perspective", "📍 Local reporting", "📡 Wire coverage"
- Show diversity rationale: "Included for geographic diversity"
- Explain single-source events: "Only source covering this event so far"

---

### 7. Apply Source Standards Consistently

**Principle**: Quality bar applies to all sources equally, regardless of region or viewpoint.

**Source Requirements**:
- **Attribution**: Clear, verifiable publisher identity
- **Corrections policy**: Public, documented process for fixing errors
- **Reporting vs commentary**: Clear separation in labeling and content
- **Verifiable identity**: Documented ownership and editorial structure

**Disqualifiers**:
- Anonymous publishers or unclear ownership
- No corrections policy or process
- Mixing reporting and commentary without labels
- Unverifiable or fabricated publisher identity
- Systematic misinformation or fabricated content

**Rules**:
- All sources meet same quality bar (no regional exceptions)
- Sources failing standards are excluded, not downranked
- Source metadata shows ownership, corrections policy, editorial description
- No ideological labels, only factual descriptors

**Current Implementation**:
- ✅ SourceMetadata includes ownership, editorial description, provenance
- ✅ No ideological labels (country, language, ownership only)
- ✅ RSS feed quality requirements (HTTPS, valid dates, attribution)
- ⚠️ No explicit corrections policy tracking
- ❌ No reporting vs commentary distinction yet
- ❌ No systematic source vetting beyond RSS technical requirements

**Future Work**:
- Add corrections policy to SourceMetadata
- Tag articles as "reporting" vs "opinion/analysis" where distinguishable
- Build source vetting process with consistent quality criteria
- Document source standards publicly
- Add "Report source quality issue" mechanism

---

### 8. Treat Unverified Claims as Claims with Attribution

**Principle**: No false balance. Attribution required. Credibility context visible.

**Rules**:
- Unverified claims must be attributed to source: "[Publisher] reports [claim]"
- Do not pair credible reporting with unsupported claims as equal perspectives
- Show when claims lack verification: "Unverified claim from [Source]"
- Do not group verified reporting with unverified claims as "different perspectives"
- Distinguish between:
  - Multiple credible sources reporting same facts (event clustering)
  - Multiple sources making conflicting claims (disputed claims, needs labeling)
  - One credible source vs one making unverified claims (not equal weight)

**Current Implementation**:
- ✅ All articles attributed to publisher with metadata
- ✅ Original headlines preserved (no editorializing)
- ✅ Safety footer: "We do not claim any article is more accurate, truthful, or biased"
- ❌ No verification status shown
- ❌ No distinction between verified reporting vs claims
- ❌ No disputed claim flagging

**Future Work**:
- Add "Unverified claim" labels when appropriate
- Distinguish "Multiple sources report X" (consensus) from "Source A claims X, Source B denies" (dispute)
- Add "Disputed" flag for conflicting claims between sources
- Show when coverage is verification vs claim vs analysis
- Never present unverified claims as equal to credible reporting

---

### 9. Measure Product Health with Diversity and Completion Metrics

**Principle**: Success is informed exposure, not engagement.

**Primary Metrics**:
- **Distinct publishers per event**: How many independent sources cover major events?
- **Languages represented**: Is coverage available in multiple languages?
- **Local-source presence**: Do events have local reporting from event location?
- **Cross-source reading**: How often do users open more than one source?
- **Coverage-map completeness**: % of events with local + international + wire coverage
- **Gap visibility**: % of incomplete-coverage events with gap notices shown

**Secondary Metrics** (for health, not optimization):
- Time spent reading original sources (not our platform)
- Diversity of sources opened per user per week
- % of users who use "Read across coverage" feature
- % of events with 3+ distinct publishers within 72 hours

**Anti-Metrics** (never optimize for):
- Total time spent in app
- Click-through rate on controversial content
- Return rate after outrage-inducing content
- Personalization accuracy (matching user preferences)

**Current Implementation**:
- ✅ Track publisher count per event (clustering data)
- ❌ No language diversity metrics
- ❌ No local-source presence tracking
- ❌ No cross-source reading metrics
- ❌ No coverage completeness scoring
- ❌ No analytics on diversity vs engagement

**Future Work**:
- Add analytics for diversity metrics
- Track cross-source reading behavior
- Measure coverage-map completeness
- Monitor gap notice visibility
- Build dashboard for product health metrics (diversity, not engagement)
- A/B test features for diversity improvement, not time spent

---

## Feature Decision Framework

When considering a new feature, ask:

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
   - Is source selection logic clear?

5. **Does it measure the right things?**
   - Does it track diversity and completion?
   - Does it avoid optimizing for time spent?
   - Does it help assess coverage quality?

**If a feature fails any of these tests, it should not be built.**

---

## Current Implementation Alignment

### Strong Alignment ✅
- No engagement optimization
- No personalization based on preferences
- Conservative clustering (avoids false grouping)
- Clear publisher attribution with country/language
- Source metadata with ownership, not ideology
- Safety-compliant language (no truth/bias claims)

### Partial Alignment ⚠️
- Source count shown, but not coverage diversity structure
- Gap notices implicit (single-source events), not explicit
- Comparison screen shows all sources, but no diversity-optimized selection
- No reporting vs commentary distinction yet

### Needs Development ❌
- Coverage map visualization (local/international/wire/specialist)
- Explicit gap notices ("Only English-language coverage")
- "Read across coverage" diversity action
- Explanation of why each article appears in an event
- Verification status and disputed claim flagging
- Diversity and completion metrics
- Source vetting with consistent standards (corrections policy, reporting separation)

---

## Design Philosophy

**CrossLens is not**:
- A personalized news feed
- An engagement optimization platform
- A truth arbiter or fact-checker
- A balanced debate platform (false balance)

**CrossLens is**:
- A coverage structure visualizer
- A diversity exposure tool
- A gap visibility system
- An informed-reading enabler

**Success looks like**:
- Users reading across multiple sources before forming opinions
- Users seeing when coverage is incomplete
- Events having diverse, attributable reporting from multiple contexts
- Users understanding the structure of available coverage

**Failure looks like**:
- Users staying in personalized bubbles
- Engagement optimization driving content selection
- False balance between verified and unverified claims
- Coverage gaps hidden or ignored
- Users optimizing for confirmation, not information

---

## Review and Evolution

These principles should guide every product decision. They should be:
- Referenced in design reviews
- Used to evaluate feature proposals
- Applied to existing features for alignment audits
- Updated when learning requires principle evolution

When principles conflict with engagement metrics, **principles win**.

When user requests conflict with informed exposure, **explain the principle and hold the line**.

When a feature seems valuable but violates a principle, **redesign the feature or reject it**.

Product quality is measured by informed exposure, not popularity.

---

**Last Updated**: 2026-09-25  
**Status**: Foundational principles established, implementation alignment audit complete  
**Next Review**: After each major feature release
