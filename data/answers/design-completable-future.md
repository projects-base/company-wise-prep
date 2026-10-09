**Short answer:** A future is a write-once cell plus a list of callbacks. Its state is either *pending* or *completed* with a value or an error. `complete` sets the outcome once, under a lock. It then wakes blocked `get()` callers and runs the registered callbacks *outside* the lock. `thenApply` and `thenCompose` return a new future and register a callback that completes it. `allOf` counts down with an `AtomicInteger`. For the data step, split the array into chunks, `supplyAsync` one task per chunk on an executor, then `allOf` and combine the partial results.

## Requirements

- `complete(v)` and `completeExceptionally(t)`: first call wins, later ones return false.
- `get()` blocks until done. It rethrows a failure as `ExecutionException`.
- Composition: `thenApply`, `thenCompose`, `whenComplete` (and through them error propagation).
- Factories: `supplyAsync(supplier, executor)`, `allOf(futures)`.
- Correct under concurrency: a callback registered at the same moment the future completes must run exactly once.
- Use it to process an array in parallel (sum as the example).

## Classes

- `Promise<T>`: the future. Holds `outcome` (null while pending) and `callbacks`, both guarded by `this`.
- `Outcome<T>`: a sealed interface with `Success<T>(value)` and `Failure<T>(error)`. It avoids the "is null a value or pending?" problem.
- `ParallelSum`: the client code.

## Patterns used

- **Promise / Future**: a placeholder for a result not ready yet.
- **Observer**: callbacks registered on the future and fired when it completes.
- **Monitor**: `synchronized` + `wait`/`notifyAll` for the blocking `get`.
- **Fork-join style split / combine** for the data step.

## Code

```java
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.*;

public final class Promise<T> {
    private sealed interface Outcome<T> permits Success, Failure {}
    private record Success<T>(T value) implements Outcome<T> {}
    private record Failure<T>(Throwable error) implements Outcome<T> {}

    private Outcome<T> outcome;                                   // guarded by this
    private List<Consumer<Outcome<T>>> callbacks = new ArrayList<>(); // guarded by this

    public boolean complete(T value)            { return finish(new Success<>(value)); }
    public boolean completeExceptionally(Throwable t) { return finish(new Failure<>(t)); }

    private boolean finish(Outcome<T> o) {
        List<Consumer<Outcome<T>>> toRun;
        synchronized (this) {
            if (outcome != null) return false;                    // write-once
            outcome = o;
            toRun = callbacks;
            callbacks = null;
            notifyAll();                                          // wake get() callers
        }
        toRun.forEach(cb -> cb.accept(o));                        // outside the lock
        return true;
    }

    private void onComplete(Consumer<Outcome<T>> cb) {
        Outcome<T> done;
        synchronized (this) {
            if (outcome == null) { callbacks.add(cb); return; }   // will run in finish()
            done = outcome;
        }
        cb.accept(done);                                          // already complete: run now
    }

    public T get() throws InterruptedException, ExecutionException {
        Outcome<T> o;
        synchronized (this) {
            while (outcome == null) wait();
            o = outcome;
        }
        return switch (o) {
            case Success<T> s -> s.value();
            case Failure<T> f -> throw new ExecutionException(f.error());
        };
    }

    public <U> Promise<U> thenApply(Function<? super T, ? extends U> fn) {
        Promise<U> next = new Promise<>();
        onComplete(o -> {
            switch (o) {
                case Success<T> s -> {
                    try { next.complete(fn.apply(s.value())); }
                    catch (Throwable t) { next.completeExceptionally(t); }
                }
                case Failure<T> f -> next.completeExceptionally(f.error());
            }
        });
        return next;
    }

    public <U> Promise<U> thenCompose(Function<? super T, Promise<U>> fn) {
        Promise<U> next = new Promise<>();
        onComplete(o -> {
            switch (o) {
                case Success<T> s -> {
                    try { fn.apply(s.value()).onComplete(next::finish); }
                    catch (Throwable t) { next.completeExceptionally(t); }
                }
                case Failure<T> f -> next.completeExceptionally(f.error());
            }
        });
        return next;
    }

    public static <T> Promise<T> supplyAsync(Supplier<T> supplier, Executor executor) {
        Promise<T> p = new Promise<>();
        executor.execute(() -> {
            try { p.complete(supplier.get()); }
            catch (Throwable t) { p.completeExceptionally(t); }
        });
        return p;
    }

    public static Promise<Void> allOf(List<? extends Promise<?>> promises) {
        Promise<Void> all = new Promise<>();
        if (promises.isEmpty()) { all.complete(null); return all; }
        AtomicInteger remaining = new AtomicInteger(promises.size());
        for (Promise<?> p : promises) {
            p.onComplete(o -> {
                if (o instanceof Failure<?> f) all.completeExceptionally(f.error()); // first failure wins
                else if (remaining.decrementAndGet() == 0) all.complete(null);
            });
        }
        return all;
    }
}

final class ParallelSum {
    static long sum(int[] a, int parts, ExecutorService pool) throws Exception {
        int chunk = Math.max(1, (a.length + parts - 1) / parts);
        List<Promise<Long>> partials = new ArrayList<>();
        for (int start = 0; start < a.length; start += chunk) {
            int from = start, to = Math.min(a.length, start + chunk);
            partials.add(Promise.supplyAsync(() -> {
                long s = 0;
                for (int i = from; i < to; i++) s += a[i];
                return s;
            }, pool));
        }
        Promise.allOf(partials).get();          // throws if any chunk failed
        long total = 0;
        for (Promise<Long> p : partials) total += p.get();   // already done: no blocking
        return total;
    }
}
```

**Why it is correct:** the check "pending? then add the callback" and the write "set the outcome, take the callbacks" happen under the same lock. So a callback is either in the list that `finish` drains, or it sees the outcome and runs itself, never both and never neither. `synchronized` also gives the happens-before edge, so a `get()` caller sees the value the completing thread wrote. Callbacks run outside the lock, so user code cannot deadlock the future or block other completers.

## Extensions

- **Which thread runs callbacks:** as in the JDK's non-async `thenApply`, either the completing thread or the registering thread. Add `thenApplyAsync(fn, executor)`, which wraps the callback in `executor.execute`, so slow callbacks do not run on an I/O thread.
- **Timeouts and cancel:** `get(timeout)` with `wait(millis)` in a loop on the remaining time. `cancel()` is just `completeExceptionally(new CancellationException())`.
- **Lock-free version:** the JDK uses a CAS on the result field and a Treiber stack of dependent actions. Mention it, but the `synchronized` version is easier to get right in an interview.
- **Deep chains:** callbacks run recursively on one stack, so very long synchronous chains can overflow it. The JDK has special handling for this.
- **Data step choices:** pick `parts` near `Runtime.getRuntime().availableProcessors()` for CPU work. For a plain sum, `Arrays.stream(a).parallel().asLongStream().sum()` or a `ForkJoinPool` `RecursiveTask` is what you would use in production. With virtual threads, the same code works for I/O-bound chunks.

Related: [B5 · CompletableFuture](../academy/lessons/B5.md), [B4 · Executors](../academy/lessons/B4.md), [A6 · Java Memory Model](../academy/lessons/A6.md), [B6 · Virtual threads](../academy/lessons/B6.md).
