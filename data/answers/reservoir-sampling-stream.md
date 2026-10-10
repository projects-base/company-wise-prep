**Short answer:** Reservoir sampling with a reservoir of size one. Keep a count `n` of elements seen and one chosen element. When the n-th element arrives, replace the chosen element with it with probability `1/n`. At any moment, every element seen so far is the chosen one with probability exactly `1/n`. O(1) memory and O(1) work per element.

## Picture it

Example 1: stream 10, 20, 30, 40. The chance that each element is the current choice after every `add`:

| After add | New element takes over with | P(10) | P(20) | P(30) | P(40) |
|---|---|---|---|---|---|
| 1 (10) | 1/1 | 1 | — | — | — |
| 2 (20) | 1/2 | 1 · 1/2 = 1/2 | 1/2 | — | — |
| 3 (30) | 1/3 | 1/2 · 2/3 = 1/3 | 1/2 · 2/3 = 1/3 | 1/3 | — |
| 4 (40) | 1/4 | 1/3 · 3/4 = 1/4 | 1/4 | 1/3 · 3/4 = 1/4 | 1/4 |

Every row is uniform, so `sample()` is correct whenever it is called. One possible run: keep 10, keep 10 (no takeover), take 30, keep 30 → `sample()` returns 30.

**The picture in one sentence:** the i-th element takes over with probability 1/i, and each survival factor (j − 1)/j telescopes so that every element ends at exactly 1/n.

## Approach

- **Brute force.** Store everything, then pick a random index. O(n) memory, and with an unbounded stream it runs out of memory.
- **Key insight.** You can keep the answer valid after every element, so it does not matter when the stream stops. The rule "the i-th element takes over with probability 1/i" gives a uniform choice at every prefix.
- **Proof.** Element `i` is chosen when it arrives with probability `1/i`. It then survives the arrival of element `j > i` with probability `1 - 1/j = (j-1)/j`. After `n` elements, the probability it is still chosen is
  `1/i × i/(i+1) × (i+1)/(i+2) × … × (n-1)/n = 1/n`. The product telescopes. Element 1 is always chosen first (probability 1/1), which matches the formula.

## Solution

```java
import java.util.Random;

class StreamSampler {
    private final Random rng;
    private long seen;
    private int chosen;

    public StreamSampler(Random rng) {
        this.rng = rng;
    }

    public void add(int x) {
        seen++;
        // keep the new element with probability 1/seen
        boolean take = seen <= Integer.MAX_VALUE
                ? rng.nextInt((int) seen) == 0
                : rng.nextDouble() * seen < 1;
        if (take) chosen = x;
    }

    public int sample() {
        return chosen;
    }
}
```

`seen` is a `long` because an infinite stream passes 2³¹ elements; `Random.nextInt(bound)` takes an `int`, so past that point the code falls back to a `double` comparison. In Java 17+ you can use `RandomGenerator.nextLong(bound)` instead.

## Complexity

- **Time:** O(1) per `add` and per `sample`.
- **Space:** O(1): a counter and one value.

## Edge cases

- One element seen: it is returned with probability 1.
- Repeated values: probabilities are per position, so a value appearing twice in three elements comes back 2/3 of the time. The algorithm handles that automatically.
- `sample()` called many times without new elements returns the same value; each call is not an independent draw. If independent draws are required, you cannot do it in O(1) space without the stream.
- Using `rng.nextInt(seen) == seen - 1` or any single fixed outcome is equally valid; `== 0` is just the common form.

## Variations

- **Sample k elements (reservoir of size k):** keep the first k. For element `i > k`, draw `j` uniform in `[0, i)`; if `j < k`, replace slot `j`. Each element ends up in the reservoir with probability `k/n`.
- **Weighted sampling:** element with weight `w` takes over with probability `w / (total weight so far)`. For k items, use the A-Res key `u^(1/w)` with a min-heap of size k.
- **Linked List Random Node (LC 382):** the same algorithm over a list of unknown length.
- **Distributed streams:** sample on each shard, then merge by choosing shard `s` with probability proportional to its count.

Practise it in the app: Run / Submit on this page.
