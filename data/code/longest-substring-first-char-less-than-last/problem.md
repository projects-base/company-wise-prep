You are given a string `s` of lowercase English letters. Find the length of the longest **substring** (a contiguous block of `s`) whose first character comes strictly earlier in the alphabet than its last character. Return 0 if no such substring exists.

A valid substring has at least 2 characters, since a single character's first and last characters are the same.

**Example 1**
Input: s = "dcbabcd"
Output: 6
Why: "cbabcd" (indices 1..6) starts with 'c' and ends with 'd'. The whole string starts and ends with 'd', so it does not count.

**Example 2**
Input: s = "zyxa"
Output: 0
Why: every letter is later in the alphabet than every letter after it.

**Example 3**
Input: s = "bcaab"
Output: 3
Why: "aab" (indices 2..4) starts with 'a' and ends with 'b'. "bcaab" starts and ends with 'b', and "caab" starts with 'c', which is after 'b'.

**Constraints**
- 1 ≤ s.length ≤ 10⁵
- s contains only lowercase English letters

**Notes**: an O(n²) scan over all start/end pairs is too slow for the hidden tests. Aim for O(26·n) or O(n).
