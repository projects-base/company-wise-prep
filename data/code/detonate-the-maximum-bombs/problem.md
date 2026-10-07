You are given a list of bombs. `bombs[i] = [x, y, r]` places bomb `i` at point `(x, y)` with blast radius `r`. When a bomb goes off, it sets off every other bomb whose centre lies **within or on** its blast circle (distance from its centre ≤ `r`). Those bombs then explode with their own radius, and so on.

You may set off exactly **one** bomb by hand. Return the largest number of bombs that can end up exploding (including the first one).

Note that reach is one-directional: a big bomb may reach a small bomb that cannot reach back.

**Example 1**
Input: bombs = [[2,1,3],[6,1,4]]
Output: 2
Why: the bombs are 4 apart. Bomb 0 (radius 3) cannot reach bomb 1, but bomb 1 (radius 4) reaches bomb 0.

**Example 2**
Input: bombs = [[1,1,5],[10,10,5]]
Output: 1
Why: neither bomb reaches the other.

**Example 3**
Input: bombs = [[1,2,3],[2,3,1],[3,4,2],[4,5,3],[5,6,4]]
Output: 5
Why: setting off bomb 0 chains through all the others.

**Constraints**
- 1 ≤ bombs.length ≤ 100
- 1 ≤ x, y, r ≤ 10⁵

**Notes**: squared distances can reach 2 · 10¹⁰, which overflows `int` — compare in `long`.
