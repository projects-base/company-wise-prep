**Short answer:** A class is thread-safe when its invariants hold however threads interleave. In order of preference: make it **immutable** (no shared mutable state), **confine** state to one thread, use **thread-safe building blocks** (`AtomicLong`, `ConcurrentHashMap`), and only then **lock**: guard every access to the shared fields with the same lock, using `synchronized` or a `ReentrantLock`, keep the state `private`, and never let the lock or the state escape. `synchronized` gives both mutual exclusion and visibility.

## Explanation

**Step 1: find the state and the invariants.** Which fields are shared and mutable? Which must change together (for example `balance` and `history`)? Fields in one invariant must be guarded by **one** lock, updated in one critical section.

**Step 2: pick a technique.**

| Technique | Use when | Note |
|---|---|---|
| Immutability (`final` fields, records) | Value objects, config | Thread-safe for free, safely published by `final` semantics |
| Thread confinement | Per-request state, `ThreadLocal` | No sharing, no locks |
| Atomics (`AtomicInteger`, `LongAdder`) | One independent variable | Lock-free CAS; `LongAdder` for hot counters |
| Concurrent collections | Shared maps/queues | `ConcurrentHashMap.compute`/`merge` are atomic per key |
| `synchronized` | Several fields, one invariant | Simple, re-entrant, released automatically |
| `ReentrantLock` | Need `tryLock`, timeouts, interruptible locking, several conditions, fairness | Must `unlock()` in `finally` |
| `ReadWriteLock` / `StampedLock` | Many readers, rare writers | More complex; measure first |

**Step 3: apply the rules.**
- **All** accesses to guarded fields, reads included, go through the lock. Unlocked reads can see stale or half-updated state.
- Prefer a **private final lock object** to `synchronized` on `this`. Outside code can lock on your instance and cause deadlocks or contention.
- Static fields are guarded by a static lock (`synchronized static` locks the `Class` object).
- Keep critical sections short. No I/O or remote calls inside a lock.
- Don't leak `this` from the constructor (no starting threads or registering listeners there).
- Return copies or unmodifiable views, never the internal mutable collection.
- Document it: `// @GuardedBy("lock")`.

**`synchronized` method vs block:** a synchronized method locks `this` (or the `Class` for static) for the whole body. A block lets you choose the lock and lock only the part that needs it.

## Example

```java
public final class BankAccount {
    private final Object lock = new Object();
    private long balance;                          // guarded by lock
    private final List<String> history = new ArrayList<>();   // guarded by lock

    public void deposit(long amount) {
        if (amount <= 0) throw new IllegalArgumentException();
        synchronized (lock) {
            balance += amount;
            history.add("D " + amount);            // same lock: invariant kept together
        }
    }

    public boolean withdraw(long amount) {
        synchronized (lock) {                      // check-then-act must be atomic
            if (balance < amount) return false;
            balance -= amount;
            history.add("W " + amount);
            return true;
        }
    }

    public long balance() {
        synchronized (lock) { return balance; }    // reads need the lock too (visibility)
    }

    public List<String> history() {
        synchronized (lock) { return List.copyOf(history); }   // don't leak internals
    }
}
```

The same with `ReentrantLock`, when you need a timeout:

```java
private final ReentrantLock lock = new ReentrantLock();

public boolean withdraw(long amount) throws InterruptedException {
    if (!lock.tryLock(100, TimeUnit.MILLISECONDS)) return false;
    try {
        if (balance < amount) return false;
        balance -= amount;
        return true;
    } finally {
        lock.unlock();
    }
}
```

A single counter needs no lock at all: `private final AtomicLong hits = new AtomicLong(); hits.incrementAndGet();`.

## Pitfalls and follow-ups

- **Transferring between two accounts:** lock both, always in a fixed global order (for example by account id), or you can deadlock.
- **Is `volatile` enough?** Only for one variable whose new value doesn't depend on the old one (a flag). Not for `count++` or multi-field invariants.
- **`Collections.synchronizedMap` vs `ConcurrentHashMap`:** the first locks the whole map per call and still needs external locking for compound actions; the second has fine-grained locking and atomic `compute`/`putIfAbsent`.
- **Virtual threads (Java 21):** blocking inside `synchronized` pins the carrier thread; prefer `ReentrantLock` around blocking calls on Java 21. JDK 24 (JEP 491) removed most pinning.
- **Spring beans:** singletons are shared by all request threads. Keep them stateless; put per-request data in method locals.
- **In a cluster,** a JVM lock protects nothing across instances. Use a database row lock, optimistic versioning or a distributed lock.

Related: [B2 · Locks](../academy/lessons/B2.md), [B3 · Atomics and CAS](../academy/lessons/B3.md), [A6 · The Java Memory Model](../academy/lessons/A6.md).
