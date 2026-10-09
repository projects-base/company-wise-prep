**Short answer:** Keep stock per (SKU, warehouse) in a transactional database and never do read-modify-write in application code. Use a conditional atomic update (`UPDATE ... SET available = available - n WHERE available >= n`) or optimistic locking, so two concurrent orders cannot both take the last unit. Model stock as on-hand, reserved and available, reserve on checkout with an expiry, commit on payment, and record every change in an append-only ledger. For high concurrency, shard by SKU and, for very hot SKUs, split stock into buckets or move reservations into an in-memory counter backed by the ledger.

## Requirements

Functional:
- Track quantity per SKU per warehouse; receive stock, reserve for orders, commit, release, adjust (damage, audit counts), transfer between warehouses.
- Query availability for the storefront; low-stock alerts.

Non-functional:
- No overselling (or a strictly bounded, explicit oversell policy).
- Auditability: every quantity is explainable from movements.
- Availability reads are very high volume and can be slightly stale; writes must be correct.
- Handle flash sales: thousands of reservations per second on one SKU.

## Estimates

- 10M SKUs x 50 warehouses, but sparse: ~50M stock rows, ~100 bytes each, ~5 GB. Fits one Postgres node, with replicas for reads.
- 1M orders/day, 3 lines each: ~35 reservations/s average, ~1k/s peak; flash sale on one SKU, ~5k/s.
- Availability reads: 50k/s from product pages; served from cache.

## API

```text
GET  /inventory/{sku}?region=               -> available (cached, approximate)
POST /reservations    {orderId, lines:[{sku, qty}], ttlSec}  Idempotency-Key -> reservationId
POST /reservations/{id}/commit              (on payment success)
POST /reservations/{id}/release             (on cancel / timeout)
POST /stock/receipts  {warehouseId, sku, qty, poId}
POST /stock/adjustments {warehouseId, sku, delta, reason}
```

## Data model

```sql
CREATE TABLE stock (
  sku TEXT, warehouse_id INT,
  on_hand INT NOT NULL CHECK (on_hand >= 0),
  reserved INT NOT NULL CHECK (reserved >= 0),
  version BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (sku, warehouse_id),
  CHECK (reserved <= on_hand)
);  -- available = on_hand - reserved

CREATE TABLE reservation (
  id UUID PRIMARY KEY, order_id TEXT UNIQUE, status TEXT,   -- HELD, COMMITTED, RELEASED, EXPIRED
  expires_at TIMESTAMPTZ, created_at TIMESTAMPTZ);
CREATE TABLE reservation_line (reservation_id UUID, sku TEXT, warehouse_id INT, qty INT);

CREATE TABLE stock_movement (                 -- append-only ledger
  id BIGSERIAL PRIMARY KEY, sku TEXT, warehouse_id INT, delta_on_hand INT, delta_reserved INT,
  type TEXT, ref_id TEXT, at TIMESTAMPTZ DEFAULT now());
```

## Architecture

```text
 Storefront --> Availability API --> Redis cache (sku -> available, short TTL / CDC refresh)
 Checkout ----> Inventory service (stateless, N instances)
                    |  one transaction: update stock + reservation + movement + outbox
                    v
                Postgres (primary, sharded by sku when needed) --> read replicas
                    | outbox -> Kafka: inventory.changed
                    v
            cache refresher, search index, low-stock alerts, analytics
 Expiry sweeper: releases HELD reservations past expires_at
 WMS / receiving -> receipts & adjustments
```

## Deep dives

**1. Correct stock updates.** The lost-update bug: two threads read `available = 1`, both subtract, both write 0, two orders ship one item. Fixes, in order of preference:

```sql
UPDATE stock
SET reserved = reserved + :qty, version = version + 1
WHERE sku = :sku AND warehouse_id = :wh AND on_hand - reserved >= :qty;
-- 1 row updated = success, 0 rows = insufficient stock
```

The row lock taken by `UPDATE` serialises concurrent writers to that row, and the condition is re-checked on the latest version, so this is safe at Postgres's default Read Committed level. Alternatives: `SELECT ... FOR UPDATE` then update (pessimistic, more round trips), or optimistic locking with a version column (`@Version` in JPA) and retry. For multi-line orders, update lines in a fixed order (sorted by SKU) to avoid deadlocks, all in one transaction so an order reserves all or nothing.

**2. Reservation lifecycle.** HELD on checkout with a TTL (say 10 minutes), COMMITTED on payment (`on_hand -= qty, reserved -= qty`), RELEASED on cancel, EXPIRED by the sweeper. Every call is idempotent on `order_id`, so retries do not double reserve. Payment and inventory are separate services, so this is a saga: payment failure triggers a release.

**3. High concurrency and scale (follow-up).** A single hot row is the bottleneck: every reservation on that SKU queues on one lock, maybe a few thousand per second at best. Options:
- *Bucketed stock:* split 1,000 units into 10 rows of 100; a request picks a random bucket and tries others if empty. Lock contention divides by 10.
- *In-memory counter:* for flash sales, load the stock into Redis and decrement atomically (`DECRBY` and check, or a Lua script). Write reservations to the database asynchronously from a queue. Redis is the gate; the ledger is the truth, with reconciliation.
- *Queue per SKU:* serialise requests for a hot SKU through one partition of Kafka and a single consumer; no lock contention, at the cost of async responses.
- *Sharding:* partition by SKU across database nodes; most orders touch few SKUs. Cross-shard orders become per-shard reservations within the saga.
In the Java service, do not add `synchronized` or JVM locks: there are many instances, so the database or Redis must be the point of serialisation.

**4. Reads.** Product pages read a cached `available`, refreshed from change events. A slightly stale "in stock" is fine because the reservation is the real check.

## Trade-offs

- **Pessimistic vs optimistic locking:** pessimistic is simpler under high contention; optimistic is better when conflicts are rare and avoids holding locks.
- **Strict vs soft limits:** some businesses allow small oversell (backorder) for availability; make it an explicit policy, not a race.
- **Ledger plus snapshot vs snapshot only:** the ledger costs storage but makes audits and repairs possible.
- **Redis gate:** high throughput, but needs reconciliation if Redis and the database disagree after a failure.

## Follow-ups

- *How do you reconcile with a physical count?* Post an adjustment movement with the difference and a reason; never overwrite `on_hand` directly.
- *Which warehouse fulfils?* An allocation step picks the nearest warehouse with stock, falling back to split shipments.

Related: [Q6 · Transactions, isolation, locking and MVCC](../academy/lessons/Q6.md), [Q8 · Scaling databases](../academy/lessons/Q8.md), [B7 · Classic concurrency problems](../academy/lessons/B7.md).
