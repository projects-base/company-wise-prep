**Short answer:** Store, next to every element, the minimum of all elements at or below it. Push stores `min(val, previous min)`; pop just drops the top entry, which brings back the previous minimum; `getMin` reads the top's stored minimum. All O(1). Two parallel `int[]` arrays avoid boxing.

## Approach

- **Brute force:** scan the stack in `getMin`. O(n) per call.
- **One "current min" variable is not enough:** after popping the minimum you do not know the next one.
- **Key insight:** a stack only changes at the top, so the minimum of the elements below a position never changes while that position exists. Save it with the element.
- **Space-saving variant:** a second stack that only receives a value when it is `≤` the current min, popped when the popped value equals its top. `≤` (not `<`) keeps duplicate minimums correct.

## Solution

```java
import java.util.Arrays;

class MinStack {
    private int[] vals = new int[16], mins = new int[16];  // mins[i] = min of vals[0..i]
    private int size;

    public void push(int val) {
        if (size == vals.length) {
            vals = Arrays.copyOf(vals, size * 2);
            mins = Arrays.copyOf(mins, size * 2);
        }
        vals[size] = val;
        mins[size] = size == 0 ? val : Math.min(val, mins[size - 1]);
        size++;
    }

    public void pop() { size--; }

    public int top() { return vals[size - 1]; }

    public int getMin() { return mins[size - 1]; }
}
```

## Complexity

- **Time:** O(1) per operation (amortised O(1) for `push` because of array doubling).
- **Space:** O(n): two ints per element.

## Edge cases

- Duplicate minimums (`push 1, push 1, pop`): the min is still 1.
- `Integer.MIN_VALUE` / `MAX_VALUE`: only compared, never subtracted, so no overflow. The "store the difference from the min" trick for O(1) extra space must use `long` for this reason.
- Operations on an empty stack are excluded by the spec; in production throw `NoSuchElementException` rather than `ArrayIndexOutOfBoundsException`.

## Follow-up: the OOP / Factory pattern version

The report says this was turned into an OOP question solved with a Factory. A sensible reading: there are several ways to implement the same contract, and callers should not depend on which one they get.

```java
public interface MinStack {
    void push(int val);
    void pop();
    int top();
    int getMin();
}

final class PairArrayMinStack implements MinStack { /* the solution above */ }
final class AuxStackMinStack implements MinStack { /* second stack, push only when <= min */ }

public final class MinStacks {
    public enum Kind { FAST, MEMORY_LEAN }

    private MinStacks() { }

    public static MinStack create(Kind kind) {
        return switch (kind) {
            case FAST -> new PairArrayMinStack();
            case MEMORY_LEAN -> new AuxStackMinStack();
        };
    }
}
```

Points to make: callers code against the interface (dependency inversion); the factory is the one place that knows the concrete classes; adding a thread-safe or generic `MinStack<T extends Comparable<T>>` variant does not change callers. In Spring you would usually let the container inject the implementation instead of a hand-written factory. See [E2 · Creational patterns](../academy/lessons/E2.md) and [E1 · SOLID](../academy/lessons/E1.md).

## Variations

- **Max Stack:** the same with `max`.
- **Generic min stack:** `Comparator<T>` and `Object[]` arrays.
- **Min queue:** two min stacks (amortised O(1)), or a monotonic deque.

Practise it in the app: Run / Submit on this page.
