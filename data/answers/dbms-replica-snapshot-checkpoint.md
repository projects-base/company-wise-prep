**Short answer:** A **replica** is a live copy of the database on another server, continuously updated from the primary, used for failover and read scaling. A **snapshot** is a frozen point-in-time view: either a storage/backup snapshot of the files, or in MVCC terms the set of committed transactions a query is allowed to see. A **checkpoint** is the moment when the database flushes all dirty pages to disk and records that in the write-ahead log, so crash recovery only needs to replay the WAL written after it. Deadlocks in a DBMS are usually *detected* (wait-for graph, abort a victim) rather than prevented.

## Explanation

**Replica**
- PostgreSQL streaming replication ships WAL records to standbys. Asynchronous: the primary does not wait (possible data loss on failover, replica lag). Synchronous: commit waits for the standby to confirm (no loss, higher latency).
- Replicas protect against server failure, *not* against a bad `DELETE` - that is replicated too.

**Snapshot**
- *Backup snapshot*: a consistent copy at time T (filesystem/EBS snapshot, `pg_basebackup`). Combined with archived WAL it allows point-in-time recovery.
- *MVCC snapshot*: in PostgreSQL each statement (READ COMMITTED) or each transaction (REPEATABLE READ and above) gets a snapshot that decides which row versions are visible. This is how readers never block writers.

**Checkpoint**
- Changes are written to WAL first and to data pages lazily. A checkpoint writes all dirty buffers and records a checkpoint location in WAL.
- After a crash, recovery starts from the last checkpoint and replays WAL forward.
- Trade-off: frequent checkpoints = faster recovery but more I/O (and more full-page writes); rare ones = longer recovery. PostgreSQL controls this with `checkpoint_timeout` and `max_wal_size`.

**Deadlock detection and prevention**
- *Detection*: build a wait-for graph (T1 waits for a lock T2 holds). A cycle is a deadlock; abort one transaction. PostgreSQL runs this check after a lock wait exceeds `deadlock_timeout` (default 1 s) and raises `deadlock detected` (SQLSTATE 40P01) in one transaction.
- *Prevention*: always lock rows in the same order (e.g. by primary key), keep transactions short, use lock timeouts, or timestamp schemes (wait-die, wound-wait) where older transactions get priority.

**Multithreading basics** that usually come with it: a race is two threads accessing shared data with at least one write and no synchronisation; fix it with locks, atomics or confinement. Thread deadlock follows the same four Coffman conditions as transaction deadlock.

## Example

```sql
-- T1                                   -- T2
BEGIN;                                  BEGIN;
UPDATE acct SET bal=bal-10 WHERE id=1;  UPDATE acct SET bal=bal-10 WHERE id=2;
UPDATE acct SET bal=bal+10 WHERE id=2;  UPDATE acct SET bal=bal+10 WHERE id=1;
-- waits for T2                         -- waits for T1 -> one gets 40P01

-- Prevention: both transfers lock the lower id first
SELECT * FROM acct WHERE id IN (1,2) ORDER BY id FOR UPDATE;
```

In Spring, catch the deadlock (Spring translates it to a `PessimisticLockingFailureException` or a subclass) and retry the whole transaction.

## Pitfalls and follow-ups

- **Is a replica a backup?** No. You need snapshots plus WAL archives for recovery from logical mistakes.
- **Read-your-writes with async replicas?** Read from the primary right after a write, or wait for the replica to reach the write's LSN.
- **Checkpoint vs commit?** Commit only needs the WAL record flushed (durability via WAL); data pages can be written later.
- **Why not prevent all deadlocks?** Global ordering is hard across arbitrary queries; detection plus retry is cheaper.

Go deeper: [Q6 · Transactions, isolation, locking and MVCC](../academy/lessons/Q6.md), [Q8 · Scaling databases](../academy/lessons/Q8.md), [B2 · Locks and deadlock](../academy/lessons/B2.md).
