Evaluate an arithmetic expression given as a string and return its integer value. The expression contains non-negative integers, the operators `+`, `-`, `*`, `/`, and spaces (no parentheses). Usual precedence applies: `*` and `/` bind tighter than `+` and `-`, and operators of equal precedence are applied left to right. Division is integer division that truncates toward zero.

Do not use any built-in function that evaluates strings as code.

**Example 1**
Input: s = "3+2*2"
Output: 7

**Example 2**
Input: s = " 3/2 "
Output: 1

**Example 3**
Input: s = " 3+5 / 2 "
Output: 5
Why: 5 / 2 = 2 first, then 3 + 2.

**Constraints**
- 1 ≤ s.length ≤ 3 × 10⁵
- s is a valid expression; every number is a non-negative integer below 2³¹
- there is no division by zero
- the answer and every intermediate value fit in a signed 32-bit integer

**Notes**: spaces can appear anywhere between tokens, including at the very start and end. There is no unary minus. Expect expressions around 100,000 characters long in the hidden tests, so aim for a single O(n) pass.
