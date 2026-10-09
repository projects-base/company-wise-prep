**Short answer:** SOLID is five design rules that keep classes easy to change: Single responsibility, Open/closed, Liskov substitution, Interface segregation and Dependency inversion. Dependency inversion says high-level business code should depend on abstractions, not on concrete low-level classes, and the low-level classes implement those abstractions. The payoff is that you can swap an implementation (a different payment provider, a fake in tests) without touching the business logic. Spring's dependency injection is the everyday way we apply it.

## Explanation

- **S, Single responsibility.** A class has one reason to change. A `BetService` that validates, prices, saves and emails has four. Split them.
- **O, Open/closed.** Open for extension, closed for modification. Add a new behaviour by adding a class (a new `PaymentMethod` implementation), not by adding another `if/else` branch to old code.
- **L, Liskov substitution.** A subclass must work anywhere its parent is expected, with no surprises. If `ReadOnlyList extends List` throws on `add`, callers break. That is a sign the inheritance is wrong.
- **I, Interface segregation.** Many small interfaces beat one fat one. Clients should not depend on methods they do not use.
- **D, Dependency inversion.** Both high-level and low-level modules depend on an abstraction. The abstraction is owned by the high-level side and shaped by what it needs.

**Advantages of dependency inversion:**
1. **Testability.** Inject a fake or a mock instead of a real database or HTTP client.
2. **Swappable implementations.** Change provider by adding a class and wiring it, with no change to the business code.
3. **Lower coupling and smaller rebuilds.** The business module does not import infrastructure classes.
4. **Parallel work.** Teams agree on the interface and build each side independently.

Dependency inversion (a design principle) is not the same as dependency injection (a technique that delivers the dependency) or inversion of control (the framework calls your code and builds your objects). Spring does IoC and DI, which makes DIP easy, but you still have to design the interface.

## Example

```java
// High-level policy owns the abstraction
public interface PaymentGateway {
    PaymentResult charge(AccountId account, Money amount);
}

@Service
public class DepositService {
    private final PaymentGateway gateway;          // depends on the abstraction
    public DepositService(PaymentGateway gateway) { this.gateway = gateway; }

    public PaymentResult deposit(AccountId id, Money amount) {
        if (amount.isNegativeOrZero()) throw new IllegalArgumentException("amount");
        return gateway.charge(id, amount);
    }
}

// Low-level detail implements it; swap it without touching DepositService
@Component
class StripeGateway implements PaymentGateway { /* HTTP calls */ }
```

In a test: `new DepositService((acc, amt) -> PaymentResult.ok())`. No Spring context needed.

## Pitfalls and follow-ups

- **Is putting an interface on every class DIP?** No. An interface with one implementation and no test seam is noise. Add it where there is a real boundary (I/O, a third party, a policy that varies).
- **Where does the interface live?** In the business module, not next to the implementation. Otherwise the dependency arrow still points the wrong way.
- **Example of an OCP violation?** A `switch` on a type string that grows with every new type. Fix with polymorphism or a `Map<Type, Handler>` of Spring beans.
- **Classic LSP example?** `Square extends Rectangle`. Setting the width changes the height, so code that relies on the rectangle contract breaks.
- **SRP taken too far?** Hundreds of tiny classes no one can follow. Group by reason to change, not by method count.

Further reading: [E1 · SOLID, by violation and refactor](../academy/lessons/E1.md), [D2 · Spring core: IoC container](../academy/lessons/D2.md).
