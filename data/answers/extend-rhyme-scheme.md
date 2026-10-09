**Short answer:** Two schemes are equivalent exactly when there is a one-to-one renaming of letters between them. Build that renaming over the overlapping prefix, using two maps (long → short and short → long). If any position conflicts, return `""`. Then, for each remaining letter of the long scheme, reuse its mapped letter. If it has no mapped letter yet, give it the smallest letter not used so far. This is O(n) time with two arrays of 26.

## Approach

- **Brute force:** try letters for each new position and check equivalence over all pairs `(i, j)`. That is O(n²) per check, plus the search.
- **Key insight:** "equal at `i` and `j` in one scheme exactly when equal in the other" is the same as saying a bijection maps letters of the long scheme to letters of the short one, the same check as LeetCode's Isomorphic Strings. A one-way map is not enough. `"AAB"` against `"ABCD"` needs long → short to catch `A→A, B→A`. `"AB"` against `"CC"` needs short → long to catch it the other way.
- **Prefix:** for each `i < m`, the pair `(long[i], short[i])` must either be new on both sides, or exactly match what is already recorded.
- **Extension:** a long letter seen before must reuse its short letter. A new long letter starts a new rhyme group, so pick the alphabetically smallest short letter not yet used, which is what makes the answer unique. Both schemes have the same pattern, so they have the same number of distinct letters, and 26 is always enough.

## Solution

```java
import java.util.*;

class Solution {
    public String extendScheme(String shortScheme, String longScheme) {
        char[] longToShort = new char[26]; // 0 = not mapped yet
        char[] shortToLong = new char[26];
        int m = shortScheme.length(), n = longScheme.length();
        if (m > n) return "";
        // The overlapping prefix must be a one-to-one letter renaming.
        for (int i = 0; i < m; i++) {
            char s = shortScheme.charAt(i), l = longScheme.charAt(i);
            if (longToShort[l - 'A'] == 0 && shortToLong[s - 'A'] == 0) {
                longToShort[l - 'A'] = s;
                shortToLong[s - 'A'] = l;
            } else if (longToShort[l - 'A'] != s || shortToLong[s - 'A'] != l) {
                return "";
            }
        }
        StringBuilder sb = new StringBuilder(shortScheme);
        for (int i = m; i < n; i++) {
            char l = longScheme.charAt(i);
            if (longToShort[l - 'A'] == 0) {
                char fresh = 'A';
                while (shortToLong[fresh - 'A'] != 0) fresh++;   // smallest unused letter
                longToShort[l - 'A'] = fresh;
                shortToLong[fresh - 'A'] = l;
            }
            sb.append(longToShort[l - 'A']);
        }
        return sb.toString();
    }
}
```

`shortToLong` also works as the "letters used in the result" set. Every letter in the result is either from the short prefix or a fresh letter, and both are recorded there.

## Complexity

- **Time:** O(n + 26·26). The search for a fresh letter runs at most 26 times, at most 26 steps each.
- **Space:** O(n) for the result, O(1) for the maps.

## Edge cases

- Equal lengths: the answer is the short scheme itself, or `""` if the patterns differ.
- `"AAB"` vs `"ABCD"`: the long → short check fails at index 1, because `B` would need to map to `A` but `A` is already mapped from `A`.
- A letter in the short scheme that the long one splits, or the reverse: caught by the two-sided check.
- Short scheme `"B"` and long `"AABCA"`: the new group for `B` gets `A`, the smallest unused letter, giving `"BBACB"`.

## Variations

- **Isomorphic Strings / Word Pattern (LeetCode 205 / 290):** the same two-map bijection check.
- **Canonical form:** rename the letters in order of first appearance (`"CDCEED"` becomes `"ABACCB"`). Two schemes are equivalent if and only if their canonical forms are equal. This is handy for grouping many schemes with a hash map.

Practise it in the app: Run / Submit on this page.
