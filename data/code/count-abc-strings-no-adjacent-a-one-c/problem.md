Count the strings of length `k` built from the letters `a`, `b` and `c` that satisfy both rules:

- no two `a`s are next to each other (the substring `"aa"` never appears), and
- the letter `c` appears **at most once**.

`b` can be used freely. Because the count grows quickly, return it modulo `1 000 000 007`.

**Example 1**
Input: k = 1
Output: 3
Why: "a", "b" and "c".

**Example 2**
Input: k = 2
Output: 7
Why: of the 9 two-letter strings, "aa" has two adjacent a's and "cc" has two c's.

**Example 3**
Input: k = 3
Output: 15

**Constraints**
- 1 ≤ k ≤ 200

**Notes**: enumerating all 3ᵏ strings is hopeless for k = 200. Track a small state while extending the string one letter at a time — whether it currently ends in `a`, and whether a `c` has been used — for an O(k) dynamic programme. Take the modulus at every step.
