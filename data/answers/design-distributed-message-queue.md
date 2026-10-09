**Short answer:** I would build it like Kafka: a topic is split into partitions, and each partition is an append-only log replicated across several brokers with one leader. Producers pick a partition by hashing the message key, so all messages for one key stay in order. Consumers in a group split the partitions between them and track their position as a committed offset. Durability comes from replication (acknowledge a write only after enough replicas have it), and scale comes from adding partitions and brokers.

## Requirements

**Functional**
- Producers publish messages to a named topic.
- Many consumers read. Each consumer group gets every message once (load-shared inside the group), and different groups read independently (pub/sub).
- Messages for the same key are delivered in order.
- Messages are retained for a configurable time so a consumer can replay.

**Non-functional**
- High throughput (hundreds of thousands of messages per second), low latency (tens of ms).
- Durable: an acknowledged message survives a broker crash.
- Highly available: one broker failing does not stop producers or consumers.
- At-least-once delivery by default; effectively-once with idempotent consumers.

## Estimates

- Say 200k messages/s at peak, 1 KB each: about 200 MB/s in. With replication factor 3, about 600 MB/s of disk writes across the cluster.
- 7 days of retention: 200 MB/s × 86,400 s × 7 ≈ 120 TB raw, × 3 replicas ≈ 360 TB. That decides the broker count and the disks.
- If one partition handles around 10 MB/s comfortably (a figure to measure, not to trust), 200 MB/s needs at least 20 partitions. Start higher (for example 48) to allow growth and consumer parallelism.

## API

```text
produce(topic, key, value, headers) -> (partition, offset)        acks = 0 | 1 | all
poll(groupId, consumerId, maxMessages, timeout) -> [ (partition, offset, key, value) ]
commit(groupId, { partition -> offset })
createTopic(name, partitions, replicationFactor, retention)
```

## Data model

- **Partition log** on disk: a sequence of segment files (for example 1 GB each). Each record has an offset, a timestamp, key, value and headers. Each segment has a sparse index from offset to file position, so finding offset N is a binary search plus a short scan.
- **Metadata** (in a consensus store: Kafka uses KRaft, older versions ZooKeeper): topics, partition count, which broker leads each partition, the in-sync replica set (ISR).
- **Consumer offsets:** stored in an internal, compacted topic keyed by `(group, topic, partition)`.

## Architecture

```text
 Producers ──hash(key) % P──►  Broker 1 [P0 leader, P1 follower, P2 follower]
                               Broker 2 [P1 leader, P2 follower, P0 follower]
                               Broker 3 [P2 leader, P0 follower, P1 follower]
                                     ▲          │ followers fetch from leaders
                                     │          ▼
                         Controller quorum (metadata, leader election)

 Consumer group "settlement":   C1 ◄─ P0, P1     C2 ◄─ P2
 Consumer group "analytics":    C3 ◄─ P0, P1, P2      (independent offsets)
```

- **Producers** batch and compress records per partition, then send to the partition leader.
- **Brokers** append to the log (sequential disk writes and the OS page cache make this fast) and serve reads with zero-copy `sendfile`.
- **Followers** pull from the leader. A follower that keeps up is in the ISR.
- **Controller** detects broker failure and elects a new leader from the ISR.
- **Group coordinator** assigns partitions to the consumers in a group and rebalances when one joins or dies.

## Deep dives

**1. Ordering, partitions and consumer groups.** Order is guaranteed only inside a partition. Use the business key (for example `betId` or `accountId`) as the message key, so all events for one entity go to one partition and are processed in order. Inside a group, each partition is read by exactly one consumer, so the useful number of consumers is at most the number of partitions. Adding partitions later changes `hash(key) % P` and breaks per-key order during the switch, so size partitions up front.

**2. Replication and durability.** With `acks=all`, the leader acknowledges only when every replica in the ISR has the record. With `min.insync.replicas=2` and replication factor 3, the topic survives one broker loss without losing acknowledged data. If the ISR shrinks below the minimum, producers get errors instead of silent data loss. Never elect a replica that is not in the ISR (unclean leader election) when data loss is unacceptable.

**3. Delivery semantics.**
- *At-most-once:* commit the offset, then process. A crash loses messages.
- *At-least-once:* process, then commit. A crash after processing but before the commit redelivers the message. This is the usual default.
- *Exactly-once:* Kafka offers an idempotent producer (sequence numbers per partition, so retries do not create duplicates) and transactions that write output and commit offsets atomically. That covers Kafka-to-Kafka flows. Once you call a database or an external API, you need an **idempotent consumer**: store the processed message ID (or a business key) with a unique constraint in the same database transaction as the side effect, and skip duplicates.

```sql
-- In the same transaction as the wallet update
INSERT INTO processed_messages (message_id) VALUES (:id)
ON CONFLICT (message_id) DO NOTHING;   -- 0 rows inserted => duplicate, skip the side effect
```

**4. Consumer failure and poison messages.** If processing fails repeatedly, retry a few times with backoff, then move the message to a dead-letter topic so one bad message does not block the partition.

**5. Publishing reliably from a service.** Writing to the database and then to the queue is a dual write; a crash in between loses the event. Use the **transactional outbox**: insert the event into an `outbox` table in the same transaction as the business change, and a relay (a poller or CDC such as Debezium) publishes it.

## Trade-offs

- **Log (Kafka) vs broker queue (RabbitMQ, SQS):** a log keeps messages after they are read, supports replay and many independent groups, and scales by partitions. A classic queue deletes on acknowledgement and has per-message routing, priorities and simpler work-queue semantics, but no replay.
- **`acks=all` vs `acks=1`:** durability vs a few milliseconds of latency.
- **More partitions:** more parallelism, but more open files, slower leader elections and longer rebalances.
- **Pull vs push:** pull lets consumers control their own rate (natural backpressure) at the cost of polling.

## Follow-ups

- **Ordering per partition; consumer groups and offsets?** Covered in deep dive 1. The committed offset is the next offset the group will read. Resetting it lets you replay.
- **At-least-once vs exactly-once; idempotent consumers?** Covered in deep dive 3. In practice: at-least-once delivery plus an idempotent consumer.
- **Code a thread-safe bounded blocking queue:** see the separate question `design-bounded-blocking-queue`. Core idea: one `ReentrantLock` with two conditions, `notFull` and `notEmpty`. `put` waits in a `while (count == capacity)` loop on `notFull`, then signals `notEmpty`; `take` does the reverse. `java.util.concurrent.ArrayBlockingQueue` is the library version.
- **How would a betting platform use it?** Bet placed, odds changed and market settled events, keyed by market or bet ID. The settlement service consumes, debits or credits the wallet idempotently, and publishes results.

Further reading: [F1 · Building blocks](../academy/lessons/F1.md), [B7 · Classic problems: bounded queue](../academy/lessons/B7.md), [F2 · Distributed theory](../academy/lessons/F2.md), [F8 · Design a sports betting platform](../academy/lessons/F8.md).
