**Short answer:** Expand around every centre. A palindrome mirrors around its middle, which is either one character (odd length) or the gap between two characters (even length). That gives 2n − 1 centres. From each, expand outward while the two ends match, and keep the longest. O(n²) time, O(1) extra space, which is fast enough for n = 3000.

## Picture it

Example 1: `s = "babad"`. Every index is tried as an odd centre (`l = r = c`) and as an even centre (`l = c`, `r = c + 1`).

| c | Centre type | Expansion | Stops at (l, r) | len = r − l − 1 | best, start |
|---|---|---|---|---|---|
| 0 | odd | b | (−1, 1) edge | 1 | 1, 0 |
| 0 | even | b ≠ a | (0, 1) | 0 | 1, 0 |
| 1 | odd | a, then b = b | (−1, 3) edge | 3 | **3, 0** |
| 1 | even | a ≠ b | (1, 2) | 0 | 3, 0 |
| 2 | odd | b, then a = a, then b ≠ d | (0, 4) | 3 | 3, 0 (not bigger) |
| 2 | even | b ≠ a | (2, 3) | 0 | 3, 0 |
| 3 | odd | a, then b ≠ d | (2, 4) | 1 | 3, 0 |
| 4 | odd | d | (3, 5) edge | 1 | 3, 0 |

```text
b a b a d
  ^          centre c = 1: expands to "bab" (indices 0..2)
```

Result: `s.substring(0, 3) = "bab"`.

**The picture in one sentence:** every palindrome grows outward from a middle, so try all 2n − 1 middles and stretch each until the ends differ.

## Approach

- **Brute force:** check every substring for being a palindrome. O(n²) substrings × O(n) check = O(n³), about 2.7·10¹⁰ for n = 3000. Too slow.
- **DP:** `pal[i][j] = s[i] == s[j] && pal[i+1][j-1]`. O(n²) time but also O(n²) memory: 9·10⁶ booleans, workable but wasteful.
- **Key insight:** the DP only extends a palindrome by one character on each side. Doing that directly from each centre gives the same O(n²) time with O(1) memory, and it stops early as soon as the ends differ.
- **Manacher's algorithm** does it in O(n) by reusing mirror information. Mention it; few interviewers expect you to code it.

## Solution

```java
class Solution {
    public String longestPalindrome(String s) {
        int best = 0, start = 0;
        for (int c = 0; c < s.length(); c++) {
            for (int even = 0; even < 2; even++) {   // centre at c, or between c and c+1
                int l = c, r = c + even;
                while (l >= 0 && r < s.length() && s.charAt(l) == s.charAt(r)) {
                    l--;
                    r++;
                }
                int len = r - l - 1;                 // l and r are one step past the palindrome
                if (len > best) {
                    best = len;
                    start = l + 1;
                }
            }
        }
        return s.substring(start, start + best);
    }
}
```

## Complexity

- **Time:** O(n²) in the worst case (a string of one repeated letter); usually much less because most expansions stop quickly.
- **Space:** O(1) besides the returned substring.

## Edge cases

- Single character: itself.
- Even-length answer (`"cbbd"` → `"bb"`): needs the between-characters centre.
- All the same letter: the whole string.
- No repeated neighbours (`"abc"`): any single character.
- Off-by-one: after the loop, `l` and `r` point one past the palindrome, so the length is `r − l − 1` and the start is `l + 1`.

## Variations

- **Palindromic Substrings (count them):** same expansion, add 1 per successful step.
- **Longest Palindromic Subsequence:** not contiguous, so it needs the O(n²) DP (or LCS of s and its reverse).
- **Manacher's O(n):** keep the rightmost palindrome found so far and start each new expansion from its mirror's radius.

See [C4 · The optimisation playbook](../academy/lessons/C4.md) for walking from O(n³) to O(n²) out loud.

Practise it in the app: Run / Submit on this page.
