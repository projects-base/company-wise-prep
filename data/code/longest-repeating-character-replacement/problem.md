You are given a string `s` of uppercase English letters and an integer `k`. You may change at most `k` characters of `s`, each into any uppercase letter. Return the length of the longest substring that can be made to consist of one repeated letter.

**Example 1**
Input: s = "ABAB", k = 2
Output: 4
Why: turn both 'A's into 'B' (or both 'B's into 'A') to get "BBBB".

**Example 2**
Input: s = "AABABBA", k = 1
Output: 4
Why: change s[3] from 'A' to 'B' to get "AABBBBA", which contains "BBBB".

**Constraints**
- 1 ≤ s.length ≤ 10⁵
- 0 ≤ k ≤ s.length
- `s` contains only uppercase English letters

**Notes**: the hidden tests include a 100,000-character string.
