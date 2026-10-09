**Short answer:** Use a stack. For each opening bracket, push the closing bracket it expects. For each closing bracket, the stack must be non-empty and its top must equal that character. At the end, the stack must be empty. O(n) time, O(n) space.

## Approach

- **Brute force.** Repeatedly delete `"()"`, `"[]"` and `"{}"` until nothing changes, then check whether the string is empty. O(n²).
- **Key insight.** Brackets close in reverse order of opening, which is last in, first out: a stack. Pushing the *expected closer* instead of the opener makes the check a single comparison, with no lookup table.
- **Use `ArrayDeque`, not `Stack`.** `java.util.Stack` is a synchronised legacy class built on `Vector`.

## Solution

```java
import java.util.*;

class Solution {
    public boolean isValid(String s) {
        Deque<Character> stack = new ArrayDeque<>();
        for (char c : s.toCharArray()) {
            if (c == '(') stack.push(')');
            else if (c == '[') stack.push(']');
            else if (c == '{') stack.push('}');
            else if (stack.isEmpty() || stack.pop() != c) return false;
        }
        return stack.isEmpty();
    }
}
```

`stack.pop()` returns a `Character`, and `!= c` unboxes it before comparing, so the comparison is by value.

## Complexity

- **Time:** O(n).
- **Space:** O(n) in the worst case (all openers). An early exit when `s.length()` is odd saves work but does not change the bound.

## Edge cases

- Odd length: always false.
- A closer with an empty stack (`")("`).
- Leftover openers at the end (`"(("`).
- Wrong nesting (`"([)]"`): false. Correct nesting (`"{[]}"`): true.

## Follow-ups

- **Minimum number of reversals to balance a string of `{` and `}`.** If the length is odd, return -1. First cancel matched pairs with a counter: scan left to right, an opener increments `open`, and a closer either cancels an `open` or increments `close`. What remains has the form `}}}…{{{`, with `close` closers then `open` openers. Each pair `}}` needs one reversal and each pair `{{` needs one. A leftover `}{` needs two. So the answer is `ceil(close / 2) + ceil(open / 2)`. For example, `"}{{}}{{{"` leaves `close = 1`, `open = 3`, so the answer is 1 + 2 = 3.

```java
static int minReversals(String s) {
    if (s.length() % 2 != 0) return -1;
    int open = 0, close = 0;
    for (char c : s.toCharArray()) {
        if (c == '{') open++;
        else if (open > 0) open--;   // matched pair
        else close++;                // unmatched closer
    }
    return (close + 1) / 2 + (open + 1) / 2;
}
```

- **Only one bracket type:** a counter replaces the stack, O(1) space. It must never go negative, and it must end at 0.
- **Minimum insertions to make it valid (LC 921):** count unmatched closers plus leftover openers.
- **Longest valid substring (LC 32):** a stack of indices, or two counter passes.

Practise it in the app: Run / Submit on this page.
