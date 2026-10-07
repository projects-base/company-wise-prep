import java.util.*;

class Solution {
    public String[] huffmanCodes(int[] freq) {
        int n = freq.length;
        String[] codes = new String[n];
        if (n == 1) {
            codes[0] = "0";
            return codes;
        }
        // Nodes 0..n-1 are leaves; internal nodes are appended as they are created.
        int[] left = new int[2 * n], right = new int[2 * n];
        long[] weight = new long[2 * n];
        PriorityQueue<Integer> pq = new PriorityQueue<>((a, b) -> Long.compare(weight[a], weight[b]));
        for (int i = 0; i < n; i++) {
            weight[i] = freq[i];
            pq.add(i);
        }
        int next = n;
        while (pq.size() > 1) {
            int a = pq.poll(), b = pq.poll();
            weight[next] = weight[a] + weight[b];
            left[next] = a;
            right[next] = b;
            pq.add(next++);
        }
        // Walk down from the root assigning 0 to left edges and 1 to right edges.
        String[] path = new String[2 * n];
        int root = next - 1;
        path[root] = "";
        for (int v = root; v >= n; v--) { // children always have smaller ids than their parent
            path[left[v]] = path[v] + "0";
            path[right[v]] = path[v] + "1";
        }
        System.arraycopy(path, 0, codes, 0, n);
        return codes;
    }
}
