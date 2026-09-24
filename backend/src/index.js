const API_ORIGINS = new Set(['https://crosslens.space', 'http://localhost:8787']);
const json = (value, request, init = {}) => new Response(JSON.stringify(value), { ...init, headers: { 'content-type': 'application/json; charset=utf-8', 'cache-control': 'public, max-age=300, stale-while-revalidate=3600', 'access-control-allow-origin': API_ORIGINS.has(new URL(request.url).origin) ? new URL(request.url).origin : 'https://crosslens.space', ...init.headers } });
const xml = (value) => new Response(value, { headers: { 'content-type': 'application/xml; charset=utf-8', 'cache-control': 'public, max-age=300, stale-while-revalidate=3600' } });
const site = 'https://crosslens.space';
async function sitemap(db) { const { results } = await db.prepare('SELECT slug, updated_at FROM events WHERE is_public = 1 ORDER BY updated_at DESC').all(); return xml(`<?xml version="1.0" encoding="UTF-8"?><urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">${results.map(({slug, updated_at}) => `<url><loc>${site}/stories/${encodeURIComponent(slug)}/</loc><lastmod>${updated_at.slice(0,10)}</lastmod></url>`).join('')}</urlset>`); }
export default {
  async fetch(request, env) {
    if (request.method === 'OPTIONS') return new Response(null, { headers: { 'access-control-allow-origin': API_ORIGINS.has(new URL(request.url).origin) ? new URL(request.url).origin : 'https://crosslens.space', 'access-control-allow-methods': 'GET, OPTIONS' } });
    const url = new URL(request.url); const path = url.pathname;
    if (path === '/v1/health') return json({ status: 'ok' }, request);
    if (path === '/v1/sources') { const { results } = await env.DB.prepare('SELECT id, name, homepage, country, language FROM sources WHERE enabled = 1 ORDER BY name').all(); return json({ sources: results }, request); }
    if (path === '/v1/events') { const { results } = await env.DB.prepare('SELECT slug, title, summary, topic_slug, published_at, updated_at FROM events WHERE is_public = 1 ORDER BY updated_at DESC LIMIT 50').all(); return json({ events: results }, request); }
    if (path.startsWith('/v1/events/')) { const slug = decodeURIComponent(path.slice('/v1/events/'.length)); const event = await env.DB.prepare('SELECT slug, title, summary, topic_slug, published_at, updated_at FROM events WHERE slug = ? AND is_public = 1').bind(slug).first(); if (!event) return json({ error: 'Not found' }, request, { status: 404 }); const { results: articles } = await env.DB.prepare('SELECT a.headline, a.original_url, a.published_at, s.name AS source_name, s.country, s.language FROM event_articles a JOIN sources s ON s.id = a.source_id WHERE a.event_slug = ? ORDER BY a.published_at DESC').bind(slug).all(); return json({ event, articles }, request); }
    if (path === '/v1/sitemap.xml') return sitemap(env.DB);
    return json({ error: 'Not found' }, request, { status: 404 });
  },
  async scheduled(_controller, _env, ctx) { ctx.waitUntil(Promise.resolve()); }
};
