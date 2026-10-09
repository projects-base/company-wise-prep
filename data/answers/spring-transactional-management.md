**Short answer:** I put `@Transactional` on service methods, and Spring wraps the bean in a proxy. When a call comes in through the proxy, a `TransactionInterceptor` asks the `PlatformTransactionManager` (with JPA, `JpaTransactionManager`) to begin a transaction, binds the connection and `EntityManager` to the current thread, runs my method, then commits, or rolls back if a `RuntimeException` or `Error` escapes. Because it is proxy-based, it only works on calls that come from outside the bean.

## Explanation

**What happens on a call:**

1. Caller → proxy (JDK dynamic proxy or CGLIB subclass).
2. The interceptor reads the attributes: propagation, isolation, timeout, `readOnly`, rollback rules.
3. The transaction manager gets a connection, sets `autoCommit=false`, and binds it to the thread (a `ThreadLocal` via `TransactionSynchronizationManager`). Repositories called inside reuse that same connection.
4. Your method runs. JPA tracks changes to managed entities (dirty checking).
5. On normal return: flush, then commit. On a `RuntimeException`/`Error`: roll back. On a checked exception: **commit** by default.

**Propagation (follow-up):**

- `REQUIRED` (default): join the current transaction, or start one if none exists. If an inner `REQUIRED` method throws, the shared transaction is marked rollback-only; even if the outer method catches the exception, the commit fails with `UnexpectedRollbackException`.
- `REQUIRES_NEW`: suspend the outer transaction and run in a fresh one that commits or rolls back on its own. Good for audit logs that must survive a business failure. It uses a second connection, so watch pool size.
- Others: `SUPPORTS`, `MANDATORY`, `NOT_SUPPORTED`, `NEVER`, `NESTED` (savepoint in the same transaction, JDBC only).

**Rollback rules:** checked exceptions commit by default (a design inherited from EJB). Use `@Transactional(rollbackFor = Exception.class)` or a specific checked class, and `noRollbackFor` for the opposite.

## Example

```java
@Service
@RequiredArgsConstructor
public class TransferService {
    private final AccountRepository accounts;
    private final AuditService audit;

    @Transactional(rollbackFor = InsufficientFundsException.class)
    public void transfer(long fromId, long toId, BigDecimal amt) throws InsufficientFundsException {
        Account from = accounts.findById(fromId).orElseThrow();
        Account to = accounts.findById(toId).orElseThrow();
        if (from.getBalance().compareTo(amt) < 0) throw new InsufficientFundsException();
        from.debit(amt);
        to.credit(amt);            // no save() needed: dirty checking flushes on commit
        audit.record(fromId, toId, amt);
    }
}

@Service
class AuditService {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(long from, long to, BigDecimal amt) { /* insert audit row */ }
}
```

## Pitfalls and follow-ups

- **Self-invocation:** `this.otherMethod()` from inside the same bean skips the proxy, so `otherMethod`'s `@Transactional` is ignored. Fix: move it to another bean (cleanest), use `TransactionTemplate`, or inject the bean's own proxy.
- **Private methods** are never intercepted. Put `@Transactional` on public service methods.
- **Swallowing the exception** inside the method means nothing escapes, so the transaction commits.
- **`readOnly = true`** for queries: Hibernate skips dirty checking and flushing, and some drivers route to a replica. It is a hint, not a lock.
- **Keep transactions short.** No HTTP calls or Kafka sends inside; a slow remote call holds a DB connection and locks. For "save then publish", use the outbox pattern.
- **Where to put it?** On the service layer, not controllers. Spring Data repository methods are already transactional by default.
- **Isolation:** default is the database's (PostgreSQL: Read Committed). Raise it with `isolation = Isolation.REPEATABLE_READ` only when you need it.
- **Programmatic option:** `TransactionTemplate.execute(status -> ...)` when you need a transaction around just part of a method.

Deeper: [D6 · Data: JPA, Hibernate and @Transactional](../academy/lessons/D6.md), [D3 · Spring AOP and the proxy traps](../academy/lessons/D3.md), [Q6 · Transactions, isolation, locking and MVCC](../academy/lessons/Q6.md).
