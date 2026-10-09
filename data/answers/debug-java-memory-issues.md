**Short answer:** First read the exact `OutOfMemoryError` message, because it tells you which area ran out (`Java heap space`, `Metaspace`, `unable to create native thread`, `GC overhead limit exceeded`). Then watch heap usage after each GC over time: if the floor keeps rising, it's a leak; if it's flat but too high, the heap is undersized or the workload loads too much at once. Take a heap dump (automatically on OOM, or with `jcmd`), open it in Eclipse MAT, and follow the dominator tree to the GC root that holds the memory. Fix the code, then confirm with metrics.

## Explanation

**Step by step:**

1. **Always run with dumps on:** `-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/dumps`. Also keep GC logs: `-Xlog:gc*:file=gc.log`.
2. **Observe:** heap used after GC, GC pause times and frequency (Actuator + Micrometer `jvm.memory.used`, `jvm.gc.pause` in Grafana, or VisualVM / JDK Mission Control locally). A sawtooth whose lows climb steadily means a leak.
3. **Quick look without a full dump:** `jcmd <pid> GC.class_histogram` shows instance counts per class. Take two a few minutes apart and compare growth.
4. **Heap dump:** `jcmd <pid> GC.heap_dump /tmp/heap.hprof` (or `jmap -dump:live,format=b,file=heap.hprof <pid>`). It pauses the app and the file is as large as the heap, so be careful in production.
5. **Analyse in Eclipse MAT:** "Leak Suspects" report, then the **dominator tree** (which objects keep the most memory alive, by retained size), then "Path to GC Roots" to see who references them.
6. **Fix and verify** under the same load.

**Common leak sources (follow-up):**

- **Static collections** that only grow (`static Map` used as a cache or registry).
- **Caches without eviction or TTL.** Use Caffeine with `maximumSize` / `expireAfterWrite`.
- **ThreadLocals in thread pools:** pool threads live forever, so values set and never `remove()`d stay. Always clear in `finally`.
- **Unclosed resources:** streams, connections, result sets. Use try-with-resources.
- **Listeners and callbacks** registered and never removed.
- **Bad `equals`/`hashCode` on map keys:** "the same" key is added again and again as new entries.
- **Loading too much at once** (not a leak, still OOM): `findAll()` on a big table, unbounded queues, huge JSON responses. Paginate or stream.

## Example

```java
// Leak: grows forever
public class SessionTracker {
    private static final Map<String, UserSession> SESSIONS = new HashMap<>();
    public static void track(String id, UserSession s) { SESSIONS.put(id, s); }   // never removed
}

// Fix: bounded cache with expiry (Caffeine)
private final Cache<String, UserSession> sessions = Caffeine.newBuilder()
        .maximumSize(10_000)
        .expireAfterAccess(Duration.ofMinutes(30))
        .build();

// ThreadLocal hygiene in pooled threads
try {
    TENANT.set(tenantId);
    handle(request);
} finally {
    TENANT.remove();
}
```

```text
jcmd 4242 GC.class_histogram | head -20
jcmd 4242 GC.heap_dump /tmp/app.hprof
```

## Pitfalls and follow-ups

- **Is raising `-Xmx` a fix?** Only if usage is flat and legitimately bigger. With a leak it just delays the crash.
- **In containers:** the JVM sizes the heap from the container limit; set `-XX:MaxRAMPercentage` (e.g. 75) so metaspace, thread stacks and direct buffers still fit. Container OOM-kill (exit 137) without a Java OOM means non-heap or native memory, not the heap.
- **Metaspace OOM:** usually classloader leaks (redeploys, dynamic proxies or generated classes being created repeatedly).
- **`unable to create native thread`:** too many threads, not too little heap. Check thread dumps and pool sizes.
- **Shallow vs retained size:** shallow is the object itself; retained is everything that would be freed if it were collected. Sort by retained.
- **Memory leak in a GC language?** Yes: anything still reachable from a GC root can't be collected, even if you'll never use it again.
- **Low-overhead profiling in prod:** Java Flight Recorder (`jcmd <pid> JFR.start`) records allocation and GC events cheaply.

Deeper: [A4 · Garbage collection](../academy/lessons/A4.md), [A5 · Diagnosing GC and memory leaks](../academy/lessons/A5.md), [A2 · JVM memory areas](../academy/lessons/A2.md).
