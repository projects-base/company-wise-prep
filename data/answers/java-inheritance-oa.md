**Short answer:** These OA tasks give you a template with an abstract base class or interface and ask you to fill in subclasses so hidden tests pass. Read the template and the expected output first, then implement each subclass by calling the right `super(...)` constructor, overriding the abstract methods with `@Override`, and matching the output format exactly. Most failures come from constructor chaining, access modifiers, or formatting, not from the logic.

## Explanation

The exact template was not reported, so prepare the rules the tests usually exercise:

- **Constructor chaining:** a subclass constructor must call a parent constructor first, either explicitly with `super(args)` or implicitly with `super()`. If the parent has no no-arg constructor, you must call `super(args)` yourself, or it won't compile.
- **Overriding:** same name and parameter types, a return type that is the same or a subtype (covariant), access that is not weaker, and no broader checked exceptions. Add `@Override` so the compiler catches typos.
- **Abstract classes and interfaces:** a concrete subclass must implement every abstract method. Interfaces can have `default` and `static` methods.
- **Dynamic dispatch:** a call through a parent-typed reference runs the subclass's override. Fields and static methods are *not* polymorphic: they resolve by the declared type.
- **`super.method()`** reuses the parent's behaviour and extends it.
- **`protected`** fields are visible to subclasses (and the same package).
- **`toString()`** format: tests usually compare printed output character by character.

## Example

A typical template: shapes, employees, or vehicles.

```java
abstract class Employee {
    private final String name;
    protected final double baseSalary;

    protected Employee(String name, double baseSalary) {
        this.name = name;
        this.baseSalary = baseSalary;
    }

    abstract double salary();                          // subclasses must implement

    String getName() { return name; }

    @Override public String toString() {
        return "%s: %.2f".formatted(name, salary());   // calls the subclass's salary()
    }
}

class Manager extends Employee {
    private final double bonus;

    Manager(String name, double base, double bonus) {
        super(name, base);                             // must be the first statement
        this.bonus = bonus;
    }

    @Override double salary() { return baseSalary + bonus; }
}

class Intern extends Employee {
    Intern(String name, double base) { super(name, base); }

    @Override double salary() { return baseSalary * 0.5; }
}

List<Employee> staff = List.of(new Manager("Asha", 100, 20), new Intern("Ravi", 40));
staff.forEach(System.out::println);    // Asha: 120.00   Ravi: 20.00
```

## Pitfalls and follow-ups

- **Calling an overridable method from the parent constructor:** the subclass's override runs before the subclass's fields are initialised, so it sees `0` or `null`. Don't do it.
- **Overloading vs overriding:** `equals(Employee e)` *overloads* `equals(Object)` and won't be used by collections. `@Override` would have caught it.
- **Field hiding:** a field with the same name in the subclass hides the parent's; `parentRef.field` reads the parent's.
- **Static methods** are hidden, not overridden.
- **Process tip for OAs:** compile early, run the sample tests, and check exact output (decimal places, spaces, newlines) before optimising anything.
- **Java 17+:** `sealed` classes restrict which subclasses are allowed; with records and pattern matching for `switch` (Java 21) they are an alternative to deep hierarchies.

Related: [E1 · SOLID](../academy/lessons/E1.md), [H1 · Java idioms for coding interviews](../academy/lessons/H1.md).
