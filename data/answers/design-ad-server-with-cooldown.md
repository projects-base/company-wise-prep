**Short answer:** Keep the ads that may be served in a max-heap ordered by score. `getAd()` polls the top ad, lowers its score and puts it in a FIFO "cooling" queue with the tick at which it becomes eligible again. At the start of every call, ads whose cooldown has ended go back into the heap. With a gap of 1 this is exactly "never the same ad twice in a row"; a bigger gap is the follow-up. Each call costs O(log n).

## Requirements

- `insertAd(content, score)` adds an ad. Scores are integers; ties break by insertion order.
- `getAd()` returns the highest-scored eligible ad, then lowers its score by a fixed `decrement` (assume 1).
- The same ad is never returned twice in a row. Follow-up: an ad served at call `t` may not be served again until call `t + gap + 1`.
- If no ad is eligible (for example, only one ad exists), return empty instead of breaking the rule.
- Single-threaded first; thread safety is an extension.

## Classes

- `Ad`: id, content, mutable score. Its score changes only while it is **outside** the heap.
- `Cooling`: a record of (ad, readyAt tick).
- `AdServer`: owns the `ready` heap, the `cooling` deque, a logical clock `tick` (one tick per `getAd` call) and the configuration (`gap`, `decrement`).

The key observation: every call puts exactly one ad into `cooling`, and `readyAt = tick + gap + 1` grows with `tick`. So the deque is already sorted by `readyAt`, and a plain `ArrayDeque` is enough. No second heap is needed. At most one ad leaves cooling per call.

## Patterns used

- No GoF pattern is needed here. It is a data-structure design: a **priority queue plus a time-ordered queue**. This is the same idea as "rearrange string k distance apart" and LeetCode's task scheduler.
- Small **Strategy** option: pass the score-decay rule as an `IntUnaryOperator` if the interviewer wants different decay rules.

## Code

```java
import java.util.*;

public final class AdServer {

    private static final class Ad {
        final long id;
        final String content;
        int score;
        Ad(long id, String content, int score) { this.id = id; this.content = content; this.score = score; }
    }

    private record Cooling(Ad ad, long readyAt) {}

    private final PriorityQueue<Ad> ready = new PriorityQueue<>(
            Comparator.comparingInt((Ad a) -> a.score).reversed()
                      .thenComparingLong(a -> a.id));
    private final ArrayDeque<Cooling> cooling = new ArrayDeque<>();
    private final int gap;        // 1 = no consecutive repeats
    private final int decrement;
    private long tick;
    private long nextId;

    public AdServer(int gap, int decrement) {
        if (gap < 1) throw new IllegalArgumentException("gap must be >= 1");
        this.gap = gap;
        this.decrement = decrement;
    }

    public void insertAd(String content, int score) {
        ready.add(new Ad(nextId++, content, score));
    }

    public Optional<String> getAd() {
        tick++;
        // release ads whose cooldown has ended; the deque is sorted by readyAt
        while (!cooling.isEmpty() && cooling.peekFirst().readyAt() <= tick) {
            ready.add(cooling.pollFirst().ad());
        }
        Ad ad = ready.poll();
        if (ad == null) return Optional.empty();          // nothing eligible right now
        ad.score -= decrement;                             // safe: ad is out of the heap
        cooling.addLast(new Cooling(ad, tick + gap + 1));
        return Optional.of(ad.content);
    }
}
```

Trace with gap 1: the ad served at tick 1 has `readyAt = 3`. At tick 2 it is still cooling, so a different ad is served. At tick 3 it is back in the heap.

## Extensions

- **Follow-up: a cooldown gap while staying near O(1).** The code above already supports any `gap`. `insertAd` is O(log n) and `getAd` is O(log n), because it does one release and one poll. The cooling side is O(1). For true O(1): if scores are small bounded integers and drop by 1, use buckets `score -> LinkedHashSet<Ad>` and a pointer `maxScore`. Moving an ad to the next bucket down is O(1), and the pointer only walks down over empty buckets. That walk is amortised O(1) when scores are dense. With sparse or real-valued scores, stay with the heap.
- **Why not "poll the top, and if it was the last ad, poll the second"?** That works for gap 1, but it does not generalise to a gap of k. The cooling queue does.
- **Never mutate a key while it is in the heap.** `PriorityQueue` does not reorder itself, so the heap invariant would break. Here the score changes only after `poll()`.
- **Remove or update an ad:** use lazy deletion. Keep a `Map<id, Ad>` with a `deleted` flag and skip deleted ads when they reach the top. `PriorityQueue.remove(Object)` would cost O(n).
- **Thread safety:** make `insertAd` and `getAd` `synchronized`, or guard them with one `ReentrantLock`. The two structures must change together, so a concurrent collection for each one separately is not enough.
- **Wall-clock cooldown** (for example, "not again within 10 s"): store `readyAt` as an `Instant` from an injected `Clock`. The deque stays sorted because time only moves forward.

Related: [C5 · Amortised analysis](../academy/lessons/C5.md), [B2 · Locks](../academy/lessons/B2.md).
