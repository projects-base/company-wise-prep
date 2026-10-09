**Short answer:** Producers put items into a shared bounded buffer and consumers take them out. Three rules must hold: only one thread changes the buffer at a time (mutual exclusion), producers wait when it is full, and consumers wait when it is empty, without busy-waiting. The classic solution uses a mutex plus two counting semaphores, `empty` (free slots, starts at N) and `full` (filled slots, starts at 0). In Java you would use a lock with two conditions, or simply a `BlockingQueue`.

## Explanation

**Semaphore version (textbook)**

```text
semaphore empty = N, full = 0;  mutex m;

producer:                    consumer:
  item = produce()             wait(full)      // block if nothing to take
  wait(empty)                  lock(m)
  lock(m)                      item = buffer.remove()
  buffer.add(item)             unlock(m)
  unlock(m)                    signal(empty)
  signal(full)                 consume(item)
```

**Order matters.** If a producer locked `m` *before* `wait(empty)` on a full buffer, it would sleep holding the mutex; no consumer could enter to free a slot, and everything deadlocks. Always wait on the counting semaphore first, then take the mutex.

**Condition-variable version.** One lock, two conditions (`notFull`, `notEmpty`). Waiting releases the lock atomically. Always re-check the condition in a `while` loop because of spurious wake-ups and because another thread may have taken the slot first.

**Design points to mention in 10-15 minutes**
- *Bounded* buffer gives back-pressure: a fast producer slows down instead of exhausting memory.
- Ring buffer (array + head/tail indexes) avoids allocation.
- Multiple producers and consumers work with the same scheme.
- Shutdown: a "poison pill" item per consumer, or an interrupt.
- Throughput: separate locks for head and tail (as `LinkedBlockingQueue` does) let one producer and one consumer proceed in parallel.
- Single-producer single-consumer can be lock-free with two atomic indexes.

## Example

```java
// Production-grade: let the JDK do it
BlockingQueue<Order> queue = new ArrayBlockingQueue<>(1_000);

Runnable producer = () -> {
    try {
        while (running) queue.put(nextOrder());      // blocks when full
        queue.put(Order.POISON);
    } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
};

Runnable consumer = () -> {
    try {
        for (Order o; (o = queue.take()) != Order.POISON; ) process(o); // blocks when empty
    } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
};

try (var exec = Executors.newVirtualThreadPerTaskExecutor()) {   // Java 21
    exec.submit(producer);
    exec.submit(consumer);
}
```

A hand-built version with `ReentrantLock` and two `Condition`s is in [B7 · Classic problems](../academy/lessons/B7.md).

## Pitfalls and follow-ups

- **`notify()` vs `notifyAll()` with `synchronized`:** with a single wait set shared by producers and consumers, `notify()` can wake the wrong kind of thread and stall; use `notifyAll()` or separate `Condition`s.
- **Why not busy-wait?** It burns CPU and, without proper memory ordering, may never see the update.
- **Unbounded queue?** No producer blocking, but memory grows without limit when consumers fall behind.
- **Across processes or machines?** The same idea is a message broker (Kafka, RabbitMQ); bounding becomes consumer lag and back-pressure.
- **Multiple consumers and ordering:** with several consumers, global processing order is lost; partition by key if per-key order matters.

Go deeper: [B7 · Classic problems: producer-consumer, bounded queue](../academy/lessons/B7.md), [B2 · Locks](../academy/lessons/B2.md).
