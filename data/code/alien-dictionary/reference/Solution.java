import java.util.*;

class Solution {
    public String alienOrder(String[] words) {
        boolean[] present = new boolean[26];
        for (String w : words) for (char c : w.toCharArray()) present[c - 'a'] = true;
        boolean[][] edge = new boolean[26][26];
        int[] indeg = new int[26];
        for (int i = 0; i + 1 < words.length; i++) {
            String a = words[i], b = words[i + 1];
            int j = 0;
            while (j < a.length() && j < b.length() && a.charAt(j) == b.charAt(j)) j++;
            if (j == a.length() || j == b.length()) {
                if (a.length() > b.length()) return ""; // longer word before its prefix
                continue;
            }
            int u = a.charAt(j) - 'a', v = b.charAt(j) - 'a';
            if (!edge[u][v]) {
                edge[u][v] = true;
                indeg[v]++;
            }
        }
        ArrayDeque<Integer> q = new ArrayDeque<>();
        int letters = 0;
        for (int c = 0; c < 26; c++) {
            if (!present[c]) continue;
            letters++;
            if (indeg[c] == 0) q.add(c);
        }
        StringBuilder sb = new StringBuilder();
        while (!q.isEmpty()) {
            int u = q.poll();
            sb.append((char) ('a' + u));
            for (int v = 0; v < 26; v++) if (edge[u][v] && --indeg[v] == 0) q.add(v);
        }
        return sb.length() == letters ? sb.toString() : "";
    }
}
