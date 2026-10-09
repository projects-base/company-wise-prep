**Short answer:** Use a stack. Scan the characters; if the top of the stack is the same letter in the other case, pop it (the pair cancels), otherwise push the character. Removing a pair can make two earlier characters adjacent, and the stack handles that automatically because the new top is exactly the character before the removed pair. A `StringBuilder` works as the stack. O(n).

## Approach

- **Brute force:** scan for a bad pair, delete it, start again. Each scan is O(n) and there can be n/2 deletions: O(n²).
- **Key insight:** this is bracket matching. Each character can only cancel with its current left neighbour among the surviving characters, which is the top of a stack. Cancellations cascade naturally.
- **Same letter, other case:** `a != b && toLowerCase(a) == toLowerCase(b)`, or, for ASCII letters only, `Math.abs(a - b) == 32`.

## Solution

```java
class Solution {
    public String makeGood(String s) {
        StringBuilder st = new StringBuilder();          // used as a stack
        for (char c : s.toCharArray()) {
            int n = st.length();
            char top = n > 0 ? st.charAt(n - 1) : 0;
            if (n > 0 && top != c && Character.toLowerCase(top) == Character.toLowerCase(c)) {
                st.setLength(n - 1);                     // pop: the pair cancels
            } else {
                st.append(c);                            // push
            }
        }
        return st.toString();
    }
}
```

## Complexity

- **Time:** O(n): each character is pushed once and popped at most once.
- **Space:** O(n) for the builder (which is also the output).

## Edge cases

- Same letter, same case (`"aa"`): not a bad pair; keep both.
- Cascading removal (`"abBA"` → `""`).
- Everything cancels: return the empty string.
- Single character: unchanged.

## Follow-up: solve without a stack

Use the input array itself, with a write pointer `w`. The prefix `a[0..w)` holds the survivors. This is still the stack idea, but with O(1) extra space beyond the char array:

```java
class Solution {
    public String makeGood(String s) {
        char[] a = s.toCharArray();
        int w = 0;
        for (int r = 0; r < a.length; r++) {
            if (w > 0 && Math.abs(a[w - 1] - a[r]) == 32) w--;  // 'a' - 'A' == 32
            else a[w++] = a[r];
        }
        return new String(a, 0, w);
    }
}
```

The `== 32` test is safe only because the input is letters: among `A–Z` and `a–z`, a difference of exactly 32 always means the same letter in the other case. If the interviewer wants literally no auxiliary structure, the repeated-scan O(n²) version also qualifies; state the trade-off.

## Follow-up: unit tests as if shipping a feature

Use JUnit 5 with `@ParameterizedTest` and `@CsvSource` (input, expected):

- Examples from the spec: `leEeetcode → leetcode`, `abBAcC → ""`, `s → s`.
- No change: `abc`, `ABC`, `aa`, `AA`.
- Single pair at the start, middle and end: `aAb`, `baAc`, `bcC`.
- Cascades: `abBA → ""`, `aBbcCA → ""`.
- Order independence: `aAa → a`, `AaA → A`.
- Large input: 10⁵ characters of `aA` repeated returns `""` within a time limit (`assertTimeoutPreemptively`), and 10⁵ of `ab` repeated is unchanged.
- Contract tests: null input (decide: throw `NullPointerException` or return `""`, and document it), and non-letters if the spec ever allows them.

Practise it in the app: Run / Submit on this page.
