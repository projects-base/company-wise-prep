Given a string `s` made only of the bracket characters `(`, `)`, `{`, `}`, `[` and `]`, return `true` if the brackets are properly balanced and `false` otherwise. Balanced means every opening bracket is closed by a bracket of the same type, closings happen in the reverse order of openings (inner pairs close before outer ones), and no closing bracket appears without a matching opening one.

**Example 1**
Input: s = "()"
Output: true

**Example 2**
Input: s = "()[]{}"
Output: true

**Example 3**
Input: s = "(]"
Output: false

**Constraints**
- 1 ≤ s.length ≤ 10⁴
- `s` contains only the six bracket characters

**Notes**: "([)]" is `false` (wrong nesting) and "{[]}" is `true`.
