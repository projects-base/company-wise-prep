**Short answer:** Pass by value gives the function its own copy of the argument: changes stay local, and the cost is a copy (cheap for small types, expensive for a big `std::vector` or `std::string`). Pass by reference gives the function an alias to the caller's object: no copy, and changes are visible to the caller unless it is `const&`. Rule of thumb in C++: small trivially-copyable types (`int`, `double`, pointers, small structs, `std::string_view`, `std::span`) by value; larger read-only objects by `const T&`; out-parameters by `T&`; and "sink" parameters you will store by value and then `std::move`.

## Explanation

**By value** `void f(Widget w)`: the argument is copy-constructed (or move-constructed from an rvalue) into the parameter. Benefits: the callee owns it, no aliasing, and small values travel in registers (on x86-64 System V, a trivially-copyable struct up to 16 bytes can go in registers).

**By reference** `void f(Widget& w)` / `void f(const Widget& w)`: implemented as a pointer under the hood. No copy, but:
- every access is an indirection through memory;
- **aliasing**: the compiler must assume the referenced object may change through another pointer, which can block optimisations (it reloads values instead of keeping them in registers);
- the referenced object must outlive the use (dangling references).

**By pointer** `void f(Widget* w)`: like a reference but can be null and can be reseated; use it when "no object" is a valid input.

**Rvalue reference** `void f(Widget&& w)`: binds only to temporaries / `std::move`d values; lets the callee steal resources.

**Cost comparison**

| Argument | By value | By `const&` |
|---|---|---|
| `int`, `double` | register, fastest | pointer + load, slower |
| `std::string` (long) | heap allocation + copy | pointer only |
| `std::string` you will store | one move from an rvalue, one copy from an lvalue | always one copy when stored |

## Example

```cpp
void incVal(int x)        { ++x; }          // caller unchanged
void incRef(int& x)       { ++x; }          // caller changed
double sum(const std::vector<double>& v);   // read-only, no copy

class Order {
    std::string id_;
public:
    // sink parameter: by value + move covers lvalue (1 copy) and rvalue (0 copies)
    explicit Order(std::string id) : id_(std::move(id)) {}
};

int a = 1;
incVal(a);   // a == 1
incRef(a);   // a == 2
Order o1(someString);            // copy into param, move into member
Order o2(std::string("X-1"));    // move, move
```

## Pitfalls and follow-ups

- **Returning a reference to a local** is undefined behaviour (dangling).
- **`const&` to a temporary** extends the temporary's lifetime only for a local reference bound directly to it, not through a function return.
- **Why pass `std::string_view` by value?** It is two words (pointer + length); copying is cheaper than an extra indirection.
- **Large objects by value in hot loops** cost allocations; small ones by reference can cost aliasing. Measure.
- **Java comparison:** Java is always pass by value; for objects the *reference* is copied, so the callee can mutate the object but cannot make the caller's variable point elsewhere.
