**Short answer:** A shallow copy creates a new top-level object but copies field values as they are, so reference fields still point to the *same* nested objects. A deep copy also copies every mutable object reachable from it, so the two graphs share nothing mutable. `Object.clone()` and `Arrays.copyOf` / `array.clone()` are shallow. For a deep copy you write copy constructors or factory methods that copy each mutable field yourself.

## Explanation

Java variables of reference type hold a reference, not the object. Copying a field copies the reference.

- **Shallow copy:** new outer object; primitive fields are copied by value; reference fields are copied by reference. Changing a nested mutable object through the copy is visible through the original.
- **Deep copy:** new outer object *and* new copies of the nested mutable objects, recursively. Immutable objects (`String`, `Integer`, `LocalDate`, records with only immutable components) can be shared safely, so a "deep enough" copy only needs to clone the mutable parts.

Ways to copy in Java:

| Technique | Depth |
|---|---|
| `Object.clone()` (with `Cloneable`) | shallow by default; you deep-copy fields inside your override |
| `array.clone()`, `Arrays.copyOf` | shallow for object arrays |
| `new ArrayList<>(list)`, `List.copyOf(list)` | shallow (new list, same elements) |
| copy constructor / static factory | whatever you write; the usual choice |
| serialization round-trip, JSON mapper | deep, but slow and needs all types to support it |

Effective Java advises against `Cloneable`: `clone()` is protected on `Object`, it bypasses constructors, it doesn't work well with `final` fields, and `Cloneable` is a marker interface with no `clone` method. Copy constructors are clearer.

## Example

```java
class Address { String city; Address(String c) { city = c; } }

class Person {
    String name;
    Address address;
    List<String> tags;

    Person(String name, Address address, List<String> tags) {
        this.name = name; this.address = address; this.tags = tags;
    }

    Person shallowCopy() {                 // shares address and tags
        return new Person(name, address, tags);
    }

    Person deepCopy() {                    // copies the mutable parts
        return new Person(name,             // String is immutable: share it
                new Address(address.city),
                new ArrayList<>(tags));     // elements are Strings: fine to share
    }
}

Person p = new Person("Akhil", new Address("Pune"), new ArrayList<>(List.of("java")));
Person s = p.shallowCopy();
s.address.city = "Delhi";
System.out.println(p.address.city);        // Delhi: shared object

Person d = p.deepCopy();
d.address.city = "Mumbai";
System.out.println(p.address.city);        // still Delhi
```

## Pitfalls and follow-ups

- **Is `new ArrayList<>(list)` a deep copy?** No. It is a new list holding the same element references.
- **What does `int[][].clone()` copy?** Only the outer array; the inner rows are shared.
- **Cycles in the object graph?** A naive recursive deep copy loops forever. Keep an `IdentityHashMap<original, copy>` of visited objects.
- **How do you avoid the problem altogether?** Prefer immutable types (records with immutable fields, `List.copyOf`). Immutable objects never need a deep copy.
- **Defensive copies:** copy mutable inputs in constructors and mutable outputs in getters, so callers can't change your internal state.
- **Does `clone()` call the constructor?** No. It allocates and copies field values directly.

Related: [A2 · JVM memory areas](../academy/lessons/A2.md).
