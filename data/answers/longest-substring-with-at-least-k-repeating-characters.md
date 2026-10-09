**Short answer:** A plain sliding window fails, because adding a character can make a window go from invalid to valid, so there is no rule for when to shrink. Fix that by adding a constraint: for each `target` from 1 to 26, find the longest window with *exactly* `target` distinct letters that all appear at least k times. With the number of distinct letters capped, the window has a clear shrink rule. 26 passes of O(n) give O(26 · n).

## Approach

- **Brute force:** every substring, with counts kept incrementally. O(26 · n²). Too slow at 10⁵.
- **Divide and conquer:** any letter that appears fewer than k times in the whole string can never be in the answer. Split the string at those letters and solve each piece recursively. Simple and accepted, O(26 · n) in the worst case because each level removes at least one letter from consideration, so depth is at most 26.
- **Why the normal window breaks:** "every letter has count ≥ k" is not monotonic in the window size. Growing can fix it (more b's) or break it (a new letter with count 1).
- **Key insight (sliding window):** fix how many distinct letters the window may have. Then "too many distinct letters" is a monotonic rule: shrink from the left until distinct ≤ target. Track how many letters reach k; the window is valid when `distinct == target && atLeastK == target`.

## Solution

```java
class Solution {
    public int longestSubstring(String s, int k) {
        int n = s.length(), best = 0;
        for (int target = 1; target <= 26; target++) {
            int[] cnt = new int[26];
            int distinct = 0, atLeastK = 0, left = 0;
            for (int right = 0; right < n; right++) {
                int c = s.charAt(right) - 'a';
                if (cnt[c]++ == 0) distinct++;
                if (cnt[c] == k) atLeastK++;
                while (distinct > target) {              // shrink until allowed
                    int d = s.charAt(left++) - 'a';
                    if (cnt[d]-- == k) atLeastK--;
                    if (cnt[d] == 0) distinct--;
                }
                if (distinct == target && atLeastK == target) {
                    best = Math.max(best, right - left + 1);
                }
            }
        }
        return best;
    }
}
```

## Complexity

- **Time:** O(26 · n): 26 passes, each moving two pointers across the string once.
- **Space:** O(26) per pass.

## Edge cases

- `k = 1`: every substring is valid, so the answer is n.
- `k > n`: no letter can appear k times, so 0.
- Increment order: when k = 1, `cnt[c]++` makes it 1 and the `== k` check counts it in the same step. The decrement uses the value *before* decrementing (`cnt[d]-- == k`), so a letter dropping from k to k−1 leaves `atLeastK`.
- One repeated letter: n if n ≥ k.

## Variations

- **Divide-and-conquer version** (often easier to explain first):

```java
class Solution {
    public int longestSubstring(String s, int k) {
        return solve(s, 0, s.length(), k);
    }

    private int solve(String s, int lo, int hi, int k) {
        if (hi - lo < k) return 0;
        int[] cnt = new int[26];
        for (int i = lo; i < hi; i++) cnt[s.charAt(i) - 'a']++;
        int best = 0, start = lo;
        for (int i = lo; i < hi; i++) {
            if (cnt[s.charAt(i) - 'a'] < k) {            // split at every bad letter
                best = Math.max(best, solve(s, start, i, k));
                start = i + 1;
            }
        }
        if (start == lo) return hi - lo;                 // no bad letter: whole range is valid
        return Math.max(best, solve(s, start, hi, k));
    }
}
```

  Split at *every* occurrence of a bad letter in one level. Then each piece contains none of this level's bad letters, so it has strictly fewer distinct letters than its parent and the depth is at most 26. (Splitting only at the first bad letter and recursing on the rest can chain O(n) calls deep.)
- **Longest substring with at most k distinct characters:** the inner window here, on its own.

Practise it in the app: Run / Submit on this page.
