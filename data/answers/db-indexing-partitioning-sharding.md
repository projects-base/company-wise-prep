**Short answer:** Every index speeds up some reads but must be updated on every insert, on deletes (eventually, by vacuum) and on updates to indexed columns, takes disk and memory, and gives the planner more choices to get wrong. So you index for the real query patterns, not every column. Partitioning splits one large table into smaller pieces *inside one database* (by range, list or hash) so queries and maintenance touch fewer rows. Sharding splits data across *several database servers*, each holding a subset, so you scale writes and storage beyond one machine at the cost of cross-shard queries and transactions.

## Explanation

**Why not many indexes**
- Write amplification: an insert into a table with 6 indexes is 7 writes plus WAL for each.
- In PostgreSQL, an update that changes an indexed column cannot be a HOT (heap-only tuple) update, so every index gets a new entry.
- Memory: indexes compete with table data for the buffer cache.
- Unused or overlapping indexes (`(a)` next to `(a, b)`) are pure cost. Find them with `pg_stat_user_indexes.idx_scan = 0`.

**Partitioning (one server)**
- PostgreSQL declarative partitioning: `PARTITION BY RANGE (created_at)`, `LIST (region)` or `HASH (customer_id)`.
- **Partition pruning**: a query with `WHERE created_at >= '2026-09-01'` scans only the matching partitions.
- Cheap retention: drop or detach an old partition instead of a huge `DELETE`.
- Each partition has its own smaller indexes.
- Limits: a primary key or unique constraint must include the partition key; queries without the key scan all partitions.

**Sharding (many servers)**
- A shard key (e.g. `customer_id`) decides the server: hash-based, range-based, or a lookup directory.
- Scales writes, storage and connections horizontally.
- Costs: cross-shard joins and aggregates happen in the application or a coordinator; distributed transactions need 2PC or sagas; rebalancing when adding shards (consistent hashing or many virtual shards helps); hot keys can overload one shard.
- PostgreSQL has no built-in sharding; options are application-level routing or extensions such as Citus.

Order of operations in practice: indexes and query fixes, then read replicas and caching, then partitioning, and only then sharding.

## Example

```sql
CREATE TABLE trades (
  id         bigint,
  account_id bigint,
  traded_at  timestamptz NOT NULL,
  qty        int,
  PRIMARY KEY (id, traded_at)
) PARTITION BY RANGE (traded_at);

CREATE TABLE trades_2026_10 PARTITION OF trades
  FOR VALUES FROM ('2026-10-01') TO ('2026-11-01');

CREATE INDEX ON trades (account_id, traded_at);  -- created on each partition

-- Only trades_2026_10 is scanned
EXPLAIN SELECT * FROM trades
WHERE traded_at >= '2026-10-05' AND account_id = 42;
```

## Pitfalls and follow-ups

- **Partitioning vs sharding in one line?** Partitioning: same server, transparent to queries. Sharding: different servers, the application or a router must know.
- **How to choose a shard key?** High cardinality, evenly distributed, present in most queries, and keeps related data together (all of one customer's orders on one shard).
- **Vertical vs horizontal partitioning?** Vertical splits columns into separate tables; horizontal splits rows.
- **Composite index column order?** Equality columns first, then the range column; the leftmost prefix rule decides which queries can use it.
- **Covering index?** `INCLUDE (qty)` lets an index-only scan answer without touching the heap.

Go deeper: [Q5 · Indexes and reading EXPLAIN](../academy/lessons/Q5.md), [Q8 · Scaling databases](../academy/lessons/Q8.md).
