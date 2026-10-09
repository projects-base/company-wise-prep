**Short answer:** Greedy from the smallest card. The smallest remaining card cannot be in the middle or end of a group (nothing smaller is left), so it must start one. If it appears `k` times, `k` groups start there, so each of the next `groupSize − 1` values must appear at least `k` times; subtract `k` from all of them. Repeat until empty. A `TreeMap` of counts gives O(n log n).

## Approach

- **Quick reject:** if `hand.length % groupSize != 0`, return false.
- **Brute force:** try every way to pick groups with backtracking. Exponential.
- **Key insight:** the minimum card has only one possible role, the start of a run. That removes all choice, so greedy is correct: if the forced groups cannot be formed, no partition exists.
- **Optimal:** count cards in a `TreeMap`. Take `firstKey()` as `start` with count `need`. For `v` in `start .. start + groupSize − 1`, require `count[v] >= need` and reduce it by `need`. Handling all `need` copies at once (instead of one group at a time) keeps the loop short when a value repeats.

## Solution

```java
import java.util.*;

class Solution {
    public boolean isNStraightHand(int[] hand, int groupSize) {
        if (hand.length % groupSize != 0) return false;
        TreeMap<Integer, Integer> count = new TreeMap<>();
        for (int c : hand) count.merge(c, 1, Integer::sum);

        while (!count.isEmpty()) {
            int start = count.firstKey();
            int need = count.get(start);              // that many groups must start here
            for (long v = start; v < (long) start + groupSize; v++) {
                Integer have = count.get((int) v);
                if (v > Integer.MAX_VALUE || have == null || have < need) return false;
                if (have == need) count.remove((int) v);
                else count.put((int) v, have - need);
            }
        }
        return true;
    }
}
```

The loop variable is a `long` so `start + groupSize` cannot overflow; card values go up to 10⁹.

## Complexity

- **Time O(n log n):** building the map is O(n log n). Each outer iteration forms at least one full group, so there are at most `n / groupSize` iterations, each doing `groupSize` map operations: O(n log n) total.
- **Space O(n)** for the map.

## Edge cases

- `groupSize = 1`: always true.
- Length not divisible by `groupSize`: false immediately.
- Gap in values (`[1,2,4,5]`, size 2 is fine; `[1,3]`, size 2 is not): the missing key returns `null`.
- Many duplicates of the smallest card: handled in one pass via `need`.

## Variations

- The interview fixes `groupSize = 5`; same code.
- Divide Array in Sets of K Consecutive Numbers (LeetCode 1296) is identical.
- Sort-based version: sort the array, use a `HashMap` of counts, and iterate in sorted order starting groups at any card whose count is still positive.

Practise it in the app: Run / Submit on this page.
