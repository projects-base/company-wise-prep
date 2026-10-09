**Short answer:** `==` compares references for objects (same object in memory) and values for primitives. `equals()` compares logical content, but only if the class overrides it; `Object.equals` is just `==`. The contract: if two objects are `equals`, they must have the same `hashCode`. Break that and hash-based collections like `HashMap` and `HashSet` stop finding your objects.

## Explanation

**The equals contract:** reflexive (`a.equals(a)`), symmetric, transitive, consistent (same answer while the objects don't change), and `a.equals(null)` is false.

**The hashCode contract:**

1. Equal objects must return the same hash code.
2. Unequal objects may share a hash code (a collision). That is legal, just slower.
3. The hash must not change while the fields used in `equals` don't change.

**Why it matters:** `HashMap.get(key)` first uses `hashCode` to choose a bucket, and only then calls `equals` inside that bucket. If you override `equals` but not `hashCode`, two "equal" keys usually get different identity-based hashes, land in different buckets, and the lookup misses. A `HashSet` will happily hold "duplicates".

## Example

```java
String a = "hi";
String b = "hi";
String c = new String("hi");
a == b;          // true: both are the same interned literal
a == c;          // false: c is a new object
a.equals(c);     // true: same characters

Integer x = 127, y = 127;
x == y;          // true: Integer cache covers -128..127
Integer p = 128, q = 128;
p == q;          // false (with default settings): two different objects
p.equals(q);     // true
```

A correct class:

```java
public final class Money {
    private final long amount;
    private final String currency;

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money m)) return false;
        return amount == m.amount && currency.equals(m.currency);
    }
    @Override public int hashCode() {
        return Objects.hash(amount, currency);
    }
}
// Or simply: record Money(long amount, String currency) {}  — equals/hashCode generated
```

## Pitfalls and follow-ups

- **Use the same fields in both methods.** A field in `hashCode` that is not in `equals` breaks rule 1.
- **Mutable keys:** changing a field after inserting into a `HashSet` makes the element unreachable. Prefer immutable keys or records.
- **JPA entities:** don't base `equals`/`hashCode` on a generated `id` that is null before persist; use a business key, or compare by id and return a constant `hashCode` for the class.
- **`equals(Object)` vs `equals(Money)`:** writing the second one overloads instead of overriding. `@Override` catches this.
- **`instanceof` vs `getClass()`:** `instanceof` allows subclasses to be equal, which can break symmetry if a subclass adds fields. Making the class `final` (or a record) avoids the issue.
- **Strings:** always compare with `equals`; `==` only works by accident for interned literals.
- **Comparing enums:** `==` is fine and null-safe, since each constant is a singleton.
