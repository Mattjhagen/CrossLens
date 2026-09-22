# CrossLens design direction

## The standard

**Make CrossLens gorgeous, tactile, and worth returning to.** Visual quality is part of the first milestone, not a layer to add after the skeleton works. The first launch should feel like opening a thoughtfully edited world magazine: a striking lead story, beautifully composed typography, room to breathe, and an immediate invitation to see another perspective.

The owner's reference is their affection for Flipboard's look and sense of flipping through an editorial publication. Translate that preference into general qualities—editorial hierarchy, immersive imagery, composed pages, and tactile transitions—while creating CrossLens's own identity. Do not reproduce Flipboard's branding, logo, exact layouts, assets, or signature animation sequence. This brief is an original direction, not a specification of Flipboard's current UI.

## Visual language

- **Editorial typography:** pair a distinctive, readable serif display face with a clean sans-serif for controls, metadata, and body copy. Bundle appropriately licensed fonts or use suitable platform fallbacks. Give headlines hierarchy without crowding translations or long international names.
- **Quiet, confident color:** begin with warm ivory surfaces, near-black ink, and a restrained deep teal accent; dark mode uses charcoal surfaces and warm light text. Treat these as a coherent starting palette, then validate contrast. Never use accent color as an implied political judgment.
- **Composed layouts:** alternate a generous lead story with compact supporting stories and occasional typographic features. Use deliberate spacing, fine rules, restrained corners, and minimal elevation. Material 3 provides accessible foundations; customize its tokens and components to create a recognizable CrossLens personality.
- **Purposeful imagery:** use a small bundled set of original or appropriately licensed illustrations/photos with attribution recorded. Clearly identify illustrative imagery for fictional news; avoid implying that an image documents the fictional event. Provide an equally polished text-only composition. Never rely on remote image downloads to make the demo attractive.
- **A CrossLens signature:** use two offset rectangular panes and a subtle crossing rule as an original perspective motif. Repeat it sparingly in the comparison entry point, source transitions, and section headings. Country/source labels remain readable text rather than a wall of flags or badges.
- **Editorial restraint:** show the headline, essential context, and a clear “Compare perspectives” action first. Reveal detailed evidence and methodology progressively. Lens Gap belongs alongside the explanation, not as an oversized sensational score.

Define reusable color, type, spacing, shape, image-ratio, and motion tokens before styling every screen. Build shared lead-story, supporting-story, source-pane, section-heading, and comparison-entry components. Avoid an endless stack of identical default cards.

## Screen composition

| Screen | Required visual treatment |
| --- | --- |
| Home | A distinctive CrossLens masthead, a labeled mock edition, one lead story, varied supporting compositions, and a finite edition ending. Keep sort controls easy to find without overwhelming the cover. The lead is the first result in the active sort/filter, not a hidden ranking override. |
| Story | A spacious headline and standfirst, an illustration or intentional text-only opening, readable article measure, and a prominent comparison invitation. Claims and source details form clear editorial sections. |
| CrossLens | Two clearly attributed source panes with aligned context, expressive but restrained transitions, and an easy way to inspect the evidence. Stack on small screens; never squeeze columns until text becomes unreadable. |
| Explore | Attractive region/topic entry points followed by visible, removable filters and composed results. Make exploring an unfamiliar part of the world inviting. |
| Settings | Calm, well-spaced groups and excellent typography consistent with the reading experience. Preferences should feel as finished as the home screen. |

## Flip-inspired motion, with its own character

Implement a small perspective-switch transition in the comparison screen: the selected pane gently tilts and slides into place with a brief change in depth. Start around 180–280 ms and tune on a device. Keep the text legible, movement modest, and input responsive. Use Compose animation primitives; a custom page-curl engine is outside this milestone.

Provide visible previous/next source controls and a source selector. A swipe can be a progressive enhancement, never the only way to navigate. Keep vertical reading natural, preserve Android back behavior, and avoid gesture conflicts with horizontal controls. Do not turn every screen change into a flip.

Respect the system's disabled-animation behavior and provide an in-app reduced-motion option. In reduced-motion mode, switch panes directly or with a minimal fade, without rotation or parallax. Verify both paths, cancellation under rapid input, and state preservation. Animation must never delay access to evidence.

## Reasons to return

Earn repeat visits through understanding, continuity, and discovery. The reader should leave feeling better informed and remember where to pick up next.

**Implement in the mock skeleton:**

- A finite, explicitly labeled mock edition with a satisfying “You're caught up with this demo edition” ending. Do not fabricate freshness, live activity, or daily updates.
- Save/unsave stories from Home and Story, plus a Saved filter within Home. Persist saved IDs locally and provide an inviting empty state. Keep source preferences in force; explain when they hide saved stories and offer a settings route.
- A “Continue reading” shortcut to the last opened story, persisted locally. If that story is missing or excluded by source preferences, hide the shortcut gracefully. This only resumes the story; exact scroll-position restoration is optional.
- A clear, evidence-backed teaser such as “Compare coverage from 3 countries,” derived from the visible fixture articles. Invite curiosity without inventing disagreement or promising more than the comparison contains.

**Later, when real data exists:** followed stories/topics, meaningful “what changed since your last visit” summaries, user-controlled briefings, and optional notifications for substantive updates. These require real update tracking and are not part of the initial skeleton.

Do not introduce streak pressure, fake urgency, random rewards, automatic notification prompts, or endless scroll merely to increase time spent. The product aim is voluntary return because the experience is beautiful and useful. Evaluate that hypothesis with readers later; visual polish alone does not establish retention.

## Design acceptance gate

A successful compile is necessary but insufficient. Before calling the skeleton complete:

- Capture and inspect all five screens in light and dark mode, including a compact phone, large text, long headlines, and RTL article content. Save representative screenshots under `docs/screenshots/` and reference them from the README. If capture is blocked, report the visual review as incomplete.
- Confirm a distinctive masthead, strong headline hierarchy, varied story compositions, coherent spacing, and polished no-image/loading/empty/error states. No accidental truncation, overlapping controls, or placeholder gray boxes as the final design.
- Verify bundled imagery/fonts work offline and record their licenses/attributions in `docs/ASSET_CREDITS.md` during implementation.
- Confirm readable contrast, accessible names, generous touch targets, and complete navigation without gestures or animation.
- Exercise the pane transition and reduced-motion path on an emulator/device; report visible stutter or untested performance rather than claiming smoothness without observation.
- Verify saved stories and the continue-reading shortcut survive process restart, and that edition labels and freshness claims remain honest.

Deliver a small, polished editorial product. Functional placeholders are acceptable during construction, not as the finished milestone.
