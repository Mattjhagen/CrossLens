# Next session to-do: V-0.0.5 stabilization

Start by inspecting the current working tree, latest commits, release-note
drafts, `CLAUDE.md`, `README.md`, and `docs/DESIGN_DIRECTION.md`. Do not mark a
visual requirement complete from a written summary alone.

## 1. Finish and verify V-0.0.5 visual branding

The Android launcher icon was visually checked on a physical Pixel and passes:
it is centered, readable, unclipped, and has no visible white border.

The in-app brand system needs an honest visual review and, if necessary,
correction.

- Home has a large text-only CrossLens masthead, tagline, and “Mock Edition ·
  Demo.” The intended crossing-perspectives motif was not visible in physical
  Pixel testing.
- Comparison shows CrossLens in the top app bar. The claimed small wordmark or
  motif near the Framing section was not visible in physical Pixel testing.
- Explore's no-results state is useful and uncluttered, but it did not visibly
  show the claimed branded empty-state motif.

Choose one coherent result:

1. Add the subtle, theme-aware crossing-perspectives motif where it is actually
   visible, readable, and secondary to content; or
2. Keep the restrained text-only experience and remove unsupported claims from
   release notes and documentation.

Do not add decoration merely to satisfy a checklist. The Home masthead should
feel editorial, story cards must remain primary, and branding must adapt to
Light, Dark, and System themes.

## 2. Repeat physical Pixel acceptance

Build a fresh V-0.0.5 APK and record the results of these checks:

1. Launcher icon: centered, not clipped, no white border.
2. Home: masthead is intentional and the branding matches the active theme.
3. Light, Dark, and System: theme-aware branding updates correctly.
4. Comparison: source name, original text, source navigation controls, framing
   evidence, and any new branding are readable and unclipped.
5. Explore: create a no-results state with filters, then use **Clear all** and
   confirm stories return.
6. Editorial Review: Approve, Reject, Defer, notes, and Reset demo reviews
   continue to work.
7. Force-close/reopen: theme preference and local editorial review decisions
   persist.
8. TalkBack: decorative branding is silent and interactive controls have useful
   labels.

Capture only the screenshots needed to document real results. A device check
that cannot run must be documented as not run.

## 3. Complete V-0.0.5 release preparation

- Run the full unit suite, lint, and `assembleDebug`.
- Update V-0.0.5 release notes using only verified behavior.
- Include the debug APK path and physical-device verification results.
- Commit and push the completed work to `main`.
- Do not publish the GitHub release until the Pixel acceptance pass is complete.

## 4. Keep future product ideas out of this stabilization task

`docs/PRODUCT_IDEAS.md` tracks reader personalization, relevant notifications,
local reporting, source access, AI summaries, pull to refresh, and
AI-assisted editorial review. Do not implement those ideas during this V-0.0.5
stabilization work.

Keep the app offline, mock-data-only, and clear about its demo boundaries.

## Final report

Report the changed files, commit hash, test/lint/build results, APK path,
Pixel checks that passed/failed/were not run, and remaining limitations.
