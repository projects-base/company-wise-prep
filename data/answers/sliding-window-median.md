**Short answer:** Keep the window split into two balanced halves: `low` (the smaller half, holding the extra element when k is odd) and `high`. The median is `low`'s largest, or the average of `low`'s largest and `high`'s smallest. To support removing the element that leaves the window in O(log k), use two `TreeSet`s of **indices** ordered by (value, index), so duplicates stay distinct. Total O(n log k).

## Picture it

The first six values of example 1: `nums = [1,3,-1,-3,5,3]`, `k = 3`. Sets are shown by value (they really hold indices).

| i | nums[i] | remove leaving | add to low, move low max to high | rebalance | low / high after | median |
|---|---|---|---|---|---|---|
| 0 | 1 | - | low {}, high {1} | move 1 back | {1} / {} | - |
| 1 | 3 | - | low {1}, high {3} | none | {1} / {3} | - |
| 2 | -1 | - | low {-1}, high {1,3} | move 1 back | {-1,1} / {3} | 1 |
| 3 | -3 | 1 (from low) | low {-3}, high {-1,3} | move -1 back | {-3,-1} / {3} | -1 |
| 4 | 5 | 3 (from high) | low {-3,-1}, high {5} | none | {-3,-1} / {5} | -1 |
| 5 | 3 | -1 (from low) | low {-3}, high {3,5} | move 3 back | {-3,3} / {5} | 3 |

k is odd, so the median is always `low`'s largest: `1, -1, -1, 3`, matching the expected output.

**The picture in one sentence:** two ordered halves where `low` holds ⌈k/2⌉ elements put the median at `low.last()`, and `TreeSet`s of indices make removing the leaving element O(log k).

## Approach

- **Brute force:** sort every window: O(n · k log k). Insertion into a sorted list is O(n·k), still too slow.
- **Key insight:** the "running median" two-heap trick works, but `PriorityQueue.remove(Object)` is O(k), which makes the whole thing O(n·k). A `TreeSet` gives O(log k) arbitrary removal. Storing indices, compared by value then index, makes every element unique, so duplicate values are not lost.
- **Each step:**
  1. If the window is full, remove index `i - k` from whichever set holds it.
  2. Add i to `low`, then move `low`'s largest to `high`. This keeps every element of `low` ≤ every element of `high`.
  3. While `high` is bigger than `low`, move `high`'s smallest back to `low`. Now `low` has ⌈k/2⌉ elements.
  4. Read the median.
- **Overflow:** values span the full `int` range, so add them as `double` (or `long`) before dividing.

## Solution

```java
import java.util.*;

class Solution {
    public double[] medianSlidingWindow(int[] nums, int k) {
        // Indices ordered by (value, index): duplicates stay distinct.
        Comparator<Integer> cmp = (a, b) -> nums[a] != nums[b]
                ? Integer.compare(nums[a], nums[b])
                : Integer.compare(a, b);
        TreeSet<Integer> low = new TreeSet<>(cmp), high = new TreeSet<>(cmp);
        double[] out = new double[nums.length - k + 1];
        for (int i = 0; i < nums.length; i++) {
            if (i >= k) {
                int old = i - k;
                if (!low.remove(old)) high.remove(old);
            }
            low.add(i);
            high.add(low.pollLast());                         // keep low <= high
            while (high.size() > low.size()) low.add(high.pollFirst());
            if (i >= k - 1) {
                out[i - k + 1] = k % 2 == 1
                        ? nums[low.last()]
                        : ((double) nums[low.last()] + nums[high.first()]) / 2.0;
            }
        }
        return out;
    }
}
```

Why the rebalance is enough: after a removal, the sizes can be off by at most one in either direction. Adding to `low` and pushing its max to `high` gives `high` the extra element, and the `while` loop moves elements back until `low` holds ⌈size/2⌉.

## Complexity

- **Time:** O(n log k). Each step does a constant number of `TreeSet` operations on sets of size ≤ k.
- **Space:** O(k) for the two sets, plus the output.

## Edge cases

- `k == 1`: each median is the element itself.
- Even k: average of the two middle values.
- `Integer.MAX_VALUE + Integer.MAX_VALUE`: the `double` cast avoids overflow.
- Many duplicates: the index tie-break keeps them distinct, so `remove(old)` deletes exactly one.

## Variations

- Two heaps with **lazy deletion** (a hash map of "to delete" counts, purged when an item reaches a heap top) is the other standard O(n log k) answer.
- Find Median from Data Stream is the same structure without removals.

Practise it in the app: Run / Submit on this page.
