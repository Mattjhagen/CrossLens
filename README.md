# CrossLens

**One event. A world of perspectives.**

CrossLens is an Android news app for understanding how the same event is reported across countries, languages, and institutions. Readers will explore clustered stories, compare source coverage, and read translations while retaining a clear path to the original reporting.

The goal is informed comparison: show where reporting overlaps, where emphasis differs, and what evidence supports those observations. A source's country is context, not a proxy for its politics or the views of an entire population.

> **Project status:** Planning and build instructions only. The Android app has not been implemented yet. The first milestone is a working, offline, mock-data Android skeleton; live sources, translation services, and automated analysis come later.

## Start building

Start with [the step-by-step Claude CLI runbook](docs/CLAUDE_BUILD_RUNBOOK.md) to build, verify, audit, fix findings, and prepare the handoff. It includes copy-and-paste prompts, quality gates, a device checklist, and required evidence.

Use [the Claude CLI build guide](docs/ANDROID_BUILD_GUIDE.md) for prerequisites, a copy-and-paste kickoff prompt, implementation phases, and completion checks. [CLAUDE.md](CLAUDE.md) provides the repository instructions for Claude Code.

```sh
git clone https://github.com/Mattjhagen/CrossLens.git
cd CrossLens
claude "Read CLAUDE.md, README.md, and docs/ANDROID_BUILD_GUIDE.md. Implement the initial Android skeleton through all build phases and verify the acceptance checklist."
```

Claude Code and the Android development tools must be installed first. The guide explains setup. There is no Gradle wrapper or runnable APK in this documentation-only starting point.

## Product principles

- **Global by design:** organize coverage by source, country, region, language, topic, and institutional context.
- **Evidence stays visible:** link summaries and comparison observations to the articles supporting them; distinguish reported claims from established facts.
- **Translation with provenance:** preserve original text, label translated text, and disclose translation availability and method.
- **Multiple dimensions of perspective:** geographic, political, and institutional context can coexist. Avoid a universal left/center/right classification.
- **Explain the comparison:** readers should understand the reasons behind a future Lens Gap result and its limitations.

## Design is a core feature

**CrossLens should be gorgeous and make readers want to return.** The visual direction takes inspiration from the owner's love of Flipboard's editorial feel and tactile browsing, expressed through an original CrossLens identity: expressive typography, generous imagery, varied magazine compositions, warm surfaces, and restrained perspective-switch motion.

The first skeleton must already feel thoughtfully designed. Material 3 is the foundation for a custom visual system, and every screen—including empty states and Settings—gets the same care. Subtle flip-inspired transitions should support comparing sources, with accessible controls and reduced-motion alternatives.

Build reasons to return from the start: a finite mock edition, locally saved stories, a continue-reading shortcut, and useful invitations to explore another perspective. Live briefings, followed topics, and meaningful update notifications come later. Return visits should come from beauty, trust, and discovery.

Read [the design direction and visual acceptance gate](docs/DESIGN_DIRECTION.md) alongside the build guide. It defines screen composition, motion, original identity, and the visual evidence required at handoff.

## The first five screens

| Screen | Initial skeleton experience |
| --- | --- |
| **Home** | Clustered mock stories with title, summary, topic, countries, source count, timestamp, and clearly labeled demo comparison data. Sort by latest, coverage breadth, or demo Lens Gap. |
| **Story** | Event overview, attributed claims, sources, original article links, and a route into comparison. Distinguish disputed claims and missing evidence. |
| **CrossLens** | Compare selected source coverage of one event: original and mock-translated headlines/excerpts, framing observations, and supporting references. Stack cards on narrow screens. |
| **Explore** | Filter mock stories by region, country, topic, and original article language; combine filters and clear them. |
| **Settings** | Persist preferred reading language, home country/region, source selections, translation display preference, and theme. |

Home, Explore, and Settings are top-level destinations. Story and CrossLens are detail destinations addressed by stable story IDs, with predictable back navigation.

## Technical foundation

| Area | Choice |
| --- | --- |
| Platform | Native Android; Kotlin; minimum Android 10 / API 29 |
| UI | Jetpack Compose, Material 3, Navigation Compose |
| Presentation | MVVM, immutable UI state, Coroutines/Flow, lifecycle-aware state collection |
| Dependency injection | Hilt |
| Networking boundary | Retrofit + OkHttp; defined for a future backend, inactive in mock mode |
| Local storage | Room for seeded stories and source data; DataStore for user preferences |
| Build | Gradle Kotlin DSL, version catalog, pinned compatible stable dependencies |

Start with one `app` module and clear package boundaries. Select and document a compatible JDK, Gradle, Android Gradle Plugin, Kotlin, Compose, Hilt, and KSP toolchain during implementation. Set compile/target SDK to a stable supported API appropriate to that toolchain; do not confuse these values with the minimum API.

```text
Compose screen → ViewModel → repository interface
                                   ↓
                          mock repository → Room → bundled fixtures
                                   ↓ later
                          live repository → Retrofit/OkHttp → backend
```

Room supplies observable local data. ViewModels never depend on fixture files, Retrofit DTOs, or database entities. Dependency injection chooses the implementation, so future ingestion and translation services can replace mocks without rewriting screens.

## Data model: events, reporting, and interpretation

These are proposed domain contracts, not existing classes. Use stable IDs, UTC instants, ISO country codes, and BCP 47 language tags. Regions are explicit groupings; sources may cover multiple countries and articles may use a different language from their publisher's default.

| Model | Essential fields and relationships |
| --- | --- |
| `Story` | ID, title, event summary, event time (optional), updated time, topic IDs, event country codes, article IDs, claim IDs; optional Lens Gap assessment |
| `Source` | ID, name, homepage, country codes, region IDs, default languages, optional ownership/funding and editorial-context records with citations and review dates |
| `Article` | ID, story ID, source ID, original URL, published time, original language, original headline and excerpt; attribution and content-use metadata |
| `Translation` | ID, article ID, source/target language, translated headline/excerpt, status (`NOT_REQUESTED`, `PENDING`, `AVAILABLE`, `FAILED`, `UNAVAILABLE`), method/provider, generation time, original-content revision |
| `Claim` | ID, story ID, statement, assessment (`REPORTED`, `CORROBORATED`, `DISPUTED`, `UNASSESSED`), supporting and contradicting article references, assessment provenance |
| `FrameObservation` | ID, story ID, article IDs, emphasized actors/claims, language or sentiment observations, evidence references, method/version and confidence; optional omission hypothesis with comparison scope |
| `Perspective` | ID, scope/article IDs, dimension (`GEOGRAPHIC`, `POLITICAL`, `INSTITUTIONAL`), descriptive label, evidence references, attribution and uncertainty |
| `LensGapAssessment` | Story ID, status, nullable score, component explanations, source/article sample IDs, coverage window, confidence/limitations, method version, generated time, `isDemo` |
| `UserPreferences` | Reading language, home country/region, enabled source IDs, translation preference, theme, reduced-motion preference |
| `ReadingState` | Locally persisted saved story IDs and last-opened story ID; independent of seeded content |

Keep original and translated content separate. Missing translations fall back to the original with a visible explanation. A corroborated claim still needs evidence; an apparent omission means “not found in this analyzed sample,” not proof of intentional suppression. Keep event location distinct from the country of a reporting source.

## Future signature feature: Lens Gap

**Lens Gap asks: how differently is this event being covered in the available reporting?**

A future assessment may describe differences in emphasized claims, selected actors, terminology, and the distribution of attention across sources. Each result should reveal its sampled articles, geographic/language coverage, time window, explanatory components, and concrete evidence. Broad regional descriptions must identify the sampled publications and avoid implying a single regional viewpoint.

A possible future display is **“Lens Gap: 72/100 — divergent emphasis.”** This is a proposed index, not a probability, factuality score, or claim that 72% of reporting is biased. The scoring method, thresholds, confidence calibration, and minimum evidence requirements remain research work.

The skeleton uses deterministic fixture assessments labeled **“Demo Lens Gap — illustrative.”** It implements no scoring algorithm. Stories without sufficient comparison data show **“Not enough coverage”** with a null score, never a misleading zero. Demo sorting places unavailable scores last and explains that scores are simulated.

Before real ranking ships, evaluate translation artifacts, duplicate/syndicated reporting, uneven source sampling, time-window effects, and uncertainty. More articles from the same syndicated report must not masquerade as independent agreement. Readers should be able to inspect the explanation and report a misleading comparison.

## Roadmap

1. **Android skeleton:** five screens, local fixtures, offline persistence, preferences, navigation, and meaningful tests.
2. **Source ingestion:** backend API, source catalog, attribution/content permissions, article normalization, event clustering, and deduplication.
3. **Translations:** provider abstraction, cache/version handling, original-text access, explicit failure states, and multilingual/RTL validation.
4. **Explainable comparisons:** evidence-backed frame observations and a human-reviewed Lens Gap evaluation set before production scoring.
5. **Discovery experiments:** evaluate coverage breadth, cross-country differences, and “outside my usual sources” sorting with transparent controls.

Accounts, subscriptions, scraping, production backend deployment, live translation, and automated political labels are outside the initial build.

## Development references

The implementation should follow [Android architecture recommendations](https://developer.android.com/topic/architecture/recommendations) for observable UI state and unidirectional data flow. See the [Claude Code CLI reference](https://code.claude.com/docs/en/cli-reference) for starting and continuing a build session.
