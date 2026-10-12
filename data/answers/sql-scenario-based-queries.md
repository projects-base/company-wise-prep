**Short answer:** Scenario rounds reuse a handful of shapes: **join + aggregate** (`GROUP BY` with `HAVING`), **"has no rows"** (`NOT EXISTS`), **top-N per group** (`ROW_NUMBER`/`DENSE_RANK` in a CTE, filter outside), **running totals and period-over-period** (`SUM(...) OVER (ORDER BY ...)`, `LAG`), **duplicates** (`GROUP BY ... HAVING COUNT(*) > 1`, or `ROW_NUMBER` to keep one), **gaps and islands** (date minus row number), and **conditional aggregation** (`COUNT(*) FILTER (WHERE ...)`). I answer each the same way: clarify ties, NULLs and the time window, build it in CTE steps, check edge cases, then name the index that makes it fast. Below, every shape on one small betting schema, in PostgreSQL.

## Explanation

### The schema

A simplified, single-leg version of [the betting schema](design-betting-database-schema.md) (there, a bet has `bet_legs`; here a bet is on one event):

```sql
CREATE TABLE users  (id BIGINT PRIMARY KEY, name TEXT NOT NULL, country CHAR(2) NOT NULL,
                     created_at TIMESTAMPTZ NOT NULL);
CREATE TABLE events (id BIGINT PRIMARY KEY, sport TEXT NOT NULL, name TEXT NOT NULL,
                     starts_at TIMESTAMPTZ NOT NULL);
CREATE TABLE bets (
  id         BIGINT PRIMARY KEY,
  user_id    BIGINT NOT NULL REFERENCES users,
  event_id   BIGINT NOT NULL REFERENCES events,
  stake      NUMERIC(19,4) NOT NULL CHECK (stake > 0),
  odds       NUMERIC(10,4) NOT NULL CHECK (odds > 1),
  status     TEXT NOT NULL CHECK (status IN ('OPEN','WON','LOST','VOID')),
  payout     NUMERIC(19,4),               -- NULL until settled
  placed_at  TIMESTAMPTZ NOT NULL
);
```

### The basics they check first

- **Logical order:** `FROM/JOIN → WHERE → GROUP BY → HAVING → SELECT → window functions → DISTINCT → ORDER BY → LIMIT`. That is why you cannot use a `SELECT` alias in `WHERE`, and why you cannot filter on a window function without a CTE or subquery.
- **`WHERE` vs `HAVING`:** `WHERE` filters rows before grouping; `HAVING` filters groups after. Put row conditions in `WHERE` so fewer rows are grouped.
- **Joins:** `INNER` keeps matches; `LEFT` keeps every left row, with NULLs where nothing matched. A condition on the right table in `WHERE` turns a `LEFT JOIN` back into an inner join; put it in `ON` instead. More in [sql-join-types](sql-join-types.md).
- **NULLs:** `COUNT(col)` skips NULLs, `COUNT(*)` does not; `SUM` of no rows is NULL, so wrap with `COALESCE`; `NOT IN (subquery)` returns nothing if the subquery has one NULL.
- **`ROW_NUMBER` vs `RANK` vs `DENSE_RANK`:** for values 100, 100, 90 they give 1,2,3 / 1,1,3 / 1,1,2. Ask "what about ties?" before choosing.

## Example

### 1. Join + aggregate: turnover and margin per sport, last 30 days

"Show each sport's number of settled bets, turnover (total stakes) and gross gaming revenue (stakes minus payouts), only sports with more than 1,000 bets."

```sql
SELECT e.sport,
       COUNT(*)                                   AS settled_bets,
       SUM(b.stake)                               AS turnover,
       SUM(b.stake) - SUM(COALESCE(b.payout, 0))  AS ggr,
       ROUND(100 * (SUM(b.stake) - SUM(COALESCE(b.payout, 0))) / SUM(b.stake), 2) AS margin_pct
FROM bets b
JOIN events e ON e.id = b.event_id
WHERE b.placed_at >= now() - INTERVAL '30 days'
  AND b.status IN ('WON', 'LOST')
GROUP BY e.sport
HAVING COUNT(*) > 1000
ORDER BY ggr DESC;
```

Index: `bets (placed_at)` for the window; the join uses the `events` primary key.

### 2. Anti-join: users who registered but never bet

```sql
SELECT u.id, u.name
FROM users u
WHERE NOT EXISTS (SELECT 1 FROM bets b WHERE b.user_id = u.id);
```

`LEFT JOIN bets b ON ... WHERE b.id IS NULL` is equivalent. Avoid `NOT IN` for the NULL reason above. Index: `bets (user_id)`; Postgres does not create indexes on foreign keys for you.

### 3. Top-N per group: the 3 biggest stakers per sport this month

```sql
WITH per_user AS (
  SELECT e.sport, b.user_id, SUM(b.stake) AS total_stake
  FROM bets b JOIN events e ON e.id = b.event_id
  WHERE b.placed_at >= date_trunc('month', now())
  GROUP BY e.sport, b.user_id
), ranked AS (
  SELECT *, DENSE_RANK() OVER (PARTITION BY sport ORDER BY total_stake DESC) AS rnk
  FROM per_user
)
SELECT sport, user_id, total_stake, rnk
FROM ranked
WHERE rnk <= 3
ORDER BY sport, rnk;
```

`DENSE_RANK` includes everyone tied for third; use `ROW_NUMBER` with a tie-breaker (`, user_id`) if they want exactly three rows. The same shape answers "second-highest stake" (`WHERE rnk = 2`) and "latest bet per user" (`ROW_NUMBER() OVER (PARTITION BY user_id ORDER BY placed_at DESC)`, keep `= 1`). In Postgres, `SELECT DISTINCT ON (user_id) ... ORDER BY user_id, placed_at DESC` is a shorter "one row per group".

### 4. Running total: each user's cumulative profit over time

```sql
SELECT user_id, id AS bet_id, placed_at,
       COALESCE(payout, 0) - stake AS pnl,
       SUM(COALESCE(payout, 0) - stake) OVER (
         PARTITION BY user_id
         ORDER BY placed_at, id
         ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW) AS running_pnl
FROM bets
WHERE status IN ('WON', 'LOST')
ORDER BY user_id, placed_at, id;
```

Say why `ROWS` is explicit: with only `ORDER BY`, the default frame is `RANGE ... CURRENT ROW`, which lumps rows with the same `placed_at` together. Adding `id` to the order and using `ROWS` gives one step per bet.

### 5. Period over period: daily turnover, change vs yesterday, 7-day average

```sql
WITH daily AS (
  SELECT placed_at::date AS day, SUM(stake) AS turnover
  FROM bets
  GROUP BY placed_at::date
)
SELECT day, turnover,
       turnover - LAG(turnover) OVER (ORDER BY day)                         AS change_vs_prev,
       AVG(turnover) OVER (ORDER BY day ROWS BETWEEN 6 PRECEDING AND CURRENT ROW) AS avg_7d
FROM daily
ORDER BY day;
```

Pitfall: a day with no bets has no row, so `LAG` compares with the last day that had bets. If that matters, generate the calendar with `generate_series` and `LEFT JOIN` the totals. Also, `placed_at::date` uses the session time zone; say which zone the business reports in (`placed_at AT TIME ZONE 'Europe/London'`).

### 6. Duplicates: find them, then keep one

"The feed sometimes double-inserts. Find bets with the same user, event and stake placed within 5 seconds of the previous one."

```sql
WITH ordered AS (
  SELECT id, user_id, event_id, stake, placed_at,
         LAG(placed_at) OVER (PARTITION BY user_id, event_id, stake ORDER BY placed_at, id) AS prev_at
  FROM bets
)
SELECT *
FROM ordered
WHERE placed_at - prev_at <= INTERVAL '5 seconds';
```

Exact duplicates are simpler: `GROUP BY user_id, event_id, stake, placed_at HAVING COUNT(*) > 1`. To delete them and keep the lowest id:

```sql
DELETE FROM bets
WHERE id IN (
  SELECT id FROM (
    SELECT id, ROW_NUMBER() OVER (PARTITION BY user_id, event_id, stake, placed_at ORDER BY id) AS rn
    FROM bets
  ) t
  WHERE rn > 1
);
```

Then add the `UNIQUE` constraint (or an idempotency key) so it cannot happen again.

### 7. Gaps and islands: longest streak of consecutive betting days

```sql
WITH days AS (
  SELECT DISTINCT user_id, placed_at::date AS day FROM bets
), grouped AS (
  SELECT user_id, day,
         day - (ROW_NUMBER() OVER (PARTITION BY user_id ORDER BY day))::int AS grp
  FROM days
)
SELECT user_id, MIN(day) AS streak_start, MAX(day) AS streak_end, COUNT(*) AS days
FROM grouped
GROUP BY user_id, grp
ORDER BY days DESC
LIMIT 10;
```

The trick: on consecutive days, the date and the row number both go up by one, so their difference stays the same and marks one island. `DISTINCT` first matters, or two bets on one day break the arithmetic. "Missing ids" (gaps) is the mirror: `SELECT id + 1 FROM (SELECT id, LEAD(id) OVER (ORDER BY id) AS next FROM bets) t WHERE next > id + 1`.

### 8. Conditional aggregation: win rate and a pivot by status

```sql
SELECT user_id,
       COUNT(*) FILTER (WHERE status = 'WON')   AS won,
       COUNT(*) FILTER (WHERE status = 'LOST')  AS lost,
       COUNT(*) FILTER (WHERE status = 'OPEN')  AS open,
       ROUND(COUNT(*) FILTER (WHERE status = 'WON')::numeric
             / NULLIF(COUNT(*) FILTER (WHERE status IN ('WON','LOST')), 0), 3) AS win_rate
FROM bets
GROUP BY user_id;
```

`FILTER` is standard SQL and PostgreSQL supports it; `SUM(CASE WHEN status = 'WON' THEN 1 ELSE 0 END)` works everywhere. `NULLIF(..., 0)` avoids division by zero, and `::numeric` avoids integer division (which would give 0).

### 9. Multi-step: users whose first bet was on football and who bet again within 7 days

```sql
WITH firsts AS (
  SELECT DISTINCT ON (b.user_id) b.user_id, b.placed_at, e.sport
  FROM bets b JOIN events e ON e.id = b.event_id
  ORDER BY b.user_id, b.placed_at, b.id
)
SELECT f.user_id
FROM firsts f
WHERE f.sport = 'football'
  AND EXISTS (SELECT 1 FROM bets b
              WHERE b.user_id = f.user_id
                AND b.placed_at > f.placed_at
                AND b.placed_at <= f.placed_at + INTERVAL '7 days');
```

This is the shape of most "retention" questions: find the anchor row per user, then `EXISTS` for the follow-up behaviour.

## Pitfalls and follow-ups

- **"This table has 500 million rows. Is it fast?"** Read the plan with `EXPLAIN (ANALYZE, BUFFERS)`. Put equality columns first and the range or sort column last: `bets (user_id, placed_at)` serves queries 4, 6, 7 and 9. Partition by month on `placed_at` so date-bounded reports scan only recent partitions. See [sql-optimization-performance-scalability](sql-optimization-performance-scalability.md).
- **Function on an indexed column?** `WHERE placed_at::date = '2026-10-01'` cannot use an index on `placed_at`; write `placed_at >= '2026-10-01' AND placed_at < '2026-10-02'`.
- **`COUNT(DISTINCT user_id)` over a window?** Not allowed in Postgres window functions; pre-aggregate in a CTE.
- **`UNION` vs `UNION ALL`?** `UNION` removes duplicates with a sort or hash; use `UNION ALL` unless you need that.
- **CTE vs subquery?** Since PostgreSQL 12, a non-recursive CTE used once is inlined like a subquery; `MATERIALIZED` forces the old behaviour.
- **Reports on the live database?** Run them on a read replica or a warehouse, never on the primary that takes bets. See [data-modeling-and-warehousing-scenarios](data-modeling-and-warehousing-scenarios.md).
- **Nth highest without window functions?** `SELECT DISTINCT stake FROM bets ORDER BY stake DESC OFFSET n-1 LIMIT 1`.

Go deeper: [Q1 · SELECT and JOINs](../academy/lessons/Q1.md), [Q2 · Aggregation, GROUP BY, subqueries and CTEs](../academy/lessons/Q2.md), [Q3 · Window functions](../academy/lessons/Q3.md), [Q4 · SQL interview patterns](../academy/lessons/Q4.md), [Q5 · Indexes and reading EXPLAIN](../academy/lessons/Q5.md).
