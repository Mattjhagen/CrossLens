CREATE TABLE IF NOT EXISTS sources (id TEXT PRIMARY KEY, name TEXT NOT NULL, homepage TEXT NOT NULL, country TEXT NOT NULL, language TEXT NOT NULL, feed_url TEXT NOT NULL, enabled INTEGER NOT NULL DEFAULT 1, updated_at TEXT NOT NULL);
CREATE TABLE IF NOT EXISTS events (slug TEXT PRIMARY KEY, title TEXT NOT NULL, summary TEXT NOT NULL, topic_slug TEXT NOT NULL, published_at TEXT NOT NULL, updated_at TEXT NOT NULL, is_public INTEGER NOT NULL DEFAULT 0);
CREATE TABLE IF NOT EXISTS event_articles (id TEXT PRIMARY KEY, event_slug TEXT NOT NULL REFERENCES events(slug), source_id TEXT NOT NULL REFERENCES sources(id), headline TEXT NOT NULL, original_url TEXT NOT NULL UNIQUE, published_at TEXT NOT NULL);
CREATE INDEX IF NOT EXISTS event_articles_event_idx ON event_articles(event_slug);
CREATE INDEX IF NOT EXISTS events_public_updated_idx ON events(is_public, updated_at DESC);
