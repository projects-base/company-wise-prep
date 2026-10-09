**Short answer:** It mixes three jobs (database access, sending email, updating state) in one method with no error handling, so it leaks connections, double-sends after a crash, and holds a database connection while it waits on a slow mail server. `List[]` is also a raw array of lists, almost certainly meant to be `List<User>`. I would select pending users in pages, send each email outside any transaction, mark sent users with a batched update, and make the whole thing idempotent so a retry never emails a user twice.

## Explanation

Problems, roughly in order of severity:

1. **Resource handling.** "Create DB connection" inside the method suggests `DriverManager.getConnection` with no pool and no `close`. Any exception leaks the connection. Use a pooled `DataSource` (HikariCP in Spring Boot) and try-with-resources, or `JdbcTemplate`, which closes resources for you.
2. **Order and atomicity.** Send all, then update all: if the process dies after sending 500 emails, none are marked, and a rerun sends all 500 again. If you update first and then the mail fails, users are marked but never emailed.
3. **Long transaction around external I/O.** If the loop runs inside one transaction, the connection and row locks are held for minutes while SMTP calls run.
4. **One update per user.** N round trips. Use JDBC batching or a single `UPDATE` over an array of IDs.
5. **No failure isolation.** One bad address throws and aborts everyone after it.
6. **Synchronous.** The caller (maybe an HTTP request) waits for every email.
7. **Typing and naming.** `List[] users` should be `List<User>`. A `'Yes'` string flag should be a status or a `sent_at` timestamp.

## Example

```java
@Service
public class MailNotificationJob {
    private final JdbcTemplate jdbc;
    private final MailSender mail;

    @Scheduled(fixedDelay = 60_000)
    public void run() {
        List<User> batch;
        while (!(batch = claimBatch(100)).isEmpty()) {
            List<Long> sent = new ArrayList<>();
            for (User u : batch) {
                try {
                    mail.send(u.email(), "...", idempotencyKey(u));  // outside any DB transaction
                    sent.add(u.id());
                } catch (MailException e) {
                    log.warn("mail failed for user {}", u.id(), e);   // others still go out
                }
            }
            markSent(sent);
        }
    }

    // Claim rows atomically so two instances never pick the same user
    List<User> claimBatch(int size) {
        return jdbc.query("""
            UPDATE users SET email_status = 'SENDING', claimed_at = now()
            WHERE id IN (SELECT id FROM users WHERE email_status = 'PENDING'
                         ORDER BY id LIMIT ? FOR UPDATE SKIP LOCKED)
            RETURNING id, email""", userMapper, size);
    }

    void markSent(List<Long> ids) {
        if (ids.isEmpty()) return;
        jdbc.batchUpdate("UPDATE users SET email_status = 'SENT', sent_at = now() WHERE id = ?",
                         ids, 100, (ps, id) -> ps.setLong(1, id));   // one round trip per batch
    }
}
```

## Pitfalls and follow-ups

- **Connection pooling and closing resources?** Opening a connection costs a TCP handshake, authentication and a new PostgreSQL backend process. A pool reuses them. Always close (return) the connection in `finally` or try-with-resources.
- **Batch updates; transaction boundaries vs the mail call?** Keep each transaction short and never put a network call to a third party inside one. Pattern: short transaction to claim, send outside, short transaction to mark sent. Batch the mark with `JdbcTemplate.batchUpdate`, or one `UPDATE ... WHERE id = ANY(array)`.
- **Async and idempotent so a crash does not double-send?** Write a row to a `notification_outbox` table in the business transaction and let a worker (or a Kafka consumer) send it. Give each email a unique key (`user_id + campaign_id`). The worker checks status before sending, and the mail provider deduplicates on the key if it supports one. Rows stuck in `SENDING` past a timeout are retried. You cannot get exactly-once with an external mail server; at-least-once plus deduplication is the honest answer.
- **Multiple instances?** `FOR UPDATE SKIP LOCKED` lets several workers claim different rows without blocking each other.
- **Large user lists?** Page through with keyset pagination (`WHERE id > :last ORDER BY id LIMIT 100`), never load everything into memory.

Further reading: [Q7 · Query performance in practice](../academy/lessons/Q7.md), [D6 · Data: JPA, Hibernate and @Transactional](../academy/lessons/D6.md), [Q6 · Transactions, isolation, locking](../academy/lessons/Q6.md).
