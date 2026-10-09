**Short answer:** These fix-the-bug tasks plant a few classic C++ mistakes: swapping pointers vs swapping the strings they point to, comparing `char*` with `<` instead of `strcmp`, off-by-one in buffers (forgetting the `'\0'`), `new[]` freed with `delete`, and memory never freed on some path. Find them by reading ownership: for every `new`, who deletes it, and on every path? Then confirm with AddressSanitizer or Valgrind. Where you may change more than one line, replace raw ownership with RAII (`std::string`, `std::vector`, `std::unique_ptr`).

## Explanation

A checklist to run through the allowed region:

1. **Comparison:** `a < b` on two `const char*` compares addresses. Use `std::strcmp(a, b) < 0`.
2. **Swap level:** to sort an array of `char*`, swap the pointers (`std::swap(arr[i], arr[j])`). A buggy version swaps local copies (pass by value) or swaps only the first character.
3. **Allocation size:** `new char[strlen(s)]` is one short; it needs `strlen(s) + 1` for the terminator.
4. **Matching forms:** `new` -> `delete`, `new[]` -> `delete[]`, `malloc` -> `free`. Mixing is undefined behaviour.
5. **Leaks on every path:** early `return`, `continue`, or an exception between `new` and `delete`.
6. **Overwritten pointers:** `p = new X;` when `p` already owned something leaks the old object.
7. **Containers of raw pointers:** `vector<Foo*>` going out of scope frees the vector, not the `Foo`s.
8. **Base class without a virtual destructor:** `delete base` leaks the derived part.
9. **Double free / use after free:** shallow copy of a class owning a raw pointer (rule of three violated).

## Example

```cpp
// Buggy
void sortNames(char** names, int n) {
    for (int i = 0; i < n; ++i)
        for (int j = i + 1; j < n; ++j)
            if (names[j] < names[i]) {          // compares addresses
                char* t = names[i]; names[i] = names[j]; names[j] = t;
            }
}
char* dup(const char* s) {
    char* d = new char[strlen(s)];             // no room for '\0'
    strcpy(d, s);
    return d;
}
// caller: char* c = dup("abc"); ... return;   // never freed

// Fixed
if (std::strcmp(names[j], names[i]) < 0) std::swap(names[i], names[j]);
char* d = new char[std::strlen(s) + 1];
// caller: delete[] c;  or better: std::string c = "abc";
```

Tools:

```text
g++ -g -fsanitize=address,undefined app.cpp && ./a.out   # leaks + overflows
valgrind --leak-check=full ./app                          # "definitely lost" blocks
```

ASan includes LeakSanitizer on Linux and reports the allocation stack of each leaked block.

## Pitfalls and follow-ups

- **"Edit only allowed parts":** you may not be able to switch to `std::string`; fix the minimal lines and add the matching `delete[]` in the allowed block.
- **Definitely vs still reachable (Valgrind)?** Definitely lost = no pointer to it remains. Still reachable = a pointer exists at exit (often globals), usually not a real leak.
- **Prevent instead of debug:** RAII and the rule of zero. If a class owns nothing raw, the compiler-generated copy, move and destructor are correct.
- **Java comparison:** Java has no manual free, so "leaks" are objects kept reachable (static maps, listeners). The C++ equivalent of that also exists with `shared_ptr` cycles.

Related: [A5 · Diagnosing GC and memory leaks](../academy/lessons/A5.md) for the Java side.
