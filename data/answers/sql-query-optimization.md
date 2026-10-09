**Short answer:** First find the slow queries with `pg_stat_statements`, then run `EXPLAIN (ANALYZE, BUFFERS)` on the worst one and read where the time and rows go. The usual fixes, in order: add the right index (often a composite one matching the `WHERE` and `ORDER BY`), rewrite the query so the index can be used (no functions on the indexed column, keyset instead of large `OFFSET`), fetch only the columns and rows you need, and keep planner statistics fresh. Then measure again with the same plan output.

## Explanation

**1. Find the query.** `pg_stat_statements` ranks statements by total time, mean time and calls. A query that takes 5 ms but runs 10,000 times a minute can matter more than one slow report. Also turn on `log_min_duration_statement` to log slow ones.

**2. Read the plan.** `EXPLAIN` shows the planner's estimate. `EXPLAIN (ANALYZE, BUFFERS)` runs the query and shows the real time, rows and pages read for each node. Look for:
- **Seq Scan** on a big table with a selective filter: a missing or unusable index. (A seq scan is correct when you read a large share of the table.)
- **Estimated rows far from actual rows:** stale or missing statistics. Run `ANALYZE`, or add extended statistics for correlated columns.
- **Sort with "external merge Disk":** the sort did not fit in `work_mem`. An index in the right order can remove the sort.
- **Nested Loop with a huge outer side:** often caused by a bad estimate.
- **Rows Removed by Filter** large: the index does not match the filter well.

**3. Fix.**
- **Composite index** in the right order: equality columns first, then the range or sort column. Example: `(customer_id, placed_at DESC)` for "a customer's latest bets".
- **Covering index** with `INCLUDE (...)` so PostgreSQL can do an **Index Only Scan** without visiting the table (this also needs the visibility map to be current, which vacuum maintains).
- **Partial index** for a hot subset: `WHERE status = 'OPEN'`.
- **Sargable predicates:** `WHERE created_at >= '2026-10-01' AND created_at < '2026-10-02'` instead of `WHERE date(created_at) = ...`, or an expression index on `lower(email)`.
- **Keyset pagination:** `WHERE (placed_at, id) < (:t, :id) ORDER BY placed_at DESC, id DESC LIMIT 50` instead of `OFFSET 100000`, which still reads and throws away 100,000 rows.
- **Fewer round trips:** remove N+1 queries in JPA (fetch join, `@BatchSize`), batch inserts.
- **Bigger changes:** partition huge time-series tables (bets by month) so old partitions are pruned and dropped cheaply; send heavy reporting to a read replica.

## Example

```sql
-- Slow: last 20 settled bets for a customer
EXPLAIN (ANALYZE, BUFFERS)
SELECT id, market_id, stake, payout, placed_at
FROM bet
WHERE customer_id = 42 AND status = 'SETTLED'
ORDER BY placed_at DESC
LIMIT 20;
-- Plan: Seq Scan on bet ... Rows Removed by Filter: 9,800,000 -> Sort -> Limit

CREATE INDEX CONCURRENTLY idx_bet_customer_status_placed
    ON bet (customer_id, status, placed_at DESC)
    INCLUDE (market_id, stake, payout);
-- Plan now: Index Only Scan using idx_bet_customer_status_placed ... Limit (no Sort)
```

`CREATE INDEX CONCURRENTLY` builds the index without blocking writes, which matters on a live betting table. It is slower and cannot run inside a transaction block.

## Pitfalls and follow-ups

- **Reading EXPLAIN: seq vs index scan, join strategy?** Index Scan reads the index then the table; Bitmap Heap Scan collects matches first and reads the table in page order (good for medium selectivity); Index Only Scan skips the table. Joins are Nested Loop, Hash Join or Merge Join. Read the plan inside out and compare estimated with actual rows at each node.
- **Composite index column order; covering indexes?** A B-tree on `(a, b, c)` works best for filters on `a`, `a, b` or `a, b, c` (the leftmost prefix). A filter on `b` alone usually cannot use it efficiently. Put equality columns first and the range or `ORDER BY` column last. `INCLUDE` adds payload columns that are not part of the search key so the query becomes index-only.
- **When does an index hurt?** Every insert and update must also update each index, so write-heavy tables (bet events, wallet ledger) slow down. Indexes use disk and memory. In PostgreSQL, updating an indexed column prevents HOT updates, which adds bloat. Low-selectivity indexes (a boolean column alone) are rarely used. Drop unused ones; `pg_stat_user_indexes.idx_scan = 0` shows candidates.
- **The plan uses the wrong index?** Check statistics first (`ANALYZE`), then the query shape. PostgreSQL has no built-in query hints.
- **Parameter type mismatches** (comparing a `bigint` column to a `numeric` parameter) can stop index use. Match types.
- **Aurora PostgreSQL** is PostgreSQL-compatible, so the same `EXPLAIN`, indexes and `pg_stat_statements` apply. Its storage layer is different, but query tuning is the same work.

Further reading: [Q5 · Indexes and reading EXPLAIN](../academy/lessons/Q5.md), [Q7 · Query performance in practice](../academy/lessons/Q7.md), [Q8 · Scaling databases](../academy/lessons/Q8.md). Official docs: [Using EXPLAIN](https://www.postgresql.org/docs/current/using-explain.html), [Multicolumn indexes](https://www.postgresql.org/docs/current/indexes-multicolumn.html).
