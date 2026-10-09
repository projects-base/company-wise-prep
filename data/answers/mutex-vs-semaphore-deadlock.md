**Short answer:** A mutex gives one thread exclusive access to a critical section and has an owner: the thread that locked it must unlock it. A semaphore is a counter of permits with no owner: `acquire` takes a permit (blocking at zero), `release` adds one, and any thread may release. Use a mutex to protect shared state; use a semaphore to limit concurrency to N or to signal between threads. In a message queue, a mutex (or lock) guards the queue's internal structure, and semaphores or condition variables count free slots and available messages. Deadlock prevention means breaking one of the four Coffman conditions, most often with a global lock order or `tryLock` with a timeout.

## Explanation

| | Mutex / lock | Counting semaphore |
|---|---|---|
| Purpose | mutual exclusion | limit to N / signalling |
| Ownership | yes, owner unlocks | no, anyone can release |
| Reentrant | `ReentrantLock`, `synchronized` are | not reentrant (a second acquire takes another permit) |
| Java type | `synchronized`, `ReentrantLock` | `java.util.concurrent.Semaphore` |

A binary semaphore (1 permit) looks like a mutex but is not one: without ownership, a bug where another thread releases it goes unnoticed, and there is no priority inheritance in OSes that offer it for mutexes.

**Deadlock** needs all four: mutual exclusion, hold and wait, no preemption, circular wait. Prevention trade-offs:

- **Lock ordering** (break circular wait): always lock A before B, e.g. by id. Cheap at runtime, but needs discipline across the codebase.
- **Acquire all at once** (break hold and wait): take every lock you need up front or none. Lower concurrency.
- **`tryLock` with timeout and back off** (emulate preemption): no deadlock, but can livelock; add random back-off.
- **Fewer locks:** one lock per queue, lock-free structures, or message passing (single-writer). Simplest but may limit throughput.
- **Detection and recovery** instead of prevention (databases do this).

Also: never call foreign code (listeners, callbacks) while holding a lock, and keep critical sections short.

## Example

A bounded in-memory message queue for the LLD, with one lock and two conditions:

```java
public final class BoundedQueue<T> {
    private final Deque<T> items = new ArrayDeque<>();
    private final int capacity;
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition notFull = lock.newCondition();
    private final Condition notEmpty = lock.newCondition();

    public BoundedQueue(int capacity) { this.capacity = capacity; }

    public void publish(T msg) throws InterruptedException {
        lock.lock();
        try {
            while (items.size() == capacity) notFull.await();   // loop: spurious wakeups
            items.addLast(msg);
            notEmpty.signal();
        } finally { lock.unlock(); }
    }

    public T consume() throws InterruptedException {
        lock.lock();
        try {
            while (items.isEmpty()) notEmpty.await();
            T msg = items.removeFirst();
            notFull.signal();
            return msg;
        } finally { lock.unlock(); }
    }
}

// Semaphore use: cap concurrent deliveries to a slow subscriber
private final Semaphore inFlight = new Semaphore(10);
void deliver(Msg m) throws InterruptedException {
    inFlight.acquire();
    try { subscriber.handle(m); } finally { inFlight.release(); }
}
```

## Pitfalls and follow-ups

- **`if` instead of `while` around `await`:** breaks on spurious wakeups and when another consumer wins the race.
- **Release in `finally`:** otherwise an exception leaks a permit or leaves the lock held.
- **Starvation:** `new ReentrantLock(true)` / `new Semaphore(n, true)` are fair at the cost of throughput.
- **Why not just `ArrayBlockingQueue`?** In production, use it; the interview wants to see you can build it.
- **Per-topic locks:** if a publish touches two topics, lock them in a fixed order (e.g. by topic name).
- **Virtual threads (Java 21):** blocking in `synchronized` pins the carrier thread in Java 21-23; prefer `ReentrantLock` there. Java 24 removed most of that pinning.

Go deeper: [B2 · Locks and deadlock](../academy/lessons/B2.md), [B7 · Classic problems](../academy/lessons/B7.md).
