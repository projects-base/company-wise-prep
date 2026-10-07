A robot holds a string `s` and an initially empty string `t`. Until both are empty, it repeatedly does one of two things:

- remove the **first** character of `s` and append it to the end of `t`, or
- remove the **last** character of `t` and write it on paper (after everything written so far).

In other words, `t` behaves like a stack. Return the lexicographically smallest string that can end up written on the paper.

**Example 1**
Input: s = "zza"
Output: "azz"
Why: push all three characters, then pop them: a, z, z.

**Example 2**
Input: s = "bac"
Output: "abc"
Why: push b, push a, pop a, pop b, push c, pop c.

**Example 3**
Input: s = "bdda"
Output: "addb"

**Constraints**
- 1 ≤ s.length ≤ 10⁵
- s contains only lowercase English letters
