**Short answer:** The machine's behaviour depends on its state (idle, has money, dispensing, out of service), so use the State pattern: each state is a class that handles `insertCoin`, `select`, and `cancel` in its own way and returns the next state. The `VendingMachine` is the context that holds inventory, the current balance and the current state. Things that vary by policy, such as how change is made or how payment is taken, are Strategies. SOLID shows up naturally: each state has one job, new states or payment types are added without editing the old ones, and the machine depends on interfaces.

## Requirements

- Slots hold products with a price and a count.
- User inserts coins (or notes), selects a slot, gets the product and change.
- Cancel returns the inserted money.
- Reject selection if the slot is empty or the balance is too low; reject sale if exact change cannot be given.
- Operator can restock and put the machine out of service.
- Single user at a time (it is one physical machine), but calls must not corrupt state.

## Classes

- `Coin` (enum with value in cents), `Product` (record), `Slot` (product plus count).
- `Inventory`: slots by code; `isAvailable`, `take`, `restock`.
- `CoinBox`: coins held by denomination; accepts coins and pays out change.
- `ChangeStrategy` (interface): `Optional<Map<Coin,Integer>> makeChange(amount, CoinBox)`. `GreedyChange` is the default.
- `State` (sealed interface): `IdleState`, `HasMoneyState`, `OutOfServiceState`. Dispensing is done inside the transition, so there is no long-lived dispensing state here.
- `VendingMachine`: the context. Holds `state`, `balance`, `Inventory`, `CoinBox`, `ChangeStrategy`. Public methods delegate to the state.
- `Dispenser` (interface): drives the hardware; mocked in tests.

## Patterns used

- **State**: removes the big `if (state == ...)` blocks from every method. Each state handles every event, so illegal actions get a clear answer ("insert money first").
- **Strategy**: `ChangeStrategy` (and later a `PaymentMethod` for card or UPI) can change without touching states.
- **SOLID, explicitly:**
  - S: `Inventory`, `CoinBox`, states and the machine each have one reason to change.
  - O: a new state (for example `MaintenanceState`) or payment method is a new class.
  - L: any `State` can stand in for another; none throws "unsupported" for a normal event.
  - I: the operator gets a separate `AdminOperations` interface; customers never see `restock`.
  - D: the machine depends on `ChangeStrategy` and `Dispenser` interfaces, injected in the constructor.

## Code

```java
public enum Coin { NICKEL(5), DIME(10), QUARTER(25), DOLLAR(100);
    final int cents; Coin(int c) { cents = c; } }

public record Product(String name, int priceCents) {}

public sealed interface State permits IdleState, HasMoneyState, OutOfServiceState {
    State insertCoin(VendingMachine m, Coin c);
    State select(VendingMachine m, String slot);
    State cancel(VendingMachine m);
}

public final class IdleState implements State {
    public State insertCoin(VendingMachine m, Coin c) { m.accept(c); return new HasMoneyState(); }
    public State select(VendingMachine m, String slot) { m.display("Insert money first"); return this; }
    public State cancel(VendingMachine m) { return this; }
}

public final class HasMoneyState implements State {
    public State insertCoin(VendingMachine m, Coin c) { m.accept(c); return this; }

    public State select(VendingMachine m, String slot) {
        if (!m.inventory().isAvailable(slot)) { m.display("Sold out"); return this; }
        int price = m.inventory().price(slot);
        if (m.balance() < price) { m.display("Insert " + (price - m.balance()) + " more"); return this; }

        Optional<Map<Coin, Integer>> change = m.changeStrategy().makeChange(m.balance() - price, m.coinBox());
        if (change.isEmpty()) { m.display("Cannot make change, use exact amount"); return this; }

        m.inventory().take(slot);
        m.dispenser().dispenseProduct(slot);
        m.coinBox().payOut(change.get());
        m.dispenser().dispenseCoins(change.get());
        m.resetBalance();
        return new IdleState();
    }

    public State cancel(VendingMachine m) { m.refundBalance(); return new IdleState(); }
}

public final class OutOfServiceState implements State {
    public State insertCoin(VendingMachine m, Coin c) { m.dispenser().dispenseCoins(Map.of(c, 1)); return this; }
    public State select(VendingMachine m, String slot) { m.display("Out of service"); return this; }
    public State cancel(VendingMachine m) { return this; }
}

public final class VendingMachine {
    private final Inventory inventory;
    private final CoinBox coinBox;
    private final ChangeStrategy changeStrategy;
    private final Dispenser dispenser;
    private final List<Coin> inserted = new ArrayList<>();
    private State state = new IdleState();
    private int balance;

    public VendingMachine(Inventory inv, CoinBox box, ChangeStrategy cs, Dispenser d) {
        this.inventory = inv; this.coinBox = box; this.changeStrategy = cs; this.dispenser = d;
    }

    // Public API: synchronized so two callers (keypad and coin slot threads) cannot interleave.
    public synchronized void insertCoin(Coin c)  { state = state.insertCoin(this, c); }
    public synchronized void select(String slot) { state = state.select(this, slot); }
    public synchronized void cancel()            { state = state.cancel(this); }

    // Used by states.
    void accept(Coin c) { coinBox.add(c); inserted.add(c); balance += c.cents; }
    void resetBalance() { balance = 0; inserted.clear(); }
    void refundBalance() { inserted.forEach(coinBox::remove); dispenser.dispenseCoins(countOf(inserted)); resetBalance(); }
    int balance() { return balance; }
    Inventory inventory() { return inventory; }
    CoinBox coinBox() { return coinBox; }
    ChangeStrategy changeStrategy() { return changeStrategy; }
    Dispenser dispenser() { return dispenser; }
    void display(String msg) { System.out.println(msg); }

    private static Map<Coin, Integer> countOf(List<Coin> coins) {
        Map<Coin, Integer> m = new EnumMap<>(Coin.class);
        coins.forEach(c -> m.merge(c, 1, Integer::sum));
        return m;
    }
}
```

**Change making:** `GreedyChange` takes the largest coin that fits while the box has it. Greedy is optimal for canonical coin systems like the US set, but with a limited coin supply it can fail when a solution exists (need 30 with one quarter and three dimes: greedy takes the quarter and gets stuck). A small DP over the available counts fixes that; it is a drop-in `ChangeStrategy`, which is the point of the Strategy.

## Extensions

- **Why State over an enum + switch:** with a switch, every new state edits every method (violates O). With an enum, you can still put behaviour per constant; that is fine for tiny machines. Classes win when states hold data or need injection.
- **Card / UPI payment:** a `PaymentMethod` strategy with `authorize(amount)` and `capture()`. Capture only after the product is dispensed; void the authorization on a jam.
- **Dispense failure:** if the motor reports a jam, refund and move to `OutOfServiceState`. That is easy to add because transitions are explicit.
- **Timeout:** if money sits for 60 s, auto-cancel. A scheduled task calls `cancel()`, which is safe because the public methods are synchronized.
- **Observer:** notify the operator when a slot reaches zero or the coin box cannot make change.
- **Parking lot variant** (the question says "or parking lot"): the same discussion applies: a ticket's lifecycle (issued, paid, exited) is a State, and pricing is a Strategy.

Deeper reading: [E1 · SOLID, by violation and refactor](../academy/lessons/E1.md), [E4 · Behavioural patterns](../academy/lessons/E4.md), [E6 · LLD case studies](../academy/lessons/E6.md).
