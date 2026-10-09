**Short answer:** Clients upload directly to object storage with a pre-signed URL, so app servers never carry the bytes. A small metadata record (short ID, type, size, status, renditions, a secret delete token) goes in a key-value or SQL store. An upload-complete event triggers background workers that validate the file, scan it, and produce renditions (thumbnails, WebP/AVIF, MP4 from GIF, several video bitrates in HLS). Everything is served through a CDN, so reads, which dominate, almost never reach our servers.

## Requirements

Functional:
- Anonymous upload of image, GIF or video; get back a share link and a delete link.
- View via link; serve the right format and size for the device and network.
- Optional expiry; takedown of abusive content.

Non-functional:
- Read-heavy (100:1 or more), low latency worldwide.
- Uploads up to, say, 20 MB images and 200 MB videos; resumable for big files.
- High durability: never lose an accepted file.
- Abuse resistance, since there is no sign-up.

## Estimates

- 5M uploads/day (~60/s, peak ~300/s). Average original 2 MB, mostly images: 10 TB/day of originals.
- Renditions add ~50%: ~15 TB/day, ~5.5 PB/year. Object storage with lifecycle tiering (hot to cold) is the only sane option.
- Views: 500M/day, ~6k/s average, peak ~30k/s. With a 95%+ CDN hit rate, origin sees ~1.5k/s.
- Metadata: 5M rows/day x 500 bytes = 2.5 GB/day, ~1 TB/year. Small.

## API

```text
POST /uploads                {contentType, size, sha256?}
  -> {mediaId, uploadUrl (pre-signed PUT or multipart), deleteToken}
POST /uploads/{mediaId}/complete
GET  /m/{mediaId}            -> page/JSON with rendition URLs (cdn.example.com/...)
GET  cdn/{mediaId}/{rendition}.{ext}    e.g. /abc123/w640.webp, /abc123/hls/master.m3u8
DELETE /m/{mediaId}          header: X-Delete-Token
```

## Data model

```text
media
  id            (short random base62, 8-10 chars)
  status        UPLOADING | PROCESSING | READY | FAILED | REMOVED
  kind          IMAGE | GIF | VIDEO
  original_key, content_type, size, sha256, width, height, duration_ms
  delete_token_hash, uploader_ip_hash, created_at, expires_at
rendition
  media_id, name (thumb, w640, mp4_720p, hls...), key, format, bytes, status
```

**SQL vs NoSQL (follow-up).** Access is almost entirely by primary key (`get media by id`) with no joins, and volume is modest. Both work. A key-value or wide-column store (DynamoDB, Cassandra, Bigtable) scales writes and reads horizontally with no sharding work and fits "get by ID". Postgres is fine for years at ~1 TB/year, gives easy ad-hoc queries for moderation ("all uploads from this IP hash today") and transactions for status changes. My pick: Postgres partitioned by month to start, because the query needs of moderation matter and the scale fits; move metadata to a KV store if writes or size outgrow it. Either way, the bytes never go in the database.

## Architecture

```text
 Client ---(1) POST /uploads----> Upload API --> metadata DB (UPLOADING)
   |   <-- pre-signed URL ---------|
   |---(2) PUT bytes -------------------------> Object storage (originals bucket)
   |---(3) complete --------------> Upload API -> queue: media.uploaded
                                                     |
                               Processing workers (autoscaled, GPU optional for video)
                                 - validate magic bytes, size, decode
                                 - abuse/CSAM hash match, virus scan
                                 - images: thumb + widths, WebP/AVIF, strip EXIF
                                 - GIF: convert to MP4/WebM (much smaller)
                                 - video: transcode to HLS ladder (240p..1080p)
                                     |
                               renditions bucket + metadata READY
 Viewer --> CDN --(miss)--> origin (object storage / image service)
```

## Deep dives

**1. Upload path.** Pre-signed URLs keep upload bandwidth off app servers. For large files use multipart upload so a dropped connection resumes from the last part. The `complete` call (or a storage event notification) moves status to `PROCESSING` and enqueues work. Dedup by content hash is optional: identical files can share the original object.

**2. Background transcoding for slow networks (follow-up).** Work is queued, not inline, so uploads return fast. Images: generate a few widths and modern formats; the client picks via `srcset`, or the edge picks by the `Accept` header. GIFs are a big win: converting to MP4 is typically many times smaller. Video: transcode into an adaptive bitrate ladder with HLS or DASH segments; the player switches bitrate by measured bandwidth, so slow networks get 240p instead of buffering. Make the first low rendition fast (show "processing" until then) and do the rest later. Jobs are idempotent per `(mediaId, rendition)`, retried with backoff, dead-lettered after N failures.

**3. Abuse without accounts.** Rate limit uploads per IP and per device fingerprint, CAPTCHA after a threshold, size and type limits, hash matching against known illegal content before marking READY, and a report button that feeds a moderation queue. The delete token is the only ownership proof; store only its hash.

**4. Serving.** URLs are immutable (`/id/rendition`), so CDN cache headers can be long-lived. Deletion purges CDN paths. Popular items stay at the edge; the long tail is served from origin and cold storage tiers.

## Trade-offs

- **Eager vs on-demand renditions:** eager costs compute and storage for files few people view; on-demand resizing at the edge (with caching) saves that but adds first-view latency. Eager for a small fixed set, on-demand for odd sizes.
- **Short random IDs vs sequential:** random IDs are not guessable, which matters when links are the only access control.
- **Storage cost:** move originals to a cold tier after 30 days; keep hot renditions.

## Follow-ups

- *Interviewer adds requirements mid-round (follow-up).* The design absorbs most: "private links" adds a signed, expiring CDN URL; "albums" adds an `album` table referencing media; "accounts" adds an owner column and auth; "live stats" adds view events from CDN logs into an analytics pipeline. Say which component changes and keep the core flow intact.
- *A viral image gets 1M views a minute?* That is a CDN problem we have already solved; origin shielding prevents a stampede on misses.

Related: [F1 · Building blocks](../academy/lessons/F1.md), [F4 · Case studies](../academy/lessons/F4.md), [Q8 · Scaling databases](../academy/lessons/Q8.md).
