A **rhyme scheme** is a string of uppercase letters where lines with the same letter rhyme, e.g. `"ABAB"`. Only the *pattern* matters: two schemes of the same length are **equivalent** if, for every pair of positions `i` and `j`, the letters at `i` and `j` are equal in one scheme exactly when they are equal in the other. So `"ABAB"`, `"CDCD"` and `"BABA"` are all equivalent, but `"AABB"` is not equivalent to them.

You are given a short scheme `shortScheme` and a long scheme `longScheme` (at least as long). Append letters to the end of `shortScheme` so that the result has the same length as `longScheme` and is equivalent to it. The existing letters of `shortScheme` must not change.

When a new rhyme group starts (a letter in `longScheme` that has not appeared before), use the **alphabetically smallest** letter that does not appear anywhere in the result so far. This makes the answer unique. If no extension can work, return the empty string `""`.

**Example 1**
Input: shortScheme = "ABA", longScheme = "CDCEED"
Output: "ABACCB"
Why: the first three letters line up as C→A, D→B. E is a new group, and the smallest unused letter is C.

**Example 2**
Input: shortScheme = "AAB", longScheme = "ABCD"
Output: ""
Why: the short scheme says lines 1 and 2 rhyme, but the long one says they do not.

**Example 3**
Input: shortScheme = "B", longScheme = "AABCA"
Output: "BBACB"

**Constraints**
- 1 ≤ shortScheme.length ≤ longScheme.length ≤ 4 · 10⁴
- both strings contain only `A`–`Z`

**Notes**: because the two schemes have the same pattern, they use the same number of distinct letters, so there are always enough unused letters for the new groups.
