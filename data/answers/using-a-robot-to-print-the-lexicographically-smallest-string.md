**Short answer:** `t` is a stack. Push characters from `s` one by one. After each push, pop and write while the stack top is less than or equal to the smallest character still left in `s`. If something smaller is still coming, you must wait for it. If nothing smaller is coming, writing the top now is never worse. A suffix-minimum array makes "smallest character still left" an O(1) lookup, so the whole thing is O(n).

## Picture it

`s = "bcab"`. Suffix minima: `minFrom = [a, a, a, b, ∞]`.

| i | Push | Stack after push | minFrom[i+1] | Pops (top ≤ minFrom) | Stack left | Output |
|---|---|---|---|---|---|---|
| 0 | b | b | a | none (b > a) | b | |
| 1 | c | b c | a | none (c > a) | b c | |
| 2 | a | b c a | b | a (a ≤ b), then c > b stops | b c | a |
| 3 | b | b c b | ∞ | b, c, b | – | **abcb** |

At i = 2 the `c` waits because a smaller `b` is still coming.

**The picture in one sentence:** pop the stack top only when nothing smaller is still waiting in `s`, which the suffix-minimum array answers in O(1).

## Approach

- **Brute force.** Explore every push/pop interleaving. That is exponential, the Catalan number of sequences.
- **Key insight (greedy).** At any moment you can write the stack top, or push more of `s`. Writing the top `c` is optimal exactly when no character smaller than `c` remains in `s`. If a smaller character `x` remains, you could push until `x` and write `x` first, which gives a smaller string. If none remains, every character you could ever write later is ≥ `c`, so writing `c` now cannot hurt. Ties (`top == min`) should pop: delaying an equal character can only let larger stack characters get stuck under new ones.
- **Suffix minimum.** `minFrom[i] = min(s[i..n-1])`, with a sentinel above `'z'` at `n`, so the stack is fully drained at the end.

Trace for "bdda": minFrom = [a,a,a,a,∞]. Push b, d, d: each is above 'a', so nothing pops. Push a: the remaining minimum is ∞, so pop a, d, d, b. Result "addb".

## Solution

```java
class Solution {
    public String robotWithString(String s) {
        int n = s.length();
        // minFrom[i] = smallest character in s[i..n-1].
        char[] minFrom = new char[n + 1];
        minFrom[n] = (char) ('z' + 1);
        for (int i = n - 1; i >= 0; i--) minFrom[i] = (char) Math.min(s.charAt(i), minFrom[i + 1]);
        StringBuilder out = new StringBuilder(n);
        char[] stack = new char[n];
        int top = 0;
        for (int i = 0; i < n; i++) {
            stack[top++] = s.charAt(i);
            // Pop while the stack top is no larger than anything still waiting in s.
            while (top > 0 && stack[top - 1] <= minFrom[i + 1]) out.append(stack[--top]);
        }
        while (top > 0) out.append(stack[--top]);
        return out.toString();
    }
}
```

A `char[]` used as a stack avoids boxing. An `ArrayDeque<Character>` works too, but it is slower.

## Complexity

- **Time:** O(n). Each character is pushed once and popped once.
- **Space:** O(n) for the suffix minima and the stack.

## Edge cases

- Already sorted input ("abc"): each character is popped right after it is pushed.
- Reverse sorted input ("cba"): everything is pushed, then popped. The output is "abc".
- All characters equal: the output is the same as the input.
- A single character.

## Variations

- **Remove Duplicate Letters / Smallest Subsequence of Distinct Characters:** a monotonic stack plus "does this character appear later?" counts.
- **Stack-sortable sequences:** the same push/pop model, asking which outputs are reachable.
- **Queue instead of stack:** the output order is fixed, so the problem becomes trivial.

Practise it in the app: Run / Submit on this page.
