**Short answer:** `String` is immutable so it can be shared safely: the string pool can hand the same instance to many callers, it is thread-safe without locks, its hash code can be cached for `HashMap` keys, and security-sensitive values (class names, file paths, URLs) can't change after they are checked. All strings, including the string pool, live on the heap. The pool moved from PermGen to the heap in Java 7.

## Explanation

**Why immutable**
1. **String pool:** literals with the same content share one instance. That only works if no one can change it.
2. **Thread safety:** immutable objects can be shared across threads with no synchronisation.
3. **Hash caching:** `String` caches its `hashCode()` in a field, so repeated map lookups don't recompute it.
4. **Security:** the class loader, file APIs and network APIs take strings. If a string could change after a permission check, the check would be meaningless.

**How it is enforced:** the class is `final`, the internal array is `private final` and never exposed, and every "modifying" method (`concat`, `replace`, `toUpperCase`) returns a new `String`.

**Storage**
- String objects are ordinary heap objects.
- The **string pool** (a hash table inside the JVM, the "StringTable") holds references to interned strings. Since Java 7 those strings are on the heap and can be garbage-collected when unreferenced.
- Literals are interned automatically when first resolved. `new String("x")` creates a separate heap object; `intern()` returns the pooled one.
- **Compact strings (Java 9+):** the internal array is a `byte[]` plus a `coder` flag, using one byte per character when all characters are Latin-1, which roughly halves memory for ASCII text.
- Compile-time constant expressions like `"a" + "b"` are folded into one literal by `javac`.

## Example

```java
String a = "hello";
String b = "hello";
String c = new String("hello");
String d = c.intern();

System.out.println(a == b);       // true: same pooled instance
System.out.println(a == c);       // false: c is a new heap object
System.out.println(a == d);       // true: intern() returns the pooled one
System.out.println(a.equals(c));  // true: always compare content with equals

String s = "x";
s.concat("y");                    // returns a new String; result discarded
System.out.println(s);            // x

String t = "ja";
String u = t + "va";              // built at run time: not pooled
System.out.println(u == "java");  // false
```

## Pitfalls and follow-ups

- **Comparing with `==`:** compares references. Use `equals`.
- **Concatenation in a loop:** each `+` creates a new string; use `StringBuilder`. A single `a + b + c` expression is fine; since Java 9 `javac` compiles it with `invokedynamic` (`StringConcatFactory`).
- **Why use `char[]` for passwords?** You can overwrite a `char[]` after use; a `String` stays in memory until GC'd and you can't clear it.
- **`StringBuilder` vs `StringBuffer`:** same API; `StringBuffer` is synchronized and almost never needed.
- **Too many duplicate strings?** G1 has string deduplication (`-XX:+UseStringDeduplication`), which shares the backing arrays of equal strings.
- **Is immutability absolute?** Through normal APIs, yes. Reflection hacks are blocked by strong encapsulation of JDK internals since Java 16/17.

Related: [A2 · JVM memory areas](../academy/lessons/A2.md), [A7 · Java versions](../academy/lessons/A7.md).
