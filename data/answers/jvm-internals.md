**Short answer:** The JVM splits memory into the heap (all objects, shared by threads and managed by the garbage collector), one stack per thread (frames with local variables and partial results), and native areas such as Metaspace (class metadata), the code cache (JIT-compiled code) and direct buffers. `new` allocates on the heap, usually by bumping a pointer in a thread-local allocation buffer, which is very cheap. The garbage collector later finds objects that are no longer reachable from GC roots and reclaims them. Most collectors are generational because most objects die young.

## Explanation

**Memory areas.**
- **Heap:** objects and arrays. Size set by `-Xms` / `-Xmx`.
- **Thread stacks:** one per thread, set by `-Xss`. A frame holds locals, operand stack and a return address. Primitives and references live here; the objects they point to are on the heap. Deep recursion causes `StackOverflowError`.
- **Metaspace (Java 8+):** class metadata in native memory, replacing PermGen. It grows unless `-XX:MaxMetaspaceSize` caps it.
- **Code cache:** machine code produced by the JIT.
- **Direct and other native memory:** `ByteBuffer.allocateDirect`, Netty buffers, thread stacks, GC data structures.

**Allocation.** Each thread gets a TLAB (thread-local allocation buffer) inside the young generation. Allocating is a pointer bump with no lock. Large objects may go elsewhere (G1 puts "humongous" objects in their own regions). The JIT's escape analysis can avoid allocating some objects that never leave a method (scalar replacement).

**Garbage collection.** Reachability starts from GC roots: thread stacks, static fields, JNI references. Unreachable objects are garbage.
- **Young generation:** Eden plus two survivor spaces. A minor GC copies live objects from Eden to a survivor space. Objects that survive enough cycles are promoted to the old generation. Copying is cheap when almost everything is dead.
- **Old generation:** collected less often, with marking (often concurrent), and compaction.
- **Collectors:** G1 is the default for server-class machines since Java 9. It splits the heap into regions and aims for a pause-time goal (`-XX:MaxGCPauseMillis`, default 200 ms). ZGC targets pauses of about a millisecond regardless of heap size; generational ZGC came in Java 21 (`-XX:+ZGenerational`) and is the default ZGC mode in later versions. Parallel GC maximises throughput for batch jobs.

## Example

```java
void handle(Request r) {
    int count = 0;                       // primitive local: on this thread's stack
    Bet bet = new Bet(r.stake());        // reference on the stack, Bet object on the heap (TLAB)
    cache.put(bet.id(), bet);            // now reachable from a static/field: survives GC
}                                        // frame popped; 'count' and the reference vanish
```

```text
java -Xms2g -Xmx2g -XX:+UseG1GC -Xlog:gc*:file=gc.log -XX:+HeapDumpOnOutOfMemoryError -jar app.jar
```

## Pitfalls and follow-ups

- **Heap generations and GC:** see above. Short-lived request objects die in Eden for free. Long-lived caches end up in the old generation, where collecting is more expensive.
- **Why does `-Xmx` not bound process memory?** It caps only the heap. The process also uses Metaspace, thread stacks (each thread's stack, about 1 MB by default on 64-bit Linux), the code cache, GC structures, direct buffers and native libraries. In a container, leave headroom above the heap, or size the heap as a fraction with `-XX:MaxRAMPercentage`. Native Memory Tracking (`-XX:NativeMemoryTracking=summary` and `jcmd <pid> VM.native_memory`) shows the breakdown.
- **Memory leak in Java?** Objects still reachable but never used: unbounded static maps, listeners never removed, `ThreadLocal` values in pooled threads. Find them with a heap dump and the dominator tree in Eclipse MAT.
- **`OutOfMemoryError` kinds:** `Java heap space`, `Metaspace`, `unable to create native thread`, `GC overhead limit exceeded`. Each points to a different area.
- **Where do string literals live?** In the string pool, which has been on the heap since Java 7.

Further reading: [A1 · How Java runs](../academy/lessons/A1.md), [A2 · JVM memory areas](../academy/lessons/A2.md), [A4 · Garbage collection](../academy/lessons/A4.md), [A5 · Diagnosing GC and memory leaks](../academy/lessons/A5.md).
