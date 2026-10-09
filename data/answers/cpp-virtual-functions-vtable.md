**Short answer:** A class with at least one virtual function gets a *vtable*: a static, per-class array of function pointers, one slot per virtual function. Every object of such a class carries a hidden *vptr* (usually the first 8 bytes) pointing to its class's vtable. A virtual call loads the vptr, loads the function pointer from a fixed slot, and makes an indirect call. A derived class's vtable copies the base layout and overwrites the slots it overrides. None of this is in the standard; it is how GCC, Clang (Itanium ABI) and MSVC implement it.

## Explanation

**Layout.** For `struct Base { virtual void f(); virtual void g(); int x; };` an object is `[vptr][x]`. `Base`'s vtable is `[&Base::f, &Base::g]`. For `struct Derived : Base { void g() override; };` the vtable is `[&Base::f, &Derived::g]`. The Itanium ABI vtable also holds an offset-to-top and an RTTI pointer (used by `dynamic_cast` and `typeid`) just before the function slots.

**Construction.** Each constructor sets the vptr to *its own* class's vtable before running its body. So during `Base`'s constructor the object is a `Base`; a virtual call there calls `Base::f`, not the override. The same applies in reverse in destructors.

**The call.** `b->g()` compiles to roughly:

```text
mov rax, [rdi]        ; load vptr from object
call [rax + 8]        ; load slot 1, indirect call
```

**Multiple inheritance.** An object with two polymorphic bases has two vptrs, one per base subobject. Converting `Derived*` to the second base adjusts the pointer, and the vtable for that base uses *thunks* that adjust `this` back before jumping to the override. Virtual inheritance adds virtual base offsets to the vtable.

## Example

```cpp
struct Shape {
    virtual ~Shape() = default;
    virtual double area() const = 0;   // pure virtual: slot exists, no body
};
struct Sq final : Shape {
    double s;
    explicit Sq(double s) : s(s) {}
    double area() const override { return s * s; }
};

double total(const std::vector<std::unique_ptr<Shape>>& v) {
    double t = 0;
    for (auto& p : v) t += p->area();   // indirect call through vtable
    return t;
}
// sizeof(Sq) == 16 on x86-64: vptr (8) + double (8)
```

## Pitfalls and follow-ups

- **Cost of a virtual call vs a direct call?** Two dependent loads plus an indirect branch. If the branch target predictor guesses right, that is a few cycles. The real costs are: the call **cannot be inlined**, a mispredicted target costs a pipeline flush, and the extra loads may miss cache when objects are scattered.
- **Devirtualisation:** the compiler turns a virtual call into a direct (and inlinable) one when it can prove the dynamic type: the object is a local of known type, the class or method is `final`, or link-time / whole-program optimisation sees only one implementation. Profile-guided compilers can also add a speculative "if type is X, call X::f directly" check.
- **Alternatives in low-latency code:** CRTP (static polymorphism), `std::variant` + `std::visit`, or templates, which resolve at compile time and inline.
- **Why must base destructors be virtual?** `delete basePtr` must dispatch to the derived destructor through the vtable.
- **Can constructors be virtual?** No; the vptr is not set until construction starts. Use a virtual `clone()` instead.
- **Java comparison:** every non-final, non-private, non-static Java method is virtual; HotSpot devirtualises aggressively using class-hierarchy analysis and inline caches.
