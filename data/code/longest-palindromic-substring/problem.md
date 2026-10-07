Given a string `s`, return a longest contiguous substring of `s` that reads the same forwards and backwards. If several different substrings share the maximum length, return any one of them.

**Example 1**
Input: s = "babad"
Output: "bab" (the checker prints its length, 3)
Why: "aba" is just as long and is also accepted.

**Example 2**
Input: s = "cbbd"
Output: "bb" (the checker prints 2)

**Constraints**
- 1 ≤ s.length ≤ 3000
- `s` contains only English letters and digits

**Notes**: any longest palindrome is accepted. The checker verifies that your answer is a non-empty palindromic substring of `s` and then prints its **length**, so the expected output shown for a test is the maximum length (`3` for "babad"). The hidden tests include 3000-character strings, so a cubic solution will be too slow.
