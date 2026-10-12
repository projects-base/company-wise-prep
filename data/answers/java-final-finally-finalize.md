**Short answer:** They are unrelated. `final` is a **modifier**: a final variable can't be reassigned, a final method can't be overridden, a final class can't be extended. `finally` is a **block** after `try` that always runs, whether the `try` completes or throws, so it is used for cleanup. `finalize()` is an `Object` **method** the garbage collector used to call before reclaiming an object; it has been deprecated since Java 9 and is **deprecated for removal** since Java 18 (JEP 421). Use try-with-resources and `java.lang.ref.Cleaner` instead.

## Explanation

**`final`**
- **Variable / field:** assigned exactly once. For a reference, the *reference* is fixed; the object it points to can still change (`final List<String> l` can still `l.add(...)`).
- **Final fields** also have a memory-model guarantee: once the constructor finishes, other threads see their values without synchronisation (if `this` didn't escape).
- **Method:** can't be overridden. **Class:** can't be subclassed (`String`, `Integer`). Records are implicitly final; Java 17 added `sealed` for "only these subclasses".
- **Effectively final:** a local never reassigned can be captured by a lambda or inner class without the keyword.

**`finally`**
- Runs after `try` (and any matching `catch`), whether the block returned, threw, or fell through.
- Doesn't run if the JVM halts (`System.exit`, a crash, `kill -9`), or if the thread never leaves the `try` (infinite loop, deadlock).
- A `return` or `throw` inside `finally` **overrides** the one from `try` and can swallow exceptions. Don't do it.
- Since Java 7, **try-with-resources** replaces most hand-written `finally` cleanup: it closes every `AutoCloseable` in reverse order and records close-time failures as *suppressed* exceptions instead of losing the original.

**`finalize()` and why it is going away**
- The GC found an unreachable object with a non-trivial `finalize()`, queued it, and a finalizer thread called the method later.
- Problems (the reasons JEP 421 lists):
  - **Unpredictable timing:** maybe much later, maybe never. Scarce resources like file handles run out first.
  - **Resurrection:** `finalize` can store `this` somewhere and bring the object back to life.
  - **Performance:** finalizable objects need at least two GC cycles to reclaim and slow allocation.
  - **Security and reliability:** exceptions thrown in `finalize` are ignored; it runs on an unspecified thread.
- Timeline: deprecated in **Java 9**; **deprecated for removal in Java 18** (JEP 421), with the `--finalization=disabled` option to test your app without it. It still exists in Java 21, but don't write new ones.

**What to use instead**
1. **try-with-resources** for anything with a clear scope (streams, connections, locks). This is the answer 99% of the time.
2. **`java.lang.ref.Cleaner`** (Java 9+) as a safety net for objects that own native or off-heap resources and might not be closed. The cleanup action must not reference the object itself.

## Example

```java
final int limit = 10;              // can't reassign
final class Money { }              // can't extend

String read(Path p) throws IOException {
    BufferedReader r = Files.newBufferedReader(p);
    try {
        return r.readLine();
    } finally {
        r.close();                 // runs on success and on exception
    }
}

// Preferred: try-with-resources
String readBetter(Path p) throws IOException {
    try (var r = Files.newBufferedReader(p)) {
        return r.readLine();
    }                              // closed automatically, failures kept as suppressed
}
```

`Cleaner` as a safety net:

```java
public final class NativeBuffer implements AutoCloseable {
    private static final Cleaner CLEANER = Cleaner.create();

    private record State(long address) implements Runnable {   // must NOT reference NativeBuffer
        public void run() { freeNative(address); }
    }

    private final Cleaner.Cleanable cleanable;

    public NativeBuffer(long size) {
        this.cleanable = CLEANER.register(this, new State(allocateNative(size)));
    }

    @Override public void close() { cleanable.clean(); }   // explicit close: runs at most once
}
```

## Pitfalls and follow-ups

- **Does `finally` run if `try` has a `return`?** Yes. The return value is computed first, then `finally` runs, then the method returns. Changing a primitive local in `finally` doesn't change the already-computed value.
- **`final` vs immutable:** `final` freezes the reference; immutability needs final fields *and* no mutable state exposed.
- **Does `final` make code faster?** For locals, no. `static final` constants are inlined at compile time. The JIT handles non-final methods well with class hierarchy analysis.
- **Is `finalize` called on `System.gc()`?** Not guaranteed. `System.gc()` is only a hint.
- **What replaced finalizers inside the JDK?** `Cleaner` and try-with-resources; for example `FileInputStream` dropped its `finalize` method and relies on a `Cleaner`.
- **Why must the cleaning action not capture the object?** It would keep the object reachable, so it would never become phantom-reachable and never be cleaned.

Related: [L2 · Object and GC lifecycle](../academy/lessons/L2.md), [A4 · Garbage collection](../academy/lessons/A4.md), [A6 · The Java Memory Model](../academy/lessons/A6.md).
