Each cell of an `m x n` grid holds `0` (empty), `1` (a fresh orange) or `2` (a rotten orange). Every minute, each fresh orange that is directly up, down, left or right of a rotten orange becomes rotten. Return the number of minutes until no fresh orange remains. Return `0` if there are no fresh oranges at the start, and `-1` if some fresh orange can never rot.

**Example 1**
Input: grid = [[2,1,1],[1,1,0],[0,1,1]]
Output: 4
Why: the rot spreads from the top-left corner and reaches the bottom-right orange in minute 4.

**Example 2**
Input: grid = [[2,1,1],[0,1,1],[1,0,1]]
Output: -1
Why: the orange in the bottom-left corner has no neighbour that can ever rot it.

**Example 3**
Input: grid = [[0,2]]
Output: 0
Why: there are no fresh oranges.

**Constraints**
- 1 ≤ m, n ≤ 250
- grid[i][j] is 0, 1 or 2

**Notes**: the hidden tests include a 250x250 grid where the rot follows a long winding path for tens of thousands of minutes, so re-scanning the whole grid every minute is far too slow; use a multi-source BFS.
