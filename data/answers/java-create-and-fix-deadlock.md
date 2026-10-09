**Short answer:** A deadlock happens when two threads each hold one lock and wait forever for the lock the other holds. To create one, have thread 1 lock A then B, and thread 2 lock B then A. The standard fix is a global lock order: every thread acquires A before B, so the cycle can't form. The alternative is `ReentrantLock.tryLock` with a timeout, so a thread backs off instead of waiting forever.

## Explanation

Four conditions must all hold (Coffman conditions): mutual exclusion, hold and wait, no preemption, circular wait. Breaking any one prevents deadlock. Lock ordering breaks circular wait; `tryLock` with timeout breaks hold-and-wait (the thread releases what it holds and retries).

## Example

**Deadlock:**

```java
public class DeadlockDemo {
    private static final Object A = new Object();
    private static final Object B = new Object();

    public static void main(String[] args) {
        Thread t1 = new Thread(() -> {
            synchronized (A) {
                sleep(100);                 // give t2 time to grab B
                synchronized (B) { System.out.println("t1 done"); }
            }
        });
        Thread t2 = new Thread(() -> {
            synchronized (B) {
                sleep(100);
                synchronized (A) { System.out.println("t2 done"); }
            }
        });
        t1.start(); t2.start();             // hangs forever
    }
    static void sleep(long ms) { try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); } }
}
```

**Fix 1: same lock order everywhere.** Change `t2` to lock `A` then `B`. For dynamic locks, like a bank transfer between two accounts, order by a stable key:

```java
void transfer(Account from, Account to, long amount) {
    Account first  = from.id() < to.id() ? from : to;
    Account second = first == from ? to : from;
    synchronized (first) {
        synchronized (second) {
            from.debit(amount);
            to.credit(amount);
        }
    }
}
```

**Fix 2: `tryLock` with timeout.**

```java
boolean transfer(Account from, Account to, long amount) throws InterruptedException {
    while (true) {
        if (from.lock.tryLock(50, TimeUnit.MILLISECONDS)) {
            try {
                if (to.lock.tryLock(50, TimeUnit.MILLISECONDS)) {
                    try { from.debit(amount); to.credit(amount); return true; }
                    finally { to.lock.unlock(); }
                }
            } finally { from.lock.unlock(); }
        }
        Thread.sleep(ThreadLocalRandom.current().nextInt(10));   // random backoff avoids livelock
    }
}
```

## Pitfalls and follow-ups

- **Lock ordering vs tryLock (follow-up):** ordering is simple, has no retries and is the first choice when you control all lock sites. `tryLock` handles cases where a global order is impractical, but adds retry logic and risks livelock (threads keep backing off in sync) without random backoff; it also needs a cap or deadline in real code.
- **Detecting in production (follow-up):** take a thread dump with `jstack <pid>` or `jcmd <pid> Thread.print`. The JVM reports "Found one Java-level deadlock" and lists which thread holds which monitor and waits for which. Programmatically: `ManagementFactory.getThreadMXBean().findDeadlockedThreads()`. Tools like VisualVM and JDK Mission Control show it too.
- **Database deadlocks** are similar: two transactions update rows in opposite order. PostgreSQL detects them and aborts one transaction; the fix is again a consistent order (e.g. update rows sorted by id) plus a retry.
- **Reduce the risk:** hold locks briefly, never call unknown code (callbacks, remote calls) while holding a lock, prefer higher-level tools (`ConcurrentHashMap`, `java.util.concurrent` queues, atomics) over nested locks.
- **Deadlock vs livelock vs starvation:** blocked forever; busy but making no progress; one thread never gets the resource.
- **Always `unlock()` in `finally`** with explicit locks; `synchronized` releases automatically.

Deeper: [B2 · Locks: synchronized, ReentrantLock, deadlock](../academy/lessons/B2.md).
