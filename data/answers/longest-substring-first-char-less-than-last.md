**Short answer:** Fix the end j. To make the substring as long as possible, the start should be the *earliest* index whose letter is smaller than `s[j]`. So keep `first[c]`, the first index where letter c appeared, and for each j take the minimum of `first[d]` over all letters d < `s[j]`. That is O(26 · n), which is linear for a fixed alphabet.

## Approach

- **Brute force:** check every pair (i, j) with i < j and `s[i] < s[j]`. O(n²) = 10¹⁰ for n = 10⁵. Too slow.
- **Key insight:** for a fixed end j, only the earliest valid start matters. And for a given letter, its first occurrence is the earliest start that letter can offer. So the whole prefix collapses into 26 numbers.
- **Optimal:** scan left to right; for each j, the best start is `min(first[d] for d < s[j])`. Update `first[s[j]]` after the query (a letter cannot pair with itself anyway, since we need strictly smaller).
- **True O(n):** `first` changes at most 26 times in total (once per letter), so you can keep a prefix-minimum array `minFirst[c] = min(first[0..c-1])` and rebuild it in O(26) only when a new letter appears. Then each query is O(1). Worth mentioning; O(26n) is usually accepted as linear.

## Solution

```java
import java.util.Arrays;

class Solution {
    public int longestSubstring(String s) {
        int[] first = new int[26];                 // first index of each letter so far
        Arrays.fill(first, Integer.MAX_VALUE);
        int best = 0;
        for (int j = 0; j < s.length(); j++) {
            int c = s.charAt(j) - 'a';
            int start = Integer.MAX_VALUE;
            for (int d = 0; d < c; d++) start = Math.min(start, first[d]);
            if (start != Integer.MAX_VALUE) best = Math.max(best, j - start + 1);
            if (first[c] == Integer.MAX_VALUE) first[c] = j;
        }
        return best;
    }
}
```

## Complexity

- **Time:** O(26 · n), effectively O(n).
- **Space:** O(26) = O(1).

## Edge cases

- Strictly non-increasing string (`"zyxa"`, `"aaaa"`): no valid pair, return 0.
- Length 1: 0.
- Equal first and last letters do not count (`"bcaab"` as a whole is invalid).
- The best start need not be the global minimum letter; it is the earliest index among *all* smaller letters, which is why we take the min of indexes, not the smallest letter.

## Variations

- **Return the substring itself:** remember the (start, j) pair with the best length.
- **First character greater than last:** mirror it, take the min of `first[d]` over d > `s[j]`.
- **Longest subarray with `a[i] < a[j]` for general integers (Maximum Width Ramp style):** the alphabet is no longer small; use a decreasing stack of candidate starts and scan ends from the right, O(n).

Practise it in the app: Run / Submit on this page.
