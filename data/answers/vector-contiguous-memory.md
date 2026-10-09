**Short answer:** Yes. The standard guarantees that `std::vector<T>` (except `vector<bool>`) stores its elements contiguously, so `&v[0] + i == &v[i]` and `v.data()` can be passed to C APIs. It is implemented with three pointers: start, end of the used elements, and end of the allocated capacity. When `push_back` finds `size() == capacity()`, it allocates a new block a constant factor larger (2x in libstdc++ and libc++, 1.5x in MSVC), moves or copies the elements across, destroys the old ones and frees the old block. Geometric growth makes `push_back` amortised O(1).

## Explanation

**Why contiguous?**
- O(1) random access by pointer arithmetic.
- Cache friendliness: sequential iteration touches consecutive 64-byte lines and the hardware prefetcher keeps up. This is why a `vector` often beats `list` even for inserts in the middle at moderate sizes.
- Interoperability with C (`write(fd, v.data(), v.size())`) and SIMD.

**How growth works**
1. Allocate `newCap` elements of raw memory.
2. Construct the new element in place first (so `v.push_back(v[0])` stays valid).
3. Relocate existing elements with `std::move_if_noexcept`: move if the move constructor is `noexcept` (or the type is not copyable), otherwise copy, to keep the strong exception guarantee.
4. Destroy the old elements, deallocate the old block, update the three pointers.

**Why geometric growth, not +k?** Growing by a constant amount makes `n` pushes cost O(n²) copies. Growing by factor `g` makes the total copies a geometric series bounded by about `n · g / (g - 1)`, so O(1) per push on average. A factor below the golden ratio (~1.618), such as 1.5, has the property that, in some allocators, freed earlier blocks can eventually add up to the size of the next request and be reused; 2x is simpler and does fewer reallocations.

**Things that do not shrink:** `clear()` and `erase` keep the capacity. `shrink_to_fit()` is a non-binding request; the swap idiom `std::vector<T>(v).swap(v)` forces it.

## Example

```cpp
std::vector<int> v;
for (int i = 0; i < 9; ++i) {
    v.push_back(i);
    std::printf("size %zu cap %zu data %p\n", v.size(), v.capacity(), (void*)v.data());
}
// libstdc++ capacities: 1 2 4 4 8 8 8 8 16 - data() changes at each growth

int* p = &v[0];
v.push_back(42);        // may reallocate: p, references and iterators now dangle

std::vector<int> w;
w.reserve(1'000);       // one allocation up front, no reallocation for 1000 pushes
```

## Pitfalls and follow-ups

- **Iterator invalidation:** any reallocation invalidates all pointers, references and iterators; insert/erase invalidates those at or after the point.
- **`reserve` vs `resize`:** `reserve` changes capacity only; `resize` changes size and constructs elements.
- **`vector<bool>`:** bit-packed proxy specialisation, not contiguous `bool`s; `&v[0]` is not a `bool*`.
- **Huge vectors:** reallocation briefly needs old + new memory (up to 3x size at peak with 2x growth). Reserve, or use `deque` if you cannot.
- **Elements of a vector of pointers** are contiguous, but the objects they point to are not.
- **Java comparison:** `ArrayList` is also a contiguous array (of references), growing by 1.5x; but the objects themselves are scattered on the heap unless they are primitives in a plain array.

Related: [C5 · Amortised analysis](../academy/lessons/C5.md).
