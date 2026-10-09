**Short answer:** CAP says that a distributed data store, when a network partition happens, must choose between Consistency (every read sees the latest write) and Availability (every request to a non-failed node gets a non-error response). Partitions are not optional in a real network, so the real choice is "CP or AP during a partition". When there is no partition you can have both, and the trade-off becomes latency vs consistency (PACELC).

## Explanation

- **C - Consistency** in CAP means *linearizability*: the system behaves as if there is one copy of the data. This is not the "C" in ACID.
- **A - Availability**: every request received by a non-failing node returns a response (not an error or timeout). It is not about uptime percentages.
- **P - Partition tolerance**: the system keeps working even if messages between nodes are lost.

Why you cannot have all three: suppose nodes N1 and N2 cannot talk. A client writes `x=2` to N1, another client reads `x` from N2. N2 can either answer with the old value (available, not consistent) or refuse / wait until the partition heals (consistent, not available).

**CP systems** reject or block some requests during a partition: ZooKeeper, etcd, a single-leader PostgreSQL setup with synchronous replication that refuses writes without quorum, HBase.
**AP systems** keep answering and reconcile later: Cassandra and DynamoDB-style stores at low consistency levels, DNS, shopping-cart style designs.

Many systems are tunable. In Cassandra, `QUORUM` reads and writes (R + W > N) behave closer to CP; `ONE` behaves AP.

**PACELC** extends CAP: if Partition, choose A or C; Else, choose Latency or Consistency. Synchronous replication to a remote replica buys consistency but adds latency on every write, even with no failures.

## Example

A Spring Boot service writing orders to a primary PostgreSQL with an async read replica:

```text
Client -> write order -> Primary  --(async WAL)-->  Replica <- read "my orders"
```

If the replica lags or is cut off, reads from it are stale: that path is AP-ish. Fixes: read-your-writes by routing a user's reads to the primary for a short window after they write, or synchronous replication (CP, higher write latency).

## Pitfalls and follow-ups

- **"Pick any two" is misleading.** You cannot give up P in a distributed system; CA only exists on a single node.
- **Is a CP system ever unavailable without a partition?** It can be, for example during leader election.
- **Eventual consistency?** If writes stop, all replicas converge. Conflicts are resolved with last-write-wins, vector clocks or CRDTs.
- **Consensus link:** CP systems usually use Raft or Paxos with a majority quorum; the minority side of a partition stops accepting writes.
- **Where does a cache like Redis sit?** A single primary with async replicas can lose acknowledged writes on failover, so treat it as AP unless configured otherwise.

Go deeper: [F2 · Distributed theory: CAP, consistency, consensus](../academy/lessons/F2.md), [Q8 · Scaling databases](../academy/lessons/Q8.md).
