**Short answer:** The Java Memory Model (JMM) defines the guarantees; the JVM maps them onto each CPU's memory model with barriers. `volatile` gives visibility and ordering for one variable: a write to it *happens-before* every later read of it, and the compiler and CPU can't move ordinary accesses across it in the wrong direction. It does **not** make compound operations like `count++` atomic. `synchronized` gives mutual exclusion plus the same visibility: unlocking a monitor happens-before the next lock of the same monitor.

## Explanation

**Why visibility breaks at all.** CPU caches are kept coherent by hardware protocols (MESI and its variants), so the problem is not "stale cache lines" by themselves. The real causes are:
1. **Compiler and JIT optimisations:** a non-volatile field read can be hoisted out of a loop and kept in a **register**, so the thread never sees the update.
2. **Store buffers:** a core writes into its private store buffer before the value reaches the cache, so other cores see it late.
3. **Reordering:** CPUs and compilers reorder independent loads and stores.

**How `volatile` is implemented (HotSpot)**
- **x86-64** has a strong model (TSO): the only reordering it allows is a later load passing an earlier store. Volatile reads are plain loads; a volatile write is followed by a full fence (HotSpot uses a `lock`-prefixed instruction) to drain the store buffer, preventing store-load reordering.
- **AArch64 (ARM)** has a weak model, so more is needed. HotSpot uses load-acquire (`ldar`) for volatile reads and store-release (`stlr`) for volatile writes.
- In both cases the JIT must also treat the field as un-cacheable in registers and not reorder around it.
- The same Java code is correct on both, because you program against the JMM, not the CPU.

**How `synchronized` works**
- Bytecode `monitorenter` / `monitorexit` (or the `ACC_SYNCHRONIZED` flag on methods).
- Uncontended: a CAS on the object header (a lightweight lock). Contended: the lock is inflated to a full monitor and waiting threads park in the OS. Biased locking was disabled by default in JDK 15 and later removed.
- Acquire has acquire semantics, release has release semantics, so everything written before the unlock is visible to the next thread that locks the same object.

**Critical sections and process communication.** A critical section is code that touches shared state and must not run in two threads at once. Threads share a heap, so they communicate through shared memory guarded by locks or volatiles. Separate *processes* don't share a heap; they communicate through OS mechanisms: pipes, sockets, shared memory segments (memory-mapped files), message queues and signals. Shared memory between processes still needs synchronisation, such as atomic operations or OS semaphores.

## Example

```java
class Worker {
    private volatile boolean running = true;    // without volatile, the loop may never stop
    private int count;                          // guarded by this

    void stop() { running = false; }

    void run() {
        while (running) {                       // re-read from memory each time
            doWork();
        }
    }

    synchronized void increment() { count++; }  // read-modify-write: needs a lock (or AtomicInteger)
}
```

Safe publication with `volatile`:

```java
config = new Config(...);   // ordinary writes to Config's fields
ready = true;               // volatile write: publishes everything above
// another thread: if (ready) { use(config); }  sees a fully built Config
```

## Pitfalls and follow-ups

- **Is `volatile int x; x++` thread-safe?** No. It is read, add, write; two threads can lose an update. Use `AtomicInteger` or a lock.
- **Double-checked locking:** the instance field must be `volatile`, or another thread can see a reference to a partly constructed object.
- **`volatile long`/`double`:** reads and writes are atomic; non-volatile 64-bit writes may be split on some 32-bit JVMs (JLS 17.7).
- **Cost:** volatile reads are cheap on x86; writes cost a fence. On ARM both have some cost.
- **`VarHandle` (Java 9+)** gives finer modes: plain, opaque, acquire/release, volatile.
- **Virtual threads (Java 21):** blocking inside `synchronized` pins the carrier; fixed in JDK 24 (JEP 491).

Related: [A6 · The Java Memory Model](../academy/lessons/A6.md), [B2 · Locks](../academy/lessons/B2.md), [B3 · Atomics and CAS](../academy/lessons/B3.md).
