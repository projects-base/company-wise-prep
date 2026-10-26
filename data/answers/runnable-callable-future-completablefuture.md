**Short answer:** `Runnable.run()` returns nothing and cannot throw checked exceptions; `Callable.call()` returns a value and can throw. Submitting either to an executor gives a `Future`, a handle whose `get()` blocks until the result is ready. `FutureTask` is the concrete class behind that handle. `CompletableFuture` adds non-blocking composition: chain, combine and recover without blocking a thread.

## Explanation

| Type | What it is | Key methods |
|---|---|---|
| `Runnable` | a task with no result | `void run()` |
| `Callable<V>` | a task with a result | `V call() throws Exception` |
| `Future<V>` | a handle to a pending result | `get()` (blocks), `get(timeout)`, `cancel()`, `isDone()` |
| `FutureTask<V>` | a `RunnableFuture`: wraps a Callable or Runnable so a `Thread` or `Executor` can run it, and stores the result | everything above |
| `CompletableFuture<V>` | a Future you can complete and compose | `supplyAsync`, `thenApply`, `thenCompose`, `thenCombine`, `allOf`, `exceptionally`, `handle`, `orTimeout` |

`ExecutorService.submit(callable)` wraps the task in a `FutureTask`, queues it and returns it as a `Future`.

The weakness of `Future` is that the only way to use the result is `get()`, which blocks a thread. `CompletableFuture` registers callbacks instead, so a pipeline of calls runs as each step finishes.

## Example

```java
ExecutorService pool = Executors.newFixedThreadPool(8);

Runnable audit = () -> log.info("bet placed");                   // no result
Callable<BigDecimal> price = () -> oddsClient.price(selectionId); // result, may throw

Future<BigDecimal> f = pool.submit(price);
BigDecimal odds = f.get(200, TimeUnit.MILLISECONDS);              // blocks, with a timeout

FutureTask<BigDecimal> task = new FutureTask<>(price);           // run it on a plain thread
new Thread(task).start();
BigDecimal again = task.get();

// Two independent calls in parallel, combined, with a timeout and a fallback
CompletableFuture<User> user = CompletableFuture.supplyAsync(() -> users.find(id), pool);
CompletableFuture<Wallet> wallet = CompletableFuture.supplyAsync(() -> wallets.find(id), pool);
BetSlipView view = user.thenCombine(wallet, BetSlipView::new)
        .orTimeout(300, TimeUnit.MILLISECONDS)
        .exceptionally(ex -> BetSlipView.fallback())
        .join();
```

## Pitfalls and follow-ups

- **`thenApply` vs `thenCompose` vs `thenCombine`?** `thenApply` maps the value (like `map`). `thenCompose` chains a step that itself returns a `CompletableFuture` (like `flatMap`, no nesting). `thenCombine` joins two independent futures when both finish.
- **Why give `CompletableFuture` its own executor?** Without one, `*Async` methods use the JVM-wide `ForkJoinPool.commonPool()`, sized for CPU work. Blocking I/O there starves parallel streams and every other user. Pass a dedicated pool (or virtual threads with Java 21).
- **Exceptions and timeouts in a chain?** An exception skips the following stages until `exceptionally` (failure only, returns a fallback), `handle` (both outcomes, can transform) or `whenComplete` (both, side effects only). `orTimeout` fails the future; `completeOnTimeout` supplies a default.
- **`execute()` vs `submit()`?** `execute` returns nothing, and an exception goes to the thread's uncaught handler. `submit` returns a Future and captures the exception; it surfaces as `ExecutionException` from `get()`, so if nobody calls `get()` it is silently lost.
- **`get()` vs `join()`?** Both block; `get()` throws checked `ExecutionException`/`InterruptedException`, `join()` throws unchecked `CompletionException`.
- **Waiting for many?** `CompletableFuture.allOf(a, b, c).join()`, then read each; `anyOf` for the first. With plain executors, `invokeAll`.
- **`@Async` in Spring?** Runs the method on a `TaskExecutor` through a proxy and can return `CompletableFuture<T>`. Configure the executor bean; self-invocation skips it.

Deeper: [B4 · Executors and sizing thread pools](../academy/lessons/B4.md), [B5 · CompletableFuture and async composition](../academy/lessons/B5.md), [B6 · Virtual threads and structured concurrency](../academy/lessons/B6.md).
