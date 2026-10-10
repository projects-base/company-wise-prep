**Short answer:** Binary search over *character positions*, not over numbers. Pick the middle character, expand left and right to the spaces around it to get the whole number there, and compare that number with `target` as a digit string. More digits means bigger; with equal length, compare digit by digit. Then keep only the half on the correct side, cutting at the number's boundaries. That is O(log L) comparisons, each costing at most one number's length.

## Picture it

`s = "1 5 12 40 999"`, `target = "41"`. Character positions:

```text
index: 0 1 2 3 4 5 6 7 8 9 10 11 12
char:  1 _ 5 _ 1 2 _ 4 0 _ 9  9  9      (_ = space)
```

| Step | Window `[lo, hi)` | `mid` | Expand to `[start, end)` | Number | Compare with "41" | Action |
|---|---|---|---|---|---|---|
| 1 | [0, 13) | 6 (a space) | [4, 6) | 12 | same length, `1` < `4`: smaller | `lo = end + 1 = 7` |
| 2 | [7, 13) | 10 | [10, 13) | 999 | 3 digits > 2: bigger | `hi = start - 1 = 9` |
| 3 | [7, 9) | 8 | [7, 9) | 40 | `4` = `4`, `0` < `1`: smaller | `lo = end + 1 = 10` |
| 4 | [10, 9) | – | – | – | window empty | return `false` |

**The picture in one sentence:** binary search on character positions, snap each midpoint out to the whole number around it, and compare digit strings by length first, then digit by digit.

## Approach

- **Brute force:** `s.split(" ")` and a linear scan (or `BigInteger` comparisons). O(L) time and O(L) extra memory — exactly what the exercise forbids.
- **Key insight 1 — compare without parsing:** for non-negative integers with no leading zeros, a longer digit string is a larger number. With the same length, lexicographic order equals numeric order. So `compare` never builds a number.
- **Key insight 2 — binary search on positions:** a character index splits the string into "numbers before" and "numbers after". Landing in the middle of a number is fine: walk to its boundaries and you have a whole number to compare. The search window `[lo, hi)` always starts and ends on number boundaries, so the expansion never crosses outside it.
- **Shrinking:** if the number is too small, set `lo = end + 1` (skip the number and the space after it). If it is too big, set `hi = start − 1` (keep only the numbers before it, without the space). If `mid` lands on a space, the left walk picks the number just before that space, which is still a valid choice.

## Solution

```java
class Solution {
    public boolean containsNumber(String s, String target) {
        int lo = 0, hi = s.length();                 // window [lo, hi) on number boundaries
        while (lo < hi) {
            int mid = (lo + hi) >>> 1;
            int start = mid, end = mid;
            while (start > lo && s.charAt(start - 1) != ' ') start--;
            while (end < hi && s.charAt(end) != ' ') end++;
            int cmp = compare(s, start, end, target);
            if (cmp == 0) return true;
            if (cmp < 0) lo = end + 1;               // drop this number and the space after it
            else hi = start - 1;                     // drop this number and the space before it
        }
        return false;
    }

    /** Compares s[start, end) with target as non-negative integers without leading zeros. */
    private int compare(String s, int start, int end, String target) {
        int len = end - start;
        if (len != target.length()) return Integer.compare(len, target.length());
        for (int i = 0; i < len; i++) {
            char a = s.charAt(start + i), b = target.charAt(i);
            if (a != b) return Character.compare(a, b);
        }
        return 0;
    }
}
```

## Complexity

- **Time:** each step removes at least one whole number plus a separator, and roughly halves the window by characters. So there are O(log L) steps. Each step scans one number to find its boundaries and compares it with `target`: O(D), where D ≤ 1000 digits. Total O(D · log L).
- **Space:** O(1). No substrings and no split.

## Edge cases

- Empty `s` → `lo == hi` at the start → `false`.
- One number → found or not after one comparison.
- Repeated values (`"17 17 17"`) → any copy matches.
- `hi = start − 1` can become `lo − 1` when the number is the first in the window. The loop condition `lo < hi` then ends it safely.
- `"0"` as the target or in `s` → it is the only number allowed to start with 0, and the length rule still holds.

## Variations

- **Lower bound / count of occurrences:** the same comparison with a "first number ≥ target" search on positions.
- **The data lives in a file too big for memory:** the same idea using `RandomAccessFile.seek(mid)`, reading a small buffer around `mid` to find the number's boundaries.
- **Variable separators or negative numbers:** adjust the boundary test and `compare` (with negatives, more digits means *smaller*).

Practise it in the app: Run / Submit on this page.
