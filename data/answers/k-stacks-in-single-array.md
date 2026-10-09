**Short answer:** Treat the array as a pool of slots and turn each stack into a linked list through a parallel `next[]` array. Keep `top[k]` for the head of each stack and a `free` head for a linked list of unused slots, threaded through the same `next[]`. Push takes a slot off the free list and links it on top of the stack; pop does the reverse. Every operation is O(1), and a push only fails when every slot is used.

## Approach

- **Brute force:** split the array into k equal parts. Simple, but one stack can be full while the others are empty, which the problem forbids.
- **Better but slow:** two stacks can grow from both ends; for k > 2 you would have to shift whole blocks when one segment fills, which is O(capacity).
- **Key insight:** a stack only needs to know "what is below me". Store that as an index in `next[]`, and the elements of one stack can sit anywhere in the array. Free slots form one more linked list, so finding a free slot is O(1) too.

## Solution

```java
import java.util.Arrays;

class KStacks {
    private final int[] values; // the one shared array
    private final int[] next;   // used slot: slot below it; free slot: next free slot
    private final int[] top;    // top slot of each stack, -1 if empty
    private int free;           // head of the free list, -1 if full

    public KStacks(int k, int capacity) {
        values = new int[capacity];
        next = new int[capacity];
        top = new int[k];
        Arrays.fill(top, -1);
        for (int i = 0; i < capacity; i++) next[i] = i + 1 < capacity ? i + 1 : -1;
        free = 0;
    }

    public boolean push(int stack, int x) {
        if (free == -1) return false;
        int slot = free;
        free = next[slot];          // take slot off the free list
        values[slot] = x;
        next[slot] = top[stack];    // link it above the old top
        top[stack] = slot;
        return true;
    }

    public int pop(int stack) {
        int slot = top[stack];
        if (slot == -1) return -1;
        top[stack] = next[slot];    // unlink from the stack
        next[slot] = free;          // give it back to the free list
        free = slot;
        return values[slot];
    }

    public int peek(int stack) {
        int slot = top[stack];
        return slot == -1 ? -1 : values[slot];
    }

    public boolean isEmpty(int stack) { return top[stack] == -1; }
}
```

## Complexity

- **Time:** O(1) per operation; the constructor is O(k + capacity).
- **Space:** O(k + capacity): `values`, `next`, `top`.

## Edge cases

- All slots used: `free == -1`, push returns `false` and changes nothing.
- Pop/peek on an empty stack returns `-1` (safe because values are ≥ 0).
- Value `0` is a real value, not an "empty" marker.
- Pop then push reuses the freed slot immediately (the free list is LIFO), which also keeps memory warm in cache.
- `k == capacity`: every stack can still take every slot if the others are empty.

## Variations

- **K queues in one array:** same free list, but each queue keeps `front` and `rear`, and `next` points from older to newer.
- **Two stacks in one array:** simpler, grow one from the left and one from the right; full when the tops meet.
- This is exactly how a simple memory allocator or object pool keeps a free list.

Practise it in the app: Run / Submit on this page.
