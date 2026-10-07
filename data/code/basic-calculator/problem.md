Evaluate an arithmetic expression given as a string and return its integer value. The grammar is:

- non-negative integer literals,
- binary `+` and `-`,
- unary minus: a `-` may also negate the number or parenthesised group that follows it (for example `"-2"`, `"-(3+4)"`, `"1-(-2)"`); a unary minus only appears at the very start of the expression or right after `(`, and unary `+` does not occur,
- parentheses `(` and `)`, which can be nested,
- spaces anywhere between tokens.

There is no multiplication or division. Do not use any built-in function that evaluates strings as code.

**Example 1**
Input: s = "1 + 1"
Output: 2

**Example 2**
Input: s = " 2-1 + 2 "
Output: 3

**Example 3**
Input: s = "(1+(4+5+2)-3)+(6+8)"
Output: 23

**Constraints**
- 1 ≤ s.length ≤ 3 × 10⁵
- s is a valid expression made of digits, `+`, `-`, `(`, `)` and spaces
- every literal, the answer and every intermediate value fit in a signed 32-bit integer

**Notes**: the hidden tests include expressions over 100,000 characters with parentheses nested hundreds of levels deep. A stack-based single pass is the expected approach (a recursive solution also works if it stays O(n)).
