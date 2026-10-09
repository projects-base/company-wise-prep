**Short answer:** Scan once and keep two running values: `result`, the sum of finished terms, and `last`, the term being built. When an operator ends a number, apply the *previous* operator. `+` and `-` add `last` to `result` and start a new term (`num` or `-num`). `*` and `/` change `last` in place. At the end, return `result + last`. O(n) time, O(1) space, no stack needed.

## Approach

- **Stack version (the classic):** push `+num` or `-num`. For `*` or `/`, pop the top, combine it with `num`, and push the result back. The answer is the sum of the stack. It is O(n) time and O(n) space.
- **Key insight:** with only two precedence levels, the stack never needs more than its top element before the sum. Everything below the top is already final. So keep that top as `last` and the rest as a running `result`.
- **Sentinel trick:** act as if there is a `'+'` after the last character (`i <= n`), so the final number is flushed by the same code.
- **Division:** Java's `/` on integers truncates toward zero, which is what the problem asks for. Python's `//` would not.

## Solution

```java
class Solution {
    public int calculate(String s) {
        long result = 0, last = 0;   // finished terms; term still being built
        long num = 0;
        char op = '+';               // operator before the current number
        int n = s.length();
        for (int i = 0; i <= n; i++) {
            char c = i < n ? s.charAt(i) : '+';      // sentinel flushes the last number
            if (c == ' ') continue;
            if (Character.isDigit(c)) {
                num = num * 10 + (c - '0');
                continue;
            }
            switch (op) {
                case '+' -> { result += last; last = num; }
                case '-' -> { result += last; last = -num; }
                case '*' -> last = last * num;
                case '/' -> last = last / num;      // truncates toward zero
            }
            op = c;
            num = 0;
        }
        return (int) (result + last);
    }
}
```

Trace for `"3+5 / 2"`: at `+`, last = 3. At `/`, result = 3 and last = 5. At the sentinel, last = 5 / 2 = 2. Answer 3 + 2 = 5.

## Complexity

- **Time:** O(n), one pass with O(1) work per character.
- **Space:** O(1).

## Edge cases

- Spaces anywhere, including leading and trailing → skipped. They must not trigger an operator flush.
- A single number, e.g. `"42"` → the sentinel flushes it.
- Negative terms with division: `"14-3/2"` → last = -3, then -3 / 2 = -1 (toward zero), answer 13. Storing the sign in `last` keeps the truncation correct.
- Multi-digit numbers → build `num` digit by digit.

## Variations

- **Parentheses + all four operators (Basic Calculator III):** recursion — on `(`, evaluate the inner expression with this same function and use its value as `num`. Or use the shunting-yard algorithm with two stacks.
- **Only `+`, `-` and parentheses:** see Basic Calculator (a sign stack).

Practise it in the app: Run / Submit on this page.
