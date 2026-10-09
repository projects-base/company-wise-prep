**Short answer:** Sliding window. A window can be made into one repeated letter if `windowLength − countOfMostFrequentLetter ≤ k`: you keep the most common letter and replace the rest. Grow the window to the right, and when it becomes invalid, move the left edge. Track the best length. O(n) with a 26-slot count array.

## Approach

- **Brute force:** for every substring, count letters and check `length − maxCount ≤ k`. O(n²) substrings with an O(26) check if you extend counts incrementally: O(26 · n²), about 2.6·10¹¹ at n = 10⁵. Too slow.
- **Per-letter windows:** for each of the 26 letters, find the longest window with at most k *other* letters (a standard at-most-k window). O(26 · n). Already fine, and easy to explain.
- **Key insight for one pass:** use the frequency of the most common letter in the window. Validity is `len − maxFreq ≤ k`.
- **The subtle trick:** `maxFreq` is never decreased when the left edge moves. That looks wrong, but it is safe: the answer only grows when a window has a *larger* `maxFreq` than any seen before. A stale `maxFreq` can only make the window keep its current size, never report a length that was not achieved earlier.

## Solution

```java
class Solution {
    public int characterReplacement(String s, int k) {
        int[] count = new int[26];
        int maxFreq = 0, best = 0, left = 0;
        for (int right = 0; right < s.length(); right++) {
            maxFreq = Math.max(maxFreq, ++count[s.charAt(right) - 'A']);
            // Window never shrinks below its best size, so a stale maxFreq is harmless.
            while (right - left + 1 - maxFreq > k) count[s.charAt(left++) - 'A']--;
            best = Math.max(best, right - left + 1);
        }
        return best;
    }
}
```

With the stale `maxFreq`, the `while` runs at most once per step, so you can also write it as an `if`.

## Complexity

- **Time:** O(n): each index enters and leaves the window at most once.
- **Space:** O(1): 26 counters.

## Edge cases

- `k = 0`: longest run of one letter.
- `k ≥ n`: the whole string.
- All the same letter: n.
- Single character: 1.

## Variations

- **Max Consecutive Ones III:** binary version; window with at most k zeros.
- **Longest Substring with At Most K Distinct Characters:** same window shape, different validity rule.
- **Lowercase or Unicode alphabet:** use a `HashMap<Character, Integer>`, or the 26-letter loop with the alphabet you actually have.
- When asked to "discuss brute force and optimal" (as the prompt says), walk this order: O(n³) naive → O(n²) incremental counts → O(26n) per letter → O(n) single pass with the max-frequency trick.

See [C4 · The optimisation playbook](../academy/lessons/C4.md).

Practise it in the app: Run / Submit on this page.
