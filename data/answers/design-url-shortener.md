**Short answer:** Generate a short code from a unique numeric ID (range-allocated or Snowflake) encoded in base62, store `code -> long URL` in a key-value store, and serve redirects from a cache-heavy, stateless tier behind a CDN. Reads outnumber writes ~100:1, so the design is about the redirect path: CDN and Redis absorb most hits, the database handles misses. Because this interviewer pushes past the textbook, be ready to justify each component, walk failure scenarios, and cover abuse (DDoS, malicious links).

## Requirements

**Functional**
- `POST` a long URL, get a short URL (optional custom alias, optional expiry).
- `GET /{code}` redirects to the long URL.
- Basic click analytics per link.

**Non-functional**
- Redirect p99 under ~50 ms; very high availability for reads.
- Codes must be unique and not guessable in sequence (to stop enumeration).
- Durable: a link, once created, must not silently change.

## Estimates

- 100M new links/month ≈ 40 writes/s average; peaks ~400/s.
- 100:1 read ratio ⇒ ~4k redirects/s average, design for 40k/s peaks.
- Storage: 100M × 12 months × 5 years = 6B links × ~500 bytes ≈ 3 TB. Sharding needed eventually.
- Code length: 62^7 ≈ 3.5 × 10^12, plenty for 6B. Seven characters.
- Cache: 20% of links get 80% of traffic; caching the hot ~10M entries × 500 B ≈ 5 GB in Redis.

## API

```text
POST /api/v1/links   { longUrl, customAlias?, expiresAt? }  -> 201 { code, shortUrl }
GET  /{code}         -> 302 Location: longUrl   (404 if missing, 410 if expired)
GET  /api/v1/links/{code}/stats  -> { clicks, byDay[], byCountry[] }
DELETE /api/v1/links/{code}
```

301 vs 302: 301 is cached by browsers, so fewer hits reach us but we lose click analytics and cannot change or disable the link. Use 302 (or 307) when analytics or takedown matter.

## Data model

```text
links (KV store or sharded Postgres, partition key = code)
  code        VARCHAR(10) PK
  long_url    TEXT
  owner_id    BIGINT NULL
  created_at  TIMESTAMPTZ
  expires_at  TIMESTAMPTZ NULL
  status      ACTIVE | DISABLED

click_events (append-only, Kafka -> columnar store)
  code, ts, country, referrer, user_agent_class
```

Optional `long_url_hash -> code` index if you want the same long URL to return the same code (dedup). Many products skip this; per-user links are often wanted anyway.

## Architecture

```text
            create path                                   redirect path
client --> LB --> [ Write API ] --> [ ID range allocator ]   client --> CDN/edge (cache 302s briefly)
                     |               (ZooKeeper/DB hands       |  miss
                     |                out blocks of 1M ids)    v
                     v                                       LB --> [ Redirect service (stateless) ]
              [ links DB (sharded by code) ] <-- miss --          |  hit
                     ^                                      [ Redis cache ]
                     |                                            |
              [ URL safety scanner ] (async)          click event -> Kafka -> [ analytics ]
```

## Deep dives

**1. Code generation, three options.**
- *Hash the URL (MD5/SHA-256, take 7 base62 chars):* deterministic, but collisions must be detected and resolved (append a salt and retry), and every insert needs a read-check.
- *Counter + base62:* unique by construction, no collision check. A single counter is a bottleneck, so each write node leases a range (say 1M IDs) from a coordinator and hands them out locally. A crash wastes at most the rest of the range, which is harmless.
- *Random 7 chars + insert-if-absent:* simple and not sequential, but collisions grow as the space fills.

Pick counter ranges, then make codes non-sequential by applying a reversible bijection (for example, a keyed permutation over the 0..62^7 range) before base62. That stops someone walking `abc0001`, `abc0002`.

**2. Redirect hot path.** Redirect service checks Redis, then the DB; on a DB miss it caches a negative entry for a short time so a flood of random codes does not hammer the DB. Click events are fire-and-forget to Kafka; never block the redirect on analytics. Links are immutable, so caching is easy: invalidate only on delete/disable.

**3. Custom aliases.** Plain `INSERT` with the alias as the primary key; a unique violation means taken. Reserve aliases that collide with routes (`api`, `login`) and block offensive words.

**4. Multi-region.** Redirects are read-only, so replicate links to every region and serve locally. Writes go to the region that owns the ID range; since ranges are disjoint, two regions never create the same code. Replication lag means a brand-new link may 404 in a far region for a second; handle by falling back to the home region on miss.

## Trade-offs

- **KV store vs Postgres:** access is pure key lookup, so a KV store (DynamoDB/Cassandra) scales horizontally with no effort. Postgres is fine up to a few TB with partitioning, and gives constraints and simpler ops for a small team.
- **Dedup of long URLs:** saves space but needs a second index and hurts per-user analytics.
- **CDN caching of redirects:** big load reduction, but disabling a malicious link takes up to the CDN TTL unless you purge.

## Follow-ups

- **How do you prevent a DDoS?** Layered: (1) CDN/edge absorbs volumetric traffic and caches popular redirects; (2) a WAF and per-IP / per-API-key rate limits at the gateway (token bucket in Redis); (3) creation requires an API key or CAPTCHA for anonymous users, with lower limits; (4) negative caching and a Bloom filter of existing codes so random-code floods never reach the DB; (5) autoscaling of the stateless redirect tier plus load shedding (return 503 early) to protect the database.
- *Abuse (phishing links)?* Scan URLs asynchronously against threat lists; disable and purge from CDN when flagged.
- *Expiry?* Check `expires_at` on read (return 410) and delete expired rows with a background job; set Redis TTL to not exceed expiry.
- *Analytics at scale?* Kafka into a columnar store, pre-aggregated per code per day.

Related lessons: [F4 · Case studies: URL shortener, rate limiter, notification system](../academy/lessons/F4.md), [F3 · The system design method and estimation](../academy/lessons/F3.md), [F1 · Building blocks](../academy/lessons/F1.md).
