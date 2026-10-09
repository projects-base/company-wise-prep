**Short answer:** Each thread has its own stack of frames; a frame holds the method's local variables and operand stack. The heap is shared by all threads and holds objects and arrays, and the garbage collector manages it. For a local `int a = 10;`, the value `10` sits directly in a local-variable slot of the current frame; nothing goes on the heap. A primitive *field* lives inside its object on the heap, and an object local is a reference on the stack pointing to the object on the heap.

## Explanation

**Stack (per thread)**
- One frame per method call: local variable array, operand stack, and a reference to the class's constant pool.
- Allocation and release are just pushing and popping frames, so they're very cheap.
- Size is set by `-Xss`; too deep a recursion throws `StackOverflowError`.
- Only that thread can see its locals, so locals are thread-safe by nature.

**Heap (shared)**
- All objects and arrays, including their primitive fields.
- Managed by the GC (G1 is the default collector for most server configurations in Java 21).
- Sized by `-Xms` / `-Xmx`; running out throws `OutOfMemoryError`.
- Shared between threads, so visibility and races matter here.

Class metadata lives in **Metaspace** (native memory, since Java 8). In modern HotSpot, static fields are stored with the class's `java.lang.Class` object on the heap.

**What happens on `int a = 10;` inside a method:**

1. At compile time, `javac` emits `bipush 10` (push the constant onto the operand stack) and `istore_1` (pop it into local slot 1).
2. At run time, the interpreter executes those on the current frame.
3. Once the JIT compiles the method, the value will usually live in a CPU register, or be folded away entirely.

`bipush` is used because 10 fits in a byte; `-1` to `5` use `iconst_*`, and larger values use `sipush` or `ldc`.

## Example

```java
class Point { int x; int y; }

void demo() {
    int a = 10;              // stack: slot holds the value 10
    Point p = new Point();   // stack: slot holds a reference; heap: the Point object
    p.x = a;                 // the value 10 is copied into the object's field on the heap
    int[] arr = {1, 2};      // the array is an object: on the heap, reference on the stack
}
```

```text
javap -c output for "int a = 10;"
   0: bipush        10
   2: istore_1
```

## Pitfalls and follow-ups

- **"Primitives are always on the stack"** is wrong. Primitive fields and array elements are on the heap.
- **"Objects are always on the heap"** is true in the language model, but the JIT's escape analysis can apply *scalar replacement*: if an object never escapes a method, its fields can be kept in registers and no allocation happens.
- **Is Java pass-by-reference?** No, always pass-by-value. For objects, the value passed is the reference.
- **Where do `Integer` objects live?** On the heap; `Integer.valueOf` caches -128 to 127.
- **Thread safety:** a local variable is safe, but the object it points to is not if the reference is shared.
- **Virtual threads (Java 21):** their stack frames are stored on the heap when they are unmounted, which is why millions of them are affordable.

Related: [A2 · JVM memory areas](../academy/lessons/A2.md), [A1 · How Java runs](../academy/lessons/A1.md).
