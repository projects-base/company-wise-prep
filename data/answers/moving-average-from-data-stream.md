**Short answer:** Keep the last `size` values in a circular buffer and a running sum. On each new value, if the buffer is full, subtract the value it overwrites; add the new value; divide by the current count. O(1) per call. For the follow-up (exclude the k largest in the window), keep the window split into two sorted multisets, the top k and the rest, and maintain the sum of the rest: O(log size) per call.

## Picture it

Example 1 (`size = 3`) plus one extra call, `next(7)`:

| Call | Full? | Drop window[head] | window after | head after | count | sum | Returns |
|---|---|---|---|---|---|---|---|
| next(1) | no | — | [1, 0, 0] | 1 | 1 | 1 | 1.0 |
| next(10) | no | — | [1, 10, 0] | 2 | 2 | 11 | 5.5 |
| next(3) | no | — | [1, 10, 3] | 0 | 3 | 14 | 4.66667 |
| next(5) | yes | 1 | [5, 10, 3] | 1 | 3 | 14 − 1 + 5 = 18 | 6.0 |
| next(7) | yes | 10 | [5, 7, 3] | 2 | 3 | 18 − 10 + 7 = 15 | 5.0 |

**The picture in one sentence:** the circular buffer's `head` always points at the oldest value, so each call subtracts it, overwrites it and adds the new one in O(1).

## Approach

- **Brute force:** store all values and re-add the last `size` on every call. O(size) per call.
- **Key insight:** consecutive windows differ by one value in and one value out. A running sum updates in O(1).
- **Circular buffer:** a fixed `int[size]` and a head index avoid allocating a node per value (as an `ArrayDeque<Integer>` would, with boxing). Use a `long` sum so it cannot overflow.

## Solution

```java
class MovingAverage {
    private final int[] window;
    private int count, head;     // head = slot to write next
    private long sum;

    public MovingAverage(int size) {
        window = new int[size];
    }

    public double next(int val) {
        if (count == window.length) sum -= window[head];   // overwrite the oldest
        else count++;
        window[head] = val;
        sum += val;
        head = (head + 1) % window.length;
        return (double) sum / count;
    }
}
```

## Complexity

- **Time:** O(1) per call.
- **Space:** O(size).

## Edge cases

- Fewer than `size` values so far: divide by `count`, not `size`.
- `size = 1`: always the latest value.
- Negative values: fine.
- Integer division: cast to `double` before dividing.
- Floats in the original prompt: a running `double` sum drifts over millions of updates. Recompute the sum from the buffer every so often, or use Kahan summation, if precision matters.

## Follow-up: exclude the k highest values in the window

Keep two multisets (`TreeMap<value, count>`): `top` holds the k largest values in the window, `rest` holds the others, and every value in `top` is ≥ every value in `rest`. Track `restSum` and the sizes.

```java
import java.util.ArrayDeque;
import java.util.TreeMap;

class MovingAverageExcludingTopK {
    private final int size, k;
    private final ArrayDeque<Integer> window = new ArrayDeque<>();
    private final TreeMap<Integer, Integer> top = new TreeMap<>(), rest = new TreeMap<>();
    private int topSize, restSize;
    private long restSum;

    MovingAverageExcludingTopK(int size, int k) { this.size = size; this.k = k; }

    public double next(int val) {
        if (window.size() == size) remove(window.pollFirst());
        window.addLast(val);
        add(top, val); topSize++;
        if (topSize > k) {                                  // push top's smallest down
            int m = top.firstKey();
            dec(top, m); topSize--;
            add(rest, m); restSize++; restSum += m;
        }
        return restSize == 0 ? 0.0 : (double) restSum / restSize;
    }

    private void remove(int v) {
        if (rest.containsKey(v)) { dec(rest, v); restSize--; restSum -= v; }
        else { dec(top, v); topSize--; }
        if (topSize < k && restSize > 0) {                  // refill top from rest's largest
            int m = rest.lastKey();
            dec(rest, m); restSize--; restSum -= m;
            add(top, m); topSize++;
        }
    }

    private static void add(TreeMap<Integer, Integer> m, int v) { m.merge(v, 1, Integer::sum); }
    private static void dec(TreeMap<Integer, Integer> m, int v) {
        m.computeIfPresent(v, (key, c) -> c == 1 ? null : c - 1);
    }
}
```

Why removal by value is safe with duplicates: if v is in both sets, it equals the boundary value, so removing either copy keeps the invariant. Each call does a constant number of `TreeMap` operations: O(log size). Ask what to return when the window holds k values or fewer (here 0.0; `Double.NaN` or an exception are also reasonable).

The same two-set structure gives a sliding-window median (keep the halves balanced instead of fixing one side at k).

Practise it in the app: Run / Submit on this page.
