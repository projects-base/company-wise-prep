**Short answer:** Choose by how often writers really collide and what a retry costs. Pessimistic locking makes the second writer wait, so it fits hot rows and work you cannot repeat; optimistic locking lets both run and makes the loser retry, so it fits rare collisions and edits that span user think time. If I rebuilt it I would measure the conflict rate first, and for a simple counter I would use neither: one conditional `UPDATE` is shorter and safer.

## Explanation

**How each one works**
- **Pessimistic:** `SELECT ... FOR UPDATE` locks the row until commit. The next locker waits, then reads the committed value.
- **Optimistic:** read `version`, then `UPDATE ... SET version = version + 1 WHERE id = ? AND version = ?`. Zero updated rows: someone was faster.
- **Conditional update:** the check and the write are one statement. The row lock lasts only until that short transaction commits.

**How to decide**

| Question | Pessimistic | Optimistic |
|---|---|---|
| Collisions on the same row? | Often | Rare |
| Is a retry cheap and safe? | No | Yes |
| Short transaction, one row? | Yes | Either |
| User edits a form for minutes? | Never hold a lock that long | Yes, version in the form or an `ETag` |
| Main danger | Waiting, deadlock, pool exhaustion | Retry storm on a hot row |

Example: 50 writers hit one row. With optimistic locking one wins per round and the rest redo their work; with a lock they queue and each does the work once.

**A template for your project** (fill in what is true, and keep it short):

> "Two [requests] could both [read the last place and write]. Conflicts on one [row] were [common, measured as ...], and a retry meant [repeating a slow or external step], so a lock was cheaper than redoing it. The lock covered [one row], the transaction was [n] ms, and we set [a lock timeout]. Today I would [first measure], and probably [use a conditional update / move the external call out of the transaction]."

**What to say you would change**
- Measure lock wait time and conflict rate before choosing.
- Move any HTTP or message call out of the locked section.
- Add a lock timeout and a fixed lock order.
- Add a concurrency test that fails without the lock.
- Keep a constraint (`CHECK (booked <= capacity)`) as the last guard, whichever lock you use.

## Example

```java
import jakarta.persistence.*;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

@Entity
class Slot {
    @Id Long id;
    int capacity;
    int booked;
    @Version long version;                                     // used only by the optimistic version

    void takePlace() {
        if (booked >= capacity) throw new IllegalStateException("slot is full");
        booked++;
    }
}

interface SlotRepository extends JpaRepository<Slot, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)                      // SELECT ... FOR UPDATE
    @Query("select s from Slot s where s.id = :id")
    Optional<Slot> findForUpdate(@Param("id") long id);

    @Modifying
    @Query("update Slot s set s.booked = s.booked + 1 where s.id = :id and s.booked < s.capacity")
    int takePlace(@Param("id") long id);                       // 1 = got it, 0 = full
}
```

```java
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

class SlotBooking {
    private final SlotRepository slots;
    private final TransactionTemplate tx;

    SlotBooking(SlotRepository slots, TransactionTemplate tx) {
        this.slots = slots;
        this.tx = tx;
    }

    @Transactional
    public void bookPessimistic(long id) {
        slots.findForUpdate(id).orElseThrow().takePlace();     // others wait here until we commit
    }

    public void bookOptimistic(long id) throws InterruptedException {
        for (int attempt = 1; ; attempt++) {
            try {
                tx.executeWithoutResult(s -> slots.findById(id).orElseThrow().takePlace());   // new transaction per attempt
                return;
            } catch (OptimisticLockingFailureException e) {    // ... WHERE id = ? AND version = ? matched 0 rows
                if (attempt == 3) throw e;
                Thread.sleep(ThreadLocalRandom.current().nextLong(20, 100));
            }
        }
    }
}
```

I ran all three on Spring Boot 3.5 with H2: 40 callers, 5 places, never more than 5 booked. In real code `SlotBooking` is a `@Service`.

## Pitfalls and follow-ups

- **Where does the retry go?** Outside the transaction. Retrying inside `@Transactional` reuses a transaction already marked for rollback and a stale persistence context.
- **Can optimistic locking deadlock?** Not while reading, but its UPDATEs still take row locks. Two transactions updating rows A and B in opposite order can deadlock.
- **Lock timeout on PostgreSQL?** Use `SET LOCAL lock_timeout = '2s'`. `NOWAIT` fails at once, and `SKIP LOCKED` suits job queues.
- **Bulk JPQL updates** skip the `@Version` check and leave loaded entities stale. Do not mix them carelessly with the optimistic version.
- **Does `SERIALIZABLE` replace this?** In PostgreSQL it detects conflicts and aborts one transaction (SQLSTATE 40001), so you still need retries.
- **Locking a row that does not exist yet?** `FOR UPDATE` locks nothing. Use a unique constraint, or lock the parent row.
- **`synchronized` instead?** Works in one JVM only, and the lock can be released before the transaction commits.
- **Redis lock instead?** Only for work outside the database. It can expire while you still work, so add a fencing token.
- **Many rows?** Lock in a fixed order, for example by id.

Go deeper: [Q6 · Transactions, isolation, locking and MVCC](../academy/lessons/Q6.md), [L7 · JPA entity and transaction lifecycle](../academy/lessons/L7.md).
