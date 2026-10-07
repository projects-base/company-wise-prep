Given a string `s`, return the length of the longest contiguous substring in which no character appears more than once.

**Example 1**
Input: s = "abcabcbb"
Output: 3
Why: "abc" has no repeats; every window of length 4 contains a repeat.

**Example 2**
Input: s = "bbbbb"
Output: 1

**Example 3**
Input: s = "pwwkew"
Output: 3
Why: "wke" works. Note that "pwke" is a subsequence, not a substring.

**Constraints**
- 0 ≤ s.length ≤ 5 · 10⁴
- `s` contains English letters, digits, spaces and printable symbols (ASCII)

**Notes**: an empty string has answer 0. The hidden tests include a 50,000-character string.
