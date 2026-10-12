**Short answer:** Reflection lets code inspect and use classes at **runtime** without knowing them at compile time: read a `Class` object's fields, methods, constructors and annotations, create instances, call methods and read or write fields, even private ones if access is allowed. Frameworks are built on it: Spring scans and injects beans, Hibernate maps entities, Jackson binds JSON, JUnit finds `@Test` methods. In application code, use it rarely: it is slower, skips compile-time checks, and is restricted by the module system.

## Explanation

**The entry point is `Class<?>`**, which the JVM creates when it loads a class. Three ways to get it: `Order.class`, `obj.getClass()`, `Class.forName("com.x.Order")`.

**Main API (`java.lang.reflect`)**

| Need | API |
|---|---|
| Members | `getDeclaredFields()` (all in this class, any visibility), `getFields()` (public, including inherited), same pattern for methods and constructors |
| Create | `getDeclaredConstructor(...).newInstance(...)` (`Class.newInstance()` is deprecated) |
| Call | `method.invoke(target, args)` |
| Read/write | `field.get(obj)`, `field.set(obj, value)` |
| Private access | `setAccessible(true)`, subject to module rules |
| Metadata | `getAnnotation(...)`, `getModifiers()`, `getGenericType()`, `getRecordComponents()`, `getPermittedSubclasses()` |
| Interfaces at runtime | `java.lang.reflect.Proxy.newProxyInstance(...)` |

**How it works and what it costs**
- `Method.invoke` checks access and argument types and boxes primitives. Since **Java 18 (JEP 416)** core reflection is implemented on top of **method handles**, which the JIT optimises well, especially when the `Method` is cached in a `static final` field.
- Slower than a direct call mainly because the JIT can't inline across an unknown target, and because of boxing and checks. Look up `Method`/`Field` objects once and cache them.
- **Strong encapsulation:** since **Java 17 (JEP 403)** you can't `setAccessible(true)` on non-public members of JDK internals; you get `InaccessibleObjectException` unless the package is opened with `--add-opens`. Your own modules must `opens` packages to frameworks that reflect on them.
- **Generics are erased**, but generic signatures of fields, method parameters and superclasses are kept, so `getGenericType()` can still tell you `List<String>`. This is how Jackson's `TypeReference` works.
- **GraalVM native image** needs reflection known at build time. Spring Boot 3's AOT processing generates reflection hints for this.

**Where you see it in Spring**
- Classpath scanning finds `@Component` classes; the container calls constructors reflectively for dependency injection.
- `@Autowired` field injection uses `Field.set` on a private field (one reason constructor injection is preferred).
- `@RequestMapping` methods are found by scanning, and Spring MVC calls them with `Method.invoke` and resolved arguments.
- JDK dynamic proxies (`Proxy`) implement interface-based AOP for `@Transactional`.

## Example

```java
public class Account {
    private String owner = "asha";
    @Audited public void close() { System.out.println("closed"); }
}

Class<?> c = Class.forName("com.example.Account");
Object acc = c.getDeclaredConstructor().newInstance();

Field owner = c.getDeclaredField("owner");
owner.setAccessible(true);                     // works: same module, our own class
owner.set(acc, "rahul");

for (Method m : c.getDeclaredMethods()) {
    if (m.isAnnotationPresent(Audited.class)) {
        m.invoke(acc);                         // calls close()
    }
}

@Retention(RetentionPolicy.RUNTIME)            // needed, or the annotation isn't visible at runtime
@Target(ElementType.METHOD)
@interface Audited {}
```

A tiny dynamic proxy:

```java
interface Greeter { String greet(String name); }

Greeter real = n -> "Hi " + n;
Greeter logged = (Greeter) Proxy.newProxyInstance(
    Greeter.class.getClassLoader(),
    new Class<?>[]{Greeter.class},
    (proxy, method, args) -> {
        System.out.println("calling " + method.getName());
        return method.invoke(real, args);
    });
logged.greet("Vikram");    // prints "calling greet", returns "Hi Vikram"
```

## Pitfalls and follow-ups

- **When would you use it in app code?** Rarely: plugin loading, generic mappers, test utilities. Prefer interfaces, lambdas or `MethodHandle`/`VarHandle`.
- **`getMethods` vs `getDeclaredMethods`:** public including inherited vs everything declared in this class only.
- **Exceptions:** `invoke` wraps the target's exception in `InvocationTargetException`; unwrap with `getCause()`.
- **Annotation not found?** Its retention must be `RUNTIME`; the default `CLASS` keeps it out of reflection.
- **Can reflection change a `final` field?** For ordinary instance fields with `setAccessible`, yes (but the JIT may have constant-folded it). For `static final` fields, record fields and hidden classes, no.
- **Reflection vs `MethodHandle`:** method handles check access once at lookup, are typed and are optimised like direct calls when constant.
- **Security:** reflection can break encapsulation and invariants; the module system's `opens` is the guard.

Related: [A3 · Class loading and delegation](../academy/lessons/A3.md), [D1 · Build your own DI container](../academy/lessons/D1.md), [D3 · Spring AOP and the proxy traps](../academy/lessons/D3.md).
