**Short answer:** Use two heaps. A max-heap `low` holds the smaller half and a min-heap `high` holds the larger half. Keep every value in `low` ≤ every value in `high`, and keep `low` the same size as `high` or one bigger. The median is then `low.peek()` (odd count) or the average of the two tops (even count). `addNum` is O(log n) and `findMedian` is O(1).

## Picture it

Stream `5, 15, 1, 3, 8`. Each `addNum` pushes into `low`, moves `low`'s max to `high`, then moves `high`'s min back if `high` got bigger. Heaps are shown as sorted lists; the top is in **bold**.

| add | after push to low | after low → high | after rebalance | low (max-heap) | high (min-heap) | median |
|---|---|---|---|---|---|---|
| 5 | low [5] | 5 up: low [], high [5] | high bigger: 5 back | [**5**] | [] | 5 |
| 15 | low [**15**, 5] | 15 up | sizes 1 = 1, no move | [**5**] | [**15**] | (5 + 15) / 2 = 10.0 |
| 1 | low [**5**, 1] | 5 up: high [5, 15] | high bigger: 5 back | [**5**, 1] | [**15**] | 5 |
| 3 | low [**5**, 3, 1] | 5 up: high [5, 15] | sizes 2 = 2, no move | [**3**, 1] | [**5**, 15] | (3 + 5) / 2 = 4.0 |
| 8 | low [**8**, 3, 1] | 8 up: high [5, 8, 15] | high bigger: 5 back | [**5**, 3, 1] | [**8**, 15] | 5 |

Check: the sorted stream is `1 3 5 8 15`, median 5.

**The picture in one sentence:** split the numbers at the middle into two heaps, so the median is always sitting on top of one or both of them.

## Approach

- **Brute force:** store everything and sort on each `findMedian`, which is O(n log n) per query. Keeping a sorted `ArrayList` with binary-search insertion is O(n) per insert because of shifting. With a median query after every insert, that is O(n²) overall.
- **Key insight:** the median only depends on the boundary between the lower half and the upper half. You never need the full order, only the largest of the lower half and the smallest of the upper half. Heaps give exactly those in O(1) and update in O(log n).
- **Insert trick, without branching on values:** push into `low`, then move `low`'s maximum into `high`. This keeps the order invariant, because the largest of the lower half goes up. Then, if `high` is bigger than `low`, move `high`'s minimum back. Each insert does at most three heap operations.

## Solution

```java
import java.util.*;

class MedianFinder {
    private final PriorityQueue<Integer> low = new PriorityQueue<>(Collections.reverseOrder()); // max-heap
    private final PriorityQueue<Integer> high = new PriorityQueue<>();                         // min-heap

    public MedianFinder() {
    }

    public void addNum(int num) {
        low.add(num);
        high.add(low.poll());                          // the largest of the lower half goes up
        if (high.size() > low.size()) low.add(high.poll());   // rebalance: low may be one bigger
    }

    public double findMedian() {
        if (low.size() > high.size()) return low.peek();
        return ((long) low.peek() + high.peek()) / 2.0;
    }
}
```

Adding as `long` before dividing by `2.0` avoids `int` overflow for large values. Here the values are within ±10⁵, but it is a good habit. Dividing by `2.0`, not `2`, keeps the `.5`.

## Complexity

- **addNum:** O(log n), a constant number of heap pushes and pops.
- **findMedian:** O(1), two peeks.
- **Space:** O(n). Every number is stored once. A boxed `Integer` in a heap costs about 16 bytes plus the reference, so an `int[]`-based heap is leaner if memory matters.

## Edge cases

- One element: `low` holds it, and the median is that element.
- Negative numbers and duplicates (Example 2): the heaps handle both.
- An even count: return the average as a `double`, e.g. 1.5.
- `Collections.reverseOrder()` is safe. A comparator written as `(a, b) -> b - a` can overflow for extreme `int` values.

## Variations

- **All numbers in [0, 100]:** use a count array of 101 buckets and walk it to the middle. That is O(1) insert and O(100) median.
- **99% of numbers in [0, 100]:** buckets plus counters for values below and above the range. The median is almost always inside the buckets.
- **Sliding Window Median (LeetCode 480):** the same two heaps plus deletions. Use lazy deletion with a "to delete" map, or two `TreeMap` multisets.
- **Any percentile p:** keep `low` at size `⌈p·n⌉` instead of half.
- **Huge or distributed streams:** use approximate quantile sketches (t-digest, KLL) instead of storing every value.

Related: [C2 · Where memory goes in Java solutions](../academy/lessons/C2.md), [H1 · Java idioms for coding interviews](../academy/lessons/H1.md).

Practise it in the app: Run / Submit on this page.
