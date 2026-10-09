**Short answer:** Opening a database connection is expensive: a TCP handshake, often TLS, authentication, and on PostgreSQL a new server process per connection. A connection pool opens a fixed set of connections once and lends them out; `close()` returns the connection to the pool instead of closing it. In Spring Boot, `DataSourceAutoConfiguration` creates a `HikariDataSource` (HikariCP is the default pool), and `JdbcTemplate`, JPA and the transaction manager all borrow connections from that `DataSource`.

## Explanation

**How a raw connection is established (PostgreSQL)**
1. The JDBC driver (found by `DriverManager` via `ServiceLoader`) parses the URL.
2. A TCP connection to port 5432, plus a TLS handshake if SSL is on.
3. A startup message with user and database; the server authenticates (SCRAM-SHA-256 is the default password method since PostgreSQL 14).
4. The postmaster forks a dedicated **backend process** for this connection.
5. Session parameters are set; the connection is ready.

That is several network round-trips and a process fork, often several milliseconds or more. Doing it per request wastes time and can exhaust the server's `max_connections`.

**What a pool does**
- Keeps up to `maximumPoolSize` open connections.
- `getConnection()` returns an idle one, or waits up to `connectionTimeout` and then throws.
- `close()` on the borrowed proxy resets its state (autoCommit, isolation, read-only) and returns it.
- Retires connections after `maxLifetime` so they are replaced before the database or a firewall drops them.

**Spring Boot classes involved**
- `DataSourceAutoConfiguration` + `DataSourceProperties` (`spring.datasource.*`) create the `DataSource`; HikariCP is chosen when it is on the classpath, which `spring-boot-starter-jdbc` and `spring-boot-starter-data-jpa` ensure.
- `HikariDataSource` / `HikariPool`: the pool. Tuned with `spring.datasource.hikari.*`.
- `JdbcTemplate` / `JdbcClient`: get and release connections through `DataSourceUtils`.
- `DataSourceTransactionManager` (JDBC) or `JpaTransactionManager` (JPA): on `@Transactional`, borrows one connection, binds it to the current thread via `TransactionSynchronizationManager`, and returns it at commit or rollback. That's how all repository calls in one transaction share one connection.
- With JPA, Hibernate gets connections from the same `DataSource`.

## Example

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/app
    username: app
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 10        # HikariCP default is 10
      connection-timeout: 3000     # ms to wait for a free connection (default 30000)
      max-lifetime: 1800000        # 30 min (default); keep below DB/firewall timeouts
      leak-detection-threshold: 20000   # warn if a connection is held > 20 s
  jpa:
    open-in-view: false            # don't hold a connection for the whole web request
```

```java
@Service
class OrderService {
    private final JdbcClient jdbc;
    OrderService(JdbcClient jdbc) { this.jdbc = jdbc; }

    @Transactional       // one pooled connection for both statements, same transaction
    void place(long orderId, long userId) {
        jdbc.sql("INSERT INTO orders(id, user_id) VALUES (?, ?)").params(orderId, userId).update();
        jdbc.sql("UPDATE users SET order_count = order_count + 1 WHERE id = ?").param(userId).update();
    }
}
```

## Pitfalls and follow-ups

- **Bigger pool = faster?** No. Past roughly the number of cores the database can use, more connections add contention. Start small and measure; HikariCP's wiki suggests about `cores * 2 + effective spindles` as a starting point for the database server.
- **Total connections:** pool size multiplied by the number of app instances must stay below PostgreSQL's `max_connections`. With many instances, use PgBouncer.
- **"Connection is not available, request timed out"** usually means connections are held too long: slow queries, remote HTTP calls inside `@Transactional`, open-in-view, or a leak.
- **Open Session in View:** `spring.jpa.open-in-view` is `true` by default in Spring Boot (it logs a warning). It keeps a connection-bound session through view rendering.
- **Virtual threads (Java 21):** they make threads cheap but not connections; the pool becomes the real limit, so size it deliberately.
- **Pool for different databases?** One `DataSource` per database, each with its own pool.

Related: [Q7 · Query performance and pooling](../academy/lessons/Q7.md), [D6 · JPA, Hibernate and @Transactional](../academy/lessons/D6.md), [D4 · Spring Boot auto-configuration](../academy/lessons/D4.md).
