**Short answer:** A process is an isolated address space plus resources (open files, sockets, page tables); a thread is a unit of execution inside a process with its own stack, registers and program counter, sharing the heap, globals and file descriptors with its sibling threads. Read-only shared data is safe *if* it is fully built and safely published before other threads read it, and nobody writes afterwards. Heap objects and static fields are shared; local variables live on each thread's own stack and are private, unless a reference to them escapes. A race condition happens when two threads access the same memory, at least one writes, and there is no synchronisation ordering those accesses.

## Explanation

**Process vs thread**

| | Process | Thread |
|---|---|---|
| Memory | own virtual address space | shares the process's |
| Creation / switch | heavier (new page tables, TLB effects) | lighter |
| Isolation | a crash usually kills only that process | a crash can take down all threads |
| Communication | IPC (pipes, sockets, shared memory) | shared memory directly |

**Read-only sharing.** Concurrent reads of immutable data need no lock. The catch is the moment of publication: if thread A builds a map and stores the reference in a plain field, thread B may see the reference but stale contents. Publish via a `final` field, a `volatile` field, a concurrent collection, or before starting the threads (`Thread.start()` is a happens-before edge). Also beware of "read-only" objects that mutate internally (lazy caches, `SimpleDateFormat`, or a `LinkedHashMap` in access order, which reorders itself on `get`).

**Heap vs stack**
- Local primitives and local references live in the thread's stack frame: private.
- Objects live on the heap (in Java, apart from escape-analysis optimisations): shared if more than one thread holds a reference.
- So a local variable is thread-safe, but the object it points to may not be.

**What causes a race**
1. **Non-atomic read-modify-write:** `count++` is load, add, store; two threads can both load 5 and both store 6.
2. **Check-then-act:** `if (!map.containsKey(k)) map.put(k, v)`.
3. **Visibility:** a write cached in a register or store buffer, or reordered by the compiler/CPU, is not seen by another thread. The Java Memory Model only guarantees visibility across a happens-before edge (lock release/acquire, `volatile` write/read, thread start/join).

## Example

```java
class Counter {
    private int count;                       // heap, shared
    void unsafeInc() { count++; }            // race: lost updates

    private final AtomicInteger safe = new AtomicInteger();
    void safeInc() { safe.incrementAndGet(); }
}

// Safe read-only sharing: immutable and published before threads start
final Map<String, Integer> limits = Map.of("A", 10, "B", 20);
Runnable r = () -> System.out.println(limits.get("A"));
Thread.ofVirtual().start(r);                 // start() is a happens-before edge

void work() {
    int local = 0;                           // on this thread's stack: private
    var list = new ArrayList<String>();      // heap, but only this thread has the reference
}
```

## Pitfalls and follow-ups

- **Is `volatile` enough for `count++`?** No; it gives visibility, not atomicity. Use `AtomicInteger`, `LongAdder` or a lock.
- **Data race vs race condition?** A data race is unsynchronised conflicting access; a race condition is a logic bug from timing (can happen even with synchronised pieces, e.g. check-then-act on a `ConcurrentHashMap` without `computeIfAbsent`).
- **Threads in Linux?** Both are tasks created by `clone()`; threads share more (memory, file table) via flags.
- **Virtual threads (Java 21)** are JVM-scheduled threads on a few carrier OS threads; same memory-model rules.

Go deeper: [B1 · Threads](../academy/lessons/B1.md), [A6 · The Java Memory Model](../academy/lessons/A6.md), [B3 · Atomics, CAS and concurrent collections](../academy/lessons/B3.md).
