**Short answer:** Model the order lifecycle as a small state machine: an enum of states where each state knows which event moves it forward. Keep a `Map<orderId, State>`, apply events in order, and ignore any event that is not a valid transition. At the end, orders in DELIVERED are completed and orders in CANCELLED are cancelled. One pass, O(n) plus sorting the two result lists.

## Approach

**Naive.** Collect every status seen per order into a set, then say "completed if it has all four stages, cancelled if it has `cancelled`". This is wrong for out-of-order or duplicate events: a `processed` before `placed`, or a `cancelled` after `delivered`, would be counted. The order of events matters.

**Key insight.** The rules are a state machine. Each order is in exactly one state, and each event either moves it to the next state or is ignored. Put the allowed transitions in one place (the enum), so the business rule is readable and easy to change.

**Optimal.** One pass over the events with a hash map of current states, then one pass over the map to classify.

## Solution

```java
import java.util.*;

class Solution {
    enum State {
        NEW, PLACED, PROCESSED, SHIPPED, DELIVERED, CANCELLED;

        /** The state after `event`, or this state unchanged if the event is not allowed here. */
        State on(String event) {
            switch (event) {
                case "placed": return this == NEW ? PLACED : this;
                case "processed": return this == PLACED ? PROCESSED : this;
                case "shipped": return this == PROCESSED ? SHIPPED : this;
                case "delivered": return this == SHIPPED ? DELIVERED : this;
                case "cancelled": return this == PLACED || this == PROCESSED || this == SHIPPED ? CANCELLED : this;
                default: return this;
            }
        }
    }

    public List<List<Integer>> classifyOrders(int[] orderIds, String[] statuses) {
        Map<Integer, State> state = new HashMap<>();
        for (int i = 0; i < orderIds.length; i++) {
            State cur = state.getOrDefault(orderIds[i], State.NEW);
            state.put(orderIds[i], cur.on(statuses[i]));
        }
        List<Integer> completed = new ArrayList<>(), cancelled = new ArrayList<>();
        for (Map.Entry<Integer, State> e : state.entrySet()) {
            if (e.getValue() == State.DELIVERED) completed.add(e.getKey());
            else if (e.getValue() == State.CANCELLED) cancelled.add(e.getKey());
        }
        Collections.sort(completed);
        Collections.sort(cancelled);
        return List.of(completed, cancelled);
    }
}
```

In Java 21 you can write `on` as a switch expression (`return switch (event) { case "placed" -> ...; ... };`). In a real codebase the event would be an enum too, not a raw string, so the compiler checks that every event is handled.

## Complexity

- **Time:** O(n + m log m), where n is the number of events and m the number of distinct orders (for sorting the outputs).
- **Space:** O(m) for the state map.

## Edge cases

- An event before `placed`: ignored, the order stays NEW.
- Duplicates (`placed` twice): the second is ignored.
- A skipped stage (`shipped` right after `placed`): ignored, and so is everything that depends on it (example 2).
- Anything after DELIVERED or CANCELLED: ignored, both are terminal.
- An order with only invalid events appears in neither list.

## Variations

- **Cleaner model for a design discussion:** an `Order` class holding its id, state and a history of events; an `OrderStatus` enum with an `EnumMap<OrderStatus, Set<OrderStatus>>` of allowed transitions; an `OrderTracker` that applies events and answers queries. That keeps the transition table as data, which is easy to extend (for example adding RETURNED after DELIVERED).
- **Strict mode:** throw or log on invalid transitions instead of silently ignoring them, so bad upstream data is visible.
- **Out-of-order delivery from a queue:** buffer by order id and sort by event timestamp before applying.

See [E4 · Behavioural patterns](../academy/lessons/E4.md) for the State pattern behind this design.

Practise it in the app: Run / Submit on this page.
