# CrossLens API

Cloudflare Worker + D1 foundation for public, source-linked event data.

It intentionally exposes only read-only endpoints. An event must be marked `is_public = 1` only after it has at least two attributed article links and editorial review.

## Endpoints

- `GET /v1/health`
- `GET /v1/sources`
- `GET /v1/events`
- `GET /v1/events/{slug}`
- `GET /v1/sitemap.xml`

## First deployment

1. Install Wrangler and authenticate to the CrossLens Cloudflare account.
2. Run `npx wrangler d1 create crosslens` and replace the placeholder database id in `wrangler.jsonc`.
3. Apply `npx wrangler d1 migrations apply crosslens --remote`.
4. Deploy `npx wrangler deploy`.
5. Attach `api.crosslens.space` only after endpoint checks pass.

The cron schedule is intentionally a no-op until the RSS ingestion adapter and source approvals are completed; it does not fetch or publish unreviewed material.
