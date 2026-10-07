A product has versions `1, 2, …, n`. At some point a bad change was released: every version from the first bad one onward fails its checks, and every version before it passes. You can test a version with the provided API `boolean isBadVersion(int version)` (inherited from `VersionControl`).

Return the number of the **first bad version**, calling the API as few times as possible.

**Example 1**
Input: n = 5, bad = 4
Output: 4
Why: isBadVersion(3) is false and isBadVersion(4) is true, so 4 is the first bad version.

**Example 2**
Input: n = 1, bad = 1
Output: 1

**Example 3**
Input: n = 10, bad = 1
Output: 1

**Constraints**
- 1 ≤ bad ≤ n ≤ 2³¹ − 1
- the second input line (`bad`) is only used by the test harness to answer `isBadVersion` — your method receives just `n`

**Notes**: the checker allows at most **40** calls to `isBadVersion`; binary search needs about 31. Watch out for `int` overflow when computing a midpoint near 2³¹ − 1.
