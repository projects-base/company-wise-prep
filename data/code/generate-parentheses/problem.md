Given `n`, return every string made of exactly `n` opening and `n` closing parentheses that is balanced — reading left to right, the number of `)` never exceeds the number of `(` seen so far, and the totals are equal. Each such string must appear exactly once. You may return them in any order.

**Example 1**
Input: n = 3
Output: ["((()))","(()())","(())()","()(())","()()()"]

**Example 2**
Input: n = 1
Output: ["()"]

**Constraints**
- 1 ≤ n ≤ 8

**Notes**: any order is accepted — the judge sorts your list before comparing. Duplicates are not allowed.
