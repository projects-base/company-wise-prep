**Short answer:** An immutable class's state can't change after construction. The recipe: make the class `final` (or seal it), make every field `private final`, set everything in the constructor, provide no setters, **defensively copy** mutable inputs coming in and mutable state going out, and return a new object for every "change". In Java 21 a `record` does most of this for you, but you still copy mutable components such as lists.

## Explanation

**The rules and why each one matters**
1. **`final` class** so a subclass can't add mutable state or override methods to change behaviour. A private constructor plus a static factory also works.
2. **`private final` fields.** `final` stops reassignment and, under the Java Memory Model, guarantees other threads see the fully constructed values once the constructor finishes (as long as `this` doesn't escape the constructor).
3. **No setters.** "Modifiers" return a new instance (`withAmount(...)`).
4. **Defensive copy in.** If the caller passes a `List` or `Date` and keeps a reference, they can change your object later.
5. **Defensive copy out.** A getter that returns the internal list lets callers mutate it.
6. **Deep immutability.** `final List<Item>` only makes the *reference* final. The list and the `Item` objects must be immutable too.

**Why bother**
- Thread-safe without locks; can be shared and cached freely.
- Safe as `HashMap` keys and `Set` elements: the hash code can't change after insertion.
- Easier to reason about: validate once in the constructor, and the object is valid forever.
- The cost is allocation on every change, which is usually cheap for small objects in a generational GC.

Examples in the JDK: `String`, the boxed types (`Integer`, `Long`), `BigDecimal`, `LocalDate` and the rest of `java.time`, `List.of(...)`.

## Example

Classic class:

```java
public final class Order {
    private final String id;
    private final BigDecimal amount;          // BigDecimal is immutable: no copy needed
    private final List<String> items;
    private final Instant createdAt;          // Instant is immutable (unlike java.util.Date)

    public Order(String id, BigDecimal amount, List<String> items, Instant createdAt) {
        this.id = Objects.requireNonNull(id);
        if (amount.signum() < 0) throw new IllegalArgumentException("negative amount");
        this.amount = amount;
        this.items = List.copyOf(items);      // copy in; also rejects nulls and is unmodifiable
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public String id()          { return id; }
    public BigDecimal amount()  { return amount; }
    public List<String> items() { return items; }   // already unmodifiable: safe to return
    public Instant createdAt()  { return createdAt; }

    public Order withAmount(BigDecimal newAmount) { // "setter" returns a new object
        return new Order(id, newAmount, items, createdAt);
    }

    @Override public boolean equals(Object o) {
        return o instanceof Order other
            && id.equals(other.id) && amount.equals(other.amount)
            && items.equals(other.items) && createdAt.equals(other.createdAt);
    }
    @Override public int hashCode() { return Objects.hash(id, amount, items, createdAt); }
}
```

The same with a record (Java 16+):

```java
public record Order(String id, BigDecimal amount, List<String> items, Instant createdAt) {
    public Order {                                   // compact constructor: validate and copy
        Objects.requireNonNull(id);
        if (amount.signum() < 0) throw new IllegalArgumentException("negative amount");
        items = List.copyOf(items);
    }
    public Order withAmount(BigDecimal a) { return new Order(id, a, items, createdAt); }
}
```

A record is implicitly `final`, its fields are `private final`, and it generates the constructor, accessors, `equals`, `hashCode` and `toString`.

If you had to hold a mutable type such as `java.util.Date`, copy both ways: `this.created = new Date(created.getTime());` and `return new Date(created.getTime());`.

## Pitfalls and follow-ups

- **`Collections.unmodifiableList(items)` without copying** is only a view; the caller's original list can still change it. Use `List.copyOf`.
- **Arrays** can't be made read-only. Clone on the way in and out, or store a `List`.
- **Is a record always immutable?** Shallowly. `record Team(List<String> members)` is mutable if you pass an `ArrayList` and don't copy it.
- **Can reflection break it?** Yes for normal classes (`setAccessible` on a `final` field); record fields and hidden classes can't be changed that way. Serialization can also create instances bypassing your constructor; records deserialize through the canonical constructor, which is one reason to prefer them.
- **Why is `String` immutable?** String pool sharing, safe hash keys with a cached hash code, security (class names, file paths), and thread safety.
- **Builder pattern** pairs well with immutable classes that have many optional fields.

Related: [A6 · The Java Memory Model](../academy/lessons/A6.md), [E2 · Creational patterns](../academy/lessons/E2.md).
