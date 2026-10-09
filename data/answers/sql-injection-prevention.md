**Short answer:** Never build SQL by concatenating user input. Use parameterised queries (JDBC `PreparedStatement`, JPA named parameters, Spring `JdbcTemplate` with `?` or `:name`), so the database receives the SQL text and the values separately and a value can never become code. Where parameters cannot be used, such as column names in `ORDER BY` or table names, validate against an allow-list. Add defence in depth: a least-privilege database user, input validation, and not leaking SQL errors to clients.

## Explanation

**How injection works.** With `"SELECT * FROM users WHERE name = '" + name + "'"`, an input of `' OR '1'='1` changes the query's structure. With a parameter, the driver sends `WHERE name = $1` plus the value; the parser has already fixed the structure, so the input is only ever data.

**Where people still get caught**
- **Dynamic identifiers:** `ORDER BY` column, sort direction, table names cannot be bound. Map user input to a fixed set (`"price" -> "p.price"`).
- **`LIKE` patterns:** bind the value, and escape `%` and `_` if users should not use wildcards.
- **`IN` lists:** bind a list (`IN (:ids)` in JPA / `NamedParameterJdbcTemplate`) or use `= ANY(?)` with an array in PostgreSQL; do not join strings.
- **JPQL/HQL concatenation** is injectable too (JPQL injection). So is building a native query string in `@Query(nativeQuery = true)` via SpEL with raw values.
- **Stored procedures** that build dynamic SQL internally (`EXECUTE 'SELECT ... ' || param` in PL/pgSQL) need `format()` with `%I`/`%L` or `EXECUTE ... USING`.
- **Second-order injection:** data stored safely, later read and concatenated into another query.

**Defence in depth**
- The app's DB user has only the privileges it needs (no DDL, no superuser).
- Validate types and formats at the API boundary (`@Valid`, `@Pattern`).
- Generic error messages; log details server-side.
- Escaping functions are a last resort, not the main defence.

## Example

```java
// Vulnerable
String sql = "SELECT * FROM orders WHERE customer_id = '" + id + "'";
jdbcTemplate.queryForList(sql);

// JDBC
try (PreparedStatement ps = conn.prepareStatement(
        "SELECT * FROM orders WHERE customer_id = ?")) {
    ps.setString(1, id);
    ResultSet rs = ps.executeQuery();
}

// Spring Data JPA
@Query("select o from Order o where o.customerId = :id")
List<Order> findByCustomer(@Param("id") String id);

// Allow-list for ORDER BY
private static final Map<String, String> SORT = Map.of(
    "date", "o.created_at", "amount", "o.amount");
String col = SORT.getOrDefault(sortParam, "o.created_at");
String dir = "desc".equalsIgnoreCase(dirParam) ? "DESC" : "ASC";
String sql = "SELECT * FROM orders o WHERE o.customer_id = ? ORDER BY " + col + " " + dir;
```

## Pitfalls and follow-ups

- **Does an ORM make you immune?** Only when you use its parameter binding; string-built JPQL or native SQL is still vulnerable.
- **Is input sanitising (stripping quotes) enough?** No; encodings and edge cases break blacklists. Parameterise.
- **Prepared statement vs parameterised query?** Same protection; prepared statements may also be cached and reused by the driver/server.
- **Spring Data derived queries** (`findByName`) and the Criteria API bind parameters for you.
- **Related attacks:** NoSQL injection, LDAP injection, and command injection follow the same rule: keep code and data separate.

Go deeper: [Q1 · SQL basics](../academy/lessons/Q1.md), [D6 · JPA, Hibernate and @Transactional](../academy/lessons/D6.md), [D7 · Spring Security](../academy/lessons/D7.md).
