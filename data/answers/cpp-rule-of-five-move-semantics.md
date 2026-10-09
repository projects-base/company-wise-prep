**Short answer:** The move constructor runs when an object is initialised from an rvalue of the same type: a temporary, a `std::move(x)`, or a returned local when copy elision does not apply. `std::move` itself moves nothing; it is a cast to an rvalue reference. When `std::vector` grows it relocates its elements, and it uses the move constructor only if that constructor is `noexcept` (or the type cannot be copied); otherwise it copies, to keep `push_back`'s strong exception guarantee. So counting copies means counting each explicit copy plus every element relocated during reallocation when the move is not `noexcept`.

## Explanation

**Rule of three:** if a class needs a custom destructor, copy constructor or copy assignment, it almost certainly needs all three (it owns a resource). **Rule of five** adds move constructor and move assignment. **Rule of zero:** prefer members that manage themselves (`std::string`, `std::vector`, `std::unique_ptr`) so you write none of them.

Important generation rule: if you declare a destructor or a copy operation, the compiler does **not** generate the move operations. Moves then silently fall back to copies.

**Vector growth:** when `size() == capacity()`, `push_back` allocates a bigger block (libstdc++ and libc++ double; MSVC grows by 1.5x), constructs the new element, relocates the old ones with `std::move_if_noexcept`, then destroys the old block. If a move threw half-way, the original vector would already be damaged, so a throwing move is only used when there is no copy constructor.

## Example

```cpp
struct T {
    static inline int copies = 0, moves = 0;
    T() = default;
    T(const T&) { ++copies; }
    T(T&&) /* noexcept? */ { ++moves; }
};

std::vector<T> v;             // capacity 0 (libstdc++ growth: 1, 2, 4)
T a;
v.push_back(a);               // copy a                        cap 1
v.push_back(std::move(a));    // move a; relocate 1 old elem   cap 2
v.push_back(T{});             // move temp; relocate 2 old     cap 4
```

| Move ctor | copies | moves |
|---|---|---|
| not `noexcept` | 1 + 1 + 2 = **4** | 2 (only the new elements) |
| `noexcept` | **1** | 1 + 1 + 1 + 2 = 5 |

`std::string`'s move constructor is `noexcept`, so with `vector<std::string>` the only copy is the `push_back(s)` of an lvalue. Calling `v.reserve(3)` first removes all relocations. `v.emplace_back(args...)` constructs in place and removes the temporary's move too.

A correct rule-of-five class:

```cpp
class Buffer {
    std::size_t n_; int* p_;
public:
    explicit Buffer(std::size_t n) : n_(n), p_(new int[n]) {}
    ~Buffer() { delete[] p_; }
    Buffer(const Buffer& o) : n_(o.n_), p_(new int[o.n_]) { std::copy(o.p_, o.p_ + n_, p_); }
    Buffer(Buffer&& o) noexcept : n_(o.n_), p_(std::exchange(o.p_, nullptr)) { o.n_ = 0; }
    Buffer& operator=(Buffer o) noexcept {             // copy-and-swap covers both
        std::swap(n_, o.n_); std::swap(p_, o.p_); return *this;
    }
};
```

## Pitfalls and follow-ups

- **Why only `noexcept` moves on reallocation?** Strong guarantee: if relocation throws, the vector must be unchanged. Copies leave the originals intact; a half-finished move does not.
- **Is a moved-from object usable?** It is in a valid but unspecified state. You may assign to it or destroy it.
- **`return std::move(local)`?** Usually a pessimisation; it blocks NRVO. C++17 guarantees elision for returning a prvalue.
- **`const T` and `std::move`:** `std::move` on a `const` object yields `const T&&`, which binds to the copy constructor. You get a copy.
- **Java comparison:** Java copies references, never objects, so there is no copy/move distinction; `ArrayList` growth copies references with `Arrays.copyOf`.
