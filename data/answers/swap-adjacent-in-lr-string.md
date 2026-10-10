**Short answer:** Pieces never pass each other, so the sequence of pieces with the `X`s removed must be identical in both strings. Then match the pieces in order: the i-th piece in `start` becomes the i-th piece in `result`. An `L` may only move left, so its target index must be `≤` its start index; an `R` may only move right, so its target index must be `≥`. Two pointers check this in O(n) time and O(1) space.

## Picture it

`start = "RXXLRXRXL"`, `result = "XRLXXRRLX"` (answer true). Skip the `X`s and pair the pieces in order:

```text
index   0 1 2 3 4 5 6 7 8
start   R X X L R X R X L
result  X R L X X R R L X
```

| pair | piece | i (start) | j (result) | rule | ok? |
|---|---|---|---|---|---|
| 1 | R | 0 | 1 | R needs j ≥ i | yes (moves right 1) |
| 2 | L | 3 | 2 | L needs j ≤ i | yes (moves left 1) |
| 3 | R | 4 | 5 | j ≥ i | yes |
| 4 | R | 6 | 6 | j ≥ i | yes (stays) |
| 5 | L | 8 | 7 | j ≤ i | yes |
| end | | 9 | 9 | both pointers at n | true |

Counter-example: `start = "XL"`, `result = "LX"` is fine (L moves left), but `start = "LX"`, `result = "XL"` pairs L at i = 0 with j = 1, an L moving right, so false.

**The picture in one sentence:** pieces never cross, so pair the k-th piece of each string and check only that `L`s go left and `R`s go right.

## Approach

- **Brute force.** BFS over string states applying the two moves. Exponential state space.
- **Key insight 1 (invariant).** A move swaps a piece with an empty cell, never two pieces. So the relative order of the `L`/`R` pieces is fixed. If `start` without `X` differs from `result` without `X`, the answer is false.
- **Key insight 2 (direction).** Since order is fixed, the k-th piece of `start` must end up as the k-th piece of `result`. An `L` that has to move right, or an `R` that has to move left, is impossible.
- **Sufficiency.** If both conditions hold, the moves can always be scheduled: process `L`s from left to right and `R`s from right to left; each piece's path is clear because the pieces it would have to pass do not exist (order is preserved) and the pieces ahead of it have already moved out of the way in the same direction.
- **Two pointers** check both conditions in one pass, skipping `X`s.

## Solution

```java
class Solution {
    public boolean canTransform(String start, String result) {
        int n = start.length();
        if (result.length() != n) return false;
        int i = 0, j = 0;
        // Pair up the pieces in order: same letters, L may only move left, R only right.
        while (true) {
            while (i < n && start.charAt(i) == 'X') i++;
            while (j < n && result.charAt(j) == 'X') j++;
            if (i == n || j == n) return i == n && j == n; // same number of pieces
            char a = start.charAt(i), b = result.charAt(j);
            if (a != b) return false;
            if (a == 'L' && j > i) return false;
            if (a == 'R' && j < i) return false;
            i++;
            j++;
        }
    }
}
```

## Complexity

- **Time:** O(n). Each pointer moves forward only.
- **Space:** O(1).

## Edge cases

- Different number of pieces ("X" vs "L"): one pointer hits the end first, false.
- Same order but an `L` would have to move right: false.
- Strings with no pieces at all: true.
- Identical strings: true.

## Variations

- **Move Pieces to Obtain a String (LC 2337):** identical, with `_` instead of `X`.
- **Minimum number of moves:** sum of `|i - j|` over the matched pairs, since each move shifts one piece by one cell.
- **Pieces that can jump each other:** the order invariant is gone; only counts and direction constraints per piece type remain, which needs a different matching.

Practise it in the app: Run / Submit on this page.
