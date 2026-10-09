**Short answer:** Polymorphism means one interface, many forms: the same call behaves differently depending on the object. Compile-time polymorphism is method overloading (same name, different parameters, chosen by the compiler). Runtime polymorphism is method overriding (a subclass redefines an inherited instance method, and the JVM picks the version from the object's actual class). Static methods can't be overridden: a subclass static method with the same signature **hides** the parent's, and the call is bound at compile time to the reference type.

## Explanation

- **Overloading:** resolved by the compiler from the static types of the arguments. Return type alone can't distinguish overloads.
- **Overriding:** same signature, compatible (covariant) return type, access not more restrictive, no broader checked exceptions. Resolved at runtime by dynamic dispatch (`invokevirtual` / `invokeinterface`).
- **Static methods** belong to the class, not an instance, so there is no object to dispatch on. Redefining one in a subclass hides it.
- **Fields** are never polymorphic either: field access uses the reference type.

## Example

```java
class Animal {
    static String kind() { return "animal"; }
    String sound()       { return "..."; }
}
class Dog extends Animal {
    static String kind() { return "dog"; }      // hides Animal.kind()
    @Override String sound() { return "woof"; } // overrides
}

Animal a = new Dog();
a.sound();        // "woof"   runtime type decides (overriding)
a.kind();         // "animal" reference type decides (hiding); IDEs flag this: call it as Animal.kind()
Dog.kind();       // "dog"

// Overloading: compile-time choice
void print(Object o) { System.out.println("object"); }
void print(String s) { System.out.println("string"); }
Object o = "hi";
print(o);         // "object": chosen from the static type Object
```

## Pitfalls and follow-ups

- **Compile-time vs runtime polymorphism (follow-up):** overloading is static binding by the compiler using declared types; overriding is dynamic binding by the JVM using the actual object.
- **Can you override a private method?** No. It isn't inherited, so a same-named method in the subclass is a new, unrelated method. `@Override` on it is a compile error.
- **Can you override a final method?** No, compile error. A `final` class can't be extended at all.
- **`@Override` on a static method (follow-up):** compile error ("method does not override or implement a method from a supertype"), which is exactly why the annotation is useful.
- **Instance method hiding a static one (or the reverse)?** Compile error: a static method can't hide an instance method and an instance method can't override a static one.
- **Can you overload `main` or static methods?** Yes, overloading works for statics; only overriding doesn't.
- **Constructors** are not inherited, so they can't be overridden, only overloaded.
- **Why it matters in Spring:** you code against interfaces (`PaymentGateway`), and the container injects an implementation. That's runtime polymorphism, and it's what makes the Strategy pattern and mocking in tests work.

Deeper: [A1 · How Java runs: javac, bytecode, interpreter, JIT](../academy/lessons/A1.md), [E1 · SOLID, by violation and refactor](../academy/lessons/E1.md).
