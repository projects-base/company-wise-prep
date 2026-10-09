**Short answer:** Walk the array once and keep a map from each value to the last index where it appeared. At index `i`, if the value was seen before at `prev` and `i − prev ≤ k`, return true. Checking only the most recent earlier index is enough, because it is the closest one. O(n) time and O(n) space, or O(k) space with a sliding-window set.

## Approach

**Brute force.** For each `i`, compare with the next `k` elements. O(n · k), which is about 10⁹ in the worst case.

**Key insight.** For a value at index `i`, the best earlier partner is the nearest earlier equal value. If even that one is more than `k` away, every older one is farther. So store only the last index per value.

**Alternative (bounded memory).** Keep a `HashSet` of the last `k` values as a sliding window. Before adding `nums[i]`, check if it is in the set; after adding, remove `nums[i − k]` when the window grows past `k`. Space O(min(n, k)).

## Solution

```java
import java.util.*;

class Solution {
    public boolean containsNearbyDuplicate(int[] nums, int k) {
        Map<Integer, Integer> lastIndex = new HashMap<>();
        for (int i = 0; i < nums.length; i++) {
            Integer prev = lastIndex.put(nums[i], i);   // put returns the old index, or null
            if (prev != null && i - prev <= k) return true;
        }
        return false;
    }
}
```

`Map.put` returns the previous value, so one call both reads the old index and stores the new one.

## Complexity

- **Time:** O(n) expected, one hash operation per element.
- **Space:** O(n) for the map in the worst case (all values distinct). The sliding-window set version uses O(min(n, k)).

## Edge cases

- `k = 0`: indices must differ, so the answer is always false. The code handles it: `i − prev ≥ 1 > 0`.
- One element: false.
- The same value appearing many times: the map always holds its latest index, which is the right one to compare with.
- Values are boxed into `Integer` keys; comparing via the map avoids the `==` on `Integer` trap.

## Variations

- **Print the duplicates within distance k (the interview variant).** Same loop, but instead of returning, add `nums[i]` to a result `LinkedHashSet` when the check passes, then print it. Use a set so a value that repeats several times is printed once.
- **Contains Duplicate III** (values within `t` and indices within `k`): use a sliding window in a `TreeSet` and query `ceiling(nums[i] − t)`, or bucket values by width `t + 1`.

See [H1 · Java idioms for coding interviews](../academy/lessons/H1.md) for `Map` return values like this one.

Practise it in the app: Run / Submit on this page.
