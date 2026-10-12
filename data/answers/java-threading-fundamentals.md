**Short answer:** A thread is an independent path of execution with its own stack, sharing the heap with other threads in the process. You rarely create threads directly in a service: you submit `Runnable`/`Callable` tasks to an `ExecutorService` and get `Future`s or `CompletableFuture`s back, and in Java 21 you can give each task its own cheap **virtual thread**. Because threads share memory, any shared mutable state needs coordination: `synchronized`/locks for atomicity, `volatile` or locks for visibility, and higher-level tools (`CountDownLatch`, `BlockingQueue`, concurrent collections) for coordination.

## Explanation

**Creating threads**
- `new Thread(runnable).start()`. Calling `run()` directly just runs the code on the current thread.
- Extending `Thread` works but mixes the task with the mechanism; prefer `Runnable`.
- `Callable<V>` returns a value and can throw checked exceptions.
- **Executors:** `Executors.newFixedThreadPool(n)`, or a `ThreadPoolExecutor` with a **bounded** queue and a rejection policy in production. Spring Boot gives you a configured `ThreadPoolTaskExecutor` for `@Async`.
- **Virtual threads (Java 21, JEP 444):** `Thread.ofVirtual().start(r)` or `Executors.newVirtualThreadPerTaskExecutor()`. Millions are possible because a blocked virtual thread unmounts from its carrier platform thread. Good for blocking I/O; no gain for CPU-bound work. In Spring Boot 3.2+, `spring.threads.virtual.enabled=true` runs request handling on them.

**Lifecycle (`Thread.State`)**: `NEW → RUNNABLE → (BLOCKED | WAITING | TIMED_WAITING) → TERMINATED`. `BLOCKED` means waiting for a monitor lock; `WAITING` means `wait()`, `join()` or `LockSupport.park()`.

**Coordination**
- `join()`: wait for a thread to finish.
- `wait()` / `notifyAll()`: low-level condition waiting. Must hold the object's monitor and must wait in a `while` loop because of spurious wakeups. Usually replaced by `java.util.concurrent` tools.
- `CountDownLatch` (wait for N events), `CyclicBarrier`, `Semaphore` (limit concurrency), `BlockingQueue` (producer-consumer), `CompletableFuture` (compose async results).
- **Interruption** is cooperative: `t.interrupt()` sets a flag; blocking methods throw `InterruptedException`. Either propagate it or restore the flag with `Thread.currentThread().interrupt()`. Never swallow it.

**Thread safety: the three problems**
1. **Atomicity:** `count++` is read-modify-write; two threads lose updates. Fix: lock or `AtomicInteger`.
2. **Visibility:** one thread's write may never be seen by another without a *happens-before* edge (lock release/acquire, `volatile`, thread start/join).
3. **Ordering:** the compiler and CPU reorder; the same happens-before rules fix it.

**Liveness problems:** deadlock (lock-order cycle), livelock, starvation. Avoid deadlock with a global lock order or `tryLock` with a timeout.

## Example

```java
ExecutorService pool = Executors.newFixedThreadPool(4);
try {
    List<Callable<Integer>> tasks = List.of(() -> price("A"), () -> price("B"), () -> price("C"));
    int total = 0;
    for (Future<Integer> f : pool.invokeAll(tasks)) {
        total += f.get();                       // waits; rethrows task failures as ExecutionException
    }
    System.out.println(total);
} finally {
    pool.shutdown();                            // always shut pools down
}

// Java 21: one virtual thread per task; close() waits for all tasks
try (var exec = Executors.newVirtualThreadPerTaskExecutor()) {
    List<Future<String>> results = ids.stream()
        .map(id -> exec.submit(() -> fetchFromRemote(id)))   // blocking I/O is fine here
        .toList();
}

// Producer-consumer without wait/notify
BlockingQueue<Order> queue = new ArrayBlockingQueue<>(100);   // bounded: back-pressure
Thread producer = Thread.ofPlatform().start(() -> {
    try { while (true) queue.put(nextOrder()); }              // blocks when full
    catch (InterruptedException e) { Thread.currentThread().interrupt(); }
});
Thread consumer = Thread.ofVirtual().start(() -> {
    try { while (true) process(queue.take()); }               // blocks when empty
    catch (InterruptedException e) { Thread.currentThread().interrupt(); }
});

// Shared counter
AtomicLong processed = new AtomicLong();
processed.incrementAndGet();
```

## Pitfalls and follow-ups

- **`start()` vs `run()`:** `start()` creates a new thread that calls `run()`; calling `run()` yourself does not. Starting a thread twice throws `IllegalThreadStateException`.
- **`sleep` vs `wait`:** `sleep` keeps any locks held; `wait` releases the monitor and must be called while holding it.
- **`Runnable` vs `Callable`:** no result and no checked exceptions vs a result and checked exceptions.
- **Daemon threads** don't keep the JVM alive; virtual threads are always daemon.
- **How big should a pool be?** CPU-bound: about the number of cores. I/O-bound: more, roughly `cores × (1 + wait time / compute time)`, or use virtual threads.
- **Unbounded queues** (`newFixedThreadPool` uses one) can hide overload until you run out of memory; bound them in production.
- **Exceptions in `execute()` tasks** go to the thread's uncaught exception handler; in `submit()` they are captured in the `Future` and lost if nobody calls `get()`.
- **Virtual thread pinning (Java 21):** blocking inside `synchronized` or native code pins the carrier; JDK 24 (JEP 491) fixed the `synchronized` case. Don't pool virtual threads, and use a `Semaphore` to limit access to scarce resources such as DB connections.
- **`ThreadLocal`** works with virtual threads but millions of copies cost memory; scoped values are the newer alternative (preview in Java 21).

Related: [B1 · Threads](../academy/lessons/B1.md), [B2 · Locks](../academy/lessons/B2.md), [B4 · Executors and sizing thread pools](../academy/lessons/B4.md), [B6 · Virtual threads](../academy/lessons/B6.md), [L3 · Thread lifecycle](../academy/lessons/L3.md), [A6 · The Java Memory Model](../academy/lessons/A6.md).
