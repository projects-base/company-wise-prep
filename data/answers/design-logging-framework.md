**Short answer:** Modules call a cheap `Logger.log(...)` that only builds a `LogRecord` and puts it on a bounded in-memory buffer. One framework thread drains the buffer in batches and hands each batch to one or more `Sink`s (file, remote tool). Each record carries a sequence number; a sink acknowledges up to a sequence number once it has durably written or the remote tool confirms, and only then is that data considered delivered. When the buffer is full, apply an explicit policy: block, drop low-severity records, or count drops.

## Requirements

- Many modules (threads) push logs concurrently: level, module, message, timestamp.
- Logging must not slow callers noticeably; no I/O on the caller's thread.
- A framework thread streams records to a destination (file or a log tool over the network).
- Acknowledgements: we know which records the destination has accepted, and we retry the rest.
- Bounded memory. Ordered per calling thread. Flush on shutdown.
- Configurable minimum level per module.

## Classes

- `LogLevel` (enum): TRACE, DEBUG, INFO, WARN, ERROR.
- `LogRecord` (record): seq, timestamp, level, module, thread, message.
- `Logger`: per-module facade; checks level, creates the record, offers it to the buffer.
- `LogBuffer`: bounded blocking queue plus overflow policy.
- `Sink` (interface): `long write(List<LogRecord> batch)` returns the highest acked sequence. `FileSink`, `RemoteSink`.
- `Dispatcher`: the framework thread. Drains, writes, waits for ack, retries unacked.
- `OverflowPolicy` (enum or strategy): BLOCK, DROP_NEW, DROP_BELOW_WARN.
- `LogFramework`: wires it up, owns lifecycle (`start`, `shutdown` with flush).

## Patterns used

- **Producer-consumer**: modules produce, the dispatcher consumes; the bounded queue decouples their speeds and gives back-pressure.
- **Facade**: `Logger` hides the buffer and dispatcher from modules.
- **Strategy**: sinks and overflow policies are pluggable (Open/Closed).
- **Factory**: `LogFramework.getLogger(module)` returns a cached logger per module.

## Code

```java
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

enum LogLevel { TRACE, DEBUG, INFO, WARN, ERROR }

record LogRecord(long seq, long timestamp, LogLevel level, String module, String message) {}

interface Sink {
    /** Writes the batch and returns the highest seq the destination has acknowledged. */
    long write(List<LogRecord> batch) throws Exception;
}

final class LogBuffer {
    private final BlockingQueue<LogRecord> queue;
    final AtomicLong dropped = new AtomicLong();

    LogBuffer(int capacity) { queue = new ArrayBlockingQueue<>(capacity); }

    void publish(LogRecord r) throws InterruptedException {
        if (r.level().compareTo(LogLevel.WARN) >= 0) {
            queue.put(r);                         // never lose WARN/ERROR: block if full
        } else if (!queue.offer(r)) {
            dropped.incrementAndGet();            // shed low-severity load
        }
    }

    LogRecord take() throws InterruptedException { return queue.take(); }
    void drainTo(List<LogRecord> out, int max) { queue.drainTo(out, max); }
}

final class Logger {
    private final String module;
    private final LogBuffer buffer;
    private final AtomicLong seq;
    private volatile LogLevel minLevel;

    Logger(String module, LogBuffer buffer, AtomicLong seq, LogLevel minLevel) {
        this.module = module; this.buffer = buffer; this.seq = seq; this.minLevel = minLevel;
    }

    void log(LogLevel level, String message) {
        if (level.compareTo(minLevel) < 0) return;
        try {
            buffer.publish(new LogRecord(seq.incrementAndGet(), System.currentTimeMillis(),
                                         level, module, message));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();   // keep the caller's interrupt status
        }
    }
}

final class Dispatcher implements Runnable {
    private static final int BATCH = 500;
    private final LogBuffer buffer;
    private final Sink sink;
    private final List<LogRecord> pending = new ArrayList<>();   // sent, not yet acked
    private volatile boolean running = true;

    Dispatcher(LogBuffer buffer, Sink sink) { this.buffer = buffer; this.sink = sink; }

    public void run() {
        try {
            while (running || !pending.isEmpty()) {
                if (pending.isEmpty()) {
                    LogRecord first = buffer.take();     // wait for work
                    pending.add(first);
                }
                buffer.drainTo(pending, BATCH - pending.size());
                sendPending();
            }
        } catch (InterruptedException e) {
            buffer.drainTo(pending, Integer.MAX_VALUE);  // shutdown: best-effort final flush
            sendPendingOnce();
        }
    }

    private void sendPending() throws InterruptedException {
        long backoff = 50;
        while (!pending.isEmpty()) {
            if (sendPendingOnce()) return;
            Thread.sleep(backoff);
            backoff = Math.min(backoff * 2, 5_000);
        }
    }

    /** Sends pending, removes everything acked. Returns true if progress was made. */
    private boolean sendPendingOnce() {
        try {
            long ackedUpTo = sink.write(List.copyOf(pending));
            int before = pending.size();
            pending.removeIf(r -> r.seq() <= ackedUpTo);
            return pending.size() < before;
        } catch (Exception e) {
            return false;                                 // keep pending, retry
        }
    }

    void stop(Thread t) { running = false; t.interrupt(); }
}
```

Records from one calling thread stay in order because one dispatcher thread drains a FIFO queue. Records from different threads can interleave, and two threads can enqueue in the opposite order of their `seq` values; that is why the ack is applied as "remove pending records with `seq <= ackedUpTo`" rather than assuming the batch is a contiguous range. For strict per-module order across threads, the module needs its own lock around `seq` and `publish`, which costs throughput.

**Follow-up: three threads exchanging data (read/write)**

A common version: thread A reads input, thread B transforms, thread C writes. Connect them with two bounded queues and a poison pill to stop cleanly.

```java
public class ThreeStagePipeline {
    private static final String POISON = "\u0000EOF";

    public static void main(String[] args) throws InterruptedException {
        BlockingQueue<String> aToB = new ArrayBlockingQueue<>(100);
        BlockingQueue<String> bToC = new ArrayBlockingQueue<>(100);

        Thread reader = Thread.ofPlatform().start(() -> {
            try {
                for (int i = 0; i < 10; i++) aToB.put("line-" + i);
                aToB.put(POISON);
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });

        Thread transformer = Thread.ofPlatform().start(() -> {
            try {
                for (String s; !(s = aToB.take()).equals(POISON); ) bToC.put(s.toUpperCase());
                bToC.put(POISON);
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });

        Thread writer = Thread.ofPlatform().start(() -> {
            try {
                for (String s; !(s = bToC.take()).equals(POISON); ) System.out.println(s);
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });

        reader.join(); transformer.join(); writer.join();
    }
}
```

If the interviewer wants it without `BlockingQueue`, implement a bounded buffer with `ReentrantLock` and two `Condition`s (`notFull`, `notEmpty`), always waiting in a `while` loop to handle spurious wake-ups.

## Extensions

- **Durability:** an in-memory buffer loses records on crash. For audit logs, append to a local file first (write-ahead) and let the dispatcher ship from the file with a stored ack offset, which is how log shippers usually work.
- **Lower contention:** `ArrayBlockingQueue` uses one lock for put and take. A lock-free ring buffer (LMAX Disruptor style) gives higher throughput if logging is very hot.
- **Multiple sinks:** one dispatcher per sink with its own queue, so a slow remote tool does not delay the file sink.
- **Lazy formatting:** accept `Supplier<String>` or a pattern plus args, so disabled levels cost almost nothing.
- **Shutdown hook:** `Runtime.getRuntime().addShutdownHook(...)` calls `stop` and joins with a timeout.
- **Dynamic levels:** `minLevel` is `volatile`, so changing it at runtime is visible to all threads.

Related: [B7 · Classic problems](../academy/lessons/B7.md), [B2 · Locks](../academy/lessons/B2.md), [B4 · Executors and sizing thread pools](../academy/lessons/B4.md).
