**Short answer:** An INNER JOIN returns only rows that match in both tables. LEFT (or RIGHT) OUTER JOIN keeps every row from the left (or right) table and fills `NULL`s where there is no match. FULL OUTER JOIN keeps unmatched rows from both sides. CROSS JOIN returns every combination, and a SELF JOIN joins a table to itself under two aliases. DDL (Data Definition Language: `CREATE`, `ALTER`, `DROP`, `TRUNCATE`) changes the structure; DML (Data Manipulation Language: `INSERT`, `UPDATE`, `DELETE`, `MERGE`, and usually `SELECT`) changes or reads the data.

## Explanation

**Joins.** Think of the join condition as a filter on all pairs of rows.
- **INNER:** pairs where the condition is true.
- **LEFT:** the inner result plus left rows with no match, right side `NULL`. The usual way to find "rows with no match": `LEFT JOIN ... WHERE right.id IS NULL` (an anti-join; `NOT EXISTS` is equivalent and often clearer).
- **RIGHT:** the mirror of LEFT. Most people rewrite it as a LEFT join with the tables swapped.
- **FULL OUTER:** both sides kept. Useful for reconciling two sources.
- **CROSS:** the Cartesian product, m × n rows. Useful for generating combinations, such as every market for every day.
- **SELF:** for hierarchies (employee and manager in one table) or comparing rows of the same table.

**DDL vs DML in PostgreSQL.** Many databases auto-commit DDL. PostgreSQL does not: DDL is transactional, so you can `BEGIN; ALTER TABLE ...; ROLLBACK;`. That makes migrations safer. `TRUNCATE` is DDL-like: it removes all rows quickly without scanning them and fires no row-level `DELETE` triggers. In PostgreSQL it can still be rolled back inside a transaction. DCL (`GRANT`, `REVOKE`) and TCL (`COMMIT`, `ROLLBACK`) are the other two groups.

## Example

```sql
-- Every customer with their bets (customers with no bets appear with NULLs)
SELECT c.id, c.name, b.id AS bet_id, b.stake
FROM customer c
LEFT JOIN bet b ON b.customer_id = c.id;

-- Customers who never placed a bet (anti-join)
SELECT c.id, c.name
FROM customer c
WHERE NOT EXISTS (SELECT 1 FROM bet b WHERE b.customer_id = c.id);

-- Self join: each employee with their manager's name
SELECT e.name AS employee, m.name AS manager
FROM employee e
LEFT JOIN employee m ON m.id = e.manager_id;
```

A classic trap: putting a filter on the right table in `WHERE` turns a LEFT join back into an INNER join, because `NULL` rows fail the filter.

```sql
-- Wrong: drops customers with no settled bets
SELECT c.id, count(b.id) FROM customer c
LEFT JOIN bet b ON b.customer_id = c.id
WHERE b.status = 'SETTLED' GROUP BY c.id;

-- Right: filter inside ON
SELECT c.id, count(b.id) FROM customer c
LEFT JOIN bet b ON b.customer_id = c.id AND b.status = 'SETTLED'
GROUP BY c.id;
```

## Pitfalls and follow-ups

- **How does the database execute a join?** PostgreSQL's planner chooses among three:
  - **Nested loop:** for each outer row, look up matching inner rows. Fast when the outer side is small and the inner side has an index on the join key.
  - **Hash join:** build a hash table on the smaller input, then probe it with the other. Good for large, unsorted inputs with an equality condition. Needs `work_mem`; spills to disk if the table does not fit.
  - **Merge join:** both inputs sorted on the key (by an index or a sort step), then walked together. Good for large inputs already in order.
  `EXPLAIN` shows the choice. A bad row estimate (stale statistics) is the usual reason for a bad choice; `ANALYZE` refreshes them.
- **`count(*)` vs `count(b.id)` after a LEFT JOIN?** `count(*)` counts the `NULL`-padded row as 1; `count(b.id)` gives 0 for a customer with no bets.
- **Duplicate rows after a join?** A one-to-many join repeats the "one" side. Aggregate first, or use `EXISTS` when you only need to filter.
- **`DELETE` vs `TRUNCATE`?** `DELETE` is DML, removes rows one by one, can have a `WHERE`, fires triggers. `TRUNCATE` removes everything at once and is much faster on big tables.

Further reading: [Q1 · Relational model and SQL basics](../academy/lessons/Q1.md), [Q5 · Indexes and reading EXPLAIN](../academy/lessons/Q5.md).
