A knight starts in the top-left room of an `m × n` dungeon and must reach the princess in the bottom-right room, moving only **right** or **down**. Each room holds a number: a negative value is damage (the knight loses that much health), a positive value is a potion (the knight gains that much), and `0` is an empty room. The first and last rooms count too.

The knight dies the moment his health drops to `0` or below. Return the **minimum starting health** that lets him reach the princess alive along some path.

**Example 1**
Input: dungeon = [[-2,-3,3],[-5,-10,1],[10,30,-5]]
Output: 7
Why: going right, right, down, down, his health goes 7 → 5 → 2 → 5 → 6 → 1, never reaching 0.

**Example 2**
Input: dungeon = [[0]]
Output: 1
Why: he needs at least 1 health just to be alive.

**Example 3**
Input: dungeon = [[100],[-150]]
Output: 51
Why: after the potion he has h + 100, and must still have at least 1 after losing 150.

**Constraints**
- 1 ≤ m, n ≤ 200
- −1000 ≤ dungeon[i][j] ≤ 1000

**Notes**: a forward DP that tracks only "health so far" cannot decide between paths; think about the requirement from the princess backwards.
