import java.util.*;

class Solution {
    public boolean canTransform(String start, String result) {
        int n = start.length();
        if (result.length() != n) return false;
        int i = 0, j = 0;
        // Pair up the pieces in order: same letters, L may only move left, R only right.
        while (true) {
            while (i < n && start.charAt(i) == 'X') i++;
            while (j < n && result.charAt(j) == 'X') j++;
            if (i == n || j == n) return i == n && j == n;
            char a = start.charAt(i), b = result.charAt(j);
            if (a != b) return false;
            if (a == 'L' && j > i) return false;
            if (a == 'R' && j < i) return false;
            i++;
            j++;
        }
    }
}
