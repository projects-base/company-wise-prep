**Short answer:** `JdbcTemplate` is Spring's thin wrapper over plain JDBC. You write the SQL; it handles the boilerplate: getting a connection from the `DataSource`, creating and closing the `PreparedStatement` and `ResultSet`, binding parameters, joining the current Spring transaction, and translating `SQLException` into Spring's unchecked `DataAccessException` hierarchy. You supply a `RowMapper` to turn each row into an object. Spring Boot auto-configures it when a `DataSource` is present, and since Spring Framework 6.1 / Boot 3.2 there is also the fluent `JdbcClient` on top of it.

## Explanation

**Why it exists.** Raw JDBC needs about 20 lines per query: `getConnection`, `prepareStatement`, set parameters, `executeQuery`, loop, close everything in `finally`, wrap the checked `SQLException`. `JdbcTemplate` is the **Template Method** pattern: it owns that fixed sequence and calls back into your code (`RowMapper`, `PreparedStatementSetter`) for the part that varies.

**Main methods**

| Need | Method |
|---|---|
| List of objects | `query(sql, rowMapper, args...)` |
| Exactly one row | `queryForObject(sql, rowMapper, args...)` (throws `EmptyResultDataAccessException` on 0 rows, `IncorrectResultSizeDataAccessException` on more than 1) |
| Single value | `queryForObject("select count(*) ...", Long.class)` |
| Insert / update / delete | `update(sql, args...)` returns rows affected |
| Batch | `batchUpdate(sql, List<Object[]>)` |
| Generated key | `update(PreparedStatementCreator, KeyHolder)` |
| DDL / anything else | `execute(sql)` |

**Related classes**
- `NamedParameterJdbcTemplate`: `:name` placeholders instead of `?`, with a `Map` or `SqlParameterSource`. Much more readable for long queries.
- `JdbcClient` (Spring 6.1+): one fluent API for both styles: `jdbcClient.sql(...).param(...).query(Bet.class).list()`.
- `DataClassRowMapper` maps columns to a record's constructor (`snake_case` columns to `camelCase` components); `BeanPropertyRowMapper` uses setters.
- `SimpleJdbcInsert` builds inserts from table metadata.

**Transactions.** `JdbcTemplate` gets its connection through `DataSourceUtils`, so inside a `@Transactional` method all calls share the same connection and commit or roll back together. Outside a transaction each call auto-commits.

**Exceptions.** Vendor error codes and SQL states are translated: a unique-constraint violation becomes `DuplicateKeyException`, a deadlock becomes a `PessimisticLockingFailureException` (or a subclass). They are unchecked, so service code isn't littered with `try/catch`.

**When to choose it over JPA:** reporting queries, bulk operations, complex SQL (window functions, CTEs), or when you want full control and no ORM surprises (lazy loading, N+1, dirty checking). Many services use both.

## Example

```java
public record Bet(long id, long userId, BigDecimal stake, String status) {}

@Repository
public class BetRepository {
    private final JdbcTemplate jdbc;
    private final NamedParameterJdbcTemplate named;

    public BetRepository(JdbcTemplate jdbc, NamedParameterJdbcTemplate named) {  // auto-configured
        this.jdbc = jdbc;
        this.named = named;
    }

    private static final RowMapper<Bet> MAPPER = (rs, rowNum) -> new Bet(
        rs.getLong("id"), rs.getLong("user_id"), rs.getBigDecimal("stake"), rs.getString("status"));

    public List<Bet> openBets(long userId) {
        return jdbc.query("select id, user_id, stake, status from bet where user_id = ? and status = 'OPEN'",
                          MAPPER, userId);                // parameters bound: no SQL injection
    }

    public Optional<Bet> find(long id) {
        return jdbc.query("select id, user_id, stake, status from bet where id = ?", MAPPER, id)
                   .stream().findFirst();                 // avoids EmptyResultDataAccessException
    }

    public int settle(long id, String status) {
        return named.update("update bet set status = :status where id = :id and status = 'OPEN'",
                            Map.of("status", status, "id", id));
    }

    public long place(long userId, BigDecimal stake) {
        KeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                "insert into bet (user_id, stake, status) values (?, ?, 'OPEN')", new String[]{"id"});
            ps.setLong(1, userId);
            ps.setBigDecimal(2, stake);
            return ps;
        }, keys);
        return keys.getKey().longValue();
    }
}
```

The same lookup with `JdbcClient`:

```java
Optional<Bet> bet = jdbcClient.sql("select id, user_id, stake, status from bet where id = :id")
                              .param("id", id)
                              .query(Bet.class)            // maps columns to the record
                              .optional();
```

## Pitfalls and follow-ups

- **Is `JdbcTemplate` thread-safe?** Yes, once configured. Create one per `DataSource` and share it as a singleton bean.
- **SQL injection:** always use `?` or named parameters; never concatenate user input into SQL.
- **`queryForObject` with no rows** throws; use `query(...).stream().findFirst()` or `JdbcClient.optional()`.
- **Large result sets:** use `query(sql, RowCallbackHandler)` or `queryForStream` (close the stream) with `setFetchSize`, instead of loading everything into a list.
- **Batch inserts:** `batchUpdate` sends one round trip per batch. With PostgreSQL, adding `reWriteBatchedInserts=true` to the JDBC URL rewrites them into multi-row inserts for more speed.
- **Connection pool:** Boot uses HikariCP by default; pool size, not `JdbcTemplate`, usually limits throughput.
- **Testing:** `@JdbcTest` slice with Testcontainers PostgreSQL.

Related: [Q7 · Query performance in practice](../academy/lessons/Q7.md), [D6 · Data: JPA, Hibernate and @Transactional](../academy/lessons/D6.md).
