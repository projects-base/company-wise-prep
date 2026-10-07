Implement `k` stacks that all share **one** integer array of fixed size `capacity`. Every slot must be usable by any stack: a push may only fail when all `capacity` slots are in use, no matter how the elements are spread across the stacks (so splitting the array into `k` fixed parts is not enough). Implement class `KStacks`:

- `KStacks(int k, int capacity)` creates `k` empty stacks, numbered `0` to `k − 1`, sharing an array of `capacity` slots.
- `boolean push(int stack, int x)` pushes `x` onto stack number `stack` and returns `true`, or returns `false` (changing nothing) if all `capacity` slots are already used.
- `int pop(int stack)` removes and returns the top of that stack, or returns `-1` if it is empty.
- `int peek(int stack)` returns the top of that stack without removing it, or `-1` if it is empty.
- `boolean isEmpty(int stack)` returns whether that stack is empty.

**Input format**: two lines — the list of operation names, then the list of argument lists. The checker prints the list of return values, with `null` for the constructor.

**Example 1**
Input:
["KStacks","push","push","push","push","peek","pop","pop","isEmpty","push","pop"]
[[3,3],[0,10],[2,20],[0,11],[1,30],[0],[0],[0],[0],[1,30],[2]]
Output: [null,true,true,true,false,11,11,10,true,true,20]
Why: three pushes fill all 3 slots, so pushing onto stack 1 fails. Popping stack 0 twice frees two slots, so the later push onto stack 1 succeeds.

**Example 2**
Input:
["KStacks","pop","peek","isEmpty","push","peek","pop","pop"]
[[2,2],[0],[1],[1],[1,0],[1],[1],[1]]
Output: [null,-1,-1,true,true,0,0,-1]
Why: empty stacks report `-1`; note that `0` is a real value, distinct from the empty marker.

**Constraints**
- 1 ≤ k ≤ capacity ≤ 10⁵
- 0 ≤ x ≤ 10⁹ (so `-1` never collides with a real value)
- 0 ≤ stack < k
- at most 10⁵ calls in total

**Notes**: aim for O(1) per operation and O(k + capacity) extra memory — the classic approach keeps, besides the value array, a `next` array that links each used slot to the slot below it in its stack, a `top` index per stack, and a free-slot list threaded through the same `next` array.
