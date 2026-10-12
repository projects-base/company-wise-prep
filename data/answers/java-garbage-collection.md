**Short answer:** The GC runs when the JVM decides it must, not on a timer and not on your command: mostly when an allocation can't be satisfied (the young generation's eden fills up), and in concurrent collectors also when heap occupancy crosses a threshold. It reclaims objects that are no longer reachable from **GC roots** (thread stacks, static fields, JNI references). Most heaps are generational because most objects die young. Java 21's default is **G1** (region-based, pause-time goal); **ZGC** gives sub-millisecond pauses on large heaps and is generational in Java 21. To design one, start with mark-and-sweep: mark everything reachable from the roots, sweep the rest into a free list, then add compaction and generations.

## Explanation

**When does GC run?**
- **Allocation failure:** a thread can't get space in its TLAB or eden, so a **young (minor) collection** runs.
- **Occupancy thresholds:** G1 starts a concurrent marking cycle when old-gen occupancy passes the Initiating Heap Occupancy Percent (adaptive by default, starting at 45%). ZGC starts cycles based on allocation rate so it finishes before the heap runs out.
- **Promotion failure / humongous allocations / metaspace full** can trigger bigger collections.
- **`System.gc()`** is only a request; many setups ignore or disable it (`-XX:+DisableExplicitGC`).
- If even a full GC can't free enough memory, you get `OutOfMemoryError`.

**Reachability, not reference counting.** Java traces from roots, so cycles of unreachable objects are collected. Reference counting (as in CPython) needs extra work for cycles.

**Generations (the weak generational hypothesis)**
- **Young generation:** `eden` plus two `survivor` spaces. New objects go into eden. A minor GC copies live objects to a survivor space (or to old gen after surviving enough cycles, the *tenuring threshold*). Copying is cheap because few objects survive; dead ones cost nothing.
- **Old generation:** long-lived objects. Collected less often, with marking and compaction.
- **Card table / remembered sets:** track old-to-young pointers so a minor GC doesn't scan the whole old gen.
- **Metaspace** (outside the heap) holds class metadata.

**The collectors in Java 21**

| Collector | Flag | Idea | Fits |
|---|---|---|---|
| Serial | `-XX:+UseSerialGC` | Single-threaded, stop-the-world | Tiny heaps, small containers |
| Parallel | `-XX:+UseParallelGC` | Multi-threaded STW, max throughput | Batch jobs |
| **G1** (default) | `-XX:+UseG1GC` | Heap split into equal regions; young collections are STW evacuations; old regions marked concurrently and collected in *mixed* collections, choosing the most-garbage regions first; aims for `-XX:MaxGCPauseMillis` (default 200 ms) | General services |
| **ZGC** | `-XX:+UseZGC -XX:+ZGenerational` | Marks and relocates concurrently using coloured pointers and load barriers; pauses typically under 1 ms regardless of heap size | Latency-sensitive, large heaps |
| Shenandoah | `-XX:+UseShenandoahGC` | Concurrent compaction with barriers | Low latency (in OpenJDK builds) |

Generational ZGC arrived in Java 21 (JEP 439) behind `-XX:+ZGenerational`; it became the default ZGC mode in Java 23, and the non-generational mode was removed in Java 24.

## Example: designing a simple mark-and-sweep collector

Requirements: a managed heap of objects with references between them; free memory that is unreachable; keep it simple first, then improve.

```java
final class Obj {
    boolean marked;
    final List<Obj> refs = new ArrayList<>();
    final int size;
    Obj(int size) { this.size = size; }
}

final class Heap {
    private final List<Obj> allObjects = new ArrayList<>();   // every allocated object
    private final Set<Obj> roots = new HashSet<>();           // stacks + statics, simplified
    private final long capacity;
    private long used;

    Heap(long capacity) { this.capacity = capacity; }

    Obj allocate(int size) {
        if (used + size > capacity) collect();                // GC runs on allocation failure
        if (used + size > capacity) throw new OutOfMemoryError();
        Obj o = new Obj(size);
        allObjects.add(o);
        used += size;
        return o;
    }

    void collect() {               // stop-the-world: mutators paused
        mark();
        sweep();
    }

    private void mark() {          // iterative DFS: no stack overflow on long chains
        Deque<Obj> stack = new ArrayDeque<>(roots);
        while (!stack.isEmpty()) {
            Obj o = stack.pop();
            if (o.marked) continue;
            o.marked = true;
            for (Obj r : o.refs) if (!r.marked) stack.push(r);
        }
    }

    private void sweep() {
        Iterator<Obj> it = allObjects.iterator();
        while (it.hasNext()) {
            Obj o = it.next();
            if (o.marked) o.marked = false;                   // reset for next cycle
            else { it.remove(); used -= o.size; }             // unreachable: reclaim
        }
    }
}
```

**How you would evolve it (what the interviewer wants to hear):**
1. **Fragmentation:** sweep leaves holes. Add **compaction** (slide live objects together and fix pointers) or a **copying** collector (copy live objects to a fresh space; allocation becomes a pointer bump).
2. **Pause time:** marking the whole heap stops the world. Use **generations** so most collections touch only the small young space, with a **write barrier** recording old-to-young pointers.
3. **Concurrency:** mark while the application runs, using **tri-colour marking** (white = unvisited, grey = to scan, black = done). A write barrier (such as SATB in G1) stops the app from hiding a white object behind a black one.
4. **Parallelism:** several GC threads with work-stealing mark stacks.
5. **Root finding:** the JIT emits stack maps at **safepoints** so the GC knows which stack slots hold references.
6. **Finalization / weak references:** process soft, weak and phantom references after marking.

## Pitfalls and follow-ups

- **Can you force GC?** No. `System.gc()` is a hint.
- **Memory leak in Java?** Objects still reachable but never used: a growing static map, unbounded caches, listeners never removed, `ThreadLocal` in thread pools. GC can't free what is reachable.
- **Which collector for a betting API with strict latency?** Start with G1 and a pause goal; try generational ZGC if p99 pauses matter and the heap is large. Measure with GC logs (`-Xlog:gc*`) and JFR.
- **Why are short-lived objects cheap?** A young GC's cost is proportional to *live* objects, not dead ones.
- **Stop-the-world:** every collector has some pauses (at least for roots); ZGC and Shenandoah keep them tiny.
- **Containers:** size the heap with `-XX:MaxRAMPercentage`; the JVM is container-aware since JDK 10.

Related: [A2 · JVM memory areas](../academy/lessons/A2.md), [A4 · Garbage collection](../academy/lessons/A4.md), [A5 · Diagnosing GC and memory leaks](../academy/lessons/A5.md), [L2 · Object and GC lifecycle](../academy/lessons/L2.md).
