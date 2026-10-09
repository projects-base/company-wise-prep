**Short answer:** A `final` method cannot be overridden by a subclass. It can still be inherited, called and overloaded. For `synchronized` instance methods, the lock is the object itself (`this`), so on one object only one thread at a time can be inside *any* of its synchronized instance methods. Non-synchronized methods of that object can run at the same time, and `static synchronized` methods lock the `Class` object, so they don't block instance synchronized methods.

## Explanation

**`final` on methods**
- The compiler rejects an override in a subclass.
- Use it to protect behaviour a subclass must not change, for example a template method that defines an algorithm's steps.
- `private` methods can't be overridden anyway (they aren't visible), and `static` methods are hidden, not overridden.
- `final` is not needed for performance. HotSpot's JIT inlines non-final methods too, using class-hierarchy analysis, and deoptimises if a new subclass is loaded later.
- `final` on a class prevents subclassing (for example `String`). Java 17 sealed classes give finer control: only listed subclasses are allowed.

**`synchronized` methods**
- A `synchronized` instance method behaves like `synchronized (this) { ... }` around the body. A `static synchronized` method locks `SomeClass.class`.
- Every object has one monitor. Thread T1 in `a()` holds the monitor, so thread T2 calling `b()` (also synchronized) on the same object blocks.
- Different objects have different monitors, so two threads can run synchronized methods on two different instances in parallel.
- Monitors are **reentrant**: a thread holding the lock can call another synchronized method on the same object.
- `wait()` releases the monitor while waiting; `sleep()` does not.
- Leaving the block (normally or by exception) releases the lock and gives happens-before with the next thread that acquires it.

## Example

```java
class Account {
    private int balance;

    synchronized void deposit(int x)  { balance += x; }   // locks this
    synchronized void withdraw(int x) { balance -= x; }   // same lock as deposit
    int peek() { return balance; }                        // no lock: runs concurrently,
                                                          // may see a stale value
    static synchronized void audit() { }                  // locks Account.class
}
```

| Thread 1 | Thread 2 (same object) | Concurrent? |
|---|---|---|
| `deposit` | `withdraw` | No, same monitor |
| `deposit` | `deposit` | No |
| `deposit` | `peek` | Yes |
| `deposit` | `Account.audit()` | Yes, different monitor |
| `deposit` on `a1` | `deposit` on `a2` | Yes |

## Pitfalls and follow-ups

- **Can a subclass override a synchronized method without `synchronized`?** Yes. `synchronized` is not inherited as part of the signature, so the override is not synchronized unless you add it.
- **Can a `final` method be overloaded?** Yes. Overloading creates a different method signature.
- **Can a constructor be `synchronized` or `final`?** No to both; it is a compile error.
- **Why is `peek()` unsafe?** Without the lock (or `volatile`), there's no happens-before edge, so the reader may see a stale value.
- **Virtual threads (Java 21):** blocking inside `synchronized` pins the virtual thread to its carrier thread. JDK 24 (JEP 491) removed most of this pinning. On 21, prefer `ReentrantLock` around blocking calls.
- **Locking on `this` is public:** any outside code can `synchronized (account)` and interfere. A private lock object avoids that.

Related: [B2 · Locks](../academy/lessons/B2.md), [A6 · Java Memory Model](../academy/lessons/A6.md).
