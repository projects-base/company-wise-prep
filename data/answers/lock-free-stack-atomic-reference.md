**Short answer:** Use a Treiber stack: a singly linked list whose `head` is an `AtomicReference<Node>`. To push, build a new node pointing to the current head and `compareAndSet(oldHead, newNode)`; to pop, read the head and `compareAndSet(oldHead, oldHead.next)`. If the CAS fails because another thread changed the head, retry in a loop. Nodes are immutable once published, so there are no locks and no torn states.

## Explanation

- **CAS** (compare-and-set) atomically replaces the value only if it still equals the expected value. On x86 it maps to `lock cmpxchg`; on AArch64 to LSE atomics or a load-exclusive/store-exclusive loop.
- **Push:** read `head`, create `new Node(value, head)`, try CAS. Failure means someone pushed or popped in between; re-read and try again.
- **Pop:** read `head`; if `null`, the stack is empty. Otherwise CAS `head` to `head.next`. The node we read is never modified, so reading `next` is safe.
- **Visibility:** `AtomicReference` reads and writes have volatile semantics, so a node's fields, written before the successful CAS, are visible to any thread that later reads it from `head`.
- **Progress:** it is *lock-free*: some thread always makes progress, but a given thread can, in theory, retry forever under heavy contention. It is not *wait-free*.

## Example

```java
import java.util.concurrent.atomic.AtomicReference;

public final class LockFreeStack<T> {
    private record Node<T>(T value, Node<T> next) {}

    private final AtomicReference<Node<T>> head = new AtomicReference<>();

    public void push(T value) {
        Node<T> oldHead, newHead;
        do {
            oldHead = head.get();
            newHead = new Node<>(value, oldHead);
        } while (!head.compareAndSet(oldHead, newHead));
    }

    public T pop() {                       // returns null when empty
        Node<T> oldHead;
        do {
            oldHead = head.get();
            if (oldHead == null) return null;
        } while (!head.compareAndSet(oldHead, oldHead.next()));
        return oldHead.value();
    }

    public T peek() {
        Node<T> h = head.get();
        return h == null ? null : h.value();
    }
}
```

`compareAndSet` compares references with `==`, not `equals`, which is what we want here.

## Pitfalls and follow-ups

**ABA problem.** Thread 1 reads head `A` (next `B`) and is paused. Thread 2 pops `A`, pops `B`, then pushes `A` back. Thread 1 resumes; head is `A` again, so its CAS succeeds and sets head to `B`, a node that is no longer in the stack.

- In Java with the code above, ABA cannot happen this way: every push allocates a *new* node, and the GC will not reuse `A`'s memory while thread 1 still holds a reference to it. So the "same" `A` can only come back if your code reuses node objects.
- It **does** happen if you pool or recycle nodes, or if you CAS on values rather than unique nodes.
- **Fix:** `AtomicStampedReference<Node<T>>`, which pairs the reference with an `int` version stamp; every update increments the stamp, so `A` with stamp 1 doesn't match `A` with stamp 3. `AtomicMarkableReference` holds a boolean instead. In C/C++ the fixes are tagged pointers, hazard pointers or epoch-based reclamation.

Other follow-ups:
- **Size?** A separate `AtomicInteger` won't be consistent with the stack at every instant; say it is approximate, or walk the list.
- **High contention?** Retries burn CPU. Add backoff, or use an elimination array (a push and a pop pair up and skip the head).
- **Why not `ConcurrentLinkedDeque`?** In production you would use it. The JDK's lock-free deque is well tested.
- **Lock-free queue?** Michael-Scott queue, the basis of `ConcurrentLinkedQueue`.

Related: [B3 · Atomics, CAS and concurrent collections](../academy/lessons/B3.md), [A6 · The Java Memory Model](../academy/lessons/A6.md).
