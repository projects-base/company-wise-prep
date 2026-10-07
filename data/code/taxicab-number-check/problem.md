Return `true` if the positive integer `n` can be written as the sum of two positive cubes, `n = a³ + b³` with `1 ≤ a ≤ b`, in **exactly two** different ways, and `false` otherwise. Pairs are unordered, so `1³ + 12³` and `12³ + 1³` count as the same way. Numbers with one way, or with three or more ways, are not accepted.

**Example 1**
Input: n = 1729
Output: true
Why: 1729 = 1³ + 12³ = 9³ + 10³.

**Example 2**
Input: n = 2
Output: false
Why: 2 = 1³ + 1³ is the only way.

**Example 3**
Input: n = 87539319
Output: false
Why: it has three ways (167³ + 436³, 228³ + 423³, 255³ + 414³).

**Constraints**
- 1 ≤ n ≤ 2·10¹⁸

**Notes**: in the interview this was a "fix the bug" task on a given C++ function; here you write the check yourself. Trying every pair (a, b) is far too slow for large `n`. Watch out for floating-point cube roots and for `long` overflow.
