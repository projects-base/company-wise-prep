import java.util.*;

class Trie {
    private static final class Node {
        final Map<Character, Node> children = new HashMap<>(); // any alphabet
        int passing; // words that go through (or end at) this node
        int ending;  // words that end exactly here
    }

    private final Node root = new Node();

    public Trie() {
    }

    public void insert(String word) {
        Node n = root;
        for (char c : word.toCharArray()) {
            n = n.children.computeIfAbsent(c, k -> new Node());
            n.passing++;
        }
        n.ending++;
    }

    public int countWordsEqualTo(String word) {
        Node n = find(word);
        return n == null ? 0 : n.ending;
    }

    public int countWordsStartingWith(String prefix) {
        Node n = find(prefix);
        return n == null ? 0 : n.passing;
    }

    public void erase(String word) {
        if (countWordsEqualTo(word) == 0) return;
        Node n = root;
        for (char c : word.toCharArray()) {
            Node next = n.children.get(c);
            if (--next.passing == 0) {
                n.children.remove(c); // prune the now-unused branch
                return;
            }
            n = next;
        }
        n.ending--;
    }

    private Node find(String s) {
        Node n = root;
        for (char c : s.toCharArray()) {
            n = n.children.get(c);
            if (n == null) return null;
        }
        return n;
    }
}
