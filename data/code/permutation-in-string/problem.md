Given two strings `s1` and `s2`, return `true` if some contiguous substring of `s2` is a rearrangement of `s1` (uses exactly the same letters with the same counts), and `false` otherwise.

**Example 1**
Input: s1 = "ab", s2 = "eidbaooo"
Output: true
Why: s2 contains "ba", which is a rearrangement of "ab".

**Example 2**
Input: s1 = "ab", s2 = "eidboaoo"
Output: false

**Constraints**
- 1 ≤ s1.length, s2.length ≤ 10⁴
- both strings contain only lowercase English letters

**Notes**: if `s1` is longer than `s2` the answer is `false`. The hidden tests include 10,000-character strings.
