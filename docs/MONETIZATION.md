# CrossLens access model

CrossLens has a useful free tier and an optional **CrossLens Plus** upgrade. The free tier must never feel like a broken preview: readers can discover stories, read the neutral event overview, save stories, use basic Explore filters, and compare a limited selection of coverage. Plus unlocks the complete comparison experience for readers who want deeper context.

## What each tier includes

| Experience | Free | CrossLens Plus |
| --- | --- | --- |
| Home, Story, saving, Continue reading, and basic region/topic exploration | Included | Included |
| Event overview, claims, and source attribution | Included | Included |
| Source comparison | First two source panes per story | All available source panes |
| Translations | Original text and one available translated article per story | Available translations for every included source |
| Lens Gap | Plain-language availability label and a short explanation preview | Full sample, components, limitations, and source-level evidence |
| Discovery | Latest and coverage-breadth sorts | Demo Lens Gap sort and advanced language/country/source filters |

The exact pricing, trial, billing period, and regional offers are intentionally undecided. Do not show invented prices, discount deadlines, subscriber counts, or “best value” claims in the skeleton.

## Paywall behavior

Use an attractive, calm modal bottom sheet or full-screen sheet that matches the editorial design. It is reached only after a reader intentionally selects a Plus feature. It must state the benefit tied to their current action—for example, “Compare all 5 sources for this story”—and keep the free content behind it readable and reachable on dismissal.

In production, the sheet includes Plus benefits, “Maybe later,” “Restore purchases,” a link to subscription terms/privacy, and an upgrade action. In the skeleton, replace those purchase-specific controls with an explicitly local “Preview Plus in this demo” action and a short statement that purchases are unavailable. It must not block navigation, trap back behavior, obscure the only route to a story summary, use a countdown, or force an upgrade at launch. Never place a paywall in front of claims, source attribution, the original article link, or error/retry states.

Show a small lock and a concise “Plus” label where an unavailable upgrade feature is introduced. The button label should be action-specific—such as “Unlock full comparison”—rather than vague persuasion. If a reader dismisses the sheet, retain their free state without nagging them again during that same route visit.

## Skeleton implementation

The initial Android skeleton implements the visual and state behavior with a local mock entitlement only. It does not integrate Google Play Billing, process money, store payment data, make a network request, or claim that a simulated upgrade is a purchase.

- Add `EntitlementRepository` and an `AccessTier` (`FREE`, `PLUS_DEMO`) domain model. Persist the chosen demo tier in DataStore and inject it through Hilt.
- In debug/mock builds, offer “Preview Plus in this demo” from the paywall and Settings. It changes the local `PLUS_DEMO` entitlement and can be reset to Free. Label it as a preview wherever its status is shown.
- Route gated actions through one reusable access check. Do not duplicate feature gates inside composables or rely on a hidden UI element as authorization.
- Define a future `BillingGateway` interface with no implementation invoked in the skeleton. A production implementation later validates purchase state using Google Play Billing and a backend where appropriate; never treat a client-side flag as a real paid entitlement.
- Keep all free and Plus fixture data local so switching tiers changes access presentation without re-seeding, fetching, or corrupting saved state.

## Future production requirements

Before charging users, choose pricing and billing terms, complete the relevant Google Play policy and legal review, implement purchase/restore/error/pending/cancel flows, verify entitlement changes across reinstall and account/device changes, and add a server-side entitlement strategy appropriate to the business model. Display price and renewal information only from the live billing product details for the reader’s locale.

Treat subscription access as a security boundary. Test that direct navigation, configuration changes, process restart, source selection changes, and stale cached UI cannot reveal Plus-only content while the active entitlement is Free. Continue to provide access to already-visible free content when billing is unavailable, and make purchase failures recoverable without losing reading state.
