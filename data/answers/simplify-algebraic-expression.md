**Short answer:** Keep a coefficient counter for each of the 26 letters. Scan the expression once, tracking the sign of the current term and the sign applied to the current parenthesised group (a stack, the same trick as Basic Calculator). Each letter adds `groupSign * sign` to its counter. Finally print the non-zero coefficients in alphabetical order with the formatting rules.

## Approach

- **Brute force:** expand parentheses by string rewriting (flip signs inside `-( … )`), then split into terms and count. It works but is fiddly and can be quadratic with repeated rewriting.
- **Key insight:** each variable's final coefficient is just the sum of ±1 over its occurrences. The only question is the effective sign of each occurrence, which is the product of the operator before it and the signs of all enclosing groups. A stack of group signs gives that product in O(1).
- **Algorithm:**
  - `+` / `-` set `sign` for the next term.
  - `(` pushes `groupSign.peek() * sign`, then resets `sign = 1` (a `-` right after `(` will set it again).
  - `)` pops.
  - A letter adds `groupSign.peek() * sign` to `coef[letter]`, then resets `sign = 1`.
- **Output:** for each letter with `k != 0`: write `-` if negative, `+` if positive and not first; write `|k|` unless it is 1; then the letter. Empty result means `"0"`.

The stack handles any nesting depth, even though the problem only promises one level.

## Solution

```java
import java.util.*;

class Solution {
    public String simplify(String expression) {
        long[] coef = new long[26];
        Deque<Integer> groupSign = new ArrayDeque<>();   // sign applied to the current group
        groupSign.push(1);
        int sign = 1;
        for (char c : expression.toCharArray()) {
            if (c == '+') sign = 1;
            else if (c == '-') sign = -1;
            else if (c == '(') { groupSign.push(groupSign.peek() * sign); sign = 1; }
            else if (c == ')') groupSign.pop();
            else { coef[c - 'a'] += (long) groupSign.peek() * sign; sign = 1; }
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 26; i++) {
            long k = coef[i];
            if (k == 0) continue;
            if (k < 0) sb.append('-');
            else if (sb.length() > 0) sb.append('+');
            if (Math.abs(k) != 1) sb.append(Math.abs(k));
            sb.append((char) ('a' + i));
        }
        return sb.length() == 0 ? "0" : sb.toString();
    }
}
```

## Complexity

- **Time:** O(L) for the scan plus O(26) for the output.
- **Space:** O(depth) for the stack (O(1) with one level of parentheses) and O(26) for the counters.

## Edge cases

- Everything cancels: `"0"`.
- Leading minus: `-a+b` gives `-a+b`.
- Minus before a group: `x-(y-x)` gives `2x-y`.
- Minus right after `(`: `a-(-b+c)` gives `a+b-c`.
- Coefficient of ±1 prints with no number.

## Variations

- **Numeric coefficients (`3a-2b`):** parse a number before the letter and multiply by it.
- **Multiplication (`2(a+b)`):** push a multiplier instead of just a sign.
- **Multi-letter variables or products like `ab`:** key a `TreeMap<String, Long>` by the term instead of a 26-slot array.

Practise it in the app: Run / Submit on this page.
