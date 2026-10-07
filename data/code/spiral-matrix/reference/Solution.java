import java.util.*;

class Solution {
    public List<Integer> spiralOrder(int[][] matrix) {
        return new SpiralTraversal().traverse(new Matrix(matrix));
    }
}

/** Read-only view of the grid. */
class Matrix {
    private final int[][] cells;

    Matrix(int[][] cells) {
        this.cells = cells;
    }

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
            if (top < bottom && left < right) {
                for (int c = right - 1; c >= left; c--) out.add(m.get(bottom, c));
                for (int r = bottom - 1; r > top; r--) out.add(m.get(r, left));
            }
            top++;
            bottom--;
            left++;
            right--;
        }
        return out;
    }
}
