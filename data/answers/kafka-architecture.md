**Short answer:** Kafka is a distributed, append-only commit log. Producers write records to **topics**; each topic is split into **partitions** spread across **brokers**, and each partition is replicated (one leader, followers in the ISR, the in-sync replica set). Consumers in a **consumer group** split the partitions between them and track their position with **offsets**. Records stay for a retention period whether or not anyone read them, so many groups can read the same data independently.

## Explanation

- **Broker:** a Kafka server storing partition data on disk. A cluster has several.
- **Topic and partition:** a partition is an ordered, immutable log; each record gets an increasing offset. Ordering is guaranteed only within a partition.
- **Key → partition:** the default partitioner hashes the record key, so the same key (e.g. `orderId`) always lands in the same partition and keeps its order. No key → spread across partitions.
- **Replication:** each partition has a replication factor (often 3). Producers and consumers talk to the leader; followers copy it. If the leader dies, an in-sync follower becomes leader.
- **Producer durability:** `acks=all` waits for all in-sync replicas; with `min.insync.replicas=2` you survive one broker loss without losing acknowledged writes. Idempotent producers (default on in recent clients) avoid duplicates from retries.
- **Consumer group:** each partition is read by exactly one consumer in the group. Consumers commit offsets to the internal `__consumer_offsets` topic. Different groups each get every record (pub/sub); one group shares the load (queue).
- **Metadata / controller:** older clusters used ZooKeeper. Newer ones use KRaft (Kafka's built-in Raft controller quorum); Kafka 4.0 removed ZooKeeper support entirely.
- **Why it's fast:** sequential disk writes, OS page cache, batching and compression, zero-copy transfer to consumers.

## Example

```text
Topic orders (3 partitions, RF=3)
  P0 [0 1 2 3 4 ...]  leader broker1
  P1 [0 1 2 ...]      leader broker2
  P2 [0 1 2 3 ...]    leader broker3

Group "billing":  consumer A ← P0, P1   consumer B ← P2
Group "email":    consumer C ← P0, P1, P2
```

```java
@KafkaListener(topics = "orders", groupId = "billing")
public void onOrder(OrderPlaced event) {
    billing.charge(event);     // must be idempotent: delivery is at-least-once
}
```

## Pitfalls and follow-ups

- **Ordering vs parallelism (follow-up):** a group's parallelism is capped by the partition count; extra consumers sit idle. More partitions mean more parallelism but ordering only per partition, so pick a key that groups what must stay ordered (all events of one order). A single partition gives global order and no parallelism.
- **Consumer-group rebalance (follow-up):** when a consumer joins, leaves, crashes (missed heartbeats) or takes too long between polls (`max.poll.interval.ms`), the group coordinator reassigns partitions. With the classic eager protocol every consumer stops and gives up its partitions first ("stop the world"); the cooperative-sticky assignor moves only the partitions that need to move. Kafka 4.0 also made the new consumer rebalance protocol (KIP-848) generally available, which moves this logic to the broker. During a rebalance, records processed but not committed are read again by the new owner, so duplicates are possible.
- **Delivery semantics:** commit after processing = at-least-once (duplicates possible); commit before = at-most-once (loss possible); exactly-once needs transactions plus idempotent writes, and only covers Kafka-to-Kafka.
- **Kafka vs RabbitMQ:** Kafka is a replayable log with retention and high throughput; RabbitMQ is a broker that routes messages and deletes them once acknowledged.
- **Poison messages:** retry a few times, then send to a dead-letter topic (Spring Kafka's `DefaultErrorHandler` with `DeadLetterPublishingRecoverer`).
- **Can you reduce partitions?** No, only increase, and increasing changes the key → partition mapping.

Deeper: [F1 · Building blocks: load balancers, caches, queues, databases](../academy/lessons/F1.md), [S9 · Cloud native & event-driven systems](../academy/lessons/S9.md).
