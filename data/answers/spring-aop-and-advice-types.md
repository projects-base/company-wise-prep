**Short answer:** AOP pulls cross-cutting code, such as transactions, security, caching, logging and metrics, out of business methods into an aspect. Spring applies it at runtime with proxies: callers get a proxy that runs the advice around the real method. The advice types are `@Before`, `@AfterReturning`, `@AfterThrowing`, `@After` and `@Around`.

## Watch it

Press **Play**, or step with **→**.

```anim
{"title":"A call through a Spring AOP proxy","stages":["Caller","Proxy","@Around (before proceed)","@Before","Target method","@AfterReturning","@After","@Around (after proceed)","Back to caller"],"frames":[
{"stage":0,"note":"OrderController calls betService.place(slip). It holds a reference to the proxy, not the real BetService.","vars":{"call":"place(slip)"}},
{"stage":1,"note":"The proxy (CGLIB subclass) looks up the advice chain that matches this method.","vars":{"call":"place(slip)","matched":"3 advices"}},
{"stage":2,"note":"@Around starts first, e.g. starts a timer, then calls pjp.proceed().","vars":{"timer":"started"}},
{"stage":3,"note":"@Before runs: an auth or validation check. Throwing here stops the call.","vars":{"timer":"started"}},
{"stage":4,"note":"The real place() method runs on the target object.","vars":{"result":"BetId(981)"}},
{"stage":5,"note":"@AfterReturning sees the result (on an exception, @AfterThrowing runs instead).","vars":{"result":"BetId(981)"}},
{"stage":6,"note":"@After always runs, like finally.","vars":{"result":"BetId(981)"}},
{"stage":7,"note":"@Around continues after proceed(): logs \"place took 12 ms\" and returns the result (it could also change it).","vars":{"timer":"12 ms"}},
{"stage":8,"note":"The caller gets the result. Trap: if place() calls this.validate() inside BetService, that call never touches the proxy, so no advice runs.","vars":{"result":"BetId(981)"}}
]}
```

## Explanation

**Terms.** An *aspect* is the module (a class with `@Aspect`). A *join point* is a point in execution; in Spring AOP it is always a method call. A *pointcut* selects join points (`execution(* com.app..*Service.*(..))`, `@annotation(Audited)`). *Advice* is the code that runs there. *Weaving* links them; Spring does it at runtime by creating proxies.

**How the proxy is made.** During the bean lifecycle, after init, an auto-proxy creator (a `BeanPostProcessor`) checks each bean against the pointcuts. If any match, it returns a proxy instead of the bean: a JDK dynamic proxy (implements the bean's interfaces) or a CGLIB subclass. Spring Boot defaults to CGLIB. The proxy builds a chain of interceptors and calls the target at the end.

**Advice types.**

| Advice | Runs | Typical use |
|---|---|---|
| `@Before` | before the method | auth check, argument validation |
| `@AfterReturning` | after a normal return, sees the result | audit of successful operations |
| `@AfterThrowing` | when an exception escapes | error metrics, alerting |
| `@After` | always, like `finally` | release resources, MDC cleanup |
| `@Around` | wraps the call, decides whether and how often to `proceed()` | timing, retries, transactions, caching |

**Where Spring itself uses it:** `@Transactional`, `@Cacheable`, `@Async`, `@PreAuthorize`, `@Retryable`, `@Validated` on methods.

## Example

```java
@Aspect
@Component
class TimingAspect {
    private static final Logger log = LoggerFactory.getLogger(TimingAspect.class);

    @Around("execution(* com.example.bet..*Service.*(..))")
    Object time(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.nanoTime();
        try {
            return pjp.proceed();                                   // the real method runs here
        } finally {
            log.info("{} took {} ms", pjp.getSignature().toShortString(), (System.nanoTime() - start) / 1_000_000);
        }
    }

    @AfterThrowing(pointcut = "execution(* com.example.wallet..*(..))", throwing = "ex")
    void onError(JoinPoint jp, Exception ex) {
        log.warn("{} failed: {}", jp.getSignature().toShortString(), ex.getMessage());
    }
}
```

## Pitfalls and follow-ups

- **JDK dynamic proxy vs CGLIB?** JDK proxies implement interfaces, so you must inject the interface type. CGLIB subclasses the class; it cannot advise `final` classes or `final`/`private` methods, and advice on them silently does nothing.
- **Why does self-invocation skip `@Transactional` or `@Async`?** `this.other()` calls the target directly, not through the proxy. Fix: move the method to another bean (cleanest), use `TransactionTemplate`, or inject the bean's own proxy lazily.
- **Pointcut, join point, aspect, weaving?** See Explanation: which calls, a call, the module, and linking them.
- **Which advice for retries?** `@Around`, the only one that can call `proceed()` again. In practice use Spring Retry or Resilience4j, which are built on it.
- **Spring AOP vs AspectJ?** Spring AOP is proxy-based and limited to public method calls on Spring beans. AspectJ weaves bytecode at compile or load time and also covers fields, constructors and self-calls.
- **Order of several aspects?** `@Order` on the aspect class; a lower value runs first on the way in, last on the way out.

Deeper: [D3 · Spring AOP and the proxy traps](../academy/lessons/D3.md), [S5 · Spring & Hibernate: POJOs, DI, AOP, ORM](../academy/lessons/S5.md).
