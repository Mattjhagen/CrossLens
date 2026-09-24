# CrossLens web-edition catalog

`catalog.json` is the single content input for CrossLens' static web edition.

Run this after changing a source, topic, or eligible event:

```sh
node scripts/build-web-edition.mjs
```

The generator rebuilds public source, topic, and story pages and writes `sitemap.xml` from the same data. It rejects duplicate slugs. A story must contain at least two original publisher links before it can generate a public page.

## Story contract

Add a story only after the ingestion and editorial systems have produced a reviewable, source-linked cluster:

```json
{
  "slug": "descriptive-event-slug",
  "title": "Clear event title",
  "summary": "Original CrossLens summary of the available reporting sample.",
  "publishedAt": "2026-09-24T18:00:00Z",
  "updatedAt": "2026-09-24T20:00:00Z",
  "sources": [
    {
      "publisher": "Publisher name",
      "headline": "Original linked headline",
      "url": "https://publisher.example/original-article"
    },
    {
      "publisher": "Another publisher",
      "headline": "Original linked headline",
      "url": "https://publisher.example/original-article"
    }
  ]
}
```

Do not use demo fixtures, reproduce full publisher articles, or generate an event page for one source. The public page must retain attribution and link readers to the original reporting.
