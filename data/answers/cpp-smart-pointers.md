**Short answer:** `unique_ptr` is sole ownership: move-only, the same size as a raw pointer (with the default deleter), and zero runtime overhead. `shared_ptr` is shared ownership through a reference-counted control block: copying it does an atomic increment, and the object dies when the last owner goes. `weak_ptr` observes an object owned by `shared_ptr`s without keeping it alive; you `lock()` it to get a temporary `shared_ptr`. Default to `unique_ptr`; reach for `shared_ptr` only when ownership is genuinely shared, and use `weak_ptr` to break cycles and for caches.

## Explanation

**`std::unique_ptr<T>`**
- Destructor deletes the object. Copy is deleted, move transfers ownership.
- A stateless custom deleter adds no size; a stateful deleter (e.g. a function pointer) adds its size.
- `unique_ptr<T[]>` calls `delete[]`.
- Typical uses: factory return types, pimpl, owning members, handles with custom deleters (`FILE*`, sockets).

**`std::shared_ptr<T>`**
- Two pointers wide: one to the object, one to a *control block* holding the strong count, the weak count and the deleter.
- Copy and destroy update the count atomically, so passing `shared_ptr` by value in a hot path costs cache-line traffic between cores.
- `std::make_shared` allocates object and control block in one allocation (better locality, one fewer `new`). Trade-off: the memory block is not freed until the last `weak_ptr` is also gone, because they share it.
- The count is thread-safe; the pointed-to object is **not** made thread-safe.

**`std::weak_ptr<T>`**
- Points at the control block, bumps the weak count only.
- `lock()` returns a `shared_ptr` (empty if expired). `expired()` alone is racy; always use `lock()`.
- Uses: break parent/child cycles, observer lists, caches that should not extend lifetime.

## Example

```cpp
struct Order { std::string id; };

std::unique_ptr<Order> make(std::string id) {
    return std::make_unique<Order>(Order{std::move(id)});
}

auto o  = make("A1");              // owns
auto o2 = std::move(o);            // o is now null

auto s1 = std::make_shared<Order>(Order{"B2"});
auto s2 = s1;                      // use_count == 2
std::weak_ptr<Order> w = s1;

s1.reset(); s2.reset();            // Order destroyed here
if (auto p = w.lock()) { /* not reached */ }

// Cycle: leaks with shared_ptr both ways
struct Node {
    std::shared_ptr<Node> child;
    std::weak_ptr<Node>   parent;  // weak breaks the cycle
};
```

## Pitfalls and follow-ups

- **Two control blocks:** `shared_ptr<T> a(raw); shared_ptr<T> b(raw);` gives two counts and a double delete. Create once; inside the class use `enable_shared_from_this` and `shared_from_this()`.
- **How to pass them to functions?** If the callee only uses the object, pass `T&` or `T*`. Pass `unique_ptr` by value to transfer ownership; `shared_ptr` by value only if the callee keeps a copy.
- **`make_shared` vs `shared_ptr(new T)`:** one allocation vs two; before C++17, `make_*` also avoided a leak in `f(shared_ptr<T>(new T), g())` if `g()` threw.
- **Overhead in a low-latency path?** `unique_ptr` is free; `shared_ptr` copies are atomics. Hot paths in trading code avoid `shared_ptr` churn and often avoid heap allocation entirely (pools, arenas).
- **Java comparison:** every Java reference behaves roughly like a `shared_ptr` whose count is replaced by the GC's reachability analysis, which is why cycles are not a problem in Java.

- **Polymorphism:** deleting a derived object through `unique_ptr<Base>` still needs a virtual destructor in `Base`. (`shared_ptr<Base>` created from `make_shared<Derived>` remembers the right deleter, but do not rely on that.)
