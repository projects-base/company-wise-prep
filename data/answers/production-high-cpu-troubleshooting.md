**Short answer:** First I check user impact and stabilise: roll back if it started with a deploy, otherwise scale out or shed load. Before killing anything I capture evidence (thread dumps, a 60-second profile) from one bad instance.

Then I find *which thread* and *which method* burn the CPU: `top -H`, thread dumps, then a flame graph from JFR or async-profiler. I fix that cause, prove it under load, and add an alert and a runbook line.

## Explanation

**1. Impact and scope (first five minutes)**
- Are latency and errors bad, or only the CPU graph? High CPU with good p99 is not an emergency.
- What changed: deploy, config, feature flag, traffic, data size, a cron job?

**2. Stabilise**
- Roll back or turn the flag off if the timing matches.
- Otherwise scale out, rate limit, or shed low-priority work.

**3. Which kind of CPU?**
- *User time:* your code or the GC.
- *System time:* many system calls, context switches, or network work.
- *Container throttling:* look at `nr_throttled` in the cgroup's `cpu.stat`. You can be slow at 50% node CPU because your limit is small.

**4. Which threads?**

```text
top -H -p <pid>                              # hot native threads (Linux)
jcmd <pid> Thread.print > dump1.txt          # repeat 3 times, 5 s apart
```

- Match the thread id from `top` with `nid=` in the dump. Recent JDKs (checked on 21) print it in decimal; on JDK 8–17 it is hex, so use `printf '%x' <tid>`.
- The dump also has `cpu=...ms` per thread (JDK 11+). Compare two dumps: the thread whose value grows is the hot one, and no `top` is needed.

| Hot threads | Likely cause |
|---|---|
| `GC Thread`, `G1 ...` | memory pressure: look at the heap, not the code |
| `C2 CompilerThread` | JIT warm-up just after a start, or a full code cache |
| Request threads, same method | hot code: regex, JSON, sorting, N+1 mapping, logging |
| One named background thread | a busy loop or a runaway job |
| Many threads, all a little | simply more traffic |

**5. Which method?**

```text
jcmd <pid> JFR.start name=cpu settings=profile duration=60s filename=/tmp/cpu.jfr
jfr view hot-methods /tmp/cpu.jfr            # JDK 21+; or open it in JDK Mission Control
jcmd <pid> GC.heap_info
jstat -gcutil <pid> 1000 10                  # is GC running all the time?
```

- JFR's cost is low enough for production. async-profiler gives a CPU flame graph that also shows native and kernel frames.
- Read a flame graph by *width*, not height: a wide flat top is where the time goes.

**6. Fix, prove, prevent:** compare the profile before and after under load, then add the alert and a blameless post-mortem.

## Example

A thread that spins. `poll()` without a timeout returns at once, so the loop never rests.

```java
import java.util.concurrent.*;

public final class PriceFeed {
    private final BlockingQueue<String> inbox = new LinkedBlockingQueue<>();

    // HOT: poll() returns null at once when the queue is empty, so this loop burns a whole core
    void runHot() {
        while (!Thread.currentThread().isInterrupted()) {
            String message = inbox.poll();
            if (message != null) handle(message);
        }
    }

    // FIXED: the thread is parked, using no CPU, until a message arrives
    void runFixed() throws InterruptedException {
        while (!Thread.currentThread().isInterrupted()) {
            String message = inbox.poll(1, TimeUnit.SECONDS);
            if (message != null) handle(message);
        }
    }

    private void handle(String message) { /* parse and store */ }
}
```

What the tools show for the hot version (JDK 21):

```text
"price-feed" #21 [28684] daemon prio=5 cpu=5156.25ms elapsed=5.58s ... nid=28684 runnable
   java.lang.Thread.State: RUNNABLE
        at PriceFeed.runHot(PriceFeed.java:11)

java.util.concurrent.LinkedBlockingQueue.poll()    345 samples  99.14%
PriceFeed.runHot()                                   3 samples   0.86%
```

5.16 s of CPU in 5.58 s of life is one full core. That line alone names the culprit.

## Pitfalls and follow-ups

- **Restarting first** destroys the evidence. Capture a dump and a profile, then restart.
- **`RUNNABLE` does not mean "using CPU".** A thread in a native socket read is also `RUNNABLE`. Trust the `cpu=` growth.
- **GC is the cause?** Check the heap after a full GC. A leak or a tiny heap makes GC run constantly, so take a heap dump.
- **Common Java causes?** Catastrophic regex backtracking, a retry loop with no delay, logging in a hot loop, huge JSON, a cache that stopped hitting, a spinning compare-and-set under contention.
- **Virtual threads?** `jcmd Thread.print` does not list them. Use `jcmd <pid> Thread.dump_to_file -format=json <file>`; the CPU shows on the carrier threads (`ForkJoinPool-1-worker-N`).
- **Low CPU but slow?** Then threads are waiting: locks, connection pool, a slow dependency.

Deeper: [A8 · Profiling: JFR, flame graphs, JMH](../academy/lessons/A8.md), [H3 · Debugging and production habits](../academy/lessons/H3.md).
