**Short answer:** Java 8 brought functional style to Java: lambdas and method references, functional interfaces, the Stream API, `Optional`, default and static methods in interfaces, and the `java.time` Date/Time API. A functional interface has exactly one abstract method, and a lambda is just a compact implementation of that one method. Streams let you describe a data pipeline (filter, map, collect) instead of writing loops.

## Explanation

- **Functional interface:** one abstract method (default and static methods don't count). `@FunctionalInterface` makes the compiler enforce it. Built-ins in `java.util.function`: `Predicate<T>` (T → boolean), `Function<T,R>`, `Consumer<T>`, `Supplier<T>`, `BiFunction`, `UnaryOperator`. Older ones also qualify: `Runnable`, `Comparator`, `Callable`.
- **Lambda:** `(a, b) -> a + b`. The compiler infers the target functional interface from context. Captured local variables must be effectively final.
- **Method reference:** `Employee::name`, `System.out::println`, `ArrayList::new`.
- **Default methods:** let interfaces evolve without breaking implementers (that is how `List.sort` and `Collection.stream()` were added). If two interfaces give the same default, the class must override and pick one with `A.super.method()`.
- **Static interface methods:** utilities on the interface itself, e.g. `Comparator.comparing`.
- **Optional:** a return type that says "may be empty". Use `map`, `orElse`, `orElseThrow`. Not meant for fields or parameters.
- **Stream API:** lazy pipelines over collections, with `Collectors` for grouping and aggregation, and `parallelStream()`.
- **java.time:** immutable, thread-safe `LocalDate`, `LocalDateTime`, `ZonedDateTime`, `Instant`, `Duration`, `Period`, replacing mutable `Date`/`Calendar` and non-thread-safe `SimpleDateFormat`.
- Also: `CompletableFuture`, `ConcurrentHashMap` rewrite, Metaspace replacing PermGen.

## Example

```java
@FunctionalInterface
interface Discount { double apply(double price); }

Discount festive = p -> p * 0.9;

Optional<Employee> top = emps.stream()
        .filter(e -> e.dept().equals("IT"))
        .max(Comparator.comparingDouble(Employee::salary));
String name = top.map(Employee::name).orElse("none");

LocalDate due = LocalDate.now().plusDays(30);
```

## Pitfalls and follow-ups

- **map vs flatMap:** `map` is one-to-one (`Stream<List<String>>` stays nested). `flatMap` maps each element to a stream and flattens: `lists.stream().flatMap(List::stream)`.
- **Intermediate vs terminal:** intermediate ops (`filter`, `map`, `sorted`, `distinct`) return a stream and do nothing yet. A terminal op (`collect`, `forEach`, `count`, `findFirst`, `reduce`) triggers the work. A stream can be consumed only once.
- **Laziness:** elements flow one at a time through the whole pipeline, and short-circuit ops (`findFirst`, `anyMatch`, `limit`) stop early. No terminal op means nothing runs, not even `peek`.
- **Lambda vs anonymous class:** `this` inside a lambda is the enclosing instance; no new class file per lambda (uses `invokedynamic`).
- **`Optional.get()` without a check** is the same bug as a null dereference. Prefer `orElseThrow()`.

**Later versions (follow-up):** `var` (10), `HttpClient` and `String.isBlank/strip/lines` (11), switch expressions (14), text blocks (15), records (16), sealed classes (17), pattern matching for `switch` and record patterns, virtual threads, sequenced collections (21). Say which ones you actually used in production, for example records for DTOs and switch expressions for mapping enums.

Deeper: [A7 · Java 8 → 11 → 17 → 21 → 25](../academy/lessons/A7.md).
