**Short answer:** Sliding window with a "need" count per character and one counter, `missing`, for how many characters of t the window still lacks. Extend `right`; when a character that was still needed enters, decrement `missing`. When `missing` hits 0 the window is valid: record it, then shrink from the left until it becomes invalid again. Each index enters and leaves once, so it is O(|s| + |t|).

## Picture it

Example 1: `s = "ADOBECODEBANC"`, `t = "ABC"`. Start `need = {A:1, B:1, C:1}`, `missing = 3`.

```text
index: 0 1 2 3 4 5 6 7 8 9 10 11 12
s:     A D O B E C O D E B A  N  C
```

| Step | right | char | missing after | Shrink loop (while missing == 0) | best |
|---|---|---|---|---|---|
| 1 | 0 | A | 2 | — | — |
| 2 | 3 | B | 1 | — (D, O at 1–2 were not needed) | — |
| 3 | 5 | C | 0 | record [0..5] "ADOBEC" (6); drop A → missing 1, left = 1 | ADOBEC |
| 4 | 9 | B | 1 | — (a surplus B: need B goes to −1) | ADOBEC |
| 5 | 10 | A | 0 | [1..10] to [5..10] are all ≥ 6, no record; drop D, O, B, E (surplus), then C → missing 1, left = 6 | ADOBEC |
| 6 | 12 | C | 0 | [6..12] 7, [7..12] 6, [8..12] "EBANC" 5 → record, [9..12] "BANC" 4 → record; drop B → missing 1, left = 10 | BANC |

Answer "BANC".

**The picture in one sentence:** grow the right edge until the window holds all of t, then pull the left edge in as far as it stays valid, recording the shortest window seen.

## Approach

- **Brute force:** check every substring of s against t's counts. O(|s|² · alphabet) or worse.
- **Key insight:** validity is monotonic in the window: if `s[l..r]` contains t, so does every larger window. So for each right end, the best left end only moves forward. That is the two-pointer pattern.
- **Constant-time validity check:** instead of comparing two count maps each step, keep `need[c]` (how many more of c the window needs; it can go negative for surplus) and `missing` (sum of positive needs). A character only changes `missing` when it moves a positive need.

## Solution

```java
class Solution {
    public String minWindow(String s, String t) {
        int[] need = new int[128];
        for (char c : t.toCharArray()) need[c]++;
        int missing = t.length(), bestStart = 0, bestLen = Integer.MAX_VALUE;
        for (int left = 0, right = 0; right < s.length(); right++) {
            if (need[s.charAt(right)]-- > 0) missing--;         // a needed char arrived
            while (missing == 0) {                              // window is valid
                if (right - left + 1 < bestLen) {               // strict: leftmost on ties
                    bestLen = right - left + 1;
                    bestStart = left;
                }
                if (++need[s.charAt(left++)] > 0) missing++;    // dropped a needed char
            }
        }
        return bestLen == Integer.MAX_VALUE ? "" : s.substring(bestStart, bestStart + bestLen);
    }
}
```

## Complexity

- **Time:** O(|s| + |t|): `right` and `left` each move at most |s| times.
- **Space:** O(1): a 128-slot array for ASCII letters.

## Edge cases

- t longer than s, or a character of t missing from s: return `""`.
- Repeated characters in t (`"aa"`): counts, not a set, so one `a` is not enough.
- Case matters: `'A'` and `'a'` are different slots.
- Ties: the strict `<` keeps the leftmost shortest window.
- Characters of s that are not in t: their `need` goes negative and never touches `missing`, so they are just carried along.

## Follow-up: optimise and clean the code

What the clean version above already does, and what to say about it:

- **`int[128]` instead of `HashMap<Character, Integer>`:** no boxing, no hashing, much faster on 10⁵ characters. Use a map only if the alphabet is all of Unicode.
- **One `missing` counter** instead of comparing two maps or tracking "formed distinct characters": the validity check is O(1).
- **Store `bestStart` and `bestLen`,** and call `substring` once at the end, not on every improvement.
- **Filtered s (when t is tiny and s is huge):** first build a list of (index, char) for characters of s that appear in t, and slide over that list. Same complexity, fewer steps when most of s is irrelevant.
- **Readability:** name things for what they mean (`need`, `missing`), keep the "expand, then shrink while valid" shape visible, and avoid special-casing the first window.

## Variations

- **Permutation in String / Find All Anagrams:** fixed-length window with the same `need` / `missing` bookkeeping.
- **Minimum window subsequence** (characters of t in order): a different technique, two-pointer forward and backward scans or DP.
- **Smallest window containing all distinct characters of s itself:** t = the set of characters of s.

Practise it in the app: Run / Submit on this page.
