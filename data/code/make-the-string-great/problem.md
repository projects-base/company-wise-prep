You are given a string `s` of English letters. Two adjacent characters form a *bad pair* when they are the same letter in different case, such as `"aA"` or `"Bb"`. Keep deleting bad pairs (both characters at once) until none remain, and return the resulting string. The final string is the same whatever order the pairs are removed in. It may be empty.

**Example 1**
Input: s = "leEeetcode"
Output: "leetcode"
Why: one "eE" pair is removed, and no bad pair remains.

**Example 2**
Input: s = "abBAcC"
Output: ""
Why: removing "bB" makes "aA" adjacent; then "cC" goes too.

**Example 3**
Input: s = "s"
Output: "s"

**Constraints**
- 1 ≤ s.length ≤ 10⁵
- s contains only lowercase and uppercase English letters

**Notes**: the hidden tests include 100,000 characters, so rescanning the whole string after every removal (O(n²)) is too slow.
