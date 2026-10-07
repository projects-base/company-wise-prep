Design a stack that, besides the usual operations, can report its current maximum. Implement class `MaxStack`:

- `MaxStack()` creates an empty stack.
- `void push(int x)` pushes `x` on top.
- `int pop()` removes the top element and returns it.
- `int top()` returns the top element without removing it.
- `int peekMax()` returns the largest element currently in the stack without removing anything.

`pop`, `top` and `peekMax` are only called on a non-empty stack. (Unlike LeetCode's version, there is no `popMax`.)

**Input format**: two lines — the list of operation names, then the list of argument lists. The checker prints the list of return values, with `null` for the constructor and `push`.

**Example 1**
Input:
["MaxStack","push","push","push","peekMax","pop","peekMax","top"]
[[],[5],[1],[5],[],[],[],[]]
Output: [null,null,null,null,5,5,5,1]
Why: after popping the second 5 the stack is [5,1]; its maximum is still 5 and its top is 1.

**Example 2**
Input:
["MaxStack","push","push","peekMax","pop","peekMax"]
[[],[2],[7],[],[],[]]
Output: [null,null,null,7,7,2]

**Constraints**
- −10⁷ ≤ x ≤ 10⁷
- at most 10⁵ calls in total

**Notes**: a linear scan for the maximum is the first idea; aim for O(1) per operation (for example, keep a second stack of running maxima). The hidden tests include 10,000 calls on a deep stack.
