**Short answer:** CQRS (command query responsibility segregation) means using separate models for writes and reads. Commands change state through a write model that enforces the business rules, usually a normalised database. Queries read from one or more read models shaped for the screens that use them, such as a denormalised table, a search index or a cache. The read side is kept in sync from the write side's changes, usually through events, so it is **eventually consistent**. It pays off when reads and writes have very different shapes or loads, and it is overkill for plain CRUD.

## Explanation

- **The problem.** One model serving both sides gets pulled two ways: writes want normalised tables and strict invariants, while reads want wide, pre-joined, pre-aggregated data at high volume. Heavy reporting queries also compete with transactions for the same database.
- **Command side.** Validates and applies changes (`PlaceBet`, `CancelOrder`) and returns little more than success or an id. It is the source of truth.
- **Query side.** One or more projections built for specific queries: "my orders" as a single wide table, product search in Elasticsearch, a dashboard total in Redis. They can be scaled and changed independently.
- **Keeping them in sync.** The command side records what changed, and a **projector** updates the read models. Doing it reliably needs the **transactional outbox** (write the event in the same transaction as the state change) or change data capture from the database log, then Kafka, then idempotent consumers. Updating both stores directly in the request is the dual-write problem: one can succeed while the other fails.
- **Levels.** CQRS can be as small as separate read and write classes or DTOs over one database, then a read replica, then separate stores fed by events. Choose the lightest level that solves the problem.

```text
 command ─► Write model (Postgres) ─► outbox ─► Kafka ─► projector ─► Read model(s)
                 source of truth                       idempotent    my-orders table, search index, cache
 query   ───────────────────────────────────────────────────────────► Read model
```

## Example

```java
// Command side: rules and the outbox in one transaction
@Transactional
public OrderId placeOrder(PlaceOrderCommand cmd) {
    Order order = Order.create(cmd);                        // validates invariants
    orders.save(order);
    outbox.save(OutboxEvent.of("OrderPlaced", order.id(), order.toEventJson()));
    return order.id();
}

// Query side: a projector keeps a denormalised view; idempotent so redelivery is harmless
@KafkaListener(topics = "orders")
public void on(OrderPlaced e) {
    jdbc.update("""
        INSERT INTO order_summary (order_id, customer_id, total, status, placed_at)
        VALUES (?, ?, ?, 'PLACED', ?)
        ON CONFLICT (order_id) DO NOTHING
        """, e.orderId(), e.customerId(), e.total(), e.placedAt());
}
```

## Pitfalls and follow-ups

- **CQRS vs event sourcing:** event sourcing stores the events themselves as the source of truth and rebuilds state by replaying them. It needs CQRS for queries, but CQRS does not need event sourcing: most CQRS systems store current state and publish events.
- **Read-your-own-writes:** after a write, the read model may lag. Options: return the new state from the command response, read that user's own data from the write side for a short window, or wait until the projection reaches the event's version.
- **Rebuilding a read model:** replay the events from Kafka (with enough retention) or from the event store into a new table, then switch reads over. That is also how you add a new view later.
- **Never decide from a read model.** It lags; business decisions such as limits, stock and balances are checked on the write side.
- **Ordering and duplicates:** partition events by aggregate id so one aggregate's events stay in order, and make projectors idempotent (upserts, or tracking the last processed version).
- **When not to use it:** simple CRUD, small loads, or a team without the operational maturity for asynchronous pipelines. It adds moving parts, eventual consistency and more to monitor.

Further reading: [DB-SQL-10 · Scaling databases](../academy/lessons/Q8.md), [BE-STORY-10 · Microservices](../academy/lessons/S8.md).
