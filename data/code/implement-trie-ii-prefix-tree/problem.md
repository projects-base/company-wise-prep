Build a dictionary as a trie (prefix tree), from scratch, that can hold the same word several times. Implement class `Trie`:

- `Trie()` creates an empty dictionary.
- `void insert(String word)` adds one occurrence of `word`.
- `int countWordsEqualTo(String word)` returns how many occurrences of exactly `word` are stored.
- `int countWordsStartingWith(String prefix)` returns how many stored occurrences (counting repeats) start with `prefix`.
- `void erase(String word)` removes one occurrence of `word`. It is only called when `word` is present at least once.

**Input format**: two lines — the list of operation names, then the list of argument lists (LeetCode's design format). The checker prints the list of return values, with `null` for the constructor and for `void` methods.

**Example 1**
Input:
["Trie","insert","insert","countWordsEqualTo","countWordsStartingWith","erase","countWordsEqualTo","countWordsStartingWith","erase","countWordsStartingWith"]
[[],["apple"],["apple"],["apple"],["app"],["apple"],["apple"],["app"],["apple"],["app"]]
Output: [null,null,null,2,2,null,1,1,null,0]

**Example 2**
Input:
["Trie","insert","insert","countWordsStartingWith","erase","countWordsEqualTo","countWordsStartingWith","countWordsEqualTo"]
[[],["app"],["apple"],["app"],["app"],["app"],["app"],["apple"]]
Output: [null,null,null,2,null,0,1,1]
Why: "app" is both a word and a prefix of "apple"; erasing the word "app" must leave "apple" intact.

**Constraints**
- 1 ≤ word.length, prefix.length ≤ 2000; the tests use lowercase English letters
- at most 3 · 10⁴ calls in total

**Notes**: the interview framed this as a multilingual dictionary — think about how your node's children should be stored if words could use any alphabet (a map instead of a fixed 26-slot array), and about removing a word without leaving the counts on its path wrong. What test cases would you write?
