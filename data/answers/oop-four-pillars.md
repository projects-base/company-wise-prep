**Short answer:** Encapsulation keeps an object's state private and changes it only through methods that protect its rules. Abstraction exposes what an object does and hides how. Inheritance lets a class reuse and specialise another class ("is-a"). Polymorphism lets one call, like `payment.process()`, run different code depending on the actual object at runtime. Together they let you change implementations without breaking callers.

## Explanation

- **Encapsulation.** Fields are `private`. State changes go through methods that enforce invariants, for example "a balance never goes negative". Callers cannot put the object in an invalid state. Records and immutable classes take this further.
- **Abstraction.** Callers depend on a contract (an interface or abstract class), not on details. A `NotificationSender` caller does not know whether it is email or SMS.
- **Inheritance.** A subclass gets the parent's fields and methods and can override them. Java has single class inheritance and multiple interface inheritance. It creates tight coupling: a change in the parent can break every child.
- **Polymorphism.** *Runtime* (overriding): the JVM picks the method from the object's actual class through dynamic dispatch. *Compile-time* (overloading): the compiler picks a method by parameter types.

## Example

```java
public sealed interface Payment permits CardPayment, WalletPayment {   // abstraction
    Receipt process(Money amount);
}

public final class Wallet {                                              // encapsulation
    private Money balance;
    public void debit(Money amount) {
        if (amount.compareTo(balance) > 0) throw new InsufficientFundsException();
        balance = balance.minus(amount);
    }
    public Money balance() { return balance; }   // no setter
}

public abstract class BaseNotifier {                                     // inheritance
    public final void send(String to, String msg) { validate(to); deliver(to, msg); }
    protected abstract void deliver(String to, String msg);
    private void validate(String to) { if (to == null || to.isBlank()) throw new IllegalArgumentException(); }
}
public class EmailNotifier extends BaseNotifier {
    @Override protected void deliver(String to, String msg) { /* SMTP */ }
}

List<Payment> payments = List.of(new CardPayment(), new WalletPayment());
payments.forEach(p -> p.process(stake));                                 // polymorphism
```

## Pitfalls and follow-ups

- **Abstract class vs interface (Java 8+)?** Interfaces can have `default`, `static` and (Java 9+) `private` methods, but no instance state and no constructors. A class can implement many interfaces but extend one class. Use an interface for a capability or contract. Use an abstract class when subclasses share state or a fixed algorithm skeleton (the template method above).
- **Composition over inheritance, when and why?** Use inheritance only for a true "is-a" where the subclass honours the whole parent contract (Liskov). Otherwise hold the other object as a field and delegate. Composition can be changed at runtime, avoids the fragile base class problem and exposes only what you choose. Example: a `RetryingClient` that wraps an `HttpClient`, instead of extending it.
- **Is a getter for every field encapsulation?** No. If callers read the fields and make the decisions, the logic has leaked out. Put behaviour next to the data ("tell, don't ask").
- **Can you override a static or private method?** No. Static methods are hidden, not overridden, and private methods are not inherited.
- **Encapsulation vs abstraction?** Encapsulation is about protecting state inside one class. Abstraction is about the contract that callers see.

Further reading: [E1 · SOLID, by violation and refactor](../academy/lessons/E1.md), [E3 · Structural patterns](../academy/lessons/E3.md).
