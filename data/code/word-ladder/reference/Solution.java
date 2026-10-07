import java.util.*;

class Solution {
    public int ladderLength(String beginWord, String endWord, List<String> wordList) {
        Set<String> unvisited = new HashSet<>(wordList);
        if (!unvisited.contains(endWord)) return 0;
        unvisited.remove(beginWord);
        ArrayDeque<String> q = new ArrayDeque<>();
        q.add(beginWord);
        int depth = 1;
        while (!q.isEmpty()) {
            depth++;
            for (int size = q.size(); size > 0; size--) {
                char[] w = q.poll().toCharArray();
                for (int i = 0; i < w.length; i++) {
                    char orig = w[i];
                    for (char c = 'a'; c <= 'z'; c++) {
                        if (c == orig) continue;
                        w[i] = c;
                        String next = new String(w);
                        if (unvisited.remove(next)) {
                            if (next.equals(endWord)) return depth;
                            q.add(next);
                        }
                    }
                    w[i] = orig;
                }
            }
        }
        return 0;
    }
}
