A phone lock screen has 9 dots in a 3 × 3 grid, numbered

```
1 2 3
4 5 6
7 8 9
```

An unlock pattern is a sequence of distinct dots joined by straight line segments. A pattern is valid when:

- every dot appears at most once, and
- whenever the segment between two consecutive dots passes **through the centre of another dot**, that other dot must already appear earlier in the pattern. (For example 1 → 3 passes through 2, so it is only allowed if 2 was used before. Moves such as 1 → 6 or 2 → 9 pass through no dot centre and are always fine.)

Given `m` and `n`, return how many valid patterns use at least `m` and at most `n` dots. Two patterns are different if their sequences of dots differ (order matters).

**Example 1**
Input: m = 1, n = 1
Output: 9
Why: each single dot is a pattern.

**Example 2**
Input: m = 1, n = 2
Output: 65
Why: 9 single dots plus 56 valid two-dot moves: all 72 ordered pairs minus the 16 that jump over an unused dot — the 4 lines through 5 (1–9, 3–7, 2–8, 4–6) and the 4 jumps along an edge (1–3, 7–9, 1–7, 3–9), each in both directions.

**Constraints**
- 1 ≤ m ≤ n ≤ 9

**Notes**: the jump rule only applies to the dot exactly in the middle of a segment — 1 → 9 passes through 5, 4 → 6 passes through 5, 1 → 7 passes through 4.
