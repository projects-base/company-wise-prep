**Short answer:** In a JDBC snippet I check four things first: resources (`Connection`, `Statement`, `ResultSet`) must be closed on every path, so use try-with-resources; SQL must use `PreparedStatement` parameters, never string concatenation; errors must not be swallowed and multi-statement work needs a transaction with rollback; and connections should come from a pooled `DataSource`, not `DriverManager` per call, with no hard-coded credentials. Then I look at correctness details like `rs.next()` handling and nulls.

## Explanation

The exact snippet was not reported, so here is a typical one with the issues interviewers plant.

```java
// BEFORE
public User findUser(String email) {
    try {
        Class.forName("org.postgresql.Driver");
        Connection con = DriverManager.getConnection(
            "jdbc:postgresql://db:5432/app", "admin", "secret");
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(
            "SELECT id, name FROM users WHERE email = '" + email + "'");
        rs.next();
        User u = new User(rs.getInt("id"), rs.getString("name"));
        con.close();
        return u;
    } catch (Exception e) {
        e.printStackTrace();
        return null;
    }
}
```

| # | Problem | Why it matters |
|---|---|---|
| 1 | String-concatenated SQL | SQL injection; also no statement reuse |
| 2 | `close()` only on the happy path; `Statement`/`ResultSet` never closed | Any exception leaks a connection; the pool or database runs out |
| 3 | `DriverManager` per call | A new TCP + auth handshake each time; no limit on connections |
| 4 | Hard-coded URL and credentials | Security, and can't change per environment |
| 5 | `rs.next()` result ignored | No row: `getInt` throws `SQLException` instead of "not found" |
| 6 | `catch (Exception)` + `printStackTrace` + `return null` | Hides failures; callers can't tell "not found" from "database down" |
| 7 | `Class.forName` | Unneeded since JDBC 4 (drivers auto-register via `ServiceLoader`) |
| 8 | No query timeout | A slow query can hold a thread and a connection indefinitely |

## Example

```java
// AFTER
private final DataSource dataSource;   // pooled (HikariCP), configured outside the code

public Optional<User> findUser(String email) {
    String sql = "SELECT id, name FROM users WHERE email = ?";
    try (Connection con = dataSource.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
        ps.setString(1, email);
        ps.setQueryTimeout(5);
        try (ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) return Optional.empty();
            return Optional.of(new User(rs.getLong("id"), rs.getString("name")));
        }
    } catch (SQLException e) {
        throw new DataAccessFailure("findUser failed", e);   // keep the cause; don't log the email
    }
}
```

Try-with-resources closes in reverse order (`rs`, `ps`, `con`) even on exceptions, and adds any close failures as suppressed exceptions. With a pool, `con.close()` returns the connection to the pool rather than closing the socket.

For **writes across several statements**, wrap them in a transaction:

```java
try (Connection con = dataSource.getConnection()) {
    con.setAutoCommit(false);
    try {
        debit(con, from, amount);
        credit(con, to, amount);
        con.commit();
    } catch (SQLException e) {
        con.rollback();
        throw e;
    }
}
```

## Pitfalls and follow-ups

- **In Spring Boot** you would use `JdbcTemplate` or `JdbcClient` (Spring 6.1+), which handle closing and translate `SQLException` into `DataAccessException`, and `@Transactional` instead of manual commit and rollback.
- **Pool connections and autoCommit:** if you change `autoCommit` or isolation, the pool (HikariCP) resets them when the connection is returned, but don't rely on leaving a transaction open.
- **Large results:** set `fetchSize`; the PostgreSQL driver only streams with `autoCommit=false` and a fetch size set, otherwise it loads all rows into memory.
- **Logging:** don't log full SQL with user data or credentials.
- **Batch inserts:** `addBatch` / `executeBatch` instead of one round-trip per row.
- **Testing:** Testcontainers with a real PostgreSQL rather than mocking JDBC.

Related: [Q7 · Query performance: N+1, pagination, batching, pooling](../academy/lessons/Q7.md), [D6 · JPA, Hibernate and @Transactional](../academy/lessons/D6.md).
