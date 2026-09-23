# Product ideas backlog

This is a living backlog for CrossLens. Items here are product directions, not
implemented behavior or a commitment to build them. Add ideas in the template
at the end of this document so their purpose, user benefit, and safeguards are
clear before they enter the roadmap.

## Personalization with reader control

### Explicit article feedback

Add **More like this** and **Less like this** actions below eligible articles.
These are explicit personal-preference signals and should be more important
than passive signals such as a click or reading time.

- “More like this” may raise related topics, places, entities, sources, and
  formats in that reader's recommendations.
- “Less like this” should reduce similar recommendations without hiding
  important public-interest coverage or changing the editorial evidence shown.
- Offer Undo immediately after an action, and let readers view, reset, or turn
  off personalization in Settings.
- Explain how a recommendation relates to a reader's selected preferences.

### Privacy and editorial boundary

Future personalization may use opted-in reading behavior such as article
opens, reading duration, saves, follows, and notification interactions. It
must remain separate from editorial review and evidence analysis.

Personalization can decide what to surface to one reader. It must not decide
what is true, alter the evidence shown, label an outlet politically, or make
an automated editorial decision.

Before implementation, define account consent, data retention, export/delete
controls, security review, evaluation criteria, and a way to disable all
personalization.

## Relevant notifications

When accounts and notifications are introduced, use a reader's opted-in
interests to prioritize useful alerts. Examples include followed topics,
regions, languages, and recurring events.

- Respect notification permission, frequency controls, quiet hours, and a
  complete opt-out.
- Prefer high-confidence relevance over engagement maximization.
- Explain why an alert was sent, for example: “You follow climate policy.”
- Do not use engagement alone to escalate emotionally charged stories.

## Local reporting

Add permissioned local sources alongside national and global coverage.

- Let readers choose a city, region, or local topics themselves; do not require
  precise location tracking.
- Add every source through the source registry with explicit content-use,
  attribution, and review requirements.
- Identify local reporting clearly while preserving the same attribution,
  duplicate/syndication detection, and evidence standards used for every other
  source.
- A source's geography provides context; it is not a political label.

## Reading and source access

### Full source articles

Let readers open the original source article from its attribution card. When
appropriate, offer an in-app browser so the reader can return to CrossLens
without losing their place in an event comparison.

- Preserve the original publisher, URL, headline, language, and attribution.
- Before opening a source likely to have a paywall or subscription requirement,
  show a brief notice that access may be limited by the publisher.
- Do not bypass publisher access controls, subscription requirements, or source
  terms of use.
- Always provide a clear way to open the article in the reader's preferred
  external browser.

### AI source digest

Explore an AI-generated summary that compares the available sources for one
event. The digest should link each observation to its supporting source
articles and make the difference between reporting, interpretation, and an
unknown clear.

- Show the source sample, coverage window, original-language availability, and
  important gaps.
- Label it as an AI-generated draft or summary with method/version and
  uncertainty information.
- Never substitute the digest for the original source articles or present it as
  a factual verdict.
- Production work requires permission-aware content handling, evaluation for
  hallucinations and translation errors, and editorial governance.

### Pull to refresh

Add pull to refresh to supported reading and discovery feeds.

- In the current offline demo, refresh should explain that it restores or
  reloads the bundled fictional edition without making a network request.
- In a future live product, display the last-updated time and a useful loading,
  success, empty, and error state.
- Do not imply that a refresh fetched live reporting until live ingestion is
  actually connected and authorized.

## Future AI-assisted review

AI may eventually triage ingestion candidates and recommend an editorial
decision with inspectable evidence. It should escalate uncertain,
high-impact, or conflicting cases for review and support quality sampling of
automated decisions.

Do not treat an AI recommendation as verification. Record the model/version,
inputs, evidence, confidence, and final outcome for every automated action.
Production work requires a separate evaluation set, error analysis, privacy
review, and editorial governance design.

## Add a new idea

```md
### Idea title

**Problem:** What reader or editor problem does this solve?

**Proposed experience:** What would someone see or do?

**Expected benefit:** What improves if this works?

**Editorial, privacy, or safety boundary:** What must this feature never do?

**Open questions:** What needs research, a product decision, or a test?
```
