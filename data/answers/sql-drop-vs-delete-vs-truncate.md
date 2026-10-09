**Short answer:** `DELETE` is DML: it removes rows one by one, can take a `WHERE` clause, fires row-level `DELETE` triggers, and is fully transactional. `TRUNCATE` removes all rows at once by deallocating the table's storage, which is much faster, but it has no `WHERE` and doesn't fire `DELETE` triggers. `DROP` removes the table itself: structure, data, indexes, constraints and triggers. A schema is a named namespace inside a database that groups tables, views and other objects.

## Explanation

| | DELETE | TRUNCATE | DROP |
|---|---|---|---|
| Type | DML | DDL | DDL |
| Removes | Chosen rows (`WHERE`) | All rows | The whole table object |
| Structure kept | Yes | Yes | No |
| Speed on big tables | Slow: row by row, logs each row | Fast | Fast |
| Row `DELETE` triggers | Fire | Don't fire | Don't fire |
| Identity / sequence | Not reset | Reset with `RESTART IDENTITY` (PostgreSQL); reset by default in MySQL/SQL Server | Gone with the table |
| Foreign keys referencing it | Checked row by row | Fails unless `CASCADE` | Fails unless `CASCADE` |
| Rollback | Yes | PostgreSQL: yes, inside a transaction. Oracle, MySQL: no (implicit commit) | PostgreSQL: yes. Oracle, MySQL: no |

**Rollback (follow-up):** the classic answer "only DELETE can be rolled back" is true for Oracle and MySQL, where DDL commits implicitly. In PostgreSQL, DDL is transactional, so `TRUNCATE` and `DROP` inside `BEGIN ... ROLLBACK` are undone. SQL Server also allows rolling back `TRUNCATE` inside an explicit transaction.

**Triggers (follow-up):** `DELETE` fires `BEFORE/AFTER DELETE` triggers. PostgreSQL has separate statement-level `ON TRUNCATE` triggers that fire for `TRUNCATE`; MySQL has no truncate triggers. `DROP` fires no table triggers (PostgreSQL event triggers can observe DDL).

**Locking:** in PostgreSQL, `TRUNCATE` and `DROP` take an `ACCESS EXCLUSIVE` lock (blocks even reads); `DELETE` takes row locks and leaves dead rows for `VACUUM`.

**Schema:** in PostgreSQL, a database contains schemas, and schemas contain tables (`sales.orders`). The default is `public`, and `search_path` decides which schema an unqualified name resolves to. Schemas separate modules or tenants and carry permissions. In MySQL, "schema" and "database" mean the same thing. The word also means the overall structure (tables, columns, constraints) of a database.

## Example

```sql
DELETE FROM orders WHERE created_at < now() - interval '1 year';   -- some rows, triggers fire

TRUNCATE TABLE order_staging RESTART IDENTITY;                    -- all rows, fast

DROP TABLE IF EXISTS order_staging_old;                           -- the table itself

BEGIN;
TRUNCATE TABLE audit_tmp;
ROLLBACK;                                                         -- PostgreSQL: rows are back

CREATE SCHEMA billing;
CREATE TABLE billing.invoice (id bigserial PRIMARY KEY, total numeric(12,2));
```

## Pitfalls and follow-ups

- **Delete millions of rows from a live table?** Delete in batches (by id range) to avoid long locks and huge transactions, or partition by date and drop old partitions.
- **`DELETE` without `WHERE`** removes every row but keeps the table, slowly. Double-check before running in production.
- **Disk space:** in PostgreSQL, `DELETE` leaves dead tuples; space is reused after `VACUUM`, not returned to the OS. `TRUNCATE` frees it immediately.
- **`TRUNCATE ... CASCADE`** also truncates every table with a foreign key to it. Know what you are wiping.
- **Soft delete** (`deleted_at` column) is common when you need audit or undo.

Deeper: [Q1 · Relational model and SQL basics](../academy/lessons/Q1.md), [Q6 · Transactions, isolation, locking and MVCC](../academy/lessons/Q6.md).
