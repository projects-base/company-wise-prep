**Short answer:** Linear version: one stack, and `peekMax` scans it, O(n). O(1) version: keep a second stack in step with the first, where each entry stores the maximum of everything at or below that position. Push stores `max(x, previous max)`, pop removes from both stacks, and `peekMax` reads the top of the maxima stack. Every operation is O(1).

## Picture it

Each element carries "the max of everything at or below me" on the `maxima` stack (stacks written bottom → top):

| Step | Call | values | maxima | Returns |
|---|---|---|---|---|
| 1 | push(5) | [5] | [5] | — |
| 2 | push(1) | [5, 1] | [5, 5] | — |
| 3 | push(7) | [5, 1, 7] | [5, 5, 7] | — |
| 4 | peekMax() | [5, 1, 7] | [5, 5, 7] | 7 |
| 5 | pop() | [5, 1] | [5, 5] | 7 |
| 6 | peekMax() | [5, 1] | [5, 5] | 5 |
| 7 | top() | [5, 1] | [5, 5] | 1 |

After popping 7, the old maximum 5 is already waiting on top of `maxima`: no scan needed.

**The picture in one sentence:** a stack only changes at the top, so storing the running max beside each element lets a pop restore the previous max for free.

## Approach

- **Linear solution:** a plain stack (`ArrayDeque<Integer>`). `push`, `pop`, `top` are O(1); `peekMax` iterates the whole stack, O(n). Fine if `peekMax` is rare.
- **Why a single "current max" variable is not enough:** after you pop the maximum, you would not know the next one without scanning.
- **Key insight:** a stack only changes at the top. So the maximum of the elements below any position never changes while that position exists. Store it alongside each element; popping simply restores the previous maximum.
- **Space-saving variant:** push onto the maxima stack only when `x >= current max`, and pop from it only when the popped value equals its top. Use `>=` so duplicate maxima are handled.

## Solution

```java
import java.util.ArrayDeque;
import java.util.Deque;

class MaxStack {
    private final Deque<Integer> values = new ArrayDeque<>();
    private final Deque<Integer> maxima = new ArrayDeque<>(); // maxima.peek() = max of values

    public void push(int x) {
        values.push(x);
        maxima.push(maxima.isEmpty() ? x : Math.max(x, maxima.peek()));
    }

    public int pop() {
        maxima.pop();
        return values.pop();
    }

    public int top() { return values.peek(); }

    public int peekMax() { return maxima.peek(); }
}
```

The linear version, for comparison:

```java
import java.util.ArrayDeque;
import java.util.Deque;

class MaxStack {
    private final Deque<Integer> values = new ArrayDeque<>();

    public void push(int x) { values.push(x); }
    public int pop() { return values.pop(); }
    public int top() { return values.peek(); }

    public int peekMax() {
        int max = Integer.MIN_VALUE;
        for (int v : values) max = Math.max(max, v);
        return max;
    }
}
```

## Complexity

- **O(1) version:** O(1) time per operation; O(n) extra space for the maxima stack (at most n entries).
- **Linear version:** O(1) for `push` / `pop` / `top`, O(n) for `peekMax`; no extra space.

## Edge cases

- Duplicate maxima (`5, 1, 5`, then pop): the max must still be 5. The parallel stack handles it; the space-saving variant needs `>=`.
- Negative values only: initialise with the first element, not 0.
- Calls on an empty stack: not allowed by the spec; in production, throw `NoSuchElementException` (which `ArrayDeque.pop` already does).
- Use `ArrayDeque`, not the legacy synchronised `java.util.Stack`.

## Variations

- **With `popMax` (LeetCode 716):** keep a doubly linked list for stack order and a `TreeMap<Integer, List<Node>>` from value to nodes. `popMax` takes the last node of the largest key and unlinks it. O(log n) per operation. A lazy-deletion version uses a stack plus a max-heap of (value, id) with a set of removed ids.
- **Min Stack:** identical with `min`.
- **Max of a sliding window / queue:** a monotonic deque instead.

Practise it in the app: Run / Submit on this page.
