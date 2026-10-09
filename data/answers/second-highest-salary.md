**Short answer:** Take the largest salary that is smaller than the maximum: `SELECT MAX(salary) FROM employee WHERE salary < (SELECT MAX(salary) FROM employee)`. Because `MAX` over no rows returns `NULL`, this already returns `NULL` when there is no second salary. To generalise to the Nth highest, use `DENSE_RANK()` so that ties share a rank, or `SELECT DISTINCT salary ... ORDER BY salary DESC OFFSET N-1 LIMIT 1` wrapped in a subquery.

## Explanation

"Second highest" means the second highest *distinct* value. If salaries are 100, 100, 90, the answer is 90, not 100. So every approach must handle duplicates:

1. **Max below the max.** Two index lookups when `salary` is indexed. Only works for N = 2.
2. **`DISTINCT` + `ORDER BY` + `OFFSET`.** Works for any N. On its own it returns *no row* when there is no Nth value; wrapping it as a scalar subquery turns "no row" into `NULL`.
3. **`DENSE_RANK()`.** Ranks 1, 1, 2 for 100, 100, 90. `RANK()` would give 1, 1, 3 and skip 2, and `ROW_NUMBER()` would give 1, 2, 3 and return 100 as second. Window functions also extend to "per department".

## Example

```sql
-- 1. Max below the max (returns NULL if none)
SELECT MAX(salary) AS second_highest_salary
FROM employee
WHERE salary < (SELECT MAX(salary) FROM employee);

-- 2. OFFSET, wrapped so an empty result becomes NULL
SELECT (SELECT DISTINCT salary
        FROM employee
        ORDER BY salary DESC
        OFFSET 1 LIMIT 1) AS second_highest_salary;

-- 3. Nth highest with DENSE_RANK (N = 3 here)
SELECT (SELECT DISTINCT salary
        FROM (SELECT salary, DENSE_RANK() OVER (ORDER BY salary DESC) AS rnk
              FROM employee) r
        WHERE rnk = 3) AS nth_highest_salary;

-- Per department: second highest in each
SELECT department_id, salary
FROM (SELECT department_id, salary,
             DENSE_RANK() OVER (PARTITION BY department_id ORDER BY salary DESC) AS rnk
      FROM employee) r
WHERE rnk = 2;
```

A PostgreSQL function for LeetCode 177 style "getNthHighestSalary(N)":

```sql
CREATE FUNCTION nth_highest_salary(n INT) RETURNS INT AS $$
  SELECT (SELECT DISTINCT salary FROM employee
          ORDER BY salary DESC OFFSET n - 1 LIMIT 1);
$$ LANGUAGE sql STABLE;
```

## Pitfalls and follow-ups

- **Nth highest with `DENSE_RANK`?** Shown above. Use `DENSE_RANK`, not `RANK` or `ROW_NUMBER`, because ties must share a rank with no gaps.
- **Ties and no second salary?** `DISTINCT` (or `DENSE_RANK`) handles ties. The scalar subquery or `MAX` returns `NULL` when nothing qualifies. A bare `LIMIT 1 OFFSET 1` returns zero rows instead, which fails the LeetCode check.
- **`NULL` salaries?** `MAX` ignores them. With `ORDER BY salary DESC`, PostgreSQL puts `NULL`s **first** by default, so add `WHERE salary IS NOT NULL` or `NULLS LAST`.
- **`OFFSET` with a negative N?** Validate input; PostgreSQL rejects a negative `OFFSET`.
- **Performance?** With an index on `salary`, the max-below-max query uses two quick index lookups. `DENSE_RANK` over the whole table scans and sorts every row, which is fine for small tables but matters at scale.
- **SQL Server or MySQL syntax?** SQL Server uses `TOP` or `OFFSET ... FETCH`. MySQL supports `LIMIT 1 OFFSET 1` like PostgreSQL.

Further reading: [Q2 · Aggregation, GROUP BY, subqueries](../academy/lessons/Q2.md), [Q3 · Window functions](../academy/lessons/Q3.md), [Q4 · SQL interview patterns](../academy/lessons/Q4.md).
