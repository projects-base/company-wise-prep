**Short answer:** Replace every loop with recursion. Convert the string to a `char[]`. One recursive method scans forward to find the end of the current word (a space or the end of the array); when it finds it, it calls a second recursive method that reverses that word by swapping its two ends and recursing inward, then continues from the next word. `"This is a Sentence"` becomes `"sihT si a ecnetneS"`. O(n) time, but O(n) recursion depth, which is the price of "no loops".

## Approach

**With loops (the normal answer).** Walk the array, and at every space reverse the range `[start, i − 1]` with two pointers. O(n) time, O(n) for the `char[]`.

**Without loops.** A loop is just recursion with the loop variables as parameters:
- `scan(c, start, i)`: `start` is where the current word began, `i` is the current position. If `c[i]` is a space or `i` is past the end, the word is `[start, i − 1]`: reverse it, then continue with `scan(c, i + 1, i + 1)`. Otherwise, call `scan(c, start, i + 1)`.
- `reverse(c, lo, hi)`: swap `c[lo]` and `c[hi]`, then `reverse(c, lo + 1, hi − 1)` until `lo >= hi`.

**Trace on `"ab cd"`:**

| Call | Found | Action |
|---|---|---|
| `scan(0, 0)`, `scan(0, 1)` | letters | move right |
| `scan(0, 2)` | space | `reverse(0, 1)` gives `"ba cd"`, then `scan(3, 3)` |
| `scan(3, 3)`, `scan(3, 4)` | letters | move right |
| `scan(3, 5)` | end of array | `reverse(3, 4)` gives `"ba dc"`, stop |

## Solution

```java
class Solution {
    public String reverseWords(String s) {
        char[] c = s.toCharArray();
        scan(c, 0, 0);
        return new String(c);
    }

    // start: first index of the current word, i: current position.
    private void scan(char[] c, int start, int i) {
        if (i == c.length || c[i] == ' ') {
            reverse(c, start, i - 1);
            if (i < c.length) scan(c, i + 1, i + 1);   // next word
            return;
        }
        scan(c, start, i + 1);
    }

    private void reverse(char[] c, int lo, int hi) {
        if (lo >= hi) return;
        char t = c[lo];
        c[lo] = c[hi];
        c[hi] = t;
        reverse(c, lo + 1, hi - 1);
    }
}
```

**Streams version.** If the interviewer accepts "no explicit loop" rather than "no iteration at all":

```java
import java.util.Arrays;
import java.util.stream.Collectors;

String out = Arrays.stream(s.split(" ", -1))
        .map(w -> new StringBuilder(w).reverse().toString())
        .collect(Collectors.joining(" "));
```

`split(" ", -1)` keeps empty strings, so repeated and trailing spaces are preserved. Say clearly that streams still iterate internally; the recursive version is the honest "no loop" answer.

## Complexity

- **Time:** O(n). `scan` visits each character once, and `reverse` swaps each character at most once.
- **Space:** O(n) for the `char[]` (Java strings are immutable) plus O(n) recursion depth, since `scan` makes one call per character. Java has no tail-call elimination, so a string of a few hundred thousand characters can throw `StackOverflowError`.

## Edge cases

- Empty string: `scan` immediately "reverses" the empty range `[0, −1]` and returns `""`.
- Single word, no spaces: the whole array is reversed once.
- Multiple or leading spaces: they produce empty words, which `reverse` skips because `lo >= hi`. Spacing is preserved.
- One-letter words such as `"a"`: unchanged.

## Variations

- **Reverse the order of the words** (`"the sky"` → `"sky the"`): reverse the whole array, then reverse each word.
- **Reverse a whole string recursively:** just `reverse(c, 0, n − 1)`.
- **Reduce stack depth:** recursing per word instead of per character is still O(n) worst case for one long word; only a loop truly fixes it.

See [H1 · Java idioms for coding interviews](../academy/lessons/H1.md) and [C2 · Where memory goes in Java solutions](../academy/lessons/C2.md) for the recursion-stack cost.
