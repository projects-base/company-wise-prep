**Short answer:** Track the set of all cells the robot might be in. Pick any of them that has not escaped yet, and walk it to the exit along a shortest path. Apply each move to every position in the set. Moves are deterministic, so positions can merge but never split. Each round removes at least the chosen position, so after fewer than (number of open cells) rounds the set is empty. First check that every open cell can reach the exit at all. If one cannot, return `""`.

## Picture it

Example 1: `grid = ["E.", ".."]`. Cells are numbered `r * cols + c`; BFS from the exit gives the distances.

```text
cell ids     dist to E
 0(E) 1       0  1
 2    3       1  2
```

The set starts as every open cell except the exit, `{1, 2, 3}`. A small-integer `HashSet` iterates in ascending order, so the candidate is the smallest id. The first direction (U, D, L, R order) that lowers `dist` by one is chosen.

| Round | Candidate | Move | 1 goes to | 2 goes to | 3 goes to | Set after | Output |
|---|---|---|---|---|---|---|---|
| 1 | 1 (dist 1) | L | 0 = E, escaped | 2 (blocked) | 2 | {2} | L |
| 2 | 2 (dist 1) | U | – | 0 = E, escaped | – | {} | LU |

In round 1, positions 2 and 3 **merge** into 2. They can never split again, because a move sends one cell to exactly one cell.

**The picture in one sentence:** drive any one possible position home by a shortest path while everything else moves along, so the set of positions only shrinks or merges until it is empty.

## Approach

- **Feasibility.** Moves are reversible: if you can step from a to b, you can step from b to a. So a BFS from the exit gives `dist[cell]`, the shortest distance from every cell to the exit. If any open cell has no distance, no sequence can exist, and the answer is `""`.
- **Brute force.** BFS over *sets* of positions, looking for the shortest synchronising word. There are 2^cells possible sets, which is hopeless.
- **Key insight (greedy merging).** The question does not ask for the shortest sequence. Take one candidate position and drive it to the exit with shortest-path moves. Everything else moves along with it, either advancing, bumping into a wall, or escaping early. When the candidate exits, the set is strictly smaller. Positions never split, because each move maps one cell to exactly one cell. Repeat until the set is empty.
- **Length bound.** At most 399 rounds, each at most 399 moves (a shortest path in a 20×20 grid), so fewer than 160,000 moves. That is under the 200,000 limit.

## Solution

```java
import java.util.*;

class Solution {
    private static final int[] DR = {-1, 1, 0, 0}, DC = {0, 0, -1, 1};
    private static final char[] NAME = {'U', 'D', 'L', 'R'};

    public String escapeSequence(String[] grid) {
        int rows = grid.length, cols = grid[0].length();
        int exit = -1;
        List<Integer> open = new ArrayList<>();
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++) {
                char ch = grid[r].charAt(c);
                if (ch == 'E') exit = r * cols + c;
                if (ch != '#') open.add(r * cols + c);
            }
        // Distance to the exit from every open cell (moves are reversible, so BFS from the exit).
        int[] dist = new int[rows * cols];
        Arrays.fill(dist, -1);
        ArrayDeque<Integer> q = new ArrayDeque<>();
        dist[exit] = 0;
        q.add(exit);
        while (!q.isEmpty()) {
            int u = q.poll();
            for (int d = 0; d < 4; d++) {
                int v = step(grid, u, d, cols);
                if (v != u && dist[v] < 0) { dist[v] = dist[u] + 1; q.add(v); }
            }
        }
        for (int cell : open) if (dist[cell] < 0) return "";

        StringBuilder out = new StringBuilder();
        Set<Integer> pos = new HashSet<>(open);
        pos.remove(exit);
        while (!pos.isEmpty()) {
            int cur = pos.iterator().next();          // any position not yet escaped
            while (cur != exit) {
                int dir = -1;
                for (int d = 0; d < 4; d++) {
                    int v = step(grid, cur, d, cols);
                    if (v != cur && dist[v] == dist[cur] - 1) { dir = d; break; }
                }
                out.append(NAME[dir]);
                Set<Integer> next = new HashSet<>();
                for (int p : pos) {
                    int v = step(grid, p, dir, cols);
                    if (v != exit) next.add(v);           // reaching E means escaped
                }
                pos = next;
                cur = step(grid, cur, dir, cols);
            }
        }
        return out.toString();
    }

    /** Cell reached from u moving in direction d (u itself if blocked). */
    private int step(String[] grid, int u, int d, int cols) {
        int r = u / cols + DR[d], c = u % cols + DC[d];
        if (r < 0 || r >= grid.length || c < 0 || c >= cols || grid[r].charAt(c) == '#') return u;
        return r * cols + c;
    }
}
```

## Complexity

Let C be the number of open cells (C ≤ 400).

- **Time:** O(C) rounds × O(C) moves × O(C) positions per move = O(C³), about 6.4·10⁷ cheap steps in the worst case.
- **Space:** O(C) for `dist` and the position set, plus the output.
- **Output length:** O(C²).

## Edge cases

- The only open cell is the exit: the answer is the empty sequence, which is valid.
- An open cell walled off from the exit: return `""`.
- A grid of one row or one column: moves perpendicular to it are no-ops, but the BFS never chooses them.
- Robots that reach `E` early must be removed from the set. Otherwise they would move on past the exit.

## Variations

- **Shortest universal sequence:** BFS over subsets. Exponential in general; this is the synchronising-word problem for automata, where finding the shortest one is NP-hard.
- **Known starting cell:** an ordinary BFS shortest path.
- **Robots that keep moving after the exit:** the exit becomes an ordinary cell, and you need every position to be *at* E at the end. Use the same merging idea, but every position must end on E together.

Practise it in the app: Run / Submit on this page.
