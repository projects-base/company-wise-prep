**Short answer:** Work backwards from the princess. Let `need[i][j]` be the minimum health the knight must have when he enters room `(i, j)` and still finishes alive. Then `need[i][j] = max(1, min(need of right, need of down) − dungeon[i][j])`. In the princess's room, the "next" requirement is 1. The answer is `need[0][0]`. This is O(m·n) time and O(n) space with a rolling row.

## Approach

- **Brute force:** try every right/down path, simulate the health along it, and keep the smallest requirement. There are C(m+n−2, m−1) paths, so this is exponential.
- **Why forward DP fails:** going forward, each cell has two numbers, "health so far" and "lowest point so far". One path can have more health but a worse low point. You cannot tell which one matters until you see the rest of the path, so there is no single value to minimise.
- **Key insight:** from the end, there is one clean value. If you know the health you need when you enter the next room, the health you need for this room is `next − dungeon[i][j]`. It can never be less than 1, because you must be alive in this room, and a potion bigger than you need does not help. Take the cheaper of right and down.
- **Optimal:** fill a table from the bottom-right to the top-left. Put an `Integer.MAX_VALUE` sentinel just outside the grid, so `min` ignores moves that leave the grid.

## Solution

```java
import java.util.*;

class Solution {
    // need[j] = minimum health on entering room (i, j) to finish alive. Fill from the bottom-right.
    public int calculateMinimumHP(int[][] dungeon) {
        int m = dungeon.length, n = dungeon[0].length;
        int[] need = new int[n + 1];                  // row i + 1; starts as "outside the grid"
        Arrays.fill(need, Integer.MAX_VALUE);
        for (int i = m - 1; i >= 0; i--) {
            int[] row = new int[n + 1];
            row[n] = Integer.MAX_VALUE;               // right of the grid
            for (int j = n - 1; j >= 0; j--) {
                int after = (i == m - 1 && j == n - 1) ? 1 : Math.min(need[j], row[j + 1]);
                row[j] = Math.max(1, after - dungeon[i][j]);
            }
            need = row;
        }
        return need[0];
    }
}
```

`after − dungeon[i][j]` cannot overflow. Every cell except the princess's has at least one neighbour inside the grid, so `min` never returns the sentinel there. Real requirements stay below about 4·10⁵, because there are at most 399 rooms on a path and each does at most 1000 damage.

## Complexity

- **Time:** O(m·n). Each cell is computed once from two neighbours.
- **Space:** O(n) for two rows. A full `(m+1)×(n+1)` table also fits easily at 200×200.

## Edge cases

- A single room: `[[0]]` gives 1, `[[5]]` gives 1 (a potion cannot push the requirement below 1), and `[[-5]]` gives 6.
- A potion followed by damage: `[[100],[-150]]` gives 51. The `max(1, …)` clamp stops the large potion from making the requirement negative.
- A single row or column: only one neighbour is inside the grid, and the sentinel handles the other.

## Variations

- **Maximise the final health instead:** now there is one value to maximise, so a forward DP works.
- **Moves in all four directions:** the grid is no longer a DAG. Binary search on the starting health and check each guess with a BFS or Dijkstra-style search.

Related: [C4 · The optimisation playbook](../academy/lessons/C4.md).

Practise it in the app: Run / Submit on this page.
