**Short answer:** First pin down the grammar (here: `+`, `-`, unary minus, nested parentheses, no `*` or `/`). Then do one pass with a running `result` and the `sign` of the next term. On `(`, push the current `result` and `sign` onto a stack and start fresh. On `)`, pop them and fold the inner value back: `result = outerResult + outerSign * result`. Unary minus needs no special case — it is just `sign = -1` before a number or a group. O(n) time.

## Approach

- **Clarify first** (the prompt is deliberately vague): which operators, is there unary minus, are there parentheses, can there be spaces, does it fit in `int`, is the input always valid? Settle these before coding.
- **Recursive descent:** `expr := term (('+'|'-') term)*` and `term := number | '(' expr ')' | '-' term`. This is clean and extends easily to `*` and `/`. But nesting hundreds of levels deep means recursion that deep, which is fine at this size but is a risk with a small stack.
- **Key insight:** with only `+` and `-`, each parenthesis level is just a running sum. To enter a group you only need to remember two things about the outer level: its sum so far and the sign in front of the group. A stack of `(result, sign)` pairs replaces recursion.
- **Why unary minus is free:** `-` sets `sign = -1`. At the start, or right after `(`, the result is 0, so `0 + (-1) * x` is just negation. `"-(3+4)"` becomes push(0, -1), the inner sum is 7, and then 0 − 7 = −7.

## Solution

```java
import java.util.*;

class Solution {
    public int calculate(String s) {
        Deque<Long> stack = new ArrayDeque<>();   // saved (result, sign) of outer levels
        long result = 0;
        int sign = 1;
        int n = s.length();
        for (int i = 0; i < n; i++) {
            char c = s.charAt(i);
            if (Character.isDigit(c)) {
                long num = 0;
                while (i < n && Character.isDigit(s.charAt(i))) num = num * 10 + (s.charAt(i++) - '0');
                i--;                               // the for loop will advance again
                result += sign * num;
            } else if (c == '+') {
                sign = 1;
            } else if (c == '-') {
                sign = -1;                         // binary and unary minus behave the same
            } else if (c == '(') {
                stack.push(result);
                stack.push((long) sign);
                result = 0;
                sign = 1;
            } else if (c == ')') {
                long outerSign = stack.pop();
                long outerResult = stack.pop();
                result = outerResult + outerSign * result;
            }
            // spaces fall through and are ignored
        }
        return (int) result;
    }
}
```

## Complexity

- **Time:** O(n). Each character is read once.
- **Space:** O(d) for the stack, where d is the maximum nesting depth.

## Edge cases

- `"1-(-2)"` → 3: the inner unary minus gives −2, and the outer minus flips it.
- Deep nesting such as `"((((1))))"` → the stack grows. There is no recursion, so no `StackOverflowError`.
- Spaces inside numbers do not occur (spaces are only between tokens), so the inner digit loop is safe.
- Using `long` internally guards the intermediate `sign * num`.

## Variations

- **Add `*` and `/` (Basic Calculator III):** recursive descent with two precedence levels, or shunting-yard (operator stack + operand stack). Or reuse the Calculator II `result`/`last` trick inside a recursive call per parenthesis.
- **Return an AST** instead of a value, so the expression can be evaluated many times with variables.

## Follow-ups

- **How would this scale with distribution, caching or parallelism?** Evaluating one expression is already linear and very cheap, so distributing a single small expression costs more than it saves. Where it helps:
  - **Caching:** if the same expressions repeat, cache by the normalised expression string (or by a hash of the parsed AST) → value. Parse once and store the AST if only the variables change.
  - **Parallelism for a huge expression:** with only `+`/`-`, each number contributes `±number`. Its final sign is the product of the signs of all the groups that enclose it. That can be computed with a parallel prefix scan over the string, after which the signed numbers are summed with a parallel reduction (for example a `ForkJoinPool` or a parallel stream). With `*` and `/`, build a balanced AST and evaluate independent subtrees in parallel.
  - **Many expressions:** that is the real scaling case. They are independent, so spread them across workers through a queue. Each evaluation is stateless and safe to retry.

Practise it in the app: Run / Submit on this page.
