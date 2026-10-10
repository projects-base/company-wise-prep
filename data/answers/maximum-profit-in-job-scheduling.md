**Short answer:** Sort jobs by end time. Let `dp[i]` be the best profit using only the first i jobs in that order. For job i you either skip it (`dp[i − 1]`) or take it, plus the best result among jobs that end at or before its start. Because ends are sorted, that "last compatible job" is found by binary search. O(n log n).

## Picture it

Example 1, jobs sorted by end time (already in order): `ends = [3,4,5,6]`.

| i | Job (start, end, profit) | lo = jobs with end ≤ start | Skip: dp[i−1] | Take: dp[lo] + profit | dp[i] |
|---|---|---|---|---|---|
| 0 | — | — | — | — | 0 |
| 1 | (1, 3, 50) | 0 | 0 | 0 + 50 = 50 | 50 |
| 2 | (2, 4, 10) | 0 (3 > 2) | 50 | 0 + 10 = 10 | 50 |
| 3 | (3, 5, 40) | 1 (end 3 ≤ 3) | 50 | dp[1] + 40 = 90 | 90 |
| 4 | (3, 6, 70) | 1 (end 3 ≤ 3) | 90 | dp[1] + 70 = 120 | 120 |

Each `dp[i]` depends on `dp[i−1]` (skip) and one earlier cell `dp[lo]` found by binary search (take). Answer `dp[4] = 120`: jobs [1,3] and [3,6].

**The picture in one sentence:** sort by end time, and for each job choose between skipping it and taking it plus the best answer among jobs that end by its start.

## Approach

- **Brute force:** try every subset and check overlaps: O(2ⁿ · n).
- **Greedy fails:** earliest end time maximises the *number* of jobs, not the profit. A single long, well-paid job can beat many short ones.
- **Key insight:** this is weighted interval scheduling. Sorted by end time, any chosen set's last job splits the problem: everything else must end by its start, which is a prefix of the sorted order. So the state is "best profit within the first i jobs", and the choice is take or skip job i.
- **Finding the compatible prefix:** binary search the sorted end times for the number of jobs with `end ≤ start[i]`. Note `≤`: a job ending at X is compatible with one starting at X.

## Solution

```java
import java.util.Arrays;

class Solution {
    public int jobScheduling(int[] startTime, int[] endTime, int[] profit) {
        int n = startTime.length;
        Integer[] idx = new Integer[n];
        for (int i = 0; i < n; i++) idx[i] = i;
        Arrays.sort(idx, (a, b) -> Integer.compare(endTime[a], endTime[b]));
        int[] ends = new int[n];
        for (int i = 0; i < n; i++) ends[i] = endTime[idx[i]];

        int[] dp = new int[n + 1];  // dp[i]: best using the first i jobs by end time
        for (int i = 1; i <= n; i++) {
            int j = idx[i - 1];
            // count of jobs among the first i-1 whose end <= startTime[j]
            int lo = 0, hi = i - 1;
            while (lo < hi) {
                int mid = (lo + hi) >>> 1;
                if (ends[mid] <= startTime[j]) lo = mid + 1; else hi = mid;
            }
            dp[i] = Math.max(dp[i - 1], dp[lo] + profit[j]);   // skip vs take
        }
        return dp[n];
    }
}
```

## Complexity

- **Time:** O(n log n): the sort plus one binary search per job.
- **Space:** O(n) for the index array, end times and dp.

## Edge cases

- Touching jobs (end = next start) are compatible; using `<` instead of `≤` in the search loses Example 1.
- All jobs overlap: the answer is the best single profit.
- Identical end times: the order between them does not matter, since neither can precede the other.
- The search only looks in `[0, i − 1)`: a job cannot be compatible with itself because `start < end`.
- Profit sum: up to 5·10⁴ × 10⁴ = 5·10⁸, fits in `int`.

## Variations

- **Return the chosen jobs:** walk back from `dp[n]`: if `dp[i] == dp[i − 1]` skip, otherwise job i was taken and jump to its compatible prefix.
- **`TreeMap` version:** sort by end, keep `TreeMap<end, bestProfit>`, and use `floorEntry(start)` to get the best compatible value. Same complexity, less index handling.
- **Unweighted (max number of jobs):** greedy by earliest end time, O(n log n).
- **At most k jobs:** add a dimension, `dp[i][c]`.

Practise it in the app: Run / Submit on this page.
