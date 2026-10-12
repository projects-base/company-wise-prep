**Short answer:** Pick one pattern you can explain end to end: the problem, the shape, and the trade-off. **Strategy** is a strong choice for a Spring backend: it replaces a growing `if/else` or `switch` on a type with one interface and one class per variant, and Spring injects all the implementations for you. Say what it bought you (adding a variant became a new class, not an edit to a big method) and what it cost (more classes, indirection).

## Explanation

Interviewers ask this to see whether you know patterns as **solutions to problems**, not as names. A good answer has four parts:

1. **The problem.** "We had a method that chose behaviour by type, and it kept growing."
2. **The pattern and its shape.** Interface, implementations, who picks one.
3. **Where it lived in the code.** Concrete: the classes, how Spring wired them.
4. **The trade-off.** When you would *not* use it.

**Strategy** (behavioural): define a family of algorithms behind one interface and make them interchangeable at runtime. It is the Open/Closed principle in practice: open for new variants, closed for edits to the dispatcher.

A story that fits a backend role (use your own; do not claim one you didn't build): **[a payment, pricing, notification or settlement flow where the logic depended on a type]**. For example, in a betting platform, settling a bet depends on the market type (match winner, over/under, handicap). Without Strategy, a `settle()` method grows a `switch` with every new market; with it, each market type is its own class.

Other patterns that are easy to explain with real Spring code:

| Pattern | Where you meet it in Spring/Java |
|---|---|
| **Template Method** | `JdbcTemplate`, `RestTemplate`: the framework owns the steps (open, execute, map, close), you supply the varying part as a callback |
| **Proxy** | `@Transactional`, `@Cacheable`, `@Async`: Spring wraps your bean in a proxy that adds behaviour around the call |
| **Factory / Builder** | `BeanFactory`; builders like `WebClient.builder()`, `ResponseEntity.ok()` |
| **Singleton** | Default bean scope: one instance per container (not the classic static singleton) |
| **Observer** | `ApplicationEventPublisher` + `@EventListener` |
| **Decorator** | `BufferedInputStream` wrapping `InputStream`; `Collections.unmodifiableList` |
| **Chain of Responsibility** | The servlet filter chain and the Spring Security filter chain |

## Example

```java
public interface SettlementStrategy {
    MarketType type();
    Outcome settle(Bet bet, MatchResult result);
}

@Component
class MatchWinnerSettlement implements SettlementStrategy {
    public MarketType type() { return MarketType.MATCH_WINNER; }
    public Outcome settle(Bet bet, MatchResult r) {
        return bet.selection().equals(r.winner()) ? Outcome.WON : Outcome.LOST;
    }
}

@Component
class OverUnderSettlement implements SettlementStrategy {
    public MarketType type() { return MarketType.OVER_UNDER; }
    public Outcome settle(Bet bet, MatchResult r) {
        int goals = r.totalGoals();
        double line = bet.line();
        boolean over = goals > line;
        return (bet.isOver() == over) ? Outcome.WON : Outcome.LOST;
    }
}

@Service
class SettlementService {
    private final Map<MarketType, SettlementStrategy> strategies;

    SettlementService(List<SettlementStrategy> all) {        // Spring injects every implementation
        this.strategies = all.stream()
            .collect(Collectors.toUnmodifiableMap(SettlementStrategy::type, s -> s));
    }

    Outcome settle(Bet bet, MatchResult result) {
        var strategy = strategies.get(bet.marketType());
        if (strategy == null) throw new IllegalStateException("No settlement for " + bet.marketType());
        return strategy.settle(bet, result);
    }
}
```

Adding a handicap market is now one new `@Component`. `SettlementService` doesn't change, and each strategy is unit-tested on its own.

With Java 21 you could also use a `sealed interface` and a pattern-matching `switch`. The compiler then checks that every case is handled. That suits a **closed** set of variants that rarely changes; Strategy suits an **open** set that grows or is plugged in.

## Pitfalls and follow-ups

- **Strategy vs State?** Same shape. In Strategy the client picks the behaviour; in State the object changes its own behaviour as its internal state moves (for example a bet going `OPEN → SETTLED → PAID`).
- **Strategy vs Template Method?** Strategy uses composition (swap an object); Template Method uses inheritance (override a step). Prefer composition.
- **When is a pattern overkill?** Two stable cases that will never grow: a plain `if` is clearer.
- **Duplicate keys:** `toUnmodifiableMap` throws if two beans claim the same type, which is a good fail-fast check at startup.
- **How does Spring implement Singleton?** One instance per `ApplicationContext`, held in its singleton registry, not a `private static` field. It is not thread-safe by default; keep singleton beans stateless.
- **Which patterns does `@Transactional` use?** Proxy (JDK dynamic proxy or CGLIB). The proxy calls a `TransactionInterceptor`, which delegates to a `PlatformTransactionManager` (a Strategy: JDBC, JPA or JTA). Self-invocation bypasses the proxy.

Related: [E1 · SOLID](../academy/lessons/E1.md), [E2 · Creational patterns](../academy/lessons/E2.md), [E3 · Structural patterns](../academy/lessons/E3.md), [E4 · Behavioural patterns](../academy/lessons/E4.md), [D3 · Spring AOP and the proxy traps](../academy/lessons/D3.md).
