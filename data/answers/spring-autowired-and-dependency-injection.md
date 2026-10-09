**Short answer:** Dependency injection means a class receives its collaborators instead of creating them, and the Spring IoC container does the wiring. `@Autowired` tells Spring to resolve a dependency by type from the bean registry. I use constructor injection: fields can be `final`, dependencies are explicit, the class is easy to unit-test, and with a single constructor `@Autowired` isn't even needed. If two beans match the type, Spring fails at startup unless `@Primary`, `@Qualifier` or a matching name picks one.

## Explanation

**How it works:** at startup Spring scans `@Component`/`@Service`/`@Repository`/`@Controller` classes and `@Bean` methods, builds bean definitions, then creates beans. For each `@Autowired` constructor, field or setter, `AutowiredAnnotationBeanPostProcessor` finds candidate beans by type and injects them. Singletons are created eagerly at startup by default, so wiring errors fail fast.

**Three styles:**

| | Constructor | Setter | Field |
|---|---|---|---|
| `final` fields | Yes | No | No |
| Required deps visible | Yes, in the signature | No | No |
| Unit test without Spring | `new Service(mock)` | Call setters | Needs reflection |
| Circular dependency | Fails at startup (good signal) | Possible | Possible |
| Use for | Default choice | Optional deps | Avoid (tests, legacy) |

Since Spring 4.3, a class with exactly one constructor is autowired without the annotation. Spring Boot 2.6+ also forbids circular references by default.

**Two beans of the same type:** Spring throws `NoUniqueBeanDefinitionException`. Resolve with:

- `@Primary` on one bean: the default winner when nothing more specific is asked for.
- `@Qualifier("name")` at the injection point: picks a specific bean, and beats `@Primary`.
- Parameter name matching a bean name: a fallback, fragile, avoid relying on it.
- Inject them all: `List<PaymentGateway>` or `Map<String, PaymentGateway>` (keyed by bean name), useful for strategy patterns.

## Example

```java
public interface PaymentGateway { void pay(Order o); }

@Component @Primary
class StripeGateway implements PaymentGateway { ... }

@Component("razorpay")
class RazorpayGateway implements PaymentGateway { ... }

@Service
public class CheckoutService {
    private final PaymentGateway defaultGateway;
    private final PaymentGateway indiaGateway;

    public CheckoutService(PaymentGateway defaultGateway,                 // Stripe (@Primary)
                           @Qualifier("razorpay") PaymentGateway india) { // Razorpay
        this.defaultGateway = defaultGateway;
        this.indiaGateway = india;
    }
}
```

## Pitfalls and follow-ups

- **`@Qualifier` vs `@Primary`:** `@Primary` sets a default at the bean side; `@Qualifier` chooses at the injection side and wins over `@Primary`.
- **Optional dependency:** `@Autowired(required = false)`, `Optional<T>`, or `ObjectProvider<T>`.
- **Bean scopes:** `singleton` (default, one per container), `prototype` (new per lookup), and web scopes `request`, `session`, `application`. Injecting a prototype into a singleton gives you one instance forever; use `ObjectProvider<T>.getObject()` or a scoped proxy to get fresh ones.
- **Lifecycle:** instantiate → inject dependencies → `*Aware` callbacks → `BeanPostProcessor` before-init → `@PostConstruct` / `afterPropertiesSet` / init-method → after-init (proxies like `@Transactional` are created here) → in use → `@PreDestroy` / `destroy` on shutdown (singletons only; Spring doesn't call destroy on prototypes).
- **Singletons are shared across threads,** so keep them stateless.
- **IoC vs DI:** IoC is the principle (the container controls object creation); DI is the way Spring implements it.

Deeper: [D2 · Spring core: IoC container, scopes, profiles](../academy/lessons/D2.md), [L4 · Spring bean lifecycle](../academy/lessons/L4.md), [D1 · Build your own DI container](../academy/lessons/D1.md).
