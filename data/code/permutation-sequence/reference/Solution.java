import java.util.*;

class Solution {
    public String getPermutation(int n, int k) {
        int[] fact = new int[n + 1];
        fact[0] = 1;
        for (int i = 1; i <= n; i++) fact[i] = fact[i - 1] * i;
        List<Integer> left = new ArrayList<>();
        for (int i = 1; i <= n; i++) left.add(i);
        k--; // 0-based rank
        StringBuilder sb = new StringBuilder();
        for (int i = n; i >= 1; i--) {
            int idx = k / fact[i - 1];
            k %= fact[i - 1];
            sb.append(left.remove(idx));
        }
        return sb.toString();
    }
}
