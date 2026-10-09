**Short answer:** A synchronous call blocks the calling thread until the result comes back, so the caller's latency includes the callee's. An asynchronous call hands the work to another thread (or a non-blocking client) and returns a `CompletableFuture` right away, so the caller can do other work or run several calls in parallel. In Spring Boot you get async with `@Async` plus `@EnableAsync`, `CompletableFuture` with your own executor, or a non-blocking `WebClient`. Async inside one process still loses the work if the process dies; for durable, decoupled work you use a message queue.

## Explanation

**Synchronous.** A controller calls `restClient.get()...` and the Tomcat request thread waits. Simple to read, debug and put in a transaction. Under load, slow downstream calls tie up request threads until the pool is exhausted.

**Asynchronous options in Spring Boot 3:**
- **`@Async`** on a public method of a Spring bean, enabled with `@EnableAsync`. Spring wraps the bean in a proxy. Calling the method submits it to a `TaskExecutor` and returns at once. The return type is `void` or `CompletableFuture<T>`. Spring Boot auto-configures a `ThreadPoolTaskExecutor` (tune it with `spring.task.execution.pool.*`). Since Boot 3.2, with `spring.threads.virtual.enabled=true` on Java 21, it uses virtual threads instead.
- **`CompletableFuture.supplyAsync(task, executor)`** to fan out and combine several calls. Always pass your own executor; the default common pool is sized for CPU work, not blocking I/O.
- **Non-blocking clients** (`WebClient`) that do not hold a thread while waiting.
- **Async MVC:** returning `CompletableFuture` or `DeferredResult` from a controller frees the Tomcat thread while the work runs.

With virtual threads (Java 21), plain blocking code becomes cheap, which removes much of the need for reactive code just to save threads. You still need async composition to run calls *in parallel*.

## Example

```java
@Configuration
@EnableAsync
class AsyncConfig {
    @Bean Executor ioExecutor() {
        var ex = new ThreadPoolTaskExecutor();
        ex.setCorePoolSize(20); ex.setMaxPoolSize(50); ex.setQueueCapacity(200);
        ex.setThreadNamePrefix("io-");
        ex.initialize();
        return ex;
    }
}

@Service
class OddsClient {
    @Async("ioExecutor")
    public CompletableFuture<Odds> fetch(long marketId) {
        return CompletableFuture.completedFuture(rest.get().uri("/odds/{id}", marketId)
                .retrieve().body(Odds.class));
    }
}

@Service
class BetslipService {
    private final OddsClient odds;   // a different bean, so the proxy is used
    Betslip price(List<Long> markets) {
        var futures = markets.stream().map(odds::fetch).toList();
        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
                .orTimeout(500, TimeUnit.MILLISECONDS).join();
        return new Betslip(futures.stream().map(CompletableFuture::join).toList());
    }
}
```

## Pitfalls and follow-ups

- **How does `@Async` work underneath, and why does self-invocation bypass it?** Spring creates an AOP proxy around the bean. The proxy's interceptor (`AsyncExecutionInterceptor`) submits the call to the executor. A call from inside the same class (`this.fetch()`) goes to the real object, not the proxy, so it runs synchronously. Fix: move the method to another bean, or inject the bean's own proxy. Private and final methods cannot be proxied either.
- **When to use a message queue instead?** When the work must survive a crash or a deploy, must be retried, can take a long time, should be done by another service, or needs to absorb a traffic spike (load levelling). Example: settlement or email after a bet. `@Async` work lives only in memory and is lost if the JVM dies.
- **Exceptions in `@Async void` methods** are not seen by the caller. They go to an `AsyncUncaughtExceptionHandler`. Return a `CompletableFuture` so failures can be handled.
- **Lost context:** `@Transactional`, the security context and MDC/trace IDs are thread-bound and do not follow the task to the new thread automatically. Use a `TaskDecorator` to copy MDC or security context.
- **Unbounded queues** hide overload. Bound the queue and choose a rejection policy.

Further reading: [D3 · Spring AOP and the proxy traps](../academy/lessons/D3.md), [B5 · CompletableFuture and async composition](../academy/lessons/B5.md), [B6 · Virtual threads](../academy/lessons/B6.md).
