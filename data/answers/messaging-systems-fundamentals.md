**Short answer:** A message broker lets services talk asynchronously: the producer hands a message to the broker and moves on, and consumers process it when they can. That decouples services in time and availability, absorbs traffic spikes and lets several consumers react to one event. Two models: **point-to-point queues** (each message goes to one consumer, e.g. a job queue in RabbitMQ or a JMS queue) and **publish/subscribe** (each subscriber group gets every message, e.g. Kafka topics or JMS topics). The cost is eventual consistency, duplicate deliveries, ordering limits and more moving parts to operate.

## Explanation

**When to use it instead of a REST call:** the caller does not need the result right away (send email, update search index, settle bets after a match ends), many services must react to one event, or the downstream system is slower or less available than the caller.

**Kafka vs a traditional queue (RabbitMQ, ActiveMQ/JMS):**

| | RabbitMQ / JMS queue | Kafka |
|---|---|---|
| Model | Broker pushes to consumers; a message is removed once acknowledged | Append-only log; consumers pull and track their own offset |
| Replay | No (once consumed, gone) | Yes, within the retention period |
| Ordering | Per queue, weakened by multiple consumers and redelivery | Per partition, guaranteed |
| Scaling consumers | Add competing consumers | Add partitions; one consumer per partition within a group |
| Strong at | Task queues, routing, per-message acks, delays | High-throughput event streams, event sourcing, many independent readers |

**Delivery guarantees:**
- *At most once:* acknowledge before processing. May lose messages.
- *At least once:* acknowledge after processing. May deliver duplicates. This is the common default.
- *Exactly once:* Kafka supports it inside Kafka (idempotent producer plus transactions); end to end with a database you still design **idempotent consumers** (dedupe on a message id, or use upserts).

**Failure handling:** retry with backoff, then move poison messages to a **dead-letter queue** so one bad message does not block the rest. Monitor consumer lag.

**The dual-write problem:** saving to the database and publishing to the broker are two systems; if one fails, they disagree. The **transactional outbox** pattern writes the event to an `outbox` table in the same database transaction, and a relay (a poller or CDC like Debezium) publishes it.

## Example

Spring Boot with Spring for Apache Kafka:

```java
@Service
class BetEvents {
    private final KafkaTemplate<String, BetPlaced> kafka;
    BetEvents(KafkaTemplate<String, BetPlaced> kafka) { this.kafka = kafka; }

    void publish(BetPlaced event) {
        kafka.send("bets.placed", event.userId(), event);   // key = userId keeps one user's bets in order
    }
}

@Component
class WalletListener {
    private final ProcessedEvents processed;                // table with a unique event_id
    private final WalletService wallet;
    WalletListener(ProcessedEvents processed, WalletService wallet) {
        this.processed = processed; this.wallet = wallet;
    }

    @KafkaListener(topics = "bets.placed", groupId = "wallet")
    @Transactional
    void on(BetPlaced event) {
        if (!processed.markIfNew(event.eventId())) return;   // duplicate delivery: skip
        wallet.reserve(event.userId(), event.stake());
    }
}
```

In the interview, describe your own usage concretely: **[which broker]**, **[what flowed through it]**, **[how you handled retries, duplicates and ordering]**, **[one problem you hit and fixed]**.

## Pitfalls and follow-ups

- **How do you keep ordering?** Use a partition key (Kafka) so related messages land in one partition; with competing consumers on a queue, ordering is lost unless you use single-active-consumer or message groups.
- **Consumer is slower than producer?** Lag grows; add partitions and consumers, batch processing, or apply backpressure. The broker buffers, but not forever (retention, disk).
- **Why not just call the service?** Synchronous calls chain latency and failures: if the downstream is down, so are you. Messaging trades that for eventual consistency.
- **Schema changes?** Version events, add fields in a backward-compatible way, consider a schema registry (Avro/Protobuf).
- **Request/reply over a queue** is possible (reply-to and correlation id) but usually a sign REST or gRPC fits better.

Go deeper: [F1 · Building blocks: load balancers, caches, queues, databases](../academy/lessons/F1.md), [S9 · Cloud native & event-driven systems](../academy/lessons/S9.md), [S7B · SOA & the enterprise service bus](../academy/lessons/S7B.md).
