You are given an expression made of single-letter variables (`a`–`z`), the operators `+` and `-`, and parentheses. Combine like terms and return the simplified expression as a string.

Input rules:
- There are no spaces and no numbers; every term is a single variable.
- A `-` may appear at the very start, or right after `(`, to negate the first term (for example `-a+b` or `a-(-b+c)`).
- Parentheses are never nested (at most one level), and a parenthesised group may be preceded by `+`, by `-` (which negates everything inside), or start the expression.

Output rules (so that there is exactly one correct string):
- One term per variable that has a non-zero total coefficient, in **alphabetical order**.
- A coefficient of `1` is written as just the letter (`a`), `-1` as `-a`, and any other value as the number followed by the letter (`2a`, `-3b`).
- Terms are joined by `+` or `-` according to their sign; the first term has no leading `+`.
- If every coefficient is zero, return `"0"`.

**Example 1**
Input: expression = "a+b+a+c-b-b-b+(d+c)"
Output: "2a-2b+2c+d"
Why: a appears twice, b once with + and three times with −, c twice, d once.

**Example 2**
Input: expression = "x-(y-x)"
Output: "2x-y"
Why: the minus in front of the parentheses flips the signs inside: x − y + x.

**Example 3**
Input: expression = "a-a+(b-b)"
Output: "0"

**Constraints**
- 1 ≤ expression.length ≤ 10⁵
- the expression is well formed: no empty parentheses, no two operators in a row
