Design a stack that, besides the usual operations, can report its smallest element — and every operation must take O(1) time. Implement the class `MinStack`:

- `MinStack()` creates an empty stack.
- `void push(int val)` puts `val` on top.
- `void pop()` removes the top element.
- `int top()` returns the top element.
- `int getMin()` returns the smallest element currently in the stack.

`pop`, `top` and `getMin` are only called when the stack is non-empty.

**Input format**: two lines — the list of operation names, then the list of argument lists (the first operation is always the constructor). The output is the list of return values, with `null` for the constructor, `push` and `pop`.

**Example 1**
Input:
["MinStack","push","push","push","getMin","pop","top","getMin"]
[[],[-2],[0],[-3],[],[],[],[]]
Output: [null,null,null,null,-3,null,0,-2]
Why: after popping -3, the stack is [-2,0], whose top is 0 and minimum is -2.

**Example 2**
Input:
["MinStack","push","push","getMin","pop","getMin"]
[[],[1],[1],[],[],[]]
Output: [null,null,null,1,null,1]
Why: duplicates of the minimum must be handled — popping one copy of 1 leaves another.

**Constraints**
- −2³¹ ≤ val ≤ 2³¹ − 1
- at most 3 · 10⁴ calls in total

**Notes**: write your code in the class `MinStack` (keep that name). The hidden tests include values at the int limits and a long sequence of operations on a deep stack, so scanning the stack in `getMin` will be slow.
