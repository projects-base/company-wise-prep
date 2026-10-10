**Short answer:** Count frequencies with a `HashMap`. A frequency is between 1 and n, so put each value into bucket `freq`, then walk the buckets from n down to 1 collecting values until you have k. That is O(n) time and space. A size-k min-heap keyed by frequency, O(n log k), is the other standard answer.

## Picture it

`nums = [1,1,1,2,2,3]`, `k = 2` (answer `[1,2]`). Counts: `{1:3, 2:2, 3:1}`. Buckets are indexed by frequency, 0..n = 0..6:

| bucket f | 0 | 1 | 2 | 3 | 4 | 5 | 6 |
|---|---|---|---|---|---|---|---|
| values seen f times | | 3 | 2 | 1 | | | |

Walk from f = 6 down:

| f | bucket | taken | out | at |
|---|---|---|---|---|
| 6, 5, 4 | empty | | [] | 0 |
| 3 | [1] | 1 | [1] | 1 |
| 2 | [2] | 2 | [1, 2] | 2 = k, stop |

**The picture in one sentence:** a frequency can never exceed n, so use it as an array index and read the buckets from the top, with no comparisons at all.

## Approach

In a 10-12 minute slot, say the plan in one breath and code the version you can write without bugs.

- **Sort by frequency.** Count, then sort the distinct values by count: O(n log n). Fine to mention, but the interviewer asks for better.
- **Min-heap of size k.** Push each (value, count); pop the smallest count when the heap exceeds k. O(n log k).
- **Key insight (bucket sort).** Counts are bounded by n, so you can index by count instead of comparing counts. `buckets[f]` holds every value seen exactly f times; reading buckets from high to low yields values in descending frequency without any sorting.

## Solution

```java
import java.util.*;

class Solution {
    public int[] topKFrequent(int[] nums, int k) {
        Map<Integer, Integer> count = new HashMap<>();
        for (int x : nums) count.merge(x, 1, Integer::sum);
        List<List<Integer>> buckets = new ArrayList<>();
        for (int i = 0; i <= nums.length; i++) buckets.add(new ArrayList<>());
        for (var e : count.entrySet()) buckets.get(e.getValue()).add(e.getKey());
        int[] out = new int[k];
        int at = 0;
        for (int f = nums.length; f >= 1 && at < k; f--) {
            for (int x : buckets.get(f)) {
                if (at < k) out[at++] = x;
            }
        }
        return out;
    }
}
```

Heap version, if you prefer it:

```java
PriorityQueue<Map.Entry<Integer, Integer>> heap =
        new PriorityQueue<>(Map.Entry.comparingByValue());
for (var e : count.entrySet()) {
    heap.add(e);
    if (heap.size() > k) heap.poll();
}
```

## Complexity

- **Bucket sort:** O(n) time, O(n) space.
- **Heap:** O(n + u log k) time, where u is the number of distinct values; O(u + k) space.

## Edge cases

- `k` equals the number of distinct values: return all of them.
- All elements equal: one bucket at index n.
- Negative values: fine, they are map keys, not indices.
- Ties at the k-th position: the tests guarantee none; in general, clarify the tie-break.

## Variations

- **Top K Frequent Words:** same, but ties broken by string order, which makes the heap comparator two-level.
- **Quickselect** on the distinct values by count: O(u) average, O(u²) worst.
- **Stream / too big for memory:** Count-Min Sketch plus a heap, or Misra-Gries / Space-Saving for approximate heavy hitters.

Practise it in the app: Run / Submit on this page.
