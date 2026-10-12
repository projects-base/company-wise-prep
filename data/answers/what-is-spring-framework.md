**Short answer:** Spring is a Java framework whose core is an **Inversion of Control (IoC) container**: instead of your classes creating their own dependencies with `new`, you declare what they need and the container (the `ApplicationContext`) creates the objects ("beans"), wires them together through **dependency injection**, and manages their lifecycle. On top of that core sit AOP (transactions, security, caching as cross-cutting proxies), Spring MVC for web, data access, and more. **Spring Boot** adds auto-configuration, starters and an embedded server so an app runs with almost no setup.

## Explanation

**Inversion of Control.** Normally your code controls object creation and the flow. With IoC, the framework does: it creates objects and calls your code. **Dependency Injection** is the way Spring does IoC: dependencies are passed in (constructor, setter or field) rather than looked up or constructed.

Why that matters:
- **Loose coupling:** `OrderService` depends on a `PaymentGateway` interface; which implementation is used is configuration, not code.
- **Testability:** pass a fake or mock through the constructor; no container needed in unit tests.
- **One place for cross-cutting concerns:** because the container creates the objects, it can wrap them in proxies that add transactions, security or metrics.

**How the container works (simplified)**
1. Read **bean definitions**: component scanning finds `@Component`/`@Service`/`@Repository`/`@Controller` classes, plus `@Bean` methods in `@Configuration` classes.
2. Run `BeanFactoryPostProcessor`s (for example resolving `${...}` properties).
3. Instantiate singletons, resolving dependencies by type (then by `@Qualifier` / `@Primary` / name).
4. Run `BeanPostProcessor`s, which is where AOP proxies are created and `@PostConstruct` is called.
5. On shutdown, call `@PreDestroy` / destroy methods.

**Scopes:** `singleton` (default: one per container), `prototype` (new each time), and in web apps `request` and `session`.

**The main modules**

| Module | What it gives you |
|---|---|
| Core / Beans / Context | IoC container, events, profiles, `Environment` properties |
| AOP | Proxy-based aspects: `@Transactional`, `@Cacheable`, `@Async`, method security |
| Data access | `JdbcTemplate`, transaction management, Spring Data repositories, exception translation |
| Web | Spring MVC (servlet, blocking) and WebFlux (reactive) |
| Others | Spring Security, Spring Batch, Spring Kafka, Spring Cloud |

**Spring vs Spring Boot**
- Spring Framework is the container and modules. You used to configure everything yourself: XML or Java config, a servlet container, a `DataSource`.
- Spring Boot is opinionated setup on top: **starters** (`spring-boot-starter-web` pulls in compatible dependencies), **auto-configuration** (`@ConditionalOnClass`, `@ConditionalOnMissingBean`: "if Hikari is on the classpath and you defined no `DataSource`, create one"), an **embedded** Tomcat/Jetty, externalised config (`application.yml`), and Actuator for health and metrics.
- Versions: Spring Boot 3.x runs on Spring Framework 6 and needs Java 17+ with `jakarta.*` packages. Spring Boot 4 (November 2025) runs on Spring Framework 7.

## Example

```java
public interface PaymentGateway { void charge(String userId, BigDecimal amount); }

@Component
class StripeGateway implements PaymentGateway {
    public void charge(String userId, BigDecimal amount) { /* call the provider */ }
}

@Service
class OrderService {
    private final PaymentGateway gateway;          // depends on the interface

    OrderService(PaymentGateway gateway) {         // constructor injection: no @Autowired needed
        this.gateway = gateway;
    }

    @Transactional                                 // the container wraps this bean in a proxy
    public void checkout(String userId, BigDecimal total) {
        gateway.charge(userId, total);
    }
}

@SpringBootApplication                             // = @Configuration + @EnableAutoConfiguration + @ComponentScan
public class ShopApplication {
    public static void main(String[] args) {
        SpringApplication.run(ShopApplication.class, args);   // builds the ApplicationContext
    }
}
```

Without Spring, `main` would have to do `new OrderService(new StripeGateway(...))` and handle transactions by hand. In a unit test you still can: `new OrderService(fakeGateway)`.

## Pitfalls and follow-ups

- **Constructor vs field injection?** Constructor: dependencies are explicit, can be `final`, the object is never half-built, and tests need no reflection. Field injection hides dependencies.
- **Two beans of the same type?** Startup fails with `NoUniqueBeanDefinitionException`; pick with `@Primary` or `@Qualifier`, or inject `List<PaymentGateway>` to get all.
- **Are singleton beans thread-safe?** No more than any object. They are shared by all request threads, so keep them stateless.
- **Why doesn't `@Transactional` work on a method called from the same class?** Self-invocation bypasses the proxy. Move it to another bean.
- **`BeanFactory` vs `ApplicationContext`:** the context is a `BeanFactory` plus eager singleton creation, events, i18n, `Environment` and automatic post-processor registration.
- **Circular dependencies:** with constructor injection they fail at startup, and Spring Boot 2.6+ forbids them by default. Usually a sign the design needs splitting.
- **`@Component` vs `@Bean`:** annotate your own classes; use `@Bean` methods for third-party classes you can't annotate or that need custom construction.

Related: [D1 · Build your own DI container](../academy/lessons/D1.md), [D2 · Spring core: IoC container, scopes, profiles](../academy/lessons/D2.md), [D4 · Spring Boot internals](../academy/lessons/D4.md), [L4 · Spring bean lifecycle](../academy/lessons/L4.md), [S5 · Spring & Hibernate](../academy/lessons/S5.md).
