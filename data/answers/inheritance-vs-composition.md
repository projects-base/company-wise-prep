**Short answer:** Inheritance is an "is-a" relationship: the subclass extends a class and inherits its implementation, so it is tightly coupled to the parent's internals. Composition is "has-a": the class holds another object and delegates to it, depending only on its public API. Prefer composition unless there is a true is-a relationship and the parent was designed for extension. In the LRU cache context: `extends LinkedHashMap` is the inheritance shortcut, while a class that *wraps* a `LinkedHashMap` and a lock is composition.

## Explanation

**Inheritance**
- Reuses code and enables polymorphism (a subclass can be used wherever the parent is expected).
- Breaks encapsulation: the subclass depends on *how* the parent works, not just what it does. If the parent's self-calls change, the subclass can break (the fragile base class problem).
- Fixed at compile time; Java allows a single superclass.
- Exposes the whole parent API, including methods you may not want callers to use.

**Composition**
- The class owns a field of another type and forwards calls to it.
- Only the public contract is used, so the inner implementation can change.
- Can be swapped at runtime (inject a different implementation).
- You choose exactly which operations to expose, which is how you enforce invariants like thread safety.

The classic example (Effective Java): a `HashSet` subclass that counts additions by overriding both `add` and `addAll` double-counts, because `HashSet.addAll` calls `add` internally. A wrapper class that forwards to a `Set` doesn't have this problem.

## Example

```java
// Inheritance: short, but every LinkedHashMap method is public on the cache,
// and none of them are thread-safe.
class LruCacheV1<K, V> extends LinkedHashMap<K, V> {
    private final int capacity;
    LruCacheV1(int capacity) { super(16, 0.75f, true); this.capacity = capacity; }
    @Override protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > capacity;
    }
}

// Composition: the map is private; the class controls the API and the locking.
final class LruCache<K, V> {
    private final Map<K, V> map;
    private final ReentrantLock lock = new ReentrantLock();

    LruCache(int capacity) {
        this.map = new LinkedHashMap<>(16, 0.75f, true) {
            @Override protected boolean removeEldestEntry(Map.Entry<K, V> e) {
                return size() > capacity;
            }
        };
    }

    V get(K key) {
        lock.lock();
        try { return map.get(key); }       // access order changes on get, so lock reads too
        finally { lock.unlock(); }
    }

    void put(K key, V value) {
        lock.lock();
        try { map.put(key, value); }
        finally { lock.unlock(); }
    }
}
```

With `LruCacheV1`, a caller can still call `putAll`, `entrySet().iterator()` or `compute` without any lock, so making it thread-safe means overriding many methods. With composition, there are only two entry points to protect.

## Pitfalls and follow-ups

- **Why must `get` take the lock in an access-ordered `LinkedHashMap`?** `get` moves the entry to the end of the list, which is a structural change.
- **Why not `Collections.synchronizedMap(lruMap)`?** It works for single calls, but iteration still needs manual synchronisation. It is itself composition (a wrapper).
- **Singleton for the cache?** An `enum` singleton or the holder idiom (a static nested class initialised lazily by the JVM's class initialisation, which is thread-safe). In Spring, a singleton-scoped bean is the usual answer.
- **When is inheritance right?** A real is-a relationship, a parent designed for extension (template method, abstract classes), or sealed hierarchies (Java 17+) modelling a closed set of types.
- **Design pattern names:** composition plus forwarding is the Decorator pattern when the wrapper implements the same interface.

Related: [E1 · SOLID](../academy/lessons/E1.md), [E3 · Structural patterns](../academy/lessons/E3.md), [B2 · Locks](../academy/lessons/B2.md).
