**Short answer:** If you `delete` a derived object through a pointer to its base class and the base destructor is not virtual, the behaviour is undefined. In practice only the base destructor runs: the derived part's members are never destroyed, so its resources leak, and the deallocation may use the wrong size. Declaring `virtual ~Base()` makes `delete` dispatch through the vtable to `~Derived()`, which then runs the base destructor automatically. Rule: a class meant to be used polymorphically gets a public virtual destructor; a base not meant for that gets a protected non-virtual one.

## Explanation

**What happens without it.** `delete b;` where `b` is `Base*` compiles to "call `Base::~Base` on `b`, then free". The compiler uses the static type because the destructor is not virtual. `Derived`'s destructor, and the destructors of its members (a `std::vector`, a file handle), never run. The standard says this is undefined behaviour, so worse things than a leak are allowed (wrong size passed to sized `operator delete`, or wrong pointer freed with multiple inheritance, where the `Base` subobject is not at the start of the allocation).

**What happens with it.** The destructor gets a vtable slot (two in the Itanium ABI: a "complete object" destructor and a "deleting" destructor that also frees memory). `delete b` calls the deleting destructor through the vtable, which runs `~Derived`, then member destructors, then `~Base`, then frees with the correct pointer and size.

**Order of destruction** is the reverse of construction: derived body, derived members (reverse declaration order), base classes (reverse order).

**Cost.** A vptr per object (8 bytes) if the class had no virtual functions yet, and an indirect call on destruction. If the class already has any virtual function, the extra cost is negligible, so just add the virtual destructor.

## Example

```cpp
struct Base {
    ~Base() { std::puts("~Base"); }          // not virtual: bug
};
struct Derived : Base {
    std::vector<int> data = std::vector<int>(1'000'000);
    ~Derived() { std::puts("~Derived"); }
};

Base* b = new Derived;
delete b;                     // UB; typically prints only "~Base", leaks the vector

struct SafeBase {
    virtual ~SafeBase() = default;           // fix
    virtual void run() = 0;
};

// Interface not meant for deletion through base: protected, non-virtual
class Listener {
protected:
    ~Listener() = default;    // `delete listenerPtr` will not compile
public:
    virtual void onEvent() = 0;
};
```

`std::unique_ptr<Base>` calls `delete` on a `Base*`, so it has the same bug. `std::shared_ptr<Base>` created with `std::make_shared<Derived>()` captures `Derived`'s deleter and happens to destroy correctly, but do not rely on that.

## Pitfalls and follow-ups

- **Should every class have a virtual destructor?** No. Value types (`Point`, `Money`) should not pay for a vptr; mark them `final` if they are not designed for inheritance.
- **Pure virtual destructor?** `virtual ~Base() = 0;` makes the class abstract, but you must still provide a definition (`Base::~Base() {}`), because derived destructors call it.
- **Virtual calls inside a destructor** dispatch to the current class, not the derived one, because the derived part is already gone.
- **Inheriting from STL containers** (`class MyVec : public std::vector<int>`) is risky for this reason: no virtual destructor.
- **Compiler warning:** `-Wnon-virtual-dtor` / `-Wdelete-non-virtual-dtor` catches it.
- **Java comparison:** Java has no destructors; cleanup is `try`-with-resources / `AutoCloseable`, and `close()` is virtual like every Java instance method.
