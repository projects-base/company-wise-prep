**Short answer:** Spring Boot is Spring with opinions: auto-configuration, starters, an embedded server and externalised config, so an app runs with almost no setup. The `ApplicationContext` is the IoC container that creates, wires and manages beans; Boot picks the context type from the app type (servlet, reactive or none). Each bean goes through instantiate → inject → Aware callbacks → post-processors → init → post-processors (proxies) → in use → destroy.

## Explanation

**Spring Boot.** `@SpringBootApplication` = `@Configuration` + `@EnableAutoConfiguration` + `@ComponentScan`. Auto-configuration classes are listed in each starter's `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` and guarded by conditions (`@ConditionalOnClass`, `@ConditionalOnMissingBean`, `@ConditionalOnProperty`), so your own bean always wins over the default.

**ApplicationContext vs BeanFactory.** `BeanFactory` is the minimal container: it creates and wires beans lazily. `ApplicationContext` extends it and adds eager singleton creation at startup (wiring errors fail fast), events, i18n, the `Environment` and profiles, resource loading, and automatic registration of `BeanPostProcessor`s, which is what makes AOP work.

**Context types.**

| Context | When |
|---|---|
| `AnnotationConfigApplicationContext` | Plain Spring, Java config; Boot with no web stack |
| `ClassPathXmlApplicationContext`, `FileSystemXmlApplicationContext` | Legacy XML config |
| `AnnotationConfigServletWebServerApplicationContext` | Boot servlet web app (Tomcat) |
| `AnnotationConfigReactiveWebServerApplicationContext` | Boot WebFlux app |
| `GenericApplicationContext` | Programmatic registration, tests |

**Bean lifecycle, in order.**

1. Instantiate (constructor; constructor injection happens here).
2. Populate properties (field and setter injection).
3. Aware callbacks: `BeanNameAware`, `BeanFactoryAware`, `ApplicationContextAware`.
4. `BeanPostProcessor.postProcessBeforeInitialization`.
5. Init: `@PostConstruct` → `InitializingBean.afterPropertiesSet()` → custom `initMethod`.
6. `BeanPostProcessor.postProcessAfterInitialization`: AOP proxies (`@Transactional`, `@Async`, `@Cacheable`) are created here, so other beans receive the proxy.
7. In use.
8. Shutdown: `@PreDestroy` → `DisposableBean.destroy()` → custom `destroyMethod`.

## Example

```java
@Component
class OddsCache implements ApplicationContextAware {
    private final OddsClient client;
    private ApplicationContext ctx;

    OddsCache(OddsClient client) { this.client = client; }          // 1. construct + inject

    @Override public void setApplicationContext(ApplicationContext ctx) { this.ctx = ctx; } // 3. Aware

    @PostConstruct void warmUp() { client.loadAll(); }               // 5. dependencies are ready here

    @PreDestroy void close() { client.close(); }                     // 8. on context close
}

// Boot picks the context type from the classpath
ConfigurableApplicationContext ctx = SpringApplication.run(BetApp.class, args);
```

## Pitfalls and follow-ups

- **BeanFactory vs ApplicationContext?** Lazy and minimal vs eager with events, profiles, i18n and post-processors. You always use ApplicationContext.
- **Where do BeanPostProcessors fit, and how are AOP proxies created?** Before and after init; the "after" hook of `AbstractAutoProxyCreator` wraps the bean in a JDK or CGLIB proxy.
- **Bean scopes?** singleton (default), prototype, and for web: request, session, application, websocket.
- **Setup in the constructor:** field-injected dependencies are still null there; use `@PostConstruct`.
- **Prototype beans:** Spring does not call their destroy callbacks.
- **Prototype inside a singleton** is injected once; use `ObjectProvider<T>`, `@Lookup` or a scoped proxy for a fresh one each time.
- **`@Transactional` in `@PostConstruct`** may not apply: you are inside the target, not the proxy, and the proxy is created after init. Use `ApplicationReadyEvent` instead.
- **Run code when the app is ready:** `@EventListener(ApplicationReadyEvent.class)` or a `CommandLineRunner`.

Deeper: [D2 · Spring core](../academy/lessons/D2.md), [L4 · Spring bean lifecycle](../academy/lessons/L4.md), [D4 · Spring Boot internals](../academy/lessons/D4.md), [L5 · Spring Boot startup](../academy/lessons/L5.md).
