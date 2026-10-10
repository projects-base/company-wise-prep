**Short answer:** Keep four boundaries: `top`, `bottom`, `left`, `right`. Walk the top row, the right column, then (only if a row and a column are both still left) the bottom row backwards and the left column upwards; then shrink all four boundaries and repeat. For the LLD framing, put this in a `SpiralTraversal` that implements a `TraversalStrategy` interface over a read-only `Matrix`, so new orders are new classes and no existing code changes.

## Picture it

Example 1: `matrix = [[1,2,3],[4,5,6],[7,8,9]]`.

```text
 1 → 2 → 3
         ↓
 4 → 5   6
 ↑       ↓
 7 ← 8 ← 9
```

| ring | top, bottom, left, right | top row → | right column ↓ | guard `top<bottom && left<right` | bottom row ← | left column ↑ |
|---|---|---|---|---|---|---|
| 1 | 0, 2, 0, 2 | 1, 2, 3 | 6, 9 | true | 8, 7 | 4 |
| 2 | 1, 1, 1, 1 | 5 | (none) | false, skip | - | - |

Output: `[1,2,3,6,9,8,7,4,5]`. Without the guard, ring 2 would walk the single cell `5` again.

**The picture in one sentence:** peel one ring per loop with four shrinking boundaries, and skip the return trip when the last ring is a single row or column.

## Approach

- **Direction + visited array.** Move in the current direction, turn right when you hit the edge or a visited cell. Works, but needs O(m·n) extra space.
- **Boundary layers (better).** Each loop peels one outer ring. The only trap is the last ring when it is a single row or a single column: without the `top < bottom && left < right` guard, you would walk that row or column twice.
- **LLD structure (what this interviewer wanted).**
  - `Matrix`: read-only view (`rows()`, `cols()`, `get(r, c)`). Hides the storage.
  - `TraversalStrategy`: one method, `List<Integer> traverse(Matrix m)`. This is the Strategy pattern.
  - `SpiralTraversal`: the algorithm above.
  - The caller depends on the interface, so adding `ZigzagTraversal` touches nothing that exists (Open/Closed principle).

## Solution

```java
import java.util.*;

class Solution {
    public List<Integer> spiralOrder(int[][] matrix) {
        return new SpiralTraversal().traverse(new Matrix(matrix));
    }
}

/** Read-only view of the grid. */
class Matrix {
    private final int[][] cells;
    Matrix(int[][] cells) { this.cells = cells; }
    int rows() { return cells.length; }
    int cols() { return cells.length == 0 ? 0 : cells[0].length; }
    int get(int r, int c) { return cells[r][c]; }
}

/** A way of walking a matrix; add new orders (zigzag, diagonal) as new implementations. */
interface TraversalStrategy {
    List<Integer> traverse(Matrix m);
}

class SpiralTraversal implements TraversalStrategy {
    @Override
    public List<Integer> traverse(Matrix m) {
        List<Integer> out = new ArrayList<>();
        int top = 0, bottom = m.rows() - 1, left = 0, right = m.cols() - 1;
        while (top <= bottom && left <= right) {
            for (int c = left; c <= right; c++) out.add(m.get(top, c));
            for (int r = top + 1; r <= bottom; r++) out.add(m.get(r, right));
            if (top < bottom && left < right) { // not a single row or column
                for (int c = right - 1; c >= left; c--) out.add(m.get(bottom, c));
                for (int r = bottom - 1; r > top; r--) out.add(m.get(r, left));
            }
            top++; bottom--; left++; right--;
        }
        return out;
    }
}
```

## Complexity

- **Time:** O(m·n), each cell is added once.
- **Space:** O(1) besides the output list.

## Edge cases

- Single row `[[1,2,3]]` and single column `[[1],[2],[3]]`: the guard prevents duplicates.
- 1×1 matrix.
- Non-square matrices where the last ring is a horizontal or vertical strip (3×4, 4×3).
- Empty matrix: `cols()` handles zero rows.

## Follow-ups

- **Future traversals via a factory, Open/Closed.** Add `enum TraversalType { SPIRAL, ZIGZAG, DIAGONAL }` and a `TraversalFactory` that maps each type to a strategy, for example `Map<TraversalType, Supplier<TraversalStrategy>>`. A new order means one new class and one new map entry; callers and existing strategies are untouched. In Spring you would inject all `TraversalStrategy` beans as a `Map<String, TraversalStrategy>` and skip the hand-written factory. Each strategy is stateless, so instances can be shared.
- **How does in-app search work?** Typical layers: an **inverted index** (term → list of document ids, as in Lucene/Elasticsearch, or PostgreSQL full-text search with a GIN index) for full matching; a **trie** or prefix index for type-ahead suggestions, with the top results cached per prefix node; a **cache** (in-memory or Redis) for hot queries; and **personalisation** as a re-ranking step on top of the candidate set (user history, location, popularity). Keep index updates asynchronous so writes stay fast.

See [E4 · Behavioural patterns](../academy/lessons/E4.md) for Strategy, [E2 · Creational patterns](../academy/lessons/E2.md) for factories, and [E1 · SOLID](../academy/lessons/E1.md) for Open/Closed.

Practise it in the app: Run / Submit on this page.
